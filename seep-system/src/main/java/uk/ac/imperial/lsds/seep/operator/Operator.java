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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.comm.routing.IRoutingObserver;
import uk.ac.imperial.lsds.seep.comm.routing.Router;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.processingunit.IProcessingUnit;
import uk.ac.imperial.lsds.seep.processingunit.StatefulProcessingUnit;
import uk.ac.imperial.lsds.seep.state.StateWrapper;

public class Operator implements Serializable, EndPoint, Connectable, Callback {

	private static final long serialVersionUID = 1L;
	private final Logger LOG = LoggerFactory.getLogger(Operator.class);

	private final int operatorId;
	private int originalOpId;

	private final OperatorCode operatorCode;
	private final StateWrapper stateWrapper;

	private OperatorContext opContext = new OperatorContext();
	private boolean ready = false;
	public IProcessingUnit processingUnit = null;
	private Router router = null;	
	private Map<Integer, InputDataIngestionMode> inputDataIngestionMode = new HashMap<Integer, InputDataIngestionMode>();

	public static Operator getStatefulOperator(int opId, OperatorCode opCode, StateWrapper s, List<String> attributes){
		return new Operator(opId, opCode, s, attributes);
	}

	public static Operator getStatelessOperator(int opId, OperatorCode opCode, List<String> attributes){
		return new Operator(opId, opCode, attributes);
	}

	public OperatorCode getOperatorCode(){
		return operatorCode;
	}

	private Operator(int opId, OperatorCode opCode, List<String> attributes){
		this.operatorId = opId;
		this.operatorCode = opCode;
		this.stateWrapper = null;
		opContext.setDeclaredWorkingAttributes(attributes);
		this.originalOpId = opId;
	}

	private Operator(int opId, OperatorCode opCode, StateWrapper s, List<String> attributes){
		this.operatorId = opId;
		this.operatorCode = opCode;
		this.stateWrapper = s;
		opContext.setDeclaredWorkingAttributes(attributes);
		this.originalOpId = opId;
	}

	public void setOriginalOpId(int x){
		originalOpId = x ;
	}

	public int getOriginalOpId(){
		return originalOpId;
	}

	/** Other methods **/

	public StateWrapper getStateWrapper(){
		return stateWrapper;
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
		//router.addObserver(this);
	}

	public void setProcessingUnit(IProcessingUnit processingUnit){
		this.processingUnit = processingUnit;
		this.operatorCode.api.setCallbackObject(this);
	}

	/** Methods used by the developers to send data **/

	// Send downstream in round robin fashion
	public synchronized void send(DataTuple dt){
		// We check the targets with our routers
		ArrayList<Integer> targets = router.forward(dt);
		processingUnit.sendData(dt, targets);
	}

	// Send downstream in round robin fashion
	public synchronized void send_lowestCost(DataTuple dt){
		// We check the targets with our routers
		ArrayList<Integer> targets = router.forward_lowestCost(dt);
		processingUnit.sendData(dt, targets);
	}

	public void send_highestWeight(DataTuple dt)
	{
		/*
		ArrayList<Integer> targets = null;
		synchronized(this)
		{
			LOG.debug("Operator sending data tuple: "+dt.getPayload().timestamp);
			targets = router.forward_highestWeight(dt);
			while (targets == null || targets.isEmpty())
			{
				LOG.debug("Nowhere to send tuple, waiting for routing change.");
				try {
					this.wait();
				} catch (InterruptedException e) {
					LOG.debug("TODO: Check if router closed.");
				}
				targets = router.forward_highestWeight(dt);
			}
		}
		
		processingUnit.sendDataDispatched(dt, targets);
		*/
		processingUnit.sendDataDispatched(dt);
	}

	// Send to a particular downstream index
	public synchronized void send_toIndex(DataTuple dt, int idx){
		ArrayList<Integer> targets = new ArrayList<Integer>();
		targets.add(idx);
		processingUnit.sendData(dt, targets);
	}

	public synchronized void send_toIndices(DataTuple[] dts, int[] idxs){
		ArrayList<Integer> targets = new ArrayList<>();
		for(int idx : idxs){
			targets.add(idx);
		}
		processingUnit.sendPartitionedData(dts, targets);
	}

	// Send downstream to stateful partitionable operator
	public synchronized void send_splitKey(DataTuple dt, int key){
		// We check the targets with our routers
		ArrayList<Integer> targets = router.forward_splitKey(dt, key);
		processingUnit.sendData(dt, targets);
	}

