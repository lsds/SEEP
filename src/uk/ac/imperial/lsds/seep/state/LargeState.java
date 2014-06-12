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
 * LargeState is State that also implements the interfaces Versionable and Streamable.
 * @author raulcf
 *
 */
public interface LargeState extends State, Versionable, Streamable{

	@Deprecated
	public Object getVersionableAndStreamableState();
	
}
