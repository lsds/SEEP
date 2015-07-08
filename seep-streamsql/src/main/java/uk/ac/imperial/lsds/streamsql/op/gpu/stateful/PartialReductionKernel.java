package uk.ac.imperial.lsds.streamsql.op.gpu.stateful;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
	
	private static int kdbg = 0;
	private static boolean debug = true;
	
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
	
	private static String filename = Utils.SEEP_HOME + 
			"/seep-system/clib/templates/PartialReduction.cl";
	
	private int qid;
	
	private int  []  intArgs;
	private long [] longArgs;
	
	private int tuples;
	
	private int [] threads;
	private int [] tgs;
	
	/* Local memory sizes */
	private int  inputSize = -1, outputSize;
	
	private int windowPtrsSize;
	
	private int offsetLength, windowCntLength;
	
	private byte [] startPtrs;
	private byte [] endPtrs;
	
	private byte [] offset, windowCounts;
	
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
		System.out.println(String.format("[DBG] output tuple size is %d bytes", 
				this.outputTupleSize));
		
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
		
		this.windowPtrsSize = tuples * 4; /* `tuples` windows x sizeof(int) */
		
		outputSize = Utils._UNBOUNDED_BUFFER_;
		
		offsetLength    = 16;          /* 2 longs */
		windowCntLength = 16 + 4;      /* 4 integers, +1 that is the mark */
		
		System.out.println(String.format("[DBG]         input.length = %13d",       inputSize));
		System.out.println(String.format("[DBG] startPointers.length = %13d",  windowPtrsSize));
		System.out.println(String.format("[DBG]   endPointers.length = %13d",  windowPtrsSize));
		System.out.println(String.format("[DBG]        offset.length = %13d",    offsetLength));
		System.out.println(String.format("[DBG]  windowCounts.length = %13d", windowCntLength));
		System.out.println(String.format("[DBG]        output.length = %13d",      outputSize));
		
		tgs = new int [4];
		tgs[0] = threadsPerGroup; /* This is a constant */
		tgs[1] = threadsPerGroup; 
		tgs[2] = threadsPerGroup; 
		tgs[3] = threadsPerGroup; 
		
		threads = new int [4];
		threads[0] = this.tuples;
		threads[1] = this.tuples;
		threads[2] = this.tuples;
		threads[3] = this.tuples;
		
		intArgs = new int [3];
		intArgs[0] = tuples;
		intArgs[1] = batchSize;
		intArgs[2] = 4 * tgs[0];
		
		longArgs = new long [2];
		longArgs[0] = 0; /* Previous pane id   */
		longArgs[1] = 0; /* Batch start offset */
		
		if (kdbg > 0) {
			startPtrs    = new byte [ windowPtrsSize];
			endPtrs      = new byte [ windowPtrsSize];
			offset       = new byte [   offsetLength];
			windowCounts = new byte [windowCntLength];
		}
		
		String source = 
			KernelCodeGenerator.getPartialReduction (inputSchema, outputSchema, filename, type, _the_aggregate, 
					windowDefinition);
		System.out.println(source);
		
		qid = TheGPU.getInstance().getQuery(source, 4, 1, 5);
		
		TheGPU.getInstance().setInput(qid, 0, inputSize);
		
		TheGPU.getInstance().setOutput(qid, 0, windowPtrsSize, 0, 1, 0, 0);
		TheGPU.getInstance().setOutput(qid, 1, windowPtrsSize, 0, 1, 0, 0);
		TheGPU.getInstance().setOutput(qid, 2,   offsetLength, 0, 1, 0, 0);
		TheGPU.getInstance().setOutput(qid, 3,windowCntLength, 0, 0, 1, 0);
		TheGPU.getInstance().setOutput(qid, 4,     outputSize, 1, 0, 0, 1);
		
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
		
		int workerId = ThreadMap.getInstance().get(Thread.currentThread().getId());
		
		int currentTaskIdx = windowBatch.getTaskId();
		int currentFreeIdx = windowBatch.getFreeOffset();
		int currentMarkIdx = windowBatch.getLatencyMark();
		
		/* Set input */
		byte [] inputArray = windowBatch.getBuffer().array();
		int start = windowBatch.getBufferStartPointer();
		int end   = windowBatch.getBufferEndPointer();
		
		TheGPU.getInstance().setInputBuffer(qid, 0, inputArray, start, end);
		
		if (kdbg > 0) {
			TheGPU.getInstance().setOutputBuffer(qid, 0, startPtrs);
			TheGPU.getInstance().setOutputBuffer(qid, 1,   endPtrs);
			TheGPU.getInstance().setOutputBuffer(qid, 2,    offset);
		}
		
		/* Set output */
		TheGPU.getInstance().setOutputBuffer(qid, 3, windowCounts);
		
		IQueryBuffer outputBuffer = UnboundedQueryBufferFactory.newInstance();
		TheGPU.getInstance().setOutputBuffer(qid, 4, outputBuffer.array());
		
		/* Set previous pane id */
		if (windowBatch.getWindowDefinition().isRangeBased()) {
			if (windowBatch.getBatchStartPointer() == 0) {
				longArgs[0] = -1;
			} else {
				/* Check the last tuple of the previous batch */
				longArgs[0] = (
						windowBatch.getTimestamp(windowBatch.getBufferStartPointer() - inputSchema.getByteSizeOfTuple()) / 
						windowBatch.getWindowDefinition().getPaneSize()
						);
			}
		} else {
			if (windowBatch.getBatchStartPointer() == 0) {
				longArgs[0] = -1;
			} else {
				/* Check the last tuple of the previous batch */
				longArgs[0] = (
						(windowBatch.getBatchStartPointer() / inputSchema.getByteSizeOfTuple()) / 
						windowBatch.getWindowDefinition().getPaneSize()) - 1;
			}
		}
		longArgs[1] = windowBatch.getBatchStartPointer();
 		
		TheGPU.getInstance().executePartialReduce(qid, threads, tgs, longArgs);
		
		ByteBuffer b2 = ByteBuffer.wrap(windowCounts).order(ByteOrder.LITTLE_ENDIAN);
		int  nclosing = b2.getInt();
		int  npending = b2.getInt();
		int ncomplete = b2.getInt();
		int  nopening = b2.getInt();
		if (debug)
			System.out.println(String.format("[DBG] offset %6d/%6d/%6d/%6d", 
				nclosing, npending, ncomplete, nopening));
		
		/* 
		 * Set position based on the data size returned from the GPU engine
		 */
		outputBuffer.position(TheGPU.getInstance().getPosition(qid, 6));
		/*
		if (debug)
			System.out.println("[DBG] output buffer position is " + outputBuffer.position());
		*/
		outputBuffer.close();
		
		/* Split output buffer into partial results */
		PartialWindowResults closing  = PartialWindowResultsFactory.newInstance(workerId);
		PartialWindowResults pending  = PartialWindowResultsFactory.newInstance(workerId);
		PartialWindowResults complete = PartialWindowResultsFactory.newInstance(workerId);
		PartialWindowResults opening  = PartialWindowResultsFactory.newInstance(workerId);
		
		IQueryBuffer  closingOutputBuffer = UnboundedQueryBufferFactory.newInstance();
		IQueryBuffer  pendingOutputBuffer = UnboundedQueryBufferFactory.newInstance();
		IQueryBuffer completeOutputBuffer = UnboundedQueryBufferFactory.newInstance();
		IQueryBuffer  openingOutputBuffer = UnboundedQueryBufferFactory.newInstance();
		
		 closing.setBuffer( closingOutputBuffer);
		 pending.setBuffer( pendingOutputBuffer);
		complete.setBuffer(completeOutputBuffer);
		 opening.setBuffer( openingOutputBuffer);
		
		/* Copy closing  windows */
		int offset = 0;
		for (int i = 0; i < nclosing; i++) {
			closing.increment();
			closingOutputBuffer.getByteBuffer().put(outputBuffer.array(), offset, outputTupleSize);
			offset += outputTupleSize;
		}
		/* Copy pending  windows */
		if (npending  > 0) {
			pending.increment();
			pendingOutputBuffer.getByteBuffer().put(outputBuffer.array(), offset, outputTupleSize);
			offset += outputTupleSize;
		}
		/* Copy complete windows */		
		for (int i = 0; i < ncomplete; i++) {
			if (i == 0) {
				/* Debug... */
			}
			complete.increment();
			completeOutputBuffer.getByteBuffer().put(outputBuffer.array(), offset, outputTupleSize);
			offset += outputTupleSize;
		}
		/* Copy opening  windows */	
		for (int i = 0; i < nopening; i++) {
			opening.increment();
			openingOutputBuffer.getByteBuffer().put(outputBuffer.array(), offset, outputTupleSize);
			offset += outputTupleSize;
		}
		
		windowBatch.setTaskId     (taskIdx[0]);
		windowBatch.setFreeOffset (freeIdx[0]);
		windowBatch.setLatencyMark(markIdx[0]);
		
		windowBatch.setSchema(outputSchema);
		
		/* At the end of processing, set window batch accordingly */
		windowBatch.setClosing  ( closing);
		windowBatch.setPending  ( pending);
		windowBatch.setComplete (complete);
		windowBatch.setOpening  ( opening);
		
		outputBuffer.release();
		
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
			System.err.println("Disrupted");
			System.exit(-1);
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
