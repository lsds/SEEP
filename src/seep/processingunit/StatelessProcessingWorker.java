package seep.processingunit;

import seep.comm.serialization.DataTuple;
import seep.infrastructure.NodeManager;
import seep.operator.Operator;
import seep.runtimeengine.DataStructureAdapter;
import seep.runtimeengine.InputQueue;

public class StatelessProcessingWorker implements Runnable{

	private InputQueue iq;
	private Operator runningOp;
	
	public StatelessProcessingWorker(DataStructureAdapter dsa, Operator runningOp) {
		if(dsa.getDSO() instanceof InputQueue){
			this.iq = (InputQueue) dsa.getDSO();
		}
		else{
			NodeManager.nLogger.severe("-> Operation not permitted at this moment.. stateful multi-core on dist barrier");
		}
		this.runningOp = runningOp;
	}

	@Override
	public void run() {
		while(true){
			DataTuple dt = iq.pull();
			runningOp.processData(dt);
		}
	}

}
