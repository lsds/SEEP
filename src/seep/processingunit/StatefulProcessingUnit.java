package seep.processingunit;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import seep.comm.serialization.ControlTuple;
import seep.comm.serialization.DataTuple;
import seep.comm.serialization.controlhelpers.BackupOperatorState;
import seep.comm.serialization.controlhelpers.InitOperatorState;
import seep.infrastructure.NodeManager;
import seep.infrastructure.monitor.MetricsReader;
import seep.operator.EndPoint;
import seep.operator.Operator;
import seep.operator.OperatorContext;
import seep.operator.OperatorStaticInformation;
import seep.operator.Partitionable;
import seep.operator.State;
import seep.operator.StatefulOperator;
import seep.runtimeengine.AsynchronousCommunicationChannel;
import seep.runtimeengine.CoreRE;
import seep.runtimeengine.DataStructureAdapter;
import seep.runtimeengine.OutputQueue;
import seep.runtimeengine.SynchronousCommunicationChannel;

/**
 * mutex or lockstate in this class are the by default java mechanism, and my custom made locking mech. Mine performs slightly better but it is far less
 * readable. This mutex has to be acquired when processinUnit gives control to user, and released every time user code returns here (for example, when they call
 * sendData). Recall that after sendData it is possible that people will keep doing computation, so mutex needs to be re-acquired, again, every time that 
 * processingunit gives control to user code. Now, statebackupWorker, and in particular its call to backupState is the place where we need to acquire the mutex,
 * in particular to deep copying the state itself. Once the state is deep copyed, the mutex is no longer necessary. 
 * 
 * MUTEX from java (Semaphore(1)
 * --------------------------------
 * 
 	// Mutex between data processing and state backup
	private Semaphore mutex = new Semaphore(1);
 * 
 * Acquire:
 		try {
			mutex.acquire();
		} 
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 * Release:
 * --------
 * 		mutex.release();
 * 
 * CUSTOM MUTEX, slightly better in terms of performance
 * -----------------------------------------------------
 * 
 	// lockState arbiters the access to the state between operators and processingUnit. 0-> free, 1->operator, 2->pu
	private AtomicInteger _lockState = new AtomicInteger(0);
 * 
 * Acquire: (1-> processing data, 2-> backup state)
 * 		// Try to acquire the lock for processing data
		while(!_lockState.compareAndSet(0,1)){
			// If not successfull wait till is available
			synchronized(_lockState){
				try{
					_lockState.wait();
				}
				catch(InterruptedException ie){
					ie.printStackTrace();
				}
			}
		}
 * 
 * 
 * Release:
 * ---------
 * 
 		//Set the lock free again
		_lockState.set(0);
		synchronized(_lockState){
			_lockState.notify();
		}
 * 
 * **/


public class StatefulProcessingUnit implements IProcessingUnit{

	private CoreRE owner = null;
	
	private PUContext ctx = null;
	private SystemStatus systemStatus = SystemStatus.NORMAL;

	// Mutex between data processing and state backup
	private Semaphore mutex = new Semaphore(1);

	//Operator and state managed by this processingUnit
	private Operator runningOp = null;
	private State runningOpState = null;

	private OutputQueue outputQueue = null;
	
	private StateBackupWorker sbw;
	private Thread stateWorker;
	private boolean isCheckpointEnabled = true;
	private ArrayList<Integer> listOfManagedStates = new ArrayList<Integer>();
	
	//Multi-core support
	private Executor pool;
	private boolean multiCoreEnabled = true;
	
	public StatefulProcessingUnit(CoreRE owner){
		this.owner = owner;
		ctx = new PUContext(owner.getNodeDescr());
	}

	public boolean isCheckpointEnabled(){
		return isCheckpointEnabled;
	}
	
	@Override
	public Operator getOperator(){
		return runningOp;
	}

	@Override
	public boolean isNodeStateful(){
		return true;
	}
	
	@Override
	public SystemStatus getSystemStatus(){
		return systemStatus;
	}
	
	@Override
	public void setSystemStatus(SystemStatus systemStatus){
		this.systemStatus = systemStatus;
	}
	
	@Override
	public boolean isMultiCoreEnabled() {
		return multiCoreEnabled;
	}
	
	/** SETUP methods **/
	@Override
	public void setOutputQueue(OutputQueue outputQueue){
		this.outputQueue = outputQueue;
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
		if(o.getState() != null){
			runningOpState = o.getState();
		}
		else{
			NodeManager.nLogger.warning("-> Initial state is null...");
		}
	}

	@Override
	public boolean isOperatorReady(){
		return runningOp.getReady();
	}
	
	@Override
	public void setOpReady(int opId) {
		NodeManager.nLogger.info("-> Setting operator ready");
		runningOp.setReady(true);
	}
	
	@Override
	public PUContext setUpRemoteConnections(){
		ctx.configureOperatorConnections(runningOp);
		return ctx;
	}
	
