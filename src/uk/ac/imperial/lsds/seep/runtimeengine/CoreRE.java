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
package uk.ac.imperial.lsds.seep.runtimeengine;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Map;

import uk.ac.imperial.lsds.seep.Main;
import uk.ac.imperial.lsds.seep.P;
import uk.ac.imperial.lsds.seep.comm.ControlHandler;
import uk.ac.imperial.lsds.seep.comm.IncomingDataHandler;
import uk.ac.imperial.lsds.seep.comm.OutgoingDataHandlerWorker;
import uk.ac.imperial.lsds.seep.comm.routing.Router;
import uk.ac.imperial.lsds.seep.comm.serialization.ControlTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.Ack;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.BackupOperatorState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.RawData;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.ReconfigureConnection;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.ReplayStateInfo;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.Resume;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.StateChunk;
import uk.ac.imperial.lsds.seep.infrastructure.NodeManager;
import uk.ac.imperial.lsds.seep.infrastructure.WorkerNodeDescription;
import uk.ac.imperial.lsds.seep.infrastructure.master.Node;
import uk.ac.imperial.lsds.seep.operator.Operator;
import uk.ac.imperial.lsds.seep.operator.OperatorStaticInformation;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;
import uk.ac.imperial.lsds.seep.operator.Operator.DataAbstractionMode;
import uk.ac.imperial.lsds.seep.operator.OperatorContext.PlacedOperator;
import uk.ac.imperial.lsds.seep.processingunit.IProcessingUnit;
import uk.ac.imperial.lsds.seep.processingunit.PUContext;
import uk.ac.imperial.lsds.seep.processingunit.StatefulProcessingUnit;
import uk.ac.imperial.lsds.seep.processingunit.StatelessProcessingUnit;
import uk.ac.imperial.lsds.seep.reliable.BackupHandler;
import uk.ac.imperial.lsds.seep.runtimeengine.workers.DataConsumer;
import uk.ac.imperial.lsds.seep.utils.dynamiccodedeployer.RuntimeClassLoader;

/**
* Operator. This is the class that must inherit any subclass (the developer must inherit this class). It is the basis for building an operator
*/

public class CoreRE {

	private WorkerNodeDescription nodeDescr = null;
	private IProcessingUnit processingUnit = null;
	private PUContext puCtx = null;
	private RuntimeClassLoader rcl = null;
	
	private int backupUpstreamIndex = -1;

	private CoreProcessingLogic coreProcessLogic;

	private DataStructureAdapter dsa;
	private DataConsumer dataConsumer;
	private Thread dConsumerH = null;
	private ControlDispatcher controlDispatcher;
	private OutputQueue outputQueue;
	private OutgoingDataHandlerWorker odhw = null;
	
	private Thread controlH = null;
	private ControlHandler ch = null;
	private Thread iDataH = null;
	private IncomingDataHandler idh = null;
	private BackupHandler bh = null;
	private Thread backupH = null;
	
	static ControlTuple genericAck;
	private int totalNumberOfChunks = -1;
	
	// Timestamp of the last data tuple processed by this operator
	private long ts_data = 0;
	// Timestamp of the last ack processed by this operator
	private long ts_ack;
		
	public CoreRE(WorkerNodeDescription nodeDescr, RuntimeClassLoader rcl){
		this.nodeDescr = nodeDescr;
		this.rcl = rcl;		
		coreProcessLogic = new CoreProcessingLogic();
//		ControlTuple b = new ControlTuple();
//		b.setType(ControlTupleType.ACK);
//		genericAck = b;
	}
	
	public RuntimeClassLoader getRuntimeClassLoader(){
		return rcl;
	}
	
	public WorkerNodeDescription getNodeDescr(){
		return nodeDescr;
	}
	
	public ControlDispatcher getControlDispatcher(){
		return controlDispatcher;
	}
	
	public void pushOperator(Operator o){
		boolean multicoreSupport = P.valueFor("multicoreSupport").equals("true") ? true : false; 
		if(o.getOpContext().getOperatorStaticInformation().isStatefull() ){
			processingUnit = new StatefulProcessingUnit(this, multicoreSupport);
		}
		else{
			processingUnit = new StatelessProcessingUnit(this, multicoreSupport);
		}
		processingUnit.newOperatorInstantiation(o);
	}
	
	public void setOpReady(int opId) {
		processingUnit.setOpReady(opId);
		if(processingUnit.isOperatorReady()){
			NodeManager.nLogger.info("-> All operators in this unit are ready. Initializing communications...");
			// Once the operators are in the node, we extract and declare how they will handle data tuples
			Map<String, Integer> idxMapper = processingUnit.createTupleAttributeMapper();
			processingUnit.initOperator();
			initializeCommunications(idxMapper);
		}
	}
	
