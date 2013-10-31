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
package uk.ac.imperial.lsds.seep.elastic;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.P;
import uk.ac.imperial.lsds.seep.api.NodeAlreadyInUseException;
import uk.ac.imperial.lsds.seep.api.QueryPlan;
import uk.ac.imperial.lsds.seep.api.ScaleOutIntentBean;
import uk.ac.imperial.lsds.seep.comm.RuntimeCommunicationTools;
import uk.ac.imperial.lsds.seep.comm.routing.Router;
import uk.ac.imperial.lsds.seep.comm.serialization.ControlTuple;
import uk.ac.imperial.lsds.seep.infrastructure.NodeManager;
import uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure;
import uk.ac.imperial.lsds.seep.infrastructure.master.Node;
import uk.ac.imperial.lsds.seep.operator.Operator;
import uk.ac.imperial.lsds.seep.operator.QuerySpecificationI;
import uk.ac.imperial.lsds.seep.operator.StatefulOperator;
import uk.ac.imperial.lsds.seep.operator.OperatorContext.PlacedOperator;
import uk.ac.imperial.lsds.seep.state.StateWrapper;


public class ElasticInfrastructureUtils {

	final private Logger LOG = LoggerFactory.getLogger(ElasticInfrastructureUtils.class);
	
	private Infrastructure inf = null;
	private RuntimeCommunicationTools rct = null;
	private URLClassLoader ucl = null;
	
