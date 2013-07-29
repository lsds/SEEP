/*******************************************************************************
 * Copyright (c) 2013 Imperial College London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial design and implementation
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.operator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import uk.ac.imperial.lsds.seep.comm.routing.Router;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.infrastructure.NodeManager;
import uk.ac.imperial.lsds.seep.processingunit.IProcessingUnit;
import uk.ac.imperial.lsds.seep.processingunit.StatefulProcessingUnit;

public abstract class Operator implements Serializable, QuerySpecificationI, EndPoint{

	private static final long serialVersionUID = 1L;

	private int operatorId;
	private OperatorContext opContext = new OperatorContext();
	private State state = null;
	private boolean ready = false;
	public Operator subclassOperator = null;
	public IProcessingUnit processingUnit = null;
	private Router router = null;
	// By default value
	private InputDataIngestionMode dataAbs = InputDataIngestionMode.ONE_AT_A_TIME;


	public Operator(){}
	
	/** Instantiation methods **/
	
	public void setOperatorId(int opId){
		this.operatorId = opId;
	}
	
	public void setState(State state){
		this.state = state;
	}
	
	public void setSubclassOperator(){
		subclassOperator = this;
	}
	
	/** Other methods **/
	
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
	
	public void setProcessingUnit(IProcessingUnit processingUnit){
		this.processingUnit = processingUnit;
	}
	
	/** Mandatory methods to implement by developers **/
	
	public abstract void setUp();

	//public abstract void processData(DataTuple dt);
	public abstract void processData(DataTuple dt);
	
	public abstract void processData(ArrayList<DataTuple> ldt);
	
	/** Methods used by the developers to send data **/
	
	public void sendDownToIndex(DataTuple dt, int idx){
		ArrayList<Integer> targets = new ArrayList<Integer>();
		targets.add(idx);
		processingUnit.sendData(dt, targets);
	}
	
	public synchronized void sendDown(DataTuple dt){
		/// \todo{FIX THIS, look for a value that cannot be present in the tuples...}
		// We check the targets with our routers
		ArrayList<Integer> targets = router.forward(dt, Integer.MIN_VALUE, false);
		processingUnit.sendData(dt, targets);
	}
	
	public synchronized void sendDownRouteByValue(DataTuple dt, int value){
		// We check the targets with our routers
		ArrayList<Integer> targets = router.forward(dt, value, false);
		processingUnit.sendData(dt, targets);
	}
	
	public void sendDownAll(DataTuple dt){
		// When routing to all, targets are all the logical downstreamoperators
		ArrayList<Integer> targets = router.forwardToAllDownstream(dt);
		processingUnit.sendData(dt, targets);
	}
	
	/** System Configuration Settings **/
	
	public void disableCheckpointing(){
		// Disable checkpointing the state for operator with operatorId
		((StatefulProcessingUnit)processingUnit).disableCheckpointForOperator(operatorId);
	}
	
	public void disableMultiCoreSupport(){
		processingUnit.disableMultiCoreSupport();
	}
	
	/** Data Delivery methods **/
	
	public InputDataIngestionMode getInputDataIngestionMode(){
		return dataAbs;
	}
	
	//Should not use this method, as this information is defined in queryspecificationI
	@Deprecated
	public void setDataAbstractionMode(InputDataIngestionMode mode){
		this.dataAbs = mode;
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
		if(originalQuery) opContext.addOriginalDownstream(down.getOperatorId());
		down.getOpContext().addUpstream(getOperatorId());
		// Store in opContext the inputDataIngestionMode, if not defined, the default: One-at-a-time
		down.getOpContext().setInputDataIngestionModePerUpstream(getOperatorId(), QuerySpecificationI.InputDataIngestionMode.ONE_AT_A_TIME);
	}
	
	public void connectTo(QuerySpecificationI down, InputDataIngestionMode mode, boolean originalQuery){
		opContext.addDownstream(down.getOperatorId());
		if(originalQuery) opContext.addOriginalDownstream(down.getOperatorId());
		down.getOpContext().addUpstream(getOperatorId());
		// Store in the opContext of the downstream I am adding, the inputdataingestion mode that I demand
		down.getOpContext().setInputDataIngestionModePerUpstream(getOperatorId(), mode);
	}
	
	public void route(String attributeToQuery, Router.RelationalOperator operand, int valueToMatch, Operator toConnect){
		int opId = toConnect.getOperatorId();
		opContext.setQueryAttribute(attributeToQuery);
		opContext.routeValueToDownstream(operand, valueToMatch, opId);
		NodeManager.nLogger.info("Operator: "+this.toString()+" sends data with value: "+valueToMatch+" to Operator: "+toConnect.toString());
	}
	
	public void _declareWorkingAttributes(List<String> attributes){
		opContext.setDeclaredWorkingAttributes(attributes);
	}
	
	public void _declareWorkingAttributes(List<String> attributes, String key){
		opContext.setKeyAttribute(key);
		opContext.setDeclaredWorkingAttributes(attributes);
	}

	/** HELPER METHODS **/
	
	@Override 
	public String toString() {
		return "Operator [operatorId="+operatorId+", opContext= "+opContext+"]";
	}
}
