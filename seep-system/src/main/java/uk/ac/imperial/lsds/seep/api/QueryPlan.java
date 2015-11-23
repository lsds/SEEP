/*******************************************************************************
 * Copyright (c) 2013 Imperial College London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial design and implementation
 *     Martin Rouaux - support for generic scaling rules
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.infrastructure.master.Node;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.PolicyRules;
import uk.ac.imperial.lsds.seep.operator.Connectable;
import uk.ac.imperial.lsds.seep.operator.Operator;
import uk.ac.imperial.lsds.seep.operator.OperatorCode;
import uk.ac.imperial.lsds.seep.operator.compose.MultiOperator;
import uk.ac.imperial.lsds.seep.operator.compose.SubOperator;
import uk.ac.imperial.lsds.seep.state.CustomState;
import uk.ac.imperial.lsds.seep.state.LargeState;
import uk.ac.imperial.lsds.seep.state.Partitionable;
import uk.ac.imperial.lsds.seep.state.StateWrapper;

public class QueryPlan {
	
	final private Logger LOG = LoggerFactory.getLogger(QueryPlan.class);
	
	private int nodeId = 0;
	
	private ArrayList<Operator> ops = new ArrayList<Operator>();
	private ArrayList<StateWrapper> states = new ArrayList<StateWrapper>();
	private ArrayList<ScaleOutIntentBean> scIntents = new ArrayList<ScaleOutIntentBean>();
	private Map<Operator, Integer> partitionRequirements = new LinkedHashMap<Operator, Integer>(0);
	private Map<Integer, Connectable> elements = new HashMap<Integer, Connectable>();
	//More than one source is supported
	private ArrayList<Operator> src = new ArrayList<Operator>();
	private Operator snk;
	//Mapping of operators to node
	private Map<Integer, Operator> mapOperatorToNode = new LinkedHashMap<Integer, Operator>();
	
    private PolicyRules policyRules;
    
	public ArrayList<Operator> getOps(){
		return ops;
	}
	
	public ArrayList<StateWrapper> getStates(){
		return states;
	}
	
	public ArrayList<ScaleOutIntentBean> getScaleOutIntents(){
		return scIntents;
	}

	public Map<Integer, Connectable> getElements() {
		return elements;
	}
	
	public Map<Operator, Integer> getPartitionRequirements(){
		return partitionRequirements;
	}

	public ArrayList<Operator> getSrc() {
		return src;
	}

	public Operator getSnk() {
		return snk;
	}
	
	public Map<Integer, Operator> getMapOperatorToNode(){
		return mapOperatorToNode;
	}
	
	/** User facing methods **/
	
	public Operator newStatefulSource(OperatorCode opCode, int opId, StateWrapper s, List<String> attributes){
		// Configure operator
		if(s.getOwnerId() != opId){
			LOG.error("Operator id: "+opId+" does not own state: "+s.getOwnerId());
			System.exit(0);
		}
		Operator op = this.newStatefulOperator(opCode, opId, s, attributes);
		this.setSource(op);
		
		op.getOpContext().setIsSource(true);
		return op;
	}
	
	public Operator newStatelessSource(OperatorCode opCode, int opId, List<String> attributes){
		// Configure operator
		Operator op = this.newStatelessOperator(opCode, opId, attributes);
		this.setSource(op);
		op.getOpContext().setIsSource(true);
		return op;
	}
	
	public Operator newStatefulSink(OperatorCode opCode, int opId, StateWrapper s, List<String> attributes){
		// Configure operator
		if(s.getOwnerId() != opId){
			LOG.error("Operator id: "+opId+" does not own state: "+s.getOwnerId());
			System.exit(0);
		}
		Operator op = this.newStatefulOperator(opCode, opId, s, attributes);
		this.setSink(op);
		op.getOpContext().setIsSink(true);
		return op;
	}
	
	public Operator newStatelessSink(OperatorCode opCode, int opId, List<String> attributes){
		Operator op = this.newStatelessOperator(opCode, opId, attributes);
		this.setSink(op);
		op.getOpContext().setIsSink(true);
		return op;
	}
	
	public Operator newStatelessOperator(OperatorCode opCode, int opId, List<String> attributes){
		// Configure operator
		LOG.info("Creating operator "+ opId + " with attributes: "+attributes);
		Operator op = Operator.getStatelessOperator(opId, opCode, attributes);
		this.addOperator(op);
		try {
			this.place(op);
		} 
		catch (NodeAlreadyInUseException e) {
			LOG.error("The instantiation has tried to place an operator in an already used node. Is queryBuilder used by multiple threads?");
			e.printStackTrace();
		}
		return op;
	}
	
	public Operator newStatefulOperator(OperatorCode opCode, int opId, StateWrapper s, List<String> attributes){
		// Configure operator
		if(s.getOwnerId() != opId){
			LOG.error("Operator id: "+opId+" does not own state: "+s.getOwnerId());
			System.exit(0);
		}
		Operator op = Operator.getStatefulOperator(opId, opCode, s, attributes);
		// Register state
		this.registerState(s);
		this.addOperator(op);
		try {
			this.place(op);
		}
		catch (NodeAlreadyInUseException e) {
			LOG.error("The instantiation has tried to place an operator in an already used node. Is queryBuilder used by multiple threads?");
			e.printStackTrace();
		}
		return op;
	}
	
	public Connectable newMultiOperator(Set<SubOperator> subOperators,
			int multiOpId, List<String> attributes) {
		// First create multiOperator
		MultiOperator mo = MultiOperator.synthesizeFrom(subOperators, multiOpId);
		// Then compose the multiOperator into a SEEP Operator
		Operator op = Operator.getStatelessOperator(multiOpId, mo, attributes);
		this.addOperator(op);
		try {
			this.place(op);
		}
		catch (NodeAlreadyInUseException e) {
			LOG.error("The instantiation has tried to place an operator in an already used node. Is queryBuilder used by multiple threads?");
			e.printStackTrace();
		}
		return op;
	}
	
	public StateWrapper newCustomState(CustomState s, int ownerId, int checkpointInterval, String keyAttribute){
		StateWrapper sw = new StateWrapper(ownerId, checkpointInterval, s);
		if(s instanceof Partitionable){
			((Partitionable)s).setKeyAttribute(keyAttribute);
		}
		else{
			// TODO: say that keyattribute is ignored as state does not implement Partitionable
		}
		return sw;
	}
	
	public StateWrapper newLargeState(LargeState s, int ownerId, int checkpointInterval){
		return new StateWrapper(ownerId, checkpointInterval, s);
	}
	
	/** Static scaling methods **/
	
	/** This is the preferred function, that will automatically load balance the static partitioning**/
	public void scaleOut(int opId, int numPartitions){
		Operator op = getOperatorWithOpId(opId);
		partitionRequirements.put(op, numPartitions);
	}
	
	/** This function is provided in case the user wants to manually define which partitions to be done**/
	public void scaleOut(int opId, int newOpId, Node newProvisionedNode){
		Operator op = getOperatorWithOpId(opId);
		// Register the intent to scale out
		ScaleOutIntentBean soib = new ScaleOutIntentBean(op, newOpId, newProvisionedNode);
		scIntents.add(soib);
	}
	
	private Operator getOperatorWithOpId(int opId){
		for(Operator op : ops){
			if(op.getOperatorId() == opId) return op;
		}
		return null;
	}
	
	
	/** Mapping between operator and node. Note that only one operator is assigned to one node. Two "conceptual" operators can be
	 * merged into one "deployable" operator. This means that at this point all optimisations regarding "conceptual" operators have
	 * been made.
	 * @throws NodeAlreadyInUseException **/
	public void place(Operator o) throws NodeAlreadyInUseException{
		if(mapOperatorToNode.containsKey(nodeId)){
			throw new NodeAlreadyInUseException();
		}
		else{
			mapOperatorToNode.put(nodeId, o);
		}
		// Finally we increase the nodeId
		nodeId++;
	}
	
	private void setSource(Operator source) {
		LOG.info("Configured NEW SOURCE, Operator: {}", src.toString());
		src.add(source);
	}

	private void setSink(Operator snk){
		LOG.info("Configured SINK as Operator: {}", snk.toString());
		this.snk = snk;
	}
	
	private void registerState(StateWrapper s){
		states.add(s);
		LOG.debug("Added new State to Query");
	}
	
	private void addOperator(Operator o) {
		ops.add(o);
		elements.put(o.getOperatorId(), o);
		LOG.info("Added new Operator to Infrastructure: {}", o.toString());
	}
    
    public void withPolicyRules(PolicyRules rules) {
        policyRules = rules;
        LOG.info("Initialising query with scaling policy: {}", rules.toString());
    }
    
    public PolicyRules getPolicyRules() {
        return policyRules;
    }
}
