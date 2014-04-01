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
package uk.ac.imperial.lsds.seep.api.largestateimpls;

import java.io.Serializable;

public class Component implements Serializable{

	private static final long serialVersionUID = 1L;
	
	
	public int col;
	public int value;

	public Component(){
		
	}
	
	public Component( int col, int value) {
		
		this.col = col;
		this.value = value;
	}
	
	@Override
	public String toString(){
		return " column-> "+col+" value-> "+value;
	}
}