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
package uk.ac.imperial.lsds.seep.runtimeengine;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class TimestampTracker {

	// Saves the ts of the last tuple received through a given stream (identified with opId at the other side)
	private HashMap<Integer, Long> tsStream = new HashMap<Integer, Long>();
	
	public Iterator<Entry<Integer, Long>> getTsStream(){
		return tsStream.entrySet().iterator();
	}
	
	public void set(int stream, long ts){
		tsStream.put(stream, ts);
	}
	
	public long get(int stream){
		return tsStream.get(stream);
	}
	
}
