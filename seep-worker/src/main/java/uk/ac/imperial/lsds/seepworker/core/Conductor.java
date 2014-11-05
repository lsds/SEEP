package uk.ac.imperial.lsds.seepworker.core;

import uk.ac.imperial.lsds.seep.api.PhysicalOperator;
import uk.ac.imperial.lsds.seep.api.SeepState;
import uk.ac.imperial.lsds.seep.api.SeepTask;
import uk.ac.imperial.lsds.seepworker.WorkerConfig;
import uk.ac.imperial.lsds.seepworker.core.input.CoreInput;
import uk.ac.imperial.lsds.seepworker.core.input.CoreInputFactory;
import uk.ac.imperial.lsds.seepworker.core.output.CoreOutput;
import uk.ac.imperial.lsds.seepworker.core.output.CoreOutputFactory;

public class Conductor {

	private CoreInput coreInput;
	private CoreOutput coreOutput;
	private ProcessingEngine engine;
	
	private SeepTask task;
	private SeepState state;
	
	public Conductor(WorkerConfig wc){
		int engineType = wc.getInt(WorkerConfig.ENGINE_TYPE);
		engine = ProcessingEngineFactory.buildProcessingEngine(engineType);
		// Use config to get all parameters that configure input, output and engine
		// TODO:
	}
	
	public void start(){
		engine.start();
	}
	
	public void stop(){
		engine.stop();
	}
	
	public void deployPhysicalOperator(PhysicalOperator o){
		coreInput = CoreInputFactory.buildCoreInputForOperator(o);
		coreOutput = CoreOutputFactory.buildCoreOutputForOperator(o);
		
		engine.setTask(task);
		engine.setSeepState(state);
		engine.setCoreInput(coreInput);
		engine.setCoreOutput(coreOutput);
	}
	
	public void plugSeepTask(SeepTask task){
		// TODO: plug and play. this will do stuff with input and output and then delegate the call to engine
		// this pattern should be the default in this conductor controller
	}
	
}
