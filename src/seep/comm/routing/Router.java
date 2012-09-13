package seep.comm.routing;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import seep.comm.tuples.Seep;
import seep.operator.OperatorContext;

public class Router implements Serializable{

	private String query = null;
	private Method queryFunction = null;
	
	//This map stores static info (for different types of downstream operators). Content-value -> list of downstreams
	public HashMap<Integer, ArrayList<Integer>> routeInfo = new HashMap<Integer, ArrayList<Integer>>();
	
	//This map stores the load balancer for each type of downstream
	private HashMap<Integer, RoutingStrategyI> downTypeToLoadBalancer = new HashMap<Integer, RoutingStrategyI>();
	
	public enum RelationalOperator{
		//LEQ, L, EQ, G, GEQ, RANGE
		//For now just supported equals operator
		EQ
	}
	
	public Router(){
		
	}
	
	public void setQueryFunction(String query){
		this.query = query;
	}
	
	public ArrayList<Integer> routeLayerOne(){
		return null;
		
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
	
	public void configureRoutingImpl(OperatorContext opContext) {
		//For every type of downstream (statefull or stateless) create an according routingStrategyI.
		for(Integer contentValue : routeInfo.keySet()){
			for(Integer opId : routeInfo.get(contentValue)){
				//If there are no load balancer configured for the OpId, we configure one
				if(!downTypeToLoadBalancer.containsKey(opId)){
					int index = opContext.findDownstream(opId).index();
					if(opContext.isDownstreamOperatorStateful(opId)){
//						StatefulDynamicLoadBalancer lb = new StatefulDynamicLoadBalancer(index);
						StatefulRoutingImpl rfull = new StatefulRoutingImpl(index);
System.out.println("CREATING STATEFUL LB : "+opId);
						downTypeToLoadBalancer.put(opId, rfull);
					}
					//If the operator is stateless
					else{
						//Default size for splitWindow
						int splitWindow = 1;
//						StatelessDynamicLoadBalancer lb = new StatelessDynamicLoadBalancer(splitWindow ,index);
						StatelessRoutingImpl rless = new StatelessRoutingImpl();
System.out.println("CREATING STATELESS LB : "+opId);
						downTypeToLoadBalancer.put(opId, rless);
					}
				}
			}
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
	
	
}
