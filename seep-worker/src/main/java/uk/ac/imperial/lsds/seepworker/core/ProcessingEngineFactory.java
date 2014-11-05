package uk.ac.imperial.lsds.seepworker.core;

public class ProcessingEngineFactory {

	public static ProcessingEngine buildProcessingEngine(int type){
		if(type == ProcessingEngineType.SINGLE_THREAD.ofType()){
			return new SingleThreadProcessingEngine();
		}
		return null;
	}
	
}
