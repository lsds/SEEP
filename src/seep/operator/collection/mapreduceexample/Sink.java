package seep.operator.collection.mapreduceexample;

import seep.comm.serialization.DataTuple;
import seep.operator.Operator;
import seep.operator.StatelessOperator;

public class Sink extends Operator implements StatelessOperator{

	private static final long serialVersionUID = 1L;

	public Sink(int opId){
		super(opId);
		subclassOperator = this;
	}

	@Override
	public void processData(DataTuple dt) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean isOrderSensitive() {
		// TODO Auto-generated method stub
		return false;
	}

}
