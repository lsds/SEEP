package seep.runtimeengine;

import seep.comm.serialization.DataTuple;

public class DataConsumer implements Runnable {

	private CoreRE owner;
	private InputQueue inputQueue;
	private boolean doWork = true;
	private boolean block = false;
	
	public void setDoWork(boolean doWork){
		this.doWork = doWork;
	}
	
	public DataConsumer(CoreRE owner, InputQueue inputQueue){
		this.owner = owner;
		this.inputQueue = inputQueue;
	}

	@Override
	public void run() {
		while(doWork){
			DataTuple data = inputQueue.pull();
			if(owner.checkSystemStatus()){
				owner.forwardData(data);
			}
			else{
				System.out.println("Trash in DATA CONSUMER");
			}
		}
	}
	
	/** RUNTIME CONTROL METHODS **/
	
}
