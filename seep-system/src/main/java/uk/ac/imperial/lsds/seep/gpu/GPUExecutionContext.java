package uk.ac.imperial.lsds.seep.gpu;

import com.amd.aparapi.Range;
import com.amd.aparapi.Kernel;
import com.amd.aparapi.ProfileInfo;

import com.amd.aparapi.device.Device;
import com.amd.aparapi.device.OpenCLDevice;

import com.amd.aparapi.opencl.OpenCL;
import com.amd.aparapi.opencl.OpenCL.Resource;

import java.lang.UnsupportedOperationException;

import java.util.List;
import java.util.Arrays;
import java.util.LinkedList;

public class GPUExecutionContext {
	
	@Resource("CongestedSegRel.cl") interface QueryOperator 
		extends OpenCL<QueryOperator> {
	
		public QueryOperator dummyPLQ (
			Range range,
			@Arg ("size")              int       size,
            @GlobalReadOnly ("input")  int  []  input,
            @GlobalWriteOnly("output") int  [] output);
		
		public QueryOperator dummyWLQ (
			Range range,
			@Arg ("size")              int       size,
            @GlobalReadOnly ("input")  int  []  input,
            @GlobalWriteOnly("output") int  [] output);
		
		public QueryOperator dummyAGG (
			Range range,
			@Arg ("size")              int       size,
            @GlobalReadOnly ("input")  int  []  input,
            @GlobalWriteOnly("output") int  [] output);
		
		public QueryOperator plq (
			Range range,
			@Arg("inputs")             int       inputs, /* # inputs */
			@Arg("max_key")            int      max_key, /* # keys */
			@GlobalReadOnly("keys")    int   []    keys,
			@GlobalReadOnly("values")  int   []  values,
			@GlobalReadOnly("offsets") int   [] offsets,
			@GlobalReadOnly("counts")  int   []  counts,
			@GlobalWriteOnly("sum")    int   []     sum,
			@GlobalWriteOnly("N")      int   []       N
		);
		
		public QueryOperator wlq (
			Range range,
			@Arg(  "size")  int    size, 
			@Arg("groups")  int  groups, /* #keys */
			@Arg( "panes")  int   panes, /* #panes/window */
			@GlobalReadOnly("S")  int     [] S,
			@GlobalReadOnly("N")  int     [] N,
			@GlobalWriteOnly("F") int     [] F
		);
		
		public QueryOperator ht_plq (
            Range range,
            @Arg ("size")                 int          size,
            @GlobalReadOnly ("key1")      int  []      key1,
            @GlobalReadOnly ("val1")      int  []      val1,
            @GlobalReadOnly ("offsets")   int  []   offsets,
            @GlobalReadOnly ("counts")    int  []    counts,
            @Arg ("_table_")              int       _table_,
            @GlobalReadOnly ("x")         int  []         x,
            @GlobalReadOnly ("y")         int  []         y,
            @Local ("__local_x")          int  [] __local_x,
            @Local ("__local_y")          int  [] __local_y,
            @Arg ("__stash_x")            int     __stash_x,
            @Arg ("__stash_y")            int     __stash_y,
            @Arg ("iterations")           int    iterations,
            @GlobalReadWrite ("sum")      int  []       sum,
            @GlobalReadWrite ("contents") long []  contents,
            @GlobalWriteOnly ("stashed")  int  []   stashed,
            @GlobalWriteOnly ("failed")   int  []    failed,
            @GlobalWriteOnly ("attempts") int  []  attempts
        );
	}
	
