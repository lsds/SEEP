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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.lsds.seep.Main;
import uk.ac.imperial.lsds.seep.buffer.Buffer;
import uk.ac.imperial.lsds.seep.comm.serialization.ControlTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.Ack;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.BackupNodeState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.BackupOperatorState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.BackupRI;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.InitNodeState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.InitOperatorState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.InitRI;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.InvalidateState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.RawData;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.ReconfigureConnection;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.ReplayStateInfo;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.Resume;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.ScaleOutInfo;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.StateAck;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.StateChunk;
import uk.ac.imperial.lsds.seep.infrastructure.NodeManager;
import uk.ac.imperial.lsds.seep.operator.Partitionable;
import uk.ac.imperial.lsds.seep.operator.State;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;
import uk.ac.imperial.lsds.seep.operator.OperatorContext.PlacedOperator;
import uk.ac.imperial.lsds.seep.processingunit.IProcessingUnit;
import uk.ac.imperial.lsds.seep.processingunit.PUContext;
import uk.ac.imperial.lsds.seep.processingunit.StatefulProcessingUnit;
import uk.ac.imperial.lsds.seep.processingunit.StreamStateChunk;
import uk.ac.imperial.lsds.seep.reliable.BackupHandler;
import uk.ac.imperial.lsds.seep.reliable.MemoryChunk;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.MapSerializer;


public class CoreProcessingLogic implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private CoreRE owner;
	private IProcessingUnit pu;
	private PUContext puCtx;
	
	private Kryo k;
	
	public void initializeSerialization(){
		k = new Kryo();
		k.setClassLoader(owner.getRuntimeClassLoader());
		k.register(ControlTuple.class);
		k.register(StreamStateChunk.class);
		k.register(StateChunk.class);
		k.register(HashMap.class, new MapSerializer());
		k.register(BackupOperatorState.class);
		k.register(byte[].class);
		k.register(RawData.class);
		k.register(Ack.class);
		k.register(BackupNodeState.class);
		k.register(Resume.class);
		k.register(ScaleOutInfo.class);
		k.register(StateAck.class);
		k.register(ArrayList.class);
		k.register(BackupRI.class);
		k.register(InitNodeState.class);
		k.register(InitOperatorState.class);
		k.register(InitRI.class);
		k.register(InvalidateState.class);
		k.register(ReconfigureConnection.class);
	}
	
	public void setOwner(CoreRE owner) {
		this.owner = owner;
	}
	public void setOpContext(PUContext puCtx) {
		this.puCtx = puCtx;
	}
	public void setProcessingUnit(IProcessingUnit processingUnit){
		this.pu = processingUnit;
	}
	
	//map where it is saved the ack received by each downstream
	private Map<Integer, TimestampTracker> downstreamLastAck = new HashMap<Integer, TimestampTracker>();
	
	//routing information, operatorCLASS - backupRI
