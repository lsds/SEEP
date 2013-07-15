/*******************************************************************************
 * Copyright (c) 2013 Raul Castro Fernandez (Ra).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Ra - Design and initial implementation
 ******************************************************************************/
package uk.co.imperial.lsds.seep.infrastructure.monitor;

import java.util.concurrent.TimeUnit;

import uk.co.imperial.lsds.seep.comm.IncomingDataHandler;
import uk.co.imperial.lsds.seep.processingunit.StatefulProcessingUnit;
import uk.co.imperial.lsds.seep.runtimeengine.InputQueue;

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
	final public static Meter eventsPerSecond = Metrics.newMeter(StatefulProcessingUnit.class, "get-events", "events", TimeUnit.SECONDS);
	// Events processed, manual counter
	final public static Counter eventsProcessed = Metrics.newCounter(StatefulProcessingUnit.class, "total-events");
	// To indicate the current number of logged tuples
	final public static Counter loggedEvents = Metrics.newCounter(InputQueue.class, "logged-events"); 
	
	public MetricsReader(){
		
	}
}
