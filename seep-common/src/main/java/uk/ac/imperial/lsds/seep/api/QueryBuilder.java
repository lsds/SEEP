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

import java.util.List;

public class QueryBuilder implements QueryAPI {
	
	private static LogicalSeepQuery qp = new LogicalSeepQuery();
	
	public static LogicalSeepQuery build(){
		return qp;
	}

	@Override
	public List<LogicalOperator> getQueryOperators() {
		return qp.getAllOperators();
	}

	@Override
	public List<LogicalState> getQueryState() {
		return qp.getAllStates();
	}

	@Override
	public int getInitialPhysicalInstancesPerLogicalOperator(int logicalOperatorId) {
		return 0;
	}

	@Override
	public List<LogicalOperator> getSources() {
		return qp.getSources();
	}

	@Override
	public LogicalOperator getSink() {
		return qp.getSink();
	}

	@Override
	public LogicalOperator newStatefulSource(SeepTask seepTask,
			LogicalState state, int opId) {
		return qp.newStatefulSource(seepTask, state, opId);
	}

	@Override
	public LogicalOperator newStatelessSource(SeepTask seepTask, int opId) {
		return qp.newStatelessSource(seepTask, opId);
	}

	@Override
	public LogicalOperator newStatefulOperator(SeepTask seepTask,
			LogicalState state, int opId) {
		return qp.newStatefulOperator(seepTask, state, opId);
	}

	@Override
	public LogicalOperator newStatelessOperator(SeepTask seepTask, int opId) {
		return qp.newStatelessOperator(seepTask, opId);
	}

	@Override
	public LogicalOperator newStatefulSink(SeepTask seepTask,
			LogicalState state, int opId) {
		return qp.newStatefulSink(seepTask, state, opId);
	}

	@Override
	public LogicalOperator newStatelessSink(SeepTask seepTask, int opId) {
		return qp.newStatelessSink(seepTask, opId);
	}

	@Override
	public void setInitialPhysicalInstancesForLogicalOperator(int opId,
			int numInstances) {
		qp.setInitialPhysicalInstancesPerLogicalOperator(opId, numInstances);
	}

	@Override
	public LogicalState newLogicalState(SeepState state, int ownerId) {
		return qp.newLogicalState(state, ownerId);
	}
	
//	public static Connectable newStatefulSource(OperatorCode op, int opId, StateWrapper s, List<String> attributes){
//		return qp.newStatefulSource(op, opId, s, attributes);
//	}
//	
//	public static Connectable newStatelessSource(OperatorCode op, int opId, List<String> attributes){
//		return qp.newStatelessSource(op, opId, attributes);
//	}
//	
//	public static Connectable newStatefulOperator(OperatorCode op, int opId, StateWrapper s, List<String> attributes){
//		return qp.newStatefulOperator(op, opId, s, attributes);
//	}
//	
//	public static Connectable newStatelessOperator(OperatorCode op, int opId, List<String> attributes){
//		return qp.newStatelessOperator(op, opId, attributes);
//	}
//	
//	public static Connectable newStatefulSink(OperatorCode op, int opId, StateWrapper s, List<String> attributes){
//		return qp.newStatefulSink(op, opId, s, attributes);
//	}
//	
//	public static Connectable newStatelessSink(OperatorCode op, int opId, List<String> attributes){
//		return qp.newStatelessSink(op, opId, attributes);
//	}
//	
//	public static Connectable newMultiOperator(Set<SubOperator> subOperators, int multiOpId, List<String> attributes){
//		return qp.newMultiOperator(subOperators, multiOpId, attributes);
//	}
//	
//	public static StateWrapper newCustomState(CustomState s, int ownerId, int checkpointInterval, String keyAttribute){
//		return qp.newCustomState(s, ownerId, checkpointInterval, keyAttribute);
//	}
//	
//	public static StateWrapper newLargeState(LargeState s, int ownerId, int checkpointInterval){
//		return qp.newLargeState(s, ownerId, checkpointInterval);
//	}
//
//	public static void scaleOut(int opToScaleOut, int numPartitions){
//		qp.scaleOut(opToScaleOut, numPartitions);
//	}
//	
//	public static void scaleOut(int opToScaleOut, int newOpId, Node newProvisionedNode){
//		qp.scaleOut(opToScaleOut, newOpId, newProvisionedNode);
//	}
}
