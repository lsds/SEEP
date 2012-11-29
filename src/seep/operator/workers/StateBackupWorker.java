package seep.operator.workers;

import java.io.Serializable;

import seep.Main;
import seep.P;
import seep.operator.Operator;
import seep.operator.StatefulOperator;

/**
* StateBackupWorker. This class is in charge of checking when the associated operator has a state to do backup and doing the backup of such state. This is operator dependant.
*/

public class StateBackupWorker implements Runnable, Serializable{

	private static final long serialVersionUID = 1L;
	
	private StatefulOperator o;
	/// \todo {get the threshold value in a proper way}
	private long threshold = 0;
	private long initTime = 0;

	public StateBackupWorker(StatefulOperator o){
		this.o = o;
		this.threshold = o.getBackupTime();
	}
	
	public void run(){
		initTime = System.currentTimeMillis();
		o.generateBackupState();
//System.out.println("TH: "+threshold+" initTime: "+initTime);
		while(true){
			long elapsedTime = System.currentTimeMillis() - initTime;
//			System.out.println("elapsedTime -> "+elapsedTime+" thresdhold -> "+threshold);
			if(elapsedTime > threshold){
				//synch this call
//	System.out.println("##### BACKUP");
long a = System.currentTimeMillis();
//				if(Main.eftMechanismEnabled){
				if(P.valueFor("eftMechanismEnabled").equals("true")){
					//if not initialisin state...
					if(!((Operator)o).getOperatorStatus().equals(Operator.OperatorStatus.INITIALISING_STATE)){
						synchronized(o){
							o.generateBackupState();
						}
					}
				}
long b = System.currentTimeMillis() -a;
System.out.println("*generate_backup_state: "+b);
				initTime = System.currentTimeMillis();
			}
			else{
				try {
					int sleep = (int) (threshold - (System.currentTimeMillis() - initTime));
					if(sleep > 5){
//						System.out.println("Sleeping "+sleep+" ms.");
						Thread.sleep(sleep);
					}
				}
				catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
