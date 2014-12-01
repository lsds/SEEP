package uk.ac.imperial.lsds.streamsql.op.gpu.stateless;

import java.lang.reflect.Proxy;

import java.util.ArrayList;
import java.util.List;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;

import uk.ac.imperial.lsds.streamsql.expressions.ExpressionsUtil;
import uk.ac.imperial.lsds.streamsql.expressions.Expression;

import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;

import uk.ac.imperial.lsds.streamsql.op.gpu.Kernel;
import uk.ac.imperial.lsds.streamsql.op.gpu.KernelDevice;
import uk.ac.imperial.lsds.streamsql.op.gpu.KernelCodeGenerator;
import uk.ac.imperial.lsds.streamsql.op.gpu.KernelInvocationHandler;

import com.amd.aparapi.Range;
import com.amd.aparapi.ProfileInfo;
import com.amd.aparapi.opencl.OpenCL;
import com.amd.aparapi.device.Device;
import com.amd.aparapi.device.OpenCLDevice;

public class ProjectionKernel implements IStreamSQLOperator, IMicroOperatorCode {
	
	private Expression [] expressions;
	private ITupleSchema outputSchema;
	
	IProjectionKernel operator;
	
	private String source;
	private Device dev;
	private OpenCLDevice device;
	private KernelDevice gpu;
	private List<Kernel> kernels;
	private KernelInvocationHandler handler;
	
	public ProjectionKernel (Expression [] expressions) {
		this.expressions  = expressions;
		this.outputSchema = ExpressionsUtil.getTupleSchemaForExpressions(expressions);
		
		setup();
	}
	
	public ProjectionKernel (Expression expression) {
		this.expressions  = new Expression [] { expression };
		this.outputSchema = ExpressionsUtil.getTupleSchemaForExpressions(expressions);
		
		setup();
	}
	
	@SuppressWarnings("rawtypes")
	private void setup () {
		dev = Device.best();
		if (! (dev instanceof OpenCLDevice)) {
		}
		device = (OpenCLDevice) dev;
		System.out.println(device);
		gpu = new KernelDevice (device);
		kernels = this.generateKernels ();
		operator = gpu.bind (IProjectionKernel.class, kernels);
		handler = (KernelInvocationHandler) Proxy.getInvocationHandler(operator);	
	}
	
	private List<Kernel> generateKernels () {
		List<Kernel> kernels = new ArrayList<Kernel>();
		kernels.add(KernelCodeGenerator.getProjection ());
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
		/* */
	}
	
	@Override
	public void accept (OperatorVisitor visitor) {
		visitor.visit(this);
	}	
}
