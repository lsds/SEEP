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
import uk.ac.imperial.lsds.seep.acita15.operators.FaceDetector;
import uk.ac.imperial.lsds.seep.acita15.operators.FaceRecognizer;
import uk.ac.imperial.lsds.seep.acita15.operators.SpeechRecognizer;
import uk.ac.imperial.lsds.seep.acita15.operators.Join;
import uk.ac.imperial.lsds.seep.acita15.operators.Sink;
import uk.ac.imperial.lsds.seep.acita15.operators.Source;
import uk.ac.imperial.lsds.seep.acita15.operators.AudioSource;
import uk.ac.imperial.lsds.seep.acita15.operators.VideoSource;
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
		
		String queryType = GLOBALS.valueFor("queryType");
		if (queryType.equals("chain"))
		{
			return composeChain();
		}
		else if (queryType.equals("join"))
		{
			return composeJoin();
		}
		else if (queryType.equals("fr"))
		{
			return composeFaceRecognizer();
		}
		else if (queryType.equals("nameAssist"))
		{
			return composeNameAssist();
		}
		else if (queryType.equals("debsGC13"))
		{
			throw new RuntimeException("TODO");
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
	
	private QueryPlan composeFaceRecognizer()
	{
		// Declare Source
		ArrayList<String> srcFields = new ArrayList<String>();
		srcFields.add("tupleId");
		srcFields.add("value");
		srcFields.add("x");
		srcFields.add("y");
		srcFields.add("height");
		srcFields.add("width");
		Connectable src = QueryBuilder.newStatelessSource(new Source(), -1, srcFields);
		
		
		//Declare FaceDetector
		ArrayList<String> faceDetectFields = new ArrayList<String>();
		faceDetectFields.add("tupleId");
		faceDetectFields.add("value");
		faceDetectFields.add("x");
		faceDetectFields.add("y");
		faceDetectFields.add("height");
		faceDetectFields.add("width");
		Connectable faceDetect = QueryBuilder.newStatelessOperator(new FaceDetector(), 0, faceDetectFields);
		
		
		//Declare SpeechRecognizer
		ArrayList<String> faceRecFields = new ArrayList<String>();
		faceRecFields.add("tupleId");
		faceRecFields.add("value");
		faceRecFields.add("x");
		faceRecFields.add("y");
		faceRecFields.add("height");
		faceRecFields.add("width");
		Connectable faceRec = QueryBuilder.newStatelessOperator(new FaceRecognizer(), 1, faceRecFields);
		
		// Declare sink
		ArrayList<String> snkFields = new ArrayList<String>();
		snkFields.add("tupleId");
		snkFields.add("value");
		snkFields.add("x");
		snkFields.add("y");
		snkFields.add("height");
		snkFields.add("width");
		Connectable snk = QueryBuilder.newStatelessSink(new Sink(), -2, snkFields);
		
		src.connectTo(faceDetect, true, 0);
		faceDetect.connectTo(faceRec, true, 1);
		faceRec.connectTo(snk, true, 2);
		
		if (REPLICATION_FACTOR > 1)
		{
			QueryBuilder.scaleOut(faceDetect.getOperatorId(), REPLICATION_FACTOR);
			QueryBuilder.scaleOut(faceRec.getOperatorId(), REPLICATION_FACTOR);
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
		
		if (REPLICATION_FACTOR > 1)
		{
			QueryBuilder.scaleOut(j.getOperatorId(), REPLICATION_FACTOR);
		}
		
		return QueryBuilder.build();
	}
	
	private QueryPlan composeNameAssist()
	{
		// Declare Source 1
		ArrayList<String> src1Fields = new ArrayList<String>();
		src1Fields.add("tupleId");
		src1Fields.add("value");
		Connectable src1 = QueryBuilder.newStatelessSource(new Source(), -1, src1Fields);
		
		// Declare Source 2
		ArrayList<String> src2Fields = new ArrayList<String>();
		src2Fields.add("tupleId");
		src2Fields.add("value");
		Connectable src2 = QueryBuilder.newStatelessSource(new AudioSource(), -3, src2Fields);
		
		// Declare sink
		ArrayList<String> snkFields = new ArrayList<String>();
		snkFields.add("tupleId");
		snkFields.add("value");
		Connectable snk = QueryBuilder.newStatelessSink(new Sink(), -2, snkFields);
		
		//Declare SpeechRecognizer
		ArrayList<String> speechFields = new ArrayList<String>();
		speechFields.add("tupleId");
		speechFields.add("value");
		Connectable speechRec = QueryBuilder.newStatelessOperator(new SpeechRecognizer(), 0, speechFields);
		
		//Declare FaceDetector
		ArrayList<String> faceFields = new ArrayList<String>();
		faceFields.add("tupleId");
		faceFields.add("value");
		Connectable faceDet = QueryBuilder.newStatelessOperator(new FaceDetector(), 1, faceFields);
		
		
		//Declare join
		ArrayList<String> jFields = new ArrayList<String>();
		jFields.add("tupleId");
		jFields.add("value");
		Connectable j = QueryBuilder.newStatelessOperator(new Join(), 2, jFields);
		
		src1.connectTo(faceDet, true, 0);
		src2.connectTo(speechRec, true, 1);
		speechRec.connectTo(j, InputDataIngestionMode.UPSTREAM_SYNC_BATCH_BUFFERED_BARRIER, true, 2);
		faceDet.connectTo(j, InputDataIngestionMode.UPSTREAM_SYNC_BATCH_BUFFERED_BARRIER, true, 3);
		j.connectTo(snk, true, 4);
		
		if (REPLICATION_FACTOR > 1)
		{
			if (REPLICATION_FACTOR > 2) { throw new RuntimeException("TODO"); }
			QueryBuilder.scaleOut(speechRec.getOperatorId(), REPLICATION_FACTOR);
			QueryBuilder.scaleOut(faceDet.getOperatorId(), REPLICATION_FACTOR);
			QueryBuilder.scaleOut(j.getOperatorId(), REPLICATION_FACTOR);
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
