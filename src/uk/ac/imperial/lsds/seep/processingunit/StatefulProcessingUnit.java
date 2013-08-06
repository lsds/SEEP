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
package uk.ac.imperial.lsds.seep.processingunit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import uk.ac.imperial.lsds.seep.comm.serialization.ControlTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.BackupOperatorState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.InitOperatorState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.RawData;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.StateChunk;
import uk.ac.imperial.lsds.seep.infrastructure.NodeManager;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.MetricsReader;
import uk.ac.imperial.lsds.seep.operator.EndPoint;
import uk.ac.imperial.lsds.seep.operator.Operator;
import uk.ac.imperial.lsds.seep.operator.OperatorContext;
import uk.ac.imperial.lsds.seep.operator.OperatorStaticInformation;
import uk.ac.imperial.lsds.seep.operator.Partitionable;
import uk.ac.imperial.lsds.seep.operator.State;
import uk.ac.imperial.lsds.seep.operator.StatefulOperator;
import uk.ac.imperial.lsds.seep.reliable.ACKWorker;
import uk.ac.imperial.lsds.seep.reliable.StateBackupWorker;
import uk.ac.imperial.lsds.seep.runtimeengine.AsynchronousCommunicationChannel;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE;
import uk.ac.imperial.lsds.seep.runtimeengine.DataStructureAdapter;
import uk.ac.imperial.lsds.seep.runtimeengine.OutputQueue;
import uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel;
import uk.ac.imperial.lsds.seep.runtimeengine.TimestampTracker;
import uk.ac.imperial.lsds.seep.utils.dynamiccodedeployer.ExtendedObjectOutputStream;

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
	
	private Semaphore executorMutex;

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
	private boolean multiCoreEnabled;
	private int numberOfWorkerThreads;
	
	public StatefulProcessingUnit(CoreRE owner, boolean multiCoreEnabled){
		this.owner = owner;
		ctx = new PUContext(owner.getNodeDescr());
		this.multiCoreEnabled = multiCoreEnabled;
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
//		try {
//			mutex.acquire();
//		} 
//		catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
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
		
		// Instrumentation
		//MetricsReader.eventsPerSecond.mark();
		MetricsReader.eventsProcessed.inc();
		// TODO: Adjust timestamp of state
		runningOp.processData(data);
		// Release the mutex
//		mutex.release();
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
//		try {
//			mutex.acquire();
//		} 
//		catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
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
		
		// Instrumentation
//		MetricsReader.eventsPerSecond.mark();
		
		MetricsReader.eventsProcessed.inc();
		// TODO: Adjust timestamp of state
		runningOp.processData(data);
		// Release the mutex
//		mutex.release();
		
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
//		mutex.release();
		
		if(multiCoreEnabled){
			executorMutex.release(numberOfWorkerThreads);
		}
		else{
			mutex.release();
		}
		
		for(int i = 0; i<targets.size(); i++){
			int target = targets.get(i);
			try{
//System.out.println("SEND TO: "+target+" SIZE: "+ctx.getDownstreamTypeConnection().size()+" targetSize: "+targets.size());
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
		long tsToAck = backupState();
		if(tsToAck != -1){
			owner.ack(tsToAck);
		}
	}
	
	public void directCheckpointAndBackupState(){
		long tsToAck = directBackupState();
		if(tsToAck != -1){
			owner.ack(tsToAck);
		}
	}
	
	public void directParallelCheckpointAndBackupState(){
		long tsToAck = directParallelBackupState();
		if(tsToAck != -1){
			owner.ack(tsToAck);
		}
	}
	
	public void blindCheckpointAndBackupState(){
		long tsToAck = blindBackupState();
		if(tsToAck != -1){
			owner.ack(tsToAck);
		}
	}
	
	public void blindParallelCheckpointAndBackupState(){
		long tsToAck = blindParallelBackupState();
		if(tsToAck != -1){
			owner.ack(tsToAck);
		}
	}
	
	public void lockFreeParallelCheckpointAndBackupState(int[] partitioningRange){
		long tsToAck = lockFreeParallelBackupState(partitioningRange);
		if(tsToAck != -1){
			owner.ack(tsToAck);
		}
	}
	
	private TimestampTracker lockFreeParallelBackupState(int[] partitioningRange){
		long last_data_proc = -1;
		TimestampTracker incomingTT = null;
		if(runningOpState != null){
			int opId = runningOpState.getOwnerId();
			String stateTag = runningOpState.getStateTag();
			// Set dirty mode (lock free)
			((Partitionable)runningOpState).setDirtyMode(true);
			
			// Set ts for consistency, etc...
			incomingTT = owner.getIncomingTT();
//			last_data_proc = owner.getTsData();
//			runningOpState.setData_ts(last_data_proc);
			runningOpState.setData_ts(incomingTT);

			// STREAMING THROUGH THE NETWORK (enforcing constant memory consumption here)
			ArrayList<Object> microBatch;
			int it = 0;
			int size = ((Partitionable)runningOpState).getSize();
//			System.out.println("SIZE of state to stream is: "+size);
			int sequenceNumber = 0;
			NodeManager.nLogger.info("% -> Backuping state with owner: "+opId);
			
			int totalChunks = ((Partitionable)runningOpState).getTotalNumberOfChunks();
			((Partitionable)runningOpState).setUpIterator();
			//while(it < size){
			int key = (partitioningRange[1]+partitioningRange[0])/2;
			while(((Partitionable)runningOpState).getIterator().hasNext()){
				
				StreamData sd = ((Partitionable)runningOpState).streamSplitState(runningOpState, it, key);
				// finished
				if(sd == null){
					StreamData[] chunks = ((Partitionable)runningOpState).getRemainingData();
					microBatch = chunks[0].microBatch;
					ArrayList<Integer> newPartitioningRange0 = new ArrayList<Integer>();
					newPartitioningRange0.add(partitioningRange[0]);
					newPartitioningRange0.add(key);
					ControlTuple ctB = new ControlTuple().makeStateChunk(opId, chunks[0].partition, sequenceNumber, totalChunks, new StreamStateChunk(microBatch), newPartitioningRange0);
					sequenceNumber++;
					owner.sendBlindData(ctB, chunks[0].partition);
//					owner._sendBlindData(ctB, chunks[0].partition, chunks[0].partition);
					((Partitionable)runningOpState).resetStructures(chunks[0].partition);
//					System.out.println("f");
					
					microBatch = chunks[1].microBatch;
					ArrayList<Integer> newPartitioningRange1 = new ArrayList<Integer>();
					newPartitioningRange1.add(partitioningRange[0]);
					newPartitioningRange1.add(key);
					ControlTuple ctB2 = new ControlTuple().makeStateChunk(opId, chunks[1].partition, sequenceNumber, totalChunks, new StreamStateChunk(microBatch), newPartitioningRange1);
					sequenceNumber++;
					owner.sendBlindData(ctB2, chunks[1].partition);
//					owner._sendBlindData(ctB2, chunks[1].partition, chunks[1].partition);
					((Partitionable)runningOpState).resetStructures(chunks[1].partition);
//					System.out.println("f");
					break;
				}
				microBatch = sd.microBatch;
				it = it + microBatch.size();
				
				// Configure new partitioningRange
				ArrayList<Integer> newPartitioningRange = new ArrayList<Integer>();
				if(sd.partition == 0){
					newPartitioningRange.add(partitioningRange[0]);
					newPartitioningRange.add(key);
				}
				else if(sd.partition == 1){
					newPartitioningRange.add(key);
					newPartitioningRange.add(partitioningRange[1]);
				}
				ControlTuple ctB = new ControlTuple().makeStateChunk(opId, sd.partition, sequenceNumber, totalChunks, new StreamStateChunk(microBatch), newPartitioningRange);
				sequenceNumber++;
				owner.sendBlindData(ctB, sd.partition);
//				owner._sendBlindData(ctB, sd.partition, sd.partition);
				((Partitionable)runningOpState).resetStructures(sd.partition);
//				System.out.println("n");
			}
			// We inform our CORE of the number of chunks of the last backup
			owner.setTotalNumberOfStateChunks(sequenceNumber);
			
			// We reconcile the dirty state with the previous state. Has to always be last op since changes dirtyMode
			long startR = System.currentTimeMillis();
			((Partitionable)runningOpState).reconcile();
			long stopR = System.currentTimeMillis();
//			System.out.println("STREAMED: "+it+" size? "+size);
			System.out.println("MSG SENT: "+sequenceNumber);
			System.out.println("TOTAL SEQ NUMER: "+totalChunks);
			System.out.println("TOTAL RECONCILIATION TIME: "+(stopR-startR));
		}
//		return last_data_proc;
		return incomingTT;
	}
	
	
	
	private TimestampTracker blindParallelBackupState(){
//		long last_data_proc = -1;
		TimestampTracker incomingTT = null;
		if(runningOpState != null){
			BackupOperatorState bs0 = new BackupOperatorState();
			BackupOperatorState bs1 = new BackupOperatorState();
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
			
//			last_data_proc = owner.getTsData();
//			runningOpState.setData_ts(last_data_proc);
			incomingTT = owner.getIncomingTT();
			runningOpState.setData_ts(incomingTT);
			
			// change 0 for proper key
			long a = System.currentTimeMillis();
			State[] partitions = ((Partitionable)runningOpState).splitState(runningOpState, 0);
			long b = System.currentTimeMillis();
			System.out.println("partitioning time: "+(b-a));
			
			bs0.setOpId(runningOpState.getOwnerId());
			bs0.setState(partitions[0]);
//			bs0.setState(runningOpState);
			bs0.setStateClass(runningOpState.getStateTag());
			
			bs1.setOpId(runningOpState.getOwnerId());
			bs1.setState(partitions[1]);
			bs1.setStateClass(runningOpState.getStateTag());
			
			ControlTuple ctB = new ControlTuple().makeBackupState(bs0);
			ControlTuple ctB2 = new ControlTuple().makeBackupState(bs1);
			
			/** A-B **/
//			owner.sendBlindData(ctB);
			
			if(multiCoreEnabled){
				executorMutex.release(numberOfWorkerThreads);
			}
			else{
				mutex.release();
			}
			// fake argument
			int hint = 0;
			owner.sendBlindData(ctB, hint);
			bs0 = null;
			bs1 = null;
			ctB = null;
			ctB2 = null;
//			owner.sendBlindData(ctB2);
		}
		//return last_data_proc;
		return incomingTT;
	}
	
	private TimestampTracker blindBackupState(){
//		long last_data_proc = -1;
		TimestampTracker incomingTT = null;
		if(runningOpState != null){
			RawData rw = new RawData();
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

//			last_data_proc = owner.getTsData();
//			rw.setTs(last_data_proc);
			incomingTT = owner.getIncomingTT();
			rw.setOpId(runningOpState.getOwnerId());
			byte[] rawData = toRawData(runningOpState);
			rw.setData(rawData);
		
			ControlTuple ctB = new ControlTuple().makeRawData(rw);
			owner.sendRawData(ctB);

			if(multiCoreEnabled){
				executorMutex.release(numberOfWorkerThreads);
			}
			else{
				mutex.release();
			}
		}
//		return last_data_proc;
		return incomingTT;
	}
	
	private byte[] toRawData(State s){
		byte[] data = null;
		try {
	    	// Write the object out to a byte array
	        ByteArrayOutputStream bos = new ByteArrayOutputStream(1000000);
	        ExtendedObjectOutputStream out = new ExtendedObjectOutputStream(bos);
	        synchronized(s){
	        	out.writeObject(s);
	        	out.flush();
	        	out.close();
	        }
	        // Make an input stream from the byte array and read
	        // a copy of the object back in.
	        data = bos.toByteArray();
	        System.out.println("SER SIZE: "+data.length+" bytes");
	    }
	    catch(IOException e) {
	    	e.printStackTrace();
	    }
	    return data;
	}
	
	private TimestampTracker directBackupState(){
//		long last_data_proc = -1;
		TimestampTracker incomingTT = null;
		if(runningOpState != null){
			BackupOperatorState bs = new BackupOperatorState();
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
//			last_data_proc = owner.getTsData();
//			runningOpState.setData_ts(last_data_proc);
			incomingTT = owner.getIncomingTT();
			runningOpState.setData_ts(incomingTT);
			bs.setOpId(runningOpState.getOwnerId());
			bs.setState(runningOpState);
			bs.setStateClass(runningOpState.getStateTag());
			
			ControlTuple ctB = new ControlTuple().makeBackupState(bs);			
			owner.sendBackupState(ctB);
			
			if(multiCoreEnabled){
				executorMutex.release(numberOfWorkerThreads);
			}
			else{
				mutex.release();
			}
		}
//		return last_data_proc;
		return incomingTT;
	}
	
	private TimestampTracker directParallelBackupState(){
//		long last_data_proc = -1;
		TimestampTracker incomingTT = null;
		if(runningOpState != null){
			BackupOperatorState bs0 = new BackupOperatorState();
			BackupOperatorState bs1 = new BackupOperatorState();
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
//			last_data_proc = owner.getTsData();
//			runningOpState.setData_ts(last_data_proc);
			incomingTT = owner.getIncomingTT();
			runningOpState.setData_ts(incomingTT);
			// change 0 for proper key
long a = System.currentTimeMillis();
			State[] partitions = ((Partitionable)runningOpState).splitState(runningOpState, 0);
long b = System.currentTimeMillis();
System.out.println("partitioning time: "+(b-a));
			bs0.setOpId(runningOpState.getOwnerId());
			bs0.setState(partitions[0]);
			bs0.setStateClass(runningOpState.getStateTag());
			
			bs1.setOpId(runningOpState.getOwnerId());
			bs1.setState(partitions[1]);
			bs1.setStateClass(runningOpState.getStateTag());
			
			ControlTuple ctB = new ControlTuple().makeBackupState(bs0);
			ControlTuple ctB2 = new ControlTuple().makeBackupState(bs1);
			owner.sendBackupState(ctB);
			
			if(multiCoreEnabled){
				executorMutex.release(numberOfWorkerThreads);
			}
			else{
				mutex.release();
			}
		}
//		return last_data_proc;
		return incomingTT;
	}
	
	private TimestampTracker backupState(){
		TimestampTracker incomingTT = null;
//		long last_data_proc = -1;
		if(runningOpState != null){
			BackupOperatorState bs = new BackupOperatorState();
			State toBackup = null;
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
//			last_data_proc = owner.getTsData();
			incomingTT = owner.getIncomingTT();
			
			long startcopy = System.currentTimeMillis();
			
			toBackup = State.deepCopy(runningOpState, owner.getRuntimeClassLoader());
			
//			toBackup = (State) owner.getControlDispatcher().deepCopy(runningOpState);
			
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
//			toBackup.setData_ts(last_data_proc);
			toBackup.setData_ts(incomingTT);
			toBackup.setStateTag(stateTag);
			
			bs.setOpId(toBackup.getOwnerId());
			bs.setState(toBackup);
			bs.setStateClass(toBackup.getStateTag());
			
			ControlTuple ctB = new ControlTuple().makeBackupState(bs);
			//Finally send the backup state
			owner.sendBackupState(ctB);
		}
//		return last_data_proc;
		return incomingTT;
	}
	
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
	
	
	public void mergeChunkToState(StateChunk chunk){
		if(chunk == null){
			((Partitionable)runningOpState).appendChunk(null);
			return;
		}
		State s = chunk.getState();
		((Partitionable)runningOpState).appendChunk(s);
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
		OperatorContext opContext = runningOp.getOpContext();
		opContext.addUpstream(opId);
		opContext.setUpstreamOperatorStaticInformation(opId, location);
		ctx.configureNewUpstreamCommunication(opId, location);
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
	public long getLastACK() {
//		return owner.getTsData();
		///\todo{check this is correct}
		return owner.getTs_ack();
	}

	@Override
	public void emitACK(long currentTs) {
		owner.ack(currentTs);
	}

	public void resetState() {
		((Partitionable)runningOpState).resetState();
		
	}

	public void configureNewPartitioningRange(
			ArrayList<Integer> partitioningRange) {
		sbw.setPartitioningRange(partitioningRange);
	}
	
	public ArrayList<Integer> getPartitioningRange(){
		return sbw.getPartitioningRange();
	}

	@Override
	public int getOpIdFromUpstreamIp(InetAddress ip) {
		return runningOp.getOpContext().getOpIdFromUpstreamIp(ip);
	}
}
