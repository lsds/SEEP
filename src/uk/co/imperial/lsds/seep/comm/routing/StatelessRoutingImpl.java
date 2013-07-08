package uk.co.imperial.lsds.seep.comm.routing;

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
		this.splitWindow = splitWindow-1;
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
		
//			/** LAYER ONE OF ROUTING.. done in both implementations */
//			ArrayList<Integer> logicTargets = routeLayerOne();			
//System.out.println("ANY");
		ArrayList<Integer> targets = new ArrayList<Integer>();
//System.out.println("##########");
//System.out.println("Rem window: "+remainingWindow);
		if(remainingWindow == 0){
			//Reinitialize the window size
			remainingWindow = splitWindow;
			// update target and reinitialize filterValue
//System.out.println("NUM DOWNSTR: "+numberOfDownstreams);
			target = downstreamIndex++%numberOfDownstreams;
//System.out.println("TARGET: "+target);
			// get the real index from the virtual one.
			//target = virtualIndexToRealIndex.get(target);
			targets.add(target);
			return targets;
		}
		remainingWindow--;
		/// \todo Return the real Index, got from the virtual one. Optimize this
		//target = virtualIndexToRealIndex.get(target);
		targets.add(target);
		return targets;
	}
	
	public int newReplica(int oldOpIndex, int newOpIndex) {
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
		return -1;
	}
	
	public int newStaticReplica(int oldOpIndex, int newOpIndex){
		//In this case oldOpIndex does not do anything
		//System.out.println("#### There is a NEW SPLIT here, newOpIndex: "+newOpIndex);
				//First of all, we map the real index to virtual index
		//System.out.println("MAPPING virtualIndex: "+virtualIndex+" to realIndex: "+newOpIndex);
				virtualIndexToRealIndex.put(virtualIndex, newOpIndex);
				virtualIndex++;
				
				//Update the number of downstreams
//		System.out.println("PREV: "+numberOfDownstreams);
//				
//		System.out.println("POST: "+numberOfDownstreams);
				//return something cause interface implementation...
				return -1;
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
	
//		public StatelessRoutingImpl(int splitWindow){
//		//value minus one to take into consideration value = 0
//		this.splitWindow = splitWindow-1;
//		//Initialize the windows size to the configured window
//		this.remainingWindow = this.splitWindow;
//		//When creating there is just one replica, this is changed later when the user establishes replicas manually or there is a scale-out
//		this.numberOfDownstreams = 1;
//	}
	
//	public StatelessRoutingImpl(){
//		//value minus one to take into consideration value = 0
//		this.splitWindow = 0;
//		//Initialize the windows size to the configured window
//		this.remainingWindow = this.splitWindow;
//		//When creating there is just one replica, this is changed later when the user establishes replicas manually or there is a scale-out
//		this.numberOfDownstreams = 1;
//	}
}
