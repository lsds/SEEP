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
package uk.co.imperial.lsds.seep.processingunit;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import uk.co.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.co.imperial.lsds.seep.infrastructure.NodeManager;
import uk.co.imperial.lsds.seep.infrastructure.monitor.MetricsReader;
import uk.co.imperial.lsds.seep.operator.EndPoint;
import uk.co.imperial.lsds.seep.operator.Operator;
import uk.co.imperial.lsds.seep.operator.OperatorContext;
import uk.co.imperial.lsds.seep.operator.OperatorStaticInformation;
import uk.co.imperial.lsds.seep.operator.OperatorContext.PlacedOperator;
import uk.co.imperial.lsds.seep.reliable.ACKWorker;
import uk.co.imperial.lsds.seep.runtimeengine.AsynchronousCommunicationChannel;
import uk.co.imperial.lsds.seep.runtimeengine.CoreRE;
import uk.co.imperial.lsds.seep.runtimeengine.DataStructureAdapter;
import uk.co.imperial.lsds.seep.runtimeengine.OutputQueue;
import uk.co.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel;

public class StatelessProcessingUnit implements IProcessingUnit {

	private CoreRE owner;
	private PUContext ctx;
	
	private SystemStatus systemStatus = SystemStatus.NORMAL;
	
	private Operator runningOp = null;
	
	private ArrayList<Integer> listOfManagedStates = new ArrayList<Integer>();
	private OutputQueue outputQueue = null;
	
	//Multi-core support
	private Executor pool;
	private boolean multiCoreEnabled;
	
	public StatelessProcessingUnit(CoreRE owner, boolean multiCoreEnabled){
		this.owner = owner;
		ctx = new PUContext(owner.getNodeDescr());
		this.multiCoreEnabled = multiCoreEnabled;
	}
	
	@Override
	public void addDownstream(int opId, OperatorStaticInformation location) {
		// First pick the most downstream operator, and add the downstream to that one
		OperatorContext opContext = runningOp.getOpContext();
		opContext.addDownstream(opId);
		opContext.setDownstreamOperatorStaticInformation(opId, location);
		ctx.configureNewDownstreamCommunication(opId, location);
	}

	@Override
	public void addUpstream(int opId, OperatorStaticInformation location) {
		// First pick the most upstream operator, and add the upstream to that one
		OperatorContext opContext = runningOp.getOpContext();
		opContext.addUpstream(opId);
		opContext.setUpstreamOperatorStaticInformation(opId, location);
		ctx.configureNewUpstreamCommunication(opId, location);
	}

	@Override
	public Map<String, Integer> createTupleAttributeMapper() {
		Map<String, Integer> idxMapper = new HashMap<String, Integer>();
		List<String> declaredWorkingAttributes = runningOp.getOpContext().getDeclaredWorkingAttributes();
		if(declaredWorkingAttributes != null){
			for(int i = 0; i<declaredWorkingAttributes.size(); i++){
				idxMapper.put(declaredWorkingAttributes.get(i), i);
			}
		}
		else{
			NodeManager.nLogger.warning("-> No tuple MAPPER. This is fine as far as I am a SRC");
		}
		return idxMapper;
	}

	@Override
	public Operator getOperator() {
		return runningOp;
	}
	
	@Override
	public CoreRE getOwner(){
		return owner;
	}

	@Override
	public SystemStatus getSystemStatus() {
		return systemStatus;
	}
	
	public boolean isMultiCoreEnabled(){
		return multiCoreEnabled;
	}

	@Override
	public void initOperator() {
		runningOp.setUp();
	}

	@Override
	public void invalidateState(int opId) {
		System.out.println("% We will invalidate management of state for: "+opId);
		//If the states figures as being managed we removed it
		int index = 0;
		if((index = listOfManagedStates.indexOf(opId)) != -1){
			System.out.println("% I was managing state for OP: "+opId+" NOT ANYMORE");
			listOfManagedStates.remove(index);
		}
		// and then we clean both the buffer and the mapping in downstreamBuffers.
		if(PUContext.downstreamBuffers.get(opId) != null){
			//First of all, we empty the buffer
			PUContext.downstreamBuffers.get(opId).replaceBackupOperatorState(null);
		}
	}

	@Override
	public boolean isManagingStateOf(int opId) {
		return listOfManagedStates.contains(opId) ? true : false;
	}

	@Override
	public boolean isNodeStateful() {
		return false;
	}

	@Override
	public boolean isOperatorReady() {
		return runningOp.getReady();
	}

	@Override
	public void newOperatorInstantiation(Operator o) {
		NodeManager.nLogger.info("-> Instantiating Operator");
		//Detect the first submitted operator
		if(runningOp == null){
			runningOp = o;
		}
		else{
			NodeManager.nLogger.warning("-> The operator in this node is being overwritten");
		}
		o.setProcessingUnit(this);
		// To identify the monitor with the op id instead of the node id
		NodeManager.nodeMonitor.setNodeId(o.getOperatorId());
	}

