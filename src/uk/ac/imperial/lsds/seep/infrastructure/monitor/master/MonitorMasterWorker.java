/*******************************************************************************
 * Copyright (c) 2014 Imperial College London
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial API and implementation
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.infrastructure.monitor.master;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.Observable;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.Stoppable;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.comm.serialization.MetricsDeserializer;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.comm.serialization.MetricsTuple;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.evaluate.AbstractEvaluator;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricName;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricValue;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.util.MetricReading;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.util.MetricReadingProvider;

/**
 * Worker for the monitor master. Each worker serves a particular monitor slave
 * (there should be one monitor slave per operator). The worker will endlessly
 * receive metric tuples over a socket and evaluate 
 * @author mrouaux
 */
public class MonitorMasterWorker 
        implements Runnable, Stoppable, Observable<MonitorMasterListener> {

    final private Logger logger = LoggerFactory.getLogger(MonitorMasterWorker.class);

    private Socket slaveSocket;
    private MetricsDeserializer deserializer;
    private AbstractEvaluator evaluator;
    
    private Map<Integer, MonitorMasterListener> listeners;
    
    private boolean receive;
    
    /**
     * Convenience constructor
     * @param slaveSocket Socket between the monitor master and a slave. The slave
     * will stream metric readings over this connection at regular intervals.
     * @param evaluator Evaluator for rules
     */
    public MonitorMasterWorker(final Socket slaveSocket,
            final MetricsDeserializer deserializer,
            final AbstractEvaluator evaluator) {
        
        this.slaveSocket = slaveSocket;
        this.deserializer = deserializer;
        this.evaluator = evaluator;
        
        this.receive = true;
        
        this.listeners = new HashMap<Integer, MonitorMasterListener>();
    }
    
    @Override
    public void run() {
        logger.info("Starting MonitorMasterWork for slave " 
                                + slaveSocket.getRemoteSocketAddress());
        
        while(receive) {
            logger.debug("Waiting for tuple from monitoring slave");
            
            // Receive message from slave and deserialise metric tuple
            final MetricsTuple tuple = deserializer.deserialize();
            
            if (tuple != null) {
                logger.debug("Received tuple from slave " + tuple.toString());

                // Use policy evaluator to evaluate the all rules 
                evaluator.evaluate(new MetricReadingProvider() {

                    /**
                     * Operator identifier for the slave that sent the current tuple.
                     */
                    @Override
                    public int getOperatorId() {
                        return tuple.getOperatorId();
                    }

                    /**
                     * Construct a reading from the received tuple and return it to
                     * the policy evaluator, which will make a decision based on it
                     * (and past history from previous readings).
                     */
                    @Override
                    public MetricReading nextReading() {
                        MetricReading reading = new MetricReading();

                        Instant timestamp = DateTime.now().toInstant();
                        Map<MetricName, MetricValue> values 
                                        = new HashMap<MetricName, MetricValue>();

                        Set<MetricName> names = tuple.metricNames();
                        for(MetricName name : names) {
                            values.put(name, tuple.getMetricValue(name));
                        }

                        reading.setTimestamp(timestamp);
                        reading.setValues(values);

                        return reading;
                    }
                });
                
                // Notify any listeners regiestered for the operator
                if (listeners.containsKey(tuple.getOperatorId())) {
                    listeners.get(tuple.getOperatorId()).onTupleReceived(tuple);
                }
            } else {
                // A null tuple indicates that the counter-part worker on the slave
                // has been closed and no further tuples will be sent by the slave.
                try {
                    slaveSocket.close();
                } catch (IOException ex) {
                }
                
                receive = false;
            }
        }
        
        logger.info("MonitorMasterWorker is stopped");
    }

    @Override
    public void stop() {
        logger.info("Stopping MonitorMasterWorker");
        receive = false;
    }

    /**
     * Register listener to be notified whenever a tuple is received for a particular
     * operator identifier.
     * 
     * @param listener A MonitorMasterListener instance.
     */
    @Override
    public void addListener(MonitorMasterListener listener) {
        listeners.put(listener.getOperatorId(), listener);
    }
}
