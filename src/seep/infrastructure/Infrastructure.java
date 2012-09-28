package seep.infrastructure;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import seep.Main;
import seep.comm.BasicCommunicationUtils;
import seep.comm.serialization.ControlTuple;
import seep.elastic.ElasticInfrastructureUtils;
import seep.infrastructure.monitor.MonitorManager;
import seep.operator.Operator;
import seep.operator.OperatorContext;
import seep.operator.QuerySpecificationI;
import seep.operator.StatefullOperator;
import seep.operator.OperatorContext.PlacedOperator;

/**
* Infrastructure. This class is in charge of dealing with nodes, deployment and profiling of the system.
*/


public class Infrastructure {

	public static Logger nLogger = Logger.getLogger("seep");
	
	int value = Integer.parseInt(Main.valueFor("maxLatencyAllowed"));
	static final int CONTROL_SOCKET = Integer.parseInt(Main.valueFor("controlSocket"));
	static final int DATA_SOCKET = Integer.parseInt(Main.valueFor("dataSocket"));
	
	static public MasterStatisticsHandler msh = new MasterStatisticsHandler();
	
	private int baseId = Integer.parseInt(Main.valueFor("baseId"));

	private Deque<Node> nodeStack = new ArrayDeque<Node>();
	private int numberRunningMachines = 0;

	private ArrayList<Operator> ops = new ArrayList<Operator>();
	public Map<Integer,QuerySpecificationI> elements = new HashMap<Integer, QuerySpecificationI>();
	private Operator src;
	private Operator snk;
	
	private BasicCommunicationUtils bcu = new BasicCommunicationUtils();

	private ElasticInfrastructureUtils eiu = new ElasticInfrastructureUtils(this);

	private ManagerWorker manager = null;
	private MonitorManager monitorManager = null;
	private int port;
	
	public Infrastructure(int listeningPort) {
		this.port = listeningPort;
	}

	public MonitorManager getMonitorManager(){
		return monitorManager;
	}
	
	public ArrayList<Operator> getOps() {
		return ops;
	}
	
	public Map<Integer, QuerySpecificationI> getElements() {
		return elements;
	}
	
	public int getNodePoolSize(){
		return nodeStack.size();
	}

	public int getNumberRunningMachines(){
		return numberRunningMachines;
	}
	
	public BasicCommunicationUtils getBcu() {
		return bcu;
	}
	
	public ElasticInfrastructureUtils getEiu() {
		return eiu;
	}
	
	public synchronized int getBaseId() {
		return baseId;
	}
	
	//This method is still valid to define which is the first operator in the query
	public void setSource(Operator src) {
		NodeManager.nLogger.info("Configured SOURCE as Operator: "+src.toString());
		this.src = src;
	}

	public void setSink(Operator snk){
		NodeManager.nLogger.info("Configured SINK as Operator: "+snk.toString());
		this.snk = snk;
	}
	
	public void addNode(Node n) {
		nodeStack.push(n);
		Infrastructure.nLogger.info("-> Infrastructure. New Node: "+n);
		Infrastructure.nLogger.info("-> Infrastructure. Num nodes: "+getNodePoolSize());
	}
	
	public void addOperator(Operator o) {
		ops.add(o);
		elements.put(o.getOperatorId(), o);
		NodeManager.nLogger.info("Added new Operator to Infrastructure: "+o.toString());
	}
	
	public void placeNew(Operator o, Node n) {
		int opID = o.getOperatorId();
		boolean isStatefull = (o instanceof StatefullOperator) ? true : false;
		OperatorStaticInformation l = new OperatorStaticInformation(n, CONTROL_SOCKET + opID, DATA_SOCKET + opID, isStatefull);
		o.getOpContext().setOperatorStaticInformation(l);
		
		for (OperatorContext.PlacedOperator downDescr: o.getOpContext().downstreams) {
			int downID = downDescr.opID();
			QuerySpecificationI downOp = elements.get(downID);
			downOp.getOpContext().setUpstreamOperatorStaticInformation(opID, l);
		}

		for (OperatorContext.PlacedOperator upDescr: o.getOpContext().upstreams) {
			int upID = upDescr.opID();
			QuerySpecificationI upOp = elements.get(upID);
			upOp.getOpContext().setDownstreamOperatorStaticInformation(opID, l);
		}
	}
	
	public void updateContextLocations(Operator o) {
		for (QuerySpecificationI op: elements.values()) {
			if (op!=o){
				setDownstreamLocationFromPotentialDownstream(o, op);
				setUpstreamLocationFromPotentialUpstream(o, op);
			}
		}
	}

	private void setDownstreamLocationFromPotentialDownstream(QuerySpecificationI target, QuerySpecificationI downstream) {
		for (PlacedOperator op: downstream.getOpContext().upstreams) {
			if (op.opID() == target.getOperatorId()) {
				target.getOpContext().setDownstreamOperatorStaticInformation(downstream.getOperatorId(), downstream.getOpContext().getOperatorStaticInformation());
			}
		}
	}
	
