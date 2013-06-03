package seep.runtimeengine.workers;

import java.io.Serializable;

import seep.P;
import seep.operator.State;
import seep.processingunit.StatefulProcessingUnit;

/**
* StateBackupWorker. This class is in charge of checking when the associated operator has a state to do backup and doing the backup of such state. This is operator dependant.
*/

public class StateBackupWorker implements Runnable, Serializable{

	private static final long serialVersionUID = 1L;
	
	private long initTime = 0;
	
	private StatefulProcessingUnit processingUnit;
	private boolean goOn = true;
	private int checkpointInterval = 0;
	private State state;
	
	public void stop(){
		this.goOn = false;
	}

	public StateBackupWorker(StatefulProcessingUnit processingUnit, State s){
		this.processingUnit = processingUnit;
		this.state = s;
	}
	
	public void run(){
		initTime = System.currentTimeMillis();
		try {
			Thread.sleep(2000);
		}
		catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		processingUnit.checkpointAndBackupState();
		checkpointInterval = state.getCheckpointInterval();
		while(goOn){
			long elapsedTime = System.currentTimeMillis() - initTime;
			if(elapsedTime > checkpointInterval){
				//synch this call
				if(P.valueFor("eftMechanismEnabled").equals("true")){
					//if not initialisin state...
					if(!processingUnit.getSystemStatus().equals(StatefulProcessingUnit.SystemStatus.INITIALISING_STATE)){
						long startCheckpoint = System.currentTimeMillis();
//						processingUnit.checkpointAndBackupState();
						processingUnit.directCheckpointAndBackupState();
//						processingUnit.directParallelCheckpointAndBackupState();
//						processingUnit.blindCheckpointAndBackupState();
						long stopCheckpoint = System.currentTimeMillis();
						System.out.println("%% Total Checkpoint: "+(stopCheckpoint-startCheckpoint));
					}
				}
				initTime = System.currentTimeMillis();
			}
			else{
				try {
					int sleep = (int) (checkpointInterval - (System.currentTimeMillis() - initTime));
					if(sleep > 0){
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
