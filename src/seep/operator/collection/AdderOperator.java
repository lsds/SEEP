package seep.operator.collection;

import seep.comm.serialization.DataTuple;
import seep.operator.*;

public class AdderOperator extends Operator{ 

	private int addFactor;

	//test purposes
	int counter = 0;
	int n_events = 100000;
	long t_start = 0;
	long t_end = 0;
	//finish test purposes

	public AdderOperator(int opID, int addFactor){
		super(opID);
		this.addFactor = addFactor;
	}
	
	//If threads are pushing data here, this processData should be sync
	public void processData(DataTuple dt){
//		Seep.DataTuple.Builder event = Seep.DataTuple.newBuilder();
////		int value = dt.getInt()+addFactor;
////		event.setInt(value);
//		
////		System.out.println("ADDER: Sending -> "+value);
//		//System.out.println();
//		sendDown(event.build());
//		counter++;
//		if(counter == n_events){
//			t_end = System.currentTimeMillis();
//			System.out.println("ADD: "+(t_end-t_start)+" ms for "+n_events+" events");
//			t_start = System.currentTimeMillis();
//			counter = 0;
//		}
	}

	@Override
	public boolean isOrderSensitive() {
		// TODO Auto-generated method stub
		return false;
	}

}
