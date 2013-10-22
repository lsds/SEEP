/*******************************************************************************
 * Copyright (c) 2013 Imperial College London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial design and implementation
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.infrastructure.monitor;

import uk.ac.imperial.lsds.seep.comm.IncomingDataHandler;
import uk.ac.imperial.lsds.seep.processingunit.StatefulProcessingUnit;
import uk.ac.imperial.lsds.seep.runtimeengine.InputQueue;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

/* MetricsReader is the centralised point where the instrumented parts of the system are registered */

public class MetricsReader {

	final static MetricRegistry metrics = new MetricRegistry();
	
	static{
		MetricRegistry.name(InputQueue.class, "events-iq");
		MetricRegistry.name(IncomingDataHandler.class, "dataThreads-idh");
		MetricRegistry.name(StatefulProcessingUnit.class, "get-events");
		MetricRegistry.name(StatefulProcessingUnit.class, "get-events");
		MetricRegistry.name(StatefulProcessingUnit.class, "total-events");
		MetricRegistry.name(InputQueue.class, "logged-events");
		
	}
	
	// To indicate the current number of events in the InputQueue (if inputQueue applies)
	final public static Counter eventsInputQueue = metrics.counter("events-iq");
	// To indicate the number of upstream connections (incoming data worker threads) that are working in this node
	final public static Counter numberIncomingDataHandlerWorkers = metrics.counter("dataThreads-idh");
	// To indicate the number of events / second being processed by this node
	final public static Meter eventsPerSecond = metrics.meter("get-events");
	// Events processed, manual counter
	final public static Counter eventsProcessed = metrics.counter("total-events");
	// To indicate the current number of logged tuples
	final public static Counter loggedEvents = metrics.counter("logged-events");
	
	// Metrics does not provide a reset method due to overhead issues in concurrent scenarios.
	// this method is an approximation, not exact behaviour.
	public static void reset(Counter toReset){
		toReset.dec(toReset.getCount());
	}
	
	public MetricsReader(){
		
	}
}
