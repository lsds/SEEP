package seep.infrastructure.monitor;

import java.util.concurrent.TimeUnit;

import seep.comm.IncomingDataHandler;
import seep.processingunit.ProcessingUnit;
import seep.runtimeengine.Barrier;
import seep.runtimeengine.InputQueue;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Meter;

/* MetricsReader is the centralised point where the instrumented parts of the system are registered */

public class MetricsReader {

	// To indicate the current number of events in the InputQueue (if inputQueue applies)
	final public static Counter eventsInputQueue = Metrics.newCounter(InputQueue.class, "events-iq");
	// To indicate the number of upstream connections (incoming data worker threads) that are working in this node
	final public static Counter numberIncomingDataHandlerWorkers = Metrics.newCounter(IncomingDataHandler.class, "dataThreads-idh");
	// To indicate the number of events / second being processed by this node
	final public static Meter eventsPerSecond = Metrics.newMeter(ProcessingUnit.class, "get-events", "events", TimeUnit.SECONDS);
	// Events processed, manual counter
	final public static Counter eventsProcessed = Metrics.newCounter(ProcessingUnit.class, "total-events");
	
	public MetricsReader(){
		
	}
}
