package seep.operator.collection.testing;

import seep.comm.tuples.Seep;
import seep.operator.Operator;
import seep.operator.StatelessOperator;

public class Bar extends Operator implements StatelessOperator{

	private int counter = 0;
	
	public Bar(int opID) {
		super(opID);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void processData(Seep.DataTuple dt) {
		counter++;
		System.out.println("VALUE: "+dt.getInt());
		sendDown(dt);
		System.out.println("COUNTER: "+counter);
	}

	@Override
	public boolean isOrderSensitive() {
		// TODO Auto-generated method stub
		return false;
	}

}
