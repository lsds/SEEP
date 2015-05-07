package uk.ac.imperial.lsds.streamsql.op.gpu.stateless;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;
import uk.ac.imperial.lsds.seep.multi.TheGPU;
import uk.ac.imperial.lsds.seep.multi.UnboundedQueryBufferFactory;
import uk.ac.imperial.lsds.seep.multi.Utils;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.op.gpu.KernelCodeGenerator;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;

public class DummyKernel implements IStreamSQLOperator, IMicroOperatorCode {
	
	private static int pipelines = Utils.PIPELINE_DEPTH;
	private int [] taskIdx;
	private int [] freeIdx;
	private int [] markIdx; /* Latency mark */
	
	private String filename = Utils.SEEP_HOME + "/seep-system/clib/templates/DummyKernel.cl";
	
	private int inputSize = 0;
	
	private ITupleSchema schema;
	
	private int qid;
	
	private int tuples;
	private int [] threads;
	private int [] tgs; /* Threads/group */
	
	public DummyKernel (ITupleSchema schema, int inputSize) {
		
		this.schema = schema;
		this.inputSize = inputSize;
		
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
	
	public void setup() {
		
		this.tuples = inputSize / schema.getByteSizeOfTuple();
		
		this.threads = new int [1];
		threads[0] = tuples;
		
		this.tgs = new int [1];
		tgs[0] = 256;
		
		String source = KernelCodeGenerator.load(filename);
		
		qid = TheGPU.getInstance().getQuery(source, 1, 1, 1);
		TheGPU.getInstance().setInput (qid, 0, inputSize);
		
		TheGPU.getInstance().setOutput(qid, 0, inputSize, 1, 0, 0, 1);
		
		TheGPU.getInstance().setKernelDummy (qid, null);
	}
	
	public int getQueryId () {
		return this.qid;
	}
	
	public int getThreads () {
		if (threads != null)
			return this.threads[0];
		else
			return 0;
	}
	
	public int getThreadsPerGroup () {
		if (tgs != null)
			return this.tgs[0];
		else
			return 0;
	}
	
	@Override
	public String toString () {
		StringBuilder sb = new StringBuilder();
		sb.append("DummyKernel ()");
		return sb.toString();
	}
	
	@Override
	public void processData (WindowBatch windowBatch, IWindowAPI api) {
		
		int currentTaskIdx = windowBatch.getTaskId();
		int currentFreeIdx = windowBatch.getFreeOffset();
		int currentMarkIdx = windowBatch.getLatencyMark();
		
		/* Set input */
		byte [] inputArray = windowBatch.getBuffer().array();
		int start = windowBatch.getBatchStartPointer();
		int end   = windowBatch.getBatchEndPointer();
		
		TheGPU.getInstance().setInputBuffer(qid, 0, inputArray, start, end);
		
		/* Set output */
		IQueryBuffer outputBuffer = UnboundedQueryBufferFactory.newInstance();
		TheGPU.getInstance().setOutputBuffer(qid, 0, outputBuffer.array());
		
		/* Execute */
		TheGPU.getInstance().execute(qid, threads, tgs);
		
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
	}
	
	@Override
	public void accept(OperatorVisitor visitor) {
		visitor.visit(this);
	}
	
	@Override
	public void processData(WindowBatch firstWindowBatch,
			WindowBatch secondWindowBatch, IWindowAPI api) {
		throw new UnsupportedOperationException("ProjectionKernel operates on a single stream only");
	}
}