	public void initializeCommunications(Map<String, Integer> tupleIdxMapper){
		outputQueue = new OutputQueue();
		// SET UP the data strcuture adapter, depending on the operators
		dsa = new DataStructureAdapter();
		/// INSTANTIATION OF THE BRIDGE OBJECT
		// We get the dataabstractionmode from the most upstream operator
		DataAbstractionMode dam = processingUnit.getOperator().getDataAbstractionMode();
		// We configure the dataStructureAdapter with this mode, and put the number of upstreams, required by some modes
		dsa.setUp(dam, processingUnit.getOperator().getOpContext().upstreams.size());
		
		// Start communications and worker threads
		int inC = processingUnit.getOperator().getOpContext().getOperatorStaticInformation().getInC();
		int inD = processingUnit.getOperator().getOpContext().getOperatorStaticInformation().getInD();
		int inBT = new Integer(P.valueFor("blindSocket"));
		//Control worker
		ch = new ControlHandler(this, inC);
		controlH = new Thread(ch);
		//Data worker
		idh = new IncomingDataHandler(this, inD, tupleIdxMapper);
		iDataH = new Thread(idh);
		//Consumer worker
		dataConsumer = new DataConsumer(this, dsa);
		dConsumerH = new Thread(dataConsumer);

//		dConsumerH.start();
		controlH.start();
		iDataH.start();
		
		/// \todo{FIX THIS. cREATE ONLY IF ANY DOWNSTREAM IS STATEFUL}
		// If some downstream is stateful, then we have to run the backupHandler
//		if(processingUnit.getOperator().getOpContext().isDownstreamStateful()){
			// Backup worker
			bh = new BackupHandler(this, inBT);
			backupH = new Thread(bh);
			backupH.start();
//		}
	}
	
	public void setRuntime(){
		
		/// At this point I need information about what connections I need to establish
		puCtx = processingUnit.setUpRemoteConnections();
		
		// Set up output communication module
		if (P.valueFor("synchronousOutput").equals("true")){
			processingUnit.setOutputQueue(outputQueue);
			NodeManager.nLogger.info("-> CONFIGURING SYSTEM WITH A SYNCHRONOUS OUTPUT");
		}
		else{
			Selector s = puCtx.getConfiguredSelector();
			odhw = new OutgoingDataHandlerWorker(s);
			Thread odhw_t = new Thread(odhw);
			odhw_t.start();
			NodeManager.nLogger.info("-> CONFIGURING SYSTEM WITH AN ASYNCHRONOUS OUTPUT");
		}
		
		// Set up multi-core support structures
		if(P.valueFor("multicoreSupport").equals("true")){
			if(processingUnit.isMultiCoreEnabled()){
				processingUnit.launchMultiCoreMechanism(this, dsa);
				NodeManager.nLogger.info("-> Multi core support enabled");
			}
			else{
				// Start the consumer thread.
				dConsumerH.start();
				NodeManager.nLogger.info("-> Multi core support not enabled in this node");
			}
		}
		else{
			// Start the consumer thread.
			dConsumerH.start();
			NodeManager.nLogger.info("-> SYSTEM CONFIGURED FOR NO MULTICORE");
		}
		
		
//		controlH.start();
//		iDataH.start();
		
		/// INSTANTIATION
		/** MORE REFACTORING HERE **/
		coreProcessLogic.setOwner(this);
		coreProcessLogic.setProcessingUnit(processingUnit);
		coreProcessLogic.setOpContext(puCtx);
		coreProcessLogic.initializeSerialization();
		
		controlDispatcher = new ControlDispatcher(puCtx);

		//initialize the genericAck message to answer some specific messages.
		ControlTuple b = new ControlTuple();
		b.setType(ControlTupleType.ACK);
		genericAck = b;
		
		NodeManager.nLogger.info("-> Node "+nodeDescr.getNodeId()+" instantiated");
		
		/// INITIALIZATION

		//Choose the upstreamBackupIndex for this operator
		int upstreamSize = processingUnit.getOperator().getOpContext().upstreams.size();
		configureUpstreamIndex(upstreamSize);
		// After configuring the upstream backup index, we start the stateBackupWorker thread if the node is stateful
		if(processingUnit.isNodeStateful()){
			if(((StatefulProcessingUnit)processingUnit).isCheckpointEnabled()){
				((StatefulProcessingUnit)processingUnit).createAndRunStateBackupWorker();
				NodeManager.nLogger.info("-> State Worker working on "+nodeDescr.getNodeId());
			}
		}
		NodeManager.nLogger.info("-> Node "+nodeDescr.getNodeId()+" comm initialized");
		
		// If ackworker is active
		if(P.valueFor("ackWorkerActive").equals("true")){
			//If this is the sink operator (extremely ugly)
			if(processingUnit.getOperator().getOpContext().downstreams.size() == 0){
				processingUnit.createAndRunAckWorker();
				NodeManager.nLogger.info("-> ACK Worker working on "+nodeDescr.getNodeId());
			}
		}
	}
	
