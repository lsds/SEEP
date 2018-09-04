/*******************************************************************************
 * Copyright (c) 2013 Imperial College London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial design and implementation
 *     Martin Rouaux - Changes to support operator scale-in of operators
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.infrastructure.master;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.Collections;
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.api.QueryPlan;
import uk.ac.imperial.lsds.seep.api.ScaleOutIntentBean;
import uk.ac.imperial.lsds.seep.comm.ConnHandler;
import uk.ac.imperial.lsds.seep.comm.NodeManagerCommunication;
import uk.ac.imperial.lsds.seep.comm.RuntimeCommunicationTools;
import uk.ac.imperial.lsds.seep.comm.routing.Router;
import uk.ac.imperial.lsds.seep.comm.serialization.ControlTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.BatchTuplePayload;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.Payload;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.TuplePayload;
import uk.ac.imperial.lsds.seep.comm.serialization.serializers.ArrayListSerializer;
import uk.ac.imperial.lsds.seep.elastic.ElasticInfrastructureUtils;
import uk.ac.imperial.lsds.seep.elastic.NodePoolEmptyException;
import uk.ac.imperial.lsds.seep.elastic.ParallelRecoveryException;
import uk.ac.imperial.lsds.seep.infrastructure.OperatorDeploymentException;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.master.MonitorMaster;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.master.MonitorMasterFactory;
import uk.ac.imperial.lsds.seep.manet.Query;
import uk.ac.imperial.lsds.seep.operator.Connectable;
import uk.ac.imperial.lsds.seep.operator.EndPoint;
import uk.ac.imperial.lsds.seep.operator.InputDataIngestionMode;
import uk.ac.imperial.lsds.seep.operator.Operator;
import uk.ac.imperial.lsds.seep.operator.OperatorContext;
import uk.ac.imperial.lsds.seep.operator.OperatorStaticInformation;
import uk.ac.imperial.lsds.seep.operator.StatefulOperator;
import uk.ac.imperial.lsds.seep.operator.OperatorContext.PlacedOperator;
import uk.ac.imperial.lsds.seep.runtimeengine.DisposableCommunicationChannel;
import uk.ac.imperial.lsds.seep.state.StateWrapper;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.comm.serialization.MetricsTuple;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.master.MonitorMasterListener;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.PolicyRules;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType;

/**
* Infrastructure. This class is in charge of dealing with nodes, deployment and profiling of the system.
*/


public class Infrastructure {

	final private Logger LOG = LoggerFactory.getLogger(Infrastructure.class);
	
	int value = Integer.parseInt(GLOBALS.valueFor("maxLatencyAllowed"));
	static public MasterStatisticsHandler msh = new MasterStatisticsHandler();
	
	private int baseId = Integer.parseInt(GLOBALS.valueFor("baseId"));
	
	private ArrayDeque<Node> nodeStack = new ArrayDeque<Node>();
	private boolean havePopped = false;
	private int numberRunningMachines = 0;

	private boolean systemIsRunning = false;
	private String pathToQueryDefinition = null;
	
	///\todo{Put this in a map{query->structure} and refer back to it properly}
	private ArrayList<Operator> ops = new ArrayList<Operator>();
	// States of the query
	private ArrayList<StateWrapper> states = new ArrayList<StateWrapper>();
	//public Map<Integer,QuerySpecificationI> elements = new HashMap<Integer, QuerySpecificationI>();
	public Map<Integer,Connectable> elements = new HashMap<Integer, Connectable>();
	//More than one source is supported
	private ArrayList<Operator> src = new ArrayList<Operator>();
	private Operator snk;
	//Mapping of nodeId-operator
	private Map<Integer, Operator> queryToNodesMapping = new HashMap<Integer, Operator>();
	//map with star topology information
	private ArrayList<EndPoint> starTopology = new ArrayList<EndPoint>();
	
	private RuntimeCommunicationTools rct = new RuntimeCommunicationTools();
	private NodeManagerCommunication bcu = new NodeManagerCommunication();
	private ElasticInfrastructureUtils eiu;

	private ManagerWorker manager = null;
	private MonitorMaster monitorMaster = null;
	private int port;
	
    // Scaling policy rules. These are needed by the MonitorMaster instance.
    private PolicyRules policyRules;
    
    public static int RESET_SYSTEM_STABLE_TIME_OP_ID = -666;
    
	public Infrastructure(int listeningPort) {
		this.port = listeningPort;
 	}
	
	public boolean isSystemRunning(){
		return systemIsRunning;
	}
	
	public ArrayList<EndPoint> getStarTopology(){
		return starTopology;
	}
	
	public void addSource(Operator op){
		this.src.add(op);
	}
	
	/** 
	 * For now, the query plan is directly submitted to the infrastructure. to support multi-query, first step is to have a map with the queries, 
	 * and then, for the below methods, indicate the query id that needs to be accessed.
	**/
	public void loadQuery(QueryPlan qp) {
        // We can only start the monitor master process at this point because
        // we need to know the scaling rules in advance. These are only accessible
        // through the QueryPlan.
        LOG.debug("-> MonitorMaster running");
        MonitorMasterFactory factory = new MonitorMasterFactory(this, qp.getPolicyRules());
        monitorMaster = factory.create();
        
        // We need a listener to reset the system stable time
        monitorMaster.addListener(new MonitorMasterListener() {

            @Override
            public int getOperatorId() {
                return RESET_SYSTEM_STABLE_TIME_OP_ID;
            }

            @Override
            public void onTupleReceived(MetricsTuple tuple) {
                Infrastructure.msh.setSystemStableTime(System.currentTimeMillis());
            }
        });
        
		Thread monitorManagerT = new Thread(monitorMaster);
		monitorManagerT.start();
        
		ops = qp.getOps();
		states = qp.getStates();
		elements = qp.getElements();
		src = qp.getSrc();
		snk = qp.getSnk();

		///\todo{log what is going on here}
		queryToNodesMapping = qp.getMapOperatorToNode();
		configureRouterStatically();
		
		for(Operator op : ops){
			// Never will be empty, as there are no sources here (so all operators will have at least one upstream
			makeDataIngestionModeLocalToOp(op);
		}
		// Then we do the inversion with sink, since this also has upstream operators.
		makeDataIngestionModeLocalToOp(snk);
		
		ArrayList<ScaleOutIntentBean> soib = new ArrayList<ScaleOutIntentBean>();
		if(!qp.getScaleOutIntents().isEmpty()){
			LOG.debug("-> Manual static scale out");
			soib = eiu.staticInstantiateNewReplicaOperator(qp.getScaleOutIntents(), qp);
		}
		// The default and preferred option, used
		else if (!qp.getPartitionRequirements().isEmpty()){
			LOG.debug("-> Automatic static scale out");
			soib = eiu.staticInstantiationNewReplicaOperators(qp);
		}
		// After everything is set up, then we scale out ops
		eiu.executeStaticScaleOutFromIntent(soib);
	}
	
	private void makeDataIngestionModeLocalToOp(Operator op){
		// Never will be empty, as there are no sources here (so all operators will have at least one upstream
		for(Entry<Integer, InputDataIngestionMode> entry : op.getInputDataIngestionModeMap().entrySet()){
			for(Operator upstream : ops){
				if(upstream.getOperatorId() == entry.getKey()){
					LOG.debug("-> Op: {} consume from Op: {} with {}",upstream.getOperatorId(), op.getOperatorId(), entry.getValue());
					// Use opContext to make an operator understand how it consumes data from its upstream
					upstream.getOpContext().setInputDataIngestionModePerUpstream(op.getOperatorId(), entry.getValue());
				}
			}
		}
	}
	
	private boolean checkReplicaOperator(Operator op, int opId){
		if(op.getOpContext().getOriginalUpstreamFromOpId(opId) != opId){
			return false;
		}
		return true;
	}
	
	public void configureRouterStatically(){
		for(Operator op: ops){
			LOG.info("-> Configuring Routing for OP {} ...", op.getOperatorId());
			boolean requiresLogicalRouting = op.getOpContext().doesRequireLogicalRouting();
			HashMap<Integer, ArrayList<Integer>> routeInfo = op.getOpContext().getRouteInfo();
			Router r = new Router(requiresLogicalRouting, routeInfo);
			// Configure routing implementations of the operator
			ArrayList<Operator> downstream = new ArrayList<Operator>();
			for(Integer i : op.getOpContext().getOriginalDownstream()){
				downstream.add(this.getOperatorById(i));
			}
			r.configureRoutingImpl(op.getOpContext(), downstream);
			op.setRouter(r);
			LOG.info("Configuring Routing for OP {} ...DONE", op.getOperatorId());
		}
	}
	
