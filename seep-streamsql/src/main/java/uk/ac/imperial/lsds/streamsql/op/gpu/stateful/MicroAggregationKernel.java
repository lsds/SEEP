package uk.ac.imperial.lsds.streamsql.op.gpu.stateful;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.amd.aparapi.Range;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition;
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
	
	private static final int THREADS_PER_GROUP = 256;
	private static final int TUPLES_PER_THREAD = 1;
	
	/*
	 * If the window is range-based, we need an estimate on the maximum
	 * number of tuples per time unit (typically, a second).
	 */
	private static final int _tuples_per_second = 1024;
	
	/* 
	 * Number of functions for GPGPU cuckoo hashing 
	 */
	private static final int _hash_functions = 5;
	
	/* 
	 * Consider a batch size of `x` tuples. We scale the size of the hashtable 
	 * table by a constant factor (typically, by 1.25) to ensure that there is
	 * enough space to insert all `x` tuples with minimal collisions.
	 * 
	 * Furthermore, we allocate a small table (a `stash`) to insert the tuples 
	 * that the cuckoo hashing algorithm has failed to insert in the hashtable. 
	 * 
	 * If this attempt fails as well, then there is an error, and the hashtable
	 * must be scaled accordingly. 
	 */
	private static final float _scale_factor = 1.25F;
	
	private static final float _min_space_requirements [] = {
		Float.MAX_VALUE,
		Float.MAX_VALUE,
		2.01F,
		1.10F,
		1.03F,
		1.02F
	};
	
	/* Default stash table size (# tuples) */
	private static final int _stash = 100;
	
	private AggregationType type;
	private FloatColumnReference _the_aggregate;
	private Expression [] groupBy;
	private Selection having;
	
	private LongColumnReference timestampReference;
	
	private ITupleSchema inputSchema, outputSchema;
	
	private WindowDefinition window;
	private int batchSize;
	
	private String filename = null;
	
	KernelOperator operator;
	
	private KernelDevice gpu;
	private List<Kernel> kernels;
	private KernelInvocationHandler<KernelOperator> handler;
	private Range range;
	
	/* Local state */
	
	private int tpp; /* # tuples/ pane */
	private int tpt; /* # tuples/table */
	private int spt; /* # slots /table */
	private int tpg; /* # tuples/group */
	
	private long ppb; /* # panes/batch */
	/* Scale factor */
	private float alpha;
	
	/* Maximum number of attempts to insert a tuple into a hash table */
	private int iterations;
	
	/* Hash constants for cuckoo hashing algorithm */
	private int [] x = new int [_hash_functions];
	private int [] y = new int [_hash_functions];
	/* Hash constants for inserting into the stash */
	private int [] __stash = new int [2];
	private int __stash_x = 0, __stash_y = 0;
	
	/* Number of failed and stashed tuples per hashtable.
	 * 
	 * Assert that for each i, failed[i] == 0. 
	 */
	private int [] failed;
	private int [] stashed;
	
	private int tuples; /* Total number of input tuples */
	private int inputSize, outputSize; /* # input and output bytes */
	
	/* GPU work group configuration parameters */
	private int threads, groups;
	private int bundles, _bundle;
	
	private byte [] input, output; /* Raw GPU input & output */
	
	private Object [] args0;
	
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
	}
	
	public void setSource (String filename) {
		this.filename = filename;
	}
	
	public void setWindowDefinition (WindowDefinition window) {
		this.window = window;
	}
	
	public void setBatchSize (int batchSize) {
		this.batchSize = batchSize;
	}
	
	@SuppressWarnings("unchecked")
	public void setup() {

		this.gpu = new KernelDevice();
		this.kernels = this.generateKernels();
		this.operator = gpu.bind(KernelOperator.class, kernels);
		this.handler = (KernelInvocationHandler<KernelOperator>) 
				Proxy.getInvocationHandler(operator);
		
		/* Panes per batch */
		this.ppb = window.panesPerSlide() * (this.batchSize - 1) + 
				window.numberOfPanes();
		
		/* Determine an upper bound on # tuples/pane */
		this.tpp = (int) window.getPaneSize();
		if (window.isRangeBased())
			this.tpp *= _tuples_per_second;
		
		this.tpg = TUPLES_PER_THREAD * THREADS_PER_GROUP;
		
		/* Determine an upper bound on # slots/table,
		 * such that we avoid collisions */
		this.alpha = _scale_factor;
		if (this.alpha < _min_space_requirements[_hash_functions])
		{
			throw new IllegalArgumentException("error: invalid hash table size");
		}
		this.tpt = (int) Math.ceil(this.tpg * this.alpha);
		this.spt = this.tpt + _stash;
		
		iterations = computeIterations (this.tpg, this.tpt);
		
		/* Generate hash function constants */
		constants (x, y, __stash);
		__stash_x = __stash[0];
		__stash_y = __stash[1];
		
		/* Input batch, in terms of # tuples and # bytes */
		this.tuples = (int) (this.ppb * this.tpp);
		this.inputSize = this.tuples * inputSchema.getByteSizeOfTuple();
		
		/* Configure kernel arguments */
		this.threads = tuples / TUPLES_PER_THREAD;
		this.bundles = TUPLES_PER_THREAD;
		this._bundle = THREADS_PER_GROUP * TUPLES_PER_THREAD;
		this.groups  = threads / THREADS_PER_GROUP;
		
		System.out.println(String.format("[DBG] %10d panes/batch", ppb));
		System.out.println(String.format("[DBG] %10d tuples/pane", tpp));
		System.out.println(String.format("[DBG] %10d tuples", tuples));
		System.out.println(String.format("[DBG] %10d slots/table", spt));
		System.out.println(String.format("[DBG] %10d attempts/insert", iterations));
		
		System.out.println(String.format("[DBG] %10d threads", threads));
		System.out.println(String.format("[DBG] %10d threads/group", THREADS_PER_GROUP));
		System.out.println(String.format("[DBG] %10d groups", groups));
		System.out.println(String.format("[DBG] %10d tuples/group (bundle size)", _bundle));
		System.out.println(String.format("[DBG] %10d bundles", bundles));
		
		/* We allocate one hash table per group */
		this.outputSize = groups * spt * outputSchema.getByteSizeOfTuple();
		
		this.input  = new byte [ inputSize];
		this.output = new byte [outputSize];
		
		/* Failed and stashed tuples per work group */
		failed  = new int [groups];
		stashed = new int [groups];
		
		args0 = new Object [14];
		args0[ 0] = range;
		args0 [1] = inputSize; /* #bytes */
		args0[ 2] = tuples; 
		args0[ 3] = _bundle;
		args0[ 4] = bundles;
		args0[ 5] = x;
		args0[ 6] = y;
		args0[ 7] = __stash_x;
		args0[ 8] = __stash_y;
		args0[ 9] = iterations;
		args0[10] = input;
		args0[11] = output;
		args0[12] = failed;
		args0[13] = stashed;
	}
	
	private int computeIterations (int n, int size) {
		int result = 7;
		float logn = (float) (Math.log(n) / Math.log(2.0));
		return (int) (result * logn);
	}
	
	private void constants (int [] x, int [] y, int [] stash) {
		Random r = new Random();
		int prime = 2147483647;
		assert (x.length == y.length);
		int i, n = x.length;
		int t;
		for (i = 0; i < n; i++) {
			t = r.nextInt(prime);
			x[i] = (1 > t ? 1 : t);
			y[i] = r.nextInt(prime) % prime;
		}
		/* Stash hash constants */
		stash[0] = Math.max(1, r.nextInt(prime)) % prime;
		stash[1] = r.nextInt(prime) % prime;
	}
	
	private List<Kernel> generateKernels () {
		List<Kernel> kernels = new ArrayList<Kernel>();
		if (this.filename != null)
			kernels.add(KernelCodeGenerator.getMicroAggregation(filename));
		else
			kernels.add(KernelCodeGenerator.getMicroAggregation());
		return kernels;
	}
	
	@Override
	public void processData (WindowBatch windowBatch, IWindowAPI api) {
		switch (type) {
			case COUNT:
			case   SUM:
			case   AVG:
			case   MAX:
			case   MIN:
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
