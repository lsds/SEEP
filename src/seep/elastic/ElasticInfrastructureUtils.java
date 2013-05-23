package seep.elastic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;

import seep.comm.NodeManagerCommunication;
import seep.comm.RuntimeCommunicationTools;
import seep.comm.routing.Router;
import seep.comm.serialization.ControlTuple;
import seep.infrastructure.NodeManager;
import seep.infrastructure.api.QueryPlan;
import seep.infrastructure.api.ScaleOutIntentBean;
import seep.infrastructure.master.Infrastructure;
import seep.infrastructure.master.Node;
import seep.operator.Operator;
import seep.operator.QuerySpecificationI;
import seep.operator.State;
import seep.operator.StatefulOperator;
import seep.operator.StatelessOperator;
import seep.operator.OperatorContext.PlacedOperator;


public class ElasticInfrastructureUtils {

	private Infrastructure inf = null;
	private RuntimeCommunicationTools rct = null;
	private NodeManagerCommunication bcu = null;
	
	private URLClassLoader ucl = null;
	
	public ElasticInfrastructureUtils(Infrastructure inf){
		this.inf = inf;
		this.rct = inf.getRCT();
		this.bcu = inf.getBCU();
	}
	
	public void setClassLoader(URLClassLoader ucl){
		this.ucl = ucl;
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
		//Get pre-configured router and assign to new operator
		Router copyOfRouter = opToParallelize.getRouter();
		newOp.setRouter(copyOfRouter);
		inf.placeNew(newOp, newNode);
		inf.updateContextLocations(newOp);
		System.out.println("7.1");
		//NodeManager.nLogger.info("Created new Op: "+newOp.toString());
		System.out.println("7.5");
		// Send query to the new node
		inf.setUp(newOp);
		System.out.println("8");
		//deploy new Operator
		//conn to new node
		inf.deploy(newOp);
		System.out.println("9");
		//ConfigureCommunications
		//conn to new node
		inf.init(newOp);
		System.out.println("10");
		// Make the new operator aware of the states in the system
		inf.broadcastState(newOp);
		System.out.println("11");
		// and also aware of the payloads
//		inf.broadcastObject(newOp);
		// Send the SET-RUNTIME to the new op
		inf.initRuntime(newOp);
		System.out.println("12");
		//add upstream conn
		//conn to down nodes
/**WAIT FOR ANSWER**/
		addUpstreamConnections(newOp);
		System.out.println("13");
		//conn to previous nodes
		addDownstreamConnections(newOp);
		System.out.println("14");
/**UNTIL HERE**/
		//conn to previous node
		sendScaleOutMessageToUpstreams(opIdToParallelize, newOpId);
		System.out.println("15");
		//conn to previous node
/**HERE AGAIN WAIT FOR ANSWER**/
		sendResumeMessageToUpstreams(opIdToParallelize, newOpId);
		System.out.println("16");
/**FINALIZE SCALE OUT PROTOCOL**/
		//once the system is ready, send the command ready to new replica to enable it to initialize the necessary steps
		sendSystemConfiguredToReplica(newOp);
		System.out.println("17");
		//sendSendInitToMinUpstream(opIdToParallelize,newOpId);
	}
	
	// One option to scale out automatically operators, statically
	public ArrayList<ScaleOutIntentBean> staticInstantiationNewReplicaOperators(QueryPlan qp){
		// We have to return an arraylist of scaleoutintent that gives the operators to scale out ordered.
		ArrayList<ScaleOutIntentBean> soib = new ArrayList<ScaleOutIntentBean>();
		// Get the operators to scale out
		Map<Operator, Integer> partitions = qp.getPartitionRequirements();
		// We perform the balancing per operator (in order, which is enforced by the map implementation used)
		for(Operator op : partitions.keySet()){
			// Per number of partitions
			
			/// \fixme{ automatic assignation of id lets 90 ops per partition, and make sure nodeId is different}
			int newOpId = 10 + (op.getOperatorId()*100);
			int nodeId = -1 * newOpId;
			// the list where replicas will be saved
			ArrayList<Operator> listOfReplicas = new ArrayList<Operator>();
			listOfReplicas.add(op);
			int accessIdx = 0;
			// We have to scale out the number of partitions minus one -> [partitions.get(op)-1]
			for(int i = 0; i<(partitions.get(op)-1); i++){
				Node newNode = new Node(nodeId);
				//oldOpId, newOpId, newNode, qp
				int oldOpId = listOfReplicas.get(accessIdx).getOperatorId();
				Operator newReplica = staticScaleOut(oldOpId, newOpId, newNode, qp);
				// First modify accessIdx for next iteration
				if(listOfReplicas.size()-1 == accessIdx){
					// So it will be reset
					accessIdx = 0;
					// Add to the end
					listOfReplicas.add(newReplica);
				}
				else{
					// Add right next to the just scaled out op
					listOfReplicas.add((accessIdx+1), newReplica);
					// We jump the new addition and go to the next op
					accessIdx += 2;
				}
				// Then add the new replica to the array
				//listOfReplicas.add((accessIdx+1), newReplica);
				// Add scaleout intent to the list (ordered)
				ScaleOutIntentBean so = new ScaleOutIntentBean(inf.getOperatorById(oldOpId), newOpId, newNode);
				so.setNewReplicaInstantiation(newReplica);
				soib.add(so);
				nodeId--;
				newOpId++;
			}
		}
		return soib;
	} 
	
