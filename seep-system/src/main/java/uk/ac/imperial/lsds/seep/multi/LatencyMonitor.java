package uk.ac.imperial.lsds.seep.multi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class LatencyMonitor {

	long count;
	double min, max, avg;
	
	long timestampReference = 0L;
	
	double latency;
	
	AtomicBoolean active;
	
	ArrayList<Double> measurements;
	
	private static int unpack (int idx, long value) {
		if (idx == 0) { /* left */
			return (int) (value >> 32);
		} else
		if (idx == 1) { /* right value */
			return (int) value;
		} else {
			return -1;
		}
	}
	
	public LatencyMonitor (long timestampReference) {
		
		this.timestampReference = timestampReference;
		
		count = 0;
		
		min = Double.MAX_VALUE;
		max = Double.MIN_VALUE;
		avg = 0.0;
		
		latency = 0.0;
		
		active = new AtomicBoolean(true);
		
		measurements = new ArrayList<Double>();
	}
	
	public void disable () {
		active.set(false);
	}
	
	@Override
	public String toString () {
		
		if (count < 2)
			return null;
		
		avg = latency / count;
		
		return String.format("~= %10.3f >= %10.3f <= %10.3f",
			avg,
			min,
			max
			);
	}
	
	public void monitor (IQueryBuffer buffer, int mark) {
		
		if (! this.active.get()) {
			return ;
		}
		
		double dt = 0;
		/* Check buffer */
		
		long t1 = (long) unpack(0, buffer.getLong(mark));
		
		long t2 = (System.nanoTime() - timestampReference) / 1000L;
		dt = (t2 - t1) / 1000.; /* In milliseconds */

		measurements.add(dt);
		
		latency += dt;
		count += 1;
		
		min = (dt < min) ? dt : min;
		max = (dt > max) ? dt : max;
		return ;
	}

	public void stop() {
		
		active.set(false);
		
		int length = measurements.size();
		
		System.out.println(String.format("[DBG] [LatencyMonitor] %10d measurements", length));
		
		if (length < 1)
			return;
		
		double [] array = new double [length];
		int i = 0;
		for (Double d: measurements)
			array[i++] = d.doubleValue();
		Arrays.sort(array);
		
		System.out.println(String.format("[DBG] [LatencyMonitor] 5th %10.3f 25th %10.3f 50th %10.3f 75th %10.3f 99th %10.3f", 
			evaluateSorted(array,  5D),
			evaluateSorted(array, 25D),
			evaluateSorted(array, 50D),
			evaluateSorted(array, 75D),
			evaluateSorted(array, 99D)
			));
	}
	
	/*
	private double evaluate (final double[] values, final int begin, final int length, final double p) {
			
		if ((p > 100) || (p <= 0)) {
			throw new IllegalArgumentException("invalid quantile value: " + p);
		}
		
		if (length == 0) {
			return Double.NaN;
		}
		
		if (length == 1) {
			return values[begin]; // always return single value for n = 1
		}
		
		double [] sorted = new double[length];
		
		System.arraycopy (values, begin, sorted, 0, length);
		
		Arrays.sort(sorted);
		
		return evaluateSorted (sorted, p);
	}
	*/
	
	private double evaluateSorted(final double[] sorted, final double p) {
		
		double n = sorted.length;
		double pos = p * (n + 1) / 100;
		double fpos = Math.floor(pos);
		int intPos = (int) fpos;
		double dif = pos - fpos;
		
		if (pos < 1) {
			return sorted[0];
		}
		
		if (pos >= n) {
			return sorted[sorted.length - 1];
		}
		
		double lower = sorted[intPos - 1];
		double upper = sorted[intPos];
		return lower + dif * (upper - lower);
	}
}