	public void setEiu(ElasticInfrastructureUtils eiu){
		this.eiu = eiu;
	}
	
	public void setPathToQueryDefinition(String pathToQueryDefinition){
		this.pathToQueryDefinition = pathToQueryDefinition;
	}
	
	public String getPathToQueryDefinition(){
		return pathToQueryDefinition;
	}

	public MonitorMaster getMonitorMaster(){
		return monitorMaster;
	}
	
	public ArrayList<Operator> getOps() {
		return ops;
	}
	
//	public Map<Integer, QuerySpecificationI> getElements() {
//		return elements;
//	}
	
	public Map<Integer, Connectable> getElements() {
		return elements;
	}
	
	public int getNodePoolSize(){
		return nodeStack.size();
	}

	public int getNumberRunningMachines(){
		return numberRunningMachines;
	}
	
	public RuntimeCommunicationTools getRCT() {
		return rct;
	}
	
	public NodeManagerCommunication getBCU(){
		return bcu;
	}
	
	public ElasticInfrastructureUtils getEiu() {
		return eiu;
	}
	
	public synchronized int getBaseId() {
		return baseId;
	}
	
	public void addNode(Node n) {
		if (havePopped) { throw new RuntimeException("Attempt to addNode when we've already started removing nodes from pool!!"); }
		nodeStack.push(n);
		LOG.debug("-> New Node: {}", n);
		LOG.debug("-> Num nodes: {}", getNodePoolSize());
	}
	
	public void updateContextLocations(Operator o) {
		for (Connectable op: elements.values()) {
			if (op!=o){
				setDownstreamLocationFromPotentialDownstream(o, op);
				setUpstreamLocationFromPotentialUpstream(o, op);
			}
		}
	}

	private void setDownstreamLocationFromPotentialDownstream(Connectable target, Connectable downstream) {
		for (PlacedOperator op: downstream.getOpContext().upstreams) {
			if (op.opID() == target.getOperatorId()) {
				target.getOpContext().setDownstreamOperatorStaticInformation(downstream.getOperatorId(), downstream.getOpContext().getOperatorStaticInformation());
			}
		}
	}
	
	private void setUpstreamLocationFromPotentialUpstream(Connectable target, Connectable upstream) {
		for (PlacedOperator op: upstream.getOpContext().downstreams) {
			if (op.opID() == target.getOperatorId()) {
				target.getOpContext().setUpstreamOperatorStaticInformation(upstream.getOperatorId(), upstream.getOpContext().getOperatorStaticInformation());
			}
		}
	}
	
	/// \todo {Any thread that it is started should be stopped somehow}
	public void startInfrastructure(){
		LOG.debug("-> ManagerWorker running");
		manager = new ManagerWorker(this, port);
		Thread centralManagerT = new Thread(manager, "managerWorkerT");
		centralManagerT.start();
	}

	public void stopWorkers(){
		// Stop monitor manager
        LOG.debug("-> MonitorMaster stoping");
		monitorMaster.stop();
	}
	
	public void localMapPhysicalOperatorsToNodes(){		
		Map<Integer, String> constraints = readMappingConstraints();
		  		
		// First assign any operators in constrained mappings to the corresponding node (ip addr, port).
		for(Entry<Integer, Operator> e : queryToNodesMapping.entrySet()){
			int opId = e.getValue().getOperatorId();
			Node a = null;
			if (constraints.containsKey(opId))
			{
				String[] splits = constraints.get(opId).split(":");
				
				InetAddress nodeIp = null;
				try {
					nodeIp = InetAddress.getByName(splits[0]);
				} catch (UnknownHostException e1) {
					LOG.error("Unknown address in constraint:"+constraints.get(opId));
					System.exit(1);
				}
				int nodePort = Integer.parseInt(splits[1]);
				
				try {
					a = getNodeFromPool(nodeIp, nodePort);
				} catch (NodePoolEmptyException e1) {
					LOG.error("Node pool empty");
					System.exit(1);
				}
				LOG.info("-> Mapping OP: {} to Node: {}", opId, a);
				placeNew(e.getValue(), a);
			}
		}
		
		//	Finally, for operators with no pre-existing constraints, assign them to one of the remaining nodes.
		for(Entry<Integer, Operator> e : queryToNodesMapping.entrySet()){
			int opId = e.getValue().getOperatorId();
			Node a = null;
			if (!constraints.containsKey(opId))
			{	
				try {
					a = getNodeFromPool();
				} 
				catch (NodePoolEmptyException e1) {
					LOG.error("Node pool empty");
					System.exit(1);
				}
				constraints.put(opId, ""+a.getIp().getHostAddress() + ":"+a.getPort());
				LOG.info("Adding constraint: "+opId +"->"+constraints.get(opId));
				LOG.info("-> Mapping OP: {} to Node: {}", opId, a);
				placeNew(e.getValue(), a);
			}
		}
				
		LOG.info("-> All operators have been mapped");
		Query q = buildFrontierQuery();
		LOG.info("Finished building frontier query.");
		for(Operator o : queryToNodesMapping.values()){
			LOG.info("OP: {}, CONF: {}", o.getOperatorId(), o);
			o.getOpContext().setFrontierQuery(q);
		}
		writeMappingConstraints(constraints);
	}
	
	private Map<Integer, String> readMappingConstraints()
	{
		Map<Integer, String> constraints = new HashMap<>();
		File f = new File("mappingRecordIn.txt");
		if (!f.exists()) { f = new File("../mappingRecordIn.txt"); }
		if (!f.exists()) { f = new File("../../mappingRecordIn.txt"); }
		if (!f.exists()) { LOG.warn("No mapping constraints found.");	return constraints; }

		FileReader fr;
		try {
			fr = new FileReader(f);
			BufferedReader br = new BufferedReader(fr);
			String constraint;
			while((constraint = br.readLine()) != null)
			{
				String[] splits = constraint.split(",");
				int opId = Integer.parseInt(splits[0]);
				if (constraints.containsKey(opId) && !constraints.get(opId).equals(splits[1]))
				{
					LOG.error("Logic error: "+ constraints + ",constraint");
					System.exit(1);
				}
				constraints.put(opId, splits[1]);
			}
		} catch (Exception e) {
			LOG.error("Unexpected exception:"+e);
			System.exit(1);
		}

		return constraints;
	}
	
	private void writeMappingConstraints(Map<Integer, String> mappingConstraints)
	{
		FileWriter fw = null;
		try {
			fw = new FileWriter("mappingRecordOut.txt",true);
			for (Entry<Integer, String> constraint: mappingConstraints.entrySet())
			{
				fw.write(constraint.getKey() + ","+constraint.getValue() +"\n");
			}
		} catch (IOException e) {
			LOG.error("Could not write mapping record!");
			System.exit(1);
		}
		finally { 
			if (fw != null) { 
				try {
					fw.close();
				} catch (IOException e) {} 
			} 
		}
	}
	
	private Query buildFrontierQuery()
	{
		String queryType = GLOBALS.valueFor("queryType"); 
		if (queryType.equals("chain"))
		{
			return buildChainQuery();
		}
		else if (queryType.equals("join"))
		{
			return buildJoinQuery();
		}
		else if (queryType.equals("nameAssist"))
		{
			return buildNameAssist();
		}
		else if (queryType.equals("fr"))
		{
			return buildFaceRecognition();
		}
		else if (queryType.equals("fdr"))
		{
			return buildFaceDetectorRecognition();
		}
		else if (queryType.equals("heatMap"))
		{
			return buildHeatMap();
		}
		else if (queryType.equals("leftJoin"))
		{
			return buildLeftJoin();
		}
		else if (queryType.equals("frJoin"))
		{
			return buildFaceRecognitionJoin();
		}
		else { throw new RuntimeException("Logic error, unknown query type."); }
	}
	
