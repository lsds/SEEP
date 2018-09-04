/*******************************************************************************
 * Copyright (c) 2013 Imperial College London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial design and implementation
 *     Martin Rouaux - Support for scale-in of operators.
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.runtimeengine;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.Socket;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.comm.ControlHandler;
import uk.ac.imperial.lsds.seep.comm.IncomingDataHandler;
import uk.ac.imperial.lsds.seep.comm.OutgoingDataHandlerWorker;
import uk.ac.imperial.lsds.seep.comm.routing.Router;
import uk.ac.imperial.lsds.seep.comm.serialization.ControlTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.Ack;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.BackupOperatorState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.FailureCtrl;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.UpDownRCtrl;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.ReconfigureConnection;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.Resume;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.StateChunk;
import uk.ac.imperial.lsds.seep.infrastructure.WorkerNodeDescription;
import uk.ac.imperial.lsds.seep.infrastructure.dynamiccodedeployer.RuntimeClassLoader;
import uk.ac.imperial.lsds.seep.infrastructure.master.Node;
import uk.ac.imperial.lsds.seep.manet.CoreGUIUtil;
import uk.ac.imperial.lsds.seep.manet.CostHandler;
import uk.ac.imperial.lsds.seep.manet.NetRateMonitor;
import uk.ac.imperial.lsds.seep.manet.NetTopologyMonitor;
import uk.ac.imperial.lsds.seep.manet.Query;
import uk.ac.imperial.lsds.seep.manet.RoutingController;
import uk.ac.imperial.lsds.seep.manet.UpstreamRoutingController;
import uk.ac.imperial.lsds.seep.operator.EndPoint;
import uk.ac.imperial.lsds.seep.operator.InputDataIngestionMode;
import uk.ac.imperial.lsds.seep.operator.Operator;
import uk.ac.imperial.lsds.seep.operator.OperatorContext;
import uk.ac.imperial.lsds.seep.operator.OperatorStaticInformation;
import uk.ac.imperial.lsds.seep.operator.OperatorContext.PlacedOperator;
import uk.ac.imperial.lsds.seep.processingunit.IProcessingUnit;
import uk.ac.imperial.lsds.seep.processingunit.PUContext;
import uk.ac.imperial.lsds.seep.processingunit.StatefulProcessingUnit;
import uk.ac.imperial.lsds.seep.processingunit.StatelessProcessingUnit;
import uk.ac.imperial.lsds.seep.reliable.BackupHandler;
import uk.ac.imperial.lsds.seep.reliable.StateBackupWorker.CheckpointMode;

/**
* Operator. This is the class that must inherit any subclass (the developer must inherit this class). It is the basis for building an operator
*/

public class CoreRE {
	
	final private Logger LOG = LoggerFactory.getLogger(CoreRE.class);
	private final boolean piggybackControlTraffic = Boolean.parseBoolean(GLOBALS.valueFor("piggybackControlTraffic"));
	private final boolean separateControlNet = Boolean.parseBoolean(GLOBALS.valueFor("separateControlNet"));
	private final boolean enableDummies = Boolean.parseBoolean(GLOBALS.valueFor("sendDummyFailureControlTraffic"));
	private final boolean mergeFailureAndRoutingCtrl = Boolean.parseBoolean(GLOBALS.valueFor("mergeFailureAndRoutingCtrl"));
	private final Map<Integer, ControlTuple> lastUpOpIndexFctrls = new ConcurrentHashMap<Integer, ControlTuple>();
	private final boolean enableUpstreamRoutingControl = Boolean.parseBoolean(GLOBALS.valueFor("enableUpstreamRoutingControl")); 
	private final boolean multiHopReplayOptimization = Boolean.parseBoolean(GLOBALS.valueFor("optimizeReplay")) && Boolean.parseBoolean(GLOBALS.valueFor("multiHopReplayOptimization")) && !GLOBALS.valueFor("frontierRouting").equals("broadcast"); 
	private WorkerNodeDescription nodeDescr = null;
	
    private Thread processingUnitThread = null;
    private IProcessingUnit processingUnit = null;
    
	private PUContext puCtx = null;
	private RuntimeClassLoader rcl = null;
	private ArrayList<EndPoint> starTopology = null;
	
	private int backupUpstreamIndex = -1;

	private CoreProcessingLogic coreProcessLogic;

	private DataStructureAdapter dsa;
	private DataConsumer dataConsumer;
	private Thread dConsumerH = null;
	private ControlDispatcher controlDispatcher;
	private ArrayList<OutputQueue> outputQueues;
	private OutgoingDataHandlerWorker odhw = null;
	
	private Thread controlH = null;
	private Thread dummyControlH = null;
	private ControlHandler ch = null;
	private Thread iDataH = null;
	private IncomingDataHandler idh = null;
	private BackupHandler bh = null;
	private Thread backupH = null;
	private Thread costHandlerT = null;
	private CostHandler costHandler = null;
	
	private RoutingController routingController = null;
	private UpstreamRoutingController upstreamRoutingController = null;
	private Thread rControllerT = null;
	
	private NetRateMonitor netRateMonitor = null;
	private NetTopologyMonitor netTopologyMonitor = null;
	private Thread ntMonT = null;
	private Thread nrMonT = null;
	
	static ControlTuple genericAck;
	private int totalNumberOfChunks = -1;
	
	// Timestamp of the last data tuple processed by this operator
	private TimestampTracker incomingTT = new TimestampTracker();
	// Track last ack processed by this op
	private TimestampTracker ts_ack_vector = new TimestampTracker();
	
	
	public CoreRE(WorkerNodeDescription nodeDescr, RuntimeClassLoader rcl){
		this.nodeDescr = nodeDescr;
		this.rcl = rcl;		
		coreProcessLogic = new CoreProcessingLogic();
	}
	
	public RuntimeClassLoader getRuntimeClassLoader(){
		return rcl;
	}
	
	public WorkerNodeDescription getNodeDescr(){
		return nodeDescr;
	}
	
	public ControlDispatcher getControlDispatcher(){
		return controlDispatcher;
	}

	public RoutingController getRoutingController()
	{ return routingController; }

	public UpstreamRoutingController getUpstreamRoutingController()
	{
		return upstreamRoutingController;
	}

	public ControlTuple removeLastFCtrl(int upOpIndex)
	{
		if (!piggybackControlTraffic || !mergeFailureAndRoutingCtrl)
		{ throw new RuntimeException("Logic error.");}
		return lastUpOpIndexFctrls.remove(upOpIndex);
	}

	public ControlTuple getLastFCtrl(int upOpIndex)
	{
		if (!piggybackControlTraffic || !mergeFailureAndRoutingCtrl)
		{ throw new RuntimeException("Logic error.");}
		return lastUpOpIndexFctrls.get(upOpIndex);
	}

    public IProcessingUnit getProcessingUnit() {
        return processingUnit;
    }

	public ControlHandler getControlHandler() { return ch; }
	
	public void pushOperator(Operator o){
		boolean multicoreSupport = GLOBALS.valueFor("multicoreSupport").equals("true") ? true : false;
		if(o.getOpContext().getOperatorStaticInformation().isStatefull() ){
			processingUnit = new StatefulProcessingUnit(this, multicoreSupport);
		}
		else{
			processingUnit = new StatelessProcessingUnit(this, multicoreSupport);
		}
		processingUnit.newOperatorInstantiation(o);
	}
	
