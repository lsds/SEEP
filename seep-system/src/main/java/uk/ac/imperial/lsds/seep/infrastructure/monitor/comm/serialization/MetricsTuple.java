/*******************************************************************************
 * Copyright (c) 2013 Imperial College London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial design and implementation
 *     Martin Rouaux - refactored to be generic and support different metrics      
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.infrastructure.monitor.comm.serialization;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricName;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricValue;

public class MetricsTuple implements Serializable {

	private int operatorId;
    private Map<MetricName,MetricValue> metrics;
    
	public MetricsTuple() {
        metrics = new HashMap<MetricName,MetricValue>();
	}

	public int getOperatorId() {
		return operatorId;
	}
	
	public void setOperatorId(int operatorId) {
		this.operatorId = operatorId;
	}
    
	public MetricValue getMetricValue(MetricName name) {
        return metrics.get(name);
	}
    
    public void setMetricValue(MetricName name, MetricValue value) {
        metrics.put(name, value);
    }
    
    public Set<MetricName> metricNames() {
        return metrics.keySet();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        for(MetricName name : metrics.keySet()) {
            sb.append(name.getName());
            sb.append("=");
            
            MetricValue value = metrics.get(name);
            if(value != null) {
                sb.append(metrics.get(name).toString());
            } else {
                sb.append("null");
            }
            
            sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        
        return "MetricsTuple{" + "operatorId=" + operatorId 
                    + ", metrics=(" + sb.toString() + ")}";
    }
}