	private Query buildChainQuery()
	{
		LOG.info("Building chain query.");
		int chainLength = Integer.parseInt(GLOBALS.valueFor("chainLength"));
		return buildChainQuery(chainLength);
	}

	private Query buildChainQuery(int chainLength)
	{
		//TreeMap logicalTopology
		TreeMap<Integer, Integer[]> logicalTopology = new TreeMap<Integer, Integer[]>();
		//TODO: Generalize this for non-face recognition queries.
		
		for (int i = 1; i <= chainLength+2; i++)
		{
			if (i == 1) { logicalTopology.put(1, new Integer[]{}); } //src
			else { logicalTopology.put(i, new Integer[]{i - 1}); }
		}
		/*
		logicalTopology.put(1, new Integer[]{}); //src
		logicalTopology.put(2, new Integer[]{1}); //face detector
		logicalTopology.put(3, new Integer[]{2}); //face recognizer
		logicalTopology.put(4, new Integer[]{3}); //sink
		*/
		
		//TreeMap log2Phys
		//use SEEP operator ids here for now. Will need to also store operator id <-> (node_addr, port) mapping
		//Will probably need to change the getCost function in LinkCostHandler too to look up the costs by node_addr
		// (from the op id).
		TreeMap<Integer, Set<Integer>> log2phys = new TreeMap<>();
		Map<Integer, InetAddress> phys2addr = new HashMap<>();

		//Walk the ops, starting at the source.
		Set<Integer> srcPhys = new HashSet<>();
		if (src.size() > 1) { throw new RuntimeException("TODO"); }
		Operator currentSrc = src.get(0);
		srcPhys.add(currentSrc.getOperatorId());
		log2phys.put(1, srcPhys);
		phys2addr.put(currentSrc.getOperatorId(), currentSrc.getOpContext().getOperatorStaticInformation().getMyNode().getIp());

		LOG.info("Source op id="+currentSrc.getOperatorId()+",physAddr="+phys2addr.get(srcPhys));
		int numDownstreams = currentSrc.getOpContext().getDownstreamSize();
		int downstreamLogicalIndex = 2;
		Operator current = currentSrc;
		while (numDownstreams > 0)
		{
			LOG.info("Number of downstreams for op "+ current.getOperatorId()+"= "+numDownstreams);
			Set<Integer> downstreamPhys = new HashSet<Integer>();

			//TODO: Complete hack. For the FR query, enough to know the downstreams of one
			//physical replica to know the downstreams of all.
			Operator next = null;
			for (PlacedOperator downstreamPlacement : current.getOpContext().downstreams)
			{
				downstreamPhys.add(downstreamPlacement.opID());
				next = getOp(downstreamPlacement.opID());
				phys2addr.put(downstreamPlacement.opID(), next.getOpContext().getOperatorStaticInformation().getMyNode().getIp());
				LOG.info("Added op "+ current.getOperatorId()+" downstream with id="+downstreamPlacement.opID()+", ip="+phys2addr.get(downstreamPlacement.opID()));
			}
			log2phys.put(downstreamLogicalIndex, downstreamPhys);
			downstreamLogicalIndex++;
			numDownstreams = next.getOpContext().getDownstreamSize();
			current = next;
		}

		return new Query(logicalTopology, log2phys, phys2addr);
	}
	
	private Query buildFaceRecognition()
	{
		return buildChainQuery(2);
	}

	private Query buildFaceDetectorRecognition()
	{
		return buildChainQuery(1);
	}
	
	private Query buildHeatMap()
	{
		//TreeMap logicalTopology
		TreeMap<Integer, Integer[]> logicalTopology = new TreeMap<Integer, Integer[]>();
		int nSources = Integer.parseInt(GLOBALS.valueFor("sources"));
		int maxFanIn = Integer.parseInt(GLOBALS.valueFor("fanin"));
		int nSinks = Integer.parseInt(GLOBALS.valueFor("sinks"));
		
		int lid = 0;
		for (; lid < nSources+1; lid++)
		{
			logicalTopology.put(lid+1, new Integer[]{});
		}
		
		Map<Integer, Integer> downstreamLogicalIds = new HashMap<>();
		int height = (int) (Math.log(nSources) / Math.log(maxFanIn));
		int[] levelOps = new int[height+1];
		levelOps[0] = nSources;
		int nextChild = 1;
		int nOps = 0;
		for (int h = 1; h < height+1; h++)
		{
			levelOps[h] = levelOps[h-1] / maxFanIn;
			if (levelOps[h-1] % maxFanIn > 0) { levelOps[h]++; }
			int levelStart = lid;
			for (; lid < levelStart + levelOps[h]; lid++)
			{
				int numChildren = levelOps[h-1] / levelOps[h];
				int remainder = levelOps[h-1] % levelOps[h];
				if (remainder > 0 && remainder < lid - levelStart) { numChildren++; }
				
				Integer[] children = new Integer[numChildren];
				for (int childIndex = 0; childIndex < numChildren; childIndex++)
				{
					LOG.debug("Setting "+lid+"(level="+h+") as parent of "+nextChild);
					children[childIndex] = nextChild;
					downstreamLogicalIds.put(nextChild, lid);
					nextChild++;
				}
				LOG.debug("Logical op "+lid+ " children="+numChildren);
				logicalTopology.put(lid, children);
				nOps++;
			}
		}
				
		int sinkStart = lid;
		for (; lid < sinkStart+nSinks; lid++)
		{
			if (levelOps[height] != 1) { throw new RuntimeException("Logic error."); }
			//if (nSinks > 1) { throw new RuntimeException("TODO: nSinks > 1"); }
			logicalTopology.put(lid, new Integer[]{sinkStart-1});
			downstreamLogicalIds.put(sinkStart-1, lid);
		}
		
		int totalLogicalOps = nSources+nSinks+nOps;
		LOG.info("Created logical topology: "+logicalTopology);
		if (lid != totalLogicalOps+1) { throw new RuntimeException("Logic error: lid="+lid+", total logical ops="+ totalLogicalOps); }
		if (logicalTopology.size() != totalLogicalOps) 
		{ 
			throw new RuntimeException("Logic error: log.top.sz="+logicalTopology.size()+", total logical ops="+totalLogicalOps);
		}
		
		TreeMap<Integer, Set<Integer>> log2phys = new TreeMap<>();
		Map<Integer, InetAddress> phys2addr = new HashMap<>();
		
		Operator currentSrc = null;
		//Walk the ops, starting at the source.
		Map<Integer, Operator> log2origOp = new HashMap<>();
		for (int i = 0; i < nSources; i++)
		{
			Set<Integer> srcPhys = new HashSet<>();
			currentSrc = src.get(i);
			srcPhys.add(currentSrc.getOperatorId());
			log2phys.put(i+1, srcPhys);
			phys2addr.put(currentSrc.getOperatorId(), currentSrc.getOpContext().getOperatorStaticInformation().getMyNode().getIp());
			LOG.info("Source op id="+currentSrc.getOperatorId()+",physAddr="+phys2addr.get(currentSrc.getOperatorId())+",logicalId="+(i+1));
			
			log2origOp.put(i+1, currentSrc);
		}
		
		LOG.debug("Source logical ids: "+log2origOp.keySet());
		//For each logical op id in downstreamLogicalIds,
		//get its logical downstream (if any) and save the mapping to the
		//corresponding set of replicas. In addition, store a mapping
		//from the downstream logical id to one of the replicated operators
		//so it can in turn create the log2phys mapping for its logical downstream
		Map<Integer, Operator> nextLog2OrigOp = new HashMap<>();
		while (!log2origOp.isEmpty())
		{
			LOG.debug("Getting downstreams of "+log2origOp.keySet());
			for (Integer currentLogId : log2origOp.keySet())
			{
				Operator current = log2origOp.get(currentLogId);
				Operator nextDownstream = null;
				Set<Integer> downstreamPhys = new HashSet<Integer>();
				
				for (PlacedOperator downstreamPlacement : current.getOpContext().downstreams)
				{
					boolean initialReplica = nextDownstream == null;
					downstreamPhys.add(downstreamPlacement.opID());
					nextDownstream = getOp(downstreamPlacement.opID());
					if (initialReplica)
					{
						nextLog2OrigOp.put(downstreamLogicalIds.get(currentLogId), nextDownstream);
					}
					phys2addr.put(downstreamPlacement.opID(), nextDownstream.getOpContext().getOperatorStaticInformation().getMyNode().getIp());
					LOG.info("Added op "+ current.getOperatorId()+" downstream with id="+downstreamPlacement.opID()+", ip="+phys2addr.get(downstreamPlacement.opID()));
				}
				if (downstreamLogicalIds.get(currentLogId) != null)
				{
					LOG.debug("Adding logical id "+downstreamLogicalIds.get(currentLogId)+" phys nodes: "+ downstreamPhys);
					log2phys.put(downstreamLogicalIds.get(currentLogId), downstreamPhys);
				}
			}
			log2origOp.clear();
			LOG.debug("Next log2origOp ids: "+nextLog2OrigOp.keySet());
			log2origOp.putAll(nextLog2OrigOp);
			nextLog2OrigOp.clear();
		}
		
		return new Query(logicalTopology, log2phys, phys2addr);
	}
	
