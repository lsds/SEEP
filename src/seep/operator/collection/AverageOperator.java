package seep.operator.collection;

import seep.Main;
import seep.comm.tuples.*;
import seep.comm.tuples.Seep.Ack;
import seep.operator.*;
import seep.utils.ExecutionConfiguration;

public class AverageOperator extends Operator implements StatefullOperator{

	private int windowSize;

	private int cumulativeState;
	private int counter;

	//test purposes
	int c = 0;
	int n_events = 100000;
	long t_start = 0;
	long t_end = 0;
	//finish test purposes

	public AverageOperator(int opID, int windowSize){
		super(opID);
		this.windowSize = windowSize;
		counter = windowSize;
		//This line is to give this reference to the Operator
		subclassOperator = this;
	}

	public void processData(Seep.DataTuple dt){
//System.out.println("AVGOP: received tuple: "+dt.getInt()+" ts: "+dt.getTs());
		Seep.DataTuple.Builder event = Seep.DataTuple.newBuilder(dt);
//		int value = dt.getInt();
//		cumulativeState += value;
		counter--;
		if(counter == 50){
			if(Main.valueFor("ftmodel").equals("newModel")){
				Seep.AverageState.Builder asB = Seep.AverageState.newBuilder();
				asB.setState(cumulativeState);
				asB.setCounter(counter);
				Seep.BackupState.Builder bsB = Seep.BackupState.newBuilder();
				//Developer needs to save just the state
				bsB.setState(asB.build());
//System.out.println("AVGOP: Checkpointing state with cumulativeS: "+cumulativeState+" and counter: "+counter);
				//backupState(bsB, this);
				/*This method is called each time the developer decides to checkpoint the state. A more powerful API should be offered, for instance able to support timers, and configured initially or whatever... For now, this explicitly does a backup of the state, and in this specific example, the developer chooses to checkpoint each time data is processed in this operator*/
//System.out.println("AVGOP: Backuping state. cumulState-> "+cumulativeState+" counter-> "+counter);
				backupState(bsB);
			}
		}
		if(counter == 0){
//			value = cumulativeState/windowSize;
			//System.out.println("AVGOP: Sending -> "+value);
			//System.out.println();
//			event.setInt(value);
System.out.println("AVGOP-> SENDING to SINK");
			sendDown(event.build());
			counter = windowSize;
			cumulativeState = 0;
		}


		c++;
		if(c == n_events){
			t_end = System.currentTimeMillis();
			System.out.println("AVG: "+(t_end-t_start)+" ms for "+n_events+" events");
			t_start = System.currentTimeMillis();
			c = 0;
		}

	}

	//FIXME this method should no longer be sync
	public synchronized void installState(Seep.InitState is){
		if (is == null) {
			cumulativeState = 0;
			counter = windowSize;
		}
		if(Main.valueFor("ftmodel").equals("twitterStormModel")){
			//Extract the specific state to this operator
			Seep.AverageState as = is.getState();
			cumulativeState = as.getState();
			counter = as.getCounter();
System.out.println("AVGOP: has restored state. cumulState-> "+cumulativeState+" counter-> "+counter);
		}
	}

	@Override
	public void generateBackupState() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getCounter() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getBackupTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isOrderSensitive() {
		// TODO Auto-generated method stub
		return false;
	}
}
