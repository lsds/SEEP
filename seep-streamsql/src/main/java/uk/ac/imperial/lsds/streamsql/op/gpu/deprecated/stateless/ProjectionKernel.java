package uk.ac.imperial.lsds.streamsql.op.gpu.deprecated.stateless;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

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
import uk.ac.imperial.lsds.streamsql.op.gpu.deprecated.Kernel;
import uk.ac.imperial.lsds.streamsql.op.gpu.deprecated.KernelCodeGenerator;
import uk.ac.imperial.lsds.streamsql.op.gpu.deprecated.KernelDevice;
import uk.ac.imperial.lsds.streamsql.op.gpu.deprecated.KernelInvocationHandler;
import uk.ac.imperial.lsds.streamsql.op.gpu.deprecated.KernelOperator;
import uk.ac.imperial.lsds.streamsql.op.gpu.deprecated.KernelStatistics;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;

import com.amd.aparapi.Range;

public class ProjectionKernel implements IStreamSQLOperator, IMicroOperatorCode {
	
	/*
	 * This size must be greater or equal to the size of the byte array backing
	 * an input window batch.
	 */
	private static final int _default_input_size = 1048576;
	private static final int _default_output_size = 1048576;
	
	private Expression[] expressions;
	private ITupleSchema inputSchema, outputSchema;
	
	private String filename = null;
	
	KernelOperator operator;
	
	private KernelDevice gpu;
	private List<Kernel> kernels;
	private KernelInvocationHandler<KernelOperator> handler;
	private Range range;
	
	private Object [] args;
	private byte [] input, output; /* Raw GPU input & output */
	private byte [] _input, _output; /* Local state */
	private int tuples;
	private int _thread_group_;
	
	private KernelStatistics statistics;
	
	public ProjectionKernel(Expression[] expressions, ITupleSchema inputSchema,
			String filename) {
		this.expressions = expressions;
		this.inputSchema = inputSchema;
		this.outputSchema = ExpressionsUtil
				.getTupleSchemaForExpressions(expressions);
		
		this.filename = filename;
		
		this.statistics = 
			new KernelStatistics("project", _default_input_size, _default_output_size);
		
		setup();
	}
	
	public ProjectionKernel (Expression[] expressions) {
		this(expressions, null, null);
	}
	
	public ProjectionKernel (Expression expression) {
		this(new Expression[] { expression }, null, null);
	}
	
	@SuppressWarnings("unchecked")
	private void setup() {

		this.gpu = new KernelDevice();
		this.kernels = this.generateKernels();
		this.operator = gpu.bind(KernelOperator.class, kernels);
		this.handler = (KernelInvocationHandler<KernelOperator>) Proxy.getInvocationHandler(operator);
		
		/* Configure kernel arguments */
		this.tuples = _default_input_size / inputSchema.getByteSizeOfTuple();
		
		this._thread_group_ = 128;

		this.range = Range.create(this.tuples, _thread_group_);
		
		this.input  = new byte[ _default_input_size];
		this.output = new byte[_default_output_size];
		
		this._input  = new byte [(_thread_group_) * inputSchema.getByteSizeOfTuple()];
		this._output = new byte [(_thread_group_) * inputSchema.getByteSizeOfTuple()];
		
		/* Arguments are: range, tuples, bytes, input, output */
		args = new Object[7];
		args[0] = range;
		args[1] = tuples;
		args[2] = _default_input_size; /* #bytes */
		args[3] = input;
		args[4] = output;
		args[5] = _input;
		args[6] = _output;
		
		/* handler.configureProjectionKernelOperator(args); */
	}
	
	public byte[] getOutput() {
		return this.output;
	}
	
	private List<Kernel> generateKernels () {
		List<Kernel> kernels = new ArrayList<Kernel>();
		if (this.filename != null)
			kernels.add(KernelCodeGenerator.getProjection(filename));
		else
			kernels.add(KernelCodeGenerator.getProjection());
		return kernels;
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
		/* Copy input */
		windowBatch.getBuffer().appendBytesTo(
				windowBatch.getBatchStartPointer(), 
				windowBatch.getBatchEndPointer(), 
				input);
		
		/* Execute kernel */
		handler.call("project", args);
		
		/*
		 * long start = System.nanoTime();
		 * handler.invokeProjectionKernelOperator (args);
		 * long dt = System.nanoTime() - start;
		 * System.out.println("dt = " + dt + " ns");
		 */
		
		/* Collect measurements
		 * statistics.collect(this.operator.getProfileInfo());
		 * statistics.print(); 
		 */
		
		IQueryBuffer outputBuffer = UnboundedQueryBufferFactory.newInstance();
		outputBuffer.put(output);
		windowBatch.setBuffer(outputBuffer);
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
