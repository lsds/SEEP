package seep.operator.collection.testing;

import seep.comm.tuples.Seep;
import seep.comm.tuples.Seep.DataTuple.Builder;
import seep.operator.Operator;
import seep.operator.StatelessOperator;

public class Bar extends Operator implements StatelessOperator{

	public Bar(int opID) {
		super(opID);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void processData(Seep.DataTuple dt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isOrderSensitive() {
		// TODO Auto-generated method stub
		return false;
	}

}
