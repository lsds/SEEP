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
package uk.ac.imperial.lsds.seep.infrastructure.master;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
import uk.ac.imperial.lsds.seep.infrastructure.monitor.comm.serialization.MetricsTuple;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.master.MonitorMasterListener;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.PolicyRules;

/**
* Infrastructure. This class is in charge of dealing with nodes, deployment and profiling of the system.
*/


public class Infrastructure {

	final private Logger LOG = LoggerFactory.getLogger(Infrastructure.class);
	
	int value = Integer.parseInt(GLOBALS.valueFor("maxLatencyAllowed"));
	static public MasterStatisticsHandler msh = new MasterStatisticsHandler();
	
	private int baseId = Integer.parseInt(GLOBALS.valueFor("baseId"));
	
	private Deque<Node> nodeStack = new ArrayDeque<Node>();
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
		//	Finally get the mapping for this query and assign real nodes
		for(Entry<Integer, Operator> e : queryToNodesMapping.entrySet()){
			Node a = null;
			try {
				a = getNodeFromPool();
			} 
			catch (NodePoolEmptyException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			LOG.debug("-> Mapping OP: {} to Node: {}", e.getValue().getOperatorId(), a);
			placeNew(e.getValue(), a);
		}
		LOG.debug("-> All operators have been mapped");
		for(Operator o : queryToNodesMapping.values()){
			LOG.debug("OP: {}, CONF: {}", o.getOperatorId(), o);
		}
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

	public synchronized Node getNodeFromPool() throws NodePoolEmptyException{
		if(nodeStack.size() < Integer.parseInt(GLOBALS.valueFor("minimumNodesAvailable"))){
			//nLogger.info("Instantiating EC2 images");
			//new Thread(new EC2Worker(this)).start();
		}
		numberRunningMachines++;
		if(nodeStack.isEmpty()){
			throw new NodePoolEmptyException("Node pool is empty, impossible to get more nodes");
		}
		
		return nodeStack.pop();
	}
	
	public synchronized void incrementBaseId(){
		baseId++;
	}
	
	public void placeNew(Operator o, Node n) {
		int opId = o.getOperatorId();
		boolean isStatefull = (o.getOperatorCode() instanceof StatefulOperator) ? true : false;
		// Note that opId and originalOpId are the same value here, since placeNew places only original operators in the query
		OperatorStaticInformation l = new OperatorStaticInformation(opId, opId, n, 
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
    
    /**
     * @return Returns a list of identifiers for the nodes that are executing a 
     * given operator at the time of invocation.
     */
    public List<Integer> getNodeIdsForOperatorId(int operatorId) {
        List<Integer> nodeIds = new ArrayList<Integer>();
        
        for(Integer nodeId : queryToNodesMapping.keySet()) {
            if(queryToNodesMapping.get(nodeId).getOperatorId() == operatorId) {
                nodeIds.add(nodeId);
            }
        }
                
        return nodeIds;
    }
}
