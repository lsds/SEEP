package uk.co.imperial.lsds.seep.reliable;

import java.io.Serializable;

import uk.co.imperial.lsds.seep.P;
import uk.co.imperial.lsds.seep.processingunit.IProcessingUnit;

/**
* ACKWorker. This runnable object is in charge of watching to the last processed tuple and generating an ACK when this has changed.
*/

public class ACKWorker implements Runnable, Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private IProcessingUnit processingUnit = null;
	private boolean goOn = true;
	private long memory = 0;

	public void stopACKWorker(){
		this.goOn = false;
	}
	
	public ACKWorker(IProcessingUnit processingUnit){
		this.processingUnit = processingUnit;
	}
	
	public void run(){
		int sleep = new Integer(P.valueFor("ackEmitInterval"));
		while(goOn){
			long currentTs = processingUnit.getLastACK();
			if(currentTs > memory){
				processingUnit.emitACK(currentTs);
//System.out.println("EMITTING ACK");
				memory = currentTs;
			}
			try{
				Thread.sleep(sleep);
			}
			catch(InterruptedException ie){
				System.out.println("ACKWorker: while trying to sleep "+ie.getMessage());
				ie.printStackTrace();
			}
		}
	}
}
