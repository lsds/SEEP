/*******************************************************************************
 * Copyright (c) 2013 Imperial College London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial design and implementation
 *     Martin Rouaux - Changes to support scale-in of operators
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.comm.routing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.DownUpRCtrl;
import uk.ac.imperial.lsds.seep.manet.IRouter;
import uk.ac.imperial.lsds.seep.operator.Operator;
import uk.ac.imperial.lsds.seep.operator.OperatorContext;
import uk.ac.imperial.lsds.seep.operator.StatefulOperator;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;


public class Router implements Serializable{

	final private Logger LOG = LoggerFactory.getLogger(Router.class);
	
	private static final long serialVersionUID = 1L;

	private static final int DEFAULT_SPLIT_WINDOW = 1;
	//This structure rests here in case there is just one type of downstream, in case routeInfo is empty
	// change this for a final int position in downstreamRoutingImpl ??
	private static final int INDEX_FOR_ROUTING_IMPL = 0;

	private static CRC32 crc32 = new CRC32();
	
	//This map stores static info (for different types of downstream operators). StreamId -> list of downstreams
	private boolean requiresLogicalRouting = false;
	private HashMap<Integer, ArrayList<Integer>> routeInfo = new HashMap<Integer, ArrayList<Integer>>();
	
	//This map stores the load balancer for each type of downstream. This map is related to routeInfo
	private HashMap<Integer, RoutingStrategyI> downstreamRoutingImpl = new HashMap<Integer, RoutingStrategyI>();

	private ArrayList<IRoutingObserver> observers = new ArrayList<>(1);
	private IRouter frontierRouter = null;
	
	public enum RelationalOperator{
		//LEQ, L, EQ, G, GEQ, RANGE
		EQ
	}
	
	public Router(boolean requiresLogicalRouting, HashMap<Integer, ArrayList<Integer>> routeInfo){
		this.requiresLogicalRouting = requiresLogicalRouting;
		this.routeInfo = routeInfo;
	}
	
	public void setFrontierRouting(IRouter frontierRouter)
	{
		this.frontierRouter = frontierRouter;
	}
	
	public IRouter getFrontierRouting() { return frontierRouter; }
	
	//Gather indexes from statefulDynamic Load balancer
	public ArrayList<Integer> getIndexesInformation(int oldOpId){
		RoutingStrategyI rs = null;
		if(!requiresLogicalRouting){
			rs = downstreamRoutingImpl.get(INDEX_FOR_ROUTING_IMPL);
			return ((StatefulRoutingImpl)rs).getKeyToDownstreamRealIndex();
		}
		rs = downstreamRoutingImpl.get(oldOpId);
		return ((StatefulRoutingImpl)rs).getKeyToDownstreamRealIndex();
	}
	
	//Gather keys from statefulDynamic Load balancer
	public ArrayList<Integer> getKeysInformation(int oldOpId){
		RoutingStrategyI rs = null;
		if(!requiresLogicalRouting){
			rs = downstreamRoutingImpl.get(INDEX_FOR_ROUTING_IMPL);
			return ((StatefulRoutingImpl)rs).getDownstreamNodeKeys();
		}
		rs = downstreamRoutingImpl.get(oldOpId);
		return ((StatefulRoutingImpl)rs).getDownstreamNodeKeys();
}

	public void configureRoutingImpl(OperatorContext opContext, ArrayList<Operator> downstream){
		RoutingStrategyI rs = null;
		LOG.debug("-> Original downstream size: {}", downstream.size());
		// For each original downstream
		for(Integer id : opContext.getOriginalDownstream()){
			Operator down = null;
			for(Operator o : downstream){
				if(o.getOperatorId() == id){
					down = o;
				}
			}
			// Get index of downstream
			int index = opContext.getDownOpIndexFromOpId(id);
			if(down.getOperatorCode() instanceof StatelessOperator){
				int numDownstreams = downstream.size();
				LOG.debug("Configuring Static Stateless Routing Impl with {} downstreams", numDownstreams);
				// At this point there can only be 1 downstream (an operator can have N downstream types,
				//but only 1 instance of a given type Ni)
				rs = new StatelessRoutingImpl(DEFAULT_SPLIT_WINDOW, index, 1);
			}
			else if(down.getOperatorCode() instanceof StatefulOperator){
				//We crash the stateful RI temporarily, anyway it will be recovered by the RI message
				rs = new StatefulRoutingImpl(index);
			}
			
			//If more than one downstream type, then put the new rs with the opId
			if(opContext.getOriginalDownstream().size() > 1){
				LOG.debug("-> More than one downstream type. Assign RS for op: {}",id);
				downstreamRoutingImpl.put(id, rs);
			}
			
			//Otherwise, store the rs in the reserved place of downstreamRoutingImpl
			//else if (opContext.downstreams.size() == 1){
			else if(opContext.getOriginalDownstream().size() == 1){
				downstreamRoutingImpl.put(INDEX_FOR_ROUTING_IMPL, rs);
			}
		}
	}
	
	public ArrayList<Integer> routeToAll(ArrayList<Integer> logicalTargets){
		ArrayList<Integer> targets = new ArrayList<Integer>();
		for(Integer lt : logicalTargets){
			targets = downstreamRoutingImpl.get(lt).routeToAll(targets);
		}
		return targets;
	}
	
	public ArrayList<Integer> forwardToAllDownstream(DataTuple dt){
		ArrayList<Integer> targets = new ArrayList<Integer>();
		if(requiresLogicalRouting){
			ArrayList<Integer> logicalTargets = logicalRouting(dt, -1);
			targets = routeToAll(logicalTargets);
		}
		else{
			targets = downstreamRoutingImpl.get(INDEX_FOR_ROUTING_IMPL).routeToAll();
		}
		return targets;
	}
	
	public ArrayList<Integer> forwardToAllOpsInStreamId(DataTuple dt, int streamId){
		ArrayList<Integer> targets = new ArrayList<Integer>();
		ArrayList<Integer> logicalTargets = logicalRouting(dt, streamId);
		targets = routeToAll(logicalTargets);
		return targets;
	}
	
	public ArrayList<Integer> forward(DataTuple dt){
		int value = 0;
		checkDownstreamRoutingImpl();
		return downstreamRoutingImpl.get(INDEX_FOR_ROUTING_IMPL).route(value);
	}
	
	public ArrayList<Integer> forward_lowestCost(DataTuple dt)
	{
		checkDownstreamRoutingImpl();
		return downstreamRoutingImpl.get(INDEX_FOR_ROUTING_IMPL).route_lowestCost();
	}
	
	private void checkDownstreamRoutingImpl()
	{
		if(downstreamRoutingImpl == null){
			System.out.println("downstreamrouting impl null");
			System.exit(0); /// xtreme
		}
		if(downstreamRoutingImpl.get(INDEX_FOR_ROUTING_IMPL) == null){
			for(Integer i : downstreamRoutingImpl.keySet()){
				System.out.println(downstreamRoutingImpl.get(i));
			}
			System.out.println("idx for routing impl");
			System.exit(0); // xtreme
		}		
	}
	
	public ArrayList<Integer> forward_highestWeight(DataTuple dt)
	{
		LOG.debug("Routing data tuple: "+dt.getPayload().timestamp);
		if (frontierRouter == null) { throw new RuntimeException("Logic error?"); }
		checkDownstreamRoutingImpl();
		return frontierRouter.route(dt.getLong("tupleId"));
	}
	
	public Set<Long> areConstrained(Set<Long> batches)
	{
		return frontierRouter.areConstrained(batches);
	}
	
	public ArrayList<Integer> forward_toOp(DataTuple dt, int streamId){
		ArrayList<Integer> targets = new ArrayList<Integer>();
		ArrayList<Integer> logicalTargets = logicalRouting(dt, streamId);
		targets = physicalRouting(logicalTargets, streamId);
		return targets;
	}
	
	public ArrayList<Integer> forward_splitKey(DataTuple dt, int key){
		return downstreamRoutingImpl.get(INDEX_FOR_ROUTING_IMPL).route(key);
	}
	
	public ArrayList<Integer> forward_toOp_splitKey(DataTuple dt, int streamId, int key){
		ArrayList<Integer> targets = new ArrayList<Integer>();
		ArrayList<Integer> logicalTargets = logicalRouting(dt, streamId);
		targets = physicalRouting(logicalTargets, key);
		return targets;
	}
	
	private ArrayList<Integer> logicalRouting(DataTuple dt, int streamId){
		return routeInfo.get(streamId);
	}
	
	private ArrayList<Integer> physicalRouting(ArrayList<Integer> logicalTargets, int key){
		ArrayList<Integer> targets = new ArrayList<Integer>();
		for(Integer ltarget : logicalTargets){
			targets = downstreamRoutingImpl.get(ltarget).route(targets, key);
		}
		return targets;
	}
	
	public int[] newStaticOperatorPartition(int oldOpId, int newOpId, int oldOpIndex, int newOpIndex){
		int key[];
		if(requiresLogicalRouting){
			return configureRoutingStrategyForNewPartition(oldOpId, newOpId, oldOpIndex, newOpIndex);
		}
		else{
			//Otherwise, we use the default RoutingImpl
			key = downstreamRoutingImpl.get(INDEX_FOR_ROUTING_IMPL).newStaticReplica(oldOpIndex, newOpIndex);
		}
		return key;
	}
	
	public void update_lowestCost(int newTarget)
	{
		downstreamRoutingImpl.get(INDEX_FOR_ROUTING_IMPL).update_lowestCost(newTarget);
	}
	
	public void update_highestWeight(DownUpRCtrl downUp)
	{
		if (frontierRouter == null) 
		{ 
			LOG.warn("Ignoring down-up routing ctrl - frontier router doesn't exist yet.");
			return;
		}
		if (downUp != null)
		{
			notifyObservers(frontierRouter.handleDownUp(downUp));
		}
		else
		{
			//downstreamRoutingImpl.get(INDEX_FOR_ROUTING_IMPL).update_highestWeight(newTarget);
			notifyObservers(null);
		}
	}

	public void update_highestWeights(Map<Integer, Double> weights, Integer downOpUpdated)
	{	
		if (frontierRouter == null) 
		{ 
			LOG.warn("Ignoring down-up routing ctrl - frontier router doesn't exist yet.");
			return;
		}
		notifyObservers(frontierRouter.handleWeights(weights, downOpUpdated));
	}

	public void update_downFailed(int downOpId)
	{
		if (frontierRouter == null) 
		{ 
			LOG.warn("Ignoring down-up routing ctrl - frontier router doesn't exist yet.");
			return;
		}
		
		frontierRouter.handleDownFailed(downOpId);
		notifyObservers(null);
	}
	
	public void addObserver(IRoutingObserver o)
	{
		observers.add(o);
	}
	private void notifyObservers(Map<Integer, Set<Long>> newConstraints)
	{
		for (IRoutingObserver o : observers)
		{
			o.routingChanged(newConstraints);
		}
	}
	
	public int[] newOperatorPartition(int oldOpId, int newOpId, int oldOpIndex, int newOpIndex){
		int key[];
		if(requiresLogicalRouting){
			return configureRoutingStrategyForNewPartition(oldOpId, newOpId, oldOpIndex, newOpIndex);
		}
		else{
			//Otherwise, we use the default RoutingImpl
			key = downstreamRoutingImpl.get(INDEX_FOR_ROUTING_IMPL).newReplica(oldOpIndex, newOpIndex);
		}
		return key;
	}
    
    public int[] collapseOperatorPartition(int operatorId, int operatorIndex) {
        int key[];
        
        key = downstreamRoutingImpl.get(INDEX_FOR_ROUTING_IMPL).collapseReplica(operatorIndex);
        return key;
    }
	
	/** this can be moved along with downTypeToLoadBalancer */
	private int[] configureRoutingStrategyForNewPartition(int oldOpId, int newOpId, int oldOpIndex, int newOpIndex) {
		//We gather the load balancer for the operator splitting (the old one)
		RoutingStrategyI rs = downstreamRoutingImpl.get(oldOpId);
		
if(rs == null){
System.out.println("LB for OP: "+oldOpId+" is null !!!!!!!!!!");
System.out.println("OPIds: "+downstreamRoutingImpl.keySet());
}

		//Then we update this load balancer (the old one) with the new information
		int key[] = rs.newReplica(oldOpIndex, newOpIndex);
		//Now since we have a new replica, we want to assign the same load balancer to this replica so that it has the same route information
		LOG.debug("-> Registering NEW LB for OP: {}",newOpId);
		downstreamRoutingImpl.put(newOpId, rs);
		//And finally we return the new key computed
		return key;
	}
	
	private void setNewLoadBalancer(int opId, RoutingStrategyI rs){
		if(requiresLogicalRouting){
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

//@Deprecated
//public ArrayList<Integer> forward(DataTuple dt, int value, boolean now){
//	ArrayList<Integer> targets = new ArrayList<Integer>();
//	
//	// Check if i have data to query data. branches
//	
//	//If it is necessary to query data to guess (logic)downstream
//	if(requiresQueryData){
//		ArrayList<Integer> logicalTargets = logicalRouting(dt, value);
//		targets = physicalRouting(logicalTargets, value);
//	}
//	else{
//		targets = downstreamRoutingImpl.get(INDEX_FOR_ROUTING_IMPL).route(value);
//	}
//	return targets;
//}


//if less or greater is than a given value. if equal could be with many values, with range is a special case as well
//public void routeValueToDownstream(RelationalOperator operator, int value, int downstream){
//	//if it is operator EQUALS, use specific routeInfo
//	if(operator.equals(RelationalOperator.EQ)){
//		//If there was a downstream assigned for this value
//		if(routeInfo.containsKey(value)){
//			// add the new downstream
//			routeInfo.get(value).add(downstream);
//		}
//		else{
//			ArrayList<Integer> aux = new ArrayList<Integer>();
//			aux.add(downstream);
//			routeInfo.put(value,aux);
//		}
//	}
//}

/**
 * this function must be executed when the operator is not initialized yet. at this point all it has is the main topology of the query
 * and so it needs just to put a routingImpl per oprator. Now, the problem is that when this is being called because of a scale out/scale in
 * the downstream is not the original one. NEED to differentiate between main/execution graph. Or, make explicit whith type is and call as it is required
 * CHANGE-> actually the DIFFERENTIATION IS required
**/
/*public void _configureRoutingImpl(OperatorContext opContext){
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
}*/