	/** Stores all the information concerning starTopology. In particular, this own operator is also included **/
	public void pushStarTopology(ArrayList<EndPoint> starTopology){
		// Store it here to enable async initialisation
		this.starTopology = starTopology;
		if(puCtx != null){
			// Also push it directly if the system is already initialised and this is a dynamic change
			puCtx.updateStarTopology(starTopology);
			puCtx.filterStarTopology(processingUnit.getOperator().getOperatorId());
		}
	}
	
	/** Retrieves all the information concerning the star topology **/
	public ArrayList<EndPoint> getInitialStarTopology(){
		return starTopology;
	}
	
	public void setOpReady(int opId) {
		processingUnit.setOpReady(opId);
		if(processingUnit.isOperatorReady()){
			LOG.info("-> All operators in this unit are ready. Initializing communications...");
			// Once the operators are in the node, we extract and declare how they will handle data tuples
			Map<String, Integer> idxMapper = processingUnit.createTupleAttributeMapper();
			processingUnit.initOperator();
			initializeCommunications(idxMapper);
		}
	}
	
	public void initializeCommunications(Map<String, Integer> tupleIdxMapper){
		int numDownstreams = processingUnit.getOperator().getOpContext().getDownstreamOpIdList().size();
		outputQueues = new ArrayList<>(numDownstreams);
		for (int i = 0; i < numDownstreams; i++)
		{
			outputQueues.add(new OutputQueue(this));
		}

		// SET UP the data structure adapter, depending on the operators
		dsa = new DataStructureAdapter();
		// We get the inputDataIngestion mode map, that consists of inputDataIngestion modes per upstream
		Map<Integer, InputDataIngestionMode> idimMap = processingUnit.getOperator().getOpContext().getInputDataIngestionModePerUpstream();
		for(Integer i : idimMap.keySet()){
			LOG.debug("Upstream: {} with this mode: {}", i, idimMap.get(i));
		}
		// We configure the dataStructureAdapter with this mode (per upstream), and put additional info required for some modes
		dsa.setUp(idimMap, processingUnit.getOperator().getOpContext());

		// Start communications and worker threads
		int inC = processingUnit.getOperator().getOpContext().getOperatorStaticInformation().getInC();
		int inD = processingUnit.getOperator().getOpContext().getOperatorStaticInformation().getInD();
		int inBT = new Integer(GLOBALS.valueFor("blindSocket"));

		
		Map<Integer, BlockingQueue<ControlTuple>> ctrlQueues = null;
		BlockingQueue<Socket> ctrlConnQueue = null;
		if (piggybackControlTraffic)
		{
			ctrlConnQueue = new LinkedBlockingQueue<Socket>();
			ctrlQueues = new ConcurrentHashMap<Integer, BlockingQueue<ControlTuple>>();
			for (Integer i : processingUnit.getOperator().getOpContext().getUpstreamOpIdList())	//TODO: Right number of upstreams?
			{
				LOG.info("Creating ctrlQueue for up op: " + i);
				ctrlQueues.put(i, new LinkedBlockingQueue<ControlTuple>());
			}
			LOG.info("Control queues: " + ctrlQueues);
		}

		//Control worker
		ch = new ControlHandler(this, inC, ctrlQueues, ctrlConnQueue);
		controlH = new Thread(ch, "controlHandlerT");
		//Data worker
		if (separateControlNet && enableDummies)
		{
			dummyControlH = new Thread(new ControlHandler(this, inC, ctrlQueues, ctrlConnQueue, true), "dummyControlHandlerT");
		} 

		
		idh = new IncomingDataHandler(this, inD, tupleIdxMapper, dsa, ctrlQueues);
		iDataH = new Thread(idh, "dataHandlerT");
		//Consumer worker
		dataConsumer = new DataConsumer(this, dsa);
		dConsumerH = new Thread(dataConsumer, "dataConsumerT");

		controlH.start();
		iDataH.start();

		if (separateControlNet && enableDummies)
		{
			dummyControlH.start(); 
		} 
		
		// Backup worker
		//dokeeffe TODO: Comment this out
		if (!"true".equals(GLOBALS.valueFor("disableBackup")))
		{
			bh = new BackupHandler(this, inBT);
			backupH = new Thread(bh, "backupHandlerT");
			backupH.start();
		}
		
		//TODO: is all this control logic really thread-safe? Don't think so.
		/*
		costHandler = new CostHandler(this);
		costHandlerT = new Thread(costHandler, "costHandlerT");
		costHandlerT.start();
		*/
		//TODO: What if we want to get costs from app-level probes?
		
	}
	
	public void setRuntime(){
		
		/// At this point I need information about what connections I need to establish
		LOG.debug("-> Configuring remote connections...");
		puCtx = processingUnit.setUpRemoteConnections();
		
		// Set up output communication module
		if (GLOBALS.valueFor("synchronousOutput").equals("true")){
			processingUnit.setOutputQueueList(outputQueues);
			LOG.debug("-> CONFIGURING SYSTEM WITH A SYNCHRONOUS OUTPUT");
		}
		else{
			Selector s = puCtx.getConfiguredSelector();
			odhw = new OutgoingDataHandlerWorker(s);
			Thread odhw_t = new Thread(odhw);
			odhw_t.start();
			LOG.debug("-> CONFIGURING SYSTEM WITH AN ASYNCHRONOUS OUTPUT");
		}
		
		// Set up multi-core support structures
		if(GLOBALS.valueFor("multicoreSupport").equals("true")){
			if(processingUnit.isMultiCoreEnabled()){
				processingUnit.launchMultiCoreMechanism(this, dsa);
				LOG.debug("-> Multi core support enabled");
			}
			else{
				// Start the consumer thread.
				dConsumerH.start();
				LOG.debug("-> Multi core support not enabled in this node");
			}
		}
		else{
			// Start the consumer thread.
			dConsumerH.start();
			LOG.debug("-> SYSTEM CONFIGURED FOR NO MULTICORE");
		}

		/// INSTANTIATION
		/** MORE REFACTORING HERE **/
		coreProcessLogic.setOwner(this);
		coreProcessLogic.setProcessingUnit(processingUnit);
		coreProcessLogic.setOpContext(puCtx);
		coreProcessLogic.initializeSerialization();
		
		controlDispatcher = new ControlDispatcher(puCtx);

		//initialize the genericAck message to answer some specific messages.
		ControlTuple b = new ControlTuple();
		b.setType(ControlTupleType.ACK);
		genericAck = b;
		
		LOG.debug("-> Node {} instantiated", nodeDescr.getNodeId());
		
		/// INITIALIZATION

		//Choose the upstreamBackupIndex for this operator
		int upstreamSize = processingUnit.getOperator().getOpContext().upstreams.size();
		configureUpstreamIndex(upstreamSize);
		// After configuring the upstream backup index, we start the stateBackupWorker thread if the node is stateful
		if(processingUnit.isNodeStateful()){
			if(((StatefulProcessingUnit)processingUnit).isCheckpointEnabled()){
				((StatefulProcessingUnit)processingUnit).createAndRunStateBackupWorker();
				LOG.debug("-> State Worker working on {}", nodeDescr.getNodeId());
			}
		}

		
		setUpFaultTolerance();
		setUpRouting();
		setUpGUI();
		
		LOG.info("-> Node "+nodeDescr.getNodeId()+" comm initialized");
	}
	
