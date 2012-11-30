package seep.processingunit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import seep.comm.serialization.DataTuple;
import seep.operator.Operator;
import seep.runtimeengine.CoreRE;


public class ProcessingUnit {

	private CoreRE owner = null;
	private PUContext ctx = null;
	//Operators managed by this processing unit [ opId<Integer> - op<Operator> ]
	static public Map<Integer, Operator> mapOP_ID = new HashMap<Integer, Operator>();
	
	public ProcessingUnit(CoreRE owner){
		this.owner = owner;
		ctx = new PUContext(owner.getNodeDescr());
	}
	
	public void newOperatorInstantiation(Operator o) {
		o.setProcessingUnit(this);
		mapOP_ID.put(o.getOperatorId(), o);
	}

	public void setOpReady(int opId) {
		mapOP_ID.get(opId).setReady(true);
	}
	
	public PUContext setUpProcessingUnit(){
		ArrayList<Operator> operatorSet = (ArrayList<Operator>) mapOP_ID.values();
		ctx.configureOperatorConnections(operatorSet);
		return ctx;
	}

	public void sendData(DataTuple dt, int minValue, boolean b) {
		// TODO Auto-generated method stub
		
	}
}
