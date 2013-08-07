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
		if(tsStream.containsKey(stream)){
			return tsStream.get(stream);
		}
		else{
			return 0; // oldest ts possible
		}
	}
	
	public static TimestampTracker returnSmaller(TimestampTracker a, TimestampTracker b){
		if(a == null) return b;
		if(b == null) return a;
		if(a.tsStream.size() != b.tsStream.size()) return null;
		TimestampTracker tt = new TimestampTracker();
		for(Integer id : a.tsStream.keySet()){ // for each component
			long ats = a.get(id); // pick the smaller component (the one that trims less)
			long bts = b.get(id);
			if(ats < bts) tt.set(id, ats);
			else tt.set(id, bts);
		}
		return tt;
	}
	
	public static boolean isSmallerOrEqual(TimestampTracker a, TimestampTracker b){
		if(a == null) return false;
		if(b == null) return false;
		if(a.tsStream.size() != b.tsStream.size()) return false;
		for(Integer id : a.tsStream.keySet()){ // for each component
			long ats = a.get(id); // pick the smaller component (the one that trims less)
			long bts = b.get(id);
			if(bts < ats) return false; // if any component is bigger, then false
		}
		return true;
	}
	
	@Override
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		for(Integer id : tsStream.keySet()){
			buffer.append(id+": "+tsStream.get(id)+" ");
		}
		return buffer.toString();
	}
	
}
