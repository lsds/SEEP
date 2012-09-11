package seep.operator;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import seep.Main;
import seep.buffer.Buffer;
import seep.buffer.StateReplayer;
import seep.buffer.TupleReplayer;
import seep.comm.ContentBasedFilter;
import seep.comm.StatefulDynamicLoadBalancer;
import seep.comm.StatelessDynamicLoadBalancer;
import seep.comm.Dispatcher.DispatchPolicy;
import seep.comm.tuples.Seep;
import seep.comm.tuples.Seep.BackupState;
import seep.infrastructure.NodeManager;
import seep.operator.OperatorContext.PlacedOperator;
import seep.utils.CommunicationChannelInformation;
import seep.utils.ExecutionConfiguration;

@SuppressWarnings("serial")
public class OperatorCommonProcessLogic implements Serializable{

	private Operator owner;
	private OperatorContext opContext;
	
	public void setOwner(Operator owner) {
		this.owner = owner;
	}
	public void setOpContext(OperatorContext opContext) {
		this.opContext = opContext;
	}
	
	//map where it is saved the ack received by each downstream
	private Map<Integer, Long> downstreamLastAck = new HashMap<Integer, Long>();
	
	//routing information, operatorCLASS - backupRI
//	private Seep.BackupRI backupRI = null;
	private HashMap<String, Seep.BackupRI> backupRoutingInformation = new HashMap<String, Seep.BackupRI>();
	
	/// \todo{check if i can avoid operations in the data structure when receiving same msg again and again}
	public void storeBackupRI(Seep.BackupRI backupRI){
		//Get operator Class
		String operatorType = backupRI.getOperatorType();
		
System.out.println("#################");
System.out.println("#################");
System.out.println("###### PREV");
if(backupRoutingInformation.containsKey(operatorType)){
System.out.println("TYPE: "+backupRoutingInformation.get(operatorType));
}
else{
System.out.println("NULL");
}
		//Save in the provided map the backupRI for this upstream, if there are replicas then we will have replicated info here...
		backupRoutingInformation.put(operatorType, backupRI);
		
		
System.out.println("###### POST");
if(backupRoutingInformation.containsKey(operatorType)){
System.out.println("OPID: "+backupRoutingInformation.get(operatorType));
}
else{
System.out.println("NULL");
}
System.out.println("#################");
System.out.println("#################");
	}
	
	/**  installRI receives a message with the routing information that this operator must implement 
	 * there are N indexes, and thus, there must be N load balancers. Each of these load balancers must have the same indexes,
	 * this is, the same routing information.
	 * Consequently, I have to locate the load balancers that this operator has right now and change the routing information.
	 * Then, create the new required ones with the new information**/
	
	public synchronized void installRI(Seep.InitRI initRI){
		NodeManager.nLogger.info("-> Installing RI from OP: "+initRI.getOpId());
		//Create new LB with the information received
		ArrayList<Integer> indexes = new ArrayList<Integer>(initRI.getIndexList());
		ArrayList<Integer> keys = new ArrayList<Integer>(initRI.getKeyList());
		StatefulDynamicLoadBalancer sdlb = new StatefulDynamicLoadBalancer(indexes, keys);
		//Assign this load balancer to all the indexes (the actual downstreams) 
		for(Integer index : indexes){
			int opId = opContext.getDownOpIdFromIndex(index);
System.out.println("OP: "+opId+" INSTALLING INDEX: "+indexes);
			((ContentBasedFilter)owner.getDispatcher().getDispatcherFilter()).setNewLoadBalancer(opId, sdlb);
		}
	}
	