	// Send to specific streamId in round robin
	public synchronized void send_toStreamId(DataTuple dt, int streamId){
		ArrayList<Integer> targets = router.forward_toOp(dt, streamId);
		processingUnit.sendData(dt, targets);
	}

	// Send to stateful partition of a given streamId
	public synchronized void send_toStreamId_splitKey(DataTuple dt, int streamId, int key){
		ArrayList<Integer> targets = router.forward_toOp_splitKey(dt, streamId, key);
		processingUnit.sendData(dt, targets);
	}

	// Send to all instances of a specific streamId
	public synchronized void send_toStreamId_toAll(DataTuple dt, int streamId){
		ArrayList<Integer> targets = router.forwardToAllOpsInStreamId(dt, streamId);
		processingUnit.sendData(dt, targets);
	}

	// Send to all downstream
	public void send_all(DataTuple dt){
		// When routing to all, targets are all the logical downstreamoperators
		ArrayList<Integer> targets = router.forwardToAllDownstream(dt);
		processingUnit.sendData(dt, targets);
	}


	public synchronized void send_toStreamId_toAll_threadPool(DataTuple dt, int streamId){
		ArrayList<Integer> targets = router.forwardToAllOpsInStreamId(dt, streamId);
		processingUnit.sendDataByThreadPool(dt, targets);
	}

	public void send_all_threadPool(DataTuple dt){
		// When routing to all, targets are all the logical downstreamoperators
		ArrayList<Integer> targets = router.forwardToAllDownstream(dt);
		processingUnit.sendDataByThreadPool(dt, targets);
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

	public InputDataIngestionMode getInputDataIngestionModeForUpstream(int opId){
		return inputDataIngestionMode.get(opId);
	}

	public Map<Integer, InputDataIngestionMode> getInputDataIngestionModeMap(){
		return inputDataIngestionMode;
	}

	public void setInputDataIngestionModeForUpstream(int opId, InputDataIngestionMode mode){
		this.inputDataIngestionMode.put(opId, mode);
	}
	
	public void ack(DataTuple dt)
	{
		processingUnit.ack(dt);
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

	public void _declareWorkingAttributes(List<String> attributes){
		opContext.setDeclaredWorkingAttributes(attributes);
	}

	public void _declareWorkingAttributes(List<String> attributes, String key){
		opContext.setKeyAttribute(key);
		opContext.setDeclaredWorkingAttributes(attributes);
	}

	public void initializeInputDataIngestionModePerUpstream(Map<Integer, InputDataIngestionMode> idim){
		for(Integer opId : idim.keySet()){
			this.getOpContext().setInputDataIngestionModePerUpstream(opId, idim.get(opId));
		}
	}

	public void processData(DataTuple data){
		operatorCode.processData(data);
	}

	public void processData(ArrayList<DataTuple> dataList){
		operatorCode.processData(dataList);
	}

	public void setUp(){
		operatorCode.setUp();
	}

	@Override
	public void connectTo(Connectable down, boolean originalQuery) {
		// default inputdataingestion and default stream
		this.connectTo(down, InputDataIngestionMode.ONE_AT_A_TIME, originalQuery, 0);
	}

	@Override
	public void connectTo(Connectable down, boolean originalQuery, int streamId) {
		// default inputdataingestion 
		this.connectTo(down, InputDataIngestionMode.ONE_AT_A_TIME, originalQuery, streamId);
	}

	@Override
	public void connectTo(Connectable down, InputDataIngestionMode mode,
			boolean originalQuery, int streamId) {
		LOG.debug("Connecting OP: "+this.getOperatorId()+" with downstream Op: "+down.getOperatorId());
		opContext.addDownstream(down.getOperatorId());
		if(originalQuery) opContext.addOriginalDownstream(down.getOperatorId());
		down.getOpContext().addUpstream(getOperatorId());
		// We store routing info, an operator sends to a streamId, who knows the end-stream edge
		opContext.routeValueToDownstream(streamId, down.getOperatorId());
		// Store in the opContext of the downstream I am adding, the inputdataingestion mode that I demand
		this.setInputDataIngestionModeForUpstream(down.getOperatorId(), mode);
	}

	/*
	@Override
	public void routingChanged()
	{
		LOG.debug("Routing changed!");
		synchronized(this) { this.notifyAll(); }
	}
	*/
	/** HELPER METHODS **/

	@Override 
	public String toString() {
		return "Operator [operatorId="+operatorId+", opContext= "+opContext+"]";
	}
}