	private void setUpGUI()
	{	
		if (Boolean.parseBoolean(GLOBALS.valueFor("enableGUI")))
		{
			//TODO: This won't work if more than one worker per host!
			if (processingUnit.getOperator().getOpContext().isSink())
			{
				CoreGUIUtil.setSinkIcon();
			}
			else if (processingUnit.getOperator().getOpContext().isSource())
			{
				CoreGUIUtil.setSourceIcon();
			}
			else
			{
				CoreGUIUtil.setOpIcon();
			}
		}
	}
	
	private void setUpFaultTolerance()
	{
		// If ackworker is active
		if(GLOBALS.valueFor("ackWorkerActive").equals("true")){
			//If this is the sink operator (extremely ugly)
			//if(processingUnit.getOperator().getOpContext().downstreams.size() == 0){
			if(processingUnit.getOperator().getOpContext().isSink()){
				processingUnit.createAndRunAckWorker();
				LOG.info("-> ACK Worker working on {}", nodeDescr.getNodeId());
			}
		}
		else if(!GLOBALS.valueFor("reliability").equals("bestEffort") && GLOBALS.valueFor("fctrlWorkerActive").equals("true"))
		{
			OperatorContext opCtx = processingUnit.getOperator().getOpContext(); 
			if(opCtx.isSink() || !opCtx.isSource())
			{
				processingUnit.createAndRunFailureCtrlWriter();
			}
		}
	}
	
	private void setUpRouting()
	{
		if (GLOBALS.valueFor("enableFrontierRouting").equals("true"))
		{
			Query frontierQuery = processingUnit.getOperator().getOpContext().getFrontierQuery();
			int replicationFactor = Integer.parseInt(GLOBALS.valueFor("replicationFactor"));
			int logicalId = frontierQuery.getLogicalNodeId(processingUnit.getOperator().getOperatorId());
			Integer downLogicalId = frontierQuery.getNextHopLogicalNodeId(logicalId);
			boolean opIsMultiInput = frontierQuery.isJoin(logicalId);
			boolean downIsMultiInput = !frontierQuery.isSink(logicalId) && frontierQuery.isJoin(downLogicalId);
			boolean isReplicatedSink = frontierQuery.isSink(logicalId) && frontierQuery.getPhysicalNodeIds(logicalId).size() > 1; 
			boolean downIsReplicatedSink = !frontierQuery.isSink(logicalId) && frontierQuery.isSink(downLogicalId) && frontierQuery.getPhysicalNodeIds(downLogicalId).size() > 1; 

			if (replicationFactor == 1 || 
					GLOBALS.valueFor("frontierRouting").equals("hash"))
			{				

				//Record net rates for debugging purposes
				ArrayList<Integer> upOpIds = processingUnit.getOperator().getOpContext().getUpstreamOpIdList();
				Map<Integer, String> upOpIdAddrs = new HashMap<>();
				
				for (Integer upOpId : upOpIds)
				{
					OperatorStaticInformation opInfo = processingUnit.getOperator().getOpContext().getUpstreamLocation(upOpId);
					upOpIdAddrs.put(upOpId, opInfo.getMyNode().getIp().getHostName());
					
				}

				if (replicationFactor > 1 || !(isReplicatedSink || downIsReplicatedSink) || !GLOBALS.valueFor("replicatedSinksHashRouting").equals("backpressure"))
				{
					
					if (replicationFactor == 1) { LOG.warn("Using hash routing since no replication."); }
					LOG.info("Starting OLSRETX net rate monitor.");
					//Null routingController since just want to log net rates.
					netRateMonitor = new NetRateMonitor(upOpIdAddrs);
					nrMonT = new Thread(netRateMonitor, "NetRateMonitor");
					nrMonT.start();		
				}
				else
				{
					// If sinks are replicated, use bp for k=1 for a fair comparison?
					if (processingUnit.getOperator().getOpContext().isSink()) {
						routingController = new RoutingController(this);
						Thread rControllerT = new Thread(routingController, "RoutingController");
						rControllerT.start();

						if (enableUpstreamRoutingControl) { throw new RuntimeException("TODO."); }
						LOG.info("Starting OLSRETX net rate monitor.");
						netRateMonitor = new NetRateMonitor(upOpIdAddrs, routingController);
						nrMonT = new Thread(netRateMonitor, "NetRateMonitor");
						nrMonT.start();		
					}
					else
					{
						if (enableUpstreamRoutingControl) { throw new RuntimeException("TODO."); }
						processingUnit.getDispatcher().startRoutingCtrlWorkers();
					}
				}

				if (!processingUnit.getOperator().getOpContext().isSink())
				{
					processingUnit.getDispatcher().startDispatcherMain();
				}
			}
			else if (GLOBALS.valueFor("frontierRouting").equals("shortestPath"))
			{				
				if (!processingUnit.getOperator().getOpContext().isSink())
				{
				
					netTopologyMonitor = new NetTopologyMonitor(processingUnit.getOperator().getOperatorId(), frontierQuery, processingUnit.getOperator().getRouter());
					ntMonT = new Thread(netTopologyMonitor, "NetTopologyMonitor");
					ntMonT.start();
				}
				if (!processingUnit.getOperator().getOpContext().isSink())
				{
					processingUnit.getDispatcher().startDispatcherMain();
				}
			}
			else if (GLOBALS.valueFor("frontierRouting").equals("backpressure") ||
								GLOBALS.valueFor("frontierRouting").equals("weightedRoundRobin") ||
								GLOBALS.valueFor("frontierRouting").equals("backpressureWeightedRoundRobin") ||
								GLOBALS.valueFor("frontierRouting").equals("roundRobin") ||
								GLOBALS.valueFor("frontierRouting").equals("powerOf2Choices") ||
								GLOBALS.valueFor("frontierRouting").equals("broadcast"))
			{
				if (replicationFactor > 1 && downIsMultiInput &&
						(GLOBALS.valueFor("frontierRouting").equals("weightedRoundRobin") || 
						  GLOBALS.valueFor("frontierRouting").equals("backpressureWeightedRoundRobin") || 
							GLOBALS.valueFor("frontierRouting").equals("roundRobin") ||
							GLOBALS.valueFor("frontierRouting").equals("powerOf2Choices") ||
							GLOBALS.valueFor("frontierRouting").equals("broadcast")))
				{ throw new RuntimeException("Logic error: can't using RR, WRR, BPWRR, P2C or Bcast with multi-input operators (yet)."); }

				if ((GLOBALS.valueFor("frontierRouting").equals("weightedRoundRobin") ||
							GLOBALS.valueFor("frontierRouting").equals("broadcast")) && 
						enableUpstreamRoutingControl && !Boolean.parseBoolean(GLOBALS.valueFor("ignoreQueueLengths")))
				{ throw new RuntimeException("Logic error: invalid configuration for WRR/Bcast."); }

				if (!processingUnit.getOperator().getOpContext().isSource())
				{
						routingController = new RoutingController(this);
						Thread rControllerT = new Thread(routingController, "RoutingController");
						rControllerT.start();
					
					if (!enableUpstreamRoutingControl || opIsMultiInput)
					{
						ArrayList<Integer> upOpIds = processingUnit.getOperator().getOpContext().getUpstreamOpIdList();
						Map<Integer, String> upOpIdAddrs = new HashMap<>();
						
						for (Integer upOpId : upOpIds)
						{
							OperatorStaticInformation opInfo = processingUnit.getOperator().getOpContext().getUpstreamLocation(upOpId);
							upOpIdAddrs.put(upOpId, opInfo.getMyNode().getIp().getHostName());
							
						}
						
						LOG.info("Starting OLSRETX net rate monitor.");
						netRateMonitor = new NetRateMonitor(upOpIdAddrs, routingController);
						nrMonT = new Thread(netRateMonitor, "NetRateMonitor");
						nrMonT.start();		
					}	
				}
				if (!processingUnit.getOperator().getOpContext().isSink())
				{
					if (enableUpstreamRoutingControl && !downIsMultiInput)
					{
						upstreamRoutingController = new UpstreamRoutingController(this);

						ArrayList<Integer> downOpIds = processingUnit.getOperator().getOpContext().getDownstreamOpIdList();
						Map<Integer, String> downOpIdAddrs = new HashMap<>();
						
						for (Integer downOpId : downOpIds)
						{
							OperatorStaticInformation opInfo = processingUnit.getOperator().getOpContext().getDownstreamLocation(downOpId);
							downOpIdAddrs.put(downOpId, opInfo.getMyNode().getIp().getHostName());
							
						}
					
						LOG.info("Starting OLSRETX net rate monitor.");
						netRateMonitor = new NetRateMonitor(downOpIdAddrs, upstreamRoutingController);
						nrMonT = new Thread(netRateMonitor, "NetRateMonitor");
						nrMonT.start();		
					}
					else
					{
						processingUnit.getDispatcher().startRoutingCtrlWorkers();
					}

					processingUnit.getDispatcher().startDispatcherMain();
					
					/*
					if (!GLOBALS.valueFor("reliability").equals("bestEffort"))
					{
						processingUnit.getDispatcher().startFailureDetector();
					}
					*/
				}
			}
			else { throw new RuntimeException("Unknown routing algorithm: "+GLOBALS.valueFor("frontierRouting")); }
		}
	}
    /**
     * This method is blocking. So, basically, if invoked from NodeManager directly,
     * we are unable to send any more control tuples to the node.
     */
	public void startDataProcessing(){
		LOG.info("-> Starting to process data...");
		processingUnit.startDataProcessing();
	}
    
