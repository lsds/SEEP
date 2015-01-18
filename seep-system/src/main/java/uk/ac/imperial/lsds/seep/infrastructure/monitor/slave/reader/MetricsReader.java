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
package uk.ac.imperial.lsds.seep.infrastructure.monitor.slave.reader;

import java.util.List;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricName;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricValue;

/**
 * Base interface for all metric readers supported by the system. At the very least,
 * a reader should be capable of returning values for a given metric and also, 
 * indicate the names of all the metrics it supports.
 * 
 * @author mrouaux
 */
public interface MetricsReader {
    
    List<MetricName> readableNames();
    
    MetricValue readValue(MetricName name);
    
}