	public void startDataProcessing(){
		NodeManager.nLogger.info("-> Starting to process data...");
		processingUnit.startDataProcessing();
	}
	
	public void stopDataProcessing(){
		NodeManager.nLogger.info("-> The system has been remotely stopped. No processing data");
	}
	
	public enum ControlTupleType{
		ACK, BACKUP_OP_STATE, BACKUP_NODE_STATE, RECONFIGURE, SCALE_OUT, RESUME, INIT_STATE, STATE_ACK, INVALIDATE_STATE,
		BACKUP_RI, INIT_RI, RAW_DATA, OPEN_BACKUP_SIGNAL, CLOSE_BACKUP_SIGNAL, REPLAY_STATE, STATE_CHUNK
	}
	
	public synchronized void setTsData(long ts_data){
		this.ts_data = ts_data;
	}

	public synchronized long getTsData(){
		return ts_data;
	}
	
//	public InputQueue getInputQueue(){
//		return inputQueue;
//	}
	
	public DataStructureAdapter getDSA(){
		return dsa;
	}
	
	public long getTs_ack() {
		return ts_ack;
	}

	public void setTs_ack(long tsAck) {
		ts_ack = tsAck;
	}
	
	public void forwardData(DataTuple data){
		processingUnit.processData(data);
	}
	
	public void forwardData(ArrayList<DataTuple> data){
		processingUnit.processData(data);
	}
	
	public int getBackupUpstreamIndex() {
		return backupUpstreamIndex;
	}

	public void setBackupUpstreamIndex(int backupUpstreamIndex) {
		System.out.println("% current backupIdx: "+this.backupUpstreamIndex+" changes to: "+backupUpstreamIndex);
		this.backupUpstreamIndex = backupUpstreamIndex;
	}
	
	//TODO To refine this method...
	/// \todo {this method should work when an operator must be killed in a proper way}
	public boolean killHandlers(){
		//controlH.destroy();
		//iDataH.destroy();
		return true;
	}
	
//	public void cleanInputQueue(){
//		System.out.println("System STATUS before cleaning: "+processingUnit.getSystemStatus());
//		inputQueue.clean();
//		System.out.println("System STATUS after cleaning: "+processingUnit.getSystemStatus());
//	}

	public boolean checkSystemStatus(){
		// is it correct like this?
		if(processingUnit.getSystemStatus().equals(StatefulProcessingUnit.SystemStatus.NORMAL)){
			return true;
		}
		return false;
	}
		
	boolean gotit = false;
	
