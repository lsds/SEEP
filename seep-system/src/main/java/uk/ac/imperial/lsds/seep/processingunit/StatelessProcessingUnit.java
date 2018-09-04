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
 *     which is required to support scale-in of operators. Scaling metrics.
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.processingunit;

import com.codahale.metrics.Timer;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.buffer.IBuffer;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.FailureCtrl;
import uk.ac.imperial.lsds.seep.infrastructure.NodeManager;
import static uk.ac.imperial.lsds.seep.infrastructure.monitor.slave.reader.DefaultMetricsNotifier.notifyThat;
import uk.ac.imperial.lsds.seep.manet.BackpressureRouter;
import uk.ac.imperial.lsds.seep.manet.Query;
import uk.ac.imperial.lsds.seep.manet.RoundRobinRouter;
import uk.ac.imperial.lsds.seep.manet.ShortestPathRouter;
import uk.ac.imperial.lsds.seep.manet.WeightedRoundRobinRouter;
import uk.ac.imperial.lsds.seep.manet.HashRouter;
import uk.ac.imperial.lsds.seep.manet.PowerOf2ChoicesRouter;
import uk.ac.imperial.lsds.seep.manet.BroadcastRouter;
import uk.ac.imperial.lsds.seep.operator.EndPoint;
import uk.ac.imperial.lsds.seep.operator.Operator;
import uk.ac.imperial.lsds.seep.operator.OperatorContext;
import uk.ac.imperial.lsds.seep.operator.OperatorStaticInformation;
import uk.ac.imperial.lsds.seep.reliable.ACKWorker;
import uk.ac.imperial.lsds.seep.reliable.FailureCtrlWriter;
import uk.ac.imperial.lsds.seep.runtimeengine.AsynchronousCommunicationChannel;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE;
import uk.ac.imperial.lsds.seep.runtimeengine.DataStructureAdapter;
import uk.ac.imperial.lsds.seep.runtimeengine.OutputQueue;
import uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel;
import uk.ac.imperial.lsds.seep.runtimeengine.TimestampTracker;

public class StatelessProcessingUnit implements IProcessingUnit {
	
	final private Logger LOG = LoggerFactory.getLogger(StatelessProcessingUnit.class);

	private CoreRE owner;
	private PUContext ctx;
	
	private SystemStatus systemStatus = SystemStatus.NORMAL;
	
	private Operator runningOp = null;
	
	private ArrayList<Integer> listOfManagedStates = new ArrayList<Integer>();
	private ArrayList<OutputQueue> outputQueues;
        
    private ExecutorService poolOfThreads = Executors.newFixedThreadPool( Math.max(1,Runtime.getRuntime().availableProcessors()-1) );
        	
	//Multi-core support
	private Executor pool;
	private boolean multiCoreEnabled;
	
	private Dispatcher dispatcher = null;
	//private BackpressureRouter bpRouter = null;
	
	public StatelessProcessingUnit(CoreRE owner, boolean multiCoreEnabled){
		this.owner = owner;
		ctx = new PUContext(owner.getNodeDescr(), owner.getInitialStarTopology());
		this.multiCoreEnabled = multiCoreEnabled;
	}

    public PUContext getPUContext() {
        return ctx;
    }
	
	@Override
	public void addDownstream(int opId, OperatorStaticInformation location) {
		// First pick the most downstream operator, and add the downstream to that one
		OperatorContext opContext = runningOp.getOpContext();
		opContext.addDownstream(opId);
		opContext.setDownstreamOperatorStaticInformation(opId, location);
		ctx.configureNewDownstreamCommunication(opId, location);
	}

    @Override
    public void removeDownstream(int opId) {
        OperatorContext opContext = runningOp.getOpContext();
        opContext.removeDownstream(opId);
    }
    
	@Override
	public void addUpstream(int opId, OperatorStaticInformation location) {
		// First pick the most upstream operator, and add the upstream to that one
		OperatorContext opContext = runningOp.getOpContext();
		opContext.addUpstream(opId);
		opContext.setUpstreamOperatorStaticInformation(opId, location);
		ctx.configureNewUpstreamCommunication(opId, location);
	}

