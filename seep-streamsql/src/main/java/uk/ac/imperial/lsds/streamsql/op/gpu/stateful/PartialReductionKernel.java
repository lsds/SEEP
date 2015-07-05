package uk.ac.imperial.lsds.streamsql.op.gpu.stateful;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import uk.ac.imperial.lsds.seep.multi.AggregationType;
import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.TheGPU;
import uk.ac.imperial.lsds.seep.multi.UnboundedQueryBufferFactory;
import uk.ac.imperial.lsds.seep.multi.Utils;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition;
import uk.ac.imperial.lsds.streamsql.expressions.Expression;
import uk.ac.imperial.lsds.streamsql.expressions.ExpressionsUtil;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.elong.LongColumnReference;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;
import uk.ac.imperial.lsds.streamsql.op.gpu.KernelCodeGenerator;

public class PartialReductionKernel implements IStreamSQLOperator, IMicroOperatorCode {

	private static final int threadsPerGroup = 256;
	
	private static int pipelines = Utils.PIPELINE_DEPTH;
	private int [] taskIdx;
	private int [] freeIdx;
	private int [] markIdx; /* Latency mark */
	
	private AggregationType type;
	private FloatColumnReference _the_aggregate;
	
	private LongColumnReference timestampReference;
	
	private int batchSize = -1;
	
	private WindowDefinition windowDefinition = null;
	
	private ITupleSchema inputSchema, outputSchema;
	
	private static String filename = Utils.SEEP_HOME + "/seep-system/clib/templates/PartialReduction.cl";
	
	private int qid;
	
	private int [] intArgs;
	private long [] longArgs;
	
	private int tuples;
	
	private int [] threads;
	private int [] tgs;
	
	/* Local memory sizes */
	private int  inputSize = -1, outputSize;
	
	private int windowPtrsSize;
	
	private byte [] startPtrs;
	private byte [] endPtrs;
	
	byte [] offsetVal;
	
	private int outputTupleSize;
	
	@SuppressWarnings("unused")
	private void printWindowPointers (byte [] startPtrs, byte [] endPtrs, int count) {
		
		ByteBuffer b = ByteBuffer.wrap(startPtrs).order(ByteOrder.LITTLE_ENDIAN);
		ByteBuffer d = ByteBuffer.wrap(  endPtrs).order(ByteOrder.LITTLE_ENDIAN);
		int wid = 0;
		while (b.hasRemaining() && d.hasRemaining()) {
			System.out.println(String.format("w %02d: starts %10d ends %10d", 
				wid, b.getInt(), d.getInt()));
				wid ++;
				if (wid > count)
					break;
		}
	}
	
