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

import java.io.OutputStream;

/**
 * Base interface for all metrics serializers. Subclasses implementing this interface
 * are responsible for converting/serializing the reported tuple to an appropriate representation
 * for the underlying channel.
 * 
 * @author mrouaux
 */
public interface MetricsSerializer<T extends OutputStream> {
    
    void initialize(T os);
    
    void serialize(MetricsTuple tuple);
    
}
