package uk.co.imperial.lsds.seep.comm.serialization.messages;

import java.util.ArrayList;

public class BatchTuplePayload {

	public int batchSize = 0;
	public ArrayList<TuplePayload> batch = new ArrayList<TuplePayload>();
	
	public synchronized void addTuple(TuplePayload payload){
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
	
	public int size(){
		return batch.size();
	}
}