	private void setUpstreamLocationFromPotentialUpstream(QuerySpecificationI target, QuerySpecificationI upstream) {
		for (PlacedOperator op: upstream.getOpContext().downstreams) {
			if (op.opID() == target.getOperatorId()) {
				target.getOpContext().setUpstreamOperatorStaticInformation(upstream.getOperatorId(), upstream.getOpContext().getOperatorStaticInformation());
			}
		}
	}
	
	/// \todo {Any thread that it is started should be stopped someway}
	public void startInfrastructure(){
		Infrastructure.nLogger.info("-> Infrastructure. ManagerWorker running");
		manager = new ManagerWorker(this, port);
		Thread centralManagerT = new Thread(manager);
		centralManagerT.start();

		Infrastructure.nLogger.info("-> Infrastructure. MonitorManager running");
		monitorManager = new MonitorManager(this);
		Thread monitorManagerT = new Thread(monitorManager);
		monitorManagerT.start();
	}

	public void stopWorkers(){
		//stop monitor manager.. 
		monitorManager.stopMManager(true);
	}
	
	public void deploy() throws DeploymentException {

  		//Deploy operators
		for(Operator op: ops){
	     	//Establish the connection with the specified address
			deploy(op);
		}

		//Configure operators communications
		for(Operator op : ops){
			//Establish the connection with the specified address
			Infrastructure.nLogger.info("-> Infrastructure. Configuring OP-"+op.getOperatorId());
			init(op);
		}
	}

	public void reDeploy(Node n){

		System.out.println("REDEPLOY-operators with ip: "+n.toString());

		//Redeploy operators
		for(QuerySpecificationI op: ops){
			//Loop through the operators, if someone has the same ip, redeploy
			if(op.getOpContext().getOperatorStaticInformation().getMyNode().equals(n)){
				Infrastructure.nLogger.info("-> Infrastructure. Redeploy OP-"+op.getOperatorId());
				bcu.sendObject(n, op);
			}
		}
		for(QuerySpecificationI op: ops){
			//Loop through the operators, if someone has the same ip, reconfigure
			if(op.getOpContext().getOperatorStaticInformation().getMyNode().equals(n)){
				Infrastructure.nLogger.info("-> Infrastructure. reconfigure OP-"+op.getOperatorId());
				bcu.sendObject(n, new Integer ((op).getOperatorId()));
			}
		}
	}

	public void deploy(Operator op) {
		Node node = op.getOpContext().getOperatorStaticInformation().getMyNode();
		Infrastructure.nLogger.info("-> Infrastructure. Deploying OP-"+op.getOperatorId());
		bcu.sendObject(node, op);
	}

	public void init(Operator op) {
		Node node = op.getOpContext().getOperatorStaticInformation().getMyNode();
		Infrastructure.nLogger.info("-> Infrastructure. Initializing OP-"+op.getOperatorId());
		bcu.sendObject(node, op.getOperatorId());
	}

