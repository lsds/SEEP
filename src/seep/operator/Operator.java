package seep.operator;

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
import seep.comm.serialization.ControlTuple;
import seep.comm.serialization.DataTuple;
import seep.comm.serialization.controlhelpers.BackupState;
import seep.comm.serialization.controlhelpers.InitState;
import seep.comm.serialization.controlhelpers.ReconfigureConnection;
import seep.comm.serialization.controlhelpers.Resume;
import seep.infrastructure.Node;
import seep.infrastructure.NodeManager;
import seep.infrastructure.OperatorInitializationException;
import seep.infrastructure.OperatorInstantiationException;
import seep.infrastructure.OperatorStaticInformation;
import seep.operator.collection.SmartWordCounter;
import seep.operator.collection.lrbenchmark.Snk;

/**
* Operator. This is the class that must inherit any subclass (the developer must inherit this class). It is the basis for building an operator
*/

public abstract class Operator implements Serializable, QuerySpecificationI {


	private static final long serialVersionUID = 1L;

public int ackCounter = 0;
	
	private int operatorId;
	private int backupUpstreamIndex = -1;

	private OperatorContext opContext;
	private OperatorCommonProcessLogic opCommonProcessLogic;
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
//	private LocalProfiler lp = new LocalProfiler();
	
	public Operator subclassOperator = null;
	
	static ControlTuple genericAck;
	
	// Timestamp of the last data tuple processed by this operator
	private long ts_data = 0;
	// Timestamp of the last ack processed by this operator
	private long ts_ack;
	
	private OperatorStatus operatorStatus = OperatorStatus.NORMAL;
	
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
	
	public Operator getSubclassOperator() {
		return subclassOperator;
	}
	
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
	
	public Operator(int opID){
		this.operatorId = opID;
		opContext = new OperatorContext();
		opCommonProcessLogic = new OperatorCommonProcessLogic();
		router = new Router();
	}
	
	@Override 
	public String toString() {
		return "Operator [operatorId=" + operatorId + ", opContext="
				+ opContext + "]";
	}

	public void instantiateOperator() throws OperatorInstantiationException{
		
		opCommonProcessLogic.setOwner(this);
		opCommonProcessLogic.setOpContext(opContext);
		inputQueue = new InputQueue();
		outputQueue = new OutputQueue();
		dispatcher = new Dispatcher(opContext, outputQueue);
		//Configure routing to downstream
		router.configureRoutingImpl(opContext);
		
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
		
		NodeManager.nLogger.info("-> OP"+this.getOperatorId()+" instantiated");
	}
	
/// \todo {return a proper boolean after check...}
	public void initializeCommunications() throws OperatorInitializationException{
		dispatcher.setOpContext(opContext);
		router.initializeQueryFunction();
		//Once Router is configured, we assign it to dispatcher that will make use of it on runtime
		dispatcher.setRouter(router);
		opContext.configureCommunication();

		//Choose the upstreamBackupIndex for this operator
		opCommonProcessLogic.configureUpstreamIndex();
		
		NodeManager.nLogger.info("-> OP"+this.getOperatorId()+" comm initialized");
	}
	
	//TODO To refine this method...
	/// \todo {this method should work when an operator must be killed in a proper way}
	public boolean killHandlers(){
		//controlH.destroy();
		//iDataH.destroy();
		return true;
	}

//	public <T extends DataTuple> void sendDown(T dt){
//		dispatcher.sendData(dt, Integer.MIN_VALUE, false);
//	}
	
	public void sendDown(DataTuple dt){
		/// \todo{FIX THIS, look for a value that cannot be present in the tuples...}
//		lp.start();
		dispatcher.sendData(dt, Integer.MIN_VALUE, false);
//		lp.finish();
	}
	
	public void sendDown(DataTuple dt, int value){
//		lp.start();
		dispatcher.sendData(dt, value, false);
//		lp.finish();
	}
	
	public void sendNow(DataTuple dt){
		/// \todo{FIX THIS, look for a value that cannot be present in the tuples...}
//		lp.start();
		dispatcher.sendData(dt, Integer.MIN_VALUE, true);
//		lp.finish();
	}
	
	public void sendNow(DataTuple dt, int value){
//		lp.start();
		dispatcher.sendData(dt, value, true);
//		lp.finish();
	}
	
	public abstract boolean isOrderSensitive();
	
	public abstract void processData(DataTuple dt);
	
