package seep.processingunit;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import seep.comm.routing.Router;
import seep.comm.serialization.ControlTuple;
import seep.comm.serialization.DataTuple;
import seep.comm.serialization.controlhelpers.BackupNodeState;
import seep.comm.serialization.controlhelpers.BackupOperatorState;
import seep.comm.serialization.controlhelpers.InitOperatorState;
import seep.infrastructure.NodeManager;
import seep.infrastructure.monitor.MetricsReader;
import seep.operator.EndPoint;
import seep.operator.Operator;
import seep.operator.OperatorContext;
import seep.operator.OperatorStaticInformation;
import seep.operator.State;
import seep.operator.StatefulOperator;
import seep.runtimeengine.CommunicationChannel;
import seep.runtimeengine.CoreRE;
import seep.runtimeengine.OutputQueue;

public class ProcessingUnit {

	private CoreRE owner = null;
	private PUContext ctx = null;
	private SystemStatus systemStatus = SystemStatus.NORMAL;

	//Operators managed by this processing unit [ opId<Integer> - op<Operator> ]
	static public Map<Integer, Operator> mapOP_ID = new HashMap<Integer, Operator>();
	//Map between operator id and state [opId<Integer> - State]
	private Map<Integer, State> mapOP_S = new HashMap<Integer, State>();
	// lockState arbiters the access to the state between operators and processingUnit. 0-> free, 1->operator, 2->pu
	private AtomicInteger lockState = new AtomicInteger(0);
	private Operator mostUpstream = null;
	private Operator mostDownstream = null;
	
	private OutputQueue outputQueue = null;
	
	private StateBackupWorker sbw;
	private Thread stateWorker;
	private ArrayList<Integer> listOfManagedStates = new ArrayList<Integer>();
	
	public ProcessingUnit(CoreRE owner){
		this.owner = owner;
		ctx = new PUContext(owner.getNodeDescr());
	}
	
	public Operator getMostUpstream(){
		return mostUpstream;
	}
	
	public Operator getMostDownstream(){
		return mostDownstream;
	}
	
	public boolean isNodeStateful(){
		// If its not empty is because a previous operator let there its state
		return !mapOP_S.isEmpty();
	}
	
	public SystemStatus getSystemStatus(){
		return systemStatus;
	}
	
	public void setSystemStatus(SystemStatus systemStatus){
		this.systemStatus = systemStatus;
	}
	
	//This enum is for aiding in the implementation of the protocols
	public enum SystemStatus {
		NORMAL, WAITING_FOR_STATE_ACK, INITIALISING_STATE//, REPLAYING_BUFFER//, RECONFIGURING_COMM
	}
	
	/** SETUP methods **/
	
	public void setOutputQueue(OutputQueue outputQueue){
		this.outputQueue = outputQueue;
	}
	
	public void newOperatorInstantiation(Operator o) {
		NodeManager.nLogger.info("-> Instantiating Operator");
		//Detect the first submitted operator
		if(mapOP_ID.isEmpty()){
			mostUpstream = o;
		}
		o.setProcessingUnit(this);
		mapOP_ID.put(o.getOperatorId(), o);
		//If the operator is stateful, we extract its state and store it in the provisioned map
		if(o instanceof StatefulOperator){
			// This may happen when operators are added dynamically and no human has added a state
			o.getState();
			if(o.getState() != null){
				mapOP_S.put(o.getOperatorId(), o.getState());
			}
		}
		// Overwritten till last instantiation. If there are movements within the same node, then this wont be valid
		mostDownstream = o;
	}

	public boolean allOperatorsReady(){
		for(Operator o : mapOP_ID.values()){
			if(!o.getReady()){
				return false;
			}
		}
		return true;
	}
	
	public void setOpReady(int opId) {
		NodeManager.nLogger.info("-> Setting operator ready");
		mapOP_ID.get(opId).setReady(true);
	}
	
	public PUContext setUpProcessingUnit(){
		//Create connections between operators
//		ArrayList<Operator> operatorSet = (ArrayList<Operator>) mapOP_ID.values();
		Collection<Operator> operatorSet = mapOP_ID.values();
		ctx.configureOperatorConnections(operatorSet);
		//Create and configure routers
		for(Operator op : operatorSet){
			// Initialize and set the routing information
			String queryFunction = op.getOpContext().getQueryFunction();
			HashMap<Integer, ArrayList<Integer>> routeInfo = op.getOpContext().getRouteInfo();
			Router r = new Router(queryFunction, routeInfo);
			// Configure routing implementations of the operator
			r.configureRoutingImpl(op.getOpContext());
			op.setRouter(r);
		}
		return ctx;
	}
	