	/// \todo{reduce messages here. ACK, RECONFIGURE, BCK_STATE, rename{send_init, init_ok, init_state}}
	public void processControlTuple(ControlTuple ct, OutputStream os, InetAddress remoteAddress) {
		ControlTupleType ctt = ct.getType();
		/** ACK message **/
		if(ctt.equals(ControlTupleType.ACK)) {
			Ack ack = ct.getAck();
			if(ack.getTs() >= ts_ack){
				coreProcessLogic.processAck(ack);
			}
		}
		/** INVALIDATE_STATE message **/
		else if(ctt.equals(ControlTupleType.INVALIDATE_STATE)) {
			NodeManager.nLogger.info("-> Node "+nodeDescr.getNodeId()+" recv ControlTuple.INVALIDATE_STATE from NODE: "+ct.getInvalidateState().getOperatorId());
			processingUnit.invalidateState(ct.getInvalidateState().getOperatorId());
		}
		/** INIT_MESSAGE message **/
		else if(ctt.equals(ControlTupleType.INIT_STATE)){
			NodeManager.nLogger.info("-> Node "+nodeDescr.getNodeId()+" recv ControlTuple.INIT_STATE from NODE: "+ct.getInitOperatorState().getOpId());
			coreProcessLogic.processInitState(ct.getInitOperatorState());
		}
		/** OPEN_SIGNAL message **/
		else if(ctt.equals(ControlTupleType.OPEN_BACKUP_SIGNAL)){
			NodeManager.nLogger.info("-> Node "+nodeDescr.getNodeId()+" recv ControlTuple.OPEN_SIGNAL from NODE: "+ct.getOpenSignal().getOpId());
			bh.openSession(ct.getOpenSignal().getOpId(), remoteAddress);
			PrintWriter out = new PrintWriter(os, true);
			out.println("ack");
			System.out.println("ANSWER OPen signal");
		}
		/** CLOSE_SIGNAL message **/
		else if(ctt.equals(ControlTupleType.CLOSE_BACKUP_SIGNAL)){
			NodeManager.nLogger.info("-> Node "+nodeDescr.getNodeId()+" recv ControlTuple.CLOSE_SIGNAL from NODE: "+ct.getCloseSignal().getOpId());
			bh.closeSession(ct.getCloseSignal().getOpId(), remoteAddress);
			
//			coreProcessLogic.directReplayState(new ReplayStateInfo(1, 1, true), bh);
		}
		/** STATE_BACKUP message **/
		else if(ctt.equals(ControlTupleType.BACKUP_OP_STATE)){
			//If communications are not being reconfigured
			//Register this state as being managed by this operator
			BackupOperatorState backupOperatorState = ct.getBackupState();
			NodeManager.nLogger.info("-> Node "+nodeDescr.getNodeId()+" recv BACKUP_OP_STATE from Op: "+backupOperatorState.getOpId());
			processingUnit.registerManagedState(backupOperatorState.getOpId());
			coreProcessLogic.processBackupState(backupOperatorState);
		}
		/** RAW_DATA message **/
		else if(ctt.equals(ControlTupleType.RAW_DATA)){
			RawData rw = ct.getRawData();
			NodeManager.nLogger.info("-> Node "+nodeDescr.getNodeId()+" recv RAW DATA from Op: "+rw.getOpId());
			// is state anyway, we register it...
			processingUnit.registerManagedState(rw.getOpId());
//			coreProcessLogic.processBackupState(backupOperatorState);
			coreProcessLogic.processRawData(rw);
		}
		/** STATE_ACK message **/
		else if(ctt.equals(ControlTupleType.STATE_ACK)){
			if(!gotit){
				gotit = true;
			}
			else{
				return;
			}
			int opId = ct.getStateAck().getMostUpstreamOpId();
			NodeManager.nLogger.info("-> Received STATE_ACK from Op: "+opId);
//			operatorStatus = OperatorStatus.REPLAYING_BUFFER;
			
//			opCommonProcessLogic.replayTuples(ct.getStateAck().getOpId());
			SynchronousCommunicationChannel cci = puCtx.getCCIfromOpId(opId, "d");
//			outputQueue.start();
			
//			outputQueue = new OutputQueue();
//			processingUnit.setOutputQueue(outputQueue);
//			dataConsumer = new DataConsumer(this, dsa);
//			dConsumerH = new Thread(dataConsumer);
//			dConsumerH.start();
			
			outputQueue.replayTuples(cci);

			// In case of failure, the thread may have died, in such case we make it runnable again.
//			if(dConsumerH.getState() != Thread.State.TERMINATED){
				dConsumerH = new Thread(dataConsumer);
				dConsumerH.start();
//			}

//			operatorStatus = OperatorStatus.NORMAL;
		}
		/** BACKUP_RI message **/
		else if(ctt.equals(ControlTupleType.BACKUP_RI)){

			NodeManager.nLogger.info("-> Node "+nodeDescr.getNodeId()+" recv ControlTuple.BACKUP_RI");
			coreProcessLogic.storeBackupRI(ct.getBackupRI());
		}
		/** INIT_RI message **/
		else if(ctt.equals(ControlTupleType.INIT_RI)){
			NodeManager.nLogger.info("-> Node "+nodeDescr.getNodeId()+" recv ControlTuple.INIT_RI from : "+ct.getInitRI().getNodeId());
			coreProcessLogic.installRI(ct.getInitRI());
		}
		/** SCALE_OUT message **/
		else if(ctt.equals(ControlTupleType.SCALE_OUT)) {
			if(P.valueFor("soccpaper").equals("false") || processingUnit.getOperator() instanceof StatelessOperator){
				//Ack the message, we do not need to wait until the end
				NodeManager.nLogger.info("-> Node "+nodeDescr.getNodeId()+" recv ControlTuple.SCALE_OUT");
				// Get index of new replica operator
				int newOpIndex = -1;
				for(PlacedOperator op: processingUnit.getOperator().getOpContext().downstreams) {
					if (op.opID() == ct.getScaleOutInfo().getNewOpId())
						newOpIndex = op.index();
				}
				// Get index of the scaling operator
				int oldOpIndex = processingUnit.getOperator().getOpContext().findDownstream(ct.getScaleOutInfo().getOldOpId()).index();
				coreProcessLogic.scaleOut(ct.getScaleOutInfo(), newOpIndex, oldOpIndex);
				controlDispatcher.ackControlMessage(genericAck, os);
			}
			else{
				NodeManager.nLogger.info("-> Node "+nodeDescr.getNodeId()+" recv ControlTuple.SCALE_OUT");
				int oldOpId = ct.getScaleOutInfo().getOldOpId();
				int newOpId = ct.getScaleOutInfo().getNewOpId();
				
				int newOpIndex = -1;
				for(PlacedOperator op: processingUnit.getOperator().getOpContext().downstreams) {
					if (op.opID() == ct.getScaleOutInfo().getNewOpId())
						newOpIndex = op.index();
				}
				// Get index of the scaling operator
				int oldOpIndex = processingUnit.getOperator().getOpContext().findDownstream(ct.getScaleOutInfo().getOldOpId()).index();
				coreProcessLogic.backupRoutingInformation(oldOpId);
				coreProcessLogic.manageStreamScaleOut(oldOpId, newOpId, oldOpIndex, newOpIndex);
				
				coreProcessLogic.directReplayState(new ReplayStateInfo(oldOpId, newOpId, false), bh);
				controlDispatcher.ackControlMessage(genericAck, os);
			}
		}
		/** REPLAY_STATE **/
		else if(ctt.equals(ControlTupleType.REPLAY_STATE)){
			//Replay the state that this node keeps
			NodeManager.nLogger.info("-> Node "+nodeDescr.getNodeId()+" recv ControlTuple.REPLAY_STATE");
			coreProcessLogic.directReplayState(ct.getReplayStateInfo(), bh);
		}
		/** STATE_CHUNK **/
		else if(ctt.equals(ControlTupleType.STATE_CHUNK)){
			// One out of n chunks of state received
			NodeManager.nLogger.info("-> Node "+nodeDescr.getNodeId()+" recv ControlTuple.STATE_CHUNK");
			StateChunk chunk = ct.getStateChunk();
//			System.out.println("CHUNK rcvd: "+chunk.getSequenceNumber());
			coreProcessLogic.handleNewChunk(chunk);
		}
		/** RESUME message **/
		else if (ctt.equals(ControlTupleType.RESUME)) {
			NodeManager.nLogger.info("-> Node "+nodeDescr.getNodeId()+" recv ControlTuple.RESUME");
			Resume resumeM = ct.getResume();
			
			// If I have previously splitted the state, I am in WAITING FOR STATE-ACK status and I have to replay it.
			// I may be managing a state but I dont have to replay it if I have not splitted it previously
			if(processingUnit.getSystemStatus().equals(StatefulProcessingUnit.SystemStatus.WAITING_FOR_STATE_ACK)){
				/// \todo {why has resumeM a list?? check this}
				for (int opId: resumeM.getOpId()){
					//Check if I am managing the state of any of the operators to which state must be replayed
					/// \todo{if this is waiting for ack it must be managing the state, so this IF would be unnecessary}
					if(processingUnit.isManagingStateOf(opId)){
						/** CHANGED ON 11/12/2012 on the road to version 0.2 **/
//						/// \todo{if this is waiting for ack it must be managing the state, so this IF would be unnecessary}
//						if(subclassOperator instanceof StateSplitI){
						NodeManager.nLogger.info("-> Replaying State");
						coreProcessLogic.replayState(opId);
//						}
					}
					else{
						NodeManager.nLogger.info("-> NOT in charge of managing this state");
					}
				}
				//Once I have replayed the required states I put my status to NORMAL
				processingUnit.setSystemStatus(StatefulProcessingUnit.SystemStatus.NORMAL);
			}
			else{
				NodeManager.nLogger.info("-> Ignoring RESUME state, I did not split this one");
			}
			//Finally ack the processing of this message
			controlDispatcher.ackControlMessage(genericAck, os);
		}
		
		/** RECONFIGURE message **/
		else if(ctt.equals(ControlTupleType.RECONFIGURE)){
			processCommand(ct.getReconfigureConnection(), os);
		}
	}
	
