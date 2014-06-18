/*******************************************************************************
 * Copyright (c) 2014 Imperial College London
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial API and implementation
 ******************************************************************************/

package uk.ac.imperial.lsds.seep.operator.compose2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import uk.ac.imperial.lsds.seep.api.QueryBuilder;
import uk.ac.imperial.lsds.seep.api.QueryComposer;
import uk.ac.imperial.lsds.seep.api.QueryPlan;
import uk.ac.imperial.lsds.seep.operator.Connectable;
import uk.ac.imperial.lsds.seep.operator.compose.micro.IMicroOperatorConnectable;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.ISubQueryConnectable;

public class Base implements QueryComposer{

	public QueryPlan compose() {
		
		// Declare Source
		ArrayList<String> srcFields = new ArrayList<String>();
		srcFields.add("value1");
		srcFields.add("value2");
		srcFields.add("value3");
		Connectable src = QueryBuilder.newStatelessSource(new Source(), -1, srcFields);
			
		// Declare sink
		ArrayList<String> snkFields = new ArrayList<String>();
		snkFields.add("value1");
		snkFields.add("value2");
		Connectable snk = QueryBuilder.newStatelessSink(new Sink(), -2, snkFields);

		// Micro ops for first subquery
		IMicroOperatorConnectable mOp1 = QueryBuilder.newMicroOperator(null, 1, null);
		IMicroOperatorConnectable mOp2 = QueryBuilder.newMicroOperator(null, 1, null);
		mOp1.connectTo(1, mOp2);
		
		// Micro ops for second subquery
		IMicroOperatorConnectable mOp3 = QueryBuilder.newMicroOperator(null, 1, null);
		IMicroOperatorConnectable mOp4 = QueryBuilder.newMicroOperator(null, 1, null);
		mOp3.connectTo(1, mOp4);
		
		// Create subqueries
		Set<IMicroOperatorConnectable> microOpConnectables1 = new HashSet<>();
		microOpConnectables1.add(mOp1);
		microOpConnectables1.add(mOp2);		
		ISubQueryConnectable sq1 = QueryBuilder.newSubQuery(microOpConnectables1, 1, srcFields);

		Set<IMicroOperatorConnectable> microOpConnectables2 = new HashSet<>();
		microOpConnectables2.add(mOp1);
		microOpConnectables2.add(mOp2);		
		ISubQueryConnectable sq2 = QueryBuilder.newSubQuery(microOpConnectables2, 1, srcFields);

		// Connect subqueries
		sq1.connectTo(1, sq2);

		Set<ISubQueryConnectable> subQueries = new HashSet<>();
		subQueries.add(sq1);
		subQueries.add(sq2);
		
		Connectable multiOp = QueryBuilder.newMultiOperator(subQueries, 1, srcFields);

		/** Connect operators **/
		src.connectTo(multiOp, true, 0);
		multiOp.connectTo(snk, true, 0);
		
		return QueryBuilder.build();
	}
}
