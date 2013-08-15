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
package uk.ac.imperial.lsds.seep.reliable;

import java.util.ArrayList;

public class MemoryChunk {

	public ArrayList<Object> chunk;
	
	public MemoryChunk(){
		
	}
	
	public MemoryChunk(ArrayList<Object> chunk){
		this.chunk = chunk;
	}
	
}
