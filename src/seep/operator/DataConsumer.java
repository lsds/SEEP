package seep.operator;

import seep.comm.serialization.DataTuple;

public class DataConsumer implements Runnable {

	private Operator owner;
	private InputQueue inputQueue;
	private boolean doWork = true;
	
	public void setDoWork(boolean doWork){
		this.doWork = doWork;
	}
	
	public DataConsumer(Operator owner, InputQueue inputQueue){
		this.owner = owner;
		this.inputQueue = inputQueue;
	}

	@Override
	public void run() {
		while(doWork){
//			System.out.println("CONSUMER->going to pull data");
			DataTuple data = inputQueue.pull();
//			System.out.println("CONSUMER->Data pulled");
			owner.processData(data);
		}
	}
}