	// One option to scale out manually operators, statically.
	public ArrayList<ScaleOutIntentBean> staticInstantiateNewReplicaOperator(ArrayList<ScaleOutIntentBean> soib, QueryPlan qp){
		// Go through intents and execute all of them, or prompt back an ERROR if not possible
		for(ScaleOutIntentBean so : soib){
			//addOperator
			int oldOpId = so.getOpToScaleOut().getOperatorId();
			int newOpId = so.getNewOpId();
			//Operator opToScaleOut = so.getOpToScaleOut();
			Node newNode = so.getNewProvisionedNode();
			
			
//			Operator newOp = addOperator(oldOpId, newOpId);
//			// Register new op associated to new node in the infrastructure
//			qp.place(newOp, newNode);
//			// conf operator context
//			configureOperatorContext(oldOpId, newOp);
//			// router for the new op
//			Router copyOfRouter = opToScaleOut.getRouter();
//			newOp.setRouter(copyOfRouter);
//			// inf place new and update context
//			System.out.println("checking new node: "+newNode.toString());
//			inf.placeNew(newOp, newNode);
//			inf.updateContextLocations(newOp);
//			NodeManager.nLogger.info("STATIC Created new Op: "+newOp.toString());
			
			Operator newOp = staticScaleOut(oldOpId, newOpId, newNode, qp);
			
			// upstream and downstream conns are statically established at this point
			// scale out to upstream. this is just about configuring the router properly, anything else, no message interchange (obviously)
			so.setNewReplicaInstantiation(newOp);
		}
		return soib;
	}
	
	private Operator staticScaleOut(int oldOpId, int newOpId, Node newNode, QueryPlan qp){
		Operator opToScaleOut = inf.getOperatorById(oldOpId);
		Operator newOp = addOperator(oldOpId, newOpId);
		// Register new op associated to new node in the infrastructure
		qp.place(newOp, newNode);
		// conf operator context
		configureOperatorContext(oldOpId, newOp);
		// router for the new op
		Router copyOfRouter = opToScaleOut.getRouter();
		newOp.setRouter(copyOfRouter);
		// inf place new and update context
		System.out.println("checking new node: "+newNode.toString());
		inf.placeNew(newOp, newNode);
		inf.updateContextLocations(newOp);
		NodeManager.nLogger.info("STATIC Created new Op: "+newOp.toString());
		return newOp;
	}
	
	public void executeStaticScaleOutFromIntent(ArrayList<ScaleOutIntentBean> soib){
		// Go through intents and execute all of them, or prompt back an ERROR if not possible
		for(ScaleOutIntentBean so : soib){
			int oldOpId = so.getOpToScaleOut().getOperatorId();
			int newOpId = so.getNewOpId();
			Operator newOp = so.getNewOperatorInstantiation();
			// upstream and downstream conns are statically established at this point
			// scale out to upstream. this is just about configuring the router properly, anything else, no message interchange (obviously)
			for(PlacedOperator op : newOp.getOpContext().upstreams){
				Operator toConf = inf.getOperatorById(op.opID());
				Router r = toConf.getRouter();
				int oldOpIndex = toConf.getOpContext().findDownstream(oldOpId).index();
				int newOpIndex = -1;
				for(PlacedOperator op_aux: toConf.getOpContext().downstreams) {
					if (op_aux.opID() == newOpId){
						newOpIndex = op_aux.index();
					}
				}
				//r.newOperatorPartition(oldOpId, newOpId, oldOpIndex, newOpIndex);
				r.newStaticOperatorPartition(oldOpId, newOpId, oldOpIndex, newOpIndex);
			}
		}
	}
	
	private void sendSystemConfiguredToReplica(Operator replica){
		NodeManager.nLogger.info("COMMAND: system_ready to: "+replica.getOperatorId());
		inf.deployConnection("system_ready", replica, null, "");
	}
	
/// \test{when is this method used?}
	private void sendSendInitToMinFailedNodeUpstream(int failedNode) {
		ArrayList<Operator> ops = inf.getOps();
		for (Operator o: ops) {
			if (o.getOperatorId() == failedNode) {
				PlacedOperator minUpstream = o.getOpContext().minimumUpstream();
				ArrayList<Integer> opIds = new ArrayList<Integer>();
				opIds.add(failedNode);
				ControlTuple ct = new ControlTuple().makeResume(opIds);
				rct.sendControlMsg(minUpstream.location(), ct, minUpstream.opID());
			}
		}
	}
	
