package uk.ac.imperial.lsds.seepworker.core;

import java.util.Iterator;
import java.util.List;

import uk.ac.imperial.lsds.seep.api.API;
import uk.ac.imperial.lsds.seep.api.SeepState;
import uk.ac.imperial.lsds.seep.api.SeepTask;
import uk.ac.imperial.lsds.seep.api.data.ITuple;
import uk.ac.imperial.lsds.seepworker.core.input.CoreInput;
import uk.ac.imperial.lsds.seepworker.core.input.InputAdapter;
import uk.ac.imperial.lsds.seepworker.core.input.InputAdapterReturnType;
import uk.ac.imperial.lsds.seepworker.core.output.CoreOutput;

public class SingleThreadProcessingEngine implements ProcessingEngine {

	private boolean working = false;
	private Thread worker;
	
	private CoreInput coreInput;
	private CoreOutput coreOutput;
	
	private SeepTask task;
	private SeepState state;
	
	public SingleThreadProcessingEngine(){
		this.worker = new Thread(new Worker());
		this.worker.setName(this.getClass().getName());
	}
	
	@Override
	public void setCoreInput(CoreInput coreInput) {
		this.coreInput = coreInput;
	}

	@Override
	public void setCoreOutput(CoreOutput coreOutput) {
		this.coreOutput = coreOutput;
	}
	
	@Override
	public void setTask(SeepTask task) {
		this.task = task;
		
	}
	
	@Override
	public void setSeepState(SeepState state) {
		this.state = state;
	}

	@Override
	public void start() {
		working = true;
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}
	
	private class Worker implements Runnable{

		@Override
		public void run() {
			List<InputAdapter> inputAdapters = coreInput.getInputAdapters();
			Iterator<InputAdapter> it = inputAdapters.iterator();
			short one = InputAdapterReturnType.ONE.ofType();
			short many = InputAdapterReturnType.MANY.ofType();
			API api = new Collector(coreOutput.getOutputAdapters());
			while(working){
				while(it.hasNext()){
					InputAdapter ia = it.next();
					if(ia.rType() == one){
						ITuple d = ia.pullDataItem();
						task.processData(d, api);
					}
					else if(ia.rType() == many){
						ITuple ld = ia.pullDataItems();
						task.processDataGroup(ld, api);
					}
					if(!it.hasNext()){
						it = inputAdapters.iterator();
					}
				}
			}
		}
	}
}
