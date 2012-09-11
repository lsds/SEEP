package seep.comm;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	public enum RouteOperator{
		//LEQ, L, EQ, G, GEQ, RANGE
		//For now just supported equals operator
		EQ
	}

	public ContentBasedFilter(String query){
		this.query = query;
//		isRoutableByFixedContent = true;
	}
	


	public void initializeFilters(){
		try {
			Class<Seep.DataTuple> c = seep.comm.tuples.Seep.DataTuple.class;
			queryFunction = c.getMethod(query);
		}
		catch (NoSuchMethodException nsme){
			nsme.printStackTrace();
		}
	}
	
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
//			if(!downTypeToLoadBalancer.containsKey(down)){
			NodeManager.nLogger.info("-> Creating LB for OP: "+down);
			downTypeToLoadBalancer.put(down, lb);
//			}
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
	
	public void routeValueToDownstream(RouteOperator operator, int value, int downstream){
		routeValueToDownstream(this.query, operator, value, downstream);
	}
	
	//if less or greater is than a given value. if equal could be with many values, with range is a special case as well
	public void routeValueToDownstream(String query, RouteOperator operator, int value, int downstream){
		if(query.equals(query)){
			//if it is operator EQUALS, use specific routeInfo
			if(operator.equals(RouteOperator.EQ)){
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
			/*
			//only supported equals operator by now
			else{
				downCommFilter.addDownstream(downstream);
			}
			//argument of the operator
			addArgument(value);
			*/
		}
		else{
			//Other different query... NOT SUPPORTED YET
		}
	}
	
	//public ArrayList<Integer> applyFilter(Seep.DataTuple dt, int value) {
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
//System.out.println("GETTING targets of group opId: "+opId);
			targets = (downTypeToLoadBalancer.get(opId).route(targets, value));
		}
		return targets;
		/*//In case this is a consistent-hashing case
		if(query == null){
//System.out.println("2- Route by hash");
			return structure.route(value);
		}
		//otherwise, apply filter and possibly chained filter
		else{
//System.out.println("2- Route by Type");
			return routeByDownstreamType(dt, value);
		}*/
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



	public void print() {
		for(Integer key : downTypeToLoadBalancer.keySet()){
			if(downTypeToLoadBalancer.get(key) instanceof StatefulDynamicLoadBalancer){
//			((StatefulDynamicLoadBalancer)downTypeToLoadBalancer.get(key)).get
				System.out.println("INDEXES: "+getIndexesInformation(key));
				System.out.println("KEYS: "+getKeysInformation(key));
			}
		}
	}
	
}
	/*public ArrayList<Integer> routeByDownstreamType(Seep.DataTuple dt, int value){
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
		
		//We have now the value for routing by Type
		//We check if routing by split OR BY ANY applies
		//If not empty...
		if(!(contentValueToLoadBalancer.isEmpty())){
//System.out.println("3- Route by Hash, contentValue: "+contentValue);
			//Apply the consistent-hashing to the structures that store this contentValue
			ArrayList<Integer> targets = new ArrayList<Integer>();
System.out.println("contentValue: "+contentValue+" DOWNFILTER-SIZE: "+contentValueToLoadBalancer.get(contentValue).size());
			for(LoadBalancerI filter : contentValueToLoadBalancer.get(contentValue)){
				if(filter instanceof StatefulDynamicLoadBalancer){
					targets.addAll(((StatefulDynamicLoadBalancer)filter).route(value));
				}
				//If not an consistenhashing, then it is an ANY filter
				else{
				/// \todo avoid the overhead of creatint an arraylist here, this must be improved...
					targets.add(((StatelessDynamicLoadBalancer)filter).route());
				}
			}
			return targets;
		}
		//If empty, then just lookup the routeInfo and return
		else{
//System.out.println("3- DONE return targets");
			return routeInfo.get(contentValue);
		}
	}*/
	
//	public void addChainFilter(Filter filter) {
//	//If exists, means that this is the case, many down types, can scale out
//	if(downCommFilter != null){
//		this.downCommFilter.valueToChain = filter.valueToChain;
//		filter.valueToChain = -1;
////System.out.println("CBF: ASSIGNING THIS CHAIN FILTER: "+filter);
//		this.downCommFilter.chainedFilter = filter;
//		this.downCommFilter.chainedFilter.init();
//	}
//	//If not exist. Just one down type, can scale out
//	else{
//		//empty constructor for consistent-hashing case
//		downCommFilter = new Filter();
//	}
//}
//
//public int retrieveValueToChainFromDownstream(int downOpId){
//	int valueToChain = 0;
//	valueToChain = downCommFilter.getKeyFromValue(downOpId);
//	return valueToChain;	
//}
	
	/*
	//Only supported equals operator
	public void routeValueToDownstream(String query, RouteOperator operator, int[] value, int downstream){
		//locate filter for this query
//		for(Filter f : filters){
//			if(f.queryFunction.equals(query)){
		downCommFilter.addArgument(value[0]);
		downCommFilter.addArgument(value[1]);
//			}
//		}
	}
	*/
	
	/*public void reconfigureRouteInfo(int oldOpIndex, int newOpIndex) {
	//oldOpId is splitted, and there is a new operator split now.
	//We localize the index key of the previous (old) operator
	int key = reverseMap(oldOpIndex);
	if(key != -1){
		//In that position, now there are a new split, so we add it.
		routeInfo.get(key).add(newOpIndex);
	}
}*/

/*public void updateStreamFilterStructures(int oldOpIndex, int newOpIndex) {
	// Get the key to access the correct filter
	int key = reverseMap(oldOpIndex);
	((StatelessDynamicLoadBalancer)indexToLoadBalancer.get(key)).newSplit(newOpIndex);
	//((StreamSplitterFilter)downTypeToChainFilter.get(key)).newSplit(newOpIndex);
}*/

