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

/**
 * Interface for providers of metric readings, capable of reporting the value 
 * for a metric. Subclasses implementing this interface need to adapt other
 * data structures (e.g.: tuples) used in the system to handle metrics and 
 * their corresponding values to a representation that is suitable for the 
 * monitoring and scaling layer, i.e.: MetricReading instances.
 * 
 * @author mrouaux
 */
public interface MetricReadingProvider {
 
    int getOperatorId();
    
    MetricReading nextReading();
    
}
