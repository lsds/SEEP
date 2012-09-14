package seep.comm.routing;

import java.util.ArrayList;

public interface RoutingStrategyI {

	public ArrayList<Integer> route(int value);
	public ArrayList<Integer> route(ArrayList<Integer> targets, int value);
	public int newReplica(int oldOpIndex, int newOpIndex);
	
}
