package seep.infrastructure.monitor;

import seep.operator.InputQueue;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;

/* MetricsReader is the centralized point where the instrumented parts of the system are registered */

public class MetricsReader {

	final public static Counter eventsInputQueue = Metrics.newCounter(InputQueue.class, "events-iq");
	
	public MetricsReader(){
		
	}
}