    /**
     * Starts data processing on a separate thread and returns immediately. If
     * invoked from NodeManager, then control is returned immediately 
     */
    public void startDataProcessingAsync() {
        processingUnitThread = new Thread(new Runnable() {

            @Override
            public void run() {
                LOG.info("-> Starting to process data (asynchronous)...");
                
                while(true) {
                    // Let's protect the operator against exceptions
                    try {
                        processingUnit.startDataProcessing();
                    } catch(Throwable t) {
                        LOG.error("Exception while processing data " + t);
                    }
                }
            }
        });
        
        processingUnitThread.start();
    }
	
	public void stopDataProcessing(){
		LOG.info("-> The system has been remotely stopped. No processing data");
        if (dataConsumer != null) {
            dataConsumer.setDoWork(false);
        }
    }
	
	public enum ControlTupleType{
		ACK, BACKUP_OP_STATE, RECONFIGURE, SCALE_OUT, SCALE_IN, RESUME, INIT_STATE, STATE_ACK, INVALIDATE_STATE,
		BACKUP_RI, INIT_RI, OPEN_BACKUP_SIGNAL, CLOSE_BACKUP_SIGNAL, STREAM_STATE, STATE_CHUNK, DISTRIBUTED_SCALE_OUT,
		KEY_SPACE_BOUNDS, FAILURE_CTRL, DOWN_UP_RCTRL, UP_DOWN_RCTRL, MERGED_CTRL
	}
	
	public synchronized void setTsData(int stream, long ts_data){
		this.incomingTT.set(stream, ts_data);
	}

	public synchronized long getTsData(int stream){
		return incomingTT.get(stream);
	}
	
	public TimestampTracker getIncomingTT(){
		return incomingTT;
	}
	
	public DataStructureAdapter getDSA(){
		return dsa;
	}
	
	public void forwardData(DataTuple data){
		processingUnit.processData(data);
	}
	
	public void forwardData(ArrayList<DataTuple> data){
		processingUnit.processData(data);
	}
	
	public int getBackupUpstreamIndex() {
		return backupUpstreamIndex;
	}

	public void setBackupUpstreamIndex(int backupUpstreamIndex) {
		System.out.println("% current backupIdx: "+this.backupUpstreamIndex+" changes to: "+backupUpstreamIndex);
		this.backupUpstreamIndex = backupUpstreamIndex;
	}
	
	public int getOriginalUpstreamFromOpId(int opId){
		return processingUnit.getOriginalUpstreamFromOpId(opId);
	}
	
	public int getOpIdFromInetAddress(InetAddress ip){
		return processingUnit.getOpIdFromUpstreamIp(ip);
	}

	public int getOpIdFromInetAddressAndPort(InetAddress ip, int port){
		return processingUnit.getOpIdFromUpstreamIpPort(ip, port);
	}
	
	//TODO To refine this method...
	/// \todo {this method should work when an operator must be killed in a proper way}
	public boolean killHandlers(){
		//controlH.destroy();
		//iDataH.destroy();
		return true;
	}

	public boolean checkSystemStatus(){
		// is it correct like this?
		if(processingUnit.getSystemStatus().equals(StatefulProcessingUnit.SystemStatus.NORMAL)){
			return true;
		}
		return false;
	}
	
