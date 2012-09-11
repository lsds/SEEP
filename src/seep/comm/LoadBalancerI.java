package seep.comm;

import java.util.ArrayList;

public interface LoadBalancerI {

	public ArrayList<Integer> route(ArrayList<Integer> targets, int value);
	
	public int newReplica(int oldOpIndex, int newOpIndex);
	
}
