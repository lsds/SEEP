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
import java.util.Random;
import java.util.LinkedList;

public class GPUExecutionContext {
	
	@Resource("Operator.cl") interface QueryOperator 
		extends OpenCL<QueryOperator> {
		
		public QueryOperator noop (
			Range range,
			@Arg ("size")               int         size,
			@GlobalReadOnly ("input")   float []   input,
			@GlobalReadOnly ("offsets") int   [] offsets,
			@GlobalReadOnly ("counts")  int   []  counts,
			@GlobalWriteOnly("output")  float []  output
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
		public long     getCPUTime() { return c; }
		private double getRate(long size, long time) {
			double gb = (double) (size * __sizeof) / __1GB;
			double seconds = (double) time / 1000000.;
			return (gb / seconds);
		}
		public double    getReadRate() { return getRate(inputs,           r); }
		public double   getWriteRate() { return getRate(         outputs, w); }
		public double getOverallRate() { return getRate(inputs + outputs, t); }
	}
	
	private final Device dev;
	private final OpenCLDevice device;
	private final QueryOperator q;
	
	/* Per function call state */
	private int __noop_runs;
	
	/* Measurements */
	private List<ProfileInfo> info;
	private long runs = 0L;
	private long delta;
	private long startTime, dt;
	private long Tx, Tr, Tw, T;
	private void resetGPUTimers() {
		Tx = 0L; Tr = 0L; Tw = 0L; T = 0L; return;
	}
	
	private LinkedList<GPUMeasurement> noop_measurements;
	private LinkedList<Long> cpu_task_measurements;
	
	private Range __noop_range;
	
	private int __panes, __max_tuples_per_pane;
	
	private int      __local_size;
	private float [] __local_attrib1;
	private float [] __local_output;
	private int   [] __local_offsets;
	private int   [] __local_count;
	
	private int __noop_threads;
	private int __noop_threads_per_group;
	private int __noop_groups;
	
	private void clearLocalState () {
		
		Arrays.fill(__local_attrib1, 0);
		Arrays.fill(__local_output,  0);
		Arrays.fill(__local_offsets, 0);
		Arrays.fill(__local_count,   0);
		return ;
	}

    private void setLocalInputs (float [] a1, int [] offset, int [] count) {
        
		System.arraycopy(a1,     0, __local_attrib1, 0,     a1.length);
		System.arraycopy(offset, 0, __local_offsets, 0, offset.length);
		System.arraycopy(count,  0, __local_count,   0,  count.length);
		return ;
	}

	public GPUExecutionContext (int panes, int max_tuples_per_pane) {
		/* Set internal counters */
		this.__noop_runs = 0;
		
		/* Get device */
		dev = Device.best();
		if (! (dev instanceof OpenCLDevice))
			throw new UnsupportedOperationException
			("OpenCL device not found.");
		
		device = (OpenCLDevice) dev;
		System.out.println(device);
		q = device.bind(QueryOperator.class);
		
		__panes = panes;
		__max_tuples_per_pane = max_tuples_per_pane;
		
		__noop_groups = __panes;
		__noop_threads_per_group = __max_tuples_per_pane;
		__noop_threads = set__noop_range (__noop_groups, __noop_threads_per_group);
		
		__local_size = __panes * __max_tuples_per_pane;
		
		__local_attrib1 = new float [__local_size];
		__local_output  = new float [__local_size];
		__local_offsets = new int   [panes];
		__local_count   = new int   [panes];
		
		noop_measurements = new LinkedList<GPUMeasurement>();
		cpu_task_measurements = new LinkedList<Long>();
	}
	
	public void setTaskExecutionTime (long dt) {
		cpu_task_measurements.add(new Long(dt));	
	}
	
	public int set__noop_range (int groups, int groupsize) {
		int threads;
		threads = groups * groupsize;
		__noop_range = Range.create(threads, groupsize);
		return threads;
	}
	
	public int getResultSize() { return __local_size; }
	
	public int noop (float [] attribute, int [] offsets, int [] count, float [] result) {
		
		/* Run Noop query */
		startTime = System.currentTimeMillis();
		
		clearLocalState();
		setLocalInputs(attribute, offsets, count);
		
		q.noop (
			__noop_range,
			__local_size,
			__local_attrib1,
			__local_offsets,
			__local_count,
			__local_output
		);
		
		dt = System.currentTimeMillis() - startTime;
		
		info = q.getProfileInfo();
		resetGPUTimers();
		if ((info != null) && (info.size() > 0)) {
			for (ProfileInfo p : info) {
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
		noop_measurements.add(
			new GPUMeasurement(
				dt, Tr, Tw, Tx, T, 
				__local_size + __panes + __panes, /* Input */
				__local_size
		));
		
		System.arraycopy(__local_output, 0, result, 0, __local_size);
		return ++__noop_runs;
	}
	
	public void stats () {
		
		if (__noop_runs <= 0)
			return;
		
		double Tr, Tx, Tw;
		double Rr, Rw, Rt; /* Read, write and overall rate */
		double sz;
		Tr = 0.;
		Tx = 0.;
		Tw = 0.;
		Rr = 0.;
		Rw = 0.;
		Rt = 0.;
		sz = 0.;
		for (GPUMeasurement m: noop_measurements) {
			Tr += (double) m.getReadTime();
			Tx += (double) m.getExecuteTime();
			Tw += (double) m.getWriteTime();
			Rr += m.getReadRate();
			Rw += m.getWriteRate();
			Rt += m.getOverallRate();
			sz += 1D;
		}
		System.out.println(
		String.format("R %10.3f us/task (%10.3f GB/s) W %10.3f us/task (%10.3f GB/s) X %10.3f us/task: %10.3f GB/s",
		Tr/sz, Rr/sz, Tw/sz, Rw/sz, Tx/sz, Rt/sz
		));
		/* Clear list contents */
		noop_measurements.clear();
	}
	
	public void analytics () {
		
		if (__noop_runs <= 0)
			return;
		
		long total_tr, total_tx, total_tw, total_dt;
		long total_task;
		
		System.out.println("GPU noop measurements");
		total_tr = 0;
		total_tx = 0;
		total_tw = 0;
		total_dt = 0;
		for (GPUMeasurement m: noop_measurements) {
			System.out.println(m);
			total_tr += m.getReadTime();
			total_tx += m.getExecuteTime();
			total_tw += m.getWriteTime();
			total_dt += m.getCPUTime();
		}
		System.out.println(
		String.format("No-op Tr %10d Tx %10d Tw %10d CPU %10d", total_tr, total_tx, total_tw, total_dt));
		
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