	///\todo{refactor: Represent this method as a finite state machine and provide methods to query and update the state}
	//TODO: dokeeffe: Shouldn't this be synchronized since it could be called from multiple controlhandlerworkers?
	public void processControlTuple(ControlTuple ct, OutputStream os, InetAddress remoteAddress) {
		/** 
		 * SCALE_OUT (light state):
		 * M = Master, U = Upstream, D = Downstream
		 * M -> (scale_out) -> U
		 * M -> (resume) -> U
		 * U -> (init_message) -> D
		 * D -> (state_ack) -> U
		 * U -> replay tuples and go on processing data
		 * 
		 * DISTRIBUTED_SCALE_OUT (large state):
		 *
		 **/
		ControlTupleType ctt = ct.getType();

		Query frontierQuery = processingUnit.getOperator().getOpContext().getFrontierQuery();
		int logicalId = frontierQuery.getLogicalNodeId(processingUnit.getOperator().getOperatorId());
		boolean opIsMultiInput = frontierQuery.isJoin(logicalId);
		boolean downIsMultiInput = !processingUnit.getOperator().getOpContext().isSink() && frontierQuery.isJoin(frontierQuery.getNextHopLogicalNodeId(logicalId));

		/** ACK message **/
		if(ctt.equals(ControlTupleType.ACK)) {
			Ack ack = ct.getAck();
			if(processingUnit.getOperator().getOpContext().isSink()){
				System.out.println("Received OPId: "+ack.getOpId()+" TS: "+ack.getTs());
				System.out.println("ACK-vector: "+ts_ack_vector);
//				System.exit(-1);
			}
			if(ack.getTs() > ts_ack_vector.get(ack.getOpId())){ // Only if this ack is newer than the last registered
				ts_ack_vector.set(ack.getOpId(), ack.getTs()); // then register for next time and process ack
				coreProcessLogic.processAck(ack);
			}
		}
		else if (ctt.equals(ControlTupleType.FAILURE_CTRL))
		{
			FailureCtrl fctrl = ct.getOpFailureCtrl().getFailureCtrl();
			int fctrlSenderOpId = ct.getOpFailureCtrl().getOpId();
			
			/*
			if (processingUnit.getOperator().getOpContext().isSink())
			{
		
			}
			else
			{
				//TODO: Check whether dupe?
				coreProcessLogic.processFailureCtrl(fctrl, fctrlSenderOpId);
			}
			*/
			
			coreProcessLogic.processFailureCtrl(fctrl, fctrlSenderOpId);
		}
		
		else if (ctt.equals(ControlTupleType.DOWN_UP_RCTRL))
		{
			if (processingUnit.getOperator().getOpContext().isSink())
			{
				throw new RuntimeException("Logic error?");
			}		

			if (!enableUpstreamRoutingControl || downIsMultiInput)
			{
				LOG.debug("About to update weight with downup rctrl: "+ct.getDownUp());
				processingUnit.getOperator().getRouter().update_highestWeight(ct.getDownUp());			
			}
			else
			{
				if (upstreamRoutingController == null) { LOG.warn("Dropping down up routing control as upstream controller is null."); }
				else { upstreamRoutingController.handleRCtrl(ct.getDownUp()); }
			}
		}
		else if (ctt.equals(ControlTupleType.UP_DOWN_RCTRL))
		{			
			if (processingUnit.getOperator().getOpContext().isSource())
			{
				throw new RuntimeException("Logic error?");
			}
			if (routingController == null)
			{
				LOG.warn("Ignoring upDown routing ctrl message - routing controller is still null.");
			}
			else
			{
				routingController.handleRCtrl(ct.getUpDown());
			}
		}
		else if (ctt.equals(ControlTupleType.MERGED_CTRL))
		{
			LOG.debug("Handling merged ctrl: "+ct);
			FailureCtrl fctrl = ct.getOpFailureCtrl().getFailureCtrl();
			int fctrlSenderOpId = ct.getOpFailureCtrl().getOpId();
			
			/*
			if (processingUnit.getOperator().getOpContext().isSink())
			{
		
			}
			else
			{
				//TODO: Check whether dupe?
				coreProcessLogic.processFailureCtrl(fctrl, fctrlSenderOpId);
			}
			*/
			
			coreProcessLogic.processFailureCtrl(fctrl, fctrlSenderOpId);

			if (processingUnit.getOperator().getOpContext().isSink())
			{
				throw new RuntimeException("Logic error?");
			}		

			LOG.debug("About to update weight with downup rctrl: "+ct.getDownUp());
			if (!enableUpstreamRoutingControl || downIsMultiInput)
			{
				LOG.debug("About to update weight with downup rctrl: "+ct.getDownUp());
				processingUnit.getOperator().getRouter().update_highestWeight(ct.getDownUp());			
			}
			else
			{
				if (upstreamRoutingController == null) { LOG.warn("Dropping down up routing control as upstream controller is null."); }
				else { upstreamRoutingController.handleRCtrl(ct.getDownUp()); }
			}
			//processingUnit.getOperator().getRouter().update_highestWeight(ct.getDownUp());			

		}
		/** INVALIDATE_STATE message **/
		else if(ctt.equals(ControlTupleType.INVALIDATE_STATE)) {
			LOG.info("-> Node {} recv ControlTuple.INVALIDATE_STATE from OP: {}", nodeDescr.getNodeId(), ct.getInvalidateState().getOperatorId());
			processingUnit.invalidateState(ct.getInvalidateState().getOperatorId());
		}
		/** INIT_STATE message **/
		else if(ctt.equals(ControlTupleType.INIT_STATE)){
			LOG.info("-> Node {} recv ControlTuple.INIT_STATE from OP: {}", nodeDescr.getNodeId(), ct.getInitOperatorState().getOpId());
			coreProcessLogic.processInitState(ct.getInitOperatorState());
		}
		/** KEY_SPACE_BOUNDS message **/
		else if(ctt.equals(ControlTupleType.KEY_SPACE_BOUNDS)){
			LOG.info("-> Node {} recv ControlTuple.KEY_SPACE_BOUNDS", nodeDescr.getNodeId());
			int minBound = ct.getKeyBounds().getMinBound();
			int maxBound = ct.getKeyBounds().getMaxBound();
			((StatefulProcessingUnit)processingUnit).setKeySpaceBounds(minBound, maxBound);
		}
		/** OPEN_SIGNAL message **/
		else if(ctt.equals(ControlTupleType.OPEN_BACKUP_SIGNAL)){
			System.out.println("%%%%%%%%%%%%%%%%%%");
			LOG.info("-> Node {} recv ControlTuple.OPEN_SIGNAL from OP: {}", nodeDescr.getNodeId(), ct.getOpenSignal().getOpId());
			bh.openSession(ct.getOpenSignal().getOpId(), remoteAddress);
			PrintWriter out = new PrintWriter(os, true);
			out.println("ack");
			LOG.debug("-> ACK Open Signal");
			System.out.println("%%%%%%%%%%%%%%%%%%");
		}
		/** CLOSE_SIGNAL message **/
		else if(ctt.equals(ControlTupleType.CLOSE_BACKUP_SIGNAL)){
			LOG.info("-> Node {} recv ControlTuple.CLOSE_SIGNAL from OP: ", nodeDescr.getNodeId(), ct.getCloseSignal().getOpId());
			bh.closeSession(ct.getCloseSignal().getOpId(), remoteAddress);
			
//			coreProcessLogic.directReplayState(new ReplayStateInfo(1, 1, true), bh);
		}
		/** STATE_BACKUP message **/
		else if(ctt.equals(ControlTupleType.BACKUP_OP_STATE)){
			//If communications are not being reconfigured
			//Register this state as being managed by this operator
			BackupOperatorState backupOperatorState = ct.getBackupState();
			LOG.info("-> Node {} recv BACKUP_OP_STATE from Op: ", nodeDescr.getNodeId(), backupOperatorState.getOpId());
			processingUnit.registerManagedState(backupOperatorState.getOpId());
			coreProcessLogic.processBackupState(backupOperatorState);
		}
		/** STATE_ACK message **/
		else if(ctt.equals(ControlTupleType.STATE_ACK)){
			if (processingUnit.getDispatcher() != null)
			{
				throw new UnsupportedOperationException("TODO: Not supported for net aware routing yet.");
			}
			int opId = ct.getStateAck().getMostUpstreamOpId();
			LOG.info("-> Received STATE_ACK from Op: {}", opId);
//			operatorStatus = OperatorStatus.REPLAYING_BUFFER;
			SynchronousCommunicationChannel cci = puCtx.getCCIfromOpId(opId, "d");
			ArrayList<Integer> downOpIds = processingUnit.getOperator().getOpContext().getDownstreamOpIdList();
			int downOpIndex = downOpIds.indexOf(opId);
			outputQueues.get(downOpIndex).replayTuples(cci);
			// In case of failure, the thread may have died, in such case we make it runnable again.
//			if(dConsumerH.getState() != Thread.State.TERMINATED){
//				dConsumerH = new Thread(dataConsumer);
//				dConsumerH.start();
//			}
//			operatorStatus = OperatorStatus.NORMAL;
		}
		/** BACKUP_RI message **/
		else if(ctt.equals(ControlTupleType.BACKUP_RI)){
			LOG.info("-> Node {} recv ControlTuple.BACKUP_RI", nodeDescr.getNodeId());
			coreProcessLogic.storeBackupRI(ct.getBackupRI());
		}
		/** INIT_RI message **/
		else if(ctt.equals(ControlTupleType.INIT_RI)){
			LOG.info("-> Node {} recv ControlTuple.INIT_RI from {}: ",nodeDescr.getNodeId(), ct.getInitRI().getNodeId());
			coreProcessLogic.installRI(ct.getInitRI());
		}
		/** SCALE_OUT message **/
		else if(ctt.equals(ControlTupleType.SCALE_OUT)) {
			
			LOG.info("-> Node {} recv ControlTuple.SCALE_OUT ", nodeDescr.getNodeId());
			// Get index of new replica operator
			int newOpIndex = -1;
			for(PlacedOperator op: processingUnit.getOperator().getOpContext().downstreams) {
				if (op.opID() == ct.getScaleOutInfo().getNewOpId()) {
					newOpIndex = op.index();
                }
            }
			
            // Get index of the scaling operator
			int oldOpIndex = processingUnit.getOperator().getOpContext().findDownstream(ct.getScaleOutInfo().getOldOpId()).index();
			coreProcessLogic.scaleOut(ct.getScaleOutInfo(), newOpIndex, oldOpIndex);
            
			//Ack the message
			controlDispatcher.ackControlMessage(genericAck, os);
		}
        /** SCALE_IN message **/
        else if(ctt.equals(ControlTupleType.SCALE_IN)) {
        
        	LOG.info("-> Node {} recv ControlTuple.SCALE_IN ", nodeDescr.getNodeId());
		
            // Get index of replica operator being terminated as part of scale-in
            // We usually refer to this replica as "victim" of the scale-in action
			int victimOpIndex = -1;
			for(PlacedOperator op: processingUnit.getOperator().getOpContext().downstreams) {
				if (op.opID() == ct.getScaleInInfo().getVictimOperatorId()) {
					victimOpIndex = op.index();
				}
            }
        
            // Perform scale-in action
            coreProcessLogic.scaleIn(ct.getScaleInInfo(), victimOpIndex);
            
            // Ack the message
			controlDispatcher.ackControlMessage(genericAck, os);
        }
		/** DISTRIBUTED_SCALE_OUT message **/
		else if(ctt.equals(ControlTupleType.DISTRIBUTED_SCALE_OUT)){
			LOG.info("-> Node {} recv ControlTuple.DISTRIBUTED_SCALE_OUT",nodeDescr.getNodeId());
			int oldOpId = ct.getDistributedScaleOutInfo().getOldOpId();
			int newOpId = ct.getDistributedScaleOutInfo().getNewOpId();
			// Stateful operator
			if(puCtx.isScalingOpDirectDownstream(oldOpId)){
				System.out.println("Direct downstream");
				int newOpIndex = -1;
				for(PlacedOperator op: processingUnit.getOperator().getOpContext().downstreams) {
					if (op.opID() == newOpId)
						newOpIndex = op.index();
				}
				// Get index of the scaling operator
				int oldOpIndex = processingUnit.getOperator().getOpContext().findDownstream(oldOpId).index();
				// And manage distributed scale out
				if(processingUnit.isNodeStateful()){ // stateful case
					int bounds[] = coreProcessLogic.manageDownstreamDistributedScaleOut(oldOpId, newOpId, oldOpIndex, newOpIndex);
					coreProcessLogic.propagateNewKeys(bounds, oldOpIndex, newOpIndex);
				}
				else{ // stateless case
					processingUnit.getOperator().getRouter().newOperatorPartition(oldOpId, newOpId, oldOpIndex, newOpIndex);
				}
			}
			if(processingUnit.isNodeStateful()){ // if stateful, backup routing information after updating it
				coreProcessLogic.backupRoutingInformation(oldOpId);
			}
			coreProcessLogic.directReplayStateScaleOut(oldOpId, newOpId, bh); // finally replay
//			controlDispatcher.ackControlMessage(genericAck, os);
		}
		/** REPLAY_STATE **/
		else if(ctt.equals(ControlTupleType.STREAM_STATE)){
//			//Replay the state that this node keeps
			LOG.info("-> Node {} recv ControlTuple.STREAM_STATE", nodeDescr.getNodeId());
//			
			int opId = ct.getStreamState().getTargetOpId();
			coreProcessLogic.directReplayStateFailure(opId, bh);
			
			// no ack, just be fast
			//Finally ack the processing of this message
//			controlDispatcher.ackControlMessage(genericAck, os);
		}
		/** STATE_CHUNK **/
		else if(ctt.equals(ControlTupleType.STATE_CHUNK)){
			// One out of n chunks of state received
			LOG.info("-> Node {} recv ControlTuple.STATE_CHUNK", nodeDescr.getNodeId());
			StateChunk chunk = ct.getStateChunk();
//			System.out.println("CHUNK rcvd: "+chunk.getSequenceNumber());
			coreProcessLogic.handleNewChunk(chunk);
		}
		/** RESUME message **/
		else if (ctt.equals(ControlTupleType.RESUME)) {
			LOG.info("-> Node {} recv ControlTuple.RESUME", nodeDescr.getNodeId());
			Resume resumeM = ct.getResume();
            
            // This if statement is necessary because processingUnit might be
            // of type StatelessProcessingUnit. Otherwise, a ClassCastException is thrown.
			if (processingUnit instanceof StatefulProcessingUnit) {
                
                if (((StatefulProcessingUnit)processingUnit).getCheckpointMode().equals(CheckpointMode.LIGHT_STATE)) {
                    // If I have previously splitted the state, I am in WAITING FOR STATE-ACK status and I have to replay it.
                    // I may be managing a state but I dont have to replay it if I have not splitted it previously
                    if(processingUnit.getSystemStatus().equals(StatefulProcessingUnit.SystemStatus.WAITING_FOR_STATE_ACK)){
                        /// \todo {why has resumeM a list?? check this}
                        for (int opId: resumeM.getOpId()){
                            //Check if I am managing the state of any of the operators to which state must be replayed
                            if(processingUnit.isManagingStateOf(opId)){
                                LOG.debug("-> Replaying State");
                                coreProcessLogic.replayState(opId);
                            }
                            else{
                                LOG.info("-> NOT in charge of managing this state");
                            }
                        }
                        //Once I have replayed the required states I put my status to NORMAL
                        processingUnit.setSystemStatus(StatefulProcessingUnit.SystemStatus.NORMAL);
                    }
                    else{
                        LOG.info("-> Ignoring RESUME state, I did not split this one");
                    }
                }
                else if (((StatefulProcessingUnit)processingUnit).getCheckpointMode().equals(CheckpointMode.LARGE_STATE)){
                    LOG.info("Ignoring RESUME message because checkpoint mode is LARGE-STATE");
                }
            }
        
			//Finally ack the processing of this message
			controlDispatcher.ackControlMessage(genericAck, os);
		}
		
		/** RECONFIGURE message **/
		else if(ctt.equals(ControlTupleType.RECONFIGURE)){
			processCommand(ct.getReconfigureConnection(), os);
		}
	}
	