	public PartialReductionKernel (AggregationType type, FloatColumnReference _the_aggregate, 
			ITupleSchema inputSchema) {
		
		this.type = type;
		this._the_aggregate = _the_aggregate;
		this.inputSchema = inputSchema;
		
		/* Create output schema */
		this.timestampReference = new LongColumnReference(0);
		Expression[] outputAttributes = new Expression[2];
		outputAttributes[0] = this.timestampReference;
		outputAttributes[1] = this._the_aggregate;
		this.outputSchema = 
				ExpressionsUtil.getTupleSchemaForExpressions(outputAttributes);
		
		this.outputTupleSize = outputSchema.getByteSizeOfTuple();
		System.out.println(String.format("[DBG] output tuple size is %d bytes", this.outputTupleSize));
		
		/* Task pipelining internal state */
		
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
	
	public void setWindowDefinition (WindowDefinition windowDefinition) {
		this.windowDefinition = windowDefinition;
	}
	
	public void setup () {
		
		if (batchSize < 0) {
			System.err.println("error: invalid batch size");
			System.exit(1);
		}
		
		this.inputSize = batchSize;
		
		this.tuples = batchSize / inputSchema.getByteSizeOfTuple();
		
		tgs = new int [2];
		tgs[0] = threadsPerGroup; /* This is a constant */
		tgs[1] = threadsPerGroup; /* This is a constant */
		
		threads = new int [2];
		threads[0] = this.tuples;
		threads[1] = this.tuples;
		
		this.outputSize = 32 * 16; /* Another 1MB */
		
		intArgs = new int [5];
		intArgs[0] = tuples;
		intArgs[1] = batchSize;
		intArgs[2] = inputSchema.getByteSizeOfTuple() * tgs[0]; // localInputSize
		
		longArgs = new long [2];
		longArgs[0] = 0; /* Previous pane id */
		longArgs[1] = 0; /* Batch start offset */
		
		this.windowPtrsSize = 65536;
		
		startPtrs = new byte [windowPtrsSize];
		endPtrs   = new byte [windowPtrsSize];
		
		String source = 
			KernelCodeGenerator.getPartialReduction (inputSchema, outputSchema, filename, type, _the_aggregate, 
					windowDefinition);
		System.out.println(source);
		
		qid = TheGPU.getInstance().getQuery(source, 2, 1, 4);
		
		TheGPU.getInstance().setInput(qid, 0, inputSize);
		/* Start and end pointers are also inputs */
		// TheGPU.getInstance().setInput(qid, 1, startPtrs.length);
		// TheGPU.getInstance().setInput(qid, 2,   endPtrs.length);
		
		TheGPU.getInstance().setOutput(qid, 0, windowPtrsSize, 0, 1, 0, 0);
		TheGPU.getInstance().setOutput(qid, 1, windowPtrsSize, 0, 1, 0, 0);
		TheGPU.getInstance().setOutput(qid, 2,             16, 1, 0, 0, 0);
		TheGPU.getInstance().setOutput(qid, 3,     outputSize, 1, 0, 0, 1);
		
		offsetVal = new byte [16];
		
		TheGPU.getInstance().setKernelPartialReduce(qid, intArgs, longArgs);
	}
	
	@Override
	public String toString () {
		final StringBuilder sb = new StringBuilder();
		sb.append(type.asString(_the_aggregate.toString()));
		return sb.toString();
	}
	
	@Override
	public void processData (WindowBatch windowBatch, IWindowAPI api) {
		
		int currentTaskIdx = windowBatch.getTaskId();
		int currentFreeIdx = windowBatch.getFreeOffset();
		int currentMarkIdx = windowBatch.getLatencyMark();
		
		/* Set input */
		byte [] inputArray = windowBatch.getBuffer().array();
		int start = windowBatch.getBufferStartPointer();
		int end   = windowBatch.getBufferEndPointer();
		
		TheGPU.getInstance().setInputBuffer(qid, 0, inputArray, start, end);
		
		/* Create the other two input buffers */
		// windowBatch.initPartialWindowPointers();
		// windowBatch.initPartialWindowPointers(startPtrs, endPtrs);
		/* The start and end pointers are normalised */
		// printWindowPointers(startPtrs, endPtrs, windowBatch.getLastWindowIndex());
		
		// TheGPU.getInstance().setInputBuffer(qid, 1, startPtrs);
		// TheGPU.getInstance().setInputBuffer(qid, 2,   endPtrs);
		
		/* Set output */
		IQueryBuffer outputBuffer = UnboundedQueryBufferFactory.newInstance();
		TheGPU.getInstance().setOutputBuffer(qid, 3, outputBuffer.array());
		
		TheGPU.getInstance().setOutputBuffer(qid, 2, offsetVal);
		
		// threads[0] = tgs[0] * (windowBatch.getLastWindowIndex() + 1); 
		
		/* Set previous pane id */
		if (windowBatch.getWindowDefinition().isRangeBased()) {
			if (windowBatch.getBatchStartPointer() == 0) {
				longArgs[0] = -1;
			} else {
				/* Check the last tuple of the previous batch */
				longArgs[0] = (windowBatch.getTimestamp(windowBatch.getBufferStartPointer() - inputSchema.getByteSizeOfTuple()) / windowBatch.getWindowDefinition().getPaneSize());
			}
		} else {
			if (windowBatch.getBatchStartPointer() == 0) {
				longArgs[0] = -1;
			} else {
				/* Check the last tuple of the previous batch */
				longArgs[0] = ((windowBatch.getBatchStartPointer() / inputSchema.getByteSizeOfTuple()) / windowBatch.getWindowDefinition().getPaneSize()) - 1;
			}
		}
		longArgs[0] = 100;
 		longArgs[1] = windowBatch.getBatchStartPointer();
 		System.out.println("[DBG] previous pane is " + longArgs[0]);
		TheGPU.getInstance().configurePartialReduce(qid, longArgs);
		System.out.println("[DBG] execute");
		TheGPU.getInstance().execute(qid, threads, tgs);
		
		ByteBuffer tmp = ByteBuffer.wrap(offsetVal).order(ByteOrder.LITTLE_ENDIAN);
		System.out.println(String.format("[DBG] batch %10d starts @ %15d: %10d, %10d", taskIdx[0], windowBatch.getBatchStartPointer(), tmp.getLong(),  tmp.getLong()));
		
		/* 
		 * Set position based on the data size returned from the GPU engine
		 */
		
		// outputBuffer.position(windowBatch.getLastWindowIndex() * 16);
		outputBuffer.position(32 * 16);
		// outputBuffer.position(TheGPU.getInstance().getPosition(qid, 0));
		// System.out.println("[DBG] output buffer position is " + outputBuffer.position());
		
		windowBatch.setBuffer(outputBuffer);
		
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
		if (windowBatch.getTaskId() == 1) {
			outputBuffer.close();
			for (int i = 0; i < 10 * 16; i++) {
				System.out.println(String.format("[DBG] %10d,%10.1f,%10d",
				outputBuffer.getByteBuffer().getLong(), outputBuffer.getByteBuffer().getFloat(), outputBuffer.getByteBuffer().getInt()
				));
			}
			System.out.println("Disrupted.");
			System.exit(1);
		}
		*/	
	}
	
	@Override
	public void accept(OperatorVisitor visitor) {
		visitor.visit(this);
	}
	
	@Override
	public void processData (WindowBatch firstWindowBatch, WindowBatch secondWindowBatch, IWindowAPI api) {
		throw new UnsupportedOperationException("PartialReductionKernel operates on a single stream only");
	}
}