	public void createAndRunStateBackupWorker(){
		// Create and run state backup worker
		NodeManager.nLogger.info("-> Stateful Node, setting the backup worker thread...");
		sbw = new StateBackupWorker(this, mapOP_S);
		stateWorker = new Thread(sbw);
		stateWorker.start();
	}
	
	public int getStateCheckpointInterval(){
		int checkpointInterval = Integer.MAX_VALUE;
		for(State s : mapOP_S.values()){
			int currentCheckpointInterval = s.getCheckpointInterval();
			if(currentCheckpointInterval < checkpointInterval){
				checkpointInterval = currentCheckpointInterval;
			}
		}
		return checkpointInterval;
	}
	
	public void startDataProcessing(){
		/// \todo{Find a better way to start the operator...}
		DataTuple fake = new DataTuple();
		this.mostUpstream.processData(fake);
	}
	
	public void initOperators(){
		for(Operator o : mapOP_ID.values()){
			// FIXME: check what exceptions may arise here and handle them properly
			o.setUp();
		}
	}
	
	/** Runtime methods **/
	
	public void processData(DataTuple data){
		// Try to acquire the lock for processing data
		while(!lockState.compareAndSet(0,1)){
			// If not successfull wait till is available
			synchronized(lockState){
				try{
					lockState.wait();
				}
				catch(InterruptedException ie){
					ie.printStackTrace();
				}
			}
		}
		// If successful process the data
		// TODO: Adjust timestamp of state
		mostUpstream.processData(data);
		//Set the lock free again
		lockState.set(0);
		synchronized(lockState){
			lockState.notify();
		}
	}