	private Query buildJoinQuery()
	{
		//TreeMap logicalTopology
		TreeMap<Integer, Integer[]> logicalTopology = new TreeMap<Integer, Integer[]>();
		
		//TODO: Tmp hack for join query with 2 sources, 1 join op (potentially replicated) and the sink.
		logicalTopology.put(1, new Integer[]{}); //src 1
		logicalTopology.put(2, new Integer[]{}); //src 2
		logicalTopology.put(3, new Integer[]{1,2}); //join
		logicalTopology.put(4, new Integer[]{3}); //snk
		
		//TreeMap log2Phys
		//use SEEP operator ids here for now. Will need to also store operator id <-> (node_addr, port) mapping
		//Will probably need to change the getCost function in LinkCostHandler too to look up the costs by node_addr
		// (from the op id).
		TreeMap<Integer, Set<Integer>> log2phys = new TreeMap<>();
		Map<Integer, InetAddress> phys2addr = new HashMap<>();

		if (src.size() != 2) { throw new RuntimeException("TODO"); }
		
		Operator currentSrc = null;
		//Walk the ops, starting at the source.
		for (int i = 0; i < 2; i++)
		{
			Set<Integer> srcPhys = new HashSet<>();
			currentSrc = src.get(i);
			srcPhys.add(currentSrc.getOperatorId());
			log2phys.put(i+1, srcPhys);
			phys2addr.put(currentSrc.getOperatorId(), currentSrc.getOpContext().getOperatorStaticInformation().getMyNode().getIp());
			LOG.info("Source op id="+currentSrc.getOperatorId()+",physAddr="+phys2addr.get(srcPhys));
		}

		int numDownstreams = currentSrc.getOpContext().getDownstreamSize();
		int downstreamLogicalIndex = 3;
		
		Operator current = currentSrc;
		while (numDownstreams > 0)
		{
			LOG.info("Number of downstreams for op "+ current.getOperatorId()+"= "+numDownstreams);
			Set<Integer> downstreamPhys = new HashSet<Integer>();

			//TODO: Complete hack. For the simple join query, enough to know the downstreams of one
			//physical replica to know the downstreams of all.
			Operator next = null;
			for (PlacedOperator downstreamPlacement : current.getOpContext().downstreams)
			{
				downstreamPhys.add(downstreamPlacement.opID());
				next = getOp(downstreamPlacement.opID());
				phys2addr.put(downstreamPlacement.opID(), next.getOpContext().getOperatorStaticInformation().getMyNode().getIp());
				LOG.info("Added op "+ current.getOperatorId()+" downstream with id="+downstreamPlacement.opID()+", ip="+phys2addr.get(downstreamPlacement.opID()));
			}
			log2phys.put(downstreamLogicalIndex, downstreamPhys);
			downstreamLogicalIndex++;
			numDownstreams = next.getOpContext().getDownstreamSize();
			current = next;
		}

		return new Query(logicalTopology, log2phys, phys2addr);	
	}

	private Query buildLeftJoin()
	{
		TreeMap<Integer, Integer[]> logicalTopology = new TreeMap<Integer, Integer[]>();
		int nSources = Integer.parseInt(GLOBALS.valueFor("sources"));
		int maxFanIn = Integer.parseInt(GLOBALS.valueFor("fanin"));
		if (maxFanIn != 2) { throw new RuntimeException("TODO."); }
		int nSinks = Integer.parseInt(GLOBALS.valueFor("sinks"));
		if (nSinks != 1) { throw new RuntimeException("TODO."); }
		
		int lid = 1;
		for (; lid < nSources+1; lid++)
		{
			logicalTopology.put(lid, new Integer[]{});
		}
		
		Map<Integer, Integer> downstreamLogicalIds = new HashMap<>();

		//join op ids = [nSources+1,..,nSources + 1 + nSources - 1]
		for (; lid < 2* nSources; lid++)
		{
			Integer[] children = new Integer[2];
			if (lid == nSources + 1) { children[0] = lid - nSources; }
			else { children[0] = lid - 1; }
			children[1] = lid - nSources + 1;
			logicalTopology.put(lid, children); 
		}
		
		//Sink	
		logicalTopology.put(lid, new Integer[]{lid-1});
		int sinkId = lid;

		int nOps = nSources - 1;	
		int totalLogicalOps = nSources+nSinks+nOps;
		LOG.info("Created logical topology: "+logicalTopology);
		if (lid != totalLogicalOps) { throw new RuntimeException("Logic error: lid="+lid+", total logical ops="+ totalLogicalOps); }
		if (logicalTopology.size() != totalLogicalOps) 
		{ 
			throw new RuntimeException("Logic error: log.top.sz="+logicalTopology.size()+", total logical ops="+totalLogicalOps);
		}
		
		TreeMap<Integer, Set<Integer>> log2phys = new TreeMap<>();
		Map<Integer, InetAddress> phys2addr = new HashMap<>();
		
		if (src.size() != nSources) { throw new RuntimeException("Logic error src.size="+src.size() + ", nSources="+nSources); }	
		Operator currentSrc = null;
		//Walk the ops, starting at the source.
		Map<Integer, Operator> log2origOp = new HashMap<>();
		for (int i = 0; i < nSources; i++)
		{
			Set<Integer> srcPhys = new HashSet<>();
			currentSrc = src.get(i);
			srcPhys.add(currentSrc.getOperatorId());
			log2phys.put(i+1, srcPhys);
			phys2addr.put(currentSrc.getOperatorId(), currentSrc.getOpContext().getOperatorStaticInformation().getMyNode().getIp());
			LOG.info("Source op id="+currentSrc.getOperatorId()+",physAddr="+phys2addr.get(currentSrc.getOperatorId())+",logicalId="+(i+1));

		}
	
		//Iterate through the rightmost inputs for the join ops.
		Operator lastOp = null;
		for (int i = 1; i < nSources; i++)
		{
			currentSrc = src.get(i);
			Set<Integer> downstreamPhys = new HashSet<Integer>();
			for (PlacedOperator downstreamPlacement : currentSrc.getOpContext().downstreams)
			{
				downstreamPhys.add(downstreamPlacement.opID());
				Operator nextDownstream = getOp(downstreamPlacement.opID());
				lastOp = nextDownstream;
				phys2addr.put(downstreamPlacement.opID(), nextDownstream.getOpContext().getOperatorStaticInformation().getMyNode().getIp());
				LOG.info("Added op "+ currentSrc.getOperatorId()+" downstream with id="+downstreamPlacement.opID()+", ip="+phys2addr.get(downstreamPlacement.opID()));
			}	
			int currentLogId = i+1+nSources-1;
			log2phys.put(currentLogId, downstreamPhys);
			LOG.debug("Adding logical id "+currentLogId+" phys nodes: "+ downstreamPhys);
		}

		Set<Integer> sinkPhys = new HashSet<>();
		for (PlacedOperator sinkPlacement : lastOp.getOpContext().downstreams)
		{
			sinkPhys.add(sinkPlacement.opID());
			phys2addr.put(sinkPlacement.opID(), getOp(sinkPlacement.opID()).getOpContext().getOperatorStaticInformation().getMyNode().getIp());
			LOG.info("Added snk replica with id="+sinkPlacement.opID()+", ip="+phys2addr.get(sinkPlacement.opID()));
		}

		log2phys.put(sinkId, sinkPhys);
		
		return new Query(logicalTopology, log2phys, phys2addr);
	}

