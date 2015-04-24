package uk.ac.imperial.lsds.streamsql.op.gpu.stateful;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.TheGPU;
import uk.ac.imperial.lsds.seep.multi.UnboundedQueryBufferFactory;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;
import uk.ac.imperial.lsds.streamsql.expressions.Expression;
import uk.ac.imperial.lsds.streamsql.expressions.ExpressionsUtil;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.elong.LongColumnReference;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;
import uk.ac.imperial.lsds.streamsql.op.gpu.KernelCodeGenerator;
import uk.ac.imperial.lsds.streamsql.op.stateful.AggregationType;

public class ReductionKernel implements IStreamSQLOperator, IMicroOperatorCode {

	private static final int threadsPerGroup = 128;
	
	private static final int pipelines = 2;
	private int [] taskIdx;
	private int [] freeIdx;
	
	private AggregationType type;
	private FloatColumnReference _the_aggregate;
	
	private LongColumnReference timestampReference;
	
	private int batchSize = -1;
	
	private ITupleSchema inputSchema, outputSchema;
	
	private static String filename = "/Users/akolious/SEEP/seep-system/clib/templates/Reduction.cl";
	
	private int qid;
	
	private int [] args;
	
	private int tuples;
	
	private int [] threads;
	private int [] tgs;
	
	private int ngroups;

	/* Local memory sizes */
	private int  inputSize = -1, outputSize;
	
	private int windowPtrsSize;
	private int localOutputSize;
	
	private byte [] startPtrs;
	private byte [] endPtrs;
	
	private int outputTupleSize;
	
	private void printWindowPointers(byte [] startPtrs, byte [] endPtrs) {
		
		ByteBuffer b = ByteBuffer.wrap(startPtrs).order(ByteOrder.LITTLE_ENDIAN);
		ByteBuffer d = ByteBuffer.wrap(  endPtrs).order(ByteOrder.LITTLE_ENDIAN);
		int wid = 0;
		while (b.hasRemaining() && d.hasRemaining()) {
			System.out.println(String.format("w %02d: starts %10d ends %10d", 
				wid, b.getInt(), d.getInt()));
				wid ++;
		}
	}
	
	public ReductionKernel (AggregationType type, FloatColumnReference _the_aggregate, 
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
		for (int i = 0; i < pipelines; i++) {
			taskIdx[i] = -1;
			freeIdx[i] = -1;
		}
	}
	
	public void setInputSize (int inputSize) {
		this.inputSize = inputSize;
	}
	
	public void setBatchSize (int batchSize) {
		this.batchSize = batchSize;
	}
	
	public void setup () {
		
		if (batchSize < 0) {
			System.err.println("error: invalid batch size");
			System.exit(1);
		}
		if (inputSize < 0) {
			System.err.println("error: invalid input size");
			System.exit(1);
		}
		this.tuples = inputSize / inputSchema.getByteSizeOfTuple();
		
		/* We assign 1 group per window */
		this.ngroups = this.batchSize;
		
		tgs = new int [1];
		tgs[0] = threadsPerGroup; /* This is a constant */
		
		int [] threads = new int [1];
		threads[0] = ngroups * tgs[0];
		
		this.outputSize = ngroups * outputSchema.getByteSizeOfTuple();
		
		this.windowPtrsSize = 4 * ngroups;
		this.localOutputSize = 4 * tgs[0];
		
		args = new int [3];
		args[0] = tuples;
		args[1] = inputSize;
		args[2] = localOutputSize;
		
		startPtrs = new byte [windowPtrsSize];
		endPtrs   = new byte [windowPtrsSize];
		
		String source = 
			KernelCodeGenerator.getReduction (inputSchema, outputSchema, filename, type, _the_aggregate);
		System.out.println(source);
		
		qid = TheGPU.getInstance().getQuery(source, 1, 3, 1);
		
		TheGPU.getInstance().setInput(qid, 0, inputSize);
		/* Start and end pointers are also inputs */
		TheGPU.getInstance().setInput(qid, 1, startPtrs.length);
		TheGPU.getInstance().setInput(qid, 2,   endPtrs.length);
		
		TheGPU.getInstance().setOutput(qid, 0, outputSize, 1, 0, 0, 1);
		
		TheGPU.getInstance().setKernelReduce(qid, args);
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
		
		/* Set input */
		byte [] inputArray = windowBatch.getBuffer().array();
		int start = windowBatch.getBatchStartPointer();
		int end   = windowBatch.getBatchEndPointer();
		
		TheGPU.getInstance().setInputBuffer(qid, 0, inputArray, start, end);
		
		/* Create the other two input buffers */
		
		windowBatch.initWindowPointers(startPtrs, endPtrs);
		/* The start and end pointers are normalised */
		printWindowPointers(startPtrs, endPtrs);
		
		TheGPU.getInstance().setInputBuffer(qid, 1, startPtrs);
		TheGPU.getInstance().setInputBuffer(qid, 2,   endPtrs);
		
		/* Set output */
		IQueryBuffer outputBuffer = UnboundedQueryBufferFactory.newInstance();
		TheGPU.getInstance().setOutputBuffer(qid, 0, outputBuffer.array());
		
		TheGPU.getInstance().execute(qid, threads, tgs);
		
		/* Deprecated: Forward time stamp (for latency measurements purposes) */
		/* outputBuffer.putLong(0, windowBatch.getBuffer().getLong(windowBatch.getBatchStartPointer())); */
		
		/* 
		 * Set position based on the data size returned from the GPU engine
		 */
		outputBuffer.position(outputSize);
		outputBuffer.close();
		
		windowBatch.setBuffer(outputBuffer);
		
		windowBatch.setTaskId     (taskIdx[0]);
		windowBatch.setFreeOffset (freeIdx[0]);
		
		for (int i = 0; i < taskIdx.length - 1; i++) {
			taskIdx[i] = taskIdx [i + 1];
			freeIdx[i] = freeIdx [i + 1];
		}
		taskIdx [taskIdx.length - 1] = currentTaskIdx;
		freeIdx [freeIdx.length - 1] = currentFreeIdx;
		
		api.outputWindowBatchResult(-1, windowBatch);
	}
	
	@Override
	public void accept(OperatorVisitor visitor) {
		visitor.visit(this);
	}
	
	@Override
	public void processData (WindowBatch firstWindowBatch, WindowBatch secondWindowBatch, IWindowAPI api) {
		throw new UnsupportedOperationException("ReductionKernel operates on a single stream only");
	}
}