	@Override
	public void processData(DataTuple data) {
		MetricsReader.eventsProcessed.inc();
		// TODO: Adjust timestamp of state
		runningOp.processData(data);

	}

	@Override
	public void processData(ArrayList<DataTuple> data) {
		MetricsReader.eventsProcessed.inc();
		// TODO: Adjust timestamp of state
		runningOp.processData(data);
	}

	/// \check{ one operator per node, so the below compairson is stupid. CHECK}
	@Override
	public void reconfigureOperatorConnection(int opId, InetAddress ip) {
//		System.out.println("COMPARING: "+runningOp.getOperatorId()+"with "+opId);
//		if(runningOp.getOperatorId() == opId){
//		System.out.println("UPDATE OP WITH NEW IP: "+ip.toString());
		ctx.updateConnection(opId, runningOp, ip);
//		}
//		else{
//			NodeManager.nLogger.warning("-> This node does not contain the requested operator: "+opId);
//		}
	}

	@Override
	public void reconfigureOperatorLocation(int opId, InetAddress ip) {
		runningOp.getOpContext().changeLocation(opId, ip);
	}

	@Override
	public void registerManagedState(int opId) {
		//If the state does not figure as being managed, we include it
		if(!listOfManagedStates.contains(opId)){
			NodeManager.nLogger.info("% -> New STATE registered for Operator: "+opId);
			listOfManagedStates.add(opId);
		}
	}

	@Override
	public void sendData(DataTuple dt, ArrayList<Integer> targets) {
		for(int i = 0; i<targets.size(); i++){
			int target = targets.get(i);
			try{
//	System.out.println("SEND TO: "+target+" SIZE: "+ctx.getDownstreamTypeConnection().size()+" targetSize: "+targets.size());
				EndPoint dest = ctx.getDownstreamTypeConnection().elementAt(target);
				// REMOTE ASYNC
				if(dest instanceof AsynchronousCommunicationChannel){
					((AsynchronousCommunicationChannel)dest).writeDataToOutputBuffer(dt);
				}
				// REMOTE SYNC
				else if(dest instanceof SynchronousCommunicationChannel){
					///\fixme{do some proper thing with var now}
					boolean now = false;
					outputQueue.sendToDownstream(dt, dest, now, false);
				}
				// LOCAL
				else if(dest instanceof Operator){
					Operator operatorObj = (Operator) dest;
					operatorObj.processData(dt);
				}
			}
			catch(ArrayIndexOutOfBoundsException aioobe){
				System.out.println("Targets size: "+targets.size()+" Target-Index: "+target+" downstreamSize: "+ctx.getDownstreamTypeConnection().size());
				aioobe.printStackTrace();
			}
		}
	}

	@Override
	public void setOpReady(int opId) {
		NodeManager.nLogger.info("-> Setting operator ready");
		runningOp.setReady(true);
	}

	@Override
	public void setOutputQueue(OutputQueue outputQueue) {
		this.outputQueue = outputQueue;
	}

	@Override
	public void setSystemStatus(SystemStatus systemStatus) {
		this.systemStatus = systemStatus;
	}

	@Override
	public PUContext setUpRemoteConnections() {
		ctx.configureOperatorConnections(runningOp);
		return ctx;
	}

	@Override
	public void startDataProcessing() {
		/// \todo{Find a better way to start the operator...}
		DataTuple fake = DataTuple.getNoopDataTuple();
		this.runningOp.processData(fake);

	}

	@Override
	public void stopConnection(int opId) {
		//Stop incoming data, a new thread is replaying
		outputQueue.stop();
		ctx.getCCIfromOpId(opId, "d").getStop().set(true);
	}

	@Override
	public void launchMultiCoreMechanism(CoreRE core, DataStructureAdapter dsa) {
		// Different strategies depending on whether the state is partitionable or not
		int numberOfProcessors = Runtime.getRuntime().availableProcessors();
		int numberOfWorkerThreads = (numberOfProcessors - 2) > 1 ? (numberOfProcessors-2) : 1;
		
		pool = Executors.newFixedThreadPool(numberOfWorkerThreads);
		// Populate pool with threads
		for(int i = 0; i<numberOfWorkerThreads; i++){
			pool.execute(new StatelessProcessingWorker(dsa, runningOp));
		}
		
		
	}

	@Override
	public void disableMultiCoreSupport() {
		multiCoreEnabled = false;
	}

	@Override
	public void createAndRunAckWorker() {
		ACKWorker ackWorker = new ACKWorker(this); 
		Thread ackT = new Thread(ackWorker);
		ackT.start();
	}

	@Override
	public long getLastACK() {
		return owner.getTsData();
	}

	@Override
	public void emitACK(long currentTs) {
		owner.ack(currentTs);
	}

	public ArrayList<Integer> getRouterIndexesInformation(int opId){
		return runningOp.getRouter().getIndexesInformation(opId);
	}
	
	public ArrayList<Integer> getRouterKeysInformation(int opId){
		return runningOp.getRouter().getKeysInformation(opId);
	}

}
