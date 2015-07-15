/*******************************************************************************
 * Copyright (c) 2013 Imperial College London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial design and implementation
 *     Martin Rouaux - Changes to support scale-in of operators
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.elastic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.api.NodeAlreadyInUseException;
import uk.ac.imperial.lsds.seep.api.QueryPlan;
import uk.ac.imperial.lsds.seep.api.ScaleOutIntentBean;
import uk.ac.imperial.lsds.seep.comm.RuntimeCommunicationTools;
import uk.ac.imperial.lsds.seep.comm.routing.Router;
import uk.ac.imperial.lsds.seep.comm.serialization.ControlTuple;
import uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure;
import uk.ac.imperial.lsds.seep.infrastructure.master.Node;
import uk.ac.imperial.lsds.seep.operator.Connectable;
import uk.ac.imperial.lsds.seep.operator.Operator;
import uk.ac.imperial.lsds.seep.operator.OperatorCode;
import uk.ac.imperial.lsds.seep.operator.StatefulOperator;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;
import uk.ac.imperial.lsds.seep.operator.OperatorContext.PlacedOperator;
import uk.ac.imperial.lsds.seep.state.StateWrapper;


public class ElasticInfrastructureUtils {

	final private Logger LOG = LoggerFactory.getLogger(ElasticInfrastructureUtils.class);
	
	private Infrastructure inf = null;
	private RuntimeCommunicationTools rct = null;
	private URLClassLoader ucl = null;
    
    // Data structure that returns the list of identifiers for scaled out physical 
    // instances of a logical query identifier. So, if operator 1 is scaled out 
    // twice and instances 50 and 51 are started, the map contains: <1, [50, 51]>
    private Map<Integer, List<Integer>> scalingMap;
	
	public ElasticInfrastructureUtils(Infrastructure inf){
		this.inf = inf;
		this.rct = inf.getRCT();
		inf.getBCU();
        
        scalingMap = new HashMap<Integer, List<Integer>>();
        
               
        // Timer to periodically log the physical query plan (i.e.: how many
        // replicas of a given instance type running at any time).
        Timer reportTimer = new Timer();
            reportTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    StringBuilder sb = new StringBuilder();
                    
                    for(Integer logicalOperatorId : scalingMap.keySet()) {
                        List<Integer> physicalOpIds = scalingMap.get(logicalOperatorId);
                        
                        sb.append("{logicalOpId=");
                        sb.append(logicalOperatorId);
                        sb.append(" physicalOpIds=");
                        sb.append(Arrays.deepToString(physicalOpIds.toArray()));
                        sb.append(" totalSize=");
                        sb.append(physicalOpIds.size() + 1);
                        sb.append("} ");
                        
                        LOG.debug("Physical query plan: {}", sb.toString());
                    }
                }
            }, 5000, 5000);
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
    
    public synchronized void unalert(int opIdToUnparallelize) {
        unalertCPU(opIdToUnparallelize);
    }
    
	/// \todo {this method should not be in infrastructure}
	public synchronized void alertCPU(int opIdToParallelize){
		System.out.println("#########################################################");
		System.out.println("INF: MONITOR reports system alert SCALE OUT");
		System.out.println("#########################################################");
		
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
            
            // Add the new identifier to the scaling map
            if (!scalingMap.containsKey(opIdToParallelize)) {
                scalingMap.put(opIdToParallelize, new ArrayList<Integer>());
            }
            
            scalingMap.get(opIdToParallelize).add(newId);
		}
		else{
			System.out.println("NO NODES AVAILABLE. IMPOSSIBLE TO PARALLELIZE");
		}
	
        System.out.println("#########################################################");
		System.out.println("INF: MASTER FINISHED SCALE OUT");
        System.out.println("#########################################################");
    }
	
    public synchronized void unalertCPU(int opIdToUnparallelize) {
		System.out.println("#########################################################");
		System.out.println("INF: MONITOR reports system alert SCALE IN");
		System.out.println("#########################################################");

        List<Integer> stoppableIds = scalingMap.get(opIdToUnparallelize);
        if (!stoppableIds.isEmpty()) {
            // We always pick the first identifier to stop first
            inf.stop(stoppableIds.get(0));
            
            // Remove stopped operator's identifier from scaling map
            scalingMap.get(opIdToUnparallelize).remove(0);
        }
        
		System.out.println("#########################################################");
		System.out.println("INF: MASTER FINISHED SCALE IN");
        System.out.println("#########################################################");
    }
    
	public synchronized void scaleOutOperator(int opIdToParallelize, int newOpId, Node newNode){
		try {
			if(GLOBALS.valueFor("checkpointMode").equals("light-state")){
				lightScaleOutOperator(opIdToParallelize, newOpId, newNode);
			}
			else if(GLOBALS.valueFor("checkpointMode").equals("large-state")){
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
			
			// We have to scale out the number of partitions minus one -> [partitions.get(op)-1]
			for(int i = 0; i<(partitions.get(op)-1); i++){
                            
				Node newNode = new Node(nodeId);
                                int oldOpId = op.getOperatorId();
				Operator newReplica = staticScaleOut(oldOpId, newOpId, newNode, qp);
                                
				if(op.getOpContext().isSource()){ // probably not the best place to do this
					LOG.debug("-> Statically scaling out SOURCE operator");
					inf.addSource(newReplica);
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
                        newOp.setOriginalOpId(oldOpId);
		} 
		catch (OperatorNotRegisteredException e) {
			
			e.printStackTrace();
		}
		// Register new op associated to new node in the infrastructure
		try {
			//qp.place(newOp, newNode);
			qp.place(newOp);
		} 
		catch (NodeAlreadyInUseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// conf operator context
		configureStaticReplicaContext(oldOpId, newOp);
		// router for the new op
		Router copyOfRouter = opToScaleOut.getRouter();
                
                newOp.setRouter(copyOfRouter);
                
		// inf place new and update context
		System.out.println("checking new node: "+newNode.toString());
		inf.placeNew(newOp, newNode);
		inf.updateContextLocations(newOp);
                
                //SANITY CHECK
                ArrayList<Operator> allOps = inf.getOps();
                for(Operator eachOp : allOps){
                    HashMap<Integer, ArrayList<Integer>> routInfoMap = eachOp.getOpContext().getRouteInfo();
                    
                        LOG.debug("-------- CHECK ROUTING FOR opID *{}*---------- ",eachOp.getOperatorId()) ;
                        
                        for(Entry<Integer, ArrayList<Integer>> entry : routInfoMap.entrySet() ){
                            LOG.debug("ROUTING: key stream id {}, value downs op id {}", entry.getKey(), entry.getValue());
                        }
                    
                }
                
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
				if(o.getOperatorCode() instanceof StatefulOperator){
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
		Connectable opToAdd = newOp;
		Connectable opToContact = null;
		for(PlacedOperator op : newOp.getOpContext().upstreams){
			//deploy new connection with all of them?
			opToContact = inf.getElements().get(op.opID());
			LOG.debug("COMMAND: add_downstream to: "+op.opID());
			inf.deployConnection("add_downstream", opToContact, opToAdd, newOp.getClass().getName());
		}
	}

	public void addUpstreamConnections(Operator newOp){
		Connectable opToAdd = newOp;
		Connectable opToContact = null;
		for(PlacedOperator op : newOp.getOpContext().downstreams){
			opToContact = inf.getElements().get(op.opID());
			//the operator that must change, the id of the new replica, the type of operator splitting
			LOG.debug("COMMAND: add_upstream to: "+op.opID());
			inf.deployConnection("add_upstream", opToContact, opToAdd, newOp.getClass().getName());
		}
	}

	public Operator addOperator(int opId, int newOpId) throws OperatorNotRegisteredException{
		OperatorCode opCode = getOperatorCode(opId);
		if(opCode == null){
			throw new OperatorNotRegisteredException("opId does not match any registered operator");
		}
		Operator op = null;
		List<String> attributes = inf.getOperatorById(opId).getOpContext().getDeclaredWorkingAttributes();
		boolean isSink = inf.getOperatorById(opId).getOpContext().isSink();
		if(opCode instanceof StatefulOperator){
			StateWrapper copyOfState = (StateWrapper) inf.getOperatorById(opId).getStateWrapper().clone();
			copyOfState.setOwnerId(newOpId);
			op = Operator.getStatefulOperator(newOpId, opCode, copyOfState, attributes);
			op.getOpContext().setIsSink(isSink);
		}
		else if(opCode instanceof StatelessOperator){
			op = Operator.getStatelessOperator(newOpId, opCode, attributes);
			op.getOpContext().setIsSink(isSink);
		}
		inf.addOperator(op);
		return op;
	}
	
	private OperatorCode getOperatorCode(int opId){
		ArrayList<Operator> ops = inf.getOps();
		for(Operator op : ops){
			if(op.getOperatorId() == opId){
				return op.getOperatorCode();
			}
		}
		return null;
	}
        
   private void configureStreamIdFromUpstreamOps(Operator op, Operator newOp, int opId) {
       
        HashMap<Integer, Integer> op_streamId_map = new HashMap<>();
        
        for (PlacedOperator up : op.getOpContext().upstreams) {
            Operator upOp = inf.getOperatorById(up.opID());
            HashMap<Integer, ArrayList<Integer>> routInfoMap = upOp.getOpContext().getRouteInfo();

            for (Entry<Integer, ArrayList<Integer>> entry : routInfoMap.entrySet()) {
                for (int o : entry.getValue()) {
                    op_streamId_map.put(o, entry.getKey()); //downstreamOp (KEY) - streamID (VAL)
                }
            }

            (inf.getElements().get(up.opID())).connectTo(inf.getElements().get(newOp.getOperatorId()), true, op_streamId_map.get(opId));
            //(inf.getElements().get(up.opID())).connectTo(inf.getElements().get(newOp.getOperatorId()),false);
        }
    }

    public void configureStaticReplicaContext(int opId, Operator newOp) {
        
        ArrayList<Operator> ops = inf.getOps();
               
		for(Operator op : ops){
			if(opId == op.getOperatorId()){
				//op.getOpContext().copyContext(newOp);
                            
                //configure streamIds that upstreams connect to the newOp
                configureStreamIdFromUpstreamOps(op, newOp, opId);

				for(PlacedOperator down : op.getOpContext().downstreams){
					inf.getElements().get(newOp.getOperatorId()).connectTo(inf.getElements().get(down.opID()), false);
				}
				//Copy the original operators to the new operatorContext
				newOp.setOriginalDownstream(op.getOpContext().getOriginalDownstream());
				//Copy the tuple declaration fields
				newOp._declareWorkingAttributes(op.getOpContext().getDeclaredWorkingAttributes());
				//Copy inputDataIngestionMode information
				newOp.initializeInputDataIngestionModePerUpstream(op.getOpContext().getInputDataIngestionModePerUpstream());
                                
                HashMap<Integer, ArrayList<Integer>> originalRouteMap = inf.getOperatorById(opId).getOpContext().getRouteInfo();
                newOp.getOpContext().setRouteInfo(originalRouteMap);
				
				if(op.getOpContext().isSource()){
					newOp.getOpContext().setIsSource(true);
				}
			}
		}
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
    
    /**
     * @return Returns a list of identifiers for the nodes that are executing a 
     * given operator at the time of invocation.
     */
    public List<Integer> getNodeIdsForOperatorId(int operatorId) {
        List<Integer> nodeIds = new ArrayList<Integer>();
        
        nodeIds.add(operatorId);
        if (scalingMap.containsKey(operatorId)) {
            nodeIds.addAll(scalingMap.get(operatorId));
        }
                
        return nodeIds;
    }
}