	class GPUMeasurement {
		private static final int __sizeof = 4;
		private static final double __1GB = 1073741824.0;
		private long c, r, w, x, t;
		private long inputs, outputs;
		public GPUMeasurement(long c, long r, long w, long x, long t, long inputs, long outputs) {
			this.c = c;
			this.r = r;
			this.w = w;
			this.x = x;
			this.t = t;
			this.inputs  =  inputs;
			this.outputs = outputs;
		}
		public String toString() {
			return String.format("C %10d T %10d R %10d W %10d X %10d R %5.1f W %5.1f Overall %5.1f Aparapi %5.1f", 
			c, t, r, w, x, getRate(inputs, r), getRate(outputs, w), getRate(inputs + outputs, t), getRate(inputs + outputs, c * 1000));
		}
		public long    getReadTime() { return r; }
		public long   getWriteTime() { return w; }
		public long getExecuteTime() { return x; }
		public long getCPUTime()     { return c; }
		private double getRate(long size, long time) {
			double gb = (double) (size * __sizeof) / __1GB;
			double seconds = (double) time / 1000000.;
			return (gb / seconds);
		}
	}
	
	public static final float MIN_SPACE_REQUIREMENTS [] = {
        Float.MAX_VALUE,
        Float.MAX_VALUE,
        2.01F,
        1.10F,
        1.03F,
        1.02F
    };

    public static int __s_major_hash (int x, int y, int k) {
        long xl = x & 0xffffffffL;
        long yl = y & 0xffffffffL;
        long kl = k & 0xffffffffL;
        long prime  = 2147483647L;
        long result = ((xl ^ kl) + yl) % prime;
        return (int) result;
    }

