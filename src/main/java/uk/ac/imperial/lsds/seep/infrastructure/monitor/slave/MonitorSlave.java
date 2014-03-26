/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.imperial.lsds.seep.infrastructure.monitor.slave;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.Stoppable;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.comm.serialization.BinaryMetricsSerializer;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.comm.serialization.MetricsTuple;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.slave.reader.DefaultMetricsReader;

/**
 *
 * @author mrouaux
 */
public class MonitorSlave implements Runnable, Stoppable {

    final private Logger logger = LoggerFactory.getLogger(MonitorSlave.class);

    private int operatorId;
    
    private String masterHost;
    private int masterPort;

    private int freqSeconds;
    private int waitInterval;
    
    private boolean report;
    
    private Socket slaveSocket;
    
    /**
     * Convenience constructor.
     * @param operatorId Unique identifier of the operator for which this slave
     * will be reporting metrics.
     * @param masterHost Host where the monitoring master process is running.
     * @param masterPort Port on which the monitoring master is listening for
     * incoming TCP connections from slave monitoring processes.
     * @freqSeconds Frequency in seconds for the slave to report metrics back to
     * the master.
     */
    public MonitorSlave(int operatorId, String masterHost, 
            int masterPort, int freqSeconds) {
        
        this.operatorId = operatorId;
        this.masterHost = masterHost;
        this.masterPort = masterPort;
        
        this.freqSeconds = freqSeconds;
        this.waitInterval = (1000 * freqSeconds) - 1;
        
        this.report = true;
    }
    
    /**
     * Execute the monitoring slave process.
     */
    @Override
    public void run() {
        try {
            logger.info("Slave [operatorId=" + operatorId + "] Connecting to master");

			slaveSocket = new Socket(masterHost, masterPort);
			
            // Once we have an output stream to the master, we can create a processor
            // and initialise it accordingly.
            MonitorSlaveProcessor processor 
                    = createSlaveProcessor(slaveSocket.getOutputStream());
            
			// Execute in a tight loop, requesting the processor to read from whatever
            // readers it is set to use and report with any configured reporters.
			while(report) {
				Thread.sleep(waitInterval);
                processor.process();
			}
            
			slaveSocket.close();
            logger.info("MonitorSlave is stopped");
            
		} catch(IOException ex) {
			logger.error("Exception connecting to MonitorMaster", ex);
		} catch(InterruptedException ex){
			logger.error("Unable to wait before sending next metrics " 
                    + "to the MonitorMaster", ex);
        }
    }

    /**
     * Stops the monitoring slave process.
     */
    @Override
    public void stop() {
        logger.info("Stopping MonitorSlave");
        report = false;
        
        try {
            slaveSocket.close();
        } catch (IOException ex) {
            logger.error("Exception closing MonitorSlave client socket", ex);
        }
    }
    
    /**
     * Pushes tuple from the slave to the master. The push is immediate, the tuple
     * is not queued up until the next metric pushing cycle.
     * @param tuple Tuple to push to the monitoring master.
     */
    public void pushMetricsTuple(MetricsTuple tuple) {
        if (slaveSocket != null) {
            logger.debug("Pushing tuple to master " + tuple.toString());
            
            BinaryMetricsSerializer serializer = new BinaryMetricsSerializer();
            
            try {
                serializer.initialize(slaveSocket.getOutputStream());
            } catch(IOException ex) {
            	logger.error("Exception serialising metric to push", ex);
            }
            
            serializer.serialize(tuple);
        }        
    }
    
    /**
     * Creates and initialised an slave processor instance, for a given output
     * stream (which should terminate at the monitoring master).
     * @param os Output stream to the monitoring master.
     * @return Processor instance.
     */
    protected MonitorSlaveProcessor createSlaveProcessor(OutputStream os) {
        MonitorSlaveProcessor processor = new MonitorSlaveProcessor(operatorId);
        
        processor.addReader(new DefaultMetricsReader());
        
        BinaryMetricsSerializer serializer = new BinaryMetricsSerializer();
        serializer.initialize(os);
        
        processor.addReporter(serializer);
        
        return processor;
    }

    public int getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(int operatorId) {
        this.operatorId = operatorId;
    }
}
