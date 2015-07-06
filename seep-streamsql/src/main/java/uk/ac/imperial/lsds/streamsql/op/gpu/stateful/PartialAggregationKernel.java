package uk.ac.imperial.lsds.streamsql.op.gpu.stateful;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

import uk.ac.imperial.lsds.seep.multi.AggregationType;
import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.PartialWindowResults;
import uk.ac.imperial.lsds.seep.multi.PartialWindowResultsFactory;
import uk.ac.imperial.lsds.seep.multi.TheGPU;
import uk.ac.imperial.lsds.seep.multi.ThreadMap;
import uk.ac.imperial.lsds.seep.multi.UnboundedQueryBufferFactory;
import uk.ac.imperial.lsds.seep.multi.Utils;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;
import uk.ac.imperial.lsds.streamsql.expressions.Expression;
import uk.ac.imperial.lsds.streamsql.expressions.ExpressionsUtil;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatExpression;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntExpression;
import uk.ac.imperial.lsds.streamsql.expressions.elong.LongColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.elong.LongExpression;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;
import uk.ac.imperial.lsds.streamsql.op.gpu.KernelCodeGenerator;

public class PartialAggregationKernel implements IStreamSQLOperator, IMicroOperatorCode {
	
	private static final int threadsPerGroup = 128;
	
	private static int kdbg = 0;
	private static boolean debug = true;
	
	private static int pipelines = Utils.PIPELINE_DEPTH;
	private int [] taskIdx;
	private int [] freeIdx;
	private int [] markIdx; /* Latency mark */
	
	private AggregationType type;
	private FloatColumnReference _the_aggregate;
	
	private Expression [] groupBy;
	
	private String selectionClause = null;
	
	private LongColumnReference timestampReference;
	
	private ITupleSchema inputSchema, outputSchema;
	
	private static String filename = Utils.SEEP_HOME + 
			"/seep-system/clib/templates/PartialAggregation.cl";
	
	private int qid;
	
	private int [] args;
	
	private int tuples;
	
	private int [] threads;
	private int [] tgs;
	
	private int batchSize = -1;

	/* Global and local memory sizes */
	private int inputSize = -1, outputSize;
	private int windowPtrsSize;
	
	private int tableSize;
	
	private byte [] startPtrs;
	private byte [] endPtrs;
	
	private int intermediateTupleSize, outputTupleSize;
	
	private byte [] failed, attempts;
	
	private int contentsLength, 
	              failedLength, 
	            attemptsLength;
	
	private static boolean isPowerOfTwo (int n) {
		if (n == 0)
			return false;
		while (n != 1) {
			if (n % 2 != 0)
				return false;
			n = n/2;
		}
		return true;
	}
	
	private void printWindowPointers(byte [] startPtrs, byte [] endPtrs, int count) {
		
		ByteBuffer b = ByteBuffer.wrap(startPtrs).order(ByteOrder.LITTLE_ENDIAN);
		ByteBuffer d = ByteBuffer.wrap(  endPtrs).order(ByteOrder.LITTLE_ENDIAN);
		int wid = 0;
		while (b.hasRemaining() && d.hasRemaining() && wid < count) {
			System.out.println(String.format("w %02d: starts %10d ends %10d", 
				wid, b.getInt(), d.getInt()));
				wid ++;
		}
	}
	
