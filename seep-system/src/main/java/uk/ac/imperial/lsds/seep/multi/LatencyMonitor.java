package uk.ac.imperial.lsds.seep.multi;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class LatencyMonitor {

	long count;
	double min, max, avg, std;
	
	double latency, latencySquared;
	
	AtomicBoolean active;
	
	ArrayList<Double> measurements;
	
	public LatencyMonitor () {
		count = 0;
		
		min = Double.MAX_VALUE;
		max = Double.MIN_VALUE;
		avg = 0.0;
		std = 0.0;
		
		latency = 0.0;
		latencySquared = 0.0;
		
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
//		std = Math.sqrt(
//			(latencySquared - (latency * latency)) / count / (count - 1)
//		);
		
		return String.format("~= %10.3f += %10.3f >= %10.3f <= %10.3f",
			avg,
			std,
			min,
			max
			);
	}
	
	public void monitor (IQueryBuffer buffer, int taskid) {
		
		if (! this.active.get())
			return ;
		
		// System.out.println("[DBG] In latency monitor: task " + taskid);
		
		double dt = 0;
		/* Check buffer */
		
		long t1 = buffer.getLong(0);
		
		long t2 = System.nanoTime();
		dt = (t2 - t1) / 1000000.0; /* In milliseconds */

		// System.out.println("[DBG] Timestamp in latency monitor is " + t1 + "; dt is " + dt);
		
		measurements.add(dt);
		
		latency += dt;
		// System.out.println("[DBG] cummulative latency in latency monitor is " + latency);
		latencySquared += (dt * dt);
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
		
		System.out.println(String.format("[DBG] [LatencyMonitor] 5th %10.3f 25th %10.3f 50th %10.3f 75th %10.3f 95th %10.3f", 
			evaluateSorted(array,  5D),
			evaluateSorted(array, 25D),
			evaluateSorted(array, 50D),
			evaluateSorted(array, 75D),
			evaluateSorted(array, 95D)
			));
	}
	
	public double evaluate (final double[] values, final int begin, final int length, final double p) {
			
		if ((p > 100) || (p <= 0)) {
			throw new IllegalArgumentException("invalid quantile value: " + p);
		}
		
		if (length == 0) {
			return Double.NaN;
		}
		
		if (length == 1) {
			return values[begin]; /* always return single value for n = 1 */
		}
		
		/* Sort array */
		double [] sorted = new double[length];
		
		System.arraycopy (values, begin, sorted, 0, length);
		
		Arrays.sort(sorted);
		
		return evaluateSorted (sorted, p);
	}
	
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
