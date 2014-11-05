package uk.ac.imperial.lsds.seepworker.core;

import uk.ac.imperial.lsds.seep.api.SeepState;
import uk.ac.imperial.lsds.seep.api.SeepTask;
import uk.ac.imperial.lsds.seepworker.core.input.CoreInput;
import uk.ac.imperial.lsds.seepworker.core.output.CoreOutput;

public interface ProcessingEngine {

	public void setCoreInput(CoreInput coreInput);
	public void setCoreOutput(CoreOutput coreOutput);
	public void setTask(SeepTask task);
	public void setSeepState(SeepState state);
	
	public void start();
	public void stop();

}