	public PartialAggregationKernel (
			AggregationType type, 
			FloatColumnReference _the_aggregate,
			Expression [] groupBy,
			String selectionClause,
			ITupleSchema inputSchema) {
		
		this.type = type;
		this._the_aggregate = _the_aggregate;
		
		this.groupBy = groupBy;
		
		this.selectionClause = selectionClause;
		
		this.inputSchema = inputSchema;
		
		/* Create output schema */
		this.timestampReference = new LongColumnReference(0);
		
		Expression [] outputAttributes = new Expression [this.groupBy.length + 2];
		/* First attribute is the time stamp */
		outputAttributes[0] = this.timestampReference;
		
		/* Followed by the group-by (composite) key */
		for (int i = 1; i <= this.groupBy.length; i++) {
			
			Expression e = this.groupBy[i - 1];
			
			if (e instanceof   IntExpression) { outputAttributes[i] = new   IntColumnReference(i);
			} else 
			if (e instanceof  LongExpression) { outputAttributes[i] = new  LongColumnReference(i);
			} else 
			if (e instanceof FloatExpression) { outputAttributes[i] = new FloatColumnReference(i);
			} else {
				throw new IllegalArgumentException("error: unknown group by expression type");
			}
		}
		/* Last attribute is the aggregate */
		outputAttributes[this.groupBy.length + 1] = new FloatColumnReference(this.groupBy.length + 1);
		
		this.outputSchema = ExpressionsUtil.getTupleSchemaForExpressions(outputAttributes);
		
		this.outputTupleSize = outputSchema.getByteSizeOfTuple();
		System.out.println(String.format("[DBG] output tuple size is %d bytes", this.outputTupleSize));
		
		this.intermediateTupleSize = KernelCodeGenerator.getIntermediateStructLength(groupBy);
		System.out.println(String.format("[DBG] intermediate tuple size is %d bytes", this.intermediateTupleSize));
		
		taskIdx = new int [pipelines];
		freeIdx = new int [pipelines];
		markIdx = new int [pipelines];
		for (int i = 0; i < pipelines; i++) {
			taskIdx[i] = -1;
			freeIdx[i] = -1;
			markIdx[i] = -1;
		}
	}
	
	public void setBatchSize (int batchSize) {
		this.batchSize = batchSize;
	}
	
	public void setup () {
		
		if (batchSize < 0) {
			System.err.println("error: invalid batch size");
			System.exit(1);
		}
		
		inputSize = batchSize;
		
		tuples = inputSize / inputSchema.getByteSizeOfTuple();
		
		windowPtrsSize = 32768 * 4;
		
		startPtrs = new byte [windowPtrsSize];
		  endPtrs = new byte [windowPtrsSize];
		
		tableSize  = 2048;
		
		/* Determine #threads */
		tgs = new int [2];
		tgs[0] = threadsPerGroup; /* This is a constant; it must be a power of 2 */
		tgs[1] = threadsPerGroup;
		
		threads = new int [2];
		threads[0] = tuples; /* Clear `indices` and `offsets` */
		threads[1] = tuples;
		
		outputSize = 1048576;
		System.out.println("[DBG] output size is " + outputSize + " bytes");
		
		/* Intermediate state */
		contentsLength = outputSize;
		failedLength = 4 * tuples;
		attemptsLength = 4 * tuples;
		
		System.out.println(String.format("[DBG] input.length      = %13d", inputSize));
		System.out.println(String.format("[DBG] contents.length   = %13d", contentsLength));
		System.out.println(String.format("[DBG] failed.length     = %13d", failedLength));
		System.out.println(String.format("[DBG] attempts.length   = %13d", attemptsLength));
		
		if (kdbg > 0) {
		failed     = new byte [    failedLength];
		attempts   = new byte [  attemptsLength];
		}
		
		String source = 
			KernelCodeGenerator.getPartialAggregation(
					inputSchema, 
					outputSchema, 
					filename, 
					type, 
					_the_aggregate, 
					groupBy, 
					selectionClause);
		System.out.println(source);
		
		qid = TheGPU.getInstance().getQuery(source, 2, 3, 3);
		
		TheGPU.getInstance().setInput(qid, 0, inputSize);
		/* Start and end pointers */
		TheGPU.getInstance().setInput(qid, 1, startPtrs.length);
		TheGPU.getInstance().setInput(qid, 2,   endPtrs.length);
		
		int move = (kdbg > 0) ? 0 : 1;
		
		TheGPU.getInstance().setOutput(qid, 0,     failedLength, 0, move, 0, 0);
		TheGPU.getInstance().setOutput(qid, 1,   attemptsLength, 0, move, 0, 0);
		TheGPU.getInstance().setOutput(qid, 2,   contentsLength, 1, move, 0, 1);
		
		args = new int [3];
		args[0] = tuples;
		args[1] = batchSize;
		args[2] = tableSize;
		
		TheGPU.getInstance().setKernelPartialAggregate(qid, args);
	}
	
