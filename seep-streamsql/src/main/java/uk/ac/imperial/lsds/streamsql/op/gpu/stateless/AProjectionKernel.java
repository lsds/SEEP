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
	/*
	 * This size must be greater or equal to the size of the byte array backing
	 * an input window batch.
	 */
	private static final int _default_input_size  = Utils._GPU_INPUT_;
	
	private static final int THREADS_PER_GROUP = 128;
	
	private static final int PIPELINES = 2;

	private boolean pinned = false;
	
	private Expression[] expressions;
	private ITupleSchema inputSchema, outputSchema;
	
	private String filename = null;
	
	private int qid;
	
	private int [] args;
	
	private int tuples;
	private int threads;
	private int tgs; /* Threads/group */
	
	/* Local memory sizes */
	private int  _input_size,  _local_input_size;
	private int _output_size, _local_output_size;
	
	private int [] taskIdx;
	private int [] freeIdx;
	
	public AProjectionKernel(Expression[] expressions, ITupleSchema inputSchema,
			String filename) {
		this.expressions = expressions;
		this.inputSchema = inputSchema;
		this.outputSchema = ExpressionsUtil
				.getTupleSchemaForExpressions(expressions);
		
		this.filename = filename;
		
		// setup();
	}
	
	public AProjectionKernel (Expression[] expressions) {
		this(expressions, null, null);
	}
	
	public AProjectionKernel (Expression expression) {
		this(new Expression[] { expression }, null, null);
	}
	
	public void setInputSize (int inputSize) {
		this._input_size = inputSize;
	}
	
	public void setup() {
		
		/* Configure kernel arguments */
		// this._input_size = _default_input_size;
		
		System.out.println("In projection kernel, the input size is " + _input_size);
		this.tuples = _input_size / inputSchema.getByteSizeOfTuple();
		
		this.threads = tuples;
		this.tgs = THREADS_PER_GROUP;
		
		this._output_size = tuples * outputSchema.getByteSizeOfTuple();
		
		this._local_input_size  = tgs *  inputSchema.getByteSizeOfTuple();
		this._local_output_size = tgs * outputSchema.getByteSizeOfTuple();
		
		/* Arguments are: tuples, bytes, _local_input_size, _local_output_size */
		args = new int [4];
		args[0] = tuples;
		args[1] = _input_size;
		args[2] =  _local_input_size;
		args[3] = _local_output_size;
		
		String source = KernelCodeGenerator.getProjection(inputSchema, outputSchema, filename);
		// System.out.println(source);
		
		// TheGPU.getInstance().init(1);
		qid = TheGPU.getInstance().getQuery(source, 1, 1, 1);
		TheGPU.getInstance().setInput (qid, 0,  _input_size);
		TheGPU.getInstance().setOutput(qid, 0, _output_size, 1);
		TheGPU.getInstance().setKernelProject (qid, args);
		
		taskIdx = new int [PIPELINES];
		freeIdx = new int [PIPELINES];
		for (int i = 0; i < PIPELINES; i++) {
			taskIdx[i] = -1;
			freeIdx[i] = -1;
		}
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
		
		if (! pinned) {
			// TheGPU.getInstance().bind(2);
			pinned = true;
		}
				
		int currentTaskIdx = windowBatch.getTaskId();
		int currentFreeIdx = windowBatch.getFreeOffset();
		
		/* Set input and output */
		byte [] inputArray = windowBatch.getBuffer().array();
		int start = windowBatch.getBatchStartPointer();
		int end   = windowBatch.getBatchEndPointer();
		
		// System.out.println("[DBG] First timestamp in operator is " + windowBatch.getBuffer().getByteBuffer().getLong(start));
		
		TheGPU.getInstance().setInputBuffer(qid, 0, inputArray, start, end);
		IQueryBuffer outputBuffer = UnboundedQueryBufferFactory.newInstance();
		
		// System.out.println("[DBG] output buffer capacity is " + outputBuffer.capacity());
		
		TheGPU.getInstance().setOutputBuffer(qid, 0, outputBuffer.array());
		TheGPU.getInstance().execute(qid, threads, tgs);
		
		// System.out.println("[DBG] Returned first timestamp is " + outputBuffer.getByteBuffer().getLong(0));
		
		windowBatch.setBuffer(outputBuffer);
		
		windowBatch.setTaskId     (taskIdx[0]);
		windowBatch.setFreeOffset (freeIdx[0]);
		
		// System.out.println("Running " + currentTaskIdx);
		
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
	public void processData(WindowBatch firstWindowBatch,
			WindowBatch secondWindowBatch, IWindowAPI api) {
		throw new UnsupportedOperationException("ProjectionKernel operates on a single stream only");
	}
}
