/*******************************************************************************
 * Copyright (c) 2013 Imperial College London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial design and implementation
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.runtimeengine;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.operator.InputDataIngestionMode;
import uk.ac.imperial.lsds.seep.operator.Operator;
import uk.ac.imperial.lsds.seep.operator.OperatorContext;

public class DataStructureAdapter {
	
	final private Logger LOG = LoggerFactory.getLogger(DataStructureAdapter.class);
	
	private Map<Integer, DataStructureI> dsoMap = new HashMap<Integer, DataStructureI>();
	private DataStructureI uniqueDso = null;
	
	public DataStructureAdapter(){
		
	}
	
	public DataStructureI getUniqueDso(){
		return uniqueDso;
	}
	
	public DataStructureI getDataStructureIForOp(int opId){
		if(dsoMap.containsKey(opId)){
			return dsoMap.get(opId);
		}
		else{
			LOG.error("-> ERROR. No adapter for given opId, not possible to forward data to operator.");
			return null;
		}
	}
	
	public int getNumberOfModes(){
		return dsoMap.size();
	}
	
	public Map<Integer, DataStructureI> getInputDataIngestionModeMap(){
		return dsoMap;
	}
	
	public void setDSOForOp(int opId, DataStructureI dso){
		dsoMap.put(opId, dso);
	}
	
//	public void setUp(Map<Integer, InputDataIngestionMode> iimMap, int numUpstreams){
	public void setUp(Map<Integer, InputDataIngestionMode> iimMap, OperatorContext opContext){
		// Differentiate between cases with only one inputdatamode and more than one (for performance reasons)
		if(iimMap.size() > 1){
			LOG.debug("-> Setting up multiple inputDataIngestionModes");
			// For processing one event per iteration, the queue is the best abstraction
			for(Entry<Integer, InputDataIngestionMode> entry : iimMap.entrySet()){
				if(entry.getValue().equals(InputDataIngestionMode.ONE_AT_A_TIME)){
					InputQueue iq = new InputQueue();
					dsoMap.put(entry.getKey(), iq);
					LOG.debug("-> Ingest with InputQueue from {}", entry.getKey());
				}
				else if(entry.getValue().equals(InputDataIngestionMode.UPSTREAM_SYNC_BARRIER)){
					///\fixme{careful with the num of upstreams. its the upstreams on the barriera, not all}
					int originalOperatorOnBarrier = entry.getKey();
					int numberUpstreamsOnBarrier = opContext.getUpstreamNumberOfType(originalOperatorOnBarrier);
                                        LOG.debug("-> ^^^^^ numberUpstreamsOnBarrier {}", numberUpstreamsOnBarrier);
					Barrier b = new Barrier(numberUpstreamsOnBarrier);
					dsoMap.put(entry.getKey(), b);
					LOG.debug("-> Ingest with Sync-Barrier from {}", entry.getKey());
				}
			}
		}
		else if(iimMap.size() == 1){
			LOG.debug("-> Setting up a unique InputDataIngestionMode");
			for(Entry<Integer, InputDataIngestionMode> entry : iimMap.entrySet()){
				if(entry.getValue().equals(InputDataIngestionMode.ONE_AT_A_TIME)){
					InputQueue iq = new InputQueue();
					uniqueDso = iq;
					LOG.debug("-> Ingest with InputQueue from {}", entry.getKey());
				}
				else if(entry.getValue().equals(InputDataIngestionMode.UPSTREAM_SYNC_BARRIER)){
					///\fixme{careful with the num of upstreams. its the upstreams on the barriera, not all. In this case is the same}
					int originalOperatorOnBarrier = entry.getKey();
					int numberUpstreamsOnBarrier = opContext.getUpstreamNumberOfType(originalOperatorOnBarrier);
//					System.out.println("Num registers on barrier: "+numberUpstreamsOnBarrier);
					///\fixme{manage this}
					numberUpstreamsOnBarrier = opContext.upstreams.size();
					Barrier b = new Barrier(numberUpstreamsOnBarrier);
					uniqueDso = b;
					LOG.debug("-> Ingest with Sync-Barrier from {}", entry.getKey());
				}
			}
		}
	}
	
	/** SPECIFIC METHODS **/
	
	public void reconfigureNumUpstream(int originalOpId, int upstreamSize){
		// Number of upstream has changed, this affects the barrier
		System.out.println("NEW UPSTREAM SIZE: "+upstreamSize);
		DataStructureI barrier = dsoMap.get(originalOpId);
		if(barrier instanceof Barrier){
			System.out.println("Calling to reconfigure barrier");
			((Barrier)barrier).reconfigureBarrier(upstreamSize);
		}
	}
}
