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
 * Base interface for all builders created in the monitoring package for SEEP.
 * Typically, a builder exposes several methods to client code in order to specify 
 * the different parts that constitute the object being built.
 * 
 * @author mrouaux
 * @param <T> Type of objects built by this builder.
 */
public interface Builder<T> {

	T build();
	
}