	/// \todo {stopping and starting the conn should be done from updateConnection in some way to hide the complexity this introduces here}
	public void processCommand(ReconfigureConnection rc, OutputStream os){
		String command = rc.getCommand();
		LOG.info("-> Node {} recv {} command ", nodeDescr.getNodeId(), command);
		InetAddress ip = null;
		int opId = rc.getOpId();
		
		try{
			ip = InetAddress.getByName(rc.getIp());
		}
		catch (UnknownHostException uhe) {
			LOG.error("-> Node "+nodeDescr.getNodeId()+" EXCEPTION while getting IP from msg "+uhe.getMessage());
			uhe.printStackTrace();
		}
		/** RECONFIGURE DOWN or RECONFIGURE UP message **/
		if(command.equals("reconfigure_D") || command.equals("reconfigure_U") || command.equals("just_reconfigure_D")){
//			operatorStatus = OperatorStatus.RECONFIGURING_COMM;
			processingUnit.reconfigureOperatorLocation(opId, ip);
				//If no twitter storm, then I have to stop sending data and replay, otherwise I just update the conn
				/// \test {what is it is twitter storm but it is also the first node, then I also need to stop connection, right?}
			if((command.equals("reconfigure_D") || command.equals("just_reconfigure_D"))){
				processingUnit.stopConnection(opId);
			} 
			processingUnit.reconfigureOperatorConnection(opId, ip);
			
			if(command.equals("reconfigure_U")){
				coreProcessLogic.sendRoutingInformation(opId, rc.getOperatorType());
			}
			if(command.equals("reconfigure_D")){
				if(processingUnit.isManagingStateOf(opId)){
					LOG.debug("-> Replaying State");
					coreProcessLogic.replayState(opId);
				}
				else{
					LOG.info("-> NOT in charge of managing this state");
				}
			}
		}
		/** ADD DOWN or ADD UP message **/
		else if(command.equals("add_downstream") || command.equals("add_upstream")){
//			operatorStatus = OperatorStatus.RECONFIGURING_COMM;
			// at this point we need opId and originalOpId
			
			OperatorStaticInformation loc = new OperatorStaticInformation(opId, rc.getOriginalOpId(), 
					new Node(ip, rc.getNode_port()), rc.getInC(), rc.getInD(), rc.getOperatorNature());
			if(command.equals("add_downstream")){
				processingUnit.addDownstream(opId, loc);
			}
			else if (command.equals("add_upstream")) {
				//Configure new upstream
				processingUnit.addUpstream(opId, loc);
				//Send to that upstream the routing information I am storing (in case there are ri).
				coreProcessLogic.sendRoutingInformation(opId, rc.getOperatorType());
				
				// Check how many replicas of this operator are at the moment and reconfigure barrier with this number.
				// This is necessary for cases where there is more than one InputDataIngestionMode
				int originalOpId = processingUnit.getOriginalUpstreamFromOpId(opId);
				/// \fixme{Reconfigure this taking into account the inputdataingestion mode}
				int upstreamSizeForBarrier = processingUnit.getOperator().getOpContext().getUpstreamNumberOfType(originalOpId);
				int upstreamSize = processingUnit.getOperator().getOpContext().upstreams.size();
				reconfigureUpstreamBackupIndex(upstreamSize);
//				dsa.reconfigureNumUpstream(originalOpId, upstreamSize);
				dsa.reconfigureNumUpstream(originalOpId, upstreamSizeForBarrier);
			}
			controlDispatcher.ackControlMessage(genericAck, os);
		}
		/** SYSTEM READY message **/
		else if (command.equals("system_ready")){
			controlDispatcher.ackControlMessage(genericAck, os);
			//Now that all the system is ready (both down and up) I manage my own information and send the required msgs
			coreProcessLogic.sendInitialStateBackup();
		}
//		/** ADD STAR TOPOLOGY message **/
//		else if(command.equals("add_star_topology")){
//			controlDispatcher.ackControlMessage(genericAck, os);
//			InetAddress newIp = rc.getIpStarTopology();
//			int starOpId = rc.getOpIdStarTopology();
//			puCtx.addNodeToStarTopolocy(opId, newIp);
//		}
//		/** REMOVE STAR TOPOLOGY message **/
//		else if(command.equals("remove_star_topology")){
//			controlDispatcher.ackControlMessage(genericAck, os);
//			int starOpId = rc.getOpIdStarTopology();
//			puCtx.removeNodeFromStarTopology(opId);
//		}
		/** REPLAY message **/
		/// \todo {this command is only used for twitter storm model...}
		else if (command.equals("replay")){
			if(processingUnit.getDispatcher() != null)
			{
				throw new UnsupportedOperationException("TODO: Replay not supported yet for net aware routing.");
			}
			//ackControlMessage(os);
			//FIXME there is only one, this must be done for each conn
			processingUnit.stopConnection(opId);
			/// \todo{avoid this deprecated function}
			//opCommonProcessLogic.startReplayer(opID);
			SynchronousCommunicationChannel cci = puCtx.getCCIfromOpId(opId, "d");
			ArrayList<Integer> downOpIds = processingUnit.getOperator().getOpContext().getDownstreamOpIdList();
			int downOpIndex = downOpIds.indexOf(opId);
			outputQueues.get(downOpIndex).replayTuples(cci);
		}
		/** NOT RECOGNIZED message **/
		else{
			LOG.warn("-> Op.processCommand, command not recognized");
			throw new RuntimeException("Operator: ERROR in processCommand");
		}
	}
	