	/// \todo{reduce messages here. ACK, RECONFIGURE, BCK_STATE, rename{send_init, init_ok, init_state}}
	public void processControlTuple(ControlTuple ct, OutputStream os) {
		/** ACK message **/
		ControlTupleType ctt = ct.getType();
		if(ctt.equals(ControlTupleType.ACK)) {
//long a = System.currentTimeMillis();
			if(ct.getAck().getTs() >= ts_ack){
				ackCounter++;
				opCommonProcessLogic.processAck(ct.getAck());
			}
//long b = System.currentTimeMillis() - a;
//System.out.println("*processAck: "+b);
		}
		/** INVALIDATE_STATE message **/
		else if(ctt.equals(ControlTupleType.INVALIDATE_STATE)) {
long a = System.currentTimeMillis();
			NodeManager.nLogger.info("-> OP "+this.getOperatorId()+" recv ControlTuple.INVALIDATE_STATE from OP: "+ct.getInvalidateState().getOpId());
			opContext.invalidateState(ct.getInvalidateState().getOpId());
long b = System.currentTimeMillis() - a;
System.out.println("*invalidate_state: "+b);
		}
		/** INIT_MESSAGE message **/
		else if(ctt.equals(ControlTupleType.INIT_STATE)){
long a = System.currentTimeMillis();
			NodeManager.nLogger.info("-> OP"+this.getOperatorId()+" recv ControlTuple.INIT_STATE from OP: "+ct.getInitState().getOpId());
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
				NodeManager.nLogger.info("-> OP "+this.getOperatorId()+" recv BACKUP_STATE from OP: "+backupState.getOpId());
				opContext.registerManagedState(backupState.getOpId());
				opCommonProcessLogic.processBackupState(backupState);
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
			CommunicationChannel cci = opContext.getCCIfromOpId(opId, "d");
			outputQueue.replayTuples(cci);
//			operatorStatus = OperatorStatus.NORMAL;
long b = System.currentTimeMillis() - a;
System.out.println("*state_ack: "+b);
		}
		/** BACKUP_RI message **/
		else if(ctt.equals(ControlTupleType.BACKUP_RI)){
long a = System.currentTimeMillis();

			NodeManager.nLogger.info("-> OP"+this.getOperatorId()+" recv ControlTuple.BACKUP_RI");
			opCommonProcessLogic.storeBackupRI(ct.getBackupRI());
long b = System.currentTimeMillis() - a;
System.out.println("*backup_ri: "+b);
		}
		/** INIT_RI message **/
		else if(ctt.equals(ControlTupleType.INIT_RI)){
long a = System.currentTimeMillis();
			NodeManager.nLogger.info("-> OP"+this.getOperatorId()+" recv ControlTuple.INIT_RI from : "+ct.getInitRI().getOpId());
			opCommonProcessLogic.installRI(ct.getInitRI());
long b = System.currentTimeMillis() - a;
System.out.println("*backup_ri: "+b);
		}
		/** SCALE_OUT message **/
		else if(ctt.equals(ControlTupleType.SCALE_OUT)) {
			//Ack the message, we do not need to wait until the end
long a = System.currentTimeMillis();
			NodeManager.nLogger.info("-> OP "+this.getOperatorId()+" recv ControlTuple.SCALE_OUT");
			opCommonProcessLogic.scaleOut(ct.getScaleOutInfo());
			dispatcher.ackControlMessage(genericAck, os);
long b = System.currentTimeMillis() - a;
System.out.println("*scaleOut: "+b);
		}
		/** RESUME message **/
		else if (ctt.equals(ControlTupleType.RESUME)) {
long a = System.currentTimeMillis();
			NodeManager.nLogger.info("-> OP "+this.getOperatorId()+" recv ControlTuple.RESUME");
			Resume resumeM = ct.getResume();
			
			// If I have previously splitted the state, I am in WAITING FOR STATE-ACK status and I have to replay it.
			// I may be managing a state but I dont have to replay it if I have not splitted it previously
			if(operatorStatus.equals(OperatorStatus.WAITING_FOR_STATE_ACK)){
				/// \todo {why has resumeM a list?? check this}
				for (int opId: resumeM.getOpId()){
					//Check if I am managing the state of any of the operators to which state must be replayed
					/// \todo{if this is waiting for ack it must be managing the state, so this IF would be unnecessary}
					if(opContext.isManagingStateOf(opId)){
						/// \todo{if this is waiting for ack it must be managing the state, so this IF would be unnecessary}
						if(subclassOperator instanceof StateSplitI){
							//new Thread(new StateReplayer(opContext.getOIfromOpId(opId, "d"))).start();
							NodeManager.nLogger.info("-> Replaying State");
							opCommonProcessLogic.replayState(opId);
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
		NodeManager.nLogger.info("-> OP "+this.getOperatorId()+" recv "+command+" command ");
		InetAddress ip = null;
		int opId = rc.getOpId();
		
		try{
			ip = InetAddress.getByName(rc.getIp());
		} 
		catch (UnknownHostException uhe) {
			NodeManager.nLogger.severe("-> OP"+this.getOperatorId()+" EXCEPTION while getting IP from msg "+uhe.getMessage());
			uhe.printStackTrace();
		}
		/** RECONFIGURE DOWN or RECONFIGURE UP message **/
		if(command.equals("reconfigure_D") || command.equals("reconfigure_U") || command.equals("just_reconfigure_D")){
//			operatorStatus = OperatorStatus.RECONFIGURING_COMM;
			opContext.changeLocation(opId, ip);
				//If no twitter storm, then I have to stop sending data and replay, otherwise I just update the conn
				/// \test {what is it is twitter storm but it is also the first node, then I also need to stop connection, right?}
			if((command.equals("reconfigure_D") || command.equals("just_reconfigure_D"))){
				dispatcher.stopConnection(opId);
			}
			opContext.updateConnection(opId, ip);
			if(command.equals("reconfigure_U")){
				opCommonProcessLogic.sendRoutingInformation(opId, rc.getOperatorType());
			}
			if(command.equals("reconfigure_D")){
				/// \todo {change this deprecated. This was the previous way of replaying stuff, now there are no threads}
				//opCommonProcessLogic.startReplayer(opId);
				// the new way would be something like the following. Anyway it is necessary to check if downstream is statefull or not
				if(opContext.isManagingStateOf(opId)){
					if(subclassOperator instanceof StateSplitI){
						//new Thread(new StateReplayer(opContext.getOIfromOpId(opId, "d"))).start();
						NodeManager.nLogger.info("-> Replaying State");
						opCommonProcessLogic.replayState(opId);
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
				opCommonProcessLogic.sendRoutingInformation(opId, rc.getOperatorType());
				
				
				//Re-Check the upstreamBackupIndex. Re-check what upstream to send the backup state.
				opCommonProcessLogic.reconfigureUpstreamBackupIndex();
			}
			dispatcher.ackControlMessage(genericAck, os);
		}
		/** SYSTEM READY message **/
		else if (command.equals("system_ready")){
			dispatcher.ackControlMessage(genericAck, os);
			//Now that all the system is ready (both down and up) I manage my own information and send the required msgs
			opCommonProcessLogic.sendInitialStateBackup();
		}
		/** REPLAY message **/
		/// \todo {this command is only used for twitter storm model...}
		else if (command.equals("replay")){
			//ackControlMessage(os);
			//FIXME there is only one, this must be done for each conn
			dispatcher.stopConnection(opId);
			/// \todo{avoid this deprecated function}
			//opCommonProcessLogic.startReplayer(opID);
			CommunicationChannel cci = opContext.getCCIfromOpId(opId, "d");
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
			dispatcher.ackControlMessage(genericAck, os);
			try{
			((Snk)this.subclassOperator).save();
			}catch(Exception e){
				((SmartWordCounter)this.subclassOperator).save();
			}
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
			ControlTuple ct = opCommonProcessLogic.buildInvalidateMsg(backupUpstreamIndex);
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
		if (subclassOperator instanceof StatefullOperator){
			NodeManager.nLogger.info("-> Installing INIT state");
			((StatefullOperator)subclassOperator).installState(ct);
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
	public void backupState(BackupState bs){
		//TODO Fill ts_e and ts_s of backupstate. FIXME now it is badly assigned
		long currentTsData = ts_data;
		
		bs.setOpId(operatorId);
		bs.setTs_e(currentTsData);
		bs.setTs_s(0);
		//Build the ControlTuple msg
		ControlTuple ctB = new ControlTuple().makeBackupState(bs);
		//Finally send the backup state
System.out.println("Sending BACKUP to : "+backupUpstreamIndex+" OPID: "+opContext.getUpOpIdFromIndex(backupUpstreamIndex));
		dispatcher.sendUpstream(ctB, backupUpstreamIndex);
	
		ack(currentTsData);
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
		router.setQueryFunction(queryFunction_methodName);
		NodeManager.nLogger.info("Configured Routing Query Function: "+queryFunction_methodName+" in Operator: "+this.toString());
	}
	
	public void route(Router.RelationalOperator operand, int value, Operator toConnect){
		int opId = toConnect.getOperatorId();
		router.routeValueToDownstream(operand, value, opId);
		NodeManager.nLogger.info("Operator: "+this.toString()+" sends data with value: "+value+" to Operator: "+toConnect.toString());
	}
	
	public void scaleOut(Operator toScaleOut){
		//TODO implement static scaleOut
	}
}