//	private Seep.BackupRI backupRI = null;
	private HashMap<String, BackupRI> backupRoutingInformation = new HashMap<String, BackupRI>();
	
	/// \todo{check if i can avoid operations in the data structure when receiving same msg again and again}
	public void storeBackupRI(BackupRI backupRI){
		//Get operator Class
		String operatorType = backupRI.getOperatorType();
		//Save in the provided map the backupRI for this upstream, if there are replicas then we will have replicated info here...
		backupRoutingInformation.put(operatorType, backupRI);
	}
	
	/**  installRI receives a message with the routing information that this operator must implement 
	 * there are N indexes, and thus, there must be N load balancers. Each of these load balancers must have the same indexes,
	 * this is, the same routing information.
	 * Consequently, I have to locate the load balancers that this operator has right now and change the routing information.
	 * Then, create the new required ones with the new information**/
	
	public synchronized void installRI(InitRI initRI){
		NodeManager.nLogger.info("-> Installing RI from Node: "+initRI.getNodeId());
		//Create new LB with the information received
		ArrayList<Integer> indexes = new ArrayList<Integer>(initRI.getIndex());
		ArrayList<Integer> keys = new ArrayList<Integer>(initRI.getKey());
		ArrayList<Integer> downstreamIds = new ArrayList<Integer>();
		for(Integer index : indexes){
			// opId from the most downstream operator
			int opId = pu.getOperator().getOpContext().getDownOpIdFromIndex(index);
			downstreamIds.add(opId);
		}
		// Reconfigure routing info of the most downstream operator
		pu.getOperator().getRouter().reconfigureRoutingInformation(downstreamIds, indexes, keys);
	}
	
	public void sendRoutingInformation(int opId, String operatorType){
		//Get index from opId
		int upstreamIndex = 0;
		for(PlacedOperator op : pu.getOperator().getOpContext().upstreams){
			if(op.opID() == opId){
				 upstreamIndex = op.index();
			}
		}
		System.out.println("## NEW UPSTREAM, op: "+opId+" type: "+operatorType);
		//If i dont have backup for that upstream, I am not in charge...
		System.out.println("BACKUPROUTINGINFO: "+backupRoutingInformation.toString());
		System.out.println("OP TYPE: "+operatorType);
		if(!backupRoutingInformation.containsKey(operatorType)){
			NodeManager.nLogger.info("-> NO routing info to send");
			return;
		}
		NodeManager.nLogger.info("-> Sending backupRI to upstream");
System.out.println("KEY: "+operatorType);
		//Otherwise I pick the backupRI msg from the data structure where i am storing these ones
		BackupRI backupRI = backupRoutingInformation.get(operatorType);
		//Form the message
		ControlTuple ct = new ControlTuple().makeInitRI(owner.getNodeDescr().getNodeId(), backupRI.getIndex(), backupRI.getKey());
		//Send the message
		owner.getControlDispatcher().sendUpstream(ct, upstreamIndex);
	}
	
	public void processBackupState(BackupOperatorState ct){
		int opId = ct.getOpId();
		SynchronousCommunicationChannel downStream = puCtx.getCCIfromOpId(opId, "d");
		TimestampTracker ts_e = ct.getState().getData_ts();
		TimestampTracker smaller = TimestampTracker.returnSmaller(ts_e, downStream.getReconf_ts());
		if(TimestampTracker.isSmallerOrEqual(downStream.getReconf_ts(), smaller)){
			
		//if(downStream.getReconf_ts() <= ts_e){
			puCtx.getBuffer(opId).replaceBackupOperatorState(ct);
		}
		else{
			NodeManager.nLogger.warning("-> Received state generated after the beginning of the reconfigure process");
		}
	}
	
	public void processRawData(RawData rw){
		int opId = rw.getOpId();
		SynchronousCommunicationChannel downStream = puCtx.getCCIfromOpId(opId, "d");
		TimestampTracker ts_e = rw.getTs();
		TimestampTracker smaller = TimestampTracker.returnSmaller(ts_e, downStream.getReconf_ts());
		if(TimestampTracker.isSmallerOrEqual(downStream.getReconf_ts(), smaller)){
//			puCtx.getBuffer(opId).replaceBackupOperatorState(ct);
			puCtx.getBuffer(opId).replaceRawData(rw);
		}
		else{
			NodeManager.nLogger.warning("-> Received data generated after the beginning of a reconfigure process");
		}
	}

	public synchronized void processAck(Ack ct){
		int opId = ct.getOpId();
		long ack_ts = ct.getTs();
		processAck(opId, ack_ts);
	}
	
