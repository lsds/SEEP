package uk.ac.imperial.lsds.seep.operator.compose;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;

public interface MicroOperator {

	public int getMicroOperatorId();
	
	public void registerInputQueue(Integer streamId, BlockingQueue<DataTuple> queue);
	public void registerOutputQueue(Integer streamId, BlockingQueue<DataTuple> queue);
	
//	public void setUp(
//			Map<Integer, BlockingQueue<DataTuple>> inputQueues, 
//			Map<Integer, BlockingQueue<DataTuple>> outputQueues);

	public void execute(ExecutorService executorService, int numberThreads, int batchSize); 
	
	public void pushData(DataTuple tuple);
	
	public void setParentLocalConnectable(LocalConnectable parent);

}
