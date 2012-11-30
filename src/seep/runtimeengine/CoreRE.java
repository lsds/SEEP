package seep.runtimeengine;

import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import seep.Main;
import seep.comm.ControlHandler;
import seep.comm.Dispatcher;
import seep.comm.IncomingDataHandler;
import seep.comm.routing.Router;
import seep.comm.routing.StatelessRoutingImpl;
import seep.comm.serialization.ControlTuple;
import seep.comm.serialization.DataTuple;
import seep.comm.serialization.controlhelpers.BackupState;
import seep.comm.serialization.controlhelpers.InitState;
import seep.comm.serialization.controlhelpers.ReconfigureConnection;
import seep.comm.serialization.controlhelpers.Resume;
import seep.comm.serialization.controlhelpers.StateI;
import seep.infrastructure.Node;
import seep.infrastructure.NodeManager;
import seep.infrastructure.OperatorInitializationException;
import seep.infrastructure.OperatorInstantiationException;
import seep.infrastructure.WorkerNodeDescription;
import seep.operator.Operator;
import seep.operator.OperatorStaticInformation;
import seep.operator.QuerySpecificationI;
import seep.operator.StateSplitI;
import seep.operator.StatefulOperator;
import seep.processingunit.PUContext;
import seep.processingunit.ProcessingUnit;

/**
* Operator. This is the class that must inherit any subclass (the developer must inherit this class). It is the basis for building an operator
*/

public class CoreRE {

	private WorkerNodeDescription nodeDescr = null;
	private ProcessingUnit processingUnit = null;
	private PUContext puCtx = null;
	private boolean coreReady = false;
	
	public int ackCounter = 0;
	
//	private int operatorId;
	private int backupUpstreamIndex = -1;

//	private RuntimeContext opContext;
	private CoreProcessingLogic coreProcessLogic;
	private Router router;

	private InputQueue inputQueue;
	private DataConsumer dataConsumer;
	private Thread dConsumerH = null;
	private Dispatcher dispatcher;
	private OutputQueue outputQueue;
	
	private Thread controlH = null;
	private ControlHandler ch = null;
	private Thread iDataH = null;
	private IncomingDataHandler idh = null;
	
//	public CoreRE subclassOperator = null;
	
	static ControlTuple genericAck;
	
	// Timestamp of the last data tuple processed by this operator
	private long ts_data = 0;
	// Timestamp of the last ack processed by this operator
	private long ts_ack;
	
	private OperatorStatus operatorStatus = OperatorStatus.NORMAL;
	
	public CoreRE(WorkerNodeDescription nodeDescr){
		this.nodeDescr = nodeDescr;
		processingUnit = new ProcessingUnit(this);
		coreProcessLogic = new CoreProcessingLogic();
	}
	
	public WorkerNodeDescription getNodeDescr(){
		return nodeDescr;
	}
	
	public void pushOperator(Operator o){
		processingUnit.newOperatorInstantiation(o);
	}
	
	public void setOpReady(int opId) {
		processingUnit.setOpReady(opId);
	}
	
	public void setRuntime(){
		/// At this point I need information about what connections I need to establish
		PUContext puCtx = processingUnit.setUpProcessingUnit();
		
		/// INSTANTIATION
		/** MORE REFACTORING HERE **/
		coreProcessLogic.setOwner(this);
		coreProcessLogic.setProcessingUnit(processingUnit);
		coreProcessLogic.setOpContext(puCtx);
		
		inputQueue = new InputQueue();
		outputQueue = new OutputQueue();
		
		dispatcher = new Dispatcher(puCtx, outputQueue);
		//Configure routing to downstream
		router.configureRoutingImpl(opContext);
		router.initializeQueryFunction();
		
		//Control worker
		ch = new ControlHandler(this, opContext.getOperatorStaticInformation().getInC());
		controlH = new Thread(ch);
		//Data worker
		idh = new IncomingDataHandler(this, opContext.getOperatorStaticInformation().getInD());
		iDataH = new Thread(idh);
		//Consumer worker
		dataConsumer = new DataConsumer(this, inputQueue);
		dConsumerH = new Thread(dataConsumer);

		dConsumerH.start();
		controlH.start();
		iDataH.start();

		//initialize the genericAck message to answer some specific messages.
		ControlTuple b = new ControlTuple();
		b.setType(ControlTupleType.ACK);
		genericAck = b;
		
		NodeManager.nLogger.info("-> Node "+nodeDescr.getNodeId()+" instantiated");
		
		/// INITIALIZATION
		
		//Once Router is configured, we assign it to dispatcher that will make use of it on runtime
		dispatcher.setRouter(router);

		//Choose the upstreamBackupIndex for this operator
		coreProcessLogic.configureUpstreamIndex();
		
		NodeManager.nLogger.info("-> Node "+nodeDescr.getNodeId()+" comm initialized");
	}
	
