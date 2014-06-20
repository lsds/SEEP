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
import java.util.Map;
import java.util.Set;

import uk.ac.imperial.lsds.seep.infrastructure.master.Node;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.PolicyRules;
import uk.ac.imperial.lsds.seep.operator.Connectable;
import uk.ac.imperial.lsds.seep.operator.OperatorCode;
import uk.ac.imperial.lsds.seep.operator.compose.micro.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.operator.compose.micro.IMicroOperatorConnectable;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.ISubQueryConnectable;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowDefinition;
import uk.ac.imperial.lsds.seep.state.CustomState;
import uk.ac.imperial.lsds.seep.state.LargeState;
import uk.ac.imperial.lsds.seep.state.StateWrapper;

public class QueryBuilder {
	
	private static QueryPlan qp = new QueryPlan();
	
	public static QueryPlan build(){
		return qp;
	}
	
	public static Connectable newStatefulSource(OperatorCode op, int opId, StateWrapper s, List<String> attributes){
		return qp.newStatefulSource(op, opId, s, attributes);
	}
	
	public static Connectable newStatelessSource(OperatorCode op, int opId, List<String> attributes){
		return qp.newStatelessSource(op, opId, attributes);
	}
	
	public static Connectable newStatefulOperator(OperatorCode op, int opId, StateWrapper s, List<String> attributes){
		return qp.newStatefulOperator(op, opId, s, attributes);
	}
	
	public static Connectable newStatelessOperator(OperatorCode op, int opId, List<String> attributes){
		return qp.newStatelessOperator(op, opId, attributes);
	}
	
	public static Connectable newStatefulSink(OperatorCode op, int opId, StateWrapper s, List<String> attributes){
		return qp.newStatefulSink(op, opId, s, attributes);
	}
	
	public static Connectable newStatelessSink(OperatorCode op, int opId, List<String> attributes){
		return qp.newStatelessSink(op, opId, attributes);
	}
	
	public static StateWrapper newCustomState(CustomState s, int ownerId, int checkpointInterval, String keyAttribute){
		return qp.newCustomState(s, ownerId, checkpointInterval, keyAttribute);
	}
	
	public static StateWrapper newLargeState(LargeState s, int ownerId, int checkpointInterval){
		return qp.newLargeState(s, ownerId, checkpointInterval);
	}

	public static void scaleOut(int opToScaleOut, int numPartitions){
		qp.scaleOut(opToScaleOut, numPartitions);
	}
	
	public static void scaleOut(int opToScaleOut, int newOpId, Node newProvisionedNode){
		qp.scaleOut(opToScaleOut, newOpId, newProvisionedNode);
	}
    
    public static void withPolicyRules(PolicyRules rules) {
        qp.withPolicyRules(rules);
    }
	
	public static Connectable newMultiOperator(Set<ISubQueryConnectable> subOperators, int opId, List<String> attributes){
		return qp.newMultiOperator(subOperators, opId, attributes);
	}

	public static IMicroOperatorConnectable newMicroOperator(IMicroOperatorCode op, int opId, List<String> attributes){
		return qp.newMicroOperator(op, opId, attributes);
	}

	public static ISubQueryConnectable newSubQuery(Set<IMicroOperatorConnectable> microOperators, int opId, List<String> attributes, Map<Integer, IWindowDefinition> windowDefs){
		return qp.newSubQuery(microOperators, opId, attributes, windowDefs);
	}

}
