package seep.operator;

import java.io.Serializable;
import java.util.ArrayList;

import seep.comm.routing.Router;
import seep.comm.serialization.DataTuple;
import seep.infrastructure.NodeManager;
import seep.runtimeengine.CoreRE;
import seep.runtimeengine.RuntimeContext;

public abstract class Operator implements Serializable, QuerySpecificationI{
	
	private static final long serialVersionUID = 1L;
	
	private int operatorId;
	private RuntimeContext opContext = null;
	private boolean ready = false;
	public Operator subclassOperator = null;
	
	public void setReady(boolean ready){
		this.ready = ready;
	}
	
	public Operator getSubclassOperator() {
		return subclassOperator;
	}

	public abstract void processData(DataTuple dt);
	
	@Override 
	public String toString() {
		return "Operator [operatorId=" + operatorId + ", opContext="
				+ opContext + "]";
	}
	
	
/** Implementation of QuerySpecificationI **/
	
	public int getOperatorId(){
		return operatorId;
	}
	
	public RuntimeContext getOpContext(){
		return opContext;
	}
	
	public void setOpContext(RuntimeContext opContext){
		this.opContext = opContext;
	}
	
	public void setOriginalDownstream(ArrayList<Integer> originalDownstream){
		this.opContext.setOriginalDownstream(originalDownstream);
	}
	
	public void connectTo(QuerySpecificationI down, boolean originalQuery) {
		opContext.addDownstream(down.getOperatorId());
		if(originalQuery)opContext.addOriginalDownstream(down.getOperatorId());
		down.getOpContext().addUpstream(getOperatorId());
//		NodeManager.nLogger.info("Operator: "+this.toString()+" is now connected to Operator: "+down.toString());
	}
	
	public void setRoutingQueryFunction(String queryFunction_methodName){
		router.setQueryFunction(queryFunction_methodName);
		NodeManager.nLogger.info("Configured Routing Query Function: "+queryFunction_methodName+" in Operator: "+this.toString());
	}
	
	public void route(Router.RelationalOperator operand, int value, Operator toConnect){
		int opId = toConnect.getOperatorId();
		router.routeValueToDownstream(operand, value, opId);
		NodeManager.nLogger.info("Operator: "+this.toString()+" sends data with value: "+value+" to Operator: "+toConnect.toString());
	}
	
	public void scaleOut(CoreRE toScaleOut){
		//TODO implement static scaleOut
	}

}
