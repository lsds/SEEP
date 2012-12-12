package seep.comm.routing;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.CRC32;

import seep.comm.serialization.DataTuple;
import seep.infrastructure.NodeManager;
import seep.operator.OperatorContext;
import seep.operator.OperatorContext.PlacedOperator;


public class Router implements Serializable{

	private static final long serialVersionUID = 1L;

	private static CRC32 crc32 = new CRC32();
	
	private Method queryFunction = null;
	private boolean requiresQueryData = false;
		
	//This map stores static info (for different types of downstream operators). Content-value -> list of downstreams
	public HashMap<Integer, ArrayList<Integer>> routeInfo = new HashMap<Integer, ArrayList<Integer>>();
	
	//This map stores the load balancer for each type of downstream. This map is related to routeInfo
	private HashMap<Integer, RoutingStrategyI> downstreamRoutingImpl = new HashMap<Integer, RoutingStrategyI>();
	
	//This structure rests here in case there is just one type of downstream, in case routeInfo is empty
	// change this for a final int position in downstreamRoutingImpl ??
	private final int INDEX_FOR_ROUTING_IMPL = 0; 
//	private RoutingStrategyI routingImpl = null;
	
	public enum RelationalOperator{
		//LEQ, L, EQ, G, GEQ, RANGE
		//For now just supported equals operator
		EQ
	}
	
	public Router(String query, HashMap<Integer, ArrayList<Integer>> routeInfo){
		if(query != null){
			this.requiresQueryData = true;
		}
		this.queryFunction = this.initializeQueryFunction(query);
		this.routeInfo = routeInfo;
	}
	
	/// \fixme{how to make this generic so that it always knows which class to query?}
	public Method initializeQueryFunction(String query){
		if(query != null){
			NodeManager.nLogger.info("Initializing method to query stream data...");
			try {
				Class<DataTuple> c = DataTuple.class;
				queryFunction = c.getMethod(query);
				return queryFunction;
			}
			catch (NoSuchMethodException nsme){
				nsme.printStackTrace();
			}
		}
		return null;
	}
	
	//Gather indexes from statefulDynamic Load balancer
	public ArrayList<Integer> getIndexesInformation(int oldOpId){
try{
		RoutingStrategyI rs = null;
		if(!requiresQueryData){
			rs = downstreamRoutingImpl.get(INDEX_FOR_ROUTING_IMPL);
			return ((StatefulRoutingImpl)rs).getKeyToDownstreamRealIndex();
		}
		rs = downstreamRoutingImpl.get(oldOpId);
		return ((StatefulRoutingImpl)rs).getKeyToDownstreamRealIndex();
}
catch(ClassCastException cce){
	System.out.println("HACKED-HACKED-HACKED-HACKED-HACKED-HACKED-HACKED");
}
//remove with the try-catch
return null;
	}
	
	//Gather keys from statefulDynamic Load balancer
	public ArrayList<Integer> getKeysInformation(int oldOpId){
try{
		RoutingStrategyI rs = null;
		if(!requiresQueryData){
			rs = downstreamRoutingImpl.get(INDEX_FOR_ROUTING_IMPL);
			return ((StatefulRoutingImpl)rs).getDownstreamNodeKeys();
		}
		rs = downstreamRoutingImpl.get(oldOpId);
		return ((StatefulRoutingImpl)rs).getDownstreamNodeKeys();
}
catch(ClassCastException cce){
System.out.println("HACKED-HACKED-HACKED-HACKED-HACKED-HACKED-HACKED");
}
//remove with the try-catch
return null;
}
	
//	public void setQueryFunction(String query){
//		this.query = query;
//		//If a function is defined, the it is necessary to query the data
//		this.requiresQueryData = true;
//	}
	
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
	
	/**
	 * this function must be executed when the operator is not initialized yet. at this point all it has is the main topology of the query
	 * and so it needs just to put a routingImpl per oprator. Now, the problem is that when this is being called because of a scale out/scale in
	 * the downstream is not the original one. NEED to differentiate between main/execution graph. Or, make explicit whith type is and call as it is required
	 * CHANGE-> actually the DIFFERENTIATION IS required
	**/
	public void _configureRoutingImpl(OperatorContext opContext){
		RoutingStrategyI rs = null;
		int opId = 0;
		//For every downstream
		for(PlacedOperator down : opContext.downstreams){
			opId = down.opID();
			int index = opContext.findDownstream(opId).index();
			if(!down.isStateful()){
				int numDownstreams = opContext.getDownstreamSize();
				rs = new StatelessRoutingImpl(1, index, numDownstreams);
			}
			else if(down.isStateful()){
				rs = new StatefulRoutingImpl(index);
			}
			System.out.println("ADDED");
			downstreamRoutingImpl.put(opId, rs);
		}
		NodeManager.nLogger.info("ROUTING ENGINE CONFIGURED");
	}
	
