package seep.processingunit;

import java.io.Serializable;
import java.util.Map;

import seep.P;
import seep.operator.State;

/**
* StateBackupWorker. This class is in charge of checking when the associated operator has a state to do backup and doing the backup of such state. This is operator dependant.
*/

public class StateBackupWorker implements Runnable, Serializable{

	private static final long serialVersionUID = 1L;
	
	private long initTime = 0;
	
	private ProcessingUnit processingUnit;
	private boolean goOn = true;
	private int checkpointInterval = 0;
	private Map<Integer, State> mapOP_S;
	
	public void stop(){
		this.goOn = false;
	}

	public StateBackupWorker(ProcessingUnit processingUnit, Map<Integer, State> mapOPS, int checkpointInterval){
		this.processingUnit = processingUnit;
		this.mapOP_S = mapOPS;
		this.checkpointInterval = checkpointInterval;
	}
	
	public void run(){
		initTime = System.currentTimeMillis();
		processingUnit.checkpointAndBackupState();
//		o.generateBackupState();
		while(goOn){
			long elapsedTime = System.currentTimeMillis() - initTime;
			if(elapsedTime > checkpointInterval){
				//synch this call
				if(P.valueFor("eftMechanismEnabled").equals("true")){
					//if not initialisin state...
					if(!processingUnit.getSystemStatus().equals(ProcessingUnit.SystemStatus.INITIALISING_STATE)){
//						synchronized(o){
						processingUnit.checkpointAndBackupState();
//							o.generateBackupState();
//						}
					}
				}
				initTime = System.currentTimeMillis();
			}
			else{
				try {
					int sleep = (int) (checkpointInterval - (System.currentTimeMillis() - initTime));
					if(sleep > 5){
						Thread.sleep(sleep);
					}
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
