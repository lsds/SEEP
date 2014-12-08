package uk.ac.imperial.lsds.streamsql.op.gpu.stateless;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;

import com.amd.aparapi.Range;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;

import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;

import uk.ac.imperial.lsds.streamsql.op.gpu.Kernel;
import uk.ac.imperial.lsds.streamsql.op.gpu.KernelDevice;
import uk.ac.imperial.lsds.streamsql.op.gpu.KernelOperator;
import uk.ac.imperial.lsds.streamsql.op.gpu.OperatorStatistics;
import uk.ac.imperial.lsds.streamsql.op.gpu.KernelCodeGenerator;
import uk.ac.imperial.lsds.streamsql.op.gpu.KernelInvocationHandler;

import uk.ac.imperial.lsds.streamsql.predicates.IPredicate;

import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;

public class SelectionKernel implements IStreamSQLOperator, IMicroOperatorCode {
	
	private IPredicate predicate;
	private ITupleSchema schema;
	
	private String filename = null;
	
	/*
	 * This size must be greater or equal to the size of the byte array backing
	 * an input window batch.
	 */
	private static final int _default_size = 1048576;
	/*
	 * Operator configuration parameters
	 */
	private static final int THREADS_PER_GROUP = 256;
	private static final int TUPLES_PER_THREAD = 1;
	
	KernelOperator operator;
	
	private KernelDevice gpu;
	private List<Kernel> kernels;
	private KernelInvocationHandler<KernelOperator> handler;
	private Range range;
	
	private Object [] args0, args1;
	private byte [] input, output; /* Raw GPU input & output */
	private int [] buffer; /* Local state */
	private int [] flags, offsets, pivots;
	private int tuples;
	
	private int threads, groups;
	private int bundles, _bundle;
	
	private OperatorStatistics statistics;
	
	public SelectionKernel (IPredicate predicate, ITupleSchema schema,
		String filename) {
		
		this.predicate = predicate;
		this.schema = schema;
		this.filename = filename;
		
		this.statistics  = new OperatorStatistics();
		
		setup ();
	}
	
	public SelectionKernel (IPredicate predicate, ITupleSchema schema) {
		this(predicate, schema, null);
	}
	
	public SelectionKernel (IPredicate predicate) {
		this(predicate, null, null);
	}
	
	public IPredicate getPredicate () {
		return this.predicate;
	}
	
	@SuppressWarnings("unchecked")
	private void setup () {
		
		this.gpu = new KernelDevice();
		this.kernels = this.generateKernels();
		this.operator = gpu.bind(KernelOperator.class, kernels);
		this.handler = (KernelInvocationHandler<KernelOperator>) Proxy.getInvocationHandler(operator);
		
		/* Configure kernel arguments */
		this.tuples  = _default_size / schema.getByteSizeOfTuple();
		this.bundles = TUPLES_PER_THREAD;
		this._bundle = THREADS_PER_GROUP * TUPLES_PER_THREAD;
		this.threads = tuples / TUPLES_PER_THREAD;
		this.groups  = threads/ THREADS_PER_GROUP;
		
		System.out.println(String.format("[DBG] %6d threads", threads));
		System.out.println(String.format("[DBG] %6d threads/group", THREADS_PER_GROUP));
		System.out.println(String.format("[DBG] %6d groups", groups));
		System.out.println(String.format("[DBG] %6d tuples/group (bundle size)", _bundle));
		System.out.println(String.format("[DBG] %6d bundles", bundles));

		this.range = Range.create(this.tuples, THREADS_PER_GROUP);
		
		this.input  = new byte[_default_size];
		this.output = new byte[_default_size];
		this.flags  = new int  [tuples];
		/* Temporary results from scan operation */
		this.offsets = new int [tuples];
		/* Local store */
		this.buffer = new int [THREADS_PER_GROUP];
		/* Inclusive sum per work group */
		this.pivots = new int [groups];
		clearState();
		
		args0 = new Object[9];
		args0[0] = range;
		args0[1] = _default_size; /* #bytes */
		args0[2] = tuples; 
		args0[3] = _bundle;
		args0[4] = bundles;
		args0[5] = input;
		args0[6] = flags;
		args0[7] = offsets;
		args0[8] = buffer;
		
		/* Setup statistics */
		statistics.add("selectKernel", 
				input.length, 
				4 * (flags.length + offsets.length));
		
		args1 = new Object[10];
		args1[0] = range;
		args1[1] = _default_size; /* #bytes */
		args1[2] = tuples; 
		args1[3] = _bundle;
		args1[4] = bundles;
		args1[5] = input;
		args1[6] = flags;
		args1[7] = offsets;
		args1[8] = pivots;
		args1[9] = output;
		
		/* Setup statistics */
		statistics.add("compactKernel", 
				input.length + 4 * (flags.length + offsets.length + pivots.length), 
				output.length);
	}
	
	private void clearState () {
		Arrays.fill(input, (byte) 0);
		Arrays.fill(output, (byte) 0);
		Arrays.fill(flags, 1);
		Arrays.fill(offsets, 0);
		Arrays.fill(buffer, 0);
		Arrays.fill(pivots, 0);
	}
	
	private List<Kernel> generateKernels () {
		if (this.filename != null)
			return KernelCodeGenerator.getSelection(filename);
		else
			return KernelCodeGenerator.getSelection();
	}

	@Override
	public void processData (WindowBatch windowBatch, IWindowAPI api) {
		
		windowBatch.getBuffer().appendBytesTo(
				windowBatch.getBatchStartPointer(), 
				windowBatch.getBatchEndPointer(), 
				input);
		
		/* Execute `select & scan` kernel */
		handler.call("selectKernel", args0);
		/* Collect measurements */
		statistics.collect("selectKernel", this.operator.getProfileInfo());
		
		/* Populate `sum` array with an inclusive scan 
		 * 
		 * This function could also be run on the GPGPU.
		 * However, only a single workgroup can be used.
		 * 
		 */
		pivots[0] = 0;
		for (int i = 1; i < groups; i++) {
			int idx = i * _bundle - 1;
			pivots[i] = offsets[idx] + 1 + pivots[i - 1];
		}
		
		/* Compact results, based on scan results */
		handler.call("compactKernel", args1);
		/* Collect measurements */
		statistics.collect("compactKernel", this.operator.getProfileInfo());
		/* Debug mode */
		statistics.print();
	}
	
	public byte[] getOutput() {
		return this.output;
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
}
