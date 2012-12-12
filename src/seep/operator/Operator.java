package seep.operator;

import java.io.Serializable;
import java.util.ArrayList;

import seep.comm.routing.Router;
import seep.comm.serialization.DataTuple;
import seep.infrastructure.NodeManager;
import seep.processingunit.ProcessingUnit;

public abstract class Operator implements Serializable, QuerySpecificationI, EndPoint{
	
	private static final long serialVersionUID = 1L;
	
	private int operatorId;
	private OperatorContext opContext = new OperatorContext();
	private State state = null;
	private boolean ready = false;
	public Operator subclassOperator = null;
	public ProcessingUnit processingUnit = null;
	private Router router = null;
	
	public Operator(int operatorId){
		this.operatorId = operatorId;
		subclassOperator = this;
		System.out.println("WHO AM I?? : "+subclassOperator.getClass().getName());
	}
	
	public State getState(){
		return state;
	}
	
	public void setReady(boolean ready){
		this.ready = ready;
	}
	
	public boolean getReady(){
		return ready;
	}
	
	public Router getRouter(){
		return router;
	}
	
	public void setRouter(Router router){
		this.router = router;
	}
	
	public Operator getSubclassOperator() {
		return subclassOperator;
	}
	
	public void setProcessingUnit(ProcessingUnit processingUnit){
		this.processingUnit = processingUnit;
	}

	public abstract void processData(DataTuple dt);
	
	/** Methods used by the developers to send data **/
	
	public void sendDown(DataTuple dt){
		/// \todo{FIX THIS, look for a value that cannot be present in the tuples...}
		// We check the targets with our routers
		ArrayList<Integer> targets = router.forward(dt, Integer.MIN_VALUE, false);
		processingUnit.sendData(dt, targets);
	}
	
	public void sendDown(DataTuple dt, int value){
		// We check the targets with our routers
		ArrayList<Integer> targets = router.forward(dt, value, false);
		processingUnit.sendData(dt, targets);
	}
	
/** Implementation of QuerySpecificationI **/
	
	public int getOperatorId(){
		return operatorId;
	}
	
	public OperatorContext getOpContext(){
		return opContext;
	}
	
	public void setOpContext(OperatorContext opContext){
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
//		router.setQueryFunction(queryFunction_methodName);
		opContext.setQueryFunction(queryFunction_methodName);
		NodeManager.nLogger.info("Configured Routing Query Function: "+queryFunction_methodName+" in Operator: "+this.toString());
	}
	
	public void route(Router.RelationalOperator operand, int value, Operator toConnect){
		int opId = toConnect.getOperatorId();
		//router.routeValueToDownstream(operand, value, opId);
		opContext.routeValueToDownstream(operand, value, opId);
		NodeManager.nLogger.info("Operator: "+this.toString()+" sends data with value: "+value+" to Operator: "+toConnect.toString());
	}
	
	public void scaleOut(Operator toScaleOut){
		//TODO implement static scaleOut
	}
	
	@Override 
	public String toString() {
		return "Operator [operatorId=" + operatorId + ", opContext="
				+ opContext + "]";
	}
}


//public void sendNow(DataTuple dt){
//	/// \todo{FIX THIS, look for a value that cannot be present in the tuples...}
//	processingUnit.sendData(dt, Integer.MIN_VALUE, true);
//}
//
//public void sendNow(DataTuple dt, int value){
//	processingUnit.sendData(dt, value, true);
//}
