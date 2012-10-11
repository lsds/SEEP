package seep.operator.collection.mapreduceexample;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

import seep.comm.serialization.DataTuple;
import seep.operator.Operator;
import seep.operator.StatelessOperator;
import seep.operator.collection.mapreduceexample.Reduce.TopPosition;


public class Sink extends Operator implements StatelessOperator{

	private static final long serialVersionUID = 1L;

	//HashMap storing the 5 most visited country codes
	private LinkedList<TopPosition> top = new LinkedList<TopPosition>();
	
	// integer storing the number of visits of the fifth member in top map
	private int top5Visits = 0;
	
	int rx = 0;
	
	public Sink(int opId){
		super(opId);
		subclassOperator = this;
		for(int i = 0; i<5; i++){
			top.add(i, new TopPosition("thrash", 0));
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
		rx++;
		ArrayList<Integer> visits = dt.getTopVisits();
		ArrayList<String> codes = dt.getTopCCode();
		for(int i = 0; i< codes.size(); i++){
			int totalVisits = visits.get(i);
			System.out.println("new: "+totalVisits+" old: "+top5Visits);
			if(totalVisits > top5Visits){
				String key = codes.get(i);
				reviewTop5(key, totalVisits);
			}
		}
		System.out.println("rx something");
		if(rx == this.getOpContext().upstreams.size()){
			printTop5();
			rx = 0;
		}
	}

	private void printTop5(){
		Date now = new Date();
		System.out.println("TIME: "+(now.toString()));
		
		for(int i = 0; i<top.size(); i++){
			System.out.println(i+":"+top.get(i).countryCode+" total: "+top.get(i).visits);
		}
		System.out.println("      ");
		System.out.println("      ");
		System.out.println("#########################");
	}
	
	@Override
	public boolean isOrderSensitive() {
		// TODO Auto-generated method stub
		return false;
	}

}
