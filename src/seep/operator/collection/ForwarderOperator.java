package seep.operator.collection;

import seep.operator.*;

import seep.comm.serialization.DataTuple;

public class ForwarderOperator extends Operator{ 

	public ForwarderOperator(int opID){
		super(opID);
	}
	
	//If threads are pushing data here, this processData should be sync
	public void processData(DataTuple dt){
//		this.sendDown(dt);
	}

	@Override
	public boolean isOrderSensitive() {
		// TODO Auto-generated method stub
		return false;
	}
}
