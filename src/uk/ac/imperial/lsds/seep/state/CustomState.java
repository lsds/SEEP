/*******************************************************************************
 * Copyright (c) 2013 Imperial College London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial design and implementation
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.state;

/**
 * CustomState is a tagging interface that indicates that a given State implementation is customized
 * and does not implement LargeState. Overall, this means that the system will deep copy this object for fault-tolerance and
 * data parallelism reasons. If deep copy is not acceptable custom state must instead implement LargeState.
 * @author raulcf
 *
 */
public interface CustomState extends State{

}
