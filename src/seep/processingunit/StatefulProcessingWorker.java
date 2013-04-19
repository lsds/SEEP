package seep.processingunit;

import seep.comm.serialization.DataTuple;
import seep.infrastructure.NodeManager;
import seep.operator.Operator;
import seep.operator.State;
import seep.runtimeengine.DataStructureAdapter;
import seep.runtimeengine.InputQueue;

public class StatefulProcessingWorker implements Runnable{

	private DataStructureAdapter dsa;
	private InputQueue iq;
	private Operator runningOp;
	private State state;
	
	public StatefulProcessingWorker(DataStructureAdapter dsa, Operator op, State s) {
		if(dsa.getDSO() instanceof InputQueue){
			this.iq = (InputQueue) dsa.getDSO();
		}
		else{
			NodeManager.nLogger.severe("-> Operation not permitted at this moment.. stateful multi-core on dist barrier");
		}
		this.runningOp = op;
		this.state = s;
	}

	@Override
	public void run() {
		while(true){
			DataTuple dt = iq.pull();
			runningOp.processData(dt);
		}
	}
}
