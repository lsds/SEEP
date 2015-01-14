package uk.ac.imperial.lsds.streamsql.op.gpu.stateless;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;
import uk.ac.imperial.lsds.seep.multi.UnboundedQueryBufferFactory;
import uk.ac.imperial.lsds.seep.multi.Utils;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.streamsql.expressions.Expression;
import uk.ac.imperial.lsds.streamsql.expressions.ExpressionsUtil;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.op.gpu.TheGPU;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;

public class AProjectionKernel implements IStreamSQLOperator, IMicroOperatorCode {
	/*
	 * This size must be greater or equal to the size of the byte array backing
	 * an input window batch.
	 */
	private static final int _default_input_size  = Utils._GPU_INPUT_;
	private static final int _default_output_size = Utils._GPU_OUTPUT_;
	
	private Expression[] expressions;
	private ITupleSchema inputSchema, outputSchema;
	
	private String filename = null;
	
	private int qid;
	
	private int [] args;
	
	private int tuples;
	private int threads;
	private int _thread_group_;
	/* Local memory sizes */
	private int _input_size, _output_size;
	
	private int last, secondLast;
	private int lastFree, secondLastFree;
	
	private byte [] input;
	
	public AProjectionKernel(Expression[] expressions, ITupleSchema inputSchema,
			String filename) {
		this.expressions = expressions;
		this.inputSchema = inputSchema;
		this.outputSchema = ExpressionsUtil
				.getTupleSchemaForExpressions(expressions);
		
		this.filename = filename;		
		
		setup();
	}
	
	public AProjectionKernel (Expression[] expressions) {
		this(expressions, null, null);
	}
	
	public AProjectionKernel (Expression expression) {
		this(new Expression[] { expression }, null, null);
	}
	
	private String load (String filename) {
		File file = new File(filename);
		try {
			byte [] bytes = Files.readAllBytes(file.toPath());
			return new String (bytes, "UTF8");
		} catch (FileNotFoundException e) {
			System.err.println(String.format("error: file %s not found", filename));
		} catch (IOException e) {
			System.err.println(String.format("error: cannot read file %s", filename));
		}
		return null;
	}
	
	private void setup() {
		
		input = new byte [_default_input_size];
		ByteBuffer b = ByteBuffer.wrap(input);
		while (b.hasRemaining())
			b.putInt(1);
		b.clear();
		
		/* Configure kernel arguments */
		this.tuples = _default_input_size / inputSchema.getByteSizeOfTuple();
		
		this.threads = tuples;
		this._thread_group_ = 128;
		
		this._input_size  = _thread_group_ *  inputSchema.getByteSizeOfTuple();
		this._output_size = _thread_group_ * outputSchema.getByteSizeOfTuple();
		
		/* Arguments are: tuples, bytes, _input, _output */
		args = new int [4];
		args[0] = tuples;
		args[1] = _default_input_size;
		args[2] =  _input_size;
		args[3] = _output_size;
		
		String source = load (filename);
		
		TheGPU.getInstance().init(1);
		qid = TheGPU.getInstance().getQuery(source, 1, 1, 1);
		TheGPU.getInstance().setInput (qid, 0,  _default_input_size);
		TheGPU.getInstance().setOutput(qid, 0, _default_output_size, 1);
		TheGPU.getInstance().setKernelProject (qid, args);
		
		last = -1;
		secondLast = -1;
		
		lastFree = -1;
		secondLastFree = -1;
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
				
		int tmp = windowBatch.getTaskId();
		int tmpFree = windowBatch.getFreeOffset();
		//System.out.println("[DBG] GPU operator executes task " + tmp);
		
		/* Set input and output */
		byte [] inputArray = windowBatch.getBuffer().array();
		int start = windowBatch.getBatchStartPointer();
		int end = windowBatch.getBatchEndPointer();
		TheGPU.getInstance().setInputBuffer(inputArray, start, end);
		// TheGPU.getInstance().setInputBuffer(input);
		IQueryBuffer outputBuffer = UnboundedQueryBufferFactory.newInstance();
		TheGPU.getInstance().setOutputBuffer(outputBuffer.array());
		
		TheGPU.getInstance().execute(qid, threads, _thread_group_);
		
		windowBatch.setBuffer(outputBuffer);
		// windowBatch.setTaskId(secondLast);
		// windowBatch.setFreeOffset(secondLastFree);
		secondLast = last;
		last = tmp;
		secondLastFree = lastFree;
		lastFree = tmpFree;
		api.outputWindowBatchResult(-1, windowBatch);
	}
	
	@Override
	public void accept(OperatorVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public void processData(WindowBatch firstWindowBatch,
			WindowBatch secondWindowBatch, IWindowAPI api) {
		throw new UnsupportedOperationException("ProjectionKernel is single input operator and does not operate on two streams");
	}
}
