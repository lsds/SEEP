package seep.comm.routing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;


@SuppressWarnings("serial")
public class StatelessDynamicLoadBalancer implements LoadBalancerI, Serializable {

	//By default, the splitWindow is a round robin, (splitWindow = 0)
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

	public StatelessDynamicLoadBalancer(int splitWindow){
		//value minus one to take into consideration value = 0
		this.splitWindow = splitWindow-1;
		//Initialize the windows size to the configured window
		this.remainingWindow = this.splitWindow;
		//When creating there is just one replica, this is changed later when the user establishes replicas manually or there is a scale-out
		this.numberOfDownstreams = 1;
	}
	
	public StatelessDynamicLoadBalancer(){
		//value minus one to take into consideration value = 0
		this.splitWindow = 0;
		//Initialize the windows size to the configured window
		this.remainingWindow = this.splitWindow;
		//When creating there is just one replica, this is changed later when the user establishes replicas manually or there is a scale-out
		this.numberOfDownstreams = 1;
	}
	
	// This constructor is only called when there is a CONTENT-BASED dispatch policy
	public StatelessDynamicLoadBalancer(int splitWindow, int index){
		this.splitWindow = splitWindow-1;
		//Map the virtual index with the real index
		virtualIndexToRealIndex.put(virtualIndex, index);
		virtualIndex++;
		//When creating there is just one replica, this is changed later when the user establishes replicas manually or there is a scale-out
		this.numberOfDownstreams = 1;
	}
	
	public ArrayList<Integer> route(ArrayList<Integer> targets, int value){
		int targetRealIndex = -1;
		if(remainingWindow == 0){
			//Reinitialize the window size
			remainingWindow = splitWindow;
			// update target and reinitialize filterValue
			target = downstreamIndex++%numberOfDownstreams;
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
	public int route(){
//System.out.println("ANY");
		if(remainingWindow == 0){
			//Reinitialize the window size
			remainingWindow = splitWindow;
			// update target and reinitialize filterValue
			target = downstreamIndex++%numberOfDownstreams;
			// get the real index from the virtual one.
			//target = virtualIndexToRealIndex.get(target);
			return target;
		}
		remainingWindow--;
		/// \todo Return the real Index, got from the virtual one. Optimize this
		//target = virtualIndexToRealIndex.get(target);
		return target;
	}

	public int newReplica(int oldOpIndex, int newOpIndex) {
		//In this case oldOpIndex does not do anything
//System.out.println("#### There is a NEW SPLIT here, newOpIndex: "+newOpIndex);
		//First of all, we map the real index to virtual index
//System.out.println("MAPPING virtualIndex: "+virtualIndex+" to realIndex: "+newOpIndex);
		virtualIndexToRealIndex.put(virtualIndex, newOpIndex);
		virtualIndex++;
		
		//Update the number of downstreams
		numberOfDownstreams++;
		//return something cause interface implementation...
		return -1;
	}
}