	public void startDataProcessing(){
		
	}
	
	public void stopDataProcessing(){
		//TODO stop and release resources
	}
	
	
	public OperatorStatus getOperatorStatus(){
		return operatorStatus;
	}
	
	public void setOperatorStatus(OperatorStatus operatorStatus){
		this.operatorStatus = operatorStatus;
	}
	
	//This enum is for aiding in the implementation of the protocols
	public enum OperatorStatus {
		NORMAL, WAITING_FOR_STATE_ACK, INITIALISING_STATE//, REPLAYING_BUFFER//, RECONFIGURING_COMM
	}
	
	public enum ControlTupleType{
		ACK, BACKUP_STATE, RECONFIGURE, SCALE_OUT, RESUME, INIT_STATE, STATE_ACK, INVALIDATE_STATE,
		BACKUP_RI, INIT_RI
	}
	
	public synchronized void setTsData(long ts_data){
		this.ts_data = ts_data;
	}

	public synchronized long getTsData(){
		return ts_data;
	}
	
	public InputQueue getInputQueue(){
		return inputQueue;
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
	
//	public CoreRE getSubclassOperator() {
//		return subclassOperator;
//	}
	
	public Dispatcher getDispatcher(){
		return dispatcher;
	}
	
	public Router getRouter(){
		//If router is to be retrieved, it has previously been initialized
		return router;
	}
	
	public void setRouter(Router router){
		this.router = router;
	}
	
	public int getBackupUpstreamIndex() {
		return backupUpstreamIndex;
	}

	public void setBackupUpstreamIndex(int backupUpstreamIndex) {
		this.backupUpstreamIndex = backupUpstreamIndex;
	}
	
	public CoreRE(int opID){
//		this.operatorId = opID;
//		opContext = new RuntimeContext();
//		opCommonProcessLogic = new CoreProcessingLogic();
		router = new Router();
	}
	
	

//	public void instantiateOperator() throws OperatorInstantiationException{
//		
//		opCommonProcessLogic.setOwner(this);
//		opCommonProcessLogic.setOpContext(opContext);
//		inputQueue = new InputQueue();
//		outputQueue = new OutputQueue();
//		dispatcher = new Dispatcher(opContext, outputQueue);
//		//Configure routing to downstream
//		router.configureRoutingImpl(opContext);
//		
//		//Control worker
//		ch = new ControlHandler(this, opContext.getOperatorStaticInformation().getInC());
//		controlH = new Thread(ch);
//		//Data worker
//		idh = new IncomingDataHandler(this, opContext.getOperatorStaticInformation().getInD());
//		iDataH = new Thread(idh);
//		//Consumer worker
//		dataConsumer = new DataConsumer(this, inputQueue);
//		dConsumerH = new Thread(dataConsumer);
//
//		dConsumerH.start();
//		controlH.start();
//		iDataH.start();
//
//		//initialize the genericAck message to answer some specific messages.
//		ControlTuple b = new ControlTuple();
//		b.setType(ControlTupleType.ACK);
//		genericAck = b;
//		
//		NodeManager.nLogger.info("-> OP"+this.getOperatorId()+" instantiated");
//	}
	
/// \todo {return a proper boolean after check...}
//	public void initializeCommunications() throws OperatorInitializationException{
//		dispatcher.setOpContext(opContext);
//		router.initializeQueryFunction();
//		//Once Router is configured, we assign it to dispatcher that will make use of it on runtime
//		dispatcher.setRouter(router);
//		opContext.configureCommunication();
//
//		//Choose the upstreamBackupIndex for this operator
//		opCommonProcessLogic.configureUpstreamIndex();
//		
//		NodeManager.nLogger.info("-> OP"+this.getOperatorId()+" comm initialized");
//	}
	
	//TODO To refine this method...
	/// \todo {this method should work when an operator must be killed in a proper way}
	public boolean killHandlers(){
		//controlH.destroy();
		//iDataH.destroy();
		return true;
	}

	/** this hacked sent was added on 17 october 2012 to avoid refactoring the inner system to allow a operator configured with a stateful down to send
	 * 
	public void hackRouter(){
		for(Integer id : opContext.getOriginalDownstream()){
			PlacedOperator down = opContext.findDownstream(id);
			int index = down.index();
			int numDownstreams = opContext.getDownstreamSize();
			StatelessRoutingImpl rs = new StatelessRoutingImpl(1, index, numDownstreams);
			System.out.println("ROUTER HACKED");
			//If more than one downstream type, then put the new rs with the opId
			router.setNewLoadBalancer(id, rs);
		}
	}
	as a stateless **/
		
	/// \todo{reduce messages here. ACK, RECONFIGURE, BCK_STATE, rename{send_init, init_ok, init_state}}
	public void processControlTuple(ControlTuple ct, OutputStream os) {
		/** ACK message **/
		ControlTupleType ctt = ct.getType();
		if(ctt.equals(ControlTupleType.ACK)) {
//long a = System.currentTimeMillis();
			if(ct.getAck().getTs() >= ts_ack){
				ackCounter++;
				coreProcessLogic.processAck(ct.getAck());
			}
//long b = System.currentTimeMillis() - a;
//System.out.println("*processAck: "+b);
		}
		/** INVALIDATE_STATE message **/
		else if(ctt.equals(ControlTupleType.INVALIDATE_STATE)) {
long a = System.currentTimeMillis();
			NodeManager.nLogger.info("-> Node "+nodeDescr.getNodeId()+" recv ControlTuple.INVALIDATE_STATE from OP: "+ct.getInvalidateState().getOpId());
			processingUnit.invalidateState(ct.getInvalidateState().getOpId());
long b = System.currentTimeMillis() - a;
System.out.println("*invalidate_state: "+b);
		}
		/** INIT_MESSAGE message **/
		else if(ctt.equals(ControlTupleType.INIT_STATE)){
long a = System.currentTimeMillis();
			NodeManager.nLogger.info("-> Node "+nodeDescr.getNodeId()+" recv ControlTuple.INIT_STATE from OP: "+ct.getInitState().getOpId());
			processInitState(ct.getInitState());
long b = System.currentTimeMillis() - a;
System.out.println("*init_message: "+b);
		}
		/** BACKUP_STATE message **/
		else if(ctt.equals(ControlTupleType.BACKUP_STATE)){
			//If communications are not being reconfigured
//			if(!operatorStatus.equals(OperatorStatus.RECONFIGURING_COMM)){
//			if(!operatorStatus.equals(OperatorStatus.REPLAYING_BUFFER)){
long a = System.currentTimeMillis();
			//Register this state as being managed by this operator
				BackupState backupState = ct.getBackupState();
				NodeManager.nLogger.info("-> Node "+nodeDescr.getNodeId()+" recv BACKUP_STATE from OP: "+backupState.getOpId());
				processingUnit.registerManagedState(backupState.getOpId());
				coreProcessLogic.processBackupState(backupState);
long b = System.currentTimeMillis() - a;
System.out.println("*backup_state: "+b);
//System.out.println("#####::: "+ct.getBackupState().getOpId()+" ::: STATE SIZE: "+ct.build().getSerializedSize());
//			}
//			else{
//				NodeManager.nLogger.info("-> Received BACKUP_STATE from OP: "+ct.getBackupState().getOpId()+" but reconfiguring, IGNORE");
//			}
		}
		/** STATE_ACK message **/
		else if(ctt.equals(ControlTupleType.STATE_ACK)){
long a = System.currentTimeMillis();
			int opId = ct.getStateAck().getOpId();
			NodeManager.nLogger.info("-> Received STATE_ACK from OP: "+opId);
//			operatorStatus = OperatorStatus.REPLAYING_BUFFER;
//			opCommonProcessLogic.replayTuples(ct.getStateAck().getOpId());
			CommunicationChannel cci = puCtx.getCCIfromOpId(opId, "d");
			outputQueue.replayTuples(cci);
//			operatorStatus = OperatorStatus.NORMAL;
long b = System.currentTimeMillis() - a;
System.out.println("*state_ack: "+b);
		}
		/** BACKUP_RI message **/
		else if(ctt.equals(ControlTupleType.BACKUP_RI)){
long a = System.currentTimeMillis();

			NodeManager.nLogger.info("-> Node "+nodeDescr.getNodeId()+" recv ControlTuple.BACKUP_RI");
			coreProcessLogic.storeBackupRI(ct.getBackupRI());
long b = System.currentTimeMillis() - a;
System.out.println("*backup_ri: "+b);
		}
		/** INIT_RI message **/
		else if(ctt.equals(ControlTupleType.INIT_RI)){
long a = System.currentTimeMillis();
			NodeManager.nLogger.info("-> Node "+nodeDescr.getNodeId()+" recv ControlTuple.INIT_RI from : "+ct.getInitRI().getOpId());
			coreProcessLogic.installRI(ct.getInitRI());
long b = System.currentTimeMillis() - a;
System.out.println("*backup_ri: "+b);
		}
		/** SCALE_OUT message **/
		else if(ctt.equals(ControlTupleType.SCALE_OUT)) {
			//Ack the message, we do not need to wait until the end
long a = System.currentTimeMillis();
			NodeManager.nLogger.info("-> Node "+nodeDescr.getNodeId()+" recv ControlTuple.SCALE_OUT");
			coreProcessLogic.scaleOut(ct.getScaleOutInfo());
			dispatcher.ackControlMessage(genericAck, os);
long b = System.currentTimeMillis() - a;
System.out.println("*scaleOut: "+b);
		}
		/** RESUME message **/
		else if (ctt.equals(ControlTupleType.RESUME)) {
long a = System.currentTimeMillis();
			NodeManager.nLogger.info("-> Node "+nodeDescr.getNodeId()+" recv ControlTuple.RESUME");
			Resume resumeM = ct.getResume();
			
			// If I have previously splitted the state, I am in WAITING FOR STATE-ACK status and I have to replay it.
			// I may be managing a state but I dont have to replay it if I have not splitted it previously
			if(operatorStatus.equals(OperatorStatus.WAITING_FOR_STATE_ACK)){
				/// \todo {why has resumeM a list?? check this}
				for (int opId: resumeM.getOpId()){
					//Check if I am managing the state of any of the operators to which state must be replayed
					/// \todo{if this is waiting for ack it must be managing the state, so this IF would be unnecessary}
					if(processingUnit.isManagingStateOf(opId)){
						/// \todo{if this is waiting for ack it must be managing the state, so this IF would be unnecessary}
						if(subclassOperator instanceof StateSplitI){
							//new Thread(new StateReplayer(opContext.getOIfromOpId(opId, "d"))).start();
							NodeManager.nLogger.info("-> Replaying State");
							coreProcessLogic.replayState(opId);
						}
					}
					else{
						NodeManager.nLogger.info("-> NOT in charge of managing this state");
					}
				}
				//Once I have replayed the required states I put my status to NORMAL
				this.operatorStatus = OperatorStatus.NORMAL;
			}
			else{
				NodeManager.nLogger.info("-> Ignoring RESUME state, I did not split this one");
			}
long b = System.currentTimeMillis() - a;
System.out.println("*resume: "+b);
			//Finally ack the processing of this message
			dispatcher.ackControlMessage(genericAck, os);
		}
		
		/** RECONFIGURE message **/
		else if(ctt.equals(ControlTupleType.RECONFIGURE)){
long a = System.currentTimeMillis();
			processCommand(ct.getReconfigureConnection(), os);
long b = System.currentTimeMillis() - a;
System.out.println("*reconfigure: "+b);
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
				dispatcher.stopConnection(opId);
			}
			processingUnit.reconfigureOperatorConnection(opId, ip);
			
			if(command.equals("reconfigure_U")){
				coreProcessLogic.sendRoutingInformation(opId, rc.getOperatorType());
			}
			if(command.equals("reconfigure_D")){
				/// \todo {change this deprecated. This was the previous way of replaying stuff, now there are no threads}
				//opCommonProcessLogic.startReplayer(opId);
				// the new way would be something like the following. Anyway it is necessary to check if downstream is statefull or not
				if(processingUnit.isManagingStateOf(opId)){
					if(subclassOperator instanceof StateSplitI){
						//new Thread(new StateReplayer(opContext.getOIfromOpId(opId, "d"))).start();
						NodeManager.nLogger.info("-> Replaying State");
						coreProcessLogic.replayState(opId);
					}
				}
				else{
					NodeManager.nLogger.info("-> NOT in charge of managing this state");
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
				opContext.addDownstream(opId);
				opContext.setDownstreamOperatorStaticInformation(opId, loc);
				opContext.configureNewDownstreamCommunication(opId, loc);
					//if we are in a partition policy the above the above line actually do
					//not activate the key yet, so it works, while in the reconfigure we had
					//to stop
			}
			else if (command.equals("add_upstream")) {
				//Configure new upstream
				opContext.addUpstream(opId);
				opContext.setUpstreamOperatorStaticInformation(opId, loc);
				opContext.configureNewUpstreamCommunication(opId,loc);
				//to avoid problems with the following messages ??
//				operatorStatus = OperatorStatus.NORMAL;
				//Send to that upstream the routing information I am storing (in case there are ri).
				coreProcessLogic.sendRoutingInformation(opId, rc.getOperatorType());
				
				
				//Re-Check the upstreamBackupIndex. Re-check what upstream to send the backup state.
				coreProcessLogic.reconfigureUpstreamBackupIndex();
			}
			dispatcher.ackControlMessage(genericAck, os);
		}
		/** SYSTEM READY message **/
		else if (command.equals("system_ready")){
			dispatcher.ackControlMessage(genericAck, os);
			//Now that all the system is ready (both down and up) I manage my own information and send the required msgs
			coreProcessLogic.sendInitialStateBackup();
		}
		/** REPLAY message **/
		/// \todo {this command is only used for twitter storm model...}
		else if (command.equals("replay")){
			//ackControlMessage(os);
			//FIXME there is only one, this must be done for each conn
			dispatcher.stopConnection(opId);
			/// \todo{avoid this deprecated function}
			//opCommonProcessLogic.startReplayer(opID);
			CommunicationChannel cci = puCtx.getCCIfromOpId(opId, "d");
			outputQueue.replayTuples(cci);
		}
		/** CONFIG SOURCE RATE message **/
		/// \todo {this command should not be delivered to operator. Maybe to nodeManager...}
		else if (command.equals("configureSourceRate")){
			dispatcher.ackControlMessage(genericAck, os);
			int numberEvents = rc.getOpId();
			int time = rc.getInC();
			if(numberEvents == 0 && time == 0){
				Main.maxRate = true;
			}
			else{
				Main.maxRate = false;
				Main.eventR = numberEvents;
				Main.period = time;
			}
		}
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
		else if (command.equals("deactivateMechanisms")){
			dispatcher.ackControlMessage(genericAck, os);
			if(Main.eftMechanismEnabled){
				NodeManager.nLogger.info("--> Desactivated ESFT mechanisms.");
				Main.eftMechanismEnabled = false;
			}
			else{
				NodeManager.nLogger.info("--> Activated ESFT mechanisms.");
				Main.eftMechanismEnabled = true;
			}
		}
		/** NOT RECOGNIZED message **/
		else{
			NodeManager.nLogger.warning("-> Op.processCommand, command not recognized");
			throw new RuntimeException("Operator: ERROR in processCommand");
		}
	}
	
	public void ack(long ts) {
		ControlTuple ack = new ControlTuple(ControlTupleType.ACK, operatorId, ts);
		dispatcher.sendAllUpstreams(ack);
	}

	private void manageBackupUpstreamIndex(int opId){
		//Firstly, I configure my upstreamBackupIndex, which is the index of the operatorId coming in this message (the one in charge of managing it)
		int newIndex = opContext.findUpstream(opId).index();
		if(backupUpstreamIndex != -1 && newIndex != backupUpstreamIndex){
			ControlTuple ct = coreProcessLogic.buildInvalidateMsg(backupUpstreamIndex);
			dispatcher.sendUpstream(ct, backupUpstreamIndex);
		}
		//Set the new backup upstream index, this has been sent by the manager.
		this.setBackupUpstreamIndex(newIndex);
	}
	
	public void processInitState(InitState ct){
long a = System.currentTimeMillis();
		//Reconfigure backup stream index
		manageBackupUpstreamIndex(ct.getOpId());
System.out.println("CONTROL THREAD: changing operator status to initialising");
		//Clean the data processing channel from remaining tuples in old batch
		operatorStatus = OperatorStatus.INITIALISING_STATE;
//		stopDataProcessingChannel();
		//Manage ts...
		//Pass the state to the user
		if (subclassOperator instanceof StatefulOperator){
			NodeManager.nLogger.info("-> Installing INIT state");
			((StatefulOperator)subclassOperator).installState(ct.getState());
			NodeManager.nLogger.info("-> State has been installed");
		}
		else{
			NodeManager.nLogger.info("-> Stateless operator, not installing state");
		}
		operatorStatus = OperatorStatus.NORMAL;
System.out.println("CONTROL THREAD: restarting data processing...");
		//Once the state has been installed, recover dataProcessingChannel
//		restartDataProcessingChannel();
		
		//Send a msg to ask for the rest of information. (tuple replaying)
		NodeManager.nLogger.info("-> Sending STATE_ACK");
		ControlTuple rb = new ControlTuple().makeStateAck(operatorId);
		dispatcher.sendAllUpstreams(rb);
long b = System.currentTimeMillis() - a;
System.out.println("OP.processInitState: "+b);
	}

	//Recover the state to Normal, so the receiver thread can go on with its work
	private void restartDataProcessingChannel() {
		operatorStatus = OperatorStatus.NORMAL;
		
	}

	//Indicate to receiver thread that the operator is initialising the state, so no tuples must be processed
	private void stopDataProcessingChannel() {
		operatorStatus = OperatorStatus.INITIALISING_STATE;
	}

	/**
	 *  DEPPRECATED-> This method is in charge of receiving the backupState msg, put it into a
	 * ControlTuple tuple and send it upstream. It sends always to the upstream with minimum opID,
	 * to make it easier for parallelization: the manager can explicitly tell that guy that
	 * is the one responsible to send INIT message (even if it does not have state, e.g. before any
	 * backup takes place)
	 * 
	 * NEW -> This method previously was sending the state to the same upstream. This strategy becomes a bottleneck when the state is big
	 * or when multiple instances are sending their state to the same upstream. A way of distributing the state is just sending to index%upstreamSize
	 * Of course, in this way, as the upstream grows, the target upstream is going to change, but this does not affect this operator. (however, the old state
	 * must be invalidated).
	 * 
	 * 
	 * NEW -> Now all the previous computation is done when there is an upstream added, to set the system to a coherent state as soon as possible
	 */
	public void backupState(StateI state, String stateClass){
		BackupState bs = new BackupState();
		//TODO Fill ts_e and ts_s of backupstate. FIXME now it is badly assigned
		long currentTsData = ts_data;
		
		bs.setOpId(operatorId);
		bs.setTs_e(currentTsData);
		bs.setTs_s(0);
		bs.setState(state);
		bs.setStateClass(stateClass);
		//Build the ControlTuple msg
		ControlTuple ctB = new ControlTuple().makeBackupState(bs);
		//Finally send the backup state
System.out.println("Sending BACKUP to : "+backupUpstreamIndex+" OPID: "+opContext.getUpOpIdFromIndex(backupUpstreamIndex));
		dispatcher.sendUpstream(ctB, backupUpstreamIndex);
	
		ack(currentTsData);
	}
	


}