	/// \todo {stopping and starting the conn should be done from updateConnection in some way to hide the complexity this introduces here}
	public void processCommand(ReconfigureConnection rc, OutputStream os){
		String command = rc.getCommand();
		NodeManager.nLogger.info("-> Node "+nodeDescr.getNodeId()+" recv "+command+" command ");
		InetAddress ip = null;
		int opId = rc.getOpId();
		
		try{
			ip = InetAddress.getByName(rc.getIp());
		}
		catch (UnknownHostException uhe) {
			NodeManager.nLogger.severe("-> Node "+nodeDescr.getNodeId()+" EXCEPTION while getting IP from msg "+uhe.getMessage());
			uhe.printStackTrace();
		}
		/** RECONFIGURE DOWN or RECONFIGURE UP message **/
		if(command.equals("reconfigure_D") || command.equals("reconfigure_U") || command.equals("just_reconfigure_D")){
//			operatorStatus = OperatorStatus.RECONFIGURING_COMM;
			processingUnit.reconfigureOperatorLocation(opId, ip);
			
				//If no twitter storm, then I have to stop sending data and replay, otherwise I just update the conn
				/// \test {what is it is twitter storm but it is also the first node, then I also need to stop connection, right?}
			if((command.equals("reconfigure_D") || command.equals("just_reconfigure_D"))){
				
				
				
				processingUnit.stopConnection(opId);
				
				
				
			}
			processingUnit.reconfigureOperatorConnection(opId, ip);
			
			if(command.equals("reconfigure_U")){
				coreProcessLogic.sendRoutingInformation(opId, rc.getOperatorType());
			}
			if(command.equals("reconfigure_D")){
				
				/** 1st mode. Send state from Buffer class **/
				if(P.valueFor("ftDiskMode").equals("false")){
					// the new way would be something like the following. Anyway it is necessary to check if downstream is statefull or not
					if(processingUnit.isManagingStateOf(opId)){
						/** WHILE REFACTORING THE FOLLOWING IF WAS REMOVED, this was here for a reason **/
//						if(subclassOperator instanceof StateSplitI){
							//new Thread(new StateReplayer(opContext.getOIfromOpId(opId, "d"))).start();
						NodeManager.nLogger.info("-> Replaying State");
						coreProcessLogic.replayState(opId);
//						}
					}
					else{
						NodeManager.nLogger.info("-> NOT in charge of managing this state");
					}
				}
				/** Large scale mode. Stream state chunks from disk **/
				else{
					// We create a new replayState request. Coming from the op to recover
					// opId (op to recover), 1 (fake), true (is failure recovery)
					coreProcessLogic.directReplayState(new ReplayStateInfo(opId, 1, true), bh);
					
				}
			}
//			operatorStatus = OperatorStatus.NORMAL;
			//ackControlMessage(os);
		}
		/** ADD DOWN or ADD UP message **/
		else if(command.equals("add_downstream") || command.equals("add_upstream")){
//			operatorStatus = OperatorStatus.RECONFIGURING_COMM;
			OperatorStaticInformation loc = new OperatorStaticInformation(new Node(ip, rc.getNode_port()), rc.getInC(), rc.getInD(), rc.getOperatorNature());
			if(command.equals("add_downstream")){
				processingUnit.addDownstream(opId, loc);
			}
			else if (command.equals("add_upstream")) {
				//Configure new upstream
				processingUnit.addUpstream(opId, loc);
				//Send to that upstream the routing information I am storing (in case there are ri).
				coreProcessLogic.sendRoutingInformation(opId, rc.getOperatorType());
				//Re-Check the upstreamBackupIndex. Re-check what upstream to send the backup state.
				int upstreamSize = processingUnit.getOperator().getOpContext().upstreams.size();
				reconfigureUpstreamBackupIndex(upstreamSize);
				dsa.reconfigureNumUpstream(upstreamSize);
			}
			controlDispatcher.ackControlMessage(genericAck, os);
		}
		/** SYSTEM READY message **/
		else if (command.equals("system_ready")){
			controlDispatcher.ackControlMessage(genericAck, os);
			//Now that all the system is ready (both down and up) I manage my own information and send the required msgs
			coreProcessLogic.sendInitialStateBackup();
		}
		/** REPLAY message **/
		/// \todo {this command is only used for twitter storm model...}
		else if (command.equals("replay")){
			//ackControlMessage(os);
			//FIXME there is only one, this must be done for each conn
			processingUnit.stopConnection(opId);
			/// \todo{avoid this deprecated function}
			//opCommonProcessLogic.startReplayer(opID);
			SynchronousCommunicationChannel cci = puCtx.getCCIfromOpId(opId, "d");
			outputQueue.replayTuples(cci);
		}
		/** CONFIG SOURCE RATE message **/
		/// \todo {this command should not be delivered to operator. Maybe to nodeManager...}
//		else if (command.equals("configureSourceRate")){
//			controlDispatcher.ackControlMessage(genericAck, os);
//			int numberEvents = rc.getOpId();
//			int time = rc.getInC();
//			if(numberEvents == 0 && time == 0){
//				Main.maxRate = true;
//			}
//			else{
//				Main.maxRate = false;
//				Main.eventR = numberEvents;
//				Main.period = time;
//			}
//		}
		/** SAVE RESULTS RATE message **/
		/// \todo {this command should not be delivered to operator. Maybe to nodeManager...}
		else if (command.equals("saveResults")){
//			dispatcher.ackControlMessage(genericAck, os);
//			try{
//			((Snk)this.subclassOperator).save();
//			}catch(Exception e){
//				((SmartWordCounter)this.subclassOperator).save();
//			}
		}
		/** DEACTIVATE elft mechanism message **/
		/// \todo {this command should not be delivered to operator. Maybe to nodeManager...}
//		else if (command.equals("deactivateMechanisms")){
//			controlDispatcher.ackControlMessage(genericAck, os);
//			if(Main.eftMechanismEnabled){
//				NodeManager.nLogger.info("--> Desactivated ESFT mechanisms.");
//				Main.eftMechanismEnabled = false;
//			}
//			else{
//				NodeManager.nLogger.info("--> Activated ESFT mechanisms.");
//				Main.eftMechanismEnabled = true;
//			}
//		}
		/** NOT RECOGNIZED message **/
		else{
			NodeManager.nLogger.warning("-> Op.processCommand, command not recognized");
			throw new RuntimeException("Operator: ERROR in processCommand");
		}
	}
	
