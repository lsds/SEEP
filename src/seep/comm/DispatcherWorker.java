package seep.comm;

import java.io.Serializable;

import seep.P;

/// This class ticks the dispatcher every maxLatencyAllowed to update the clocks of the downstream channels

@SuppressWarnings("serial")
public class DispatcherWorker implements Serializable, Runnable{

	private Dispatcher dispatcher;
	
	public DispatcherWorker(Dispatcher dispatcher){
		this.dispatcher = dispatcher;
	}
	
	@Override
	public void run() {
		try{
			int value = Integer.parseInt(P.valueFor("maxLatencyAllowed"));
			Thread.sleep(value);
			dispatcher.batchTimeOut();
		}
		catch(InterruptedException ie){
			
			ie.printStackTrace();
		}
	}
}
