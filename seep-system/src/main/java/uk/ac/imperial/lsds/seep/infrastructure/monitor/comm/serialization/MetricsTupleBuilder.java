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
package uk.ac.imperial.lsds.seep.infrastructure.monitor.comm.serialization;

import java.util.HashMap;
import java.util.Map;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.Builder;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricName;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricValue;

/**
 * Builder class to create a generic tuple containing metrics. Typically, instances
 * of this class are serialized and sent from the monitor slaves to the monitor 
 * master. Tuples contain an unbounded list of key-value pairs (where the key is 
 * the name of the metric and the value is the actual value in addition to the
 * measurement unit).
 * 
 * @author mrouaux
 */
public class MetricsTupleBuilder implements Builder<MetricsTuple> {
    
    private int operatorId;
    private Map<MetricName, MetricValue> metrics;
    
    public static MetricsTupleBuilder tuple() {
        return new MetricsTupleBuilder();
    }
    
    /**
     * Default constructor
     */
    public MetricsTupleBuilder() {
        metrics = new HashMap<MetricName, MetricValue>();
    }
    
    /**
     * Convenience builder method to define the operator identifier for the tuple.
     * @param operatorId Unique operator identifier
     * @return Builder instance to allow for chained calls to the builder.
     */
    public MetricsTupleBuilder forOperator(int operatorId) {
        this.operatorId = operatorId;
        return this;
    } 
    
    /**
     * Convenience builder method to add a metric value to the tuple.
     * @param name Name of the metric
     * @param value Value (and unit) for the metric
     * @return Builder instance to allow for chained calls to the builder.
     */
    public MetricsTupleBuilder withMetric(MetricName name, MetricValue value) {
        metrics.put(name, value);
        return this;
    }
    
    /**
     * Builds tuple with the provided metric names and values.
     * @return Metrics tuple.
     */
    @Override
    public MetricsTuple build() {
        MetricsTuple tuple = new MetricsTuple();
        
        tuple.setOperatorId(operatorId);
        
        for(MetricName name : metrics.keySet()) {
            MetricValue value = metrics.get(name);
            tuple.setMetricValue(name, value);
        }
        
        return tuple;
    }
}