	@Override
	public String toString () {
		final StringBuilder sb = new StringBuilder();
		sb.append(type.asString(_the_aggregate.toString()));
		return sb.toString();
	}
	
	@Override
	public void processData (WindowBatch windowBatch, IWindowAPI api) {
		
		int workerId = ThreadMap.getInstance().get(Thread.currentThread().getId());
		
		int currentTaskIdx = windowBatch.getTaskId();
		int currentFreeIdx = windowBatch.getFreeOffset();
		int currentMarkIdx = windowBatch.getLatencyMark();
		
		/* Set input */
		byte [] inputArray = windowBatch.getBuffer().array();
		int start = windowBatch.normalise(windowBatch.getBufferStartPointer());
		int end   = windowBatch.normalise(windowBatch.getBufferEndPointer());
		
		if (end > windowBatch.getBuffer().capacity()) {
			System.err.println(String.format("warning: batch end pointer (%d) is greater than its buffer size (%d)", 
				end, windowBatch.getBuffer().capacity()));
			System.exit(1);
		}
		
		TheGPU.getInstance().setInputBuffer(qid, 0, inputArray, start, end);
		
		windowBatch.initPartialWindowPointers();
		windowBatch.initPartialWindowPointers(startPtrs, endPtrs);
		if (debug)
			printWindowPointers (startPtrs, endPtrs, windowBatch.getLastWindowIndex() + 1);
		
		TheGPU.getInstance().setInputBuffer(qid, 1, startPtrs);
		TheGPU.getInstance().setInputBuffer(qid, 2,   endPtrs);
		
		/* Set output */
		if (kdbg > 0) {
		TheGPU.getInstance().setOutputBuffer(qid, 0,     failed);
		TheGPU.getInstance().setOutputBuffer(qid, 1,   attempts);
		}
		
		/* The output */
		IQueryBuffer outputBuffer = UnboundedQueryBufferFactory.newInstance();
		TheGPU.getInstance().setOutputBuffer(qid, 2, outputBuffer.array());
		
		TheGPU.getInstance().execute(qid, threads, tgs);
		
		/* 
		 * Set position based on the data size returned from the GPU engine
		 */
		outputBuffer.position(TheGPU.getInstance().getPosition(qid, 2));
		if (debug)
			System.out.println("[DBG] output buffer position is " + outputBuffer.position());
		
		windowBatch.setBuffer(outputBuffer);
		
		/* Print tuples
		outputBuffer.close();
		int tid = 1;
		while (outputBuffer.hasRemaining()) {
			// Each tuple is 16-bytes long
			System.out.println(String.format("%04d: %2d,%4d,%4.1f", 
			tid++,
			outputBuffer.getByteBuffer().getLong (),
			outputBuffer.getByteBuffer().getInt  (),
			outputBuffer.getByteBuffer().getFloat()
			));
		}
		*/
		windowBatch.setTaskId     (taskIdx[0]);
		windowBatch.setFreeOffset (freeIdx[0]);
		windowBatch.setLatencyMark(markIdx[0]);
		
		for (int i = 0; i < taskIdx.length - 1; i++) {
			taskIdx[i] = taskIdx [i + 1];
			freeIdx[i] = freeIdx [i + 1];
			markIdx[i] = markIdx [i + 1];
		}
		taskIdx [taskIdx.length - 1] = currentTaskIdx;
		freeIdx [freeIdx.length - 1] = currentFreeIdx;
		markIdx [markIdx.length - 1] = currentMarkIdx;
		
		api.outputWindowBatchResult(-1, windowBatch);
		/*
		System.err.println("Disrupted");
		System.exit(-1);
		*/
	}
	
	@Override
	public void accept(OperatorVisitor visitor) {
		visitor.visit(this);
	}
	
	@Override
	public void processData (WindowBatch firstWindowBatch, WindowBatch secondWindowBatch, IWindowAPI api) {
		throw new UnsupportedOperationException("AggregationKernel operates on a single stream only");
	}
}
