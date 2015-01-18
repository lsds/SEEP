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
 * Partitionable defines methods required to split CustomState. Note that LargeState implementations do not need to implement Partitionable,
 * only CustomState implementations need to do it in case they are indeed partitionable.
 * @author raulcf
 *
 */
public interface Partitionable {

	public void setKeyAttribute(String keyAttribute);
	public String getKeyAttribute();
	public StateWrapper[] splitState(StateWrapper toSplit, int key);
	public void resetState();
	
}
