package seep.operator.collection.testing;

import seep.comm.serialization.DataTuple;
import seep.comm.tuples.Seep;
import seep.comm.tuples.Seep.DataTuple.Builder;
import seep.operator.Operator;
import seep.operator.StatelessOperator;

public class TestSink extends Operator implements StatelessOperator{

	public TestSink(int opID) {
		super(opID);
		subclassOperator = this;
	}

	
	public void processData(DataTuple dt) {
//		System.out.println("RCV: "+dt.getInt());
		
	}

	@Override
	public boolean isOrderSensitive() {
		// TODO Auto-generated method stub
		return false;
	}

}
