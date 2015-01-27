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
import java.util.Map;
import java.util.HashMap;

import uk.ac.imperial.lsds.seep.acita15.operators.Processor;
import uk.ac.imperial.lsds.seep.acita15.operators.Sink;
import uk.ac.imperial.lsds.seep.acita15.operators.Source;
import uk.ac.imperial.lsds.seep.api.QueryBuilder;
import uk.ac.imperial.lsds.seep.api.QueryComposer;
import uk.ac.imperial.lsds.seep.api.QueryPlan;
import uk.ac.imperial.lsds.seep.operator.Connectable;

public class Base implements QueryComposer{
	private final int CHAIN_LENGTH = 2;
	private final int REPLICATION_FACTOR = 2;
	private final boolean AUTO_SCALEOUT = true;
	private final boolean CONNECT_TO_ALL_DOWNSTREAMS = false;	//Deprecated.

	public QueryPlan compose() {
		/** Declare operators **/
		//TODO: Operator ids
		//TODO: Stream ids
		
		// Declare Source
		ArrayList<String> srcFields = new ArrayList<String>();
		srcFields.add("tupleId");
		srcFields.add("value");
		Connectable src = QueryBuilder.newStatelessSource(new Source(), -1, srcFields);
		
		
		
		// Declare sink
		ArrayList<String> snkFields = new ArrayList<String>();
		snkFields.add("tupleId");
		snkFields.add("value");
		Connectable snk = QueryBuilder.newStatelessSink(new Sink(), -2, snkFields);
		
		
		if (AUTO_SCALEOUT)
		{
			Map<Integer, Map<Integer, Connectable>> ops = this.createChainOps(CHAIN_LENGTH, 1); 
			connectToOneDownstream(src, snk, ops);
			autoScaleout(ops);
		}
		else
		{
			// Declare processors
			Map<Integer, Map<Integer, Connectable>> ops = this.createChainOps(CHAIN_LENGTH, REPLICATION_FACTOR); 
			
			if (CONNECT_TO_ALL_DOWNSTREAMS)
			{
				connectToAllDownstreams(src, snk, ops);
			}
			else
			{
				connectToOneDownstream(src, snk, ops);
			}
		}
		
		return QueryBuilder.build();
	}

	private Map<Integer, Map<Integer, Connectable>> createChainOps(int chainLength, int replicationFactor)
	{
		Map<Integer, Map<Integer, Connectable>> ops = new HashMap();

		for (int i = 0; i < chainLength; i++)
		{
			ops.put(i, new HashMap<Integer, Connectable>());
			for (int j = 0; j < replicationFactor; j++)
			{ 
				ArrayList<String> pFields = new ArrayList<String>();
				pFields.add("tupleId");
				pFields.add("value");
				Connectable p = QueryBuilder.newStatelessOperator(new Processor(), (i*replicationFactor)+j, pFields);
				ops.get(i).put(j, p);
			}
		}

		return ops;
	}

	private void connectToAllDownstreams(Connectable src, Connectable snk, Map<Integer, Map<Integer, Connectable>> ops)
	{
		// Connect intermediate ops 
		for (int i = 0; i < CHAIN_LENGTH-1; i++)
		{
			for (int j = 0; j < ops.get(i).size(); j++)
			{
				for (int k=0; k < ops.get(i+1).size(); k++)
				{
					ops.get(i).get(j).connectTo(ops.get(i+1).get(k), true, i+1);
				}
			}
		}

		// Connect src to first layer ops 
		for (int j = 0; j < ops.get(0).size(); j++)
		{
			src.connectTo(ops.get(0).get(j), true, 0);
		}

		// Connect final layer ops to sink
		for (int j = 0; j < ops.get(CHAIN_LENGTH-1).size(); j++)
		{
			ops.get(CHAIN_LENGTH-1).get(j).connectTo(snk, true, CHAIN_LENGTH);
		}
	}

	private void connectToOneDownstream(Connectable src, Connectable snk, Map<Integer, Map<Integer, Connectable>> ops)
	{
		// Connect intermediate ops 
		// Assumes all layers are the same size.
		for (int i = 0; i < CHAIN_LENGTH-1; i++)
		{
			for (int j = 0; j < ops.get(i).size(); j++)
			{
				ops.get(i).get(j).connectTo(ops.get(i+1).get(j), true, i+1);
			}
		}

		//Connect src to first op
		src.connectTo(ops.get(0).get(0), true, 0);

		// Connect ops to sink
		for (int j = 0; j < ops.get(CHAIN_LENGTH-1).size(); j++)
		{
			ops.get(CHAIN_LENGTH-1).get(j).connectTo(snk, true, CHAIN_LENGTH);
		}
	}
	
	private void autoScaleout(Map<Integer, Map<Integer, Connectable>> ops)
	{
		for (int i = 0; i < CHAIN_LENGTH; i++)
		{
			if (ops.get(i).size() != 1) { throw new RuntimeException("Logic error."); }
			QueryBuilder.scaleOut(ops.get(i).get(0).getOperatorId(), REPLICATION_FACTOR);
		}
	}
}
