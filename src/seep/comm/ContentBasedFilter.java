package seep.comm;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import seep.comm.routing.LoadBalancerI;
import seep.comm.routing.StatefulDynamicLoadBalancer;
import seep.comm.routing.StatelessDynamicLoadBalancer;
import seep.comm.tuples.Seep;
import seep.infrastructure.NodeManager;
import seep.operator.OperatorContext;


/// \todo {this class should not be named filter and should not implement loadBalancerI}

@SuppressWarnings("serial")
public class ContentBasedFilter implements LoadBalancerI, Serializable{

	private String query = null;
	private Method queryFunction = null;
	//For now, just supported equals operator
//	private RouteOperator routeOperator = null;
	//private int argument = -1;
	
	//This map stores static info (for different types of downstream operators). Content-value -> list of downstreams
	public HashMap<Integer, ArrayList<Integer>> routeInfo = new HashMap<Integer, ArrayList<Integer>>();
	
	//This map stores the load balancer for each type of downstream
	private HashMap<Integer, LoadBalancerI> downTypeToLoadBalancer = new HashMap<Integer, LoadBalancerI>();
	
	public enum RelationalOperator{
		//LEQ, L, EQ, G, GEQ, RANGE
		//For now just supported equals operator
		EQ
	}

	/** CALLED STATIC */
	public ContentBasedFilter(String query){
		this.query = query;
	}

	/** CALLED STATIC */
	public void initializeFilters(){
		try {
			Class<Seep.DataTuple> c = seep.comm.tuples.Seep.DataTuple.class;
			queryFunction = c.getMethod(query);
		}
		catch (NoSuchMethodException nsme){
			nsme.printStackTrace();
		}
	}
	
	/** CALLED STATIC when initializing dispatcher... this is statically configuring the loadBalancers for stateless and stateful ops*/
	public void configureLoadBalancers(OperatorContext opContext) {
		//For every type of downstream (statefull or stateless) create an according loadBalancer.
		for(Integer contentValue : routeInfo.keySet()){
			for(Integer opId : routeInfo.get(contentValue)){
				//If there are no load balancer configured for the OpId, we configure one
				if(!downTypeToLoadBalancer.containsKey(opId)){
					int index = opContext.findDownstream(opId).index();
					if(opContext.isDownstreamOperatorStateful(opId)){
						StatefulDynamicLoadBalancer lb = new StatefulDynamicLoadBalancer(index);
System.out.println("CREATING STATEFUL LB : "+opId);
						downTypeToLoadBalancer.put(opId, lb);
					}
				//If the operator is stateless
					else{
						//Default size for splitWindow
						int splitWindow = 1;
						StatelessDynamicLoadBalancer lb = new StatelessDynamicLoadBalancer(splitWindow ,index);
System.out.println("CREATING STATELESS LB : "+opId);
						downTypeToLoadBalancer.put(opId, lb);
					}
				}
			}
		}
	}
	
	public void configureLoadBalancer(int opId, List<Integer> downstreamOpId){
		//We gather lb of opId
		LoadBalancerI lb = downTypeToLoadBalancer.get(opId);
		//And assign it to the rest of downstreams
		for(Integer down : downstreamOpId){
			//For every downstream operator (for every replica) I always assign the new routing information, so the new load balancer.
			NodeManager.nLogger.info("-> Creating LB for OP: "+down);
			downTypeToLoadBalancer.put(down, lb);
		}
	}
	
	//Gather indexes from statefulDynamic Load balancer
	public ArrayList<Integer> getIndexesInformation(int oldOpId){
		LoadBalancerI lb = downTypeToLoadBalancer.get(oldOpId);
		return ((StatefulDynamicLoadBalancer)lb).getKeyToDownstreamRealIndex();
	}
	
	//Gather keys from statefulDynamic Load balancer
	public ArrayList<Integer> getKeysInformation(int oldOpId){
		LoadBalancerI lb = downTypeToLoadBalancer.get(oldOpId);
		return ((StatefulDynamicLoadBalancer)lb).getDownstreamNodeKeys();
	}
	
