package uk.ac.imperial.lsds.streamsql.op.gpu.stateful;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.TheGPU;
import uk.ac.imperial.lsds.seep.multi.UnboundedQueryBufferFactory;
import uk.ac.imperial.lsds.seep.multi.Utils;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;
import uk.ac.imperial.lsds.streamsql.expressions.Expression;
import uk.ac.imperial.lsds.streamsql.expressions.ExpressionsUtil;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.elong.LongColumnReference;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;
import uk.ac.imperial.lsds.streamsql.op.gpu.KernelCodeGenerator;
import uk.ac.imperial.lsds.streamsql.op.stateful.AggregationType;

public class AggregationKernel implements IStreamSQLOperator, IMicroOperatorCode {
	
	/*
	 * This size must be greater or equal to the size of the byte array backing
	 * an input window batch.
	 */
	private static final int _default_input_size = Utils._GPU_INPUT_;
	private static final int _INT_ = 4;
	/*
	 * Operator configuration parameters
	 */
	private static final int THREADS_PER_GROUP = 256;
	
	private static final int PIPELINES = 2;
	
	private static final int _hash_functions = 5;

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
	static int _stash = 100;
	
	private AggregationType type;
	private FloatColumnReference _the_aggregate;
	
	boolean pinned = false;
	
	private LongColumnReference timestampReference;
	
	private int batchSize, windowSize;
	
	private ITupleSchema inputSchema, outputSchema;
	
	private String filename = null;
	
	private int qid;
	
	private int [] args;
	
	private int tuples;
	private int threads, groups;
	
	private int __compact_threads, __compact_threadsPerGroup;

	/* Local memory sizes */
	private int  _input_size, _window_ptrs_size;
	private int _output_size, _local_input_size;
	
	private int __stash_x, __stash_y;
	private int [] x;
	private int [] y;
	
	private ByteBuffer _int_x;
	private ByteBuffer _int_y;
	
	private int iterations;
	
	private int _table_size, _table_slots;
	
	private byte [] startPtrs;
	private byte [] endPtrs;
	
	private int [] taskIdx;
	private int [] freeIdx;
	
	private boolean isPowerOfTwo (int n) {
		if (n == 0)
			return false;
		while (n != 1) {
			if (n % 2 != 0)
				return false;
			n = n/2;
		}
		return true;
	}
	