//	private synchronized void _processAck(int opId, long ack_ts){
//		// Trim local buffer
//		Buffer buffer = puCtx.getBuffer(opId);
//		buffer.trim(ack_ts);
//		
//		// Check when to send ack upstream
//		int counter = 0;
//		long minWithCurrent = ack_ts;
//		long minWithoutCurrent = Long.MAX_VALUE;
//		for( Map.Entry<Integer, Long> entry: downstreamLastAck.entrySet()) {
//			long ts = entry.getValue();
//			// Search for the minimum ack
//			if (entry.getKey() != opId) {
//				if(ts < minWithCurrent){ 
//					minWithCurrent = ts;
//				}
//			}
//			if(ts < minWithoutCurrent){
//				minWithoutCurrent = ts;
//			}
//			counter++;
//		}
//		downstreamLastAck.put(opId, ack_ts);
//		// Forward only if stateless. Stateful operator forward the state instead
//		if(pu.getOperator() instanceof StatelessOperator || !((StatefulProcessingUnit)pu).isCheckpointEnabled()){
//			owner.ack(minWithCurrent);
//		}
//		// To indicate that this is the last ack processed by this operator
//		owner.setTs_ack(ack_ts);
//	}
	
	private synchronized void processAck(int opId, long ack_ts){
		// Trim local buffer
		Buffer buffer = puCtx.getBuffer(opId);
		TimestampTracker oldest = buffer.trim(ack_ts);
		
		// Check whether this operator is responsible to control when to backpropagate acks
		if(pu.getOperator() instanceof StatelessOperator || !((StatefulProcessingUnit)pu).isCheckpointEnabled()){
			// First assign this ackV to the opId, in case it is updating a previous value
			if(oldest != null){
				downstreamLastAck.put(opId, oldest);
			}
			// Now we get the smaller one and backpropagate it (if we have seen at least once per downstream)
			TimestampTracker toBackPropagate = null;
//			System.out.println("OLDEST : "+oldest);
			for(Integer id : downstreamLastAck.keySet()){
//				System.out.println("OPID: "+id+" tt: "+downstreamLastAck.get(id));
				toBackPropagate = TimestampTracker.returnSmaller(toBackPropagate, downstreamLastAck.get(id));
			}
			// Here we have the smaller vector, that we use to backpropagate if it is not null
			if(toBackPropagate != null){
//				System.out.println("TO backpropagate: "+toBackPropagate);
				owner.ack(toBackPropagate);
			}
			else{
				System.out.println("is NULL");
			}
		}
	}
	
	public void splitState(int oldOpId, int newOpId, int key) {
//		StateSplitI operatorToSplit = (StateSplitI)owner.getSubclassOperator();
//		BackupOperatorState oldState = puCtx.getBuffer(oldOpId).getBackupState();
		
//		BackupNodeState backupNodeState = puCtx.getBuffer(oldOpId).getBackupState();
//		for(BackupOperatorState bos : backupNodeState.getBackupOperatorState()){
//			System.out.println("% Got Buffer from OP: "+oldOpId+" and BOS opId is: "+bos.getOpId());
//		}
		
		BackupOperatorState backupOperatorState = puCtx.getBuffer(oldOpId).getBackupState();
		
		/** Small hack
		BackupOperatorState backupOperatorState = backupNodeState.getBackupOperatorStateWithOpId(oldOpId);
		**/
		/** DUE to the last refactorization, we added a layer of indirection, making an explicit differentiation between node and operator state.
		 * the problem of this approach is that it was breaking here. I have to access the state directly, something that without the node layer
		 * was assumed. With the node layer, tough, the system looks for the particular ID, that might have been lost in previous calls to splitState**/
//		BackupOperatorState backupOperatorState = backupNodeState.getBackupOperatorState()[0];
		
		
		State stateToSplit = backupOperatorState.getState();
		int stateCheckpointInterval = stateToSplit.getCheckpointInterval();
		String stateTag = stateToSplit.getStateTag();
//		long data_ts = stateToSplit.getData_ts();
		TimestampTracker data_ts = stateToSplit.getData_ts();
		State splitted[] = null;
		if(stateToSplit instanceof Partitionable){
			splitted = ((Partitionable)stateToSplit).splitState(stateToSplit, key);
		}
		else{
			NodeManager.nLogger.warning("-> this state is not partitionable");
			// Crash the system at this point
			System.exit(0);
		}
		// Create the two states, and fill the necessary info, as ts, etc.
		//Set the old partition
		State oldStatePartition = splitted[0];
		oldStatePartition.setOwnerId(oldOpId);
		oldStatePartition.setCheckpointInterval(stateCheckpointInterval);
		oldStatePartition.setStateTag(stateTag);
		oldStatePartition.setData_ts(data_ts);
		// Set the new partition
		State newStatePartition = splitted[1];
		newStatePartition.setOwnerId(newOpId);
		newStatePartition.setCheckpointInterval(stateCheckpointInterval);
		newStatePartition.setStateTag(stateTag);
		newStatePartition.setData_ts(data_ts);
		
		BackupOperatorState newPartition = new BackupOperatorState();
		BackupOperatorState oldPartition = new BackupOperatorState();
		
		newPartition.setOpId(newOpId);
		newPartition.setStateClass(stateTag);
		newPartition.setState(newStatePartition);
		
		oldPartition.setOpId(oldOpId);
		oldPartition.setStateClass(stateTag);
		oldPartition.setState(oldStatePartition);

		NodeManager.nLogger.severe("-> Replacing backup states for OP: "+oldOpId+" and OP: "+newOpId);
		
		// Replace state in the old and new operators
		puCtx.getBuffer(oldOpId).replaceBackupOperatorState(oldPartition);
		puCtx.getBuffer(newOpId).replaceBackupOperatorState(newPartition);
		
		//Indicate that we are now managing both these states
		NodeManager.nLogger.severe("-> Registering management of state for OP: "+oldOpId+" and OP: "+newOpId);
		// We state we manage the state of the old and new ops replicas
		pu.registerManagedState(oldOpId);
		pu.registerManagedState(newOpId);
		
		
		System.out.println("% After SPLIT, oldOpId: "+puCtx.getBuffer(oldOpId).getBackupState().getOpId()+" newOpId: "+puCtx.getBuffer(newOpId).getBackupState().getOpId());
	}
	
	public synchronized void manageStreamScaleOut(int oldOpId, int newOpId, int oldOpIndex, int newOpIndex){
		pu.stopConnection(oldOpId);
		pu.stopConnection(newOpId);
		pu.getOperator().getRouter().newOperatorPartition(oldOpId, newOpId, oldOpIndex, newOpIndex);
	}
	
	public synchronized void scaleOut(ScaleOutInfo scaleOutInfo, int newOpIndex, int oldOpIndex){
		int oldOpId = scaleOutInfo.getOldOpId();
		int newOpId = scaleOutInfo.getNewOpId();
		boolean isStatefulScaleOut = scaleOutInfo.isStatefulScaleOut();
		
		//if operator is stateful and (this) can split state
//		if(opContext.isDownstreamOperatorStateful(oldOpId) && owner.subclassOperator instanceof StateSplitI){
		//If scaling operator is stateful
		if(isStatefulScaleOut){
			NodeManager.nLogger.info("-> Scaling out STATEFUL op");
			scaleOutStatefulOperator(oldOpId, newOpId, oldOpIndex, newOpIndex);
		}
		
		// If operator splitting is stateless...
//		else if (!opContext.isDownstreamOperatorStateful(oldOpId)){
		else{
			NodeManager.nLogger.info("-> Scaling out STATELESS op");
//			configureNewDownstreamStatelessOperatorPartition(oldOpId, newOpId, oldOpIndex, newOpIndex);
			pu.getOperator().getRouter().newOperatorPartition(oldOpId, newOpId, oldOpIndex, newOpIndex);
		}
	}
	
	public void scaleOutStatefulOperator(int oldOpId, int newOpId, int oldOpIndex, int newOpIndex){
		//All operators receiving the scale-out message have to change their routing information
		int newKey = configureNewDownstreamStatefulOperatorPartition(oldOpId, newOpId, oldOpIndex, newOpIndex);
		//I have configured the new split, check if I am also in charge of splitting the state or not
		if(pu.isManagingStateOf(oldOpId)){
			NodeManager.nLogger.info("-> Splitting state");
			splitState(oldOpId, newOpId, newKey);
			pu.setSystemStatus(StatefulProcessingUnit.SystemStatus.WAITING_FOR_STATE_ACK);
			//Just one operator needs to send routing information backup, cause downstream is saving this info according to op type.
			NodeManager.nLogger.info("-> Generating and sending RI backup");
//			backupRoutingInformation(oldOpId);
		}
		else{
			NodeManager.nLogger.info("-> NOT in charge of split state");
		}
		
		pu.setSystemStatus(StatefulProcessingUnit.SystemStatus.WAITING_FOR_STATE_ACK);
		//Just one operator needs to send routing information backup, cause downstream is saving this info according to op type.
		NodeManager.nLogger.info("-> Generating and sending RI backup");
//		backupRoutingInformation(oldOpId);
		
		//I always backup the routing info. This leads to replicate messages, but I cant avoid it easily since I can have more than one type of upstream
		///\fixme{FIX THIS INEFFICIENCY}
		backupRoutingInformation(oldOpId);
	}
	
	public int configureNewDownstreamStatefulOperatorPartition(int oldOpId, int newOpId, int oldOpIndex, int newOpIndex){
		int newKey = -1;
		
		/** BLOCK OF CODE TO REFACTOR **/
		//. stop sending data to op1 remember last data sent
		SynchronousCommunicationChannel oldConnection = ((SynchronousCommunicationChannel)puCtx.getDownstreamTypeConnection().get(oldOpIndex));
		// necessary to ignore state checkpoints of the old operator before split.
		long last_ts = oldConnection.getLast_ts();
		TimestampTracker last_tsV = oldConnection.getBuffer().getInputVTsForOutputTs(last_ts);
		oldConnection.setReconf_ts(last_tsV);
		/** END BLOCK OF CODE **/
				
		//Stop connections to perform the update
		NodeManager.nLogger.info("-> Stopping connections of oldOpId: "+oldOpId+" and newOpId: "+newOpId);
		pu.stopConnection(oldOpId);
		pu.stopConnection(newOpId);

		newKey = pu.getOperator().getRouter().newOperatorPartition(oldOpId, newOpId, oldOpIndex, newOpIndex);
		return newKey;
	}
	
	public void backupRoutingInformation(int oldOpId) {
		//Get routing information of the operator that has scaled out
		ArrayList<Integer> indexes = pu.getRouterIndexesInformation(oldOpId);
		ArrayList<Integer> keys = pu.getRouterKeysInformation(oldOpId);
		//Create message
		ControlTuple msg = new ControlTuple().makeBackupRI(owner.getNodeDescr().getNodeId(), indexes, keys, pu.getOperator().getClass().getName());
		//Send message to downstreams (for now, all downstreams)
		/// \fixme{make this more efficient, not sending to all (same mechanism upstreamIndexBackup than downstreamIndexBackup?)}
		for(Integer index : indexes){
			owner.getControlDispatcher().sendDownstream(msg, index);
		}
	}
	
	public void replayState(int opId) {
		//Get channel information
		SynchronousCommunicationChannel cci = puCtx.getCCIfromOpId(opId, "d");
		Buffer buffer = cci.getBuffer();
		Socket controlDownstreamSocket = cci.getDownstreamControlSocket();

		//Get a proper init state and just send it
		BackupOperatorState backupOperatorState = buffer.getBackupState();
		ControlTuple ct = new ControlTuple().makeInitOperatorState(pu.getOperator().getOperatorId(), backupOperatorState.getState());
		try {
			owner.getControlDispatcher().initStateMessage(ct, controlDownstreamSocket.getOutputStream());
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void processInitState(InitOperatorState ct){
		//Reconfigure backup stream index
		//Pick one of the opIds of the message
		int opId = ct.getOpId();
		owner.manageBackupUpstreamIndex(opId);
		//Clean the data processing channel from remaining tuples in old batch
		NodeManager.nLogger.info("Changing to INITIALISING STATE, stopping all incoming comm");
		pu.setSystemStatus(StatefulProcessingUnit.SystemStatus.INITIALISING_STATE);
		//Give state to processing unit for it to manage it
		((StatefulProcessingUnit)pu).installState(ct);
		//Once the state has been installed, recover dataProcessingChannel
		
		//Send a msg to ask for the rest of information. (tuple replaying)
		NodeManager.nLogger.info("-> Sending STATE_ACK");
		ControlTuple rb = new ControlTuple().makeStateAck(owner.getNodeDescr().getNodeId(), pu.getOperator().getOperatorId());
		owner.getControlDispatcher().sendAllUpstreams(rb);
//		pu.cleanInputQueue();
		pu.setSystemStatus(StatefulProcessingUnit.SystemStatus.NORMAL);
		System.out.println("Changing to NORMAL mode, recovering data processing");
	}
	
	
	//This method simply backups its state. It is useful for making the upstream know that is in charge of managing this state.
	public void sendInitialStateBackup(){
		//Without waiting for the counter, we backup the state right now, (in case operator is stateful)
		if(pu.isNodeStateful()){
		//if(owner.subclassOperator instanceof StatefulOperator){
System.out.println("NODE: "+owner.getNodeDescr().getNodeId()+" INITIAL BACKUP!!!!!!#############");
//			((StatefulOperator)owner.subclassOperator).generateBackupState();
			((StatefulProcessingUnit)pu).checkpointAndBackupState();
		}
	}
	
	public void directReplayState(ReplayStateInfo rsi, BackupHandler bh){
		// Stream to one or multiple nodes?
		File folder = new File("backup/");
		// this basically means that is because of a failure
		if(rsi.isStreamToSingleNode()){
			directReplayStateFailure(rsi, bh, folder);
		}
		else{
			directReplayStateScaleOut(rsi, bh, folder);
		}
	}
	
	private void directReplayStateFailure(ReplayStateInfo rsi, BackupHandler bh, File folder){
		int opId = rsi.getOldOpId();
		InetAddress ip = puCtx.getDCCfromOpId(opId).getIp();
		String lastSessionName = bh.getLastBackupSessionName(opId);
		ArrayList<File> filesToStream = new ArrayList<File>();
		int totalNumberChunks = 0;
		
		// Read folder and filter out files to send through the network
		try {
			Socket controlSocket = new Socket(ip, (Main.CONTROL_SOCKET+opId));
			NodeManager.nLogger.info("-> Request to stream to a single node");
			
			Output output = new Output(controlSocket.getOutputStream());
			///\todo{Can't all this block of code just be avoided by accessing directly the channels}
			// ok, cause there is no simple way to access to the filechannel names. We can use the file objects that are ready to
			// garbage collect to access to this information, saving some valuable IO interactions with the disk, not only in this block
			// but most improtnatly below, at deserialization time
			for(File chunkFile : folder.listFiles()){
				String chunkName = chunkFile.getName();
				if(matchSession(opId, chunkName, lastSessionName)){
					totalNumberChunks++;
					filesToStream.add(chunkFile);
					System.out.println("Filename: "+chunkName);
				}
			}

			long timeread = 0;
			long timewrite = 0;
			for(File chunk : filesToStream){
				Input i = new Input(new FileInputStream(chunk));
//				Input i = new Input(new FileInputStream(filesToStream.get(ig)));
				long a = System.currentTimeMillis();
				ControlTuple ct = k.readObject(i, ControlTuple.class);
				long b = System.currentTimeMillis();
				ct.getStateChunk().setTotalChunks(filesToStream.size());
//				ct.getStateChunk().setTotalChunks(fakechunksnumber);
				k.writeObject(output, ct);
				output.flush();
				long c = System.currentTimeMillis();
				i.close();
				System.out.println("CT: "+ct.toString());
				timeread = timeread + (b-a);
				timewrite = timewrite + (c-b);
			}
			System.out.println("READ: "+timeread);
			System.out.println("WRITE: "+timewrite);
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void directReplayStateScaleOut(ReplayStateInfo rsi, BackupHandler bh, File folder){
		int oldOpId = rsi.getOldOpId();
		int newOpId = rsi.getNewOpId();
		String lastSessionName = bh.getLastBackupSessionName(oldOpId);
		int totalNumberChunks = 0;
		
		InetAddress oldIp = puCtx.getDCCfromOpId(oldOpId).getIp();
		InetAddress newIp = puCtx.getDCCfromOpId(newOpId).getIp();
		
		ArrayList<File> filesToStream = new ArrayList<File>();
		try{
			Socket oldS = new Socket(oldIp, (Main.CONTROL_SOCKET+oldOpId));
			Socket newS = new Socket(oldIp, (Main.CONTROL_SOCKET+newOpId));
			Output oldO = new Output(oldS.getOutputStream());
			Output newO = new Output(newS.getOutputStream());
			// Get files to replay
			for(File chunkFile : folder.listFiles()){
				String chunkName = chunkFile.getName();
				if(matchSession(oldOpId, chunkName, lastSessionName)){
					totalNumberChunks++;
					filesToStream.add(chunkFile);
					System.out.println("Filename: "+chunkName);
				}
			}
			
			// There is a fixed size per chunk, so there is an upper bound size per partition. Let's then
			// make dynamically-sized chunks.
			// Every two file chunks, we send the batched state
			ArrayList<Object> oldPartition = new ArrayList<Object>();
			ArrayList<Object> newPartition = new ArrayList<Object>();
			for(File chunk : filesToStream){
				Input i = new Input(new FileInputStream(chunk));
				ControlTuple ct = k.readObject(i, ControlTuple.class);
				MemoryChunk mc = ct.getStateChunk().getMemoryChunk();
			}
			
			k.writeObject(output, ct);
			output.flush();
			i.close();
			System.out.println("CT: "+ct.toString());
			
		}
		catch(IOException io){
			
			io.printStackTrace();
		}
		
	}
	
	///\fixme{REFACTOR this whole method. Now it is time to hash on key and then module the downstream. Also we have to access
	// the routing information to make sure we are not doing anything wrong}
	private void _directReplayStateScaleOut(ReplayStateInfo rsi, final BackupHandler bh, File folder){
		NodeManager.nLogger.info("-> DISTRIBUTED-SCALE-OUT");
		int oldOpId = rsi.getOldOpId();
		int newOpId = rsi.getNewOpId();
		
		
		SynchronousCommunicationChannel old_cci = puCtx.getCCIfromOpId(oldOpId, "d");
		SynchronousCommunicationChannel new_cci = puCtx.getCCIfromOpId(newOpId, "d");
		Socket old_controlDownstreamSocket = old_cci.getDownstreamControlSocket();
		Socket new_controlDownstreamSocket = new_cci.getDownstreamControlSocket();
		
		//Only the old op could send a backup
		String lastSessionName = bh.getLastBackupSessionName(oldOpId);
		
		
		int old_totalNumberChunks = 0;
		int new_totalNumberChunks = 0;
		final ArrayList<File> old_filesToStream = new ArrayList<File>();
		ArrayList<File> new_filesToStream = new ArrayList<File>();
		// Read folder and filter out files to send through the network
		try {
			final Output old_output = new Output(old_controlDownstreamSocket.getOutputStream());
			Output new_output = new Output(new_controlDownstreamSocket.getOutputStream());
			for(File chunkFile : folder.listFiles()){
				String chunkName = chunkFile.getName();
				String[] splits = chunkName.split("_");
				// If chunk matches the current session
				if(splits[1].equals(lastSessionName)){
					if(splits[0].equals("P0")){
						old_totalNumberChunks++;
						old_filesToStream.add(chunkFile);
					}
					else if(splits[0].equals("P1")){
						new_totalNumberChunks++;
						new_filesToStream.add(chunkFile);
					}
				}
			}
			NodeManager.nLogger.info("-> Stream: "+old_filesToStream.size()+" to old operator");
			NodeManager.nLogger.info("-> Stream: "+new_filesToStream.size()+" to new operator");
			// Finally we stream the files back to the new operators (in parallel)
			for(File chunkFile : old_filesToStream){
				Input i;
				try {
					i = new Input(new FileInputStream(chunkFile));
					ControlTuple ct = k.readObject(i, ControlTuple.class);
					ct.getStateChunk().setTotalChunks(old_filesToStream.size());
					k.writeObject(old_output, ct);
					old_output.flush();
					i.close();
				} 
				catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			for(File chunkFile : new_filesToStream){
				Input i;
				try {
					i = new Input(new FileInputStream(chunkFile));
					ControlTuple ct = k.readObject(i, ControlTuple.class);
					ct.getStateChunk().setTotalChunks(new_filesToStream.size());
					k.writeObject(new_output, ct);
					new_output.flush();
					i.close();
				} 
				catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private int totalExpectedChunks = -1;
	private int totalNumberOfChunks = -1;
	public void setTotalNumberOfChunks(int totalNumberOfChunks){
		this.totalNumberOfChunks = totalNumberOfChunks;
	}
	private int totalReceivedChunks;
	
	public void handleNewChunk(StateChunk stateChunk){
		if(totalExpectedChunks == -1){
			totalExpectedChunks = stateChunk.getTotalChunks();
			//((Partitionable)).resetState();
			((StatefulProcessingUnit)pu).resetState();
//			((StatefulProcessingUnit)pu).configureNewPartitioningRange(stateChunk.getPartitioningRange());
		}
//		totalReceivedChunks++;
//		System.out.println("CHUNKS: "+totalReceivedChunks+"/"+totalExpectedChunks);
//		if(totalReceivedChunks == totalExpectedChunks){
//			// reset variables
//			totalReceivedChunks = 0;
//			totalExpectedChunks = -1; 
//		}
		((StatefulProcessingUnit)pu).mergeChunkToState(stateChunk);
		totalReceivedChunks++;
		System.out.println("CHUNKS: "+totalReceivedChunks+"/"+totalExpectedChunks);
		if(totalReceivedChunks == totalExpectedChunks){
			// reset variables
			totalReceivedChunks = 0;
			totalExpectedChunks = -1;
			((StatefulProcessingUnit)pu).mergeChunkToState(null);
			
//			//Once state is recovered, then we do this
			ControlTuple rb = new ControlTuple().makeStateAck(owner.getNodeDescr().getNodeId(), pu.getOperator().getOperatorId());
			owner.getControlDispatcher().sendAllUpstreams(rb);
			
			
			
			
//			int opId = stateChunk.getOpId();
//			owner.manageBackupUpstreamIndex(opId);
//			//Clean the data processing channel from remaining tuples in old batch
//			NodeManager.nLogger.info("Changing to INITIALISING STATE, stopping all incoming comm");
//			pu.setSystemStatus(StatefulProcessingUnit.SystemStatus.INITIALISING_STATE);
//			
//			
//			//Send a msg to ask for the rest of information. (tuple replaying)
//			NodeManager.nLogger.info("-> Sending STATE_ACK");
//			ControlTuple rb = new ControlTuple().makeStateAck(owner.getNodeDescr().getNodeId(), pu.getOperator().getOperatorId());
//			owner.getControlDispatcher().sendAllUpstreams(rb);
////			pu.cleanInputQueue();
//			pu.setSystemStatus(StatefulProcessingUnit.SystemStatus.NORMAL);
//			System.out.println("Changing to NORMAL mode, recovering data processing");
		}
	}
	
	private boolean matchSession(int opId, String fileName, String sessionName){
		String[] splits = fileName.split("_");
		
		return (splits[2].equals(sessionName) && splits[1].equals(new Integer(opId).toString()));
	}
}