	/// \test {some variables were bad, check if now is working}
	public void reMap(InetAddress oldIp, InetAddress newIp){
		OperatorContext opCtx = null;
		for(QuerySpecificationI op: ops){
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
		NodeManager.nLogger.warning("-> using sendControlMsg WITHOUT ACK");
		//Update operator information
		for(QuerySpecificationI me : ops){
			//If there is an operator that was placed in the oldIP...
			if(me.getOpContext().getOperatorStaticInformation().getMyNode().getIp().equals(oldIp)){
				//We get its downstreams
				for(PlacedOperator downD : me.getOpContext().downstreams){
					//Now we change each downstream info (about me) and update its conn with me
					for(QuerySpecificationI downstream: ops){
						if(downstream.getOperatorId() == downD.opID()){
							//To change info of this operator, locally first
							downstream.getOpContext().changeLocation(oldIp, newIp);
							
							ControlTuple ctb = new ControlTuple().makeReconfigure(me.getOperatorId(), "reconfigure_U", newIp.getHostAddress());
							
							Infrastructure.nLogger.info("-> Infrastructure. updating Upstream OP-"+downstream.getOperatorId());
							//bcu.sendControlMsg(downstream.getOpContext().getOperatorStaticInformation(), ctb.build(), downstream.getOperatorId());
							bcu.sendControlMsgWithoutACK(downstream.getOpContext().getOperatorStaticInformation(), ctb, downstream.getOperatorId());
						}
					}
				}
				for(PlacedOperator upU: me.getOpContext().upstreams){
					for(QuerySpecificationI upstream: ops){
						if(upstream.getOperatorId() == upU.opID()){
							//To change info of this operator, locally and remotely
							upstream.getOpContext().changeLocation(oldIp, newIp);
							ControlTuple ctb = null;
							//It needs to change its upstream conn
							if(!parallelRecovery){
								ctb = new ControlTuple().makeReconfigure(me.getOperatorId(), "reconfigure_D", newIp.getHostAddress());
							}
							else{
								ctb = new ControlTuple().makeReconfigure(me.getOperatorId(), "just_reconfigure_D", newIp.getHostAddress());
							}
							Infrastructure.nLogger.info("-> Infrastructure. updating Downstream OP-"+upstream.getOperatorId());
							//bcu.sendControlMsg(upstream.getOpContext().getOperatorStaticInformation(), ctb.build(), upstream.getOperatorId());
							bcu.sendControlMsgWithoutACK(upstream.getOpContext().getOperatorStaticInformation(), ctb, upstream.getOperatorId());
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
		//Send the message to start the source
		String msg = "START "+src.getOperatorId();
		System.out.println("STARTING SOURCE, sending-> "+msg);
		Infrastructure.nLogger.info("-> Infrastructure. Starting system");
		bcu.sendObject(src.getOpContext().getOperatorStaticInformation().getMyNode(), msg);
		//Start clock in sink.
		bcu.sendObject(snk.getOpContext().getOperatorStaticInformation().getMyNode(), "CLOCK");
	}

	public synchronized Node getNodeFromPool(){
		if(nodeStack.size() < Integer.parseInt(Main.valueFor("minimumNodesAvailable"))){
			//nLogger.info("Instantiating EC2 images");
			//new Thread(new EC2Worker(this)).start();
		}
		numberRunningMachines++;
		if(nodeStack.isEmpty()){
			NodeManager.nLogger.warning("-> Node Pool empty, Impossible to scale-out");
			return null;
		}
		return nodeStack.pop();
	}
	
	public synchronized void incrementBaseId(){
		baseId++;
	}

	public void deployConnection(String command, QuerySpecificationI opToContact, QuerySpecificationI opToAdd, String operatorType) {
		
		ControlTuple ct = null;
		String ip = null;
		//Some commands do not require opToAdd
		if(opToAdd != null){
			int opId = opToAdd.getOperatorId();
			ip = opToAdd.getOpContext().getOperatorStaticInformation().getMyNode().getIp().getHostAddress();
			int node_port = opToAdd.getOpContext().getOperatorStaticInformation().getMyNode().getPort();
			int in_c = opToAdd.getOpContext().getOperatorStaticInformation().getInC();
			int in_d = opToAdd.getOpContext().getOperatorStaticInformation().getInD();
			boolean operatorNature = opToAdd.getOpContext().getOperatorStaticInformation().isStatefull();
			ct = new ControlTuple().makeReconfigure(opId, command, ip, node_port, in_c, in_d, operatorNature);
		}
		else{
			ct = new ControlTuple().makeReconfigure(0, command, ip);
		}
		bcu.sendControlMsg(opToContact.getOpContext().getOperatorStaticInformation(), ct, opToContact.getOperatorId());
	}
	
	public void configureSourceRate(int numberEvents, int time){
		
		ControlTuple tuple = new ControlTuple().makeReconfigureSourceRate(numberEvents, "configureSourceRate", time);
		
		Main.eventR = numberEvents;
		Main.period = time;
		bcu.sendControlMsg(src.getOpContext().getOperatorStaticInformation(), tuple, src.getOperatorId());
		bcu.sendControlMsg(snk.getOpContext().getOperatorStaticInformation(), tuple, snk.getOperatorId());
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
		for (QuerySpecificationI op: ops) {
			System.out.println(op);
			System.out.println();
		}
		System.out.println("END: printCurrentInfrastructure");
		System.out.println("##########################");
	}

	public void saveResults() {
		ControlTuple tuple = new ControlTuple().makeReconfigureSingleCommand("saveResults");
		bcu.sendControlMsg(snk.getOpContext().getOperatorStaticInformation(), tuple, snk.getOperatorId());
	}
	
	public void switchMechanisms(){
		ControlTuple tuple = new ControlTuple().makeReconfigureSingleCommand("deactivateMechanisms");
		for(Operator o : ops){
			bcu.sendControlMsg(o.getOpContext().getOperatorStaticInformation(), tuple, o.getOperatorId());
		}
		//Send msg to src and snk
		bcu.sendControlMsg(src.getOpContext().getOperatorStaticInformation(), tuple, src.getOperatorId());
		bcu.sendControlMsg(snk.getOpContext().getOperatorStaticInformation(), tuple, snk.getOperatorId());
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
		eiu.executeParallelRecovery(oldIp_txt);
	}

	public void saveResultsSWC() {
		ControlTuple tuple = new ControlTuple().makeReconfigureSingleCommand("saveResults");
		Operator aux = null;
		for(Operator op : ops){
			if(op.getClass().getName().equals("seep.operator.collection.SmartWordCounter")){
				aux = op;
			}
		}
		bcu.sendControlMsg(aux.getOpContext().getOperatorStaticInformation(), tuple, aux.getOperatorId());
	}

	public Operator getOperatorById(int opIdToParallelize) {
		for(Operator op : ops){
			if(op.getOperatorId() == opIdToParallelize){
				return op;
			}
		}
		return null;
	}
}