	public void ack(long ts) {
		ControlTuple ack = new ControlTuple(ControlTupleType.ACK, processingUnit.getOperator().getOperatorId(), ts);
		controlDispatcher.sendAllUpstreams(ack);
	}
	
	@Deprecated
	public void _signalOpenBackupSession(){
		int opId = processingUnit.getOperator().getOperatorId();
		NodeManager.nLogger.info("% -> Opening backup session from: "+opId);
		ControlTuple openSignal = new ControlTuple().makeOpenSignalBackup(opId);
//		controlDispatcher.sendUpstream(openSignal, backupUpstreamIndex);
//		controlDispatcher.sendUpstreamWaitForReply(openSignal, backupUpstreamIndex);
		System.out.println("open session to 0");
		controlDispatcher.sendUpstreamWaitForReply(openSignal, 0);
		System.out.println("open session to 1");
		controlDispatcher.sendUpstreamWaitForReply(openSignal, 1);
	}
	
	public void signalOpenBackupSession(){
		int opId = processingUnit.getOperator().getOperatorId();
		NodeManager.nLogger.info("% -> Opening backup session from: "+opId);
		ControlTuple openSignal = new ControlTuple().makeOpenSignalBackup(opId);
		controlDispatcher.sendUpstreamWaitForReply(openSignal, backupUpstreamIndex);
	}
	
