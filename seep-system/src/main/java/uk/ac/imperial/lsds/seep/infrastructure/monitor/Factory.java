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
package uk.ac.imperial.lsds.seep.infrastructure.monitor;

/**
 * Base interface for all factories created in the monitoring package for SEEP.
 * Typically, a factory exposes a few methods to client code in order to create
 * instances of predetermined classes. In general, client code has no or little 
 * control over how these objects are created.
 *
 * @author mrouaux
 * @param <T> Type of objects created by this factory.
 */
public interface Factory<T> {
    
    T create();
    
}
