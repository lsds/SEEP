package seep.comm.serialization;

import java.util.ArrayList;

public class BatchDataTuple {
	
	private ArrayList<DataTuple> batch = new ArrayList<DataTuple>();
	private int batchSize = 0;
	
	public void addTuple(DataTuple data){
		batch.add(data);
		batchSize++;
	}
	
	public void clear(){
		batch = new ArrayList<DataTuple>();
		batchSize = 0;
	}
	
	public BatchDataTuple getBatch(){
		return this;
	}
	
	public int getBatchSize(){
		return batchSize;
	}
	
	public DataTuple getTuple(int index){
		return batch.get(index);
	}
	
	public ArrayList<DataTuple> getTuples(){
		return batch;
	}
	
	public BatchDataTuple(){
		
	}
}
