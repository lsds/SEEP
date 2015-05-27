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

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.acita15.operators.Processor;
import uk.ac.imperial.lsds.seep.acita15.operators.Join;
import uk.ac.imperial.lsds.seep.acita15.operators.Sink;
import uk.ac.imperial.lsds.seep.acita15.operators.Source;
import uk.ac.imperial.lsds.seep.api.QueryBuilder;
import uk.ac.imperial.lsds.seep.api.QueryComposer;
import uk.ac.imperial.lsds.seep.api.QueryPlan;
import uk.ac.imperial.lsds.seep.operator.Connectable;
import uk.ac.imperial.lsds.seep.operator.InputDataIngestionMode;

public class Base implements QueryComposer{
	private int CHAIN_LENGTH;
	private int REPLICATION_FACTOR;
	private final boolean AUTO_SCALEOUT = true;
	private final boolean CONNECT_TO_ALL_DOWNSTREAMS = false;	//Deprecated.

	public QueryPlan compose() {
		/** Declare operators **/
		//TODO: Operator ids
		//TODO: Stream ids
		
		REPLICATION_FACTOR = Integer.parseInt(GLOBALS.valueFor("replicationFactor"));
		CHAIN_LENGTH = Integer.parseInt(GLOBALS.valueFor("chainLength"));
		
		if (GLOBALS.valueFor("queryType").equals("chain"))
		{
			return composeChain();
		}
		else if (GLOBALS.valueFor("queryType").equals("join"))
		{
			return composeJoin();
		}
		else { throw new RuntimeException("Logic error - unknown query type: "+GLOBALS.valueFor("queryType")); }
	}

	private QueryPlan composeChain()
	{
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
	
	private QueryPlan composeJoin()
	{
		if (CHAIN_LENGTH != 1) { throw new RuntimeException("TODO"); }
		
		// Declare Source 1
		ArrayList<String> src1Fields = new ArrayList<String>();
		src1Fields.add("tupleId");
		src1Fields.add("value");
		Connectable src1 = QueryBuilder.newStatelessSource(new Source(), -1, src1Fields);
		
		// Declare Source 2
		ArrayList<String> src2Fields = new ArrayList<String>();
		src2Fields.add("tupleId");
		src2Fields.add("value");
		Connectable src2 = QueryBuilder.newStatelessSource(new Source(), -3, src2Fields);
		
		// Declare sink
		ArrayList<String> snkFields = new ArrayList<String>();
		snkFields.add("tupleId");
		snkFields.add("value");
		Connectable snk = QueryBuilder.newStatelessSink(new Sink(), -2, snkFields);
		
		//Declare join
		ArrayList<String> jFields = new ArrayList<String>();
		jFields.add("tupleId");
		jFields.add("value");
		Connectable j = QueryBuilder.newStatelessOperator(new Join(), 0, jFields);
		
		src1.connectTo(j, InputDataIngestionMode.UPSTREAM_SYNC_BATCH_BUFFERED_BARRIER, true, 0);
		src2.connectTo(j, InputDataIngestionMode.UPSTREAM_SYNC_BATCH_BUFFERED_BARRIER, true, 1);
		j.connectTo(snk, true, 2);
		
		QueryBuilder.scaleOut(j.getOperatorId(), REPLICATION_FACTOR);
		
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

		if (ops.isEmpty())
		{
			src.connectTo(snk, true, 0);
		}
		else
		{
			//Connect src to first op
			src.connectTo(ops.get(0).get(0), true, 0);
	
			// Connect ops to sink
			for (int j = 0; j < ops.get(CHAIN_LENGTH-1).size(); j++)
			{
				ops.get(CHAIN_LENGTH-1).get(j).connectTo(snk, true, CHAIN_LENGTH);
			}
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
