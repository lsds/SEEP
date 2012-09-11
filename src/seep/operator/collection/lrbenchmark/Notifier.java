package seep.operator.collection.lrbenchmark;

import java.util.HashMap;

import seep.comm.Dispatcher.DispatchPolicy;
import seep.comm.tuples.Seep;
import seep.comm.tuples.Seep.InitState;
import seep.comm.tuples.Seep.DataTuple.Builder;
import seep.operator.Operator;
import seep.operator.StatefullOperator;

@SuppressWarnings("serial")
public class Notifier extends Operator implements StatefullOperator{

	//stores per segment if there is an accident (true) or not
	private HashMap<Integer, Boolean> accidents = new HashMap<Integer, Boolean>();
	
	public Notifier(int opID) {
		super(opID);
		subclassOperator = this;
		for(int i = -5; i<105; i++){
			accidents.put(i, false);
		}
		setDispatchPolicy(DispatchPolicy.ALL);
	}

	@Override
	public void processData(Seep.DataTuple dt) {
		Seep.DataTuple.Builder event = Seep.DataTuple.newBuilder(dt);
		//toll notification message
		if(dt.getType() == 0){
			if(accidents.get(dt.getSeg()) || accidents.get(dt.getSeg()-1) || accidents.get(dt.getSeg()-2) || accidents.get(dt.getSeg()-3)|| accidents.get(dt.getSeg()-4)){
				// if there is an accident, there is no toll
				event.setToll(0);
				//send accident notification
				event.setType(1);
System.out.println("Segment: "+dt.getSeg()+" Accident near");
				sendDown(event.build());
			}
			
			event.setType(0);
			sendDown(event.build());
		}
		//accident notification message
		else if(dt.getType() == 1){
			//store accident in memory
			accidents.put(dt.getSeg(), true);
		}
		//accident gone notification message
		else if(dt.getType() == 5){
			accidents.put(dt.getSeg(), false);
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
	public void installState(InitState is) {
		// TODO Auto-generated method stub
		
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
