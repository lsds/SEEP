package seep.elastic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import seep.comm.BasicCommunicationUtils;
import seep.comm.routing.Router;
import seep.comm.tuples.Seep;
import seep.comm.tuples.Seep.ControlTuple;
import seep.infrastructure.Infrastructure;
import seep.infrastructure.Node;
import seep.infrastructure.NodeManager;
import seep.operator.Operator;
import seep.operator.QuerySpecificationI;
import seep.operator.OperatorContext.PlacedOperator;

public class ElasticInfrastructureUtils {

	private Infrastructure inf = null;
	private BasicCommunicationUtils bcu = null;
	
	public ElasticInfrastructureUtils(Infrastructure inf){
		this.inf = inf;
		this.bcu = inf.getBcu();
	}
	
	public boolean promptForUserValidation(int opIdToParallelize){
		boolean valid = false;
		System.out.println("###################");
		System.out.println("###################");
		System.out.println("OP: "+opIdToParallelize+ "GRANT SCALE-OUT? YES/NO");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String option = null;
		try{
			option = br.readLine();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		if(option.equals("y")) valid = true;
		return valid;
	}
	
	public void alert(int opIdToParallelize){
//		boolean valid = promptForUserValidation(opIdToParallelize);
//		if (valid)	alertCPU(opIdToParallelize);
		alertCPU(opIdToParallelize);
//		else System.out.println("IGNORED");
	}
	
	/// \todo {this method should not be in infrastructure}
	public synchronized void alertCPU(int opIdToParallelize){
		System.out.println("INF: MONITOR reports system alert");
		System.out.println("###################");
		Node newNode = inf.getNodeFromPool();
		if(newNode != null){
			int newId = inf.getBaseId();
			System.out.println("INF: Parallelization PARAMS: OpToParal "+opIdToParallelize+" newOp: "+newId+" newNode: "+newNode.toString());
			inf.getEiu().scaleOutOperator(opIdToParallelize, newId, newNode);
			inf.incrementBaseId();
		}
		else{
			System.out.println("NO NODES AVAILABLE. IMPOSSIBLE TO PARALLELIZE");
		}
		System.out.println("###################");
		System.out.println("INF: MASTER FINISHED SCALE OUT");
	}
	
	public synchronized void scaleOutOperator(int opIdToParallelize, int newOpId, Node newNode){
		//get number of upstreams to indicate msh how many messages to wait -> measurement purposes
		int numUpstreamsOpId = inf.getNumUpstreams(opIdToParallelize);
		//set initial time and number of downstreams
		Infrastructure.msh.setParallelizationStartTime(System.currentTimeMillis(), numUpstreamsOpId);
		Operator newOp = addOperator(opIdToParallelize, newOpId);
		if(newOp == null){
			NodeManager.nLogger.severe("-> Impossible to scale out, operator instantiation failed");
			return;
		}
		//connect new operator to downstreams and upstreams
		configureOperatorContext(opIdToParallelize, newOp);
		//Get operator to parallelize
		Operator opToParallelize = inf.getOperatorById(opIdToParallelize);
		Router copyOfRouter = opToParallelize.getRouter();
		newOp.setRouter(copyOfRouter);
		//Get router and assign to new operator
		inf.placeNew(newOp, newNode);
		inf.updateContextLocations(newOp);
		NodeManager.nLogger.info("Created new Op: "+newOp.toString());
		//deploy new Operator
		//conn to new node
		inf.deploy(newOp);
		//ConfigureCommunications
		//conn to new node
		inf.init(newOp);
		//add upstream conn
		//conn to down nodes
/**WAIT FOR ANSWER**/
		addUpstreamConnections(newOp);
		//conn to previous nodes
		addDownstreamConnections(newOp);
/**UNTIL HERE**/
		//conn to previous node
		sendScaleOutMessageToUpstreams(opIdToParallelize, newOpId);
		//conn to previous node
/**HERE AGAIN WAIT FOR ANSWER**/
		sendResumeMessageToUpstreams(opIdToParallelize, newOpId);
/**FINALIZE SCALE OUT PROTOCOL**/
		//once the system is ready, send the command ready to new replica to enable it to initialize the necessary steps
		sendSystemConfiguredToReplica(newOp);
		//sendSendInitToMinUpstream(opIdToParallelize,newOpId);
	}
	
	private void sendSystemConfiguredToReplica(Operator replica){
		inf.deployConnection("system_ready", replica, null, "");
	}
	
/// \test{when is this method used?}
	private void sendSendInitToMinFailedNodeUpstream(int failedNode) {
		ArrayList<Operator> ops = inf.getOps();
		for (Operator o: ops) {
			if (o.getOperatorId() == failedNode) {
				PlacedOperator minUpstream = o.getOpContext().minimumUpstream();
				bcu.sendControlMsg(minUpstream.location(), ControlTuple.newBuilder()
						.setType(ControlTuple.Type.RESUME)
						.setResume(Seep.Resume.newBuilder()
								.setOpId(0, failedNode)
								.build())
								.build()
				, minUpstream.opID());
			}
		}
	}
	
	private void sendResumeMessageToUpstreams(int opIdToParallelize, int newOpId) {
		ArrayList<Operator> ops = inf.getOps();
		for (Operator o: ops) {
			if (o.getOperatorId() == opIdToParallelize) {
				for (PlacedOperator upstream: o.getOpContext().upstreams) {
					Seep.ControlTuple.Builder ct = Seep.ControlTuple.newBuilder();
					ct.setType(ControlTuple.Type.RESUME);
					Seep.Resume.Builder si = Seep.Resume.newBuilder();
					si.addOpId(opIdToParallelize);
					si.addOpId(newOpId);
					ct.setResume(si.build());
					
					bcu.sendControlMsg(upstream.location(), ct.build(), upstream.opID());
				}
			}
		}
	}
	
	@Deprecated
	private void sendSendInitToMinUpstream(int opIdToParallelize, int newOpId) {
		ArrayList<Operator> ops = inf.getOps();
		for (Operator o: ops) {
			if (o.getOperatorId() == opIdToParallelize) {
				PlacedOperator minUpstream = o.getOpContext().minimumUpstream();
				Seep.ControlTuple.Builder ct = Seep.ControlTuple.newBuilder();
				ct.setType(ControlTuple.Type.RESUME);
				Seep.Resume.Builder si = Seep.Resume.newBuilder();
				//si.setOpId(1, opIdToParallelize);
				si.addOpId(opIdToParallelize);
				//si.setOpId(2, newOpId);
				si.addOpId(newOpId);
				ct.setResume(si.build());
				bcu.sendControlMsg(minUpstream.location(), ct.build(), minUpstream.opID());
			}
		}
	}

	private void sendScaleOutMessageToUpstreams(int opIdToParallelize, int newOpID) {
		ArrayList<Operator> ops = inf.getOps();
		for (Operator o: ops) {
			if (o.getOperatorId() == opIdToParallelize) {
				for (PlacedOperator upstream: o.getOpContext().upstreams) {
					NodeManager.nLogger.info("-> scale_out to: "+upstream.opID());
					Seep.ControlTuple.Builder ct = Seep.ControlTuple.newBuilder();
					ct.setType(ControlTuple.Type.SCALE_OUT);
					Seep.ScaleOutInfo.Builder scaleOutInfo = Seep.ScaleOutInfo.newBuilder();
					scaleOutInfo.setOldOpID(opIdToParallelize);
					scaleOutInfo.setNewOpID(newOpID);
					ct.setScaleOutInfo(scaleOutInfo.build());
					
					bcu.sendControlMsg(upstream.location(), ct.build(), upstream.opID());
				}
			}
		}
	}
	
	public void executeParallelRecovery(String oldIp_txt) throws UnknownHostException{
		//First we remap the old failed one
		//get opId from ip
		InetAddress oldIp = InetAddress.getByName(oldIp_txt);
		int opId = inf.getOpIdFromIp(oldIp);
		if(opId == -1){
			NodeManager.nLogger.severe("IP not bounded to an operator: "+oldIp_txt);
			return;
		}
		//get numDownstreams from opId
		int numOfUpstreams = inf.getNumUpstreams(opId);
		//set initial time of crash and number of downstreams
		Infrastructure.msh.setCrashInitialTime(System.currentTimeMillis(), numOfUpstreams);
		//Pick new node
		Node newNode = inf.getNodeFromPool();
		inf.reDeploy(newNode);
		//updateU_D could get nodes instead of IPs to build correct nodes, but
		//it also work just with IPs
		inf.updateU_D(oldIp, newNode.getIp(), true);
		
		//Then we scale out that operator with the new replica as well
		Node newReplica = inf.getNodeFromPool();
		if(newReplica == null){
			NodeManager.nLogger.severe("-> No Nodes available, impossible to retrieve a new node");
			return;
		}
		int newReplicaId = inf.getBaseId();
System.out.println("SCALING OUT WITH, opId: "+opId+" newReplicaId: "+newReplicaId);
		scaleOutOperator(opId, newReplicaId, newReplica);
		/// \todo{Embed this function in another one to avoid errors}
		inf.incrementBaseId();
	}

	public void addDownstreamConnections(Operator newOp){
		//Search for all upstream ids
		QuerySpecificationI opToAdd = newOp;
		QuerySpecificationI opToContact = null;
		for(PlacedOperator op : newOp.getOpContext().upstreams){
			//deploy new connection with all of them?
			opToContact = inf.getElements().get(op.opID());
			inf.deployConnection("add_downstream", opToContact, opToAdd, newOp.getClass().getName());
		}
	}

	public void addUpstreamConnections(Operator newOp){
		QuerySpecificationI opToAdd = newOp;
		QuerySpecificationI opToContact = null;
		for(PlacedOperator op : newOp.getOpContext().downstreams){
			opToContact = inf.getElements().get(op.opID());
			//the operator that must change, the id of the new replica, the type of operator splitting
			inf.deployConnection("add_upstream", opToContact, opToAdd, newOp.getClass().getName());
		}
	}

	public Operator addOperator(int opId, int newOpId){
		Operator op = null;
		String className = getOperatorClassName(opId);
		if(className == null){
			NodeManager.nLogger.severe("-> Not found className for opId: "+opId);
			return null;
		}
		try{
			NodeManager.nLogger.info("-> Registering new OP: "+newOpId+" as OPType: "+className);
			op = (Operator) Class.forName(className).getConstructor(int.class).newInstance(newOpId);
		}
		catch(ClassNotFoundException cnfe){
			System.out.println("While looking for className: "+cnfe.getMessage());
		}
		catch(NoSuchMethodException nsme){
			System.out.println("While invoking constructor: "+nsme.getMessage());
		}
		catch(InstantiationException ie){
			System.out.println("While instantiating operator: "+ie.getMessage());
		}
		catch(IllegalAccessException iae){
			System.out.println("While instantiating...: "+iae.getMessage());
		}
		catch(InvocationTargetException ite){
			System.out.println("While instantiating....: "+ite.getMessage());
		}
		inf.addOperator(op);
		return op;
	}

	public void configureOperatorContext(int opId, Operator newOp){
		ArrayList<Operator> ops = inf.getOps();
		for(Operator op : ops){
			if(opId == op.getOperatorId()){
				//op.getOpContext().copyContext(newOp);
				for(PlacedOperator up : op.getOpContext().upstreams){
					
					(inf.getElements().get(up.opID())).connectTo(inf.getElements().get(newOp.getOperatorId()));
				}
				for(PlacedOperator down : op.getOpContext().downstreams){
					inf.getElements().get(newOp.getOperatorId()).connectTo(inf.getElements().get(down.opID()));
				}
			}
		}
	}

	public String getOperatorClassName(int opId){
		ArrayList<Operator> ops = inf.getOps();
		String className = null;
		for(Operator op : ops){
			if(op.getOperatorId() == opId){
				className = op.getClass().getName();
			}
		}
		return className;
	}
}
