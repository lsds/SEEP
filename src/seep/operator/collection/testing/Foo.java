package seep.operator.collection.testing;

import seep.comm.serialization.DataTuple;
import seep.operator.Operator;
import seep.operator.StatelessOperator;

public class Foo extends Operator implements StatelessOperator{

	private static final long serialVersionUID = 1L;

	private int counter = 0;
	
	boolean first = true;
	long t_start = 0;
	long i_time = 0;
	
	public Foo(int opID) {
		super(opID);
		subclassOperator = this;
		
	}

	public void processData(DataTuple dt) {
		counter++;
//		System.out.println("VALUE: "+dt.getId());
		sendDown(dt, dt.getId());
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