    public static void constants (Random r, int [] x, int [] y, int [] stash) {
        int prime = 2147483647;
        /* assert (x.length == y.length); */
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

    public static int computeIterations (int n, int size) {
        int result = 7;
        float logn = (float) (Math.log((double) n) / Math.log(2.0));
        return (int) (result * logn);
    }
	
	public static int computeIterationsEmpirically (int n, int size) {
        int result;
        float logn = (float) (Math.log((double) n) / Math.log(2.0));
        float load = (float) n / (float) size;
        float logload = (float) (Math.log((double) load) / Math.log(2.71828183));
        result = (int) (
            4.0 *
            Math.ceil(-1.0 / (0.028255 + 1.1594772 * logload) *
            logn)
        );
        return result;
    }
	
	private final Device dev;
	private final OpenCLDevice device;
	private final QueryOperator q;
	
	/* Per function call state */
	private int __plq_runs;
	private int __wlq_runs;
	private int __agg_runs;
	
	/* Measurements */
	private List<ProfileInfo> plqinfo, wlqinfo;
	private long runs = 0L;
	private long delta;
	private long startTime, dt;
	private long Tx, Tr, Tw, T;
	private void resetGPUTimers() {
		Tx = 0L; Tr = 0L; Tw = 0L; T = 0L; return;
	}
	private LinkedList<GPUMeasurement> plq_measurements;
	private LinkedList<GPUMeasurement> wlq_measurements;
	
	private LinkedList<Long> cpu_task_measurements;
	
	private Range __plq_range;
	private Range __plq_range_ht;
	private Range __wlq_range;
	private Range __agg_range;
	
	private int __panes, __max_keys, __panes_per_window, __max_tuples_per_pane;
	
	private int      __local_size;
	private int   [] __local_keys;
	private int   [] __local_values;
	private int   [] __local_offsets;
	private int   [] __local_count;
	
	private int   [] __S;
	private int   [] __N;
	private int   [] __F;
	
	private int __plq_threads;
	private int __plq_threads_per_group;
	private int __plq_groups;
	private int __plq_outputs;
	
	private int __plq_threads_ht;
	private int __plq_threads_per_group_ht;
	private int __plq_groups_ht;
	private int __plq_outputs_ht;
	
	private int __wlq_threads;
	private int __wlq_threads_per_group;
	private int __wlq_groups;
	private int __wlq_outputs;
	
    private int [] __local_key1; /* Keys */
    private int [] __local_val1; /* Values */
	
	/* Hash table configurations */
    private int _HASH_FUNCTIONS_;
    private int _stash_;
    private float scale;
    private int _table_;
    private int slots;
    private int iter1; /* Iterations */
    private int iter2;
    private int [] failed; /* Metadata */
    private int [] stashed;
    private int [] attempts;
    private int [] x, __local_x; /* Hash constants */
    private int [] y, __local_y;
    private int [] sum;
    private int [] __stash;
    private int __stash_x, __stash_y;

    private long [] contents;
	
	private void clearLocalInputs() {
        Arrays.fill(__local_key1, 0);
        Arrays.fill(__local_key2, 0);
        Arrays.fill(__local_key3, 0);
        Arrays.fill(__local_val1, 0);

        Arrays.fill(__local_offsets, 0);
        Arrays.fill(__local_counts,  0);
        return ;
    }

    private void setLocalInputs(
        int [] k1, int [] v1, int [] offset, int [] count) {
        System.arraycopy(k1, 0, __local_key1, 0, k1.length);
        System.arraycopy(v1, 0, __local_val1, 0, v1.length);

        System.arraycopy(offset, 0, __local_offsets, 0, offset.length);
        System.arraycopy(count,  0, __local_counts,  0,  count.length);
        return ;
    }

    private void clearContents () {
        Arrays.fill(contents, 0xffffffffL << 32);
        Arrays.fill(sum, 0);
    }

    private void clearMetadata () {
        Arrays.fill(failed  , 0);
        Arrays.fill(stashed , 0);
        Arrays.fill(attempts, 0);
    }
	
	public GPUExecutionContext (int panes, int max_keys, int panes_per_window, int max_tuples_per_pane) {
		/* Set internal counters */
		this.__plq_runs = 0;
		this.__wlq_runs = 0;
		this.__agg_runs = 0;
		
		/* Get device */
		dev = Device.best();
		if (! (dev instanceof OpenCLDevice))
			throw new UnsupportedOperationException
			("OpenCL device not found.");
		
		device = (OpenCLDevice) dev;
		System.out.println(device);
		q = device.bind(QueryOperator.class);
		
		__panes = panes;
		__max_keys = max_keys;
		__panes_per_window = panes_per_window;
		__max_tuples_per_pane = max_tuples_per_pane;
		
		__plq_groups = __panes;
		__plq_threads_per_group = 256;
		__plq_threads = set__plq_range (__plq_groups, __plq_threads_per_group);
		__plq_outputs = __panes * __max_keys;
		
		__wlq_groups = __panes;
		__wlq_threads_per_group = max_keys / 4; /* Due to vectorisation. */
		__wlq_threads = set__wlq_range (__wlq_groups, __wlq_threads_per_group);
		__wlq_outputs = __plq_outputs;
		
		__local_size = __panes * __max_tuples_per_pane;
		
		__local_keys = new int [__local_size];
		__local_values = new int [__local_size];
		__local_offsets = new int [panes];
		__local_count = new int [panes];
		
		__S = new int [__plq_outputs]; 
		__N = new int [__plq_outputs]; 
		__F = new int [__wlq_outputs]; 
		
		plq_measurements = new LinkedList<GPUMeasurement>();
		wlq_measurements = new LinkedList<GPUMeasurement>();
		cpu_task_measurements = new LinkedList<Long>();
		
		__local_key1    = new int [__local_size];
        __local_val1    = new int [__local_size];
		
        _HASH_FUNCTIONS_ = 5;
        _stash_ = 101;
        scale = 1.25f;
        /* Check scale requirements */
        if (scale < MIN_SPACE_REQUIREMENTS[_HASH_FUNCTIONS_])
            throw new UnsupportedOperationException
            ("Invalid scale factor.");

        /* Size of hash table */
        _table_ = (int) Math.ceil(max_tuples_per_pane * scale);
        slots = (_table_ + _stash_) * panes; /* One stash per pane */

        iter1 = computeIterations (max_tuples_per_pane, _table_);
        iter2 = computeIterationsEmpirically (max_tuples_per_pane, _table_);

        System.out.println(String.format("[DBG] %2d iterations (constant)" , iter1));
        System.out.println(String.format("[DBG] %2d iterations (empirical)", iter2));
        System.out.println("[DBG] |ht|   = " + _table_);
        System.out.println("[DBG] #slots = " + slots);
		
		failed   = new int [panes];
        stashed  = new int [panes];
        attempts = new int [__local_size];

        x = new int [_HASH_FUNCTIONS_];
        y = new int [_HASH_FUNCTIONS_];

        __local_x = new int [_HASH_FUNCTIONS_];
        __local_y = new int [_HASH_FUNCTIONS_];


        __stash = new int [2];

        sum = new int [slots];
        contents = new long [slots];

        Random random = new Random ();

        /* Generate hash constants */
        constants(random, x, y, __stash);
        __stash_x = __stash[0];
        __stash_y = __stash[0];
		
		__plq_inputs_ht = __local_size;
        __plq_threads_per_group_ht = 128;
        __plq_groups_ht = panes;
        __plq_threads_ht = __plq_groups * __plq_threads_per_group;
        __plq_outputs = slots;

        __plq_range_ht = Range.create(__plq_threads, __plq_threads_per_group);
        /* Every pane is processed in parallel. `256` thread/workgroup */
	}
	
	public void setTaskExecutionTime (long dt) {
		cpu_task_measurements.add(new Long(dt));	
	}
	
	public int set__plq_range (int groups, int groupsize) {
		int threads;
		threads = groups * groupsize;
		__plq_range = Range.create(threads, groupsize);
		return threads;
	}
	
	public int set__wlq_range (int groups, int groupsize) {
		int threads;
		threads = groups * groupsize;
		__wlq_range = Range.create(threads, groupsize);
		return threads;
	}
	
	public void set__agg_range (int groups, int groupsize) {
		int threads;
		threads = groups * groupsize;
		__agg_range = Range.create(threads, groupsize);
	}
	
	public int plq (int [] inputs, int [] outputs) {
		
		return ++__plq_runs;
	}
	
	public int wlq (int [] inputs, int [] outputs) {
		
		return ++__wlq_runs;
	}
	
	/* Execute both PLQ and WLQ level queries */
	public int aggregate () { 
		
		return ++__agg_runs;
	}
	
	public int getResultSize() { return __wlq_outputs; }
	
	public int ht_aggregate (int [] keys, int [] values, int [] offsets, int [] count, int [] results) {
		
		clearLocalInputs();
		setLocalInputs(keys, values, offset, count);
		clearContents();
		clearMetadata();
		
		q.ht_plq(
		__plq_range_ht,
        __local_size,
        __local_key1,
        __local_val1,
        __local_offsets,
        __local_counts,
        _table_,
        x,
        y,
        __local_x,
        __local_y,
        __stash_x,
        __stash_y,
        iter1,
        sum,
        contents,
        stashed,
        failed,
        attempts
        );
	}
	
	public int aggregate (int [] keys, int [] values, int [] offsets, int [] count, int [] result) {
		
		/* keys === segments, values === speed */
		
		/* Local, write-only results */
		/* int [] __S = new int   [__plq_outputs];
		int [] __N = new int   [__plq_outputs];
		int [] __F = new int   [__wlq_outputs]; */
		Arrays.fill(__S, 0);
		Arrays.fill(__N, 0);
		Arrays.fill(__F, 0);
		
		/* Fill in input data */
		Arrays.fill(__local_keys,    0);
		Arrays.fill(__local_values,  0);
		Arrays.fill(__local_offsets, 0);
		Arrays.fill(__local_count,   0);
		
		System.arraycopy(keys,    0, __local_keys,    0,    keys.length);
		System.arraycopy(values,  0, __local_values,  0,  values.length);
		System.arraycopy(offsets, 0, __local_offsets, 0, offsets.length);
		System.arraycopy(count,   0, __local_count,   0,   count.length);
		
		/* Run PLQ query */
		startTime = System.currentTimeMillis();
		q.plq (
			__plq_range,
			keys.length,
			__max_keys,
			__local_keys,
			__local_values,
			__local_offsets,
			__local_count,
			__S,
			__N);
		dt = System.currentTimeMillis() - startTime;
		plqinfo = q.getProfileInfo();
		resetGPUTimers();
		if ((plqinfo != null) && (plqinfo.size() > 0)) {
			for (ProfileInfo p : plqinfo) {
				delta = (p.getEnd() - p.getStart()) / 1000; /* usec */
				String type = String.format("%1s", p.getType());
				if (type.equals("R")) {
					Tr += delta;
				} else
				if (type.equals("X")) {
					Tx += delta;
				} else
				if (type.equals("W")) {
					Tw += delta;
				} else {
					System.err.println("Error: unknown measurement type: " + type);
				}
				T += delta;
			}
		}
		plq_measurements.add(
			new GPUMeasurement(
				dt, Tr, Tw, Tx, T, 
				__local_size + __local_size + __panes + __panes, /* Input */
				__S.length + __N.length
		));
		
		/* Run WLQ query */
		startTime = System.currentTimeMillis();
		q.wlq(
			__wlq_range, 
			__plq_outputs, 
			__max_keys, 
			__panes_per_window,
			__S,
			__N,
			__F);
		dt = System.currentTimeMillis() - startTime;
		wlqinfo = q.getProfileInfo();
		resetGPUTimers();
		if ((wlqinfo != null) && (wlqinfo.size() > 0)) {
			for (ProfileInfo p : wlqinfo) {
				delta = (p.getEnd() - p.getStart()) / 1000; // usec
				String type = String.format("%1s", p.getType());
				if (type.equals("R")) {
					Tr += delta;
				} else
				if (type.equals("X")) {
					Tx += delta;
				} else
				if (type.equals("W")) {
					Tw += delta;
				} else {
					System.err.println("Error: unknown measurement type: " + type);
				}
				T += delta;
			}
		}
		wlq_measurements.add(
			new GPUMeasurement(
				dt, Tr, Tw, Tx, T, 
				__S.length + __N.length,
				__F.length
		));
		
		System.arraycopy(__F, 0, result, 0, __wlq_outputs);
		
		return ++__agg_runs;
	}
	
	public void stats() {
		
		if (__agg_runs <= 0)
			return;
		
		long total_tr, total_tx, total_tw, total_dt;
		long total_task;
		
		/* Process PLQ, WLQ measurements */
		
		System.out.println("GPU PLQ measurements");
		total_tr = 0;
		total_tx = 0;
		total_tw = 0;
		total_dt = 0;
		for (GPUMeasurement m: plq_measurements) {
			System.out.println(m);
			total_tr += m.getReadTime();
			total_tx += m.getExecuteTime();
			total_tw += m.getWriteTime();
			total_dt += m.getCPUTime();
		}
		System.out.println(
		String.format("PLQ Tr %10d Tx %10d Tw %10d CPU %10d", total_tr, total_tx, total_tw, total_dt));
		
		System.out.println("GPU WLQ measurements");
		total_tr = 0;
		total_tx = 0;
		total_tw = 0;
		total_dt = 0;
		for (GPUMeasurement m: wlq_measurements) {
			System.out.println(m);
			total_tr += m.getReadTime();
			total_tx += m.getExecuteTime();
			total_tw += m.getWriteTime();
			total_dt += m.getCPUTime();
		}
		System.out.println(
		String.format("WLQ Tr %10d Tx %10d Tw %10d CPU %10d", total_tr, total_tx, total_tw, total_dt));
		
		System.out.println("CPU task measurements");
		total_task = 0;
		for (Long l: cpu_task_measurements) {
			System.out.println(String.format("C %10d", l.longValue()));
			total_task += l.longValue();
		}
		System.out.println(
		String.format("CPU %10d", total_task));
		
		return;
	}
}
