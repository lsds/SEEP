package seep.comm.routing;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import seep.comm.tuples.Seep;
import seep.operator.Operator;
import seep.operator.OperatorContext;
import seep.operator.StatefullOperator;
import seep.operator.StatelessOperator;
import seep.operator.OperatorContext.PlacedOperator;

public class Router implements Serializable{

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
		//LogicalTargets saves the indexes of the downstream logical nodes (those for which a RoutinImpl exists)
		ArrayList<Integer> logicalTargets = new ArrayList<Integer>();
		//If it is necessary to query data to guess (logic)downstream
		if(requiresQueryData){
			logicalTargets = routeLayerOne(dt, value);
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
}
