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
package uk.ac.imperial.lsds.seep.comm.serialization.messages;

import java.util.ArrayList;
import java.util.Iterator;

import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.FailureCtrl;
import uk.ac.imperial.lsds.seep.comm.serialization.ControlTuple;


public class BatchTuplePayload {

	public int batchSize = 0;
	public ArrayList<TuplePayload> batch = new ArrayList<TuplePayload>();
	public long outputTs = -1;
	public Integer rctrl = null;
	public ControlTuple fctrl = null;
	
	public synchronized void addTuple(TuplePayload payload){
		outputTs = payload.timestamp; //update the newest ts in the batch
		batch.add(payload);
		batchSize++;
	}
	
	public void clear(){
		batch = new ArrayList<TuplePayload>();
		batchSize = 0;
	}
	
	public TuplePayload getTuple(int index){
		return batch.get(index);
	}
	
	public BatchTuplePayload(){
		
	}
	
	public synchronized int size(){
		//TODO: dokeeffe - should this not be batchSize? is it thread safe currently?
		return batch.size();
	}
	
	public synchronized void trim(FailureCtrl otherFctrl)
	{
		//TODO: Is this even thread safe wrt output sending?
		Iterator<TuplePayload> iter = batch.iterator();
		long newOutputTs = -1;
		while (iter.hasNext())
		{
			long tupleTs = iter.next().timestamp;
			// Don't remove based on alives here - we're using this log for replay.
			if (tupleTs <= otherFctrl.lw() || otherFctrl.acks().contains(tupleTs) /*|| otherFctrl.alives().contains(tupleTs)*/)
			{
				iter.remove();
				batchSize--;
			}
			else
			{
				newOutputTs = Math.max(newOutputTs, tupleTs);
			}
		}
		outputTs = newOutputTs;	//TODO: This will probably mess up the existing acking relationships.
	}

	public synchronized boolean containsAcked(FailureCtrl otherFctrl)
	{
		//TODO: Is this even thread safe wrt output sending?
		Iterator<TuplePayload> iter = batch.iterator();
		while (iter.hasNext())
		{
			long tupleTs = iter.next().timestamp;
			// Don't remove based on alives here - we're using this log for replay.
			if (tupleTs <= otherFctrl.lw() || otherFctrl.acks().contains(tupleTs) /*|| otherFctrl.alives().contains(tupleTs)*/)
			{
				return true;	
			}
		}
		return false;
	}
}