	public void sendData(DataTuple dt, ArrayList<Integer> targets){
		for(int i = 0; i<targets.size(); i++){
			int target = targets.get(i);
			try{
				EndPoint dest = ctx.getDownstreamTypeConnection().elementAt(target);
				// REMOTE
				if(dest instanceof CommunicationChannel){
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
	
	public void cleanInputQueue(){
		System.out.println("Going to clean");
		owner.cleanInputQueue();
		System.out.println("Returning from cleaning...");
	}
	
	/** System configuration settings used by the developers **/
	
	public void disableCheckpointForOperator(int opId){
		// Just remove the state to backup
		mapOP_S.remove(opId);
	}
	
	public synchronized void stopConnection(int opID) {
		//Stop incoming data, a new thread is replaying
//		NodeManager.nLogger.info("-> Dispatcher. replaySemaphore increments: "+replaySemaphore.toString());
		
		/**
		 * hack done on july the third 2012 to get parallel recovery results.
		 *  we make sure that conn is only stop once
		 */
//if (replaySemaphore.get() > 0){
//	return;
//}
		
//		replaySemaphore.incrementAndGet();
		outputQueue.stop();
		ctx.getCCIfromOpId(opID, "d").getStop().set(true);
	}
	
//	public void sendData(DataTuple dt, int value, boolean now) {
//		ArrayList<Integer> targets = router.forward(dt, value, now);
//		for(Integer target : targets){
//			try{
////			System.out.println("TARGET: "+target.toString());
//				EndPoint dest = puCtx.getDownstreamTypeConnection().elementAt(target);
//				outputQueue.sendToDownstream(dt, dest, now, false);
//			}
//			catch(ArrayIndexOutOfBoundsException aioobe){
//				System.out.println("Targets size: "+targets.size()+" Target-Index: "+target+" downstreamSize: "+puCtx.getDownstreamTypeConnection().size());
//				aioobe.printStackTrace();
//			}
//		}
//		
//		
//	}
	
	/** Operator information management **/
	
	public void reconfigureOperatorLocation(int opId, InetAddress ip){
		mapOP_ID.get(opId).getOpContext().changeLocation(opId, ip);
	}
	
	public void reconfigureOperatorConnection(int opId, InetAddress ip){
		ctx.updateConnection(opId, ip);
	}
	
	public ArrayList<Integer> getRouterIndexesInformation(int opId){
		return mostDownstream.getRouter().getIndexesInformation(opId);
	}
	
	public ArrayList<Integer> getRouterKeysInformation(int opId){
		return mostDownstream.getRouter().getKeysInformation(opId);
	}
	
	/** State Management Stuff **/
	
		/** State operations **/
	public void checkpointAndBackupState(){
		// Try to set lockState to 2 for backuping state
		while(!lockState.compareAndSet(0,2)){
			synchronized(lockState){
				try{
					lockState.wait();
				}
				catch(InterruptedException ie){
					ie.printStackTrace();
				}
			}
		}
		// Backup state
		backupState();
		//Set the lock free again
		lockState.set(0);
		synchronized(lockState){
			lockState.notify();
		}
	}
	
	private void backupState(){
		int numberOfStates = mapOP_S.values().size();
		// If there is something to backup...
		// FIXME: this should anyway be avoided by controlling whether the statebackupworker needs to execute, according to this value
		if(numberOfStates > 0){
			//Create the array of backup states
			BackupNodeState backupNodeState = new BackupNodeState(owner.getNodeDescr().getNodeId(), mostUpstream.getOperatorId());
			BackupOperatorState[] backupState = new BackupOperatorState[numberOfStates];
			// We fill the array with the states
			/// \fixme{UGLY STUFF}
			Collection<State> aux = mapOP_S.values();
			// Not using constructor with 
			ArrayList<State> statesToBackup = new ArrayList<State>(aux);
			
			System.out.println("States to backup size: "+statesToBackup.size());
			for(int i = 0; i < numberOfStates ; i++){
				State current = statesToBackup.get(i);
				BackupOperatorState bs = new BackupOperatorState();
				// current is null in a parallelized operator
				bs.setOpId(current.getOwnerId());
				bs.setState(current);
				bs.setStateClass(current.getStateTag());
				backupState[i] = bs;
			}
			NodeManager.nLogger.info("-> Backuping the "+backupState.length+" states in this node");
			//Build the ControlTuple msg
			backupNodeState.setBackupOperatorState(backupState);
			ControlTuple ctB = new ControlTuple().makeBackupState(backupNodeState);
			//Finally send the backup state
//System.out.println("Sending BACKUP to : "+backupUpstreamIndex+" OPID: "+opContext.getUpOpIdFromIndex(backupUpstreamIndex));
			owner.sendBackupState(ctB);
//			controlDispatcher.sendUpstream(ctB, backupUpstreamIndex);
//			ack(currentTsData);
		}
	}
	
	public void installState(InitOperatorState[] initOperatorState){
		System.out.println("Installing state: inputqueue size: "+MetricsReader.eventsInputQueue.getCount());
		NodeManager.nLogger.info("Installing state in the operator");
		// Simply replace the state and update operator references
		System.out.println("I receive "+initOperatorState.length+" states to recover");
		for(int i = 0; i < initOperatorState.length; i++){
			InitOperatorState current = initOperatorState[i];
			int stateOwnerId = current.getOpId();
			System.out.println("This state ownerID is:  "+stateOwnerId);
			State state = current.getState();
			// Replace state
			mapOP_S.put(stateOwnerId, state);
			// And reference in operator
			((StatefulOperator)mapOP_ID.get(stateOwnerId)).replaceState(state);
		}
		System.out.println("END INSTALL state: inputqueue size: "+MetricsReader.eventsInputQueue.getCount());
	}
	
	
		/** Who manages which state? **/
	
	public synchronized void invalidateState(int opId) {
		//If the states figures as being managed we removed it
		int index = 0;
		if((index = listOfManagedStates.indexOf(opId)) != -1) listOfManagedStates.remove(index);
		// and then we clean both the buffer and the mapping in downstreamBuffers.
		if(PUContext.downstreamBuffers.get(opId) != null){
			//First of all, we empty the buffer
			PUContext.downstreamBuffers.get(opId).replaceBackupNodeState(null);
		}
	}
	
	public synchronized void registerManagedState(int opId) {
		//If the state does not figure as being managed, we include it
		if(!listOfManagedStates.contains(opId)){
			NodeManager.nLogger.info("-> New STATE registered for NODE: "+opId);
			listOfManagedStates.add(opId);
		}
	}
	
	public boolean isManagingStateOf(int opId) {
		return listOfManagedStates.contains(opId) ? true : false;
	}
	
		/** Dynamic change of operator information **/
	
	public void addDownstream(int opId, OperatorStaticInformation location){
		// First pick the most downstream operator, and add the downstream to that one
		OperatorContext opContext = mapOP_ID.get(mostDownstream.getOperatorId()).getOpContext();
		opContext.addDownstream(opId);
		opContext.setDownstreamOperatorStaticInformation(opId, location);
		ctx.configureNewDownstreamCommunication(opId, location);
	}
	
	public void addUpstream(int opId, OperatorStaticInformation location){
		// First pick the most upstream operator, and add the upstream to that one
		OperatorContext opContext = mapOP_ID.get(mostUpstream.getOperatorId()).getOpContext();
		opContext.addUpstream(opId);
		opContext.setUpstreamOperatorStaticInformation(opId, location);
		ctx.configureNewUpstreamCommunication(opId, location);
	}
}
