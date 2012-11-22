package seep.operator.collection.financialQuery;

import java.util.ArrayList;
import java.util.HashMap;

import seep.comm.serialization.DataTuple;
import seep.comm.serialization.controlhelpers.StateI;
import seep.infrastructure.monitor.MetricsReader;
import seep.operator.Operator;
import seep.operator.StatefulOperator;

public class ParityChecker extends Operator implements StatefulOperator{

	private static final long serialVersionUID = 1L;

	private HashMap<Integer, ArrayList<Value>> memory = new HashMap<Integer, ArrayList<Value>>();
	
	/** TIME CONTROL**/
	int counter = 0;
	long t_start = 0;
	long i_time = 0;
	boolean first = true;
	int avgCounter = 0;
	double avg = 0;
	int sec = 0;
	
	public ParityChecker(int opID) {
		super(opID);
		subclassOperator = this;
	}

	@Override
	public boolean isOrderSensitive() {
		// TODO Auto-generated method stub
		return false;
	}

	private Value checkParity(int key, Value a, ArrayList<Value> b){
		//for(Value v : b){
		for(int i = 0; i<b.size(); i++){
			Value v = b.get(i);
			//parity
			if(a.exId.equals(v.exId) && Math.abs(a.monthCode - v.monthCode) == 12){
				return v;
			}
			//tuple overlap
			else if (a.exId.equals(v.exId) && Math.abs(a.monthCode - v.monthCode) < 12){
				//get rid of old tuple
				b.remove(i);
				//update the map
				memory.put(key, b);
			}
		}
		return null;
	}
	
	@Override
	public void processData(DataTuple dt) {
		if(first){
			t_start = System.currentTimeMillis();
			first = false;
		}
		
		int k = dt.getKey();
		
		Value n = new Value(dt.getExchangeId(), dt.getMonthCode());
		if(!memory.containsKey(k)){
			ArrayList<Value> aux = new ArrayList<Value>();
			aux.add(n);
			memory.put(k, aux);
		}
		else{
			Value par = checkParity(k, n, memory.get(k));
			if(par != null){
				// This exchange id
				dt.setxParity(par.exId);
				sendDown(dt);
			}
		}
		
		
		/**TIME CONTROL**/
		counter++;
		i_time = System.currentTimeMillis();
		long currentTime = i_time - t_start;
		if(currentTime >= 1000){
			sec++;
			System.out.println("E/S: "+counter);
			System.out.println("INPUTQ-counter: "+MetricsReader.eventsInputQueue.getCount());
			t_start = System.currentTimeMillis();
			counter = 0;
		}
	}

	@Override
	public void generateBackupState() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long getBackupTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getCounter() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void installState(StateI is) {
		// TODO Auto-generated method stub
		
	}

}
