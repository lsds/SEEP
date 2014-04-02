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
package uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.util;

import java.util.HashMap;
import java.util.Map;
import org.joda.time.Instant;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricName;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricValue;

/**
 * Encapsulates a metric name and a value. The class also provides a time reference 
 * for the monitoring/scaling layer. We want to use an external time reference to 
 * facilitate testing (we can mock a clock and use fake timestamps for metric 
 * readings during testing).
 *
 * @author mrouaux
 */
public class MetricReading {
    
    private Map<MetricName, MetricValue> values;
    private Instant timestamp;

    /**
     * Default constructor
     */
    public MetricReading() {
        this.values = new HashMap<MetricName, MetricValue>();
        this.timestamp = null;
    }

    /**
     * Convenience constructor
     * @param values Map of metric names and values, in a representation that is 
     * suitable for the monitoring/scaling layer.
     * @param timestamp Timestamp when the metric reading was produce/received
     * (this is not relevant as long as the same source is used).
     */
    public MetricReading(
            Map<MetricName, MetricValue> values, Instant timestamp) {
        this.values = values;
        this.timestamp = timestamp;
    }

    /**
     * @return Map of metric names and values
     */
    public Map<MetricName, MetricValue> getValues() {
        return values;
    }

    /**
     * @param values Map of metric names and values
     */
    public void setValues(Map<MetricName, MetricValue> values) {
        this.values = values;
    }

    /**
     * @return Timestamp representing the time when the current metric was either
     * received or produced.
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp Timestamp representing the time when the current metric 
     * was either received or produced.
     */
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @return String representation of the object
     */
    @Override
    public String toString() {
        return "MetricReading{" + "values=" + values + ", timestamp=" + timestamp + '}';
    }
}
