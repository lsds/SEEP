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

import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.acita15.operators.Processor;
import uk.ac.imperial.lsds.seep.acita15.operators.FaceDetector;
import uk.ac.imperial.lsds.seep.acita15.operators.FaceDetectorRecognizer;
import uk.ac.imperial.lsds.seep.acita15.operators.SEEPFaceRecognizer;
import uk.ac.imperial.lsds.seep.acita15.operators.SEEPFaceRecognizerJoin;
//import uk.ac.imperial.lsds.seep.acita15.operators.SpeechRecognizer;
import uk.ac.imperial.lsds.seep.acita15.operators.Join;
import uk.ac.imperial.lsds.seep.acita15.operators.HeatMapJoin;
import uk.ac.imperial.lsds.seep.acita15.operators.HeatMapSink;
import uk.ac.imperial.lsds.seep.acita15.operators.Sink;
import uk.ac.imperial.lsds.seep.acita15.operators.Source;
//import uk.ac.imperial.lsds.seep.acita15.operators.AudioSource;
import uk.ac.imperial.lsds.seep.acita15.operators.VideoSource;
import uk.ac.imperial.lsds.seep.acita15.operators.VideoSource2;
import uk.ac.imperial.lsds.seep.acita15.operators.VideoSink;
import uk.ac.imperial.lsds.seep.acita15.operators.LocationSource;
import uk.ac.imperial.lsds.seep.api.QueryBuilder;
import uk.ac.imperial.lsds.seep.api.QueryComposer;
import uk.ac.imperial.lsds.seep.api.QueryPlan;
import uk.ac.imperial.lsds.seep.operator.Connectable;
import uk.ac.imperial.lsds.seep.operator.InputDataIngestionMode;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class Base implements QueryComposer{
	private final static Logger logger = LoggerFactory.getLogger(Base.class);
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
		else if (queryType.equals("fdr"))
		{
			return composeFaceDetectorRecognizer();
		}
		else if (queryType.equals("nameAssist"))
		{
			return composeNameAssist();
		}
		else if (queryType.equals("debsGC13"))
		{
			throw new RuntimeException("TODO");
		}
		else if (queryType.equals("heatMap"))
		{
			return composeHeatMap();
		}
		else if (queryType.equals("leftJoin"))
		{
			return composeLeftJoin();
		}
		else if (queryType.equals("frJoin"))
		{
			return composeFaceRecognizerJoin();
		}
		else { throw new RuntimeException("Logic error - unknown query type: "+GLOBALS.valueFor("queryType")); }
	}

	private QueryPlan composeChain()
	{
		// Declare Source
		ArrayList<String> srcFields = new ArrayList<String>();
		srcFields.add("tupleId");
		srcFields.add("value");
		srcFields.add("latencyBreakdown");
		Connectable src = QueryBuilder.newStatelessSource(new Source(), -1, srcFields);
		
		
		
		// Declare sink
		ArrayList<String> snkFields = new ArrayList<String>();
		snkFields.add("tupleId");
		snkFields.add("value");
		snkFields.add("latencyBreakdown");
		Connectable snk = QueryBuilder.newStatelessSink(new Sink(), -2, snkFields);
		
		
		if (AUTO_SCALEOUT)
		{
			if (Boolean.parseBoolean(GLOBALS.valueFor("scaleOutSinks")))
			{
				int sinkScaleFactor = Integer.parseInt(GLOBALS.valueFor("sinkScaleFactor"));
				QueryBuilder.scaleOut(snk.getOperatorId(), sinkScaleFactor > 0 ? sinkScaleFactor : REPLICATION_FACTOR);
			}
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
		srcFields.add("rows");
		srcFields.add("cols");
		srcFields.add("type");
		srcFields.add("x");
		srcFields.add("y");
		srcFields.add("x2");
		srcFields.add("y2");
		srcFields.add("label");
		Connectable src = QueryBuilder.newStatelessSource(new VideoSource(), -1, srcFields);
		
		
		//Declare FaceDetector
		ArrayList<String> faceDetectFields = new ArrayList<String>();
		faceDetectFields.add("tupleId");
		faceDetectFields.add("value");
		faceDetectFields.add("rows");
		faceDetectFields.add("cols");
		faceDetectFields.add("type");
		faceDetectFields.add("x");
		faceDetectFields.add("y");
		faceDetectFields.add("x2");
		faceDetectFields.add("y2");
		faceDetectFields.add("label");
		Connectable faceDetect = QueryBuilder.newStatelessOperator(new FaceDetector(), 0, faceDetectFields);
		
		
		//Declare SpeechRecognizer
		ArrayList<String> faceRecFields = new ArrayList<String>();
		faceRecFields.add("tupleId");
		faceRecFields.add("value");
		faceRecFields.add("rows");
		faceRecFields.add("cols");
		faceRecFields.add("type");
		faceRecFields.add("x");
		faceRecFields.add("y");
		faceRecFields.add("x2");
		faceRecFields.add("y2");
		faceRecFields.add("label");
		Connectable faceRec = QueryBuilder.newStatelessOperator(new SEEPFaceRecognizer(), 1, faceRecFields);
		
		// Declare sink
		ArrayList<String> snkFields = new ArrayList<String>();
		snkFields.add("tupleId");
		snkFields.add("value");
		snkFields.add("rows");
		snkFields.add("cols");
		snkFields.add("type");
		snkFields.add("x");
		snkFields.add("y");
		snkFields.add("x2");
		snkFields.add("y2");
		snkFields.add("label");
		//Connectable snk = QueryBuilder.newStatelessSink(new Sink(), -2, snkFields);
		Connectable snk = QueryBuilder.newStatelessSink(new VideoSink(), -2, snkFields);
		
		src.connectTo(faceDetect, true, 0);
		faceDetect.connectTo(faceRec, true, 1);
		faceRec.connectTo(snk, true, 2);
		
			
		if (Boolean.parseBoolean(GLOBALS.valueFor("scaleOutSinks")))
		{
			int sinkScaleFactor = Integer.parseInt(GLOBALS.valueFor("sinkScaleFactor"));
			if (REPLICATION_FACTOR > 1 || sinkScaleFactor > 1)
			{
				QueryBuilder.scaleOut(snk.getOperatorId(), sinkScaleFactor > 0 ? sinkScaleFactor : REPLICATION_FACTOR);
			}
		}
		
		if (REPLICATION_FACTOR > 1)
		{
			QueryBuilder.scaleOut(faceDetect.getOperatorId(), REPLICATION_FACTOR);
			QueryBuilder.scaleOut(faceRec.getOperatorId(), REPLICATION_FACTOR);
		}

		return QueryBuilder.build();
	}
	
	private QueryPlan composeFaceDetectorRecognizer()
	{
		// Declare Source
		ArrayList<String> srcFields = new ArrayList<String>();
		srcFields.add("tupleId");
		srcFields.add("value");
		srcFields.add("rows");
		srcFields.add("cols");
		srcFields.add("type");
		srcFields.add("x");
		srcFields.add("y");
		srcFields.add("x2");
		srcFields.add("y2");
		srcFields.add("label");
		Connectable src = QueryBuilder.newStatelessSource(new VideoSource(), -1, srcFields);
		
		
		//Declare FaceDetector
		ArrayList<String> faceDetectorRecognizerFields = new ArrayList<String>();
		faceDetectorRecognizerFields.add("tupleId");
		faceDetectorRecognizerFields.add("value");
		faceDetectorRecognizerFields.add("rows");
		faceDetectorRecognizerFields.add("cols");
		faceDetectorRecognizerFields.add("type");
		faceDetectorRecognizerFields.add("x");
		faceDetectorRecognizerFields.add("y");
		faceDetectorRecognizerFields.add("x2");
		faceDetectorRecognizerFields.add("y2");
		faceDetectorRecognizerFields.add("label");
		Connectable faceDetectorRecognizer = QueryBuilder.newStatelessOperator(new FaceDetectorRecognizer(), 0, faceDetectorRecognizerFields);
		
		
		// Declare sink
		ArrayList<String> snkFields = new ArrayList<String>();
		snkFields.add("tupleId");
		snkFields.add("value");
		snkFields.add("rows");
		snkFields.add("cols");
		snkFields.add("type");
		snkFields.add("x");
		snkFields.add("y");
		snkFields.add("x2");
		snkFields.add("y2");
		snkFields.add("label");
		//Connectable snk = QueryBuilder.newStatelessSink(new Sink(), -2, snkFields);
		Connectable snk = QueryBuilder.newStatelessSink(new VideoSink(), -2, snkFields);
		
		src.connectTo(faceDetectorRecognizer, true, 0);
		faceDetectorRecognizer.connectTo(snk, true, 1);
		
		if (Boolean.parseBoolean(GLOBALS.valueFor("scaleOutSinks")))
		{
			int sinkScaleFactor = Integer.parseInt(GLOBALS.valueFor("sinkScaleFactor"));
			if (REPLICATION_FACTOR > 1 || sinkScaleFactor > 1)
			{
				QueryBuilder.scaleOut(snk.getOperatorId(), sinkScaleFactor > 0 ? sinkScaleFactor : REPLICATION_FACTOR);
			}
		}
		
		if (REPLICATION_FACTOR > 1)
		{
			QueryBuilder.scaleOut(faceDetectorRecognizer.getOperatorId(), REPLICATION_FACTOR);
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
		
		
		//if (Boolean.parseBoolean(GLOBALS.valueFor("scaleOutSinks"))) { throw new RuntimeException("TODO."); }
		if (Boolean.parseBoolean(GLOBALS.valueFor("scaleOutSinks")))
		{
			int sinkScaleFactor = Integer.parseInt(GLOBALS.valueFor("sinkScaleFactor"));
			if (REPLICATION_FACTOR > 1 || sinkScaleFactor > 1)
			{
				QueryBuilder.scaleOut(snk.getOperatorId(), sinkScaleFactor > 0 ? sinkScaleFactor : REPLICATION_FACTOR);
			}
		}

		if (REPLICATION_FACTOR > 1)
		{
			QueryBuilder.scaleOut(j.getOperatorId(), REPLICATION_FACTOR);
		}

		return QueryBuilder.build();
	}
	
	private QueryPlan composeHeatMap()
	{
		int nSources = Integer.parseInt(GLOBALS.valueFor("sources"));
		int nSinks = Integer.parseInt(GLOBALS.valueFor("sinks"));
		int maxFanIn = Integer.parseInt(GLOBALS.valueFor("fanin"));
		
		if (nSinks != 1) { throw new RuntimeException("TODO"); }
		int[] tree = computeJoinTree(nSources, maxFanIn);
		
		Connectable[] sources = new Connectable[nSources];
		Connectable[][] opsTree = new Connectable[tree.length][];
		Connectable[] sinks = new Connectable[nSinks];
		
		ArrayList<String> srcFields = new ArrayList<String>();
		srcFields.add("tupleId");
		srcFields.add("value");
		srcFields.add("padding");
		for (int i = 0; i < nSources; i++)
		{
			sources[i] = QueryBuilder.newStatelessSource(new LocationSource(), -(i+1), srcFields);
		}
		
		ArrayList<String> heatMapFields = new ArrayList<String>();
		heatMapFields.add("tupleId");
		heatMapFields.add("value");
		heatMapFields.add("padding");
		int opId = 0;
		for (int h = 0; h < tree.length; h++)
		{
			opsTree[h] = new Connectable[tree[h]];
			
			for (int i = 0; i < tree[h]; i++)
			{
				opsTree[h][i] = QueryBuilder.newStatelessOperator(new HeatMapJoin(), opId, heatMapFields);
				opId++;
			}
		}
		
		// Declare sink
		ArrayList<String> snkFields = new ArrayList<String>();
		snkFields.add("tupleId");
		snkFields.add("value");
		snkFields.add("padding");
		for (int i = 0; i < nSinks; i++)
		{
			sinks[i] = QueryBuilder.newStatelessSink(new HeatMapSink(), -(nSources+i+1), snkFields);
		}
		
		//Now connect everything up
		int streamId = 0;
		for (int i = 0; i < nSources; i++)
		{
			int parentIndex = i / maxFanIn;
			sources[i].connectTo(opsTree[0][parentIndex], InputDataIngestionMode.UPSTREAM_SYNC_BATCH_BUFFERED_BARRIER, true, streamId);
			streamId++;
		}
		
		for (int h = 0; h < opsTree.length-1; h++)
		{
			for (int i = 0; i < opsTree[h].length; i++)
			{
				int parentIndex = i / maxFanIn;
				opsTree[h][i].connectTo(opsTree[h+1][parentIndex], InputDataIngestionMode.UPSTREAM_SYNC_BATCH_BUFFERED_BARRIER, true, streamId);
				streamId++;
			}
		}
		
		for (int i = 0; i < opsTree[opsTree.length-1].length; i++)
		{
			for (int s = 0; s < sinks.length; s++)
			{
				opsTree[opsTree.length-1][i].connectTo(sinks[s], true, streamId);
				streamId++;
			}
		}
		

		if (Boolean.parseBoolean(GLOBALS.valueFor("scaleOutSinks")))
		{
			int sinkScaleFactor = Integer.parseInt(GLOBALS.valueFor("sinkScaleFactor"));
			if (REPLICATION_FACTOR > 1 || sinkScaleFactor > 1)
			{
				for (int i = 0; i < sinks.length; i++)
				{
					//QueryBuilder.scaleOut(sinks[i].getOperatorId(), REPLICATION_FACTOR);
					QueryBuilder.scaleOut(sinks[i].getOperatorId(), sinkScaleFactor > 0 ? sinkScaleFactor : REPLICATION_FACTOR);
				}
			}
		}
		
		if (REPLICATION_FACTOR > 1)
		{
			//Finally, scale out the operators.
			for (int h = 0; h < opsTree.length; h++)
			{
				for (int i = 0; i < opsTree[h].length; i++)
				{
					QueryBuilder.scaleOut(opsTree[h][i].getOperatorId(), REPLICATION_FACTOR);
				}
			}
		}	

		return QueryBuilder.build();
	}
	
	private QueryPlan composeLeftJoin()
	{
		int nSources = Integer.parseInt(GLOBALS.valueFor("sources"));
		int nSinks = Integer.parseInt(GLOBALS.valueFor("sinks"));
		int maxFanIn = Integer.parseInt(GLOBALS.valueFor("fanin"));
		
		if (nSinks != 1 || maxFanIn != 2) { throw new RuntimeException("TODO"); }
		int[] tree = computeJoinTree(nSources, maxFanIn);
		
		Connectable[] sources = new Connectable[nSources];
		Connectable[] ops = new Connectable[nSources-1];
		Connectable[] sinks = new Connectable[nSinks];
		
		ArrayList<String> srcFields = new ArrayList<String>();
		srcFields.add("tupleId");
		srcFields.add("value");
		srcFields.add("padding");

		for (int i = 0; i < nSources; i++)
		{
			sources[i] = QueryBuilder.newStatelessSource(new LocationSource(), -(i+1), srcFields);
		}
		
		ArrayList<String> heatMapFields = new ArrayList<String>();
		heatMapFields.add("tupleId");
		heatMapFields.add("value");
		heatMapFields.add("padding");
		int opId = 0;
		for (int h = 0; h < nSources-1; h++)
		{
			ops[h] = QueryBuilder.newStatelessOperator(new HeatMapJoin(), opId, heatMapFields);
			opId++;
		}
		
		// Declare sink
		ArrayList<String> snkFields = new ArrayList<String>();
		snkFields.add("tupleId");
		snkFields.add("value");
		snkFields.add("padding");
		for (int i = 0; i < nSinks; i++)
		{
			sinks[i] = QueryBuilder.newStatelessSink(new HeatMapSink(), -(nSources+i+1), snkFields);
		}
		
		//Now connect everything up
		sources[0].connectTo(ops[0], InputDataIngestionMode.UPSTREAM_SYNC_BATCH_BUFFERED_BARRIER, true, 0);
		int streamId = 1;
		for (int i = 1; i < nSources; i++)
		{
			sources[i].connectTo(ops[i-1], InputDataIngestionMode.UPSTREAM_SYNC_BATCH_BUFFERED_BARRIER, true, streamId);
			streamId++;
		}
	
		for (int h = 0; h < ops.length-1; h++)
		{
			ops[h].connectTo(ops[h+1], InputDataIngestionMode.UPSTREAM_SYNC_BATCH_BUFFERED_BARRIER, true, streamId);
			streamId++;
		}

		for (int s = 0; s < sinks.length; s++)
		{
			ops[ops.length-1].connectTo(sinks[s], true, streamId);
			streamId++;
		}
	
		if (Boolean.parseBoolean(GLOBALS.valueFor("scaleOutSinks")))
		{
			int sinkScaleFactor = Integer.parseInt(GLOBALS.valueFor("sinkScaleFactor"));
			if (REPLICATION_FACTOR > 1 || sinkScaleFactor > 1)
			{
				for (int i = 0; i < sinks.length; i++)
				{
					//QueryBuilder.scaleOut(sinks[i].getOperatorId(), REPLICATION_FACTOR);
					QueryBuilder.scaleOut(sinks[i].getOperatorId(), sinkScaleFactor > 0 ? sinkScaleFactor : REPLICATION_FACTOR);
				}
			}
		}
		
		if (REPLICATION_FACTOR > 1)
		{
			//Finally, scale out the operators.
			for (int h = 0; h < ops.length; h++)
			{
				QueryBuilder.scaleOut(ops[h].getOperatorId(), REPLICATION_FACTOR);
			}
		}	

		return QueryBuilder.build();
	}

	private QueryPlan composeFaceRecognizerJoin()
	{
		// Declare Source
		ArrayList<String> srcFields = new ArrayList<String>();
		srcFields.add("tupleId");
		srcFields.add("value");
		srcFields.add("rows");
		srcFields.add("cols");
		srcFields.add("type");
		srcFields.add("x");
		srcFields.add("y");
		srcFields.add("x2");
		srcFields.add("y2");
		srcFields.add("label");
		Connectable src1 = QueryBuilder.newStatelessSource(new VideoSource(), -1, srcFields);
		Connectable src2 = QueryBuilder.newStatelessSource(new VideoSource2(), -2, srcFields);
		
		
		//Declare FaceDetector
		ArrayList<String> faceDetectFields = new ArrayList<String>();
		faceDetectFields.add("tupleId");
		faceDetectFields.add("value");
		faceDetectFields.add("rows");
		faceDetectFields.add("cols");
		faceDetectFields.add("type");
		faceDetectFields.add("x");
		faceDetectFields.add("y");
		faceDetectFields.add("x2");
		faceDetectFields.add("y2");
		faceDetectFields.add("label");
		Connectable faceDetect1 = QueryBuilder.newStatelessOperator(new FaceDetector(), 0, faceDetectFields);
		Connectable faceDetect2 = QueryBuilder.newStatelessOperator(new FaceDetector(), 1, faceDetectFields);
		
		
		//Declare SpeechRecognizer
		ArrayList<String> faceRecFields = new ArrayList<String>();
		faceRecFields.add("tupleId");
		faceRecFields.add("value");
		faceRecFields.add("rows");
		faceRecFields.add("cols");
		faceRecFields.add("type");
		faceRecFields.add("x");
		faceRecFields.add("y");
		faceRecFields.add("x2");
		faceRecFields.add("y2");
		faceRecFields.add("label");
		Connectable faceRecJoin = QueryBuilder.newStatelessOperator(new SEEPFaceRecognizerJoin(), 2, faceRecFields);
		
		// Declare sink
		ArrayList<String> snkFields = new ArrayList<String>();
		snkFields.add("tupleId");
		snkFields.add("value");
		snkFields.add("rows");
		snkFields.add("cols");
		snkFields.add("type");
		snkFields.add("x");
		snkFields.add("y");
		snkFields.add("x2");
		snkFields.add("y2");
		snkFields.add("label");
		//Connectable snk = QueryBuilder.newStatelessSink(new Sink(), -2, snkFields);
		Connectable snk = QueryBuilder.newStatelessSink(new VideoSink(), -3, snkFields);
		
		src1.connectTo(faceDetect1, true, 0);
		src2.connectTo(faceDetect2, true, 1);
		faceDetect1.connectTo(faceRecJoin, InputDataIngestionMode.UPSTREAM_SYNC_BATCH_BUFFERED_BARRIER, true, 2);
		faceDetect2.connectTo(faceRecJoin, InputDataIngestionMode.UPSTREAM_SYNC_BATCH_BUFFERED_BARRIER, true, 3);
		faceRecJoin.connectTo(snk, true, 4);
		
		if (Boolean.parseBoolean(GLOBALS.valueFor("scaleOutSinks")))
		{
			int sinkScaleFactor = Integer.parseInt(GLOBALS.valueFor("sinkScaleFactor"));
			if (REPLICATION_FACTOR > 1 || sinkScaleFactor > 1)
			{
				QueryBuilder.scaleOut(snk.getOperatorId(), sinkScaleFactor > 0 ? sinkScaleFactor : REPLICATION_FACTOR);
			}
		}
		
		if (REPLICATION_FACTOR > 1)
		{
			QueryBuilder.scaleOut(faceDetect1.getOperatorId(), REPLICATION_FACTOR);
			QueryBuilder.scaleOut(faceDetect2.getOperatorId(), REPLICATION_FACTOR);
			QueryBuilder.scaleOut(faceRecJoin.getOperatorId(), REPLICATION_FACTOR);
		}

		return QueryBuilder.build();
	}
	private int[] computeJoinTree(int sources, int maxFanIn)
	{

		
		logger.debug("Composing join with "+sources+" sources and max fan-in "+maxFanIn);
		
		//Compute the number of levels needed in the join tree given the number
		//of sources and the max join fan in.
		int height = (int)Math.ceil(Math.log(sources) / Math.log(maxFanIn));
		logger.debug("Computed query height="+height);
		
		//Compute the number of '1st level' join ops.
		int[] levelOps = new int[height];
		levelOps[0] = sources / maxFanIn; 
		if (sources % maxFanIn > 0) { levelOps[0]++; }
		logger.debug("Number of ops at level 0="+levelOps[0]);
		
		//Compute the number of higher level join ops.
		for (int i = 1; i < height; i++)
		{
			levelOps[i] = levelOps[i-1] / maxFanIn;
			if (levelOps[i-1] % maxFanIn > 0) { levelOps[i]++; }
			logger.debug("Number of ops at level "+i+"="+levelOps[i]);
		}
		
		return levelOps;
	}
	
	private QueryPlan composeNameAssist()
	{
		throw new RuntimeException("Commented out due to temporary build failure.");
		/*
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
		*/
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
				pFields.add("latencyBreakdown");
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
