/*******************************************************************************
 * Copyright (c) 2013 Imperial College London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial design and implementation
 *     Martin Rouaux - Removal of upstream and downstream connections
 *     which is required to support scale-in of operators.
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.processingunit;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.buffer.IBuffer;
import uk.ac.imperial.lsds.seep.buffer.OutputBuffer;
import uk.ac.imperial.lsds.seep.comm.serialization.ControlTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.BackupOperatorState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.FailureCtrl;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.InitOperatorState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.StateChunk;
import uk.ac.imperial.lsds.seep.infrastructure.NodeManager;
import uk.ac.imperial.lsds.seep.manet.BackpressureRouter;
import uk.ac.imperial.lsds.seep.operator.EndPoint;
import uk.ac.imperial.lsds.seep.operator.Operator;
import uk.ac.imperial.lsds.seep.operator.OperatorContext;
import uk.ac.imperial.lsds.seep.operator.OperatorStaticInformation;
import uk.ac.imperial.lsds.seep.operator.StatefulOperator;
import uk.ac.imperial.lsds.seep.reliable.ACKWorker;
import uk.ac.imperial.lsds.seep.reliable.FailureCtrlWriter;
import uk.ac.imperial.lsds.seep.reliable.MemoryChunk;
import uk.ac.imperial.lsds.seep.reliable.SerialiserWorker;
import uk.ac.imperial.lsds.seep.reliable.StateBackupWorker;
import uk.ac.imperial.lsds.seep.reliable.StateBackupWorker.CheckpointMode;
import uk.ac.imperial.lsds.seep.reliable.StreamStateManager;
import uk.ac.imperial.lsds.seep.runtimeengine.AsynchronousCommunicationChannel;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE;
import uk.ac.imperial.lsds.seep.runtimeengine.DataStructureAdapter;
import uk.ac.imperial.lsds.seep.runtimeengine.DisposableCommunicationChannel;
import uk.ac.imperial.lsds.seep.runtimeengine.JobBean;
import uk.ac.imperial.lsds.seep.runtimeengine.OutputQueue;
import uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel;
import uk.ac.imperial.lsds.seep.runtimeengine.TimestampTracker;
import uk.ac.imperial.lsds.seep.state.LargeState;
import uk.ac.imperial.lsds.seep.state.MalformedStateChunk;
import uk.ac.imperial.lsds.seep.state.NullChunkWhileMerging;
import uk.ac.imperial.lsds.seep.state.Partitionable;
import uk.ac.imperial.lsds.seep.state.StateWrapper;
import uk.ac.imperial.lsds.seep.state.Streamable;
import uk.ac.imperial.lsds.seep.state.Versionable;

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
	
	final private Logger LOG = LoggerFactory.getLogger(StatefulProcessingUnit.class);

	private CoreRE owner = null;
	
	private PUContext ctx = null;
	private SystemStatus systemStatus = SystemStatus.NORMAL;

	// Mutex between data processing and state backup
	private Semaphore mutex = new Semaphore(1);
	
	private Semaphore executorMutex;

	//Operator and state managed by this processingUnit
	private Operator runningOp = null;
	private StateWrapper runningOpState = null;
	private int minBoundKeySpace = Integer.MIN_VALUE;
	private int maxBoundKeySpace = Integer.MAX_VALUE;

	private OutputQueue outputQueue = null;
	
	private StateBackupWorker sbw;
	private Thread stateWorker;
	private boolean isCheckpointEnabled = true;
	private ArrayList<Integer> listOfManagedStates = new ArrayList<Integer>();
        
        protected ExecutorService poolOfThreads = Executors.newFixedThreadPool( Runtime.getRuntime().availableProcessors()-1 );
	
	//Multi-core support
	private Executor pool;
	private boolean multiCoreEnabled;
	private int numberOfWorkerThreads;
	
	public StatefulProcessingUnit(CoreRE owner, boolean multiCoreEnabled){
		this.owner = owner;
		ctx = new PUContext(owner.getNodeDescr(), owner.getInitialStarTopology());
		this.multiCoreEnabled = multiCoreEnabled;
	}
	
	public void setKeySpaceBounds(int minBound, int maxBound){
		minBoundKeySpace = minBound;
		maxBoundKeySpace = maxBound;
		LOG.debug("New keySpace bounds: ["+minBoundKeySpace+" "+maxBoundKeySpace+"]");
	}
	
	public CheckpointMode getCheckpointMode(){
		return sbw.getCheckpointMode();
	}
	
	public PUContext getPuContext(){
		return ctx;
	}

	public boolean isCheckpointEnabled(){
		return isCheckpointEnabled;
	}
	
	@Override
	public Operator getOperator(){
		return runningOp;
	}
	
	@Override
	public CoreRE getOwner(){
		return owner;
	}

	@Override
	public boolean isNodeStateful(){
		return true;
	}
	
	@Override
	public int getOriginalUpstreamFromOpId(int opId) {
		return runningOp.getOpContext().getOriginalUpstreamFromOpId(opId);
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
		LOG.info("-> Instantiating Stateful Operator...");
		//Detect the first submitted operator
		if(runningOp == null){
			runningOp = o;
		}
		else{
			LOG.warn("-> The operator in this node is being overwritten");
		}
		o.setProcessingUnit(this);
        
		// To identify the monitor with the op id instead of the node id
		NodeManager.monitorSlave.setOperatorId(o.getOperatorId());
		if(o.getStateWrapper() != null){
			runningOpState = o.getStateWrapper();
		}
		else{
			LOG.warn("-> Initial state is null...");
		}
	}

	@Override
	public boolean isOperatorReady(){
		return runningOp.getReady();
	}
	
	@Override
	public void setOpReady(int opId) {
		LOG.debug("-> Setting operator ready");
		runningOp.setReady(true);
	}
	
	@Override
	public PUContext setUpRemoteConnections(){
		ctx.configureOperatorConnections(runningOp);
		return ctx;
	}
	
	public void createAndRunStateBackupWorker(){
		// Create and run state backup worker
		LOG.debug("-> Stateful Node, setting the backup worker thread...");
		///\fixme{fix this mess}
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
			LOG.warn("-> No tuple MAPPER. This is fine as far as I am a SRC");
		}
		return idxMapper;
	}
	
	/** Runtime methods **/
	
	@Override
	public void processData(DataTuple data){
		// Get the mutex		
		if(multiCoreEnabled){
			try {
				executorMutex.acquire(numberOfWorkerThreads);
			}
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// Mutex for data processing
		else{
			try {
				mutex.acquire();
			}
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// TODO: Adjust timestamp of state
		runningOp.processData(data);
		// Release the mutex
		if(multiCoreEnabled){
			executorMutex.release(numberOfWorkerThreads);
		}
		else{
			mutex.release();
		}
	}
	
	@Override
	public void processData(ArrayList<DataTuple> data){
		// Get the mutex
		if(multiCoreEnabled){
			try {
				executorMutex.acquire(numberOfWorkerThreads);
			}
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// Mutex for data processing
		else{
			try {
				mutex.acquire();
			}
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
		// TODO: Adjust timestamp of state
		runningOp.processData(data);
		// Release the mutex		
		if(multiCoreEnabled){
			executorMutex.release(numberOfWorkerThreads);
		}
		else{
			mutex.release();
		}
	}

	@Override
	public void sendData(DataTuple dt, ArrayList<Integer> targets){
		// Here user code (operator) returns from execution, so release mutex		
		if(multiCoreEnabled){
			executorMutex.release(numberOfWorkerThreads);
		}
		else{
			mutex.release();
		}
		
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
					outputQueue.sendToDownstream(dt, dest);
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
		if(multiCoreEnabled){
			try {
				executorMutex.acquire(numberOfWorkerThreads);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		// Mutex for data processing
		else{
			try {
				mutex.acquire();
			}
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
        
        @Override
        public void sendDataByThreadPool(DataTuple dt, ArrayList<Integer> targets){
        
            int numTargets = targets.size();
            final ArrayList<Integer> targetsCopy =  new ArrayList<>(targets);
            List<Callable<Object>> taskList = new ArrayList<>(numTargets);
            
            for (int i = 0; i < numTargets; i++) {
                
                final int j = i;
                final int target = targetsCopy.get(j);
                final DataTuple dtCopy = dt; 
                
                taskList.add(new Callable() {
                    public Object call() throws Exception {
                        
                        EndPoint dest = ctx.getDownstreamTypeConnection().elementAt(target);

                        // REMOTE ASYNC
                        if (dest instanceof AsynchronousCommunicationChannel) {
                            ((AsynchronousCommunicationChannel) dest).writeDataToOutputBuffer(dtCopy);
                        } // REMOTE SYNC
                        
                        else if (dest instanceof SynchronousCommunicationChannel) {
                            ///\fixme{do some proper thing with var now}
                            outputQueue.sendToDownstream(dtCopy, dest);
                            //System.out.println("Send to: "+dest.toString());
                        } // LOCAL
                        
                        else if (dest instanceof Operator) {
                            Operator operatorObj = (Operator) dest;
                            operatorObj.processData(dtCopy);
                        }
                        
                        return null;
                    }
                });
            }
            
            try {
                poolOfThreads.invokeAll(taskList);
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(StatelessProcessingUnit.class.getName()).log(Level.SEVERE, null, ex);
            }
            
       
        }
        
        @Override
        public void sendPartitionedData(DataTuple[] dts, ArrayList<Integer> targets){
            
            //Send each data tuple to its corresponding target
            
            int numTargets = targets.size();
            final ArrayList<Integer> targetsCopy =  new ArrayList<>(targets);
            List<Callable<Object>> taskList = new ArrayList<>(numTargets);
            
            for (int i = 0; i < numTargets; i++) {
                
                final int j = i;
                final int target = targetsCopy.get(j);
                final DataTuple dtCopy = dts[i]; 
                
                taskList.add(new Callable() {
                    public Object call() throws Exception {
                        
                        EndPoint dest = ctx.getDownstreamTypeConnection().elementAt(target);

                        // REMOTE ASYNC
                        if (dest instanceof AsynchronousCommunicationChannel) {
                            ((AsynchronousCommunicationChannel) dest).writeDataToOutputBuffer(dtCopy);
                        } // REMOTE SYNC
                        
                        else if (dest instanceof SynchronousCommunicationChannel) {
                            ///\fixme{do some proper thing with var now}
                            outputQueue.sendToDownstream(dtCopy, dest);
                            //System.out.println("Send to: "+dest.toString());
                        } // LOCAL
                        
                        else if (dest instanceof Operator) {
                            Operator operatorObj = (Operator) dest;
                            operatorObj.processData(dtCopy);
                        }
                        
                        return null;
                    }
                });
            }
            
            try {
                poolOfThreads.invokeAll(taskList);
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(StatelessProcessingUnit.class.getName()).log(Level.SEVERE, null, ex);
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
		//if(runningOp.getOperatorId() == opId){
			ctx.updateConnection(opId, runningOp, ip);
		//}
//		else{
//			NodeManager.nLogger.warning("-> This node does not contain the requested operator: "+opId);
//		}
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
		TimestampTracker tsVToAck = backupState();
		if(tsVToAck != null){
			owner.ack(tsVToAck);
		}
	}
	
	public void lockFreeParallelCheckpointAndBackupState(){
		TimestampTracker tsVToAck = lockFreeParallelBackupState();
		if(tsVToAck != null){
			owner.ack(tsVToAck);
		}
	}
	
	private TimestampTracker lockFreeParallelBackupState(){
		// Initial checks
		if(runningOpState == null){
			LOG.error("-> NULL state");
			System.exit(-666);
			return null;
		}
		if(!(runningOpState instanceof LargeState)){
			LOG.error("-> Not Large STATE. wrong method");
			System.exit(-666);
			return null;
		}
		
		TimestampTracker incomingTT = null;
		
		int opId = runningOpState.getOwnerId();
		ArrayList<OutputBuffer> outputBuffers = ctx.getOutputBuffers(); // copy of these buffers?
		// Set ts for consistency, etc...
		incomingTT = owner.getIncomingTT();
		///\todo{this assignment should go with the chunks ?? }
		runningOpState.setData_ts(incomingTT);
		((Versionable)runningOpState).setSnapshotMode(true); //((Versionable)vns).setSnapshotMode(true);
		StreamStateManager ssm = null;
		// Create a manager for stream the state
		if(((Streamable)runningOpState).getSize() > 0){
			ssm = new StreamStateManager((Streamable)runningOpState); //StreamStateManager ssm = new StreamStateManager(((Streamable)vns));
		}
		else{
			LOG.warn("State size is 0, so no backup is necessary ?");
			return incomingTT;
		}
		
		// Get and send all chunks
		MemoryChunk mc = null;
		int sequenceNumber = 0;
		
		// Size of start topology
		int sizeST = ctx.getStarTopologySize();
		int index = 0;
		int splittingKey = (int)(maxBoundKeySpace - minBoundKeySpace)/2;
		int keeperOpId = -666; // fake id since this value is up to this point empty
		
		long total = 0;
		/** Single thread **/
//		while((mc = ssm.getChunk()) != null){
//			ControlTuple chunkMessage = new ControlTuple().makeStateChunk(opId, keeperOpId, sequenceNumber, ssm.getTotalNumberChunks(), mc, splittingKey);
//			sequenceNumber++;
//			int idx = index % sizeST;
//			long start = System.currentTimeMillis();
//			owner.sendBlindData(chunkMessage, idx);
//			long stop = System.currentTimeMillis();
//			total += (stop-start);
//			index++;
//		}
		
		/** Worker pool **/
		ArrayBlockingQueue<JobBean> jobQueue = new ArrayBlockingQueue<JobBean>(4);
		SerialiserWorker s1 = new SerialiserWorker(jobQueue);
		SerialiserWorker s2 = new SerialiserWorker(jobQueue);
		Thread t1 = new Thread(s1);
		t1.setName("S1");
		Thread t2 = new Thread(s2);
		t2.setName("S2");
		t1.start();
		t2.start();
		int counter = 0;
		while((mc = ssm.getChunk()) != null){
			if(mc.chunk == null){ // -> rather make sure state chunks are small enough
				System.out.println("mc.chunk is null. Continuing... %%");
				continue;
			}
			ControlTuple chunkMessage = new ControlTuple().makeStateChunk(opId, keeperOpId, sequenceNumber, ssm.getTotalNumberChunks(), mc, splittingKey);
			sequenceNumber++;
			int idx = index % sizeST;
			InetAddress ip_endpoint = ((DisposableCommunicationChannel)ctx.getStarTopology().get(idx)).getIp();
			JobBean jb = new JobBean(ip_endpoint, chunkMessage);
			System.out.println("chunk: "+counter);
			counter++;
//			jobQueue.offer(jb);
			try {
				jobQueue.put(jb);
			} 
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			index++;
		}
		JobBean jb = new JobBean(null, null);
		try {
			jobQueue.put(jb);
		} 
		catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
//		while(jobQueue.size() > 0);
//		s1.killThread();
//		s2.killThread();
		try {
			t1.join();
			t2.join();
		}
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		long startR = System.currentTimeMillis();
		//((Versionable)runningOpState).reconcile();
		((Versionable)((LargeState)runningOpState).getVersionableAndStreamableState()).reconcile();
		long stopR = System.currentTimeMillis();
		System.out.println("MSG SENT: "+sequenceNumber);
		System.out.println("Total TIME SEND: "+total);
//		System.out.println("TOTAL SEQ NUMER: "+ssm.getTotalNumberChunks());
		System.out.println("TOTAL RECONCILIATION TIME: "+(stopR-startR));
		
		return incomingTT;
	}
	
	private TimestampTracker backupState(){
		TimestampTracker incomingTT = null;
		if(runningOpState != null){
			BackupOperatorState bs = new BackupOperatorState();
			StateWrapper toBackup = null;
			int ownerId = runningOpState.getOwnerId();
			int checkpointInterval = runningOpState.getCheckpointInterval();
			TimestampTracker data_ts = runningOpState.getData_ts();
			String stateTag = runningOpState.getStateTag();
			long startmutex = System.currentTimeMillis();
			
			// Mutex for executor (in case multicore)
			if(multiCoreEnabled){
				try {
					executorMutex.acquire(numberOfWorkerThreads);
				}
				catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// Mutex for data processing
			else{
				
				try {
					mutex.acquire();
				}
				catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			incomingTT = owner.getIncomingTT();
			
			long startcopy = System.currentTimeMillis();
			
			toBackup = StateWrapper.deepCopy(runningOpState, owner.getRuntimeClassLoader());
			ArrayList<OutputBuffer> outputBuffers = ctx.getOutputBuffers();
			
			long stopcopy = System.currentTimeMillis();
			System.out.println("% Deep COPY: "+(stopcopy-startcopy));
			if(multiCoreEnabled){
				executorMutex.release(numberOfWorkerThreads);
			}
			else{
				mutex.release();
			}
			
			long stopmutex = System.currentTimeMillis();
			System.out.println("% mutex: "+(stopmutex-startmutex));
			toBackup.setOwnerId(ownerId);
			toBackup.setCheckpointInterval(checkpointInterval);
			toBackup.setData_ts(incomingTT);
			toBackup.setStateTag(stateTag);
			
			bs.setOpId(toBackup.getOwnerId());
			bs.setState(toBackup);
			bs.setOutputBuffers(outputBuffers);
			bs.setStateClass(toBackup.getStateTag());
			
			ControlTuple ctB = new ControlTuple().makeBackupState(bs);
			//Finally send the backup state
			owner.sendBackupState(ctB);
		}
		return incomingTT;
	}
	
	public void installState(InitOperatorState initOperatorState){
//		System.out.println("Installing state: inputqueue size: "+MetricsReader.eventsInputQueue.getCount());
		// Simply replace the state and update operator references
		int stateOwnerId = initOperatorState.getState().getOwnerId();
		LOG.info("Installing state (whom owner is {}) in the operator");
		StateWrapper state = initOperatorState.getState();
		// Replace state
		this.runningOpState = state;
		// And reference in operator
		((StatefulOperator)runningOp).replaceState(state);
//		System.out.println("END INSTALL state: inputqueue size: "+MetricsReader.eventsInputQueue.getCount());
	}
	
	public synchronized void mergeChunkToState(StateChunk chunk){
		try{
			if(chunk == null){
				LOG.info("Finished recreating state, current size: {}", ((LargeState)runningOpState).getSize());
				return;
			}
			MemoryChunk mc = chunk.getMemoryChunk();
			//	((Streamable)((LargeState)runningOpState).getVersionableAndStreamableState()).appendChunk(mc.chunk);
			((LargeState)runningOpState).appendChunk(mc.chunk);
		}
		catch(NullChunkWhileMerging ncwm){
			ncwm.printStackTrace();
		} 
		catch (MalformedStateChunk e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		IBuffer b = ctx.getBuffer(opId);
		if(b != null){
			//First of all, we empty the buffer
			b.replaceBackupOperatorState(null);
		}
	}
	
	@Override
	public synchronized void registerManagedState(int opId) {
		//If the state does not figure as being managed, we include it
		if(!listOfManagedStates.contains(opId)){
			LOG.info("-> New STATE registered for Operator: {}", opId);
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
	
    /**
     * Removes downstream operator from the processing unit (and indirectly,
     * from the context of the currently running operator).
     * @param opId Operator identifier
     */
    @Override
    public void removeDownstream(int opId) {
        OperatorContext opContext = runningOp.getOpContext();
		opContext.removeDownstream(opId);
    }
    
	@Override
	public void addUpstream(int opId, OperatorStaticInformation location){
		OperatorContext opContext = runningOp.getOpContext();
		opContext.addUpstream(opId);
		opContext.setUpstreamOperatorStaticInformation(opId, location);
		ctx.configureNewUpstreamCommunication(opId, location);
	}

    /**
     * Removes upstream operator from the processing unit (and indirectly,
     * from the context of the currently running operator).
     * @param opId Operator identifier
     */
    @Override
    public void removeUpstream(int opId) {
    	OperatorContext opContext = runningOp.getOpContext();
		opContext.removeUpstream(opId);
	}
    
	@Override
	public void launchMultiCoreMechanism(CoreRE owner, DataStructureAdapter dsa) {
		// Different strategies depending on whether the state is partitionable or not
		int numberOfProcessors = Runtime.getRuntime().availableProcessors();
		numberOfWorkerThreads = (numberOfProcessors - 2) > 1 ? (numberOfProcessors-2) : 1;
		///\fixme{It's fine this for now, but find a formula such the previuos lines to establish the best number of Cores} 
		numberOfWorkerThreads = 4;
		if(runningOpState instanceof Partitionable){
			executorMutex = new Semaphore(numberOfWorkerThreads, true);
			pool = Executors.newFixedThreadPool(numberOfWorkerThreads);
			// Populate pool with threads
			for(int i = 0; i<numberOfWorkerThreads; i++){
				pool.execute(new StatefulProcessingWorker(dsa, runningOp, runningOpState, executorMutex));
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

	@Override
	public void createAndRunAckWorker() {
		ACKWorker ackWorker = new ACKWorker(this); 
		Thread ackT = new Thread(ackWorker);
		ackT.start();
	}

	@Override
	public void createAndRunFailureCtrlWriter()
	{
		FailureCtrlWriter fctrlWriter = new FailureCtrlWriter(this);
		Thread fctrlT = new Thread(fctrlWriter);
		fctrlT.start();
	}
	
	@Override
	public TimestampTracker getLastACK() {
		return owner.getIncomingTT();
	}

	@Override
	public void emitACK(TimestampTracker currentTs) {
		owner.ack(currentTs);
	}

	@Override
	public void emitFailureCtrl(FailureCtrl nodeFctrl, boolean downstreamsRoutable) {
		owner.writeFailureCtrls(getOperator().getOpContext().getListOfUpstreamIndexes(), nodeFctrl, downstreamsRoutable);
	}
	
	public void resetState() {
		//((Streamable)((LargeState)runningOpState).getVersionableAndStreamableState()).reset();
		((Streamable)runningOpState).reset();
	}

	@Override
	public int getOpIdFromUpstreamIp(InetAddress ip) {
		return runningOp.getOpContext().getOpIdFromUpstreamIp(ip);
	}

	@Override
	public int getOpIdFromUpstreamIpPort(InetAddress ip, int port) {
		return runningOp.getOpContext().getOpIdFromUpstreamIpPort(ip, port);
	}

	@Override
	public void setOutputQueueList(ArrayList<OutputQueue> downOpId_outputQ_map) {
		throw new RuntimeException("TODO");
		
	}

	@Override
	public PUContext getPUContext() {
		throw new RuntimeException("TODO");
	}

	@Override
	public Dispatcher getDispatcher() {
		throw new RuntimeException("TODO");
	}

	@Override
	public void sendDataDispatched(DataTuple dt) {
		throw new RuntimeException("TODO");
		
	}
	@Override
	public void ack(DataTuple dt) { throw new RuntimeException("TMP"); }

}
