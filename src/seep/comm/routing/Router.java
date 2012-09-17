package seep.comm.routing;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.CRC32;

import seep.comm.tuples.Seep;
import seep.infrastructure.NodeManager;
import seep.operator.OperatorContext;
import seep.operator.OperatorContext.PlacedOperator;

public class Router implements Serializable{

	private static CRC32 crc32 = new CRC32();
	
	private String query = null;
	private Method queryFunction = null;
	private boolean requiresQueryData = false;
	
	//This map stores static info (for different types of downstream operators). Content-value -> list of downstreams
	public HashMap<Integer, ArrayList<Integer>> routeInfo = new HashMap<Integer, ArrayList<Integer>>();
	
	//This map stores the load balancer for each type of downstream
	private HashMap<Integer, RoutingStrategyI> downstreamRoutingImpl = new HashMap<Integer, RoutingStrategyI>();
	
	public enum RelationalOperator{
		//LEQ, L, EQ, G, GEQ, RANGE
		//For now just supported equals operator
		EQ
	}
	
	public Router(){
		
	}
	
	//Gather indexes from statefulDynamic Load balancer
	public ArrayList<Integer> getIndexesInformation(int oldOpId){
		RoutingStrategyI rs = downstreamRoutingImpl.get(oldOpId);
		return ((StatefulRoutingImpl)rs).getKeyToDownstreamRealIndex();
	}
	
	//Gather keys from statefulDynamic Load balancer
	public ArrayList<Integer> getKeysInformation(int oldOpId){
		RoutingStrategyI rs = downstreamRoutingImpl.get(oldOpId);
		return ((StatefulRoutingImpl)rs).getDownstreamNodeKeys();
	}
	
	public void setQueryFunction(String query){
		this.query = query;
		//If a function is defined, the it is necessary to query the data
		this.requiresQueryData = true;
	}
	