	public void ack(TimestampTracker tsVector) {
		// ack per input channel
		Iterator<Entry<Integer, Long>> i = tsVector.getTsStream();
		while(i.hasNext()){
			Entry<Integer, Long> channelInfo = i.next();
			int opId = channelInfo.getKey();
			long ts = channelInfo.getValue();
			ControlTuple ack = new ControlTuple(ControlTupleType.ACK, processingUnit.getOperator().getOperatorId(), ts);
			int index = processingUnit.getOperator().getOpContext().getUpOpIndexFromOpId(opId);
			boolean bestEffortAcks = "true".equals(GLOBALS.valueFor("bestEffortAcks"));
			controlDispatcher.sendUpstream(ack, index, !bestEffortAcks);
				
		}
	}
	
	public void writeFailureCtrls(ArrayList<Integer> upOpIndexes, FailureCtrl nodeFctrl, boolean downstreamsRoutable)
	{
		if (controlDispatcher == null || upOpIndexes.isEmpty()) { return; }
		long tStart = System.currentTimeMillis();
		LOG.debug("Writing failure ctrl to up op indices:"+upOpIndexes.toString());
		DataStructureI dso = dsa.getUniqueDso();
		if (dso == null) { throw new RuntimeException("TODO"); }
		//FailureCtrl purgeFctrl = multiHopReplayOptimization ? nodeFctrl : new FailureCtrl(nodeFctrl.lw(), nodeFctrl.acks(), null);
		FailureCtrl purgeFctrl = multiHopReplayOptimization ? nodeFctrl : nodeFctrl.copy(false);
		ArrayList<FailureCtrl> upFctrls = dso.purge(purgeFctrl);
		Query frontierQuery = processingUnit.getOperator().getOpContext().getFrontierQuery();
		int opId = processingUnit.getOperator().getOperatorId();
		for (int upOpIndex : upOpIndexes)
		{
			int upOpId = processingUnit.getOperator().getOpContext().getUpOpIdFromIndex(upOpIndex);
			LOG.debug("Writing failure ctrl to up op id:"+upOpId);
			FailureCtrl upFctrl = upFctrls.get(frontierQuery.getLogicalInputIndex(
					frontierQuery.getLogicalNodeId(opId), 
					frontierQuery.getLogicalNodeId(upOpId)));
			if (!downstreamsRoutable) { upFctrl = purgeFctrl; }
			LOG.debug("Writing failure ctrl, node="+nodeFctrl+",upOp="+upFctrl);
			ControlTuple ct = new ControlTuple(ControlTupleType.FAILURE_CTRL, opId , upFctrl);
			boolean bestEffortAcks = "true".equals(GLOBALS.valueFor("bestEffortAcks"));

			if (!piggybackControlTraffic || !mergeFailureAndRoutingCtrl || routingController == null)
			{
				controlDispatcher.sendUpstream(ct, upOpIndex, !bestEffortAcks);
			}
			else
			{
				lastUpOpIndexFctrls.put(upOpIndex, ct);
			}

			if (separateControlNet && enableDummies) { controlDispatcher.sendDummyUpstream(ct, upOpIndex); }
		}
		LOG.debug("Wrote failure ctrl in "+ (System.currentTimeMillis() - tStart) + " ms");
	}
	