	@Deprecated
	public void _signalCloseBackupSession(){
		int opId = processingUnit.getOperator().getOperatorId();
		NodeManager.nLogger.info("% -> Closing backup session from: "+opId);
		ControlTuple closeSignal = new ControlTuple().makeCloseSignalBackup(opId, totalNumberOfChunks);
//		controlDispatcher.sendUpstream(closeSignal, backupUpstreamIndex);
		controlDispatcher.sendUpstream(closeSignal, 0);
		controlDispatcher.sendUpstream(closeSignal, 1);
	}
	
	public void signalCloseBackupSession(){
		int opId = processingUnit.getOperator().getOperatorId();
		NodeManager.nLogger.info("% -> Closing backup session from: "+opId);
		ControlTuple closeSignal = new ControlTuple().makeCloseSignalBackup(opId, totalNumberOfChunks);
		controlDispatcher.sendUpstream(closeSignal, backupUpstreamIndex);
	}

	public void manageBackupUpstreamIndex(int opId){
		//Firstly, I configure my upstreamBackupIndex, which is the index of the operatorId coming in this message (the one in charge of managing it)
//		int newIndex = opContext.findUpstream(opId).index();
		int newIndex = processingUnit.getOperator().getOpContext().findUpstream(opId).index();
		if(backupUpstreamIndex != -1 && newIndex != backupUpstreamIndex){
			//ControlTuple ct = new ControlTuple().makeInvalidateMessage(backupUpstreamIndex);
			ControlTuple ct = new ControlTuple().makeInvalidateMessage(processingUnit.getOperator().getOperatorId());
			
			controlDispatcher.sendUpstream(ct, backupUpstreamIndex);
		}
		//Set the new backup upstream index, this has been sent by the manager.
		this.setBackupUpstreamIndex(newIndex);
	}
	
	public void sendBackupState(ControlTuple ctB){
		int stateOwnerId = ctB.getBackupState().getOpId();
		NodeManager.nLogger.info("% -> Backuping state with owner: "+stateOwnerId+" to NODE index: "+backupUpstreamIndex);
		controlDispatcher.sendUpstream_large(ctB, backupUpstreamIndex);
	}
	