	private Query buildFaceRecognitionJoin()
	{
		
		TreeMap<Integer, Integer[]> logicalTopology = new TreeMap<Integer, Integer[]>();
		//TODO: Generalize this for non-face recognition queries.
		
		logicalTopology.put(1, new Integer[]{});
		logicalTopology.put(2, new Integer[]{});
		logicalTopology.put(3, new Integer[]{1});
		logicalTopology.put(4, new Integer[]{2});
		logicalTopology.put(5, new Integer[]{3,4});
		logicalTopology.put(6, new Integer[]{5});

		if (src.size() > 2) { throw new RuntimeException("TODO"); }

		TreeMap<Integer, Set<Integer>> log2phys = new TreeMap<>();
		Map<Integer, InetAddress> phys2addr = new HashMap<>();

		for (int i = 0; i < src.size(); i++)
		{
			Set<Integer> srcPhys = new HashSet<>();
			Operator currentSrc = src.get(i);
			srcPhys.add(currentSrc.getOperatorId());
			log2phys.put(i+1, srcPhys);
			phys2addr.put(currentSrc.getOperatorId(), currentSrc.getOpContext().getOperatorStaticInformation().getMyNode().getIp());
		}

		Operator lastOp = null;
		for (int i = 2; i < 4; i++)
		{
			Set<Integer> fdPhys = new HashSet<>();
			for (PlacedOperator downstreamPlacement : src.get(i-2).getOpContext().downstreams)
			{
				fdPhys.add(downstreamPlacement.opID());
				Operator nextDownstream = getOp(downstreamPlacement.opID());
				lastOp = nextDownstream;
				phys2addr.put(downstreamPlacement.opID(), nextDownstream.getOpContext().getOperatorStaticInformation().getMyNode().getIp());
				LOG.info("Added op "+ src.get(i-2).getOperatorId()+" downstream with id="+downstreamPlacement.opID()+", ip="+phys2addr.get(downstreamPlacement.opID()));
			}	
			log2phys.put(i+1, fdPhys);
			LOG.debug("Adding logical id "+(i+1)+" phys nodes: "+ fdPhys);
		}

		Set<Integer> joinPhys = new HashSet<>();
		for (PlacedOperator downstreamPlacement: lastOp.getOpContext().downstreams)
		{
			joinPhys.add(downstreamPlacement.opID());
			Operator nextDownstream = getOp(downstreamPlacement.opID());
			lastOp = nextDownstream;
			phys2addr.put(downstreamPlacement.opID(), nextDownstream.getOpContext().getOperatorStaticInformation().getMyNode().getIp());
			LOG.info("Added op "+ lastOp.getOperatorId()+" downstream with id="+downstreamPlacement.opID()+", ip="+phys2addr.get(downstreamPlacement.opID()));
		}
		log2phys.put(5, joinPhys);

		Set<Integer> sinkPhys = new HashSet<>();
		for (PlacedOperator downstreamPlacement: lastOp.getOpContext().downstreams)
		{
			sinkPhys.add(downstreamPlacement.opID());
			Operator nextDownstream = getOp(downstreamPlacement.opID());
			lastOp = nextDownstream;
			phys2addr.put(downstreamPlacement.opID(), nextDownstream.getOpContext().getOperatorStaticInformation().getMyNode().getIp());
			LOG.info("Added op "+ lastOp.getOperatorId()+" downstream with id="+downstreamPlacement.opID()+", ip="+phys2addr.get(downstreamPlacement.opID()));
		}
		log2phys.put(6, sinkPhys);

		return new Query(logicalTopology, log2phys, phys2addr);
	}
	
	private Query buildNameAssist()
	{
		//TreeMap logicalTopology
		TreeMap<Integer, Integer[]> logicalTopology = new TreeMap<Integer, Integer[]>();
		
		//TODO: Tmp hack for join query with 2 sources, 1 join op (potentially replicated) and the sink.
		logicalTopology.put(1, new Integer[]{}); //video src
		logicalTopology.put(2, new Integer[]{}); //audio src
		logicalTopology.put(3, new Integer[]{1}); //face detector
		logicalTopology.put(4, new Integer[]{2}); //speech recognizer
		logicalTopology.put(5, new Integer[]{3,4}); //join
		logicalTopology.put(6, new Integer[]{5}); //snk
		
		//TreeMap log2Phys
		//use SEEP operator ids here for now. Will need to also store operator id <-> (node_addr, port) mapping
		//Will probably need to change the getCost function in LinkCostHandler too to look up the costs by node_addr
		// (from the op id).
		TreeMap<Integer, Set<Integer>> log2phys = new TreeMap<>();
		Map<Integer, InetAddress> phys2addr = new HashMap<>();

		if (src.size() != 2) { throw new RuntimeException("TODO"); }

		Set<Integer> allPhys = new HashSet<>();
		Operator currentSrc = null;
		//Walk the ops, starting at the source.
		for (int i = 0; i < 2; i++)
		{
			Set<Integer> srcPhys = new HashSet<>();
			currentSrc = src.get(i);
			srcPhys.add(currentSrc.getOperatorId());
			allPhys.add(currentSrc.getOperatorId());
			log2phys.put(i+1, srcPhys);
			phys2addr.put(currentSrc.getOperatorId(), currentSrc.getOpContext().getOperatorStaticInformation().getMyNode().getIp());
			LOG.info("Source op id="+currentSrc.getOperatorId()+",physAddr="+phys2addr.get(srcPhys));
		}
		
		ArrayList<Operator> currentOps = new ArrayList<>(src);
		ArrayList<Operator> nextOps = new ArrayList<>();
		
		int currentOpsIndex = 0;
		int nextLogicalId = currentOps.size() + 1;
		
		//Breadth first traverse logical ops and record all physical replicas.		
		while (currentOpsIndex < currentOps.size() || !nextOps.isEmpty())
		{
			if (currentOpsIndex >= currentOps.size())
			{
				currentOps = nextOps;
				nextOps = new ArrayList<>();
				currentOpsIndex = 0;
			}
			
			Operator current = currentOps.get(currentOpsIndex);
			int numDownstreams = current.getOpContext().getDownstreamSize();
			
			LOG.info("Number of downstreams for op "+ current.getOperatorId()+"= "+numDownstreams);
			Set<Integer> downstreamPhys = new HashSet<>();
			
			Operator next = null;
			for (PlacedOperator downstreamPlacement : current.getOpContext().downstreams)
			{
				if (allPhys.contains(downstreamPlacement.opID())) { break; }
				downstreamPhys.add(downstreamPlacement.opID());
				next = getOp(downstreamPlacement.opID());
				phys2addr.put(downstreamPlacement.opID(), next.getOpContext().getOperatorStaticInformation().getMyNode().getIp());
				LOG.info("Added op "+ current.getOperatorId()+" downstream with id="+downstreamPlacement.opID()+", ip="+phys2addr.get(downstreamPlacement.opID()));
			}
			
			if (next != null)
			{
				log2phys.put(nextLogicalId, downstreamPhys);
				nextOps.add(next);
				allPhys.addAll(downstreamPhys);
				nextLogicalId++;
			}
			
			currentOpsIndex++;
		}
		LOG.debug("Creating new frontier query, logTop="+logicalTopology+", log2phys="+log2phys+",phys2addr="+phys2addr);
		return new Query(logicalTopology, log2phys, phys2addr);	
	}


	
	private Operator getOp(int opId)
	{
		for (Operator op : ops)
		{
			if (op.getOperatorId() == opId) { return op; }
		}
		throw new RuntimeException("Logic error, no operator with id: "+opId);
	}
	
	public void createInitialStarTopology(){
		// We build the initialStarTopology
		for(Operator op : ops){
			// sources and sinks are not part of the starTopology
			if(!(op.getOpContext().isSink()) && !(op.getOpContext().isSource())){
				int opId = op.getOperatorId();
				InetAddress ip = op.getOpContext().getOperatorStaticInformation().getMyNode().getIp();
				DisposableCommunicationChannel oscc = new DisposableCommunicationChannel(opId, ip);
				starTopology.add(oscc);
			}
		}
		LOG.debug("Initial StarTopology Size: {}",starTopology.size());
		for(EndPoint ep : starTopology){
			LOG.debug("Op: {} IP: {}", ep.getOperatorId(), ((DisposableCommunicationChannel)ep).getIp().toString());
		}
	}
	