//public int updateConsistentHashingStructures(int oldOpIndex, int newOpIndex){
//	//We get the key to the correct routingStructure by reverse mapping the routeInfo with the oldOpId
//	//A Bijection data structure would be necessary but since this is an operation that is done just a few times, Ill do it manually
//	int key = reverseMap(oldOpIndex);
//	if(key != -1){
//System.out.println("ACCESSING to downTypeRoutingStructure: "+key);
//		return ((StatefulDynamicLoadBalancer)indexToLoadBalancer.get(key)).updateDataStructures(oldOpIndex, newOpIndex);
//	}
//	return -1;
//}

//private int reverseMap(int oldOpId) {
//	//Loop through the keyset looking for a match
//	for(Integer key : routeInfo.keySet()){
//		//if this key has our downstream, this is the key we want
//		if(routeInfo.get(key).indexOf(oldOpId) != -1){
//			return key;
//		}
//	}
//	return -1;
//}

/*//In case. Only one downstream type, can scale out
if(isRoutableByHash && !isRoutableByFixedContent){
System.out.println("ONLY IS ROUTABLE PER HASH");
	//Create the routingStructure to do consistent hashing
	//In this case we pass all the downstreams, because all are of the same type
	ArrayList<Integer> downIndexes = opContext.getListOfDownstreamIndexes();
	//structure = new ConsistentHashingUtil(downIndexes);
	//since just one split is permitted from the beginning we can get the position 0
	structure = new StatefulDynamicLoadBalancer(downIndexes.get(0));
}
//otherwise, create the structures for the different downstream types
else{
System.out.println("ROUTABLE PER TYPE AND HASH/ANY");
	ArrayList<Integer> visited = new ArrayList<Integer>();
	for(Integer value : routeInfo.keySet()){
		for(Integer index : routeInfo.get(value)){
	//for(PlacedOperator down : opContext.downstreams){
			int opId = opContext.getOpIdFromIndex(index);
		//int opId = down.opID();
		//int index = down.index();
			//For every downstream, check if it is stateless or stateful and add a filter accordingly (streamSplit or consistentHash)
			if(opContext.isDownstreamOperatorStateful(opId)){
				//Previously we passed all the downstreams, this is an error since the downstream can be a mix of stateful and stateless operators
				//downTypeToChainFilter.put(value, new ConsistentHashingUtil(routeInfo.get(value)));
System.out.println("DOWNCHAIN: "+index+" consistentHash");
				StatefulDynamicLoadBalancer f = new StatefulDynamicLoadBalancer(index);
				if(contentValueToLoadBalancer.get(index) == null){
					ArrayList<LoadBalancerI> aux = new ArrayList<LoadBalancerI>();
					aux.add(f);
					contentValueToLoadBalancer.put(index, aux);
				}
				else if(visited.indexOf(opId) != -1){
					contentValueToLoadBalancer.get(index).add(f);
				}
				//update support structure
				indexToLoadBalancer.put(index, f);
			}
			else{
				System.out.println("CONFIGURING NEW STATELESS DOWN");
				//Create a new stream splitter filter with splitWindow 0 and passing the real index
System.out.println("DOWNCHAIN: "+index+" streamSplitter");
				StatelessDynamicLoadBalancer f = new StatelessDynamicLoadBalancer(1, index);
				if(contentValueToLoadBalancer.get(index) == null){
					ArrayList<LoadBalancerI> aux = new ArrayList<LoadBalancerI>();
					aux.add(f);
					contentValueToLoadBalancer.put(index, aux);
				}
				else if(visited.indexOf(opId) != -1){
					contentValueToLoadBalancer.get(index).add(f);
				}
				//update support structure
				indexToLoadBalancer.put(index, f);
			}
			//this opId has been served
			visited.add(opId);
		}
	}
}
//	public void chainFilter(int oldOpId, int newOpId) {
//		//Get contentValue of oldOpId
//		int key = reverseMap(oldOpId);
//		//With the key
//		ArrayList<LoadBalancerI> aux = new ArrayList<LoadBalancerI>();
//		aux.add(new StatelessDynamicLoadBalancer());
//		contentValueToLoadBalancer.put(key, aux);
//	}

}*/

//public ContentBasedFilter(boolean isRoutableByHash) {
//this.isRoutableByHash = isRoutableByHash;
//}
//
//public void setIsRoutableByHash(boolean isRoutableByHash){
//this.isRoutableByHash = isRoutableByHash;
//}

//This structure stores the routingStructures necessary to do consistent hashing. The size equals the number of different downstream types that can scale out
/// \todo{refactor this thing, with chainedFilter should be enough}
//private StatefulDynamicLoadBalancer structure = null;
//
//public StatefulDynamicLoadBalancer getStructure() {
//	return structure;
//}

//This structure stores index - array[RoutingStructure]. RoutingStructure can be consistenthashingUtil for stateful or streamsplitter for stateless
//private HashMap<Integer, ArrayList<LoadBalancerI>> contentValueToLoadBalancer = new HashMap<Integer, ArrayList<LoadBalancerI>>();

//support structure for updating the structures. downstreamIndex - LoadBalancer
//private HashMap<Integer, LoadBalancerI> indexToLoadBalancer = new HashMap<Integer, LoadBalancerI>();

//private Filter downCommFilter = null;

//private boolean isRoutableByFixedContent = false;
//private boolean isRoutableByHash = false;

//public boolean isRoutableByHash() {
//	return isRoutableByHash;
//}