    @Override
    public void removeUpstream(int opId) {
        OperatorContext opContext = runningOp.getOpContext();
        opContext.removeUpstream(opId);
    }
    
	@Override
	public Map<String, Integer> createTupleAttributeMapper() {
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

	@Override
	public Operator getOperator() {
		return runningOp;
	}
	
	@Override
	public Dispatcher getDispatcher()
	{
		return dispatcher;
	}
	
	/*
	@Override
	public BackpressureRouter getBackpressureRouter()
	{
		return bpRouter;
	}
	*/
	
	@Override
	public CoreRE getOwner(){
		return owner;
	}

	@Override
	public SystemStatus getSystemStatus() {
		return systemStatus;
	}
	
	public boolean isMultiCoreEnabled(){
		return multiCoreEnabled;
	}

	@Override
	public void initOperator() {
		runningOp.setUp();
	}

	@Override
	public void invalidateState(int opId) {
		System.out.println("% We will invalidate management of state for: "+opId);
		//If the states figures as being managed we removed it
		int index = 0;
		if((index = listOfManagedStates.indexOf(opId)) != -1){
			System.out.println("% I was managing state for OP: "+opId+" NOT ANYMORE");
			listOfManagedStates.remove(index);
		}
		// and then we clean both the buffer and the mapping in downstreamBuffers.
//		if(PUContext.downstreamBuffers.get(opId) != null){
		IBuffer b = ctx.getBuffer(opId);
		if(b != null){
			//First of all, we empty the buffer
			b.replaceBackupOperatorState(null);
		}
	}

	@Override
	public boolean isManagingStateOf(int opId) {
		return listOfManagedStates.contains(opId) ? true : false;
	}

	@Override
	public boolean isNodeStateful() {
		return false;
	}
	
	@Override
	public int getOriginalUpstreamFromOpId(int opId) {
		return runningOp.getOpContext().getOriginalUpstreamFromOpId(opId);
	}

	@Override
	public boolean isOperatorReady() {
		return runningOp.getReady();
	}

	@Override
	public void newOperatorInstantiation(Operator o) {
		LOG.info("-> Instantiating Operator...");
		//Detect the first submitted operator
		if(runningOp == null){
			runningOp = o;
		}
		else{
			LOG.warn("-> The operator in this node is being overwritten");
		}
		o.setProcessingUnit(this);
        
		if (GLOBALS.valueFor("netAwareDispatcher").equals("true"))
		{
			dispatcher = new Dispatcher(this);
		}
		
		// To identify the monitor with the op id instead of the node id
		NodeManager.monitorSlave.setOperatorId(o.getOperatorId());
		LOG.debug("Operator: {}", o);
        
		LOG.info("-> Instantiating Operator...DONE");
	}

	@Override
	public void processData(DataTuple data) {
		// TODO: Adjust timestamp of state
        
        // Seep monitoring: notify start of data tuple processing
        int operatorId = runningOp.getOperatorId();
        
        Timer.Context context = notifyThat(operatorId).operatorStart();
        
		runningOp.processData(data);
        
        // Seep monitoring: notify end of data tuple processing
        notifyThat(operatorId).operatorEnd(context);
	}

	@Override
	public void processData(ArrayList<DataTuple> data) {
		// TODO: Adjust timestamp of state
        
        // Seep monitoring: notify start of data tuple processing
        int operatorId = runningOp.getOperatorId();
        
        Timer.Context context = notifyThat(operatorId).operatorStart();
        
		runningOp.processData(data);
        
        // Seep monitoring: notify end of data tuple processing
        notifyThat(operatorId).operatorEnd(context);
	}

	@Override
	public void reconfigureOperatorConnection(int opId, InetAddress ip) {
		ctx.updateConnection(opId, runningOp, ip);
	}

	@Override
	public void reconfigureOperatorLocation(int opId, InetAddress ip) {
		runningOp.getOpContext().changeLocation(opId, ip);
	}

	@Override
	public void registerManagedState(int opId) {
		//If the state does not figure as being managed, we include it
		if(!listOfManagedStates.contains(opId)){
			LOG.info("-> New STATE registered for Operator: {}", opId);
			listOfManagedStates.add(opId);
		}
	}

	@Override
	public void sendData(DataTuple dt, ArrayList<Integer> targets) {
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
					outputQueues.get(target).sendToDownstream(dt, dest);
//System.out.println("Send to: "+dest.toString());
				}
				// LOCAL
				else if(dest instanceof Operator){
					Operator operatorObj = (Operator) dest;
                    
                    // Seep monitoring: notify start of data tuple processing
                    int operatorId = runningOp.getOperatorId();

                    Timer.Context context = notifyThat(operatorId).operatorStart();

                    operatorObj.processData(dt);

                    // Seep monitoring: notify end of data tuple processing
                    notifyThat(operatorId).operatorEnd(context);
				}
			}
			catch(ArrayIndexOutOfBoundsException aioobe){
				System.out.println("Targets size: "+targets.size()+" Target-Index: "+target+" downstreamSize: "+ctx.getDownstreamTypeConnection().size());
				aioobe.printStackTrace();
			}
		}
	}
        
	/*
	public void sendDataDispatched(DataTuple dt, ArrayList<Integer> targets)
	{
		dispatcher.dispatch(dt, targets);
	}
	*/
	public void sendDataDispatched(DataTuple dt)
	{
		dispatcher.dispatch(dt);
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
                            outputQueues.get(target).sendToDownstream(dtCopy, dest);
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
                            outputQueues.get(target).sendToDownstream(dtCopy, dest);
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
	public void setOpReady(int opId) {
		LOG.info("-> Setting operator ready");
		runningOp.setReady(true);
	}

	@Override
	public void setOutputQueue(OutputQueue outputQueue) {
		throw new RuntimeException("Changed to list of outputqueues");
	}

	@Override
	public void setSystemStatus(SystemStatus systemStatus) {
		this.systemStatus = systemStatus;
	}

	@Override
	public PUContext setUpRemoteConnections() {
		ctx.configureOperatorConnections(runningOp);
		if (dispatcher != null && !getOperator().getOpContext().isSink())
		{
			String routingAlg = GLOBALS.valueFor("frontierRouting");
			int replicationFactor = Integer.parseInt(GLOBALS.valueFor("replicationFactor"));
			if ("hash".equals(routingAlg) || replicationFactor == 1)
			{
				if (replicationFactor == 1) { LOG.warn("Using hash routing since no replication.");}
				Query frontierQuery = getOperator().getOpContext().getFrontierQuery();
				int logicalId = frontierQuery.getLogicalNodeId(getOperator().getOperatorId());
				int downLogicalId = frontierQuery.getNextHopLogicalNodeId(logicalId);
				boolean downIsReplicatedSink = frontierQuery.isSink(downLogicalId) && frontierQuery.getPhysicalNodeIds(downLogicalId).size() > 1; 
				if (downIsReplicatedSink && GLOBALS.valueFor("replicatedSinksHashRouting").equals("backpressure")) {
					getOperator().getRouter().setFrontierRouting(new BackpressureRouter(getOperator().getOpContext()));
					LOG.info("Using backpressure routing for this operator since down op is replicated sink.");
				} else {
					getOperator().getRouter().setFrontierRouting(new HashRouter(getOperator().getOpContext()));
					LOG.info("Using hash routing.");
				}
			}
			else if ("roundRobin".equals(routingAlg))
			{
				getOperator().getRouter().setFrontierRouting(new RoundRobinRouter(getOperator().getOpContext()));
				LOG.info("Using round robin routing.");
			}
			else if ("weightedRoundRobin".equals(routingAlg))
			{
				getOperator().getRouter().setFrontierRouting(new WeightedRoundRobinRouter(getOperator().getOpContext()));
				LOG.info("Using weighted round robin routing.");
			}
			else if ("backpressureWeightedRoundRobin".equals(routingAlg))
			{
				getOperator().getRouter().setFrontierRouting(new WeightedRoundRobinRouter(getOperator().getOpContext()));
				LOG.info("Using backpressure weighted round robin routing.");
			}
			else if ("powerOf2Choices".equals(routingAlg))
			{
				getOperator().getRouter().setFrontierRouting(new PowerOf2ChoicesRouter(getOperator().getOpContext()));
				LOG.info("Using power of 2 choices routing.");
			}
			else if ("broadcast".equals(routingAlg))
			{
				getOperator().getRouter().setFrontierRouting(new BroadcastRouter(getOperator().getOpContext()));
				LOG.info("Using broadcast routing.");
			}
			else if ("shortestPath".equals(routingAlg))
			{
				getOperator().getRouter().setFrontierRouting(new ShortestPathRouter(getOperator().getOpContext()));
				LOG.info("Using shortest path routing.");
			}
			else if ("backpressure".equals(routingAlg))
			{
				getOperator().getRouter().setFrontierRouting(new BackpressureRouter(getOperator().getOpContext()));
				LOG.info("Using backpressure routing.");
			}
			else
			{
				LOG.error("Unknown routing alg:"+routingAlg);
				System.exit(2);
			}
		}
		return ctx;
	}

	@Override
	public void startDataProcessing() {
		/// \todo{Find a better way to start the operator...}
		DataTuple fake = DataTuple.getNoopDataTuple();
		fake = null;
		this.runningOp.processData(fake);

	}

	@Override
	public void stopConnection(int opId) {
		//Stop incoming data, a new thread is replaying
		LOG.info("Stopping connection to OP: {}", opId);
		ArrayList<Integer> downstreamOpIdList = getOperator().getOpContext().getDownstreamOpIdList();
		int indexOfThisOpId = downstreamOpIdList.indexOf(opId);
		outputQueues.get(indexOfThisOpId).stop();
		ctx.getCCIfromOpId(opId, "d").getStop().set(true);
		throw new RuntimeException("What if using dispatcher? Should stop the associated dispatcher worker thread");
	}

	@Override
	public void launchMultiCoreMechanism(CoreRE core, DataStructureAdapter dsa) {
		// Different strategies depending on whether the state is partitionable or not
		int numberOfProcessors = Runtime.getRuntime().availableProcessors();
		int numberOfWorkerThreads = (numberOfProcessors - 2) > 1 ? (numberOfProcessors-2) : 1;
		
		pool = Executors.newFixedThreadPool(numberOfWorkerThreads);
		// Populate pool with threads
		for(int i = 0; i<numberOfWorkerThreads; i++){
			pool.execute(new StatelessProcessingWorker(dsa, runningOp));
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
	public void emitFailureCtrl(FailureCtrl nodeFctrl, boolean downstreamsRoutable)
	{	
		owner.writeFailureCtrls(getOperator().getOpContext().getListOfUpstreamIndexes(), nodeFctrl, downstreamsRoutable);
		
		Query frontierQuery = getOperator().getOpContext().getFrontierQuery();
		int logicalId = frontierQuery.getLogicalNodeId(getOperator().getOperatorId());
		if (!frontierQuery.isSink(logicalId))
		{
			int downLogicalId = frontierQuery.getNextHopLogicalNodeId(logicalId);
			if (frontierQuery.isSink(downLogicalId) && frontierQuery.getPhysicalNodeIds(downLogicalId).size() > 1)
			{
				//Downstream is a replicated sink, send combined failure ctrl *downstream* too.
				owner.writeDownstreamFailureCtrls(getOperator().getOpContext().getListOfDownstreamIndexes(), nodeFctrl);
			}
		}
	}

	public ArrayList<Integer> getRouterIndexesInformation(int opId){
		return runningOp.getRouter().getIndexesInformation(opId);
	}
	
	public ArrayList<Integer> getRouterKeysInformation(int opId){
		return runningOp.getRouter().getKeysInformation(opId);
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
		this.outputQueues = downOpId_outputQ_map;
		if (dispatcher != null) {
			getOperator().getRouter().addObserver(dispatcher);	//TODO: Bit weird to do this here.
			dispatcher.setOutputQueues(outputQueues); 
		}
	}
	
	@Override
	public void ack(DataTuple dt)
	{
		if (!getOperator().getOpContext().isSink())
		{
			throw new RuntimeException("Logic error.");
		}
		if (!GLOBALS.valueFor("reliability").equals("bestEffort"))
		{
			dispatcher.ack(dt);
		}
	}

}
