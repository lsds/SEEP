package uk.ac.imperial.lsds.streamsql.op.gpu.stateful;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import com.amd.aparapi.Range;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;
import uk.ac.imperial.lsds.streamsql.expressions.Expression;
import uk.ac.imperial.lsds.streamsql.expressions.ExpressionsUtil;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.elong.LongColumnReference;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.op.gpu.Kernel;
import uk.ac.imperial.lsds.streamsql.op.gpu.KernelCodeGenerator;
import uk.ac.imperial.lsds.streamsql.op.gpu.KernelDevice;
import uk.ac.imperial.lsds.streamsql.op.gpu.KernelInvocationHandler;
import uk.ac.imperial.lsds.streamsql.op.gpu.KernelOperator;
import uk.ac.imperial.lsds.streamsql.op.stateful.AggregationType;
import uk.ac.imperial.lsds.streamsql.op.stateless.Selection;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;

public class MicroAggregationKernel implements IStreamSQLOperator, IMicroOperatorCode {
	
	private AggregationType type;
	private FloatColumnReference _the_aggregate;
	private Expression [] groupBy;
	private Selection having;
	
	private LongColumnReference timestampReference;
	
	private ITupleSchema inputSchema, outputSchema;
	
	private String filename = null;
	
	KernelOperator operator;
	
	private KernelDevice gpu;
	private List<Kernel> kernels;
	private KernelInvocationHandler<KernelOperator> handler;
	private Range range;
	
	public MicroAggregationKernel (AggregationType type, FloatColumnReference _the_aggregate) {
		this(type, _the_aggregate, new Expression[0], null, null);
	}
	
	public MicroAggregationKernel (AggregationType type, 
		FloatColumnReference _the_aggregate, Expression [] groupBy, 
		Selection having, ITupleSchema inputSchema) {
		
		this.type = type;
		this._the_aggregate = _the_aggregate;
		this.groupBy = groupBy;
		this.having = having;
		this.inputSchema = inputSchema;
		/* Create output schema */
		this.timestampReference = new LongColumnReference(0);
		int _len = this.groupBy.length + 2; /* +1 for timestamp, +1 for value */
		Expression [] out = new Expression[_len]; 
		out[0] = this.timestampReference;
		for (int i = 0; i < this.groupBy.length; i++)
			out[i + 1] = this.groupBy[i];
		out[_len - 1] = this._the_aggregate;
		this.outputSchema = ExpressionsUtil.getTupleSchemaForExpressions(out);
		
		setup ();
	}
	
	public void setSource (String filename) {
		this.filename = filename;
	}
	
	private List<Kernel> generateKernels () {
		List<Kernel> kernels = new ArrayList<Kernel>();
		if (this.filename != null)
			kernels.add(KernelCodeGenerator.getMicroAggregation(filename));
		else
			kernels.add(KernelCodeGenerator.getMicroAggregation());
		return kernels;
	}
	
	@SuppressWarnings("unchecked")
	private void setup() {

		this.gpu = new KernelDevice();
		this.kernels = this.generateKernels();
		this.operator = gpu.bind(KernelOperator.class, kernels);
		this.handler = (KernelInvocationHandler<KernelOperator>) Proxy.getInvocationHandler(operator);
		
		/* ... */
	}
	
	@Override
	public void processData (WindowBatch windowBatch, IWindowAPI api) {
		switch (type) {
			case COUNT:
			case SUM:
			case AVG:
				_processData (windowBatch, api);
				break;
			case MAX:
			case MIN:
				_processData (windowBatch, api);
				break;
			default:
				break;
		}
	}
	
	private void _processData (WindowBatch windowBatch, IWindowAPI api) {
		return ;
	}
	
	@Override
	public void accept (OperatorVisitor visitor) {
		visitor.visit(this);
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(type.asString(_the_aggregate.toString()));
		return sb.toString();
	}
}
