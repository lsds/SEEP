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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import operators.Sink;
import operators.Source;
import uk.ac.imperial.lsds.seep.api.QueryBuilder;
import uk.ac.imperial.lsds.seep.api.QueryComposer;
import uk.ac.imperial.lsds.seep.api.QueryPlan;
import uk.ac.imperial.lsds.seep.operator.Connectable;
import uk.ac.imperial.lsds.seep.operator.compose.micro.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.operator.compose.micro.IMicroOperatorConnectable;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.ISubQueryConnectable;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowDefinition;
import uk.ac.imperial.lsds.seep.operator.compose.window.WindowDefinition;
import uk.ac.imperial.lsds.seep.operator.compose.window.WindowDefinition.WindowType;
import uk.ac.imperial.lsds.streamsql.conversion.IntegerConversion;
import uk.ac.imperial.lsds.streamsql.conversion.StringConversion;
import uk.ac.imperial.lsds.streamsql.expressions.ColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.Division;
import uk.ac.imperial.lsds.streamsql.expressions.IValueExpression;
import uk.ac.imperial.lsds.streamsql.expressions.ValueExpression;
import uk.ac.imperial.lsds.streamsql.op.stateless.Projection;

public class Base implements QueryComposer{
	public QueryPlan compose() {
		
		// Declare Source
		List<String> posSpeedStr = new ArrayList<String>();
		posSpeedStr.add("vehicleId");
		posSpeedStr.add("speed");
		posSpeedStr.add("xPos");
		posSpeedStr.add("dir");
		posSpeedStr.add("hwy");
		Connectable src = QueryBuilder.newStatelessSource(new Source(), -1, posSpeedStr);


		
		return QueryBuilder.build();
	}
}