	public void createAndRunStateBackupWorker(){
		// Create and run state backup worker
		NodeManager.nLogger.info("-> Stateful Node, setting the backup worker thread...");
		if(this == null || runningOpState == null){
			System.out.println("NULL runningopstate");
			System.exit(0);
		}
		sbw = new StateBackupWorker(this, runningOpState);
		stateWorker = new Thread(sbw);
		stateWorker.start();
	}
	
	public int getStateCheckpointInterval(){
		return runningOpState.getCheckpointInterval();
	}
	
	@Override
	public void startDataProcessing(){
		/// \todo{Find a better way to start the operator...}
		DataTuple fake = DataTuple.getNoopDataTuple();
		this.runningOp.processData(fake);
	}
	
	@Override
	public void initOperator(){
		runningOp.setUp();
	}
	
	@Override
	public Map<String, Integer> createTupleAttributeMapper(){
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
	
	/** Runtime methods **/
	
	@Override
	public void processData(DataTuple data){
		// Get the mutex
		try {
			mutex.acquire();
		} 
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Instrumentation
		//MetricsReader.eventsPerSecond.mark();
		MetricsReader.eventsProcessed.inc();
		// TODO: Adjust timestamp of state
		runningOp.processData(data);
		// Release the mutex
		mutex.release();
	}
	
	@Override
	public void processData(ArrayList<DataTuple> data){
		// Get the mutex
		try {
			mutex.acquire();
		} 
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Instrumentation
//		MetricsReader.eventsPerSecond.mark();
		
		MetricsReader.eventsProcessed.inc();
		// TODO: Adjust timestamp of state
		runningOp.processData(data);
		// Release the mutex
		mutex.release();
	}

	@Override
	public void sendData(DataTuple dt, ArrayList<Integer> targets){
		// Here user code (operator) returns from execution, so release mutex
		mutex.release();
		for(int i = 0; i<targets.size(); i++){
			int target = targets.get(i);
			try{
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
		// Here, user code can potentially keep modifying state, acquire the mutex
		// Note that if user code finishes after this call, the mutex will be released after processData anyway, so it is safe to get the mutex here.
		try {
			mutex.acquire();
		} 
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/** System configuration settings used by the developers **/
	
	public void disableCheckpointForOperator(int opId){
		// Just remove the state to backup
		isCheckpointEnabled = false;
	}
	
	@Override
	public synchronized void stopConnection(int opID) {
		//Stop incoming data, a new thread is replaying
		outputQueue.stop();
		ctx.getCCIfromOpId(opID, "d").getStop().set(true);
	}
	
	/** Operator information management **/
	@Override
	public void reconfigureOperatorLocation(int opId, InetAddress ip){
		runningOp.getOpContext().changeLocation(opId, ip);
	}
	
	@Override
	public void reconfigureOperatorConnection(int opId, InetAddress ip){
		if(runningOp.getOperatorId() == opId){
			ctx.updateConnection(runningOp, ip);
		}
		else{
			NodeManager.nLogger.warning("-> This node does not contain the requested operator: "+opId);
		}
	}
	
	public ArrayList<Integer> getRouterIndexesInformation(int opId){
		return runningOp.getRouter().getIndexesInformation(opId);
	}
	
	public ArrayList<Integer> getRouterKeysInformation(int opId){
		return runningOp.getRouter().getKeysInformation(opId);
	}
	
	/** State Management Stuff **/
	
	/** State operations **/
	public void checkpointAndBackupState(){
		// Backup state
		long startBackup = System.currentTimeMillis();
		backupState();
		long stopBackup = System.currentTimeMillis();
		System.out.println("Total BACKUP: "+(stopBackup-startBackup));
	}
	
	private void backupState(){
		if(runningOpState != null){
			BackupOperatorState bs = new BackupOperatorState();;
			State toBackup = null;
			int ownerId = runningOpState.getOwnerId();
			int checkpointInterval = runningOpState.getCheckpointInterval();
			long data_ts = runningOpState.getData_ts();
			String stateTag = runningOpState.getStateTag();
			long startmutex = System.currentTimeMillis();
			
			try {
				mutex.acquire();
			} 
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			long startcopy = System.currentTimeMillis();
			toBackup = State.deepCopy(runningOpState, owner.getRuntimeClassLoader());
			long stopcopy = System.currentTimeMillis();
			System.out.println("Deep COPY: "+(stopcopy-startcopy));
			mutex.release();
			
			long stopmutex = System.currentTimeMillis();
			System.out.println("MUTEX: "+(stopmutex-startmutex));
			toBackup.setOwnerId(ownerId);
			toBackup.setCheckpointInterval(checkpointInterval);
			toBackup.setData_ts(data_ts);
			toBackup.setStateTag(stateTag);
			
			bs.setOpId(toBackup.getOwnerId());
			bs.setState(toBackup);
			bs.setStateClass(toBackup.getStateTag());
			
			ControlTuple ctB = new ControlTuple().makeBackupState(bs);
			//Finally send the backup state
			owner.sendBackupState(ctB);
		}
	}
	
//	private void _backupState(){
//		// If there is something to backup...
//		// FIXME: this should anyway be avoided by controlling whether the statebackupworker needs to execute, according to this value
//		if(runningOpState != null){
//			//Create the array of backup states
//			BackupNodeState backupNodeState = new BackupNodeState(owner.getNodeDescr().getNodeId(), mostUpstream.getOperatorId());
//			BackupOperatorState[] backupState = new BackupOperatorState[numberOfStates];
//			
//			// We fill the array with the states
//			for(int i = 0; i < numberOfStates ; i++){
//				State toBackup = null;
//				int ownerId = statesToBackup.get(i).getOwnerId();
//				int checkpointInterval = statesToBackup.get(i).getCheckpointInterval();
//				long data_ts = statesToBackup.get(i).getData_ts();
//				String stateTag = statesToBackup.get(i).getStateTag();
//				
//				long startmutex = System.currentTimeMillis();
//				
//				try {
//					mutex.acquire();
//				} 
//				catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				
//				long startcopy = System.currentTimeMillis();
//				toBackup = State.deepCopy(statesToBackup.get(i), owner.getRuntimeClassLoader());
//				long stopcopy = System.currentTimeMillis();
//				System.out.println("Deep COPY: "+(stopcopy-startcopy));
//				mutex.release();
//				
//				long stopmutex = System.currentTimeMillis();
//				System.out.println("MUTEX: "+(stopmutex-startmutex));
//				toBackup.setOwnerId(ownerId);
//				toBackup.setCheckpointInterval(checkpointInterval);
//				toBackup.setData_ts(data_ts);
//				toBackup.setStateTag(stateTag);
//				
//				BackupOperatorState bs = new BackupOperatorState();
//				// current is null in a parallelised operator
//				bs.setOpId(toBackup.getOwnerId());
//				bs.setState(toBackup);
//				bs.setStateClass(toBackup.getStateTag());
//				backupState[i] = bs;
//			}
//			NodeManager.nLogger.info("-> Backuping the "+backupState.length+" states in this node");
//			//Build the ControlTuple msg
//			backupNodeState.setBackupOperatorState(backupState);
//			
//			ControlTuple ctB = new ControlTuple().makeBackupState(backupNodeState);
//			//Finally send the backup state
//			ctB.getBackupState().getBackupOperatorState()[0].getOpId();
//			owner.sendBackupState(ctB);
//		}
//	}
	
	public void installState(InitOperatorState initOperatorState){
		System.out.println("Installing state: inputqueue size: "+MetricsReader.eventsInputQueue.getCount());
		NodeManager.nLogger.info("Installing state in the operator");
		// Simply replace the state and update operator references
		int stateOwnerId = initOperatorState.getState().getOwnerId();
		System.out.println("This state ownerID is:  "+stateOwnerId);
		State state = initOperatorState.getState();
		// Replace state
		this.runningOpState = state;
		// And reference in operator
		((StatefulOperator)runningOp).replaceState(state);
		System.out.println("END INSTALL state: inputqueue size: "+MetricsReader.eventsInputQueue.getCount());
	}
	
	
		/** Who manages which state? **/
	@Override
	public synchronized void invalidateState(int opId) {
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
	public synchronized void registerManagedState(int opId) {
		//If the state does not figure as being managed, we include it
		if(!listOfManagedStates.contains(opId)){
			NodeManager.nLogger.info("% -> New STATE registered for Operator: "+opId);
			listOfManagedStates.add(opId);
		}
	}
	
	@Override
	public boolean isManagingStateOf(int opId) {
		return listOfManagedStates.contains(opId) ? true : false;
	}
	
	/** Dynamic change of operator information **/
	@Override
	public void addDownstream(int opId, OperatorStaticInformation location){
		// First pick the most downstream operator, and add the downstream to that one
		OperatorContext opContext = runningOp.getOpContext();
		opContext.addDownstream(opId);
		opContext.setDownstreamOperatorStaticInformation(opId, location);
		ctx.configureNewDownstreamCommunication(opId, location);
	}
	
	@Override
	public void addUpstream(int opId, OperatorStaticInformation location){
		// First pick the most upstream operator, and add the upstream to that one
		OperatorContext opContext = runningOp.getOpContext();
		opContext.addUpstream(opId);
		opContext.setUpstreamOperatorStaticInformation(opId, location);
		ctx.configureNewUpstreamCommunication(opId, location);
	}

	@Override
	public void launchMultiCoreMechanism(CoreRE owner, DataStructureAdapter dsa) {
		// Different strategies depending on whether the state is partitionable or not
		int numberOfProcessors = Runtime.getRuntime().availableProcessors();
		int numberOfWorkerThreads = (numberOfProcessors - 2) > 1 ? (numberOfProcessors-2) : 1;
		/** Fixed policy at the moment **/
		numberOfWorkerThreads = 4;
		if(runningOpState instanceof Partitionable){
			pool = Executors.newFixedThreadPool(numberOfWorkerThreads);
			// Populate pool with threads
			for(int i = 0; i<numberOfWorkerThreads; i++){
				pool.execute(new StatefulProcessingWorker(dsa, runningOp, runningOpState));
			}
		}
		else{
			// Lock the state somehow
			
		}
	}

	@Override
	public void disableMultiCoreSupport() {
		multiCoreEnabled = false;
	}
}
