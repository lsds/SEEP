package seep.processingunit;

import java.util.HashMap;
import java.util.Map;

import seep.operator.Operator;


public class ProcessingUnit {

	private int totalOperators = 0;
	private int readyOperators = 0;
	static public Map<Integer, Operator> mapOP_ID = new HashMap<Integer, Operator>();
	
	public void newOperatorInstantiation(Operator o) {
		totalOperators++;
		mapOP_ID.put(o.getOperatorId(), o);
//		o.instantiateOperator();
	}

	public void setOpReady(int opId) {
		mapOP_ID.get(opId).setReady(true);
		readyOperators++;
		//Check if all the operators are ready and initialize all the necessary structures
		if(totalOperators == readyOperators){
			
		}
	}

}
