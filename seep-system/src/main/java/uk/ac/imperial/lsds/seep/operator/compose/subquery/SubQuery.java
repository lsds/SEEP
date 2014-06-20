package uk.ac.imperial.lsds.seep.operator.compose.subquery;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.compose.micro.IMicroOperatorConnectable;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowBatch;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowDefinition;

public class SubQuery {
	
	private int id;
	
	private ISubQueryConnectable parent;
	private Set<IMicroOperatorConnectable> microOperators;
	
	private Map<Integer, BlockingQueue<DataTuple>> inputQueues;
	private Map<Integer, BlockingQueue<DataTuple>> outputQueues;
	
	private Map<Integer, IWindowDefinition>  inputWindowDefinitions;
	
	private SubQuery(Set<IMicroOperatorConnectable> microOperators, int id, Map<Integer, IWindowDefinition>  inputWindowDefinitions) {
		this.id = id;
		this.inputQueues = new HashMap<Integer, BlockingQueue<DataTuple>>();
		this.outputQueues = new HashMap<Integer, BlockingQueue<DataTuple>>();
		this.inputWindowDefinitions = inputWindowDefinitions;
	}
	
	public void execute(ExecutorService executorService, int numberThreads, int batchSize) {
		for (int i = 0; i < numberThreads; i++)
			executorService.execute(new SubQueryTask());
	}

	public int getId() {
		return id;
	}

	public void registerInputQueue(Integer streamId,
			BlockingQueue<DataTuple> queue) {
		this.inputQueues.put(streamId, queue);
	}

	public void registerOutputQueue(Integer streamId,
			BlockingQueue<DataTuple> queue) {
		this.outputQueues.put(streamId, queue);
	}

	public void pushData(DataTuple tuple) {
		try {
			this.inputQueues.get(tuple.getPayload().emittingOperatorId).put(tuple);
		} catch (InterruptedException e) {
			//TODO: notify microOp and multiOp about failure
		}
	}
	public static SubQuery newSubQuery (
			Set<IMicroOperatorConnectable> microOperators, int opId, Map<Integer, IWindowDefinition>  inputWindowDefinitions) {
		return new SubQuery(microOperators, opId, inputWindowDefinitions);
	}


	public void setParentSubQueryConnectable(
			ISubQueryConnectable subQueryConnectable) {
		this.parent = subQueryConnectable;
	};

	
}