	private int computeIterations (int n) {
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
	
	public AggregationKernel (AggregationType type, FloatColumnReference _the_aggregate) {
		this (type, _the_aggregate, null);
	}
	
	public AggregationKernel (AggregationType type, FloatColumnReference _the_aggregate, ITupleSchema inputSchema) {
		
		this.type = type;
		this._the_aggregate = _the_aggregate;
		
		this.inputSchema = inputSchema;
		
		/* Create output schema */
		this.timestampReference = new LongColumnReference(0);
		
		Expression[] outputAttributes = new Expression[3];
		outputAttributes[0] = this.timestampReference;
		outputAttributes[1] = this._the_aggregate;
		outputAttributes[2] = new IntColumnReference(2);
		
		this.outputSchema = ExpressionsUtil.getTupleSchemaForExpressions(outputAttributes);
		
		this._input_size = _default_input_size;
	}
	
	public void setSource (String filename) {
		this.filename = filename;
	}
	
	public void setInputSize (int size) {
		this._input_size = size;
	}
	
	public void setInputSchema (ITupleSchema inputSchema) {
		this.inputSchema = inputSchema;
	}
	
	public void setBatchSize (int batchSize) {
		this.batchSize = batchSize;
	}
	
	public void setWindowSize (int windowSize) {
		this.windowSize = windowSize;
	}
	
	public void setup () {
		
		/* Configure kernel arguments */
		
		this.tuples = _input_size / inputSchema.getByteSizeOfTuple();
		
		/* We assign 1 group per window */
		this.groups = this.batchSize;
		this.threads = groups * THREADS_PER_GROUP;
		/*
		*/
		
		this._output_size = tuples * outputSchema.getByteSizeOfTuple();
		
		this._window_ptrs_size = _INT_ * groups;
		
		this.x = new int [_hash_functions];
		this.y = new int [_hash_functions];
		
		this._int_x = ByteBuffer.allocate(_hash_functions * _INT_).order(ByteOrder.LITTLE_ENDIAN);
		this._int_y = ByteBuffer.allocate(_hash_functions * _INT_).order(ByteOrder.LITTLE_ENDIAN);
		
		int [] stash = new int [2];
		constants(x, y, stash);
		this.__stash_x = stash[0];
		this.__stash_y = stash[1];
		
		/* Convert int to bytes */
		for (int i = 0; i < _hash_functions; i++) {
			_int_x.putInt(x[i]);
			_int_y.putInt(y[i]);
		}
		
		iterations = computeIterations (windowSize);
		
		/* Determine an upper bound on # slots/table,
		 * such that we avoid collisions */
		float alpha = _scale_factor;
		if (alpha < _min_space_requirements[_hash_functions])
		{
			throw new IllegalArgumentException("error: invalid hash table size");
		}
		this._table_size  = (int) Math.ceil(this.windowSize * alpha);
		while (((_table_size + _stash) % THREADS_PER_GROUP) != 0)
			_table_size += 1;
		this._table_slots = this._table_size + _stash;
		
		System.out.println(String.format("[DBG] # slots (~2) is %4d\n", _table_slots));
		while (! isPowerOfTwo(_table_slots)) {
			_table_slots += 1;
		}
		System.out.println(String.format("[DBG] # slots (^2) is %4d\n", _table_slots));
		
		__compact_threads = _table_slots / 2;
		__compact_threadsPerGroup = __compact_threads / groups;
		
		this._local_input_size = _INT_ * __compact_threadsPerGroup * 2; /* local buffer size */
		
		System.out.println(String.format("[DBG] %6d tuples", tuples));
		System.out.println(String.format("[DBG] %6d threads", threads));
		System.out.println(String.format("[DBG] %6d groups", groups));
		System.out.println(String.format("[DBG] %6d threads for compaction", __compact_threads));
		System.out.println(String.format("[DBG] %6d threads/group for compaction", __compact_threadsPerGroup));
		System.out.println(String.format("[DBG] %6d bytes local memory", _local_input_size));
		System.out.println(String.format("[DBG] %6d bytes output", _output_size));
		
		
		System.out.println(String.format("[DBG] %6d iterations\n", iterations));
		System.out.println(String.format("[DBG] |t| = %d\n", _table_size));
		
		startPtrs = new byte [_window_ptrs_size];
		endPtrs   = new byte [_window_ptrs_size];
		
		String source = KernelCodeGenerator.getAggregation (inputSchema, outputSchema, filename, type);
		
		// TheGPU.getInstance().init(1);
		qid = TheGPU.getInstance().getQuery(source, 3, 5, 8);
		
		TheGPU.getInstance().setInput (qid, 0,  _input_size);
		TheGPU.getInstance().setInput (qid, 1,  _window_ptrs_size);
		TheGPU.getInstance().setInput (qid, 2,  _window_ptrs_size);
		TheGPU.getInstance().setInput (qid, 3, _INT_ * _hash_functions);
		TheGPU.getInstance().setInput (qid, 4, _INT_ * _hash_functions);
		
	
		TheGPU.getInstance().setOutput (qid, 0, outputSchema.getByteSizeOfTuple() * _table_slots * groups, 0);
		TheGPU.getInstance().setOutput (qid, 1, _INT_ * groups, 0);
		TheGPU.getInstance().setOutput (qid, 2, _INT_ * groups, 0);
		TheGPU.getInstance().setOutput (qid, 3, _INT_ * tuples, 0);
		TheGPU.getInstance().setOutput (qid, 4, _INT_ * _table_slots * groups, 0);
		TheGPU.getInstance().setOutput (qid, 5, _INT_ * _table_slots * groups, 0);
		TheGPU.getInstance().setOutput (qid, 6, _INT_ * groups, 0); /* parts */
		TheGPU.getInstance().setOutput (qid, 7, _output_size, 1);
		
		args = new int [6];
		args[0] = _table_slots * groups; // tuples;
		args[1] = _table_size;
		args[2] = __stash_x;
		args[3] = __stash_y;
		args[4] = iterations;
		args[5] = _local_input_size;
		
		TheGPU.getInstance().setKernelAggregate (qid, args);
		
		taskIdx = new int [PIPELINES];
		freeIdx = new int [PIPELINES];
		for (int i = 0; i < PIPELINES; i++) {
			taskIdx[i] = -1;
			freeIdx[i] = -1;
		}
	}
	
	@Override
	public String toString () {
		final StringBuilder sb = new StringBuilder();
		sb.append(type.asString(_the_aggregate.toString()));
		return sb.toString();
	}
	
	@Override
	public void processData (WindowBatch windowBatch, IWindowAPI api) {
		
		if (! pinned) {
			TheGPU.getInstance().bind(1);
			pinned = true;
		}
		
		int currentTaskIdx = windowBatch.getTaskId();
		int currentFreeIdx = windowBatch.getFreeOffset();
		
		windowBatch.initWindowPointers(startPtrs, endPtrs);
		
		/* Set input and output */
		byte [] inputArray = windowBatch.getBuffer().array();
		int start = windowBatch.getBatchStartPointer();
		int end   = windowBatch.getBatchEndPointer();
		TheGPU.getInstance().setInputBuffer(qid, 0, inputArray, start, end);
		
		/* The output */
		IQueryBuffer outputBuffer = UnboundedQueryBufferFactory.newInstance();
		TheGPU.getInstance().setOutputBuffer(qid, 7, outputBuffer.array());
		
		/* Create the other two input buffers */
		TheGPU.getInstance().setInputBuffer(qid, 1, startPtrs);
		TheGPU.getInstance().setInputBuffer(qid, 2, endPtrs);
		TheGPU.getInstance().setInputBuffer(qid, 3, _int_x.array());
		TheGPU.getInstance().setInputBuffer(qid, 4, _int_y.array());
		
		/* TheGPU.getInstance().execute(qid, threads, THREADS_PER_GROUP); */
		
		TheGPU.getInstance().executeCustom(qid, threads, THREADS_PER_GROUP, __compact_threads, __compact_threadsPerGroup);
		
		outputBuffer.position(_output_size);
		
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
	public void accept(OperatorVisitor visitor) {
		visitor.visit(this);
	}
	
	@Override
	public void processData (WindowBatch firstWindowBatch, WindowBatch secondWindowBatch, IWindowAPI api) {
		throw new UnsupportedOperationException("AggregationKernel operates on a single stream only");
	}
}