	public void sendRoutingInformation(int opId, String operatorType){
		//Get index from opId
		int upstreamIndex = 0;
		for(PlacedOperator op : opContext.upstreams){
			if(op.opID() == opId){
				 upstreamIndex = op.index();
			}
		}
		System.out.println("## NEW UPSTREAM, op: "+opId+" type: "+operatorType);
		//If i dont have backup for that upstream, I am not in charge...
		if(!backupRoutingInformation.containsKey(operatorType)){
			NodeManager.nLogger.info("-> NO routing info to send");
			return;
		}
		NodeManager.nLogger.info("-> Sending backupRI to upstream");
System.out.println("KEY: "+operatorType);
		//Otherwise I pick the backupRI msg from the data structure where i am storing these ones
		Seep.BackupRI backupRI = backupRoutingInformation.get(operatorType);
		//Form the message
		Seep.InitRI.Builder initRI = Seep.InitRI.newBuilder();
		initRI.addAllIndex(backupRI.getIndexList());
		initRI.addAllKey(backupRI.getKeyList());
		initRI.setOpId(owner.getOperatorId());
		Seep.ControlTuple.Builder ct = Seep.ControlTuple.newBuilder();
		ct.setType(Seep.ControlTuple.Type.INIT_RI);
		ct.setInitRI(initRI);
		//Send the message
		owner.getDispatcher().sendUpstream(ct, upstreamIndex);
	}
	
	/*Checkpoint the state that it has received in the corresponding buffer (The buffer of the connection from which it received the state)*/
	/// \todo{there is a double check (unnecesary) of the nature of downStream variable}
	public synchronized void processBackupState(Seep.BackupState ct){
long a = System.currentTimeMillis();
		//Get operator id
		int opId = ct.getOpId();
		//Get buffer for this operator and save the backupState of downstream operator
		CommunicationChannelInformation downStream = opContext.getCCIfromOpId(opId, "d");
		if (downStream instanceof CommunicationChannelInformation) {
			CommunicationChannelInformation connection = (CommunicationChannelInformation) downStream;
//System.out.println("comparing "+connection.reconf_ts+" with: "+ct.getTsE());
			if (connection.reconf_ts <= ct.getTsE()) {
				//(downstreamBuffers.get(opId)).replaceBackupState(ct);
//				System.out.println("replacing backupState");
				opContext.getBuffer(opId).replaceBackupState(ct);
			}
		}
		else{
			/// \todo{for local operators (residing in same machine) provide a tagging interface so that all operators have a structure in downloadType vector<>}
			System.out.println("############ processBackupSTATE NON CCI");
		}
long e = System.currentTimeMillis();
//System.out.println("### Process backup state: "+(e-a));
	}

	@Deprecated
	private void startReplayer( CommunicationChannelInformation oi ) {
		oi.sharedIterator = oi.buffer.iterator();
		//If new model, then send state and this would trigger the replay buffer
		if(Main.valueFor("ftmodel").equals("newModel")){
			StateReplayer replayer = new StateReplayer(oi);
			Thread replayerT = new Thread(replayer);
			NodeManager.nLogger.info("stateReplayer running");
			replayerT.start();
		}
		//If it is upstream backup, then we should replay buffer directly
		else {
			TupleReplayer tReplayer = new TupleReplayer(oi, owner.getDispatcher());
			NodeManager.nLogger.info("tupleReplayer running");
			new Thread(tReplayer).start();
		}
	}
		
	@Deprecated
	public void startReplayer(int opID) {
		CommunicationChannelInformation oi = opContext.getCCIfromOpId(opID, "d");
		startReplayer(oi);
	}

	//Send ACK tuples to the upstream nodes and update TS_ACK
	/// \todo{now this method is syncrhonized, with ACK Tile mechanism we can avoid most of the method, but it is not finished yet}
	public synchronized void processAck(Seep.Ack ct){
		int opId = ct.getOpId();
		long current_ts = ct.getTs();
		int counter = 0;
		long minWithCurrent = current_ts;
		long minWithoutCurrent = Long.MAX_VALUE;
		/// \todo {this code block dependant on FT is a source of problems that must be solved}
		if(!Main.valueFor("ftmodel").equals("twitterStormModel") || opContext.upstreams.size() == 0){
			Buffer buffer = opContext.getBuffer(opId);
			if(buffer != null){
				buffer.trim(current_ts);
			}
		}
		//i.e. if the operator is stateless (we should use a tagging interface). Stateless and NOT first operator
		if ((owner.subclassOperator instanceof StatelessOperator) && !((opContext.upstreams.size()) == 0)) {
			for( Map.Entry<Integer, Long> entry: downstreamLastAck.entrySet()) {
				long ts = entry.getValue();
				if (entry.getKey() != opId) {
					if(ts < minWithCurrent){ 
						minWithCurrent = ts; 
					}
				}
				if(ts < minWithoutCurrent){ 
					minWithoutCurrent = ts; 
				}				
				counter++;
			}
			downstreamLastAck.put(opId, current_ts);
				//FIXME if I remove a downstream (scale down) I should remove from map/clear the map
	/// \todo {scaling down operators introduce a new bunch of possibilities}
			if(downstreamLastAck.size() != opContext.downstreams.size()) {
		//			System.out.println("missing some acks suppressing ACK");
			}
			else if (minWithCurrent==minWithoutCurrent) {
		//			System.out.println("min did not changed, suppress ACK");
			}
			else{
				owner.ack(minWithCurrent);
			}
		}
		//if the operator is stateful, we backpropagate the ACK directly. with batching, we will always propagate unique ACK's
		//else if (owner.subclassOperator instanceof StatefullOperator){
		else{
			owner.ack(current_ts);
		}
		// To indicate that this is the last ack processed by this operator
		owner.setTs_ack(current_ts);
	}
	
	public void splitState(int oldOpId, int newOpId, int key) {
		StateSplitI operatorToSplit = (StateSplitI)owner.getSubclassOperator();
		BackupState oldState = opContext.getBuffer(oldOpId).getBackupState();

		if(!opContext.isManagingStateOf(oldOpId)){
			NodeManager.nLogger.severe("NOT MANAGING STATE?????   ");
		}
		
		BackupState.Builder splitted[] = operatorToSplit.parallelizeState(oldState, key);

		splitted[0].setTsE(oldState.getTsE());
		splitted[1].setTsE(oldState.getTsE());
		splitted[0].setOpId(oldOpId);
		splitted[1].setOpId(newOpId);
		NodeManager.nLogger.severe("-> Replacing backup states for OP: "+oldOpId+" and OP: "+newOpId);
		opContext.getBuffer(oldOpId).replaceBackupState(splitted[0].build());
		opContext.getBuffer(newOpId).replaceBackupState(splitted[1].build());
		//Indicate that we are now managing both these states
		NodeManager.nLogger.severe("-> Registering management of state for OP: "+oldOpId+" and OP: "+newOpId);
		opContext.registerManagedState(oldOpId);
		opContext.registerManagedState(newOpId);
	}
	
	public synchronized void scaleOut(Seep.ScaleOutInfo scaleOutInfo){
		int oldOpId = scaleOutInfo.getOldOpID();
		int newOpId = scaleOutInfo.getNewOpID();
		int newOpIndex = -1;
		for(PlacedOperator op: opContext.downstreams) {
			if (op.opID() == newOpId)
				newOpIndex = op.index();
		}
		//pick the index of the operator to split
		int oldOpIndex = opContext.findDownstream(oldOpId).index();
		
		//if operator is stateful and it can split state
		if(opContext.isDownstreamOperatorStateful(oldOpId) && owner.subclassOperator instanceof StateSplitI){
			NodeManager.nLogger.info("-> Scaling out STATEFUL op");
			scaleOutStatefulOperator(oldOpId, newOpId, oldOpIndex, newOpIndex);
		}
		
		// If operator splitting is stateless...
		else if (!opContext.isDownstreamOperatorStateful(oldOpId)){
			NodeManager.nLogger.info("-> Scaling out STATELESS op");
			configureNewDownstreamStatelessOperatorSplit(oldOpId, newOpId, oldOpIndex, newOpIndex);
		}
	}
	