	public ElasticInfrastructureUtils(Infrastructure inf){
		this.inf = inf;
		this.rct = inf.getRCT();
		inf.getBCU();
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
		Node newNode = null;
		try {
			newNode = inf.getNodeFromPool();
		} 
		catch (NodePoolEmptyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		try {
			if(P.valueFor("checkpointMode").equals("light-state")){
				lightScaleOutOperator(opIdToParallelize, newOpId, newNode);
			}
			else if(P.valueFor("checkpointMode").equals("large-state")){
				largeScaleOutOperator(opIdToParallelize, newOpId, newNode);
			}
		} 
		catch (ScaleOutException e) {
			LOG.error("While scaling out light op: {}", e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void lightScaleOutOperator(int opIdToParallelize, int newOpId, Node newNode) throws ScaleOutException{
		//get number of upstreams to indicate msh how many messages to wait -> measurement purposes
		int numUpstreamsOpId = inf.getNumUpstreams(opIdToParallelize);
		//set initial time and number of downstreams
		Infrastructure.msh.setParallelizationStartTime(System.currentTimeMillis(), numUpstreamsOpId);
		Operator newOp = null;
		try {
			newOp = addOperator(opIdToParallelize, newOpId);
		} 
		catch (OperatorNotRegisteredException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(newOp == null){
			throw new ScaleOutException("Impossible to scale out, operator instantiation failed");
		}
		//connect new operator to downstreams and upstreams
		configureOperatorContext(opIdToParallelize, newOp);
		//Get operator to parallelize
		
		Operator opToParallelize = inf.getOperatorById(opIdToParallelize);
		//Get pre-configured router and assign to new operator
		Router copyOfRouter = opToParallelize.getRouter();
		newOp.setRouter(copyOfRouter);
//		inf.placeNew(newOp, newNode);
		inf.placeNewParallelReplica(opToParallelize, newOp, newNode);
		inf.updateContextLocations(newOp);
		//NodeManager.nLogger.info("Created new Op: "+newOp.toString());
		// Send query to the new node
		inf.deployCodeToOperator(newOp);
		//deploy new Operator
		//conn to new node
		inf.remoteOperatorInstantiation(newOp);
		//ConfigureCommunications
		//conn to new node
		inf.init(newOp);
		// Make the new operator aware of the states in the system
		inf.broadcastState(newOp);
		// and also aware of the payloads
		// Send the SET-RUNTIME to the new op
		inf.initRuntime(newOp);
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
	}
	
	public void largeScaleOutOperator(int opIdToParallelize, int newOpId, Node newNode) throws ScaleOutException{
		//get number of upstreams to indicate msh how many messages to wait -> measurement purposes
		int numUpstreamsOpId = inf.getNumUpstreams(opIdToParallelize);
		//set initial time and number of downstreams
		Infrastructure.msh.setParallelizationStartTime(System.currentTimeMillis(), numUpstreamsOpId);
		Operator newOp = null;
		try {
			newOp = addOperator(opIdToParallelize, newOpId);
		} 
		catch (OperatorNotRegisteredException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(newOp == null){
			throw new ScaleOutException("Impossible to scale out, operator instantiation failed");
		}
		//connect new operator to downstreams and upstreams
		configureOperatorContext(opIdToParallelize, newOp);
		//Get operator to parallelize
		
		Operator opToParallelize = inf.getOperatorById(opIdToParallelize);
		//Get pre-configured router and assign to new operator
		Router copyOfRouter = opToParallelize.getRouter();
		newOp.setRouter(copyOfRouter);
//		inf.placeNew(newOp, newNode);
		inf.placeNewParallelReplica(opToParallelize, newOp, newNode);
		inf.updateContextLocations(newOp);
		// Update starTopology
		inf.addNodeToStarTopology(newOp.getOperatorId(), newOp.getOpContext().getOperatorStaticInformation().getMyNode().getIp());
		//Send updated star topology to nodes in the system
		inf.broadcastStarTopology();
		// Send query to the new node
		inf.deployCodeToOperator(newOp);
		//deploy new Operator
		//conn to new node
		inf.remoteOperatorInstantiation(newOp);
		//ConfigureCommunications
		//conn to new node
		inf.init(newOp);
		// Make the new operator aware of the states in the system
		inf.broadcastState(newOp);
		// and also aware of the payloads
		// Send the SET-RUNTIME to the new op
		inf.initRuntime(newOp);
		//add upstream conn
		//conn to down nodes
/**WAIT FOR ANSWER**/
		addUpstreamConnections(newOp);
		//conn to previous nodes
		addDownstreamConnections(newOp);
/**UNTIL HERE**/
		//conn to previous node
//		sendScaleOutMessageToUpstreams(opIdToParallelize, newOpId);
		sendDistributedScaleOutMessageToStarTopology(opIdToParallelize, newOpId);
		//conn to previous node
/**HERE AGAIN WAIT FOR ANSWER**/
		//sendResumeMessageToUpstreams(opIdToParallelize, newOpId);
/**FINALIZE SCALE OUT PROTOCOL**/
		//once the system is ready, send the command ready to new replica to enable it to initialize the necessary steps
		sendSystemConfiguredToReplica(newOp);
	}
	
	public void sendDistributedScaleOutMessageToStarTopology(int opIdToParallelize, int newOpId){
		ArrayList<Operator> ops = inf.getOps();
		for (Operator o: ops) {
			if(o.getOperatorId() != opIdToParallelize && o.getOperatorId() != newOpId){ // Do not send to involved ops
//				if(!(o.getOpContext().isSink())){
				if(!(o.getOpContext().isSink()) && !(o.getOpContext().isSource())){
					LOG.debug("COMMAND: distributed_scale_out to: {}", o.getOperatorId());
					ControlTuple ct = new ControlTuple().makeDistributedScaleOut(opIdToParallelize, newOpId);
					rct.sendControlMsgWithoutACK(o.getOpContext().getOperatorStaticInformation(), ct, o.getOperatorId());
				}
			}
		}
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
				if(op.getOpContext().isSource()){ // probably not the best place to do this
					LOG.debug("-> Statically scaling out SOURCE operator");
					inf.addSource(newReplica);
				}
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
			
			Operator newOp = staticScaleOut(oldOpId, newOpId, newNode, qp);
			///\todo{ probably not the best place to do this}
			if(inf.getOperatorById(oldOpId).getOpContext().isSource()){
				LOG.debug("-> Statically scaling out SOURCE operator");
				inf.addSource(newOp);
			}
			// upstream and downstream conns are statically established at this point
			// scale out to upstream. this is just about configuring the router properly, anything else, no message interchange (obviously)
			so.setNewReplicaInstantiation(newOp);
		}
		return soib;
	}
	
	private Operator staticScaleOut(int oldOpId, int newOpId, Node newNode, QueryPlan qp){
		Operator opToScaleOut = inf.getOperatorById(oldOpId);
		Operator newOp = null;
		try {
			newOp = addOperator(oldOpId, newOpId);
		} 
		catch (OperatorNotRegisteredException e) {
			
			e.printStackTrace();
		}
		// Register new op associated to new node in the infrastructure
		try {
			qp.place(newOp, newNode);
		} 
		catch (NodeAlreadyInUseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// conf operator context
		configureOperatorContext(oldOpId, newOp);
		// router for the new op
		Router copyOfRouter = opToScaleOut.getRouter();
		newOp.setRouter(copyOfRouter);
		// inf place new and update context
		System.out.println("checking new node: "+newNode.toString());
		inf.placeNew(newOp, newNode);
		inf.updateContextLocations(newOp);
		LOG.debug("STATIC Created new Op: {}", newOp.toString());
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
				r.newStaticOperatorPartition(oldOpId, newOpId, oldOpIndex, newOpIndex);
			}
		}
	}
	
	private void sendSystemConfiguredToReplica(Operator replica){
		LOG.debug("COMMAND: system_ready to: {}", replica.getOperatorId());
		inf.deployConnection("system_ready", replica, null, "");
	}
	
///// \test{when is this method used?}
//	private void sendSendInitToMinFailedNodeUpstream(int failedNode) {
//		ArrayList<Operator> ops = inf.getOps();
//		for (Operator o: ops) {
//			if (o.getOperatorId() == failedNode) {
//				PlacedOperator minUpstream = o.getOpContext().minimumUpstream();
//				ArrayList<Integer> opIds = new ArrayList<Integer>();
//				opIds.add(failedNode);
//				ControlTuple ct = new ControlTuple().makeResume(opIds);
//				rct.sendControlMsg(minUpstream.location(), ct, minUpstream.opID());
//			}
//		}
//	}
	
	private void sendResumeMessageToUpstreams(int opIdToParallelize, int newOpId) {
		ArrayList<Operator> ops = inf.getOps();
		for (Operator o: ops) {
			if (o.getOperatorId() == opIdToParallelize) {
				for (PlacedOperator upstream: o.getOpContext().upstreams) {
					
					ArrayList<Integer> opIds = new ArrayList<Integer>();
					opIds.add(opIdToParallelize);
					opIds.add(newOpId);
					ControlTuple ct = new ControlTuple().makeResume(opIds);
					LOG.debug("COMMAND: resume to: {}", upstream.opID());
					rct.sendControlMsg(upstream.location(), ct, upstream.opID());
				}
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
					LOG.debug("COMMAND: scale_out to: {}", upstream.opID());
					
					ControlTuple ct = new ControlTuple().makeScaleOut(opIdToParallelize, newOpId, isStateful);
					
					rct.sendControlMsg(upstream.location(), ct, upstream.opID());
				}
			}
		}
	}
	
	public void executeParallelRecovery(String oldIp_txt) throws UnknownHostException, NodePoolEmptyException, ParallelRecoveryException{
		//First we remap the old failed one
		//get opId from ip
		InetAddress oldIp = InetAddress.getByName(oldIp_txt);
		int opId = inf.getOpIdFromIp(oldIp);
		if(opId == -1){
			throw new ParallelRecoveryException("IP not bounded to an operator: "+oldIp_txt);
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
			throw new NodePoolEmptyException("No Nodes available, impossible to retrieve a new node");
		}
		int newReplicaId = inf.getBaseId();
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
			LOG.debug("COMMAND: add_downstream to: "+op.opID());
			inf.deployConnection("add_downstream", opToContact, opToAdd, newOp.getClass().getName());
		}
	}

	public void addUpstreamConnections(Operator newOp){
		QuerySpecificationI opToAdd = newOp;
		QuerySpecificationI opToContact = null;
		for(PlacedOperator op : newOp.getOpContext().downstreams){
			opToContact = inf.getElements().get(op.opID());
			//the operator that must change, the id of the new replica, the type of operator splitting
			LOG.debug("COMMAND: add_upstream to: "+op.opID());
			inf.deployConnection("add_upstream", opToContact, opToAdd, newOp.getClass().getName());
		}
	}

	public Operator addOperator(int opId, int newOpId) throws OperatorNotRegisteredException{
		Operator op = null;
		String className = getOperatorClassName(opId);
		if(className == null){
			throw new OperatorNotRegisteredException("Operator class name not found");
		}
		try{
			LOG.debug("-> Registering new OP: "+newOpId+" as OPType: "+className);
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
				StateWrapper copyOfState = (StateWrapper) inf.getOperatorById(opId).getStateWrapper().clone();
				copyOfState.setOwnerId(newOpId);
				op.setStateWrapper(copyOfState);
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
				//Copy inputDataIngestionMode information
				newOp.initializeInputDataIngestionModePerUpstream(op.getOpContext().getInputDataIngestionModePerUpstream());
				
				if(op.getOpContext().isSource()){
					newOp.getOpContext().setIsSource(true);
				}
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
