package uk.ac.imperial.lsds.seep.operator.compose;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.OperatorCode;
import uk.ac.imperial.lsds.seep.state.StateWrapper;

public class StatefulMicroOperator implements MicroOperator {

	Map<Integer, BlockingQueue<DataTuple>> inputQueues;
	
	Map<Integer, BlockingQueue<DataTuple>> outputQueues;

	OperatorCode op;
	
	private int id;
	
	@Override
	public int getMicroOperatorId() {
		return id;
	}

	@Override
	public void execute(ExecutorService executorService, int numberThreads,
			int batchSize) {
		// TODO Auto-generated method stub

	}
	
	public static MicroOperator newStatefulMicroOperator(OperatorCode op,
			int opId, StateWrapper s) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void pushData(DataTuple tuple) {
		/*
		 * This method is only used to push data
		 * to the most upstream operator, which cannot
		 * have more than one input queue		
		 */
		try {
			this.inputQueues.values().iterator().next().put(tuple);
		} catch (InterruptedException e) {
			//TODO: notify microOp and multiOp about failure
		}
	}

	@Override
	public void registerInputQueue(Integer streamId,
			BlockingQueue<DataTuple> queue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void registerOutputQueue(Integer streamId,
			BlockingQueue<DataTuple> queue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setParentLocalConnectable(LocalConnectable parent) {
		// TODO Auto-generated method stub
		
	}

}
