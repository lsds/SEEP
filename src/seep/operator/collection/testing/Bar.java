package seep.operator.collection.testing;

import seep.comm.serialization.DataTuple;
import seep.comm.tuples.Seep;
import seep.operator.Operator;
import seep.operator.StatelessOperator;

public class Bar extends Operator implements StatelessOperator{

	private int counter = 0;
	
	
	boolean first = true;
	long t_start = 0;
	long i_time = 0;
	
	public Bar(int opID) {
		super(opID);
		// TODO Auto-generated constructor stub
	}

	
	public void processData(DataTuple dt) {
		counter++;
//		System.out.println("VALUE: "+dt.getId());
		sendDown(dt);
//		System.out.println("COUNTER: "+counter);
		counter++;
//		System.out.println("VALUE: "+dt.getId());
//		sendDown(dt, dt.getId());
//		System.out.println("COUNTER: "+counter);
		if(first){
			t_start = System.currentTimeMillis();
			first = false;
		}
		i_time = System.currentTimeMillis();
		long currentTime = i_time - t_start;
		
		if(currentTime >= 1000){
			System.out.println("InputQueue Size: "+getInputQueue().getSize());
			t_start = System.currentTimeMillis();
			counter = 0;
		}
	}

	@Override
	public boolean isOrderSensitive() {
		// TODO Auto-generated method stub
		return false;
	}

}
