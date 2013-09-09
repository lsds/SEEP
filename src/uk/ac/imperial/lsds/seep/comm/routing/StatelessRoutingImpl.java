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
package uk.ac.imperial.lsds.seep.comm.routing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class StatelessRoutingImpl implements RoutingStrategyI, Serializable{

	private static final long serialVersionUID = 1L;
	private int splitWindow = 0;
	private int target = 0;
	private int downstreamIndex = 0;
	private int remainingWindow = 0;
		
	private int numberOfDownstreams = 0;
		
	//This structure maps the real indexes (where is a given downstream) with the virtual index (where is a downstream within the set of downstream of same type
	// so map virtual integer with real integer
	/// \todo{consider change this structure to arraylist}
	private HashMap<Integer, Integer> virtualIndexToRealIndex = new HashMap<Integer, Integer>();
	private int virtualIndex = 0;
		
//	public void setNumberOfDownstreams(int numberOfDownstreams){
//		this.numberOfDownstreams = numberOfDownstreams;
//	}
		
	public StatelessRoutingImpl(int splitWindow, int index, int numOfDownstreams){
		//this.splitWindow = splitWindow-1;
		this.splitWindow = splitWindow;
		//Map the virtual index with the real index
		virtualIndexToRealIndex.put(virtualIndex, index);
		virtualIndex++;
		this.numberOfDownstreams = numOfDownstreams;
	}
		
	public ArrayList<Integer> route(ArrayList<Integer> targets, int value){
		int targetRealIndex = -1;
		if(remainingWindow == 0){
			//Reinitialize the window size
			remainingWindow = splitWindow;
			// update target and reinitialize filterValue
			target = downstreamIndex++%numberOfDownstreams;
			
//System.out.println("NUM DOWNSTR: "+numberOfDownstreams);
//System.out.println("Down INDEX: "+downstreamIndex);
//System.out.println("Target: "+target);
//			System.out.println("Real routing info is: ");
//			for(Entry<Integer, Integer> entry : virtualIndexToRealIndex.entrySet()){
//				System.out.println("VirtualIdx: "+entry.getKey()+" RealIdx: "+entry.getValue());
//			}
//			System.out.println("And we are asking for: "+target);
			targetRealIndex = virtualIndexToRealIndex.get(target);
	//System.out.println("NW routeStrem add realIndex: "+targetRealIndex );
			//If the index was not present in the targets list, add it.
			if(!targets.contains(targetRealIndex)){
				targets.add(targetRealIndex);
			}
	//System.out.println("NoWindow: targets-size: "+targets.size());
			return targets;
		}
		remainingWindow--;
		/// \todo Return the real Index, got from the virtual one. Optimize this
//		for(Entry<Integer, Integer> entry : virtualIndexToRealIndex.entrySet()){
//			System.out.println("vidx: "+entry.getKey()+" ridx: "+entry.getValue());
//		}
		targetRealIndex = virtualIndexToRealIndex.get(target);
//System.out.println("W routeStrem add realIndex: "+virtualIndexToRealIndex.get(target));
		if(!targets.contains(targetRealIndex)){
			targets.add(targetRealIndex);
		}
//System.out.println("Window: targets-size: "+targets.size());
		return targets;
	}
	
	//overriden to make ANY faster...
	public ArrayList<Integer> route(int value){
System.out.println("Rem window: "+remainingWindow);
System.out.println("splitWindow: "+splitWindow);
		ArrayList<Integer> targets = new ArrayList<Integer>();
		if(remainingWindow == 0){
			//Reinitialize the window size
			remainingWindow = splitWindow;
			// update target and reinitialize filterValue
			target = downstreamIndex++%numberOfDownstreams;
			// get the real index from the virtual one.
			//target = virtualIndexToRealIndex.get(target);
System.out.println("target: "+target);
System.out.println("numberOfDownstreams: "+numberOfDownstreams);
			targets.add(target);
			return targets;
		}
		remainingWindow--;
		/// \todo Return the real Index, got from the virtual one. Optimize this
		//target = virtualIndexToRealIndex.get(target);
System.out.println("Target: "+target);
		targets.add(target);
		return targets;
	}
	
	public int[] newReplica(int oldOpIndex, int newOpIndex) {
		//In this case oldOpIndex does not do anything
//System.out.println("#### There is a NEW SPLIT here, newOpIndex: "+newOpIndex);
		//First of all, we map the real index to virtual index
//System.out.println("MAPPING virtualIndex: "+virtualIndex+" to realIndex: "+newOpIndex);
		virtualIndexToRealIndex.put(virtualIndex, newOpIndex);
		virtualIndex++;
		
		//Update the number of downstreams
//System.out.println("PREV: "+numberOfDownstreams);
		numberOfDownstreams++;
//System.out.println("POST: "+numberOfDownstreams);
		//return something cause interface implementation...
		//return -1;
		int[] fakeKeys = {-1, -1};
		return fakeKeys;
	}
	
	public int[] newStaticReplica(int oldOpIndex, int newOpIndex){
		//In this case oldOpIndex does not do anything
		//First of all, we map the real index to virtual index
		virtualIndexToRealIndex.put(virtualIndex, newOpIndex);
		virtualIndex++;
		
		numberOfDownstreams++;
		
		///\fixme{return something meaningful}
		int[] fakeKeys = {-1, -1};
		return fakeKeys;
	}
		
	@Override
	public ArrayList<Integer> routeToAll(ArrayList<Integer> targets) {
		return new ArrayList<Integer>(virtualIndexToRealIndex.values());
	}

	@Override
	public ArrayList<Integer> routeToAll() {
		ArrayList<Integer> targets = new ArrayList<Integer>();
		return routeToAll(targets);
	}
}
