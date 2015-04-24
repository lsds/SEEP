package uk.ac.imperial.lsds.streamsql.op.gpu.stateless;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;
import uk.ac.imperial.lsds.seep.multi.TheGPU;
import uk.ac.imperial.lsds.seep.multi.UnboundedQueryBufferFactory;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.op.gpu.KernelCodeGenerator;
import uk.ac.imperial.lsds.streamsql.predicates.IPredicate;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;

public class ASelectionKernel implements IStreamSQLOperator, IMicroOperatorCode {
	
	private static final int threadsPerGroup = 256;
	private static final int tuplesPerThread = 2;
	
	private static final int pipelines = 2;
	private int [] taskIdx; /* Control output based on depth of pipeline */
	private int [] freeIdx;
	
	private IPredicate predicate;
	private ITupleSchema schema;
	
	private static String filename = "/Users/akolious/SEEP/seep-system/clib/templates/Selection.cl";
	
	private String customFunctor = null;
	
	int qid; /* Query id */
	
	private int [] args; /* Arguments to the selection kernel */
	
	private int tuples, tuples_;
	
	private int [] threads;
	private int [] tgs;
	
	private int ngroups;
	
	private int inputSize = -1, outputSize;
	
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
	
	public ASelectionKernel (IPredicate predicate, ITupleSchema schema) {
		
		this.predicate = predicate;
		this.schema = schema;
		
		/* Task pipelining internal state */
		
		taskIdx = new int [pipelines];
		freeIdx = new int [pipelines];
		for (int i = 0; i < pipelines; i++) {
			taskIdx[i] = -1;
			freeIdx[i] = -1;
		}
	}
	
	public IPredicate getPredicate () {
		return this.predicate;
	}
	
	public void setInputSize (int inputSize) {
		this.inputSize = inputSize;
	}
	
	public void setCustomFunctor (String customFunctor) {
		this.customFunctor = customFunctor;
	}
	
	public void setup () {
		
		if (inputSize < 0) {
			System.err.println("error: invalid input size");
			System.exit(1);
		}
		this.outputSize = inputSize;
		
		this.tuples = this.inputSize / schema.getByteSizeOfTuple();
		this.tuples_= this.tuples;
		while (! isPowerOfTwo(tuples_))
			tuples_ += 1;
		
		System.out.println(String.format("[DBG] #tuples (~2) = %6d", tuples ));
		System.out.println(String.format("[DBG] #tuples (^2) = %6d", tuples_));
		
		threads = new int [2];
		threads[0] = tuples_ / tuplesPerThread;
		threads[1] = tuples_ / tuplesPerThread;
		
		tgs = new int [2];
		tgs[0] = threadsPerGroup;
		tgs[1] = threadsPerGroup;
		
		ngroups = threads[0] / tgs[0];
		
		args = new int[3];
		args[0] = inputSize;
		args[1] = tuples;
		args[2] = 4 * tgs[0] * tuplesPerThread;
		
		String source = 
			KernelCodeGenerator.getSelection (schema, schema, predicate, filename, customFunctor);
		System.out.println(source);
		
		qid = TheGPU.getInstance().getQuery(source, 2, 1, 4);
		
		TheGPU.getInstance().setInput(qid, 0, inputSize);
		
		TheGPU.getInstance().setOutput(qid, 0, 4 * tuples_, 0, 0, 1, 0); /* flags     */
		TheGPU.getInstance().setOutput(qid, 1, 4 * tuples_, 0, 1, 0, 0); /* offsets   */
		TheGPU.getInstance().setOutput(qid, 2, 4 * ngroups, 0, 1, 0, 0); /* partitions */
		TheGPU.getInstance().setOutput(qid, 3,  outputSize, 1, 0, 0, 1);
		
		TheGPU.getInstance().setKernelSelect (qid, args);
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
		
		/* Set output */
		IQueryBuffer outputBuffer = UnboundedQueryBufferFactory.newInstance();
		TheGPU.getInstance().setOutputBuffer(qid, 3, outputBuffer.array());
		
		TheGPU.getInstance().execute(qid, threads, tgs);
		
		/* Deprecated: Forward time stamp (for latency measurements purposes) */
		/* outputBuffer.putLong(0, windowBatch.getBuffer().getLong(windowBatch.getBatchStartPointer())); */
		
		/* 
		 * Set position based on the data size returned from the GPU engine
		 */
		outputBuffer.position(TheGPU.getInstance().getPosition(qid, 3));
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
	public void accept (OperatorVisitor visitor) {
		visitor.visit(this);
	}
	
	@Override
	public String toString () {
		final StringBuilder sb = new StringBuilder();
		sb.append("Selection (");
		sb.append(predicate.toString());
		sb.append(")");
		return sb.toString();
	}
	
	@Override
	public void processData(WindowBatch first, WindowBatch second, IWindowAPI api) {
		
		throw new UnsupportedOperationException("SelectionKernel operates on a single stream only");
	}
}