	public void scaleOutStatefulOperator(int oldOpId, int newOpId, int oldOpIndex, int newOpIndex){
		//All operators receiving the scale-out message have to change their routing information
		int newKey = configureNewDownstreamStatefulOperatorSplit(oldOpId, newOpId, oldOpIndex, newOpIndex);
		//I have configured the new split, check if I am also in charge of splitting the state or not
		if(opContext.isManagingStateOf(oldOpId)){
			NodeManager.nLogger.info("-> Splitting state");
			splitState(oldOpId, newOpId, newKey);
			owner.setOperatorStatus(Operator.OperatorStatus.WAITING_FOR_STATE_ACK);
			//Just one operator needs to send routing information backup, cause downstream is saving this info according to op type.
			NodeManager.nLogger.info("-> Generating and sending RI backup");
//			backupRoutingInformation(oldOpId);
		}
		else{
			NodeManager.nLogger.info("-> NOT in charge of split state");
		}
		
		/**dependent code**/
		if(!Main.valueFor("ftmodel").equals("newModel")){
			owner.setOperatorStatus(Operator.OperatorStatus.WAITING_FOR_STATE_ACK);
			//Just one operator needs to send routing information backup, cause downstream is saving this info according to op type.
			NodeManager.nLogger.info("-> Generating and sending RI backup");
//			backupRoutingInformation(oldOpId);
		}
		/****/
		
		//I always backup the routing info. This leads to replicate messages, but I cant avoid it easily since I can have more than one type of upstream
		backupRoutingInformation(oldOpId);
	}
	
	public void configureNewDownstreamStatelessOperatorSplit(int oldOpID, int newOpId, int oldOpIndex, int newOpIndex){			
		// If this method is called that means downstream is a stateless operator, so we have to specifically change the ANY policy
		if (owner.getDispatcher().getDispatchPolicy() == null){
			// in this case it is needed to change to an ANY policy, by default with windows == 1
			owner.getDispatcher().setDispatchPolicy(DispatchPolicy.ANY, new StatelessDynamicLoadBalancer());
		}
		else if (owner.getDispatcher().getDispatchPolicy() == DispatchPolicy.CONTENT_BASED){
			//Call newSplit, with oldOpId (to identify group) and newOpINDEX to identify replica
			((ContentBasedFilter)owner.getDispatcher().getDispatcherFilter()).newSplit(oldOpID, newOpId, oldOpIndex, newOpIndex);
		}
		else if (owner.getDispatcher().getDispatchPolicy() == DispatchPolicy.ANY){
			//Access the loadBalancer, and call newReplica
			((StatelessDynamicLoadBalancer)owner.getDispatcher().getDispatcherFilter()).newReplica(oldOpIndex, newOpIndex);
		}
	}
			
	public int configureNewDownstreamStatefulOperatorSplit(int oldOpId, int newOpId, int oldOpIndex, int newOpIndex){
		int newKey = -1;
				
		//. stop sending data to op1 remember last data sent
		CommunicationChannelInformation oldConnection = ((CommunicationChannelInformation)opContext.getDownstreamTypeConnection().get(oldOpIndex));
		// necessary to ignore state checkpoints of the old operator before split.
		oldConnection.reconf_ts = oldConnection.last_ts;
				
		//Stop connections to perform the update
		NodeManager.nLogger.info("-> Stopping connections of oldOpId: "+oldOpId+" and newOpId: "+newOpId);
		owner.getDispatcher().stopConnection(oldOpId);
		owner.getDispatcher().stopConnection(newOpId);
				
		newKey = ((ContentBasedFilter)owner.getDispatcher().getDispatcherFilter()).newSplit(oldOpId, newOpId, oldOpIndex, newOpIndex);
		return newKey;
	}
	
