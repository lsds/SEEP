package seep.operator.collection.financialQuery;

import java.util.HashMap;

import seep.comm.routing.Router;
import seep.comm.serialization.DataTuple;
import seep.infrastructure.monitor.MetricsReader;
import seep.operator.Operator;
import seep.operator.StatelessOperator;

public class Splitter extends Operator implements StatelessOperator{

	private static final long serialVersionUID = 1L;
	
	private HashMap<String, Integer> mapping = new HashMap<String, Integer>();

	
	/** TIME CONTROL**/
	int counter = 0;
	long t_start = 0;
	long i_time = 0;
	boolean first = true;
	int avgCounter = 0;
	double avg = 0;
	int sec = 0;
	
	public Splitter(int opID) {
		super(opID);
		subclassOperator = this;
		mapping.put("A", 1);
		mapping.put("B", 2);
		mapping.put("C", 3);
		mapping.put("D", 4);
		mapping.put("E", 5);
		mapping.put("F", 6);
		mapping.put("G", 7);
		mapping.put("H", 8);
		mapping.put("I", 9);
		mapping.put("J", 10);
		mapping.put("K", 11);
		mapping.put("L", 12);
		mapping.put("M", 13);
		mapping.put("N", 14);
		mapping.put("O", 15);
		mapping.put("P", 16);
		mapping.put("Q", 17);
		mapping.put("R", 18);
		mapping.put("S", 19);
		mapping.put("T", 20);
		mapping.put("U", 21);
		mapping.put("V", 22);
		mapping.put("W", 23);
		mapping.put("X", 24);
	}

	@Override
	public boolean isOrderSensitive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void processData(DataTuple dt) {
		if(first){
			t_start = System.currentTimeMillis();
			first = false;
		}
		
		String keyString = null;
		String strike = Double.toString(dt.getStrikePrice());
		String eDay = Integer.toString(dt.getExpiryDay());
		String eYear = Integer.toString(dt.getExpiryYear());
		keyString = strike+eDay+eYear;
		int key = Router.customHash(keyString);
		dt.setKey(key);
		int mKey = mapping.get(dt.getMonth());
		dt.setMonthCode(mKey);
		sendDown(dt, mKey);
		
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

}