	//if less or greater is than a given value. if equal could be with many values, with range is a special case as well
	public void routeValueToDownstream(RelationalOperator operator, int value, int downstream){
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
	
	public void configureRoutingImpl(OperatorContext opContext){
		RoutingStrategyI rs = null;
		int opId = 0;
		//For every downstream
		for(PlacedOperator down : opContext.downstreams){
			opId = down.opID();
			if(!down.isStateful()){
				rs = new StatelessRoutingImpl();
			}
			else if(down.isStateful()){
				int index = opContext.findDownstream(opId).index();
				rs = new StatefulRoutingImpl(index);
			}
			downstreamRoutingImpl.put(opId, rs);
		}
	}
	
	public void initializeQueryFunction(){
		try {
			Class<Seep.DataTuple> c = seep.comm.tuples.Seep.DataTuple.class;
			queryFunction = c.getMethod(query);
		}
		catch (NoSuchMethodException nsme){
			nsme.printStackTrace();
		}
	}
	
	public ArrayList<Integer> forward(Seep.DataTuple dt, int value, boolean now){
		ArrayList<Integer> targets = new ArrayList<Integer>();
		//If it is necessary to query data to guess (logic)downstream
		if(requiresQueryData){
			ArrayList<Integer> logicalTargets = routeLayerOne(dt, value);
			targets = routeLayerTwo(logicalTargets, value);
		}
		else{
			//Otherwise, we use the default RoutingImpl
			//There will only be ONE entry in the map, this is an ugly "hack" to take advantage of the same data structure
			for(Integer target : downstreamRoutingImpl.keySet()){
				//This should be called just once...
				/// \fixme{CHECK THIS}
				targets = downstreamRoutingImpl.get(target).route(value);
			}
		}
		return targets;
	}
	
	public ArrayList<Integer> routeLayerOne(Seep.DataTuple dt, int value){
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
		return routeInfo.get(contentValue);
		/** THIS IS PART OF ROUTING LAYER-2 **/
//		//For every type of downstream that match the filter
//		for(Integer opId : routeInfo.get(contentValue)){
//			//We append the downstreams returned
//			targets = (downstreamRoutingImpl.get(opId).route(targets, value));
//		}
//		return targets;
	}
	
	public ArrayList<Integer> routeLayerTwo(ArrayList<Integer> logicalTargets, int value){
		ArrayList<Integer> targets = new ArrayList<Integer>();
		for(Integer ltarget : logicalTargets){
			targets = downstreamRoutingImpl.get(ltarget).route(targets, value);
		}
		return targets;
	}
	
	public int newOperatorPartition(int oldOpId, int newOpId, int oldOpIndex, int newOpIndex){
		int key = -1;
		if(requiresQueryData){
			return configureRoutingStrategyForNewPartition(oldOpId, newOpId, oldOpIndex, newOpIndex);
		}
		else{
			//Otherwise, we use the default RoutingImpl
			//There will only be ONE entry in the map, this is an ugly "hack" to take advantage of the same data structure
			for(Integer target : downstreamRoutingImpl.keySet()){
				//This should be called just once...
				/// \fixme{CHECK THIS}
				return downstreamRoutingImpl.get(target).newReplica(oldOpIndex, newOpIndex);
			}
		}
		return key;
	}
	
	/** this can be moved along with downTypeToLoadBalancer */
	public int configureRoutingStrategyForNewPartition(int oldOpId, int newOpId, int oldOpIndex, int newOpIndex) {
		//We gather the load balancer for the operator splitting (the old one)
		RoutingStrategyI rs = downstreamRoutingImpl.get(oldOpId);
if(rs == null){
System.out.println("LB for OP: "+oldOpId+" is null !!!!!!!!!!");
System.out.println("OPIds: "+downstreamRoutingImpl.keySet());
}
		//Then we update this load balancer (the old one) with the new information
		int key = rs.newReplica(oldOpIndex, newOpIndex);
		//Now since we have a new replica, we want to assign the same load balancer to this replica so that it has the same route information
		NodeManager.nLogger.info("-> Registering NEW LB for OP: "+newOpId);
		downstreamRoutingImpl.put(newOpId, rs);
		//And finally we return the new key computed
		return key;
	}

	private void setNewLoadBalancer(int opId, RoutingStrategyI rs){
		downstreamRoutingImpl.put(opId, rs);
	}
	
	public void reconfigureRoutingInformation(ArrayList<Integer> downstreamIds, ArrayList<Integer> indexes, ArrayList<Integer> keys) {
		for(Integer opId : downstreamIds){
			StatefulRoutingImpl sr = new StatefulRoutingImpl(indexes, keys);
			setNewLoadBalancer(opId, sr);
		}
	}
	
	public static int customHash(int value){
		crc32.update(value);
		int v = (int)crc32.getValue();
		crc32.reset();
		return v;
	}
}
//	public void configureRoutingImpl(OperatorContext opContext) {
//		//For every type of downstream (statefull or stateless) create an according routingStrategyI.
//		for(Integer contentValue : routeInfo.keySet()){
//			for(Integer opId : routeInfo.get(contentValue)){
//				//If there are no load balancer configured for the OpId, we configure one
//				if(!downstreamRoutingImpl.containsKey(opId)){
//					int index = opContext.findDownstream(opId).index();
//					if(opContext.isDownstreamOperatorStateful(opId)){
////						StatefulDynamicLoadBalancer lb = new StatefulDynamicLoadBalancer(index);
//						StatefulRoutingImpl rfull = new StatefulRoutingImpl(index);
//System.out.println("CREATING STATEFUL LB : "+opId);
//						downstreamRoutingImpl.put(opId, rfull);
//					}
//					//If the operator is stateless
//					else{
//						//Default size for splitWindow
//						int splitWindow = 1;
////						StatelessDynamicLoadBalancer lb = new StatelessDynamicLoadBalancer(splitWindow ,index);
//						StatelessRoutingImpl rless = new StatelessRoutingImpl();
//System.out.println("CREATING STATELESS LB : "+opId);
//						downstreamRoutingImpl.put(opId, rless);
//					}
//				}
//			}
//		}
//	}
	
//	public void newStatelessOperatorPartition(int oldOpId, int newOpId, int oldOpIndex, int newOpIndex){
//	if(requiresQueryData){
//		configureRoutingStrategyForNewPartition(oldOpId, newOpId, oldOpIndex, newOpIndex);
//	}
//	else{
//		//Otherwise, we use the default RoutingImpl
//		//There will only be ONE entry in the map, this is an ugly "hack" to take advantage of the same data structure
//		for(Integer target : downstreamRoutingImpl.keySet()){
//			//This should be called just once...
//			/// \fixme{CHECK THIS}
//			downstreamRoutingImpl.get(target).newReplica(oldOpIndex, newOpIndex);
//		}
//	}
//}