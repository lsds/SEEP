package uk.ac.imperial.lsds.seep.multi;

import java.nio.ByteOrder;
import java.util.ArrayList;
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
	
	public void monitor (IQueryBuffer buffer) {
		
		if (! this.active.get())
			return ;
		
		double dt = 0;
		/* Check buffer */
		long t1 = buffer.getLong(0);
		
		long t2 = System.nanoTime();
		dt = (t2 - t1) / 1000000.0; /* In milliseconds */
		
		measurements.add(dt);
		
		// System.out.println("[DBG] Timestamp in latency monitor is " + t1 + "; dt is " + dt);
		
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
		System.out.println(String.format("[DBG] [LatencyMonitor] %10d measurements", measurements.size()));
		for (Double d: measurements) {
			System.out.println(String.format("%10.3f", d));
			System.out.flush();
		}
		System.out.println("Done.");
	}
}
