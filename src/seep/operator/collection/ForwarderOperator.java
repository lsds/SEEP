package seep.operator.collection;

import seep.operator.*;

import seep.comm.tuples.*;

public class ForwarderOperator extends Operator{ 

	public ForwarderOperator(int opID){
		super(opID);
	}
	
	//If threads are pushing data here, this processData should be sync
	public void processData(Seep.DataTuple dt){
		this.sendDown(dt);
	}

	@Override
	public boolean isOrderSensitive() {
		// TODO Auto-generated method stub
		return false;
	}
}
