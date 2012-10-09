package seep.operator.collection.mapreduceexample;

import seep.comm.serialization.DataTuple;
import seep.operator.Operator;
import seep.operator.StatelessOperator;

public class Map extends Operator implements StatelessOperator{
	
	private static final long serialVersionUID = 1L;

	public Map(int opId){
		super(opId);
		subclassOperator = this;
	}
	
	@Override
	public void processData(DataTuple dt) {
		//Emit <K,V> being <countryCode, 1>
		dt.setMRValue(1);
		sendDown(dt);
	}

	@Override
	public boolean isOrderSensitive() {
		// TODO Auto-generated method stub
		return false;
	}

}