	public void addNodeToStarTopology(int opId, InetAddress ip){
		DisposableCommunicationChannel dcc = new DisposableCommunicationChannel(opId, ip);
		starTopology.add(dcc);
	}
	
	public void removeNodeFromStarTopology(int opId){
		for(int i = 0; i<starTopology.size(); i++){
			EndPoint ep = starTopology.get(i);
			if(ep.getOperatorId() == opId){
				starTopology.remove(i);
			}
		}
	}
	
	public byte[] getDataFromFile(String pathToQueryDefinition){
		FileInputStream fis = null;
		long fileSize = 0;
		byte[] data = null;
		try {
			//Open stream to file
			LOG.debug("Opening stream to file: {}", pathToQueryDefinition);
			File f = new File(pathToQueryDefinition);
			fis = new FileInputStream(f);
			fileSize = f.length();
			//Read file data
			data = new byte[(int)fileSize];
			int readBytesFromFile = fis.read(data);
			//Check if we have read correctly
			if(readBytesFromFile != fileSize){
				LOG.warn("Mismatch between read bytes and file size");
			}
			//Close the stream
			fis.close();
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			try {
				fis.close();
			} 
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return data;
	}
	
	public void deployCodeToAllOperators() throws CodeDeploymentException{
		LOG.debug("-> Deploying code to operators...");
		byte data[] = getDataFromFile(pathToQueryDefinition);
		//Send data to operators
		for(Operator op: ops){
			sendCode(op, data);
		}
		LOG.debug("-> Deploying code to operators...DONE");
	}
	
	public void deployCodeToOperator(Operator op){
		byte data[] = getDataFromFile(pathToQueryDefinition);
		sendCode(op, data);
	}
	
	public void broadcastState(StateWrapper s){
		for(Operator op: ops){
			Node node = op.getOpContext().getOperatorStaticInformation().getMyNode();
			bcu.sendObject(node, s);
		}
	}
	
	public void broadcastState(Operator op){
		for(StateWrapper s : states){
			Node node = op.getOpContext().getOperatorStaticInformation().getMyNode();
			bcu.sendObject(node, s);
		}
	}
	
	public void deployQuery() throws OperatorDeploymentException {
		LOG.debug("-> Deploying query...");
		//First broadcast the information regarding the initialStarTopology
		broadcastStarTopology();
		
  		//Deploy operators (push operators to nodes)
		for(Operator op: ops){
	     	//Establish the connection with the specified address
			LOG.debug("-> Deploying OP: ", op.getOperatorId());
			remoteOperatorInstantiation(op);
		}

		//Once all operators have been pushed to the nodes, we say that those are ready to run
		ArrayList<Thread> activeThreads = new ArrayList<Thread>();
		for(Operator op : ops){
			
			Thread t = new Thread(new ConnHandler(op, this));
//			//Establish the connection with the specified address
			LOG.debug("-> Configuring OP: {}", op.getOperatorId());
			//init(op);
			t.start();
			activeThreads.add(t);
		}
		// Then wait for active connections to die
		for(Thread t : activeThreads){
			try {
				t.join();
			}
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//Broadcast the registered states to all the worker nodes, so that these can register the classes in the custom class loader
		for(StateWrapper s : states){
			//Send every state to all the worker nodes
			broadcastState(s);
			LOG.debug("-> Broadcasting state {} to nodes", s);
		}
		
		//Finally, we tell the nodes to initialize all communications
		Map<Integer, Boolean> nodesVisited = new HashMap<Integer, Boolean>();
		for(Operator op : ops){
			// If we havent communicated to this node yet, we do
			if (!nodesVisited.containsKey(op.getOperatorId())){
				initRuntime(op);
				nodesVisited.put(op.getOperatorId(), true);
			}
		}
		LOG.debug("-> Deploying query...DONE");
		writeDeployComplete();
	}
	
	private void writeDeployComplete()
	{
		FileWriter fw = null;
		try {
			fw = new FileWriter("deployComplete.txt",true);
			fw.write("DONE\n");
		} catch (IOException e) {
			LOG.error("Could not write deploy complete!");
			System.exit(1);
		}
		finally { 
			if (fw != null) { 
				try {
					fw.close();
				} catch (IOException e) {} 
			} 
		}
	}

	public void reDeploy(Node n){

		System.out.println("REDEPLOY-operators with ip: "+n.toString());

		//Redeploy operators
		for(Connectable op: ops){
			//Loop through the operators, if someone has the same ip, redeploy
			if(op.getOpContext().getOperatorStaticInformation().getMyNode().equals(n)){
				LOG.debug("-> Redeploy OP: {}", op.getOperatorId());
				bcu.sendObject(n, op);
			}
		}
		for(Connectable op: ops){
			//Loop through the operators, if someone has the same ip, reconfigure
			if(op.getOpContext().getOperatorStaticInformation().getMyNode().equals(n)){
				LOG.debug("-> Reconfigure OP: ", op.getOperatorId());
				bcu.sendObject(n, new Integer ((op).getOperatorId()));
			}
		}
	}
	
	public void failure(int opId){
		// create a controltuple with a streamstate, target opid
		ControlTuple streamState = new ControlTuple().makeStreamState(opId);
		// Get access to starTopology and send the controltuple to all of them
		for(Operator op : ops){
System.out.println("OP: "+op.getOperatorId());
			if(op.getOperatorId() != opId){
				if(!(op.getOpContext().isSink()) && !(op.getOpContext().isSource())){
					OperatorStaticInformation osi = op.getOpContext().getOperatorStaticInformation();
System.out.println("sending stream state to : "+op.getOperatorId());
					rct.sendControlMsgWithoutACK(osi, streamState, op.getOperatorId());
				}
			}
		}
	}
	
	public void sendCode(Node n, byte[] data){
		bcu.sendFile(n, data);
	}
	
	public void sendCode(Operator op, byte[] data){
		///\fixme{once there are more than one op per node this code will need to be fixed}
		Node node = op.getOpContext().getOperatorStaticInformation().getMyNode();
		LOG.debug("-> Sending CODE to Op: {} , Node: {}",op.getOperatorId(), node.toString());
		bcu.sendFile(node, data);
	}

	public void remoteOperatorInstantiation(Operator op) {
		Node node = op.getOpContext().getOperatorStaticInformation().getMyNode();
		LOG.debug("-> Remotely instantiating OP: ", op.getOperatorId());
		bcu.sendObject(node, op);
	}
	
	public void broadcastStarTopology(){
		for(Operator op : ops){
			if(!(op.getOpContext().isSink()) && !(op.getOpContext().isSource())){
				Node node = op.getOpContext().getOperatorStaticInformation().getMyNode();
				LOG.debug("-> Sending updated starTopology to OP: {}",op.getOperatorId());
				bcu.sendObject(node, starTopology);
			}
		}
	}

	public void init(Operator op) {
		Node node = op.getOpContext().getOperatorStaticInformation().getMyNode();
		LOG.debug("-> Initializing OP: {}", op.getOperatorId());
		bcu.sendObject(node, op.getOperatorId());
	}
	
//	public void _init(ArrayList<Operator> ops){
//		bcu.sendObjectNonBlocking(ops);
//	}
	
	public void initRuntime(Operator op){
		Node node = op.getOpContext().getOperatorStaticInformation().getMyNode();
		LOG.info("-> Starting RUNTIME of OP: {}", op.getOperatorId());
		bcu.sendObject(node, "SET-RUNTIME");
	}

	/// \test {some variables were bad, check if now is working}
	public void reMap(InetAddress oldIp, InetAddress newIp){
		OperatorContext opCtx = null;
		for(Connectable op: ops){
			opCtx = op.getOpContext();
			OperatorStaticInformation loc = opCtx.getOperatorStaticInformation();
			Node node = loc.getMyNode();
			if(node.getIp().equals(oldIp)){
				Node newNode = node.setIp(newIp);
				OperatorStaticInformation newLoc = loc.setNode(newNode);
				opCtx.setOperatorStaticInformation(newLoc);
			}
		}
	}

/// \todo{remove boolean paralell recovery}
/// parallel recovery was added to force the scale out of the failed operator before recovering it. it is necessary to change this and make it properly
	public void updateU_D(InetAddress oldIp, InetAddress newIp, boolean parallelRecovery){
		LOG.warn("-> Using sendControlMsg WITHOUT ACK");
		//Update operator information
		for(Connectable me : ops){
			//If there is an operator that was placed in the oldIP...
			if(me.getOpContext().getOperatorStaticInformation().getMyNode().getIp().equals(oldIp)){
				//We get its downstreams
				for(PlacedOperator downD : me.getOpContext().downstreams){
					//Now we change each downstream info (about me) and update its conn with me
					for(Connectable downstream: ops){
						if(downstream.getOperatorId() == downD.opID()){
							//To change info of this operator, locally first
							downstream.getOpContext().changeLocation(oldIp, newIp);
							
							ControlTuple ctb = new ControlTuple().makeReconfigure(me.getOperatorId(), "reconfigure_U", newIp.getHostAddress());
							
							LOG.debug("-> Updating Upstream OP: {}", downstream.getOperatorId());
							//bcu.sendControlMsg(downstream.getOpContext().getOperatorStaticInformation(), ctb.build(), downstream.getOperatorId());
							rct.sendControlMsgWithoutACK(downstream.getOpContext().getOperatorStaticInformation(), ctb, downstream.getOperatorId());
						}
					}
				}
				for(PlacedOperator upU: me.getOpContext().upstreams){
					for(Connectable upstream: ops){
						if(upstream.getOperatorId() == upU.opID()){
							//To change info of this operator, locally and remotely
							upstream.getOpContext().changeLocation(oldIp, newIp);
							ControlTuple ctb = null;
							//It needs to change its upstream conn
							if(!parallelRecovery){
								System.out.println("");
								ctb = new ControlTuple().makeReconfigure(me.getOperatorId(), "reconfigure_D", newIp.getHostAddress());
							}
							else{
								ctb = new ControlTuple().makeReconfigure(me.getOperatorId(), "just_reconfigure_D", newIp.getHostAddress());
							}
							LOG.debug("-> Updating Downstream OP: {}", upstream.getOperatorId());
							//bcu.sendControlMsg(upstream.getOpContext().getOperatorStaticInformation(), ctb.build(), upstream.getOperatorId());
							rct.sendControlMsgWithoutACK(upstream.getOpContext().getOperatorStaticInformation(), ctb, upstream.getOperatorId());
							//It needs to replay buffer
							String target = "";
							ControlTuple ctb2 = new ControlTuple().makeReconfigure(0, "replay", target);
						}	
					}
				}
			}
		}
	}	

	public void start() throws ESFTRuntimeException{
		//Send the messages to start the sources
		for(Operator source : src){
			String msg = "START "+source.getOperatorId();
			LOG.info("-> Starting source, msg = {}", msg);
			bcu.sendObject(source.getOpContext().getOperatorStaticInformation().getMyNode(), msg);
		}
		//Start clock in sink.
		bcu.sendObject(snk.getOpContext().getOperatorStaticInformation().getMyNode(), "CLOCK");
		LOG.info("SOURCES have been notified. System started.");
		systemIsRunning = true;
	}
    
    public void stop(int opId) {
        LOG.info("Stopping operator [{}]", opId);
        for(Operator op : ops) {
            if (op.getOperatorId() == opId) {
                String msg = "STOP " + op.getOperatorId();
                LOG.info("-> Stopping operator, msg = {}", msg);
                
                bcu.sendObject(op.getOpContext().getOperatorStaticInformation().getMyNode(), opId, msg);
                
                // Monitoring: scale-in feature
                
                // We first find the upstream operator for the chosen victim
                
                // TODO: support multiple upstreams. I only intend to support one 
                // upstream at the moment. Not difficult to support many.
                int size = op.getOpContext().getUpstreamSize();
                if (size != 1) {
                    LOG.warn("More than one upstream operator detected for [{}]", opId);
                }
               
                // Send SCALE_IN control tuple to the upstream of the victim
                PlacedOperator upstreamVictimOp = op.getOpContext().upstreams.iterator().next();
                if (upstreamVictimOp != null) {
                    LOG.info("-> Stopping upstream operator {}, msg = {}", 
                            upstreamVictimOp.opID(), ControlTupleType.SCALE_IN.toString());
                    
                    ControlTuple ct = new ControlTuple()
                            .makeScaleIn(upstreamVictimOp.opID(), opId, upstreamVictimOp.isStateful());
                    
                    rct.sendControlMsg(upstreamVictimOp.location(), ct, upstreamVictimOp.opID());
                }
            }
        }
    }
    
	public synchronized Node getNodeFromPool() throws NodePoolEmptyException{
		if(nodeStack.size() < Integer.parseInt(GLOBALS.valueFor("minimumNodesAvailable"))){
			//nLogger.info("Instantiating EC2 images");
			//new Thread(new EC2Worker(this)).start();
		}

		checkEmptyPool();
		numberRunningMachines++;		

		if (!havePopped)
		{
			havePopped = true;
			sortNodeStack();
			//Collections.sort(nodeStack);
		}
		return nodeStack.pop();
	}
	
	public synchronized Node getNodeFromPool(InetAddress nodeIp, int nodePort) throws NodePoolEmptyException
	{
		if (!havePopped)
		{
			havePopped = true;
			sortNodeStack();
		}

		Iterator<Node> iter = nodeStack.iterator();
		while (iter.hasNext())
		{
			Node nxt = iter.next();
			if (nxt.getIp().equals(nodeIp) && nxt.getPort() == nodePort)
			{
				iter.remove();
				numberRunningMachines++;
				return nxt;
			}
		}
		
		if ("true".equals(GLOBALS.valueFor("abortOnNodePoolEmpty")))
		{
			LOG.error("No node in pool matching "+nodeIp+":"+nodePort);
			System.exit(1);
			return null;
		}
		else
		{
			throw new NodePoolEmptyException("No node in pool matching "+nodeIp+":"+nodePort);
		}
	}
	
	//Make the assignment deterministic given the same mapping constraints.
	private void sortNodeStack()
	{
		List<Node> nodeList = new LinkedList<>(nodeStack);
		
		Collections.sort(nodeList, new Comparator<Node>()
		{
			public int compare( Node o1, Node o2 )
			{
				String id1 = o1.getIp() + ":" + o1.getPort();
				String id2 = o2.getIp() + ":" + o2.getPort();
				return id1.compareTo(id2);
			}
		});	

		nodeStack.clear();
		nodeStack.addAll(nodeList);
	}

	private void checkEmptyPool() throws NodePoolEmptyException
	{
		if(nodeStack.isEmpty()){
			if ("true".equals(GLOBALS.valueFor("abortOnNodePoolEmpty")))
			{
				LOG.error("Node pool is empty, impossible to get more nodes");
				System.exit(1);
			}
			else
			{
				throw new NodePoolEmptyException("Node pool is empty, impossible to get more nodes");
			}
		}		
	}
	
	public synchronized void incrementBaseId(){
		baseId++;
	}
	
	public void placeNew(Operator o, Node n) {
            
		int opId = o.getOperatorId();
                int originalOpId = o.getOriginalOpId();
                
		boolean isStatefull = (o.getOperatorCode() instanceof StatefulOperator) ? true : false;
		// Note that opId and originalOpId are the same value here, since placeNew places only original operators in the query
		
                OperatorStaticInformation l = new OperatorStaticInformation(opId, originalOpId, n, 
				Integer.parseInt(GLOBALS.valueFor("controlSocket")) + opId, 
				Integer.parseInt(GLOBALS.valueFor("dataSocket")) + opId, isStatefull);
		o.getOpContext().setOperatorStaticInformation(l);
		
		for (OperatorContext.PlacedOperator downDescr: o.getOpContext().downstreams) {
			int downID = downDescr.opID();
			Connectable downOp = elements.get(downID);
			downOp.getOpContext().setUpstreamOperatorStaticInformation(opId, l);
		}

		for (OperatorContext.PlacedOperator upDescr: o.getOpContext().upstreams) {
			int upID = upDescr.opID();
			Connectable upOp = elements.get(upID);
			upOp.getOpContext().setDownstreamOperatorStaticInformation(opId, l);
		}
	}
	
	public void placeNewParallelReplica(Operator originalOp, Operator o, Node n){
		int opId = o.getOperatorId();
		int originalOpId = originalOp.getOpContext().getOperatorStaticInformation().getOpId();
		boolean isStatefull = (o.getOperatorCode() instanceof StatefulOperator) ? true : false;
		
		OperatorStaticInformation l = new OperatorStaticInformation(opId, originalOpId, n, 
				Integer.parseInt(GLOBALS.valueFor("controlSocket")) + opId, 
				Integer.parseInt(GLOBALS.valueFor("dataSocket")) + opId, isStatefull);
		o.getOpContext().setOperatorStaticInformation(l);
		
		for (OperatorContext.PlacedOperator downDescr: o.getOpContext().downstreams) {
			int downID = downDescr.opID();
			Connectable downOp = elements.get(downID);
			downOp.getOpContext().setUpstreamOperatorStaticInformation(opId, l);
		}

		for (OperatorContext.PlacedOperator upDescr: o.getOpContext().upstreams) {
			int upID = upDescr.opID();
			Connectable upOp = elements.get(upID);
			upOp.getOpContext().setDownstreamOperatorStaticInformation(opId, l);
		}
	}

	public void deployConnection(String command, Connectable opToContact, Connectable opToAdd, String operatorType) {
		System.out.println("OPERATOR TYPE: "+operatorType);
		ControlTuple ct = null;
		String ip = null;
		//Some commands do not require opToAdd
		if(opToAdd != null){
			int opId = opToAdd.getOperatorId();
			ip = opToAdd.getOpContext().getOperatorStaticInformation().getMyNode().getIp().getHostAddress();
			int originalOpId = opToAdd.getOpContext().getOperatorStaticInformation().getOriginalOpId();
			int node_port = opToAdd.getOpContext().getOperatorStaticInformation().getMyNode().getPort();
			int in_c = opToAdd.getOpContext().getOperatorStaticInformation().getInC();
			int in_d = opToAdd.getOpContext().getOperatorStaticInformation().getInD();
			boolean operatorNature = opToAdd.getOpContext().getOperatorStaticInformation().isStatefull();
			ct = new ControlTuple().makeReconfigure(opId, originalOpId, command, ip, node_port, in_c, in_d, operatorNature, operatorType);
		}
		else{
			ct = new ControlTuple().makeReconfigure(0, command, ip);
		}
		rct.sendControlMsg(opToContact.getOpContext().getOperatorStaticInformation(), ct, opToContact.getOperatorId());
	}
	
	@Deprecated
	public void configureSourceRate(int numberEvents, int time){
		
		ControlTuple tuple = new ControlTuple().makeReconfigureSourceRate(numberEvents, "configureSourceRate", time);
		
//		Main.eventR = numberEvents;
//		Main.period = time;
		for(Operator source : src){
			rct.sendControlMsg(source.getOpContext().getOperatorStaticInformation(), tuple, source.getOperatorId());
		}
		rct.sendControlMsg(snk.getOpContext().getOperatorStaticInformation(), tuple, snk.getOperatorId());
	}
	
	public int getOpIdFromIp(InetAddress ip){
		int opId = -1;
		for(Operator op : ops){
			if(op.getOpContext().getOperatorStaticInformation().getMyNode().getIp().equals(ip)){
				opId = op.getOperatorId();
				return opId;
			}
		}
		return opId;
	}
	
	public int getNumDownstreams(int opId){
		for(Operator op : ops){
			if(op.getOperatorId() == opId){
				return op.getOpContext().downstreams.size();
			}
		}
		return -1;
	}
	
	public int getNumUpstreams(int opId){
		for(Operator op : ops){
			if(op.getOperatorId() == opId){
				return op.getOpContext().upstreams.size();
			}
		}
		return -1;
	}
	
	public void printCurrentInfrastructure(){
		System.out.println("##########################");
		System.out.println("INIT: printCurrentInfrastructure");
		System.out.println("Nodes registered in system:");
		System.out.println("  ");
		System.out.println();
		for(Node n : nodeStack){
			System.out.println(n);
		}
		System.out.println("  ");

		System.out.println("OPERATORS: ");
		for (Connectable op: ops) {
			System.out.println(op);
			System.out.println();
		}
		System.out.println("END: printCurrentInfrastructure");
		System.out.println("##########################");
	}

	public void saveResults() {
		ControlTuple tuple = new ControlTuple().makeReconfigureSingleCommand("saveResults");
		rct.sendControlMsg(snk.getOpContext().getOperatorStaticInformation(), tuple, snk.getOperatorId());
	}
	
	public void switchMechanisms(){
		ControlTuple tuple = new ControlTuple().makeReconfigureSingleCommand("deactivateMechanisms");
		for(Operator o : ops){
			rct.sendControlMsg(o.getOpContext().getOperatorStaticInformation(), tuple, o.getOperatorId());
		}
		//Send msg to src and snk
		for(Operator source : src){
			rct.sendControlMsg(source.getOpContext().getOperatorStaticInformation(), tuple, source.getOperatorId());
		}
		rct.sendControlMsg(snk.getOpContext().getOperatorStaticInformation(), tuple, snk.getOperatorId());
	}

	public String getOpType(int opId) {
		for(Operator op : ops){
			if(op.getOperatorId() == opId){
				return op.getClass().getName(); 
			}
		}
		return null;
	}
	
	public void parallelRecovery(String oldIp_txt) throws UnknownHostException{
		try {
			eiu.executeParallelRecovery(oldIp_txt);
		} 
		catch (NodePoolEmptyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (ParallelRecoveryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void saveResultsSWC() {
		ControlTuple tuple = new ControlTuple().makeReconfigureSingleCommand("saveResults");
		Operator aux = null;
		for(Operator op : ops){
			if(op.getClass().getName().equals("seep.operator.collection.SmartWordCounter")){
				aux = op;
			}
		}
		rct.sendControlMsg(aux.getOpContext().getOperatorStaticInformation(), tuple, aux.getOperatorId());
	}

	public Operator getOperatorById(int opIdToParallelize) {
		for(Operator op : ops){
			if(op.getOperatorId() == opIdToParallelize){
				return op;
			}
		}
		return null;
	}
	
	public void parseFileForNetflix() {
		System.out.println("Parse file for Netflix...");
		File f = new File("data.txt");
		File o = new File("data.bin");
		
		Kryo k = new Kryo();
		k.register(ArrayList.class, new ArrayListSerializer());
		k.register(Payload.class);
		k.register(TuplePayload.class);
		k.register(BatchTuplePayload.class);
		try {
			//OUT
			FileOutputStream fos = new FileOutputStream(o);
			Output output = new Output(fos);
			
			//IN
			FileReader fr = new FileReader(f);
			BufferedReader br = new BufferedReader(fr);
			String currentLine = null;
			
			//PARSE
			Map<String, Integer> mapper = new HashMap<String, Integer>();
			ArrayList<String> artList = new ArrayList<String>();
			artList.add("userId");
			artList.add("itemId");
			artList.add("rating");
			for(int i = 0; i<artList.size(); i++){
				System.out.println("MAP: "+artList.get(i));
				mapper.put(artList.get(i), i);
			}
			
			DataTuple dts = new DataTuple(mapper, new TuplePayload());
			int counter = 0;
			int total = 0;
			while((currentLine = br.readLine()) != null){
				
				counter++;
				if(counter == 10000){
					total += 10000;
					System.out.println("total: "+total);
					counter = 0;
				}
				
				String[] tokens = currentLine.split(",");
//				dt.setUserId(Integer.parseInt(tokens[1]));
//				dt.setItemId(Integer.parseInt(tokens[0]));
//				dt.setRating(Integer.parseInt(tokens[2]));
				TuplePayload tp = new TuplePayload();
//				tp.attrValues = new Payload(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[0]), Integer.parseInt(tokens[2]));
				DataTuple dt = dts.newTuple(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[0]), Integer.parseInt(tokens[2]));
//				dt.setValues(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[0]), Integer.parseInt(tokens[2]));
				
				k.writeObject(output, dt);
				//Flush the buffer to the stream
				output.flush();
			}
			fos.close();
			br.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	public void addOperator(Operator o) {
		ops.add(o);
		elements.put(o.getOperatorId(), o);
		LOG.debug("Added new Operator to Infrastructure: {}", o.toString());
	}
}
