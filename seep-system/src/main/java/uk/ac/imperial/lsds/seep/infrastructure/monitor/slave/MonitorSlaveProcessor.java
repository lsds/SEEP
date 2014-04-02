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
package uk.ac.imperial.lsds.seep.infrastructure.monitor.slave;

import uk.ac.imperial.lsds.seep.infrastructure.monitor.Processor;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.comm.serialization.MetricsTuple;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.comm.serialization.MetricsTupleBuilder;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricName;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricValue;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.slave.reader.MetricsReader;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.comm.serialization.MetricsSerializer;

/**
 *
 * @author mrouaux
 */
public class MonitorSlaveProcessor implements Processor {
  
    private static final Logger logger = 
                            LoggerFactory.getLogger(MonitorSlaveProcessor.class);
  
    private List<MetricsReader> readers;
    private List<MetricsSerializer> reporters;
    
    private int operatorId;
    
    public MonitorSlaveProcessor(final int operatorId) {
        this.operatorId = operatorId;
        
        this.readers = new ArrayList<MetricsReader>();
        this.reporters = new ArrayList<MetricsSerializer>();
    }
    
    /**
     * Registers a metrics reader with the slave monitoring processor.
     * @param reader
     * @return the processor itself (to allow method chaining)
     */
    public MonitorSlaveProcessor addReader(MetricsReader reader) {
        this.readers.add(reader);
        return this;
    }
    
    /**
     * Registers a reporter with the slave monitoring processor.
     * @param reporter
     * @return the processor itself (to allow method chaining)
     */
    public MonitorSlaveProcessor addReporter(MetricsSerializer reporter) {
        this.reporters.add(reporter);
        return this;
    }

    /*
     * Executes the usual process for monitoring a slave node. This is iterating
     * over all readers, read values for all supported metrics and construct the
     * corresponding tuple. Then, the tuple needs to be passed to all reporters.
     */
    public void process() {
        MetricsTupleBuilder builder = new MetricsTupleBuilder();
        
        builder.forOperator(operatorId);
        
        // Construct the tuple with all metrics from all readers
        for(MetricsReader reader : readers) {
            for(MetricName name : reader.readableNames()) {
                logger.info("Reading value for metric " + name.toString());
                
                MetricValue value = reader.readValue(name);
                logger.debug(name.toString() + "," + value.toString());
                
                builder.withMetric(name, value);
            }
        }
        
        MetricsTuple tuple = builder.build();
        logger.debug("Tuple built by slave " + tuple.toString());
        
        // Now pass the tuple to each one of the reporters
        for(MetricsSerializer reporter : reporters) {
            logger.info("Reporting tuple via " + reporter.toString());
            reporter.serialize(tuple);
        }
    }
}