	private void sendResumeMessageToUpstreams(int opIdToParallelize, int newOpId) {
		ArrayList<Operator> ops = inf.getOps();
		for (Operator o: ops) {
			if (o.getOperatorId() == opIdToParallelize) {
				for (PlacedOperator upstream: o.getOpContext().upstreams) {
					
					ArrayList<Integer> opIds = new ArrayList<Integer>();
					opIds.add(opIdToParallelize);
					opIds.add(newOpId);
					ControlTuple ct = new ControlTuple().makeResume(opIds);
					NodeManager.nLogger.info("COMMAND: resume to: "+upstream.opID());
					rct.sendControlMsg(upstream.location(), ct, upstream.opID());
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
				
				ArrayList<Integer> opIds = new ArrayList<Integer>();
				opIds.add(opIdToParallelize);
				opIds.add(newOpId);
				ControlTuple ct = new ControlTuple().makeResume(opIds);
				
				rct.sendControlMsg(minUpstream.location(), ct, minUpstream.opID());
			}
		}
	}

	private void sendScaleOutMessageToUpstreams(int opIdToParallelize, int newOpId) {
		ArrayList<Operator> ops = inf.getOps();
		boolean isStateful = false;
		for (Operator o: ops) {
			if (o.getOperatorId() == opIdToParallelize) {
				if(o instanceof StatefulOperator){
					isStateful = true;
				}
				for (PlacedOperator upstream: o.getOpContext().upstreams) {
					NodeManager.nLogger.info("COMMAND: scale_out to: "+upstream.opID());
					
					ControlTuple ct = new ControlTuple().makeScaleOut(opIdToParallelize, newOpId, isStateful);
					
					rct.sendControlMsg(upstream.location(), ct, upstream.opID());
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
			NodeManager.nLogger.info("COMMAND: add_downstream to: "+op.opID());
			inf.deployConnection("add_downstream", opToContact, opToAdd, newOp.getClass().getName());
		}
	}

	public void addUpstreamConnections(Operator newOp){
		QuerySpecificationI opToAdd = newOp;
		QuerySpecificationI opToContact = null;
		for(PlacedOperator op : newOp.getOpContext().downstreams){
			opToContact = inf.getElements().get(op.opID());
			//the operator that must change, the id of the new replica, the type of operator splitting
			NodeManager.nLogger.info("COMMAND: add_upstream to: "+op.opID());
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
			// I use the custom class loader to load the operator, since its code coming from the user side
			Object instance = null;
			Constructor<?> constructor = null;
			// We load the class
			Class<?> operatorClass = ucl.loadClass(className);
			// By reflection we extract the constructor for this class
			Class<?> parameterTypes[] = {};
			constructor = operatorClass.getConstructor(parameterTypes);
			
			instance = constructor.newInstance();
			// Cast instance to operator
			op = (Operator)instance;
			
			Operator toScaleOut = inf.getOperatorById(opId);
			if(toScaleOut instanceof StatefulOperator){
				// State injection. Pick the already existing operator, getState, clone it and then change the operatorId
				State copyOfState = (State) inf.getOperatorById(opId).getState().clone();
				copyOfState.setOwnerId(newOpId);
				op.setState(copyOfState);
			}
			op.setOperatorId(newOpId);
			op.setSubclassOperator();
			//op = (Operator) Class.forName(className).getConstructor(int.class).newInstance(newOpId);
		}
		catch(ClassNotFoundException cnfe){
			System.out.println("While looking for className: "+cnfe.getMessage());
			cnfe.printStackTrace();
		}
		catch(InstantiationException ie){
			System.out.println("While instantiating operator: "+ie.getMessage());
			ie.printStackTrace();
		}
		catch(IllegalAccessException iae){
			System.out.println("While instantiating...: "+iae.getMessage());
			iae.printStackTrace();
		} 
		catch (SecurityException se) {
			System.out.println("While instantiating...: "+se.getMessage());
			se.printStackTrace();
		} 
		catch (NoSuchMethodException e) {
			System.out.println("While instantiating...: "+e.getMessage());
			e.printStackTrace();
		} 
		catch (IllegalArgumentException e) {
			System.out.println("While instantiating...: "+e.getMessage());
			e.printStackTrace();
		} 
		catch (InvocationTargetException e) {
			System.out.println("While instantiating...: "+e.getMessage());
			e.printStackTrace();
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
					(inf.getElements().get(up.opID())).connectTo(inf.getElements().get(newOp.getOperatorId()), false);
				}
				for(PlacedOperator down : op.getOpContext().downstreams){
					inf.getElements().get(newOp.getOperatorId()).connectTo(inf.getElements().get(down.opID()), false);
				}
				//Copy the original operators to the new operatorContext
				newOp.setOriginalDownstream(op.getOpContext().getOriginalDownstream());
				//Copy the tuple declaration fields
				newOp._declareWorkingAttributes(op.getOpContext().getDeclaredWorkingAttributes());
			}
		}
	}

	public String getOperatorClassName(int opId){
		ArrayList<Operator> ops = inf.getOps();
		String className = null;
		for(Operator op : ops){
			if(op.getOperatorId() == opId){
				className = op.getClass().getCanonicalName();
			}
		}
		return className;
	}
}