	public void configureRoutingImpl(OperatorContext opContext){
		RoutingStrategyI rs = null;
		int opId = 0;
		//For every downstream in the original query graph
		System.out.println("ORIGINAL DOWN SIZE: "+opContext.getOriginalDownstream().size());
		for(Integer id : opContext.getOriginalDownstream()){
//		for(PlacedOperator down : opContext.downstreams){
			PlacedOperator down = opContext.findDownstream(id);
			int index = down.index();
			if(!down.isStateful()){
				int numDownstreams = opContext.getDownstreamSize();
				rs = new StatelessRoutingImpl(1, index, numDownstreams);
			}
			else if(down.isStateful()){
				//We crash the stateful RI temporarily, anyway it will be recovered by the RI message
				rs = new StatefulRoutingImpl(index);
			}
			System.out.println("ADDED");
			//If more than one downstream type, then put the new rs with the opId
			if(opContext.downstreams.size() > 1){
				downstreamRoutingImpl.put(opId, rs);
			}
			//Otherwise, store the rs in the reserved place of downstreamRoutingImpl
			else if (opContext.downstreams.size() == 1){
				downstreamRoutingImpl.put(INDEX_FOR_ROUTING_IMPL, rs);
			}
		}
		NodeManager.nLogger.info("Routing Engine Configured");
	}
	
	public ArrayList<Integer> forward(DataTuple dt, int value, boolean now){
		ArrayList<Integer> targets = new ArrayList<Integer>();
		//If it is necessary to query data to guess (logic)downstream
		if(requiresQueryData){
//			System.out.println("REQUIRES QUERY DATA");
			ArrayList<Integer> logicalTargets = routeLayerOne(dt, value);
//			System.out.println("LOGICAL TARGETS: "+logicalTargets.size());
			targets = routeLayerTwo(logicalTargets, value);
		}
		else{
//			System.out.println("NO query data");
			//Otherwise, we use the default RoutingImpl
//			/**TSTING **/
//			
//			RoutingStrategyI rsi = downstreamRoutingImpl.get(INDEX_FOR_ROUTING_IMPL);
//			if(rsi instanceof StatelessRoutingImpl){
//				System.out.println("STATELESS DOWNSTREAM");
//			}
//			else if (rsi instanceof StatefulRoutingImpl){
//				System.out.println("STATEFUL DOWNSTREAM");
//				ArrayList<Integer> keys = ((StatefulRoutingImpl)rsi).getDownstreamNodeKeys();
//				if(!keys.isEmpty()){
//					System.out.println("KEYS: "+keys.toString());
//				}
//				else{
//					System.out.println("KEYS is empty");
//				}
//				
//				ArrayList<Integer> indexes = ((StatefulRoutingImpl)rsi).getKeyToDownstreamRealIndex();
//				if(!indexes.isEmpty()){
//					System.out.println("INDEXES: "+indexes.toString());
//				}
//				else{
//					System.out.println("INDEXES is empty");
//				}
//			}

//			/**TSTING **/
			
			targets = downstreamRoutingImpl.get(INDEX_FOR_ROUTING_IMPL).route(value);
//			targets = routingImpl.route(value);
		}
		return targets;
	}
	
	public ArrayList<Integer> routeLayerOne(DataTuple dt, int value){
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
//			key = routingImpl.newReplica(oldOpIndex, newOpIndex);
			
			key = downstreamRoutingImpl.get(INDEX_FOR_ROUTING_IMPL).newReplica(oldOpIndex, newOpIndex);
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

	/**this function was made public on 17 oct 2012 to enable a higher level hack**/
	
	public void setNewLoadBalancer(int opId, RoutingStrategyI rs){
		if(requiresQueryData){
			downstreamRoutingImpl.put(opId, rs);
		}
		else{
			downstreamRoutingImpl.put(INDEX_FOR_ROUTING_IMPL, rs);
		}
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
	
	public static int customHash(String value){
		/// \todo{Search for a more efficient way of hashing a java string}
		int v = 0; 
		v = value.hashCode();
		return customHash(v);
	}
}