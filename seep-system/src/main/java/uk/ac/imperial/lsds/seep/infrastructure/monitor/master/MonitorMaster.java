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
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.Observable;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.Stoppable;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.comm.serialization.BinaryMetricsDeserializer;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.comm.serialization.MetricsDeserializer;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.PolicyRules;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.evaluate.AbstractEvaluator;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.evaluate.PolicyRulesEvaluator;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.util.InfrastructureAdaptor;

/**
 * Monitoring master.
 * 
 * @author mrouaux
 */
public class MonitorMaster implements Runnable, Stoppable, Observable<MonitorMasterListener> {

    final private Logger logger = LoggerFactory.getLogger(MonitorMaster.class);

    private InfrastructureAdaptor infrastructure;
    private PolicyRules rules;
    
    private int port;
    private boolean listen;
    
    private List<MonitorMasterWorker> workers;
    private Map<Integer, MonitorMasterListener> listeners;
    
    private ServerSocket serverSocket;
    
    /**
     * Convenience constructor.
     * @param infrastructure 
     */
    public MonitorMaster(final InfrastructureAdaptor infrastructure, 
                                final PolicyRules rules, final int port) {
        this.infrastructure = infrastructure;
        this.rules = rules;
        this.port = port;
        
        this.listen = true;
        this.workers = new ArrayList<MonitorMasterWorker>();
        
        this.listeners = new HashMap<Integer, MonitorMasterListener>();
    }
    
    /**
     * Execute the monitoring master process.
     */
    @Override
    public void run() {
        listen = true;
		
        try {
            logger.info("Starting MonitorMaster on port " + port);
			serverSocket = new ServerSocket(port);
            
            while(listen) {
                Socket slaveSocket = null;
                
                try {
                    slaveSocket = serverSocket.accept();
                } catch(SocketException ex) {
                    if (!serverSocket.isClosed()) {
                        logger.error("Exception accepting connection from slave ", ex);
                    }
                }
                
                if ((slaveSocket != null) && (!serverSocket.isClosed())) {
                    logger.info("Received connection request from " 
                                            + slaveSocket.getRemoteSocketAddress());

                    MetricsDeserializer binaryDeserializer = new BinaryMetricsDeserializer();
                    binaryDeserializer.initialize(slaveSocket.getInputStream());

                    AbstractEvaluator policyRulesEvaluator 
                                    = createEvaluator(infrastructure, rules);

                    MonitorMasterWorker worker = createMasterWorker(slaveSocket, 
                                    binaryDeserializer, policyRulesEvaluator);

                    startMasterWorker(worker);

                    // Keep a list of all workers so we can stop them when the server is stopped.
                    workers.add(worker);
                }
            }
            
            // The server is stopping, we need to pass on the stop signal to all
            // workers that were created before, in order to terminate gracefully
            for(MonitorMasterWorker worker : workers) {
                worker.stop();
            }
            
            serverSocket.close();
            logger.info("MonitorMaster is stopped");
			
        } catch(IOException ex){
            logger.error("Exception reading ", ex);
		}
    }

    /**
     * Stops the monitoring master process.
     */
    @Override
    public void stop() {
        logger.info("Stopping MonitorMaster");
        listen = false;
        
        try {
            serverSocket.close();
        } catch (IOException ex) {
            logger.error("Exception closing MonitorMaster server socket", ex);
        }
    }
    
    /**
     * Creates an evaluator for the policy rules, acting upon the infrastructure 
     * proxied by the adaptor instance.
     * @param adaptor Adaptor for the underlying infrastructure.
     * @param rules Scaling policy rules to be evaluated by the evaluator.
     * @return PolicyRulesEvaluator instance.
     */
    protected AbstractEvaluator createEvaluator(final InfrastructureAdaptor adaptor, 
                                final PolicyRules rules) {
        
        return new PolicyRulesEvaluator(rules, adaptor);
    }
    
    /**
     * Creates worker instance for the MonitorMaster, associated to a particular
     * slave instance.
     * @param slaveSocket Socket connected to a monitor slave.
     * @param deserializer Deserializer to apply to received MetricsTuple messages.
     * @param evaluator Evaluator to evaluate scaling policy rules.
     * @return MonitorMasterWorker instance.
     */
    protected MonitorMasterWorker createMasterWorker(final Socket slaveSocket, 
            final MetricsDeserializer deserializer, final AbstractEvaluator evaluator) {
        
        MonitorMasterWorker worker 
                = new MonitorMasterWorker(slaveSocket, deserializer, evaluator);
        
        // Copy all listeners from the master to the new worker
        for(Entry<Integer, MonitorMasterListener> entry : listeners.entrySet()) {
            worker.addListener(entry.getValue());
        }
        
        return worker;
    }
    
    protected void startMasterWorker(MonitorMasterWorker worker) {
        new Thread(worker).start();  
    }

    @Override
    public void addListener(MonitorMasterListener listener) {
        listeners.put(listener.getOperatorId(), listener);
    }
}
