package seep.operator.collection.mapreduceexample;

import java.util.HashMap;
import java.util.LinkedList;

import seep.comm.serialization.DataTuple;
import seep.comm.serialization.controlhelpers.InitState;
import seep.operator.Operator;
import seep.operator.StatefulOperator;

public class Reduce extends Operator implements StatefulOperator{

	private static final long serialVersionUID = 1L;
	
	/** VAR BUSINESS LOGIC **/
	//HashMap storing the 5 most visited country codes
	private LinkedList<TopPosition> top = new LinkedList<TopPosition>();
	// integer storing the number of visits of the fifth member in top map
	private int top5Visits = 0;
	//map storing the number of visits per country code
	private HashMap<String, Integer> countryCode = new HashMap<String, Integer>();
	int batchSize = 0;
	
	/** VAR TIME CONTROL **/
	boolean first = true;
	long t_start = 0;
	long i_time = 0;
	
	public Reduce(int opId){
		super(opId);
		subclassOperator = this;
	}
	
	class TopPosition{
		public String countryCode = null;
		public int visits = 0;
		
		public TopPosition(String countryCode, int visits){
			this.countryCode = countryCode;
			this.visits = visits;
		}
	}
	
	private void reviewTop5(String key, int totalVisits){
		for(int i = 0; i<top.size(); i++){
			if(totalVisits > top.get(i).visits){
				TopPosition tp = new TopPosition(key, totalVisits);
				top.add(i, tp);
				top.remove(5);
				top5Visits = top.get(4).visits;
			}
		}
	}
	
	@Override
	public void processData(DataTuple dt) {
		if(first){
			t_start = System.currentTimeMillis();
			first = false;
		}
		
		/** OPERATOR BUSINESS LOGIC **/
		String key = dt.getCountryCode();
		int totalVisits = 0;
		if(countryCode.containsKey(key)){
			totalVisits = countryCode.get(key) + 1;
			countryCode.put(key, totalVisits);
		}
		else{
			countryCode.put(key, 1);
		}
		if(totalVisits > top5Visits){
			reviewTop5(key, totalVisits);
		}
		
		/** TIME WINDOW CONTROL**/
		i_time = System.currentTimeMillis();
		long currentTime = i_time - t_start;
		if(currentTime >= 30000){
			t_start = System.currentTimeMillis();
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
	public void installState(InitState is) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean isOrderSensitive() {
		// TODO Auto-generated method stub
		return false;
	}
}
