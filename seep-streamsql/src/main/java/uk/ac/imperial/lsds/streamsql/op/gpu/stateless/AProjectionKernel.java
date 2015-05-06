package uk.ac.imperial.lsds.streamsql.op.gpu.stateless;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;
import uk.ac.imperial.lsds.seep.multi.TheGPU;
import uk.ac.imperial.lsds.seep.multi.UnboundedQueryBufferFactory;
import uk.ac.imperial.lsds.seep.multi.Utils;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.streamsql.expressions.Expression;
import uk.ac.imperial.lsds.streamsql.expressions.ExpressionsUtil;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.op.gpu.KernelCodeGenerator;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;

public class AProjectionKernel implements IStreamSQLOperator, IMicroOperatorCode {
	
	private static final int threadsPerGroup = 128;
	
	private static int pipelines = Utils.PIPELINE_DEPTH;
	private int [] taskIdx;
	private int [] freeIdx;
	private int [] markIdx; /* Latency mark */
	
	private static int dbg = 1;
	
	private Expression [] expressions;
	private ITupleSchema inputSchema, outputSchema;
	
	/* Floating point expression depth. This is tightly coupled with our synthetic benchmark */
	private int depth = -1;
	
	
	private static String filename = Utils.SEEP_HOME + "/seep-system/clib/templates/Projection.cl";
	
	private int qid;
	
	private int [] args;
	
	private int tuples;
	
	private int [] threads;
	private int [] tgs; /* Threads/group */
	
	/* GPU global and local memory sizes */
	private int  inputSize = -1,  localInputSize;
	private int outputSize = -1, localOutputSize;
	
	public AProjectionKernel(Expression[] expressions, ITupleSchema inputSchema) {
		
		this.expressions = expressions;
		this.inputSchema = inputSchema;
		
		this.outputSchema = 
				ExpressionsUtil.getTupleSchemaForExpressions(expressions);
		
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
	
	public void setInputSize (int inputSize) {
		this.inputSize = inputSize;
	}
	
	public void setDepth (int depth) {
		this.depth = depth;
	}
	
	public void setup() {
		
		/* Configure kernel arguments */
		if (depth < 0) {
			System.err.println("error: invalid projection expression");
			System.exit(1);
		}
		if (inputSize < 0) {
			System.err.println("error: invalid input size");
			System.exit(1);
		}
		this.tuples = inputSize / inputSchema.getByteSizeOfTuple();
		
		this.threads = new int [1];
		threads[0] = tuples;
		
		this.tgs = new int [1];
		tgs[0] = threadsPerGroup;
		
		this.outputSize = tuples * outputSchema.getByteSizeOfTuple();
		
		this.localInputSize  = tgs[0] *  inputSchema.getByteSizeOfTuple();
		this.localOutputSize = tgs[0] * outputSchema.getByteSizeOfTuple();
		
		args = new int [4];
		args[0] = tuples;
		args[1] = inputSize;
		args[2] = localInputSize;
		args[3] = localOutputSize;
		
		String source = KernelCodeGenerator.getProjection(inputSchema, outputSchema, filename, depth);
		if (dbg > 0) {
		System.out.println(source);
		}
		
		qid = TheGPU.getInstance().getQuery(source, 1, 1, 1);
		
		TheGPU.getInstance().setInput (qid, 0,  inputSize);
		TheGPU.getInstance().setOutput(qid, 0, outputSize, 1, 0, 0, 1);
		
		TheGPU.getInstance().setKernelProject (qid, args);
	}
	
	@Override
	public String toString () {
		StringBuilder sb = new StringBuilder();
		sb.append("Projection (");
		for (Expression expr : expressions)
			sb.append(expr.toString() + " ");
		sb.append(")");
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
