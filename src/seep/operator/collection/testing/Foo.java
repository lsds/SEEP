package seep.operator.collection.testing;

import seep.comm.serialization.DataTuple;
import seep.comm.tuples.Seep;
import seep.comm.tuples.Seep.DataTuple.Builder;
import seep.operator.Operator;
import seep.operator.StatelessOperator;

public class Foo extends Operator implements StatelessOperator{

	private int counter = 0;
	
	public Foo(int opID) {
		super(opID);
		subclassOperator = this;
		
	}

	
	public void processData(DataTuple dt) {
//		counter++;
//		System.out.println("VALUE: "+dt.getInt());
//		sendDown(dt, dt.getInt());
//		System.out.println("COUNTER: "+counter);
	}

	@Override
	public boolean isOrderSensitive() {
		// TODO Auto-generated method stub
		return false;
	}
}
