package seep.runtimeengine;

import java.util.ArrayList;

public class LocalProfiler {

	private long init;
	private int iterations = 0;
	private int windowSize = 10000; 
	private long aggregatedOnWindow = 0;
	private ArrayList<Integer> memory = new ArrayList<Integer>();
	
	public void start(){
		init = System.currentTimeMillis();
	}
	
	public void finish(){
		iterations++;
		aggregatedOnWindow += System.currentTimeMillis() - init;
		init = 0;
		if(iterations == windowSize){
			iterations = 0;
			int avg = (int)(aggregatedOnWindow/windowSize);
			memory.add(avg);
		}
	}
	
}