	private void backupRoutingInformation(int oldOpId) {
		//Get routing information
		ArrayList<Integer> indexes = ((ContentBasedFilter)owner.getDispatcher().getDispatcherFilter()).getIndexesInformation(oldOpId);
		ArrayList<Integer> keys = ((ContentBasedFilter)owner.getDispatcher().getDispatcherFilter()).getKeysInformation(oldOpId);
System.out.println("BACKUP INDEXES: "+indexes.toString());
System.out.println("BACKUP KEYS: "+keys.toString());
		//Create message
		Seep.BackupRI.Builder backupRI = Seep.BackupRI.newBuilder();
		for(Integer index : indexes){
			backupRI.addIndex(index);
		}
		for(Integer key : keys){
			backupRI.addKey(key);
		}
		//Set the id of this operator, and the type as well
		backupRI.setOpId(owner.getOperatorId());
		backupRI.setOperatorType(owner.getClass().getName());

		Seep.ControlTuple.Builder msg = Seep.ControlTuple.newBuilder();
		msg.setType(Seep.ControlTuple.Type.BACKUP_RI);
		msg.setBackupRI(backupRI);
		//Send message to downstreams (for now, all downstreams) 
		/// \todo{make this more efficient, not sending to all (same mechanism upstreamIndexBackup than downstreamIndexBackup?)}
		for(Integer index : indexes){
			owner.getDispatcher().sendDownstream(msg, index);
		}
	}
	
	/// \todo {refactor or make clearer this method}
	public void replayState(int opId) {
		//Get channel information
		CommunicationChannelInformation cci = opContext.getCCIfromOpId(opId, "d");
		Buffer buffer = cci.buffer;
		Socket controlDownstreamSocket = cci.downstreamSocketC;

		/// \todo {this block of code should not be ft model dependant}
		if(!Main.valueFor("ftmodel").equals("twitterStormModel")){
			//Get a proper init state and just send it
			Seep.ControlTuple.Builder ctB = Seep.ControlTuple.newBuilder();

			Seep.InitState state = null;
			Seep.BackupState bs = buffer.getBackupState();
			//if state is null (upstream backup or new model at the start)
			if (bs != null) {
				Seep.InitState.Builder isB = Seep.InitState.newBuilder();
				//Set opID
				isB.setOpId(owner.getOperatorId());
				//Ts of init state is the newest ts of the checkpointed state
				isB.setTs(bs.getTsE());
/** THIS WHOLE BLOCK OF CODE IS OPERATOR DEPENDANT. THEREFORE THIS MUST BE FIXED **/
/// \fix {block dependant of operator, fix this thing.}
				if(bs.getTcState().getStateId() == 1){
					//This line is specially important, since each message state has a different name in the proto.file
/// \todo {this operator specific line must be avoided, another function buildInitState must be implemented in the operator, so that here we call that function.}
					//System.out.println("WORD COUNTER????");
					//isB.setWcState(bs.getWcState());
					System.out.println("LRB-TC????");
					isB.setTcState(bs.getTcState());
					state = isB.build();
				}
				else if (bs.getBaState().getStateId() == 1){
					System.out.println("LRB-BA????");
					isB.setBaState(bs.getBaState());
					state = isB.build();
				}
				else if (bs.getWcState() != null){
					System.out.println("SMART-CNT????");
					isB.setWcState(bs.getWcState());
					state = isB.build();
				}
				ctB.setInitState(state);
			}
			else {
				NodeManager.nLogger.info("-> Replaying EMPTY State");
			}
			ctB.setType(Seep.ControlTuple.Type.INIT_STATE);
			try{
				NodeManager.nLogger.info("-> INIT_STATE sent to OP: "+opId);
				//If there is a state, send it ALWAYS
				if(state != null){
					synchronized(controlDownstreamSocket){
						(ctB.build()).writeDelimitedTo(controlDownstreamSocket.getOutputStream());
					}
				}
				//If there is no state, send the empty state just if the ft model is not new model. In new model someone else is sending a state.
				/// \todo{is this block of code ever used? is correct that state is null?}
				else{
					if(!Main.valueFor("ftmodel").equals("newModel")){
						synchronized(controlDownstreamSocket){
							(ctB.build()).writeDelimitedTo(controlDownstreamSocket.getOutputStream());
						}
					}
				}
			}
			catch(IOException io){
				System.out.println("REPLAYER: Error while trying to send the INIT_STATE msg: "+io.getMessage());
				io.printStackTrace();
			}
		}
	}
	
	public void replayTuples(int opId) {
long a = System.currentTimeMillis();
		CommunicationChannelInformation cci = opContext.getCCIfromOpId(opId, "d");
		Iterator<Seep.EventBatch> sharedIterator = cci.buffer.iterator();
		Socket socket = cci.downstreamSocketD;
		int bufferSize = cci.buffer.size();
		int controlThreshold = (int)(bufferSize)/10;
System.out.println("TO REPLAY: "+bufferSize+ "tuples");
		int replayed = 0;
		while(sharedIterator.hasNext()) {
//			System.out.println("I DO HAVE TUPLES");
			try{
				Seep.EventBatch dt = sharedIterator.next();
//				System.out.println("is dt null?");
//				if(dt == null) System.out.println("yes");
//				else System.out.println("no");
				
				synchronized(socket){
//					System.out.print("*");
					OutputStream o = socket.getOutputStream();
					if(o == null) System.out.println("o is NULL");
//					System.out.print("1");
					dt.writeDelimitedTo(o);
//					System.out.print("/");
				}
				replayed++;
				//Criteria for knowing how to transfer control back to incomingdatahandler
				/// \test {test this functionality. is this necessary?}
				if((bufferSize-replayed) <= (controlThreshold+1)){
					break;
				}
			}
			catch(IOException io){
				System.out.println("ERROR in replayer when replaying info: "+io.getMessage());
				io.printStackTrace();
			}
		}
		//Restablish communication. Set variables and sharedIterator with the current iteration state.
		NodeManager.nLogger.info("-> Recovering connections");
		cci.replay.set(true);
		cci.stop.set(false);
		cci.sharedIterator = sharedIterator;
		owner.getDispatcher().startIncomingData();
long b = System.currentTimeMillis() - a;
System.out.println("replayTuples: "+b);
	}
	
	//Initial compute of upstreamBackupindex. This is useful for initial instantiations (not for splits, because in splits, upstreamIdx comes in the INIT_STATE)
	public void configureUpstreamIndex(){
		int ownInfo = StatefulDynamicLoadBalancer.customHash(owner.getOperatorId());
		int upstreamSize = opContext.upstreams.size();
		//source obviously cant compute this value
		if(upstreamSize == 0){
			return;
		}
		int upIndex = ownInfo%upstreamSize;
		upIndex = (upIndex < 0) ? upIndex*-1 : upIndex;
		
		//Update my information
		owner.setBackupUpstreamIndex(upIndex);
	}
	
