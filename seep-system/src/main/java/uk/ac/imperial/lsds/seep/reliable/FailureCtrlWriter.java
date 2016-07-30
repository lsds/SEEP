package uk.ac.imperial.lsds.seep.reliable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.FailureCtrl;
import uk.ac.imperial.lsds.seep.processingunit.IProcessingUnit;

public class FailureCtrlWriter implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(FailureCtrlWriter.class);
	private final IProcessingUnit processingUnit;
	private boolean goOn = true;

	public FailureCtrlWriter(IProcessingUnit processingUnit){
		this.processingUnit = processingUnit;
	}
	
	public void run(){
		int sleep = new Integer(GLOBALS.valueFor("fctrlEmitInterval"));
		while(goOn){
			
			FailureCtrl fctrl = processingUnit.getDispatcher().getCombinedDownFailureCtrl();
			boolean downstreamsRoutable = processingUnit.getDispatcher().areDownstreamsRoutable();
			
			/*
			TimestampTracker currentTsV = processingUnit.getLastACK();
			System.out.println("ACKWorker: EmitACK");
			processingUnit.emitACK(currentTsV);
			*/
			processingUnit.emitFailureCtrl(fctrl,  downstreamsRoutable);
			logger.debug("Writing failure ctrl: "+ fctrl.toString()+",downstreamsRoutable="+downstreamsRoutable);
			
			try{
				Thread.sleep(sleep);
			}
			catch(InterruptedException ie){
				System.out.println("ACKWorker: while trying to sleep "+ie.getMessage());
				ie.printStackTrace();
			}
		}
	}
	
	public void stop(){
		this.goOn = false;
	}
}
