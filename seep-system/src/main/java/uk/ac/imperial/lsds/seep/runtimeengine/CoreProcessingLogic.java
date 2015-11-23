/*******************************************************************************
 * Copyright (c) 2013 Imperial College London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial design and implementation
 *     Martin Rouaux - Support for scale-in of operators
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.runtimeengine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.buffer.IBuffer;
import uk.ac.imperial.lsds.seep.comm.routing.Router;
import uk.ac.imperial.lsds.seep.comm.serialization.ControlTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.Ack;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.BackupNodeState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.BackupOperatorState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.BackupRI;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.FailureCtrl;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.InitNodeState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.InitOperatorState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.InitRI;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.InvalidateState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.RawData;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.ReconfigureConnection;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.Resume;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.ScaleOutInfo;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.StateAck;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.StateChunk;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;
import uk.ac.imperial.lsds.seep.operator.OperatorContext.PlacedOperator;
import uk.ac.imperial.lsds.seep.processingunit.IProcessingUnit;
import uk.ac.imperial.lsds.seep.processingunit.PUContext;
import uk.ac.imperial.lsds.seep.processingunit.StatefulProcessingUnit;
import uk.ac.imperial.lsds.seep.reliable.BackupHandler;
import uk.ac.imperial.lsds.seep.reliable.MemoryChunk;
import uk.ac.imperial.lsds.seep.reliable.StreamerWorker;
import uk.ac.imperial.lsds.seep.state.Partitionable;
import uk.ac.imperial.lsds.seep.state.StateWrapper;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.MapSerializer;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.ScaleInInfo;
import uk.ac.imperial.lsds.seep.operator.EndPoint;
import uk.ac.imperial.lsds.seep.processingunit.StatelessProcessingUnit;


public class CoreProcessingLogic implements Serializable{
	
	final private Logger LOG = LoggerFactory.getLogger(CoreProcessingLogic.class);

	private static final long serialVersionUID = 1L;
	
	private CoreRE owner;
	private IProcessingUnit pu;
	private PUContext puCtx;
	
	//map where it is saved the ack received by each downstream
	private Map<Integer, TimestampTracker> downstreamLastAck = new HashMap<Integer, TimestampTracker>();
	
	//routing information, operatorCLASS - backupRI
//	private Seep.BackupRI backupRI = null;
	private HashMap<String, BackupRI> backupRoutingInformation = new HashMap<String, BackupRI>();
	
	private Kryo k;
	
	public void initializeSerialization(){
		k = new Kryo();
		k.register(ControlTuple.class);
		k.register(MemoryChunk.class);
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
	
	/// \todo{check if i can avoid operations in the data structure when receiving same msg again and again}
	public void storeBackupRI(BackupRI backupRI){
		//Get operator Class
		String operatorType = backupRI.getOperatorType();
		//Save in the provided map the backupRI for this upstream, if there are replicas then we will have replicated info here...
		backupRoutingInformation.put(operatorType, backupRI);
	}
	
	public synchronized void installRI(InitRI initRI){
		LOG.info("-> Installing RI from Node: {}", initRI.getNodeId());
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
		//If i dont have backup for that upstream, I am not in charge...
		if(!backupRoutingInformation.containsKey(operatorType)){
			LOG.debug("-> NO routing info to send");
			return;
		}
		LOG.debug("-> Sending backupRI to upstream");
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
			LOG.warn("-> Received state generated after the beginning of the reconfigure process");
		}
	}

	public synchronized void processAck(Ack ct){
		int opId = ct.getOpId();
		long ack_ts = ct.getTs();
		processAck(opId, ack_ts);
	}
	
	private synchronized void processAck(int opId, long ack_ts){
		// Trim local buffer
		IBuffer buffer = puCtx.getBuffer(opId);
		TimestampTracker oldest = buffer.trim(ack_ts);
		
		// Check whether this operator is responsible to control when to backpropagate acks
		if(pu.getOperator().getOperatorCode() instanceof StatelessOperator || !((StatefulProcessingUnit)pu).isCheckpointEnabled()){
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
	
	public void processFailureCtrl(FailureCtrl fctrl, int fctrlSenderOpId)
	{
		//TODO: Effectively this implements the defaultFailureCtrlHandler.
		//Presuming it won't block then should be ok to just do it in this
		//thread (which should be the associated downstream's controlHandlerWorkerThread)
		//1) Update the dispatcher's failure ctrl
		//2) Trim the node out buf and timers
		//3) Trim the stuff waiting in the dispatcher's queues
		//4) Trim the stuff waiting in the buffer for this op (and all ops?)
		//5) TODO: Trim the input queues/buffers? 
		//6) Notify variuos parties
		//6) TODO: Forward the ack upstream if different.
		if (pu != null && pu.getDispatcher() != null)
		{
			if (!pu.getOperator().getOpContext().isSink())
			{
				//LOG.trace("Processing down failure ctrl received from "+fctrlSenderOpId+",fctrl.lw="+fctrl.lw()+",fctrl.acks="+fctrl.acks()); 
				FailureCtrl updatedFctrl = pu.getDispatcher().handleFailureCtrl(fctrl, fctrlSenderOpId);
				
				//Now trim the input data structures + trigger the fctrl writer to write 
				//a new fctrl.
				if (!pu.getOperator().getOpContext().isSource() && !Boolean.parseBoolean(GLOBALS.valueFor("perodicFctrlsOnly")))
				{
					//owner.writeFailureCtrls(pu.getOperator().getOpContext().getListOfUpstreamIndexes(), updatedFctrl);
				}
			}
			else
			{
				if (!fctrl.alives().isEmpty()) { throw new RuntimeException("Logic error, upstream fctrl alives non-empty."); }
				//LOG.trace("Processing up failure ctrl received from "+fctrlSenderOpId+",fctrl.lw="+fctrl.lw()+",fctrl.acks="+fctrl.acks()); 
				//Probably a replicated sink, need to handle an upstream failure ctrl from another sink.
				pu.getDispatcher().handleUpstreamFailureCtrl(fctrl, fctrlSenderOpId);

			}
		}
	}
	
	public void splitState(int oldOpId, int newOpId, int key) {
		
		BackupOperatorState backupOperatorState = puCtx.getBuffer(oldOpId).getBackupState();
		
		StateWrapper stateToSplit = backupOperatorState.getState();
		int stateCheckpointInterval = stateToSplit.getCheckpointInterval();
		String stateTag = stateToSplit.getStateTag();
		TimestampTracker data_ts = stateToSplit.getData_ts();
		StateWrapper splitted[] = null;
		if(stateToSplit instanceof Partitionable){
			splitted = ((Partitionable)stateToSplit).splitState(stateToSplit, key);
		}
		else{
			LOG.warn("-> this state is not partitionable");
			// Crash the system at this point
			System.exit(0);
		}
		// Create the two states, and fill the necessary info, as ts, etc.
		//Set the old partition
		StateWrapper oldStatePartition = splitted[0];
		oldStatePartition.setOwnerId(oldOpId);
		oldStatePartition.setCheckpointInterval(stateCheckpointInterval);
		oldStatePartition.setStateTag(stateTag);
		oldStatePartition.setData_ts(data_ts);
		// Set the new partition
		StateWrapper newStatePartition = splitted[1];
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

		LOG.error("-> Replacing backup states for OP: "+oldOpId+" and OP: "+newOpId);
		
		// Replace state in the old and new operators
		puCtx.getBuffer(oldOpId).replaceBackupOperatorState(oldPartition);
		puCtx.getBuffer(newOpId).replaceBackupOperatorState(newPartition);
		
		//Indicate that we are now managing both these states
		LOG.error("-> Registering management of state for OP: "+oldOpId+" and OP: "+newOpId);
		// We state we manage the state of the old and new ops replicas
		pu.registerManagedState(oldOpId);
		pu.registerManagedState(newOpId);
		
		System.out.println("% After SPLIT, oldOpId: "+puCtx.getBuffer(oldOpId).getBackupState().getOpId()+" newOpId: "+puCtx.getBuffer(newOpId).getBackupState().getOpId());
	}
	
	public synchronized int[] manageDownstreamDistributedScaleOut(int oldOpId, int newOpId, int oldOpIndex, int newOpIndex){
		pu.stopConnection(oldOpId);
		pu.stopConnection(newOpId);
		// In case it's a stateful operator, it will return the new key that has partitioned the key space
		return pu.getOperator().getRouter().newOperatorPartition(oldOpId, newOpId, oldOpIndex, newOpIndex);
	}

	
	public synchronized void scaleOut(ScaleOutInfo scaleOutInfo, int newOpIndex, int oldOpIndex){
		int oldOpId = scaleOutInfo.getOldOpId();
		int newOpId = scaleOutInfo.getNewOpId();
		boolean isStatefulScaleOut = scaleOutInfo.isStatefulScaleOut();
		//If scaling operator is stateful
		if(isStatefulScaleOut){
			LOG.info("-> Scaling out STATEFUL op");
			scaleOutStatefulOperator(oldOpId, newOpId, oldOpIndex, newOpIndex);
		}
		// If operator splitting is stateless...
		else{
			LOG.info("-> Scaling out STATELESS op");
			pu.getOperator().getRouter().newOperatorPartition(oldOpId, newOpId, oldOpIndex, newOpIndex);
		}
	}
    
    public synchronized void scaleIn(ScaleInInfo scaleInInfo, int victimOperatorIndex) {
        int victimOperatorId = scaleInInfo.getVictimOperatorId();
        boolean isStatefulScaleIn = scaleInInfo.isStatefulScaleOut();
        
        // If scaling operator is stateful
		if (isStatefulScaleIn){
			LOG.info("-> Scaling in STATEFUL op");
            // TODO: this is not yet supported. State needs to be collapsed and 
            // re-partiotioned amonst surviving instances. This is a complex task.
		} else {
			LOG.info("-> Scaling in STATELESS op");
            LOG.info("Removing connection from [{}] to [{}]", 
                    pu.getOperator().getOperatorId(), victimOperatorId);
            
            // Remove downstream connections
            pu.getOperator().getOpContext().removeDownstream(victimOperatorId);
            
            if (pu instanceof StatelessProcessingUnit) {
                LOG.info("Removing end point at index " + victimOperatorIndex);
                
                ((StatelessProcessingUnit)pu).getPUContext()
                        .getDownstreamTypeConnection()
                        .remove(victimOperatorIndex);
            }
            
            // Remove partition at the router level
			pu.getOperator().getRouter().collapseOperatorPartition(victimOperatorId, victimOperatorIndex);
        }
    }
	
	public void scaleOutStatefulOperator(int oldOpId, int newOpId, int oldOpIndex, int newOpIndex){
		//All operators receiving the scale-out message have to change their routing information
		int newKey = configureNewDownstreamStatefulOperatorPartition(oldOpId, newOpId, oldOpIndex, newOpIndex);
		//I have configured the new split, check if I am also in charge of splitting the state or not
		if(pu.isManagingStateOf(oldOpId)){
			LOG.debug("-> Splitting state");
			splitState(oldOpId, newOpId, newKey);
			pu.setSystemStatus(StatefulProcessingUnit.SystemStatus.WAITING_FOR_STATE_ACK);
			//Just one operator needs to send routing information backup, cause downstream is saving this info according to op type.
			LOG.debug("-> Generating and sending RI backup");
		}
		else{
			LOG.debug("-> NOT in charge of split state");
		}
		
		pu.setSystemStatus(StatefulProcessingUnit.SystemStatus.WAITING_FOR_STATE_ACK);
		//Just one operator needs to send routing information backup, cause downstream is saving this info according to op type.
		LOG.debug("-> Generating and sending RI backup");
		
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
		LOG.debug("-> Stopping connections of oldOpId: {}and newOpId: {}", oldOpId, newOpId);
		pu.stopConnection(oldOpId);
		pu.stopConnection(newOpId);

		int bounds[] = pu.getOperator().getRouter().newOperatorPartition(oldOpId, newOpId, oldOpIndex, newOpIndex);
		newKey = (bounds[1]-bounds[0])/2;
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
		IBuffer buffer = cci.getBuffer();
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
		LOG.debug("Changing to INITIALISING STATE, stopping all incoming comm");
		pu.setSystemStatus(StatefulProcessingUnit.SystemStatus.INITIALISING_STATE);
		//Give state to processing unit for it to manage it
		((StatefulProcessingUnit)pu).installState(ct);
		//Once the state has been installed, recover dataProcessingChannel
		
		//Send a msg to ask for the rest of information. (tuple replaying)
		LOG.info("-> Sending STATE_ACK");
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
			LOG.debug("NODE: {} INITIAL BACKUP", owner.getNodeDescr().getNodeId());
			if(GLOBALS.valueFor("checkpointMode").equals("large-state")){
				System.out.println("skip initial state backup");
			}
			else if(GLOBALS.valueFor("checkpointMode").equals("light-state")){
				((StatefulProcessingUnit)pu).checkpointAndBackupState();
			}
			else{
				System.out.println("skip initial state backup");
			}
		}
	}
	
	//public void directReplayStateFailure(ReplayStateInfo rsi, BackupHandler bh, File folder){
	public void directReplayStateFailure(int opId, BackupHandler bh){
		File folder = new File("backup/");
		String lastSessionName = bh.getLastBackupSessionName(opId);
		List<File> filesToStream = new ArrayList<File>();
		SynchronousCommunicationChannel cci = puCtx.getCCIfromOpId(opId, "d");
		Socket controlSocket = cci.getDownstreamControlSocket();
		int totalNumberChunks = 0;
		int keeperOpId = pu.getOperator().getOperatorId(); // myself
		// Read folder and filter out files to send through the network
		try {
			LOG.debug("-> Request to stream to a single node");
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
			
			/** end test2 **/
			
			long timeread = 0;
			long timewrite = 0;

			for(File chunk : filesToStream){
				System.out.println("STREAM CHUNK ("+keeperOpId+"): "+chunk.getName());
				Input i = new Input(new FileInputStream(chunk));
//				Input i = new Input(new FileInputStream(filesToStream.get(ig)));
				long a = System.currentTimeMillis();
				ControlTuple ct = k.readObject(i, ControlTuple.class);
				long b = System.currentTimeMillis();
				ct.getStateChunk().setTotalChunks(filesToStream.size());
				ct.getStateChunk().setKeeperOpId(keeperOpId);
//				ct.getStateChunk().setTotalChunks(fakechunksnumber);
				k.writeObject(output, ct);
				output.flush();
				long c = System.currentTimeMillis();
				i.close();
				//System.out.println("CT: "+ct.toString());
				timeread = timeread + (b-a);
				timewrite = timewrite + (c-b);
			}
			// Empty state chunk to indicate end of stream
			ControlTuple endOfStream = new ControlTuple().makeStateChunk(opId, keeperOpId, 0, 0, null, 0);
			k.writeObject(output, endOfStream);
			output.flush();
			
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
	
	//public void directReplayStateScaleOut(ReplayStateInfo rsi, BackupHandler bh, File folder){
	public void directReplayStateScaleOut(int oldOpId, int newOpId, BackupHandler bh){
		// DEBUG VAR
		int _np = 0;
		int _op = 0;
		// END DEBUG VAR
		File folder = new File("backup/");
		String lastSessionName = bh.getLastBackupSessionName(oldOpId);
		int totalNumberChunks = 0;
		int keeperOpId = pu.getOperator().getOperatorId();
		
		System.out.println("Scaling out: "+oldOpId+" to new OP: "+newOpId);
		if(puCtx.getCCIfromOpId(oldOpId, "d") == null){
			System.out.println("No CCI for opid: "+oldOpId);
		}
		
//		// not valid as these sockets might not be in the info of this node
		Socket oldS = puCtx.getCCIfromOpId(oldOpId, "d").getDownstreamControlSocket();
		Socket newS = puCtx.getCCIfromOpId(newOpId, "d").getDownstreamControlSocket();
		
		// rather build new sockets with the IP (from star topology), the control socket and the op id
//		InetAddress oldIp = puCtx.getDCCfromOpIdInStarTopology(oldOpId).getIp();
//		InetAddress newIp = puCtx.getDCCfromOpIdInStarTopology(newOpId).getIp();
//		
		List<File> filesToStream = new ArrayList<File>();
		
		try{
//			Socket oldS = new Socket(oldIp, Main.CONTROL_SOCKET+oldOpId);
//			Socket newS = new Socket(newIp, Main.CONTROL_SOCKET+newOpId);
			
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
System.out.println("there are: "+filesToStream.size()+" to stream");

//			// There is a fixed size per chunk, so there is an upper bound size per partition. Let's then
//			// make dynamically-sized chunks.
//			// Every two file chunks, we send the batched state
			Input i = null;
			ArrayList<Object> oldPartition = new ArrayList<Object>();
			ArrayList<Object> newPartition = new ArrayList<Object>();
			int numberBatchChunks = 2;
			int currentNumberBatch = 0;
			
			long readFromDiskTime = 0;
			long splittingTime = 0;
			long writeOld = 0;
			long writeNew = 0;
			long c = 0;
			long d = 0;
			ArrayBlockingQueue<Object> oldToStream = new ArrayBlockingQueue<Object>(4);
			ArrayBlockingQueue<Object> newToStream = new ArrayBlockingQueue<Object>(4);
			StreamerWorker s1 = new StreamerWorker(oldS, oldToStream, oldOpId, keeperOpId, 0, 0);
			StreamerWorker s2 = new StreamerWorker(newS, newToStream, newOpId, keeperOpId, 0, 0);
			Thread t1 = new Thread(s1);
			Thread t2 = new Thread(s2);
			t1.start();
			t2.start();
			
			for(File chunk : filesToStream){
				System.out.println("Splitting file: "+chunk.getName());
				currentNumberBatch++;
				i = new Input(new FileInputStream(chunk));
				long a = System.currentTimeMillis();
				ControlTuple ct = k.readObject(i, ControlTuple.class);
				long b = System.currentTimeMillis();
				readFromDiskTime += (b-a);
				MemoryChunk mc = ct.getStateChunk().getMemoryChunk();
				int key = ct.getStateChunk().getSplittingKey(); // read it every time? ...
				if(mc == null){
					System.out.println("mc is null");
					System.exit(0);
				}
				if(mc.chunk == null){
					System.out.println("mc.chunk is null");
					System.exit(0);
				}
				if(mc.chunk.get(0) == null){
					System.out.println("mc.chunk.get(0) is null");
					System.out.println("total size: "+mc.chunk.size());
					System.exit(0);
				}
				Object sample = mc.chunk.get(0);
				// agh... java...
				///\todo{i may bring this info in memoryChunk so that it is not necessary to do that erro-prone sample above...}
				if(sample instanceof Integer){
					c = System.currentTimeMillis();
					for(int j = 0; j < mc.chunk.size(); j++){
						Integer k = (Integer)mc.chunk.get(j);
						if(Router.customHash(k) > key){
							newPartition.add(k);
							j++;
							newPartition.add(mc.chunk.get(j));
						}
						else{
							oldPartition.add(k);
							j++;
							oldPartition.add(mc.chunk.get(j));
						}
					}
					d = System.currentTimeMillis();
				}
				else if(sample instanceof String){
					c = System.currentTimeMillis();
					for(int j = 0; j < mc.chunk.size(); j++){
						String k = (String)mc.chunk.get(j);
						if(Router.customHash(k) > key){
							_np++;
							newPartition.add(k);
							j++;
							newPartition.add(mc.chunk.get(j));
						}
						else{
							_op++;
							oldPartition.add(k);
							j++;
							oldPartition.add(mc.chunk.get(j));
						}
					}
					d = System.currentTimeMillis();
				}
				splittingTime += (d-c);
				if(currentNumberBatch == numberBatchChunks){
					currentNumberBatch = 0;
					oldToStream.put(new ArrayList<Object>(oldPartition));
					newToStream.put(new ArrayList<Object>(newPartition));
					oldPartition.clear();
					newPartition.clear();
				}
				i.close();
			}
			//make sure they die
			oldToStream.put(new ArrayList<Object>());
			newToStream.put(new ArrayList<Object>());
			t1.join();
			t2.join();
			
			// Indicate end of stream to both operators
			ControlTuple endOfStream = new ControlTuple().makeStateChunk(pu.getOperator().getOperatorId(), keeperOpId, 0, 0, null, 0);

			k.writeObject(oldO, endOfStream);
			oldO.flush();
			k.writeObject(newO, endOfStream);
			newO.flush();
			System.out.println("Time read from disk: "+readFromDiskTime);
			System.out.println("Splitting time: "+splittingTime);
			System.out.println("time to write to old: "+writeOld);
			System.out.println("time to write to new: "+writeNew);
			LOG.debug("Finished streaming state");
		}
		catch(IOException io){	
			io.printStackTrace();
		}

		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean matchSession(int opId, String fileName, String sessionName){
		String[] splits = fileName.split("_");
		return (splits[2].equals(sessionName) && splits[1].equals(new Integer(opId).toString()));
	}
	
	// Structure and method to keep tracking of merging state
	private Set<Integer> activeOpStreaming = new HashSet<Integer>();
	long mergeTotal = 0;
	public void handleNewChunk(StateChunk stateChunk){
		// First we check who is sending this chunk
		int opId = stateChunk.getKeeperOpId();
		System.out.println("STATE CHUNK FROM : "+opId);
		// If not in state merging state
		if(!pu.getSystemStatus().equals(StatefulProcessingUnit.SystemStatus.MERGING_STATE)){
			// change to merging state
			pu.setSystemStatus(StatefulProcessingUnit.SystemStatus.MERGING_STATE);
			((StatefulProcessingUnit)pu).resetState();
		}
		// If null means this operator has finished streaming
		if(stateChunk.getMemoryChunk() == null){
			activeOpStreaming.remove(opId);
			LOG.debug("OP: {} has finished streaming", opId);
			if(activeOpStreaming.size() == 0){
				// finished merging state
				LOG.debug("Finished merging streaming state");
				System.out.println("TOTAL MERGING TIME: "+mergeTotal);
				((StatefulProcessingUnit)pu).mergeChunkToState(null);
				ControlTuple rb = new ControlTuple().makeStateAck(owner.getNodeDescr().getNodeId(), pu.getOperator().getOperatorId());
				owner.getControlDispatcher().sendAllUpstreams(rb);
				// No longer merging state
				pu.setSystemStatus(StatefulProcessingUnit.SystemStatus.NORMAL);
			}
		}
		// an active operator sends us a chunk
		else{
			// New chunk to merge. We state this op is actively streaming
			activeOpStreaming.add(opId);
			LOG.debug("New OP streaming: {}", opId);		
//			// And we call the correct function to merge the state
			long a = System.currentTimeMillis();
			((StatefulProcessingUnit)pu).mergeChunkToState(stateChunk);
			long b = System.currentTimeMillis();
			mergeTotal += (b-a);
		}
	}

	public void propagateNewKeys(int[] bounds, int oldOpIndex, int newOpIndex) {
		int splittingKey = (int)(bounds[1]-bounds[0])/2;
		ControlTuple boundsForOldOp = new ControlTuple().makeKeyBounds(bounds[0], splittingKey);
		ControlTuple boundsForNewOp = new ControlTuple().makeKeyBounds(splittingKey+1, bounds[1]);
		owner.getControlDispatcher().sendDownstream(boundsForOldOp, oldOpIndex);
		owner.getControlDispatcher().sendDownstream(boundsForNewOp, newOpIndex);
	}
}