	//This method simply backups its state. It is useful for making the upstream know that is in charge of managing this state.
	public void sendInitialStateBackup(){
		//Without waiting for the counter, we backup the state right now, (in case operator is stateful)
		if(owner.subclassOperator instanceof StatefullOperator){
System.out.println("OP: "+owner.getOperatorId()+" INITIAL BACKUP!!!!!!#############");
			((StatefullOperator)owner.subclassOperator).generateBackupState();
		}
	}
	
	
	public void reconfigureUpstreamBackupIndex(){
		NodeManager.nLogger.info("-> Reconfiguring upstream backup index");
		//First I compute my own info, which is the hash of my id.
		/** There is a reason to hash the opId. Imagine upSize=2 and opId of downstream 2, 4, 6, 8... So better to mix the space*/
		int ownInfo = StatefulDynamicLoadBalancer.customHash(owner.getOperatorId());
		int upstreamSize = opContext.upstreams.size();
		int upIndex = ownInfo%upstreamSize;
		//Since ownInfo (hashed) may be negative, this enforces the final value is always positive.
		upIndex = (upIndex < 0) ? upIndex*-1 : upIndex;
		//int upId = opContext.getUpOpIdFromIndex(upIndex);
System.out.println("backupUpstreamIndex: "+owner.getBackupUpstreamIndex()+ "upIndex: "+upIndex);
		int backupUpstreamIndex = owner.getBackupUpstreamIndex();
		//If the upstream is different from previous sent, then additional management is necessary
		//In particular, invalidate the management of my state in the previous operator in charge to do so...
		//... and notify the new operator in charge of my OPID
		//UPDATE!! AND send STATE in case this operator is backuping state
		if(upIndex != backupUpstreamIndex){
			NodeManager.nLogger.info("-> Upstream backup has changed...");
			//Invalidate old Upstream state
			Seep.ControlTuple.Builder ct = buildInvalidateMsg(backupUpstreamIndex);
			owner.getDispatcher().sendUpstream(ct, backupUpstreamIndex);
		
			//Update my information
			owner.setBackupUpstreamIndex(upIndex);
			
			//Without waiting for the counter, we backup the state right now, (in case operator is stateful)
			if(owner.subclassOperator instanceof StatefullOperator){
				NodeManager.nLogger.info("-> sending BACKUP_STATE to the new manager of my state");
				((StatefullOperator)owner.subclassOperator).generateBackupState();
			}
		}
		//Else, all remains the same
	}
	
	public Seep.ControlTuple.Builder buildInvalidateMsg(int backupUpstreamIndex) {
		Seep.ControlTuple.Builder ct = Seep.ControlTuple.newBuilder();
		ct.setType(Seep.ControlTuple.Type.INVALIDATE_STATE);
		Seep.InvalidateState.Builder is = Seep.InvalidateState.newBuilder();
		is.setOpId(owner.getOperatorId());
		ct.setInvalidateState(is.build());
		//Send invalidation message to old upstream
		NodeManager.nLogger.info("-> sending INVALIDATE_STATE to OP "+opContext.getUpOpIdFromIndex(backupUpstreamIndex));
		return ct;
	}
	
	public void printRoutingInfo() {
//		if(owner.getDispatcher().getDispatcherFilter() instanceof StatelessDynamicLoadBalancer){ 
//			((StatelessDynamicLoadBalancer)owner.getDispatcher().getDispatcherFilter()).print();
//		}
		if ((owner.getDispatcher().getDispatcherFilter() instanceof ContentBasedFilter)){
			((ContentBasedFilter)owner.getDispatcher().getDispatcherFilter()).print();
		}
	}
}

/*
//CASE 1, there are more than one type of stateful downstream conn and they can scale out
if (owner.getDispatcher().getDispatchPolicy().equals(DispatchPolicy.CONTENT_BASED)){
System.out.println("MORE THAN ONE DOWN TYPE");
System.out.println("Old Op ID: "+oldOpID+" index: "+oldOpIndex);
System.out.println("New Op ID: "+newOpID+" index: "+newOpIndex);
	//We remap the routeInfo map to cope with the new split
	((ContentBasedFilter)owner.getDispatcher().getDispatcherFilter()).reconfigureRouteInfo(oldOpIndex, newOpIndex);
	//We get the downstream that has scaled out and configure the dispatching info for it
	newKey = ((ContentBasedFilter)owner.getDispatcher().getDispatcherFilter()).updateConsistentHashingStructures(oldOpIndex, newOpIndex);
}
//CASE 2, There is only one type of stateful downstream conn and it can scale out
else{
System.out.println("JUST ONE DOWN TYPE");
	//There is not a dispatch filter, so access directly to structure
	newKey = ((ContentBasedFilter)owner.getDispatcher().getDispatcherFilter()).getStructure().updateDataStructures(oldOpIndex, newOpIndex);
}
return newKey;
*/
//}



