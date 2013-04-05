package seep.comm.serialization.messages;

import java.util.ArrayList;

public class BatchTuplePayload {

	public int batchSize = 0;
	public ArrayList<TuplePayload> batch = new ArrayList<TuplePayload>();
	
	public void addTuple(TuplePayload payload){
		batch.add(payload);
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
}
