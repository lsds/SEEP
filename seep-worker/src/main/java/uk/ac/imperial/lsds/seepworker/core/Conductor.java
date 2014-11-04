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
	
	private SeepTask task;
	private SeepState state;
	
	public Conductor(WorkerConfig wc){
		
	}
	
	public void deployPhysicalOperator(PhysicalOperator o){
		// TODO: configure, at the very least, input and output for this operator
		// TODO: also set up state and the right mechanisms to keep this stuff tidy
		coreInput = CoreInputFactory.buildCoreInputForOperator(o);
		coreOutput = CoreOutputFactory.buildCoreOutputForOperator(o);
		
	}
	
	public void plugSeepTask(SeepTask task){
		// TODO: plug and play
	}
	
}
