package seep.operator.collection.testing;

import seep.comm.tuples.Seep;
import seep.comm.tuples.Seep.DataTuple.Builder;
import seep.operator.Operator;
import seep.operator.StatelessOperator;

public class Foo extends Operator implements StatelessOperator{

	public Foo(int opID) {
		super(opID);
		subclassOperator = this;
		
	}

	@Override
	public void processData(Seep.DataTuple dt) {
		System.out.println("VALUE: "+dt.getInt());
		sendDown(dt);
	}

	@Override
	public boolean isOrderSensitive() {
		// TODO Auto-generated method stub
		return false;
	}
}
