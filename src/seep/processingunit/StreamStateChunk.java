package seep.processingunit;

import java.util.ArrayList;

import seep.operator.State;

public class StreamStateChunk extends State{

	private static final long serialVersionUID = 1L;
	private ArrayList<Object> microBatch;
	
	public StreamStateChunk(){
	}
	
	public ArrayList<Object> getMicroBatch(){
		return microBatch;
	}
	
	public StreamStateChunk(ArrayList<Object> microBatch){
		this.microBatch = microBatch;
	}

}