	@Deprecated
	public void _sendBlindData(ControlTuple ctB, int hint, int hack){
		int stateOwnerId = ctB.getStateChunk().getOpId();
//		NodeManager.nLogger.info("% -> Backuping state with owner: "+stateOwnerId+" to NODE index: "+backupUpstreamIndex);
		controlDispatcher.sendUpstream_blind(ctB, hack, hint);
	}
	
	public void sendBlindData(ControlTuple ctB, int hint){
		int stateOwnerId = ctB.getStateChunk().getOpId();
//		NodeManager.nLogger.info("% -> Backuping state with owner: "+stateOwnerId+" to NODE index: "+backupUpstreamIndex);
		controlDispatcher.sendUpstream_blind(ctB, backupUpstreamIndex, hint);
	}
	
	public void sendBlindMetaData(int data){
		controlDispatcher.sendUpstream_blind_metadata(data, backupUpstreamIndex);
	}
	
	public void sendRawData(ControlTuple ctB){
		int dataOwnerId = ctB.getRawData().getOpId();
		NodeManager.nLogger.info("% -> Backuping DATA with owner: "+dataOwnerId+" to NODE index: "+backupUpstreamIndex);
		controlDispatcher.sendUpstream_large(ctB, backupUpstreamIndex);
	}
	
	public void setTotalNumberOfStateChunks(int number){
		this.totalNumberOfChunks = number;
		//coreProcessLogic.setTotalNumberOfChunks(number);
	}
	
	//Initial compute of upstreamBackupindex. This is useful for initial instantiations (not for splits, because in splits, upstreamIdx comes in the INIT_STATE)
	// This method is called only once during initialisation of the node (from setRuntime)
	public void configureUpstreamIndex(int upstreamSize){
		int ownInfo = Router.customHash(getNodeDescr().getNodeId());
//		int upstreamSize = opContext.upstreams.size();
		
		//source obviously cant compute this value
		if(upstreamSize == 0){
			NodeManager.nLogger.warning("-> If this node is not the most upstream, there is a problem");
			return;
		}
		int upIndex = ownInfo%upstreamSize;
		upIndex = (upIndex < 0) ? upIndex*-1 : upIndex;
		
		//Update my information
		System.out.println("% ConfigureUpstreamIndex");
		this.setBackupUpstreamIndex(upIndex);
		NodeManager.nLogger.info("-> The new Upstream backup INDEX is: "+backupUpstreamIndex);
	}
	
	public void reconfigureUpstreamBackupIndex(int upstreamSize){
		NodeManager.nLogger.info("-> Reconfiguring upstream backup index");
		//First I compute my own info, which is the hash of my id.
		/** There is a reason to hash the opId. Imagine upSize=2 and opId of downstream 2, 4, 6, 8... So better to mix the space*/
		int ownInfo = Router.customHash(nodeDescr.getNodeId());
//		int upstreamSize = opContext.upstreams.size();
		int upIndex = ownInfo%upstreamSize;
		//Since ownInfo (hashed) may be negative, this enforces the final value is always positive.
		upIndex = (upIndex < 0) ? upIndex*-1 : upIndex;
		//If the upstream is different from previous sent, then additional management is necessary
		//In particular, invalidate the management of my state in the previous operator in charge to do so...
		//... and notify the new operator in charge of my OPID
		//UPDATE!! AND send STATE in case this operator is backuping state
		if(upIndex != backupUpstreamIndex){
			NodeManager.nLogger.info("-> Upstream backup has changed...");
			//Invalidate old Upstream state
			//ControlTuple ct = new ControlTuple().makeInvalidateMessage(backupUpstreamIndex);
			ControlTuple ct = new ControlTuple().makeInvalidateMessage(processingUnit.getOperator().getOperatorId());
			controlDispatcher.sendUpstream(ct, backupUpstreamIndex);
		
			//Update my information
			System.out.println("% ReconfigureUpstreamIndex");
			this.setBackupUpstreamIndex(upIndex);
			
			//Without waiting for the counter, we backup the state right now, (in case operator is stateful)
			if(processingUnit.isNodeStateful()){
				NodeManager.nLogger.info("-> sending BACKUP_STATE to the new manager of my state");
				//((StatefulProcessingUnit)processingUnit).checkpointAndBackupState();
				ArrayList<Integer> pr = ((StatefulProcessingUnit)processingUnit).getPartitioningRange();
				int[] pr1 = new int[]{pr.get(0), pr.get(1)};
				((StatefulProcessingUnit)processingUnit).lockFreeParallelCheckpointAndBackupState(pr1);
			}
		}
	}
}
