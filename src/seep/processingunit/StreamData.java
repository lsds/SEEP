package seep.processingunit;

import java.util.ArrayList;

import seep.operator.State;

public class StreamData extends State{
	
	private static final long serialVersionUID = 1L;
	private ArrayList<Object> microBatch;
			
	public StreamData(){}
	
	public StreamData(ArrayList<Object> mb){
		this.microBatch = mb;
	}
}