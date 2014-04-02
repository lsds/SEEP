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

import java.io.InputStream;

/**
 * Base interface for all metrics deserializers. Subclasses implementing this interface
 * are responsible for converting/de-serializing the appropriate data representation
 * from the underlying channel into a tuple.
 * 
 * @author mrouaux
 */
public interface MetricsDeserializer<T extends InputStream> {
    
    void initialize(T is);
    
    MetricsTuple  deserialize();
    
}
