package seep.operator.collection.mapreduceexample;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;

import seep.comm.serialization.DataTuple;
import seep.comm.serialization.controlhelpers.InitState;
import seep.infrastructure.monitor.MetricsReader;
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
	
	/** VAR TIME CONTROL BUSINESS LOGIC **/
	boolean first = true;
	long t_start = 0;
	long i_time = 0;
	
	/** TIME CONTROL **/
	long t_start2 = 0;
	long i_time2 = 0;
	private int counter = 0;
	
	public Reduce(int opId){
		super(opId);
		subclassOperator = this;
		
		for(int i = 0; i<5; i++){
			top.add(i, new TopPosition("trash", 0));
		}
	}
	
	class TopPosition implements Serializable{
		
		private static final long serialVersionUID = 1L;
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
				if(top.get(i).countryCode.equals(key)){
					top.remove(i);
					top.add(i, tp);
				}
				else{
					top.add(i, tp);
					top.remove(5);
				}
				top5Visits = top.get(4).visits;
				break;
			}
		}
	}
	
	@Override
	public void processData(DataTuple dt) {
		if(first){
			t_start = System.currentTimeMillis();
			t_start2 = t_start;
			first = false;
		}
//		System.out.println("RX-> K: "+dt.getCountryCode()+" V: "+dt.getMRValue());
		counter++;
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
			dt.setTop5(top.get(0).countryCode, top.get(0).visits, top.get(1).countryCode, top.get(1).visits, top.get(2).countryCode, 
					top.get(2).visits, top.get(3).countryCode, top.get(3).visits, top.get(4).countryCode, top.get(4).visits);
			System.out.println("Sent top5");
			System.out.println("size of codes: "+dt.getTopCCode().size());
			System.out.println("size of visits: "+dt.getTopVisits().size());
			sendDown(dt);
			t_start = System.currentTimeMillis();
		}
		
		/**TIME CONTROL**/
		i_time2 = System.currentTimeMillis();
		long currentTime2 = i_time2 - t_start2;
		if(currentTime2 >= 1000){
			System.out.println("E/S: "+counter);
			System.out.println("INPUTQ-counter: "+MetricsReader.eventsInputQueue.getCount());
			t_start2 = System.currentTimeMillis();
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
	public void installState(InitState is) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean isOrderSensitive() {
		// TODO Auto-generated method stub
		return false;
	}
}