	public void writeDownstreamFailureCtrls(ArrayList<Integer> downOpIndexes, FailureCtrl nodeFctrl)
	{
		LOG.debug("Writing failure ctrl to down op indices:"+downOpIndexes.toString());
		int opId = processingUnit.getOperator().getOperatorId();
		//FailureCtrl noAlives = new FailureCtrl(nodeFctrl.lw(), nodeFctrl.acks(), null);
		FailureCtrl noAlives = nodeFctrl.copy(false);
		for (int downOpIndex : downOpIndexes)
		{
			int downOpId = processingUnit.getOperator().getOpContext().getDownOpIdFromIndex(downOpIndex);
			LOG.debug("Writing failure ctrl to down op id="+downOpId+",fctrl="+noAlives);
			ControlTuple ct = new ControlTuple(ControlTupleType.FAILURE_CTRL, opId, noAlives);
			//Why don't I care about this failing here?
			if (!piggybackControlTraffic)
			{
				boolean bestEffortAcks = "true".equals(GLOBALS.valueFor("bestEffortAcks"));
				controlDispatcher.sendDownstream(ct, downOpIndex, !bestEffortAcks);
			}
			else
			{
				outputQueues.get(downOpIndex).sendToDownstream(ct);	
			}

			if (separateControlNet && enableDummies) { controlDispatcher.sendDummyDownstream(ct, downOpIndex); }
		}	
	}
	
	public void signalOpenBackupSession(int totalSizeST){
		int opId = processingUnit.getOperator().getOperatorId();
		LOG.debug("-> Opening backup session from: {}", opId);
		ControlTuple openSignal = new ControlTuple().makeOpenSignalBackup(opId);
		for(int i = 0; i < totalSizeST; i++){
			//controlDispatcher.sendOpenSessionWaitACK(openSignal, backupUpstreamIndex);
			controlDispatcher.sendOpenSessionWaitACK(openSignal, i);
//			controlDispatcher.sendUpstream(openSignal, i);
		}
		LOG.debug("-> SESSION opened from {}", opId);
	}
	
	public void signalCloseBackupSession(int starTopologySize){
		int opId = processingUnit.getOperator().getOperatorId();
		LOG.debug("-> Closing backup session from: {}", opId);
		ControlTuple closeSignal = new ControlTuple().makeCloseSignalBackup(opId, totalNumberOfChunks);
		for(int i = 0; i < starTopologySize; i++){
			controlDispatcher.sendCloseSession(closeSignal, i);
		}
	}

	public void manageBackupUpstreamIndex(int opId){
		//Firstly, I configure my upstreamBackupIndex, which is the index of the operatorId coming in this message (the one in charge of managing it)
//		int newIndex = opContext.findUpstream(opId).index();
		int newIndex = processingUnit.getOperator().getOpContext().findUpstream(opId).index();
		if(backupUpstreamIndex != -1 && newIndex != backupUpstreamIndex){
			//ControlTuple ct = new ControlTuple().makeInvalidateMessage(backupUpstreamIndex);
			ControlTuple ct = new ControlTuple().makeInvalidateMessage(processingUnit.getOperator().getOperatorId());
			
			controlDispatcher.sendUpstream(ct, backupUpstreamIndex);
		}
		//Set the new backup upstream index, this has been sent by the manager.
		this.setBackupUpstreamIndex(newIndex);
	}
	
	public void sendBackupState(ControlTuple ctB){
		int stateOwnerId = ctB.getBackupState().getOpId();
		LOG.debug(" -> Backuping state with owner: {} to NODE index: {}", stateOwnerId, backupUpstreamIndex);
//		controlDispatcher.sendUpstream_large(ctB, backupUpstreamIndex);
		controlDispatcher.sendUpstream(ctB, backupUpstreamIndex);
	}
	
	public void sendBlindData(ControlTuple ctB, int index){
		controlDispatcher.sendUpstream_blind(ctB, index);
	}
	
	public void setTotalNumberOfStateChunks(int number){
		this.totalNumberOfChunks = number;
		//coreProcessLogic.setTotalNumberOfChunks(number);
	}
	
	//Initial compute of upstreamBackupindex. This is useful for initial instantiations (not for splits, because in splits, upstreamIdx comes in the INIT_STATE)
	// This method is called only once during initialisation of the node (from setRuntime)
	public void configureUpstreamIndex(int upstreamSize){
		int ownInfo = Router.customHash(getNodeDescr().getNodeId());
//		int upstreamSize = opContext.upstreams.size();
		
		//source obviously cant compute this value
		if(upstreamSize == 0){
			LOG.warn("-> If this node is not the most upstream, there is a problem");
			return;
		}
		int upIndex = ownInfo%upstreamSize;
		upIndex = (upIndex < 0) ? upIndex*-1 : upIndex;
		
		//Update my information
		System.out.println("% ConfigureUpstreamIndex");
		this.setBackupUpstreamIndex(upIndex);
		LOG.debug("-> The new Upstream backup INDEX is: "+backupUpstreamIndex);
	}
	
	public void reconfigureUpstreamBackupIndex(int upstreamSize){
		LOG.debug("-> Reconfiguring upstream backup index");
		//First I compute my own info, which is the hash of my id.
		/** There is a reason to hash the opId. Imagine upSize=2 and opId of downstream 2, 4, 6, 8... So better to mix the space*/
		int ownInfo = Router.customHash(nodeDescr.getNodeId());
//		int upstreamSize = opContext.upstreams.size();
		int upIndex = ownInfo%upstreamSize;
		//Since ownInfo (hashed) may be negative, this enforces the final value is always positive.
		upIndex = (upIndex < 0) ? upIndex*-1 : upIndex;
		//If the upstream is different from previous sent, then additional management is necessary
		//In particular, invalidate the management of my state in the previous operator in charge to do so...
		//... and notify the new operator in charge of my OPID
		//UPDATE!! AND send STATE in case this operator is backuping state
		if(upIndex != backupUpstreamIndex){
			LOG.debug("-> Upstream backup has changed...");
			//Invalidate old Upstream state
			//ControlTuple ct = new ControlTuple().makeInvalidateMessage(backupUpstreamIndex);
			ControlTuple ct = new ControlTuple().makeInvalidateMessage(processingUnit.getOperator().getOperatorId());
			controlDispatcher.sendUpstream(ct, backupUpstreamIndex);
		
			//Update my information
			System.out.println("% ReconfigureUpstreamIndex");
			this.setBackupUpstreamIndex(upIndex);
			
			//Without waiting for the counter, we backup the state right now, (in case operator is stateful)
			if(processingUnit.isNodeStateful()){
				LOG.debug("-> sending BACKUP_STATE to the new manager of my state");
				
				((StatefulProcessingUnit)processingUnit).lockFreeParallelCheckpointAndBackupState();
			}
		}
	}
}