///// \todo {consider refactor the two methods inside here...}
//public void scaleOut(int oldOpID, int newOpID, boolean stateful) {
//	// !splitState == Downstream operator is stateless
//	if(!stateful){
////		configureNewDownstreamStatelessOperatorSplit(oldOpID, newOpID);
//	}
//	// splitState == downstream operator is statefull
//	if(stateful){
//		int newKey = configureNewDownstreamStatefulOperatorSplit(oldOpId, newOpId);
//		//I have configured the new split, check if I am also in charge of splitting the state or not
//		if(opContext.isManagingStateOf(oldOpID)){
//			NodeManager.nLogger.info("-> Splitting state");
//			splitState(oldOpID, newOpID, newKey);
//			owner.setOperatorStatus(Operator.OperatorStatus.WAITING_FOR_STATE_ACK);
//		}
//		else{
//			NodeManager.nLogger.info("-> NOT in charge of split state");
//		}
//		//Furthermore, we have to backup the routing state to the downstreams
////System.out.println("calling to backup ri");
//		//When downstream is stateful, in this operator it is generated RI, meaning that we have to backup this information
//		NodeManager.nLogger.info("-> Generating and sending RI backup");
//		backupRoutingInformation(oldOpID);
//System.out.println("RI bakcup DONE");
//	}
//}

//public synchronized void installRI(Seep.InitRI initRI){
//NodeManager.nLogger.info("-> Installing RI from OP: "+initRI.getOpId());
////Get list of indexes
//List<Integer> indexes = initRI.getIndexList();
////Build list by transforming indexes into opIds.
//List<Integer> downstreamOpId = new ArrayList<Integer>();
//for(Integer index : indexes){
//	downstreamOpId.add(opContext.getDownOpIdFromIndex(index));
//}
//System.out.println("INSTALL RI for LB of: "+downstreamOpId);
////Search among the indexes that one with a Load balancer assigned
//System.out.println("##### INDEXES TO INSTALL :"+indexes);
//for(Integer index : indexes){
//	int opId = opContext.getDownOpIdFromIndex(index);
//System.out.println("INSTALL-RI: hasLBForOperator -> "+opId);
//	//Here, this operator should have a load balancer for one of its downstreams (the main operator)
//	if(((ContentBasedFilter)owner.getDispatcher().getDispatcherFilter()).hasLBForOperator(opId)){
//System.out.println("####### YES");
//		//Once we are sure that our own operator has a load balancer assigned, we update the routing information of this load balancer with the received info
//		((ContentBasedFilter)owner.getDispatcher().getDispatcherFilter()).setIndexesInformation(indexes, initRI.getOpId());
//		((ContentBasedFilter)owner.getDispatcher().getDispatcherFilter()).setKeysInformation(initRI.getKeyList(), initRI.getOpId());
//		
//		/// \test {this methos may be unnecessary}
//		//And finally, we assign the load Balancer of opId to the rest of downstreams, (so the replicas of the main operator downstream)
//		((ContentBasedFilter)owner.getDispatcher().getDispatcherFilter()).configureLoadBalancer(opId, downstreamOpId);
//	}
//	else{
//System.out.println("####### NO!!!");
//System.exit(0);
//	}
//}
////System.out.println("## DOUBLE CHECK, PER LB< ITS INDEXES");
////for(Integer index : indexes){
////int opId = opContext.getDownOpIdFromIndex(index);
////System.out.println("##### INDEXES INSTALLED for OP: "+opId+" -> "+((ContentBasedFilter)owner.getDispatcher().getDispatcherFilter()).getIndexesInformation(opId));
////}
//}