	/// \test{necessary anymore?} Install indexes from downstream message in the load balancer 
	public void setIndexesInformation(List<Integer> indexes, int opId){
		LoadBalancerI lb = downTypeToLoadBalancer.get(opId);
System.out.println("INSTALL INDEXES: "+indexes);
if(lb == null) System.out.println("LB IS NULL NULL NULL");
		((StatefulDynamicLoadBalancer)lb).setKeyToDownstreamRealIndex(new ArrayList<Integer>(indexes));
	}
	
	// \test{necessary anymore?} Install keys from downstream message in the load balancer
	public void setKeysInformation(List<Integer> keys, int opId){
		LoadBalancerI lb = downTypeToLoadBalancer.get(opId);
System.out.println("INSTALL KEYS: "+keys);
		((StatefulDynamicLoadBalancer)lb).setDownstreamNodeKeys(new ArrayList<Integer>(keys));
	}
	
	//Set load balancer for a given operator
	public void setNewLoadBalancer(int opId, LoadBalancerI lb){
		downTypeToLoadBalancer.put(opId, lb);
	}
	
	public boolean hasLBForOperator(int opId){
		return downTypeToLoadBalancer.containsKey(opId);
	}
	
	/** CALLED STATIC */
	public void routeValueToDownstream(RelationalOperator operator, int value, int downstream){
		routeValueToDownstream(this.query, operator, value, downstream);
	}
	
	/** CALLED STATIC */
	//if less or greater is than a given value. if equal could be with many values, with range is a special case as well
	public void routeValueToDownstream(String query, RelationalOperator operator, int value, int downstream){
		if(query.equals(query)){
			//if it is operator EQUALS, use specific routeInfo
			if(operator.equals(RelationalOperator.EQ)){
				//If there was a downstream assigned for this value
				if(routeInfo.containsKey(value)){
					// add the new downstream
					routeInfo.get(value).add(downstream);
				}
				else{
					ArrayList<Integer> aux = new ArrayList<Integer>();
					aux.add(downstream);
					routeInfo.put(value,aux);
				}
			}
		}
		else{
			//Other different query... NOT SUPPORTED YET
		}
	}
	
	/** this can be moved along with downTypeToLoadBalancer */
	public int newSplit(int oldOpId, int newOpId, int oldOpIndex, int newOpIndex) {
		//We gather the load balancer for the operator splitting (the old one)
		LoadBalancerI lb = downTypeToLoadBalancer.get(oldOpId);
if(lb == null){
System.out.println("LB for OP: "+oldOpId+" is null !!!!!!!!!!");
System.out.println("OPIds: "+downTypeToLoadBalancer.keySet());
}
		//Then we update this load balancer (the old one) with the new information
		int key = lb.newReplica(oldOpIndex, newOpIndex);
		//Now since we have a new replica, we want to assign the same load balancer to this replica so that it has the same route information
		NodeManager.nLogger.info("-> Registering NEW LB for OP: "+newOpId);
		downTypeToLoadBalancer.put(newOpId, lb);
		//And finally we return the new key computed
		return key;
	}
	
	public ArrayList<Integer> applyFilter(Seep.DataTuple dt, int value) {
		ArrayList<Integer> targets = new ArrayList<Integer>();
		
		int contentValue = 0;
		
		try {
			contentValue = (Integer)queryFunction.invoke(dt);
		}
		catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//For every type of downstream that match the filter
		for(Integer opId : routeInfo.get(contentValue)){
			//We append the downstreams returned
			targets = (downTypeToLoadBalancer.get(opId).route(targets, value));
		}
		return targets;
	}

	@Override
	public ArrayList<Integer> route(ArrayList<Integer> targets, int value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int newReplica(int oldOpIndex, int newOpIndex) {
		// TODO Auto-generated method stub
		return -1;
	}
}