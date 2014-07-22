package uk.ac.imperial.lsds.seep.operator.compose.subquery;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.compose.micro.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.operator.compose.micro.IMicroOperatorConnectable;
import uk.ac.imperial.lsds.seep.operator.compose.micro.IStatefulMicroOperator;
import uk.ac.imperial.lsds.seep.operator.compose.multi.SubQueryBuffer;
import uk.ac.imperial.lsds.seep.operator.compose.window.IStaticWindowBatch;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowAPI;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowBatch;
import uk.ac.imperial.lsds.seep.operator.compose.window.StaticWindowBatch;

public class SubQueryTaskCallable implements Callable<SubQueryTaskResult>, IWindowAPI {
		
	
	private ISubQueryConnectable subQueryConnectable;
	private Map<Integer, IWindowBatch> windowBatches;
	
	private IMicroOperatorConnectable currentOperator;
	private Map<Integer, IStaticWindowBatch> currentWindowBatchResults;
	private IStaticWindowBatch finalWindowBatchResult;

	private SubQueryTaskResult result;
	public SubQueryTaskCallable(ISubQueryConnectable subQueryConnectable, Map<Integer, IWindowBatch> windowBatches, int logicalOrderID, Map<SubQueryBuffer, Integer> freeUpToIndices) {
		this.subQueryConnectable = subQueryConnectable;
		this.windowBatches = windowBatches;
		
		this.result = new SubQueryTaskResult(logicalOrderID, freeUpToIndices);
	}
	
	public ISubQueryConnectable getSubQueryConnectable() {
		return this.subQueryConnectable;
	}

	@Override
	public SubQueryTaskResult call() throws Exception {
		
		this.finalWindowBatchResult = new StaticWindowBatch();
		Set<IMicroOperatorConnectable> processed = new HashSet<>();
		Set<IMicroOperatorConnectable> toProcess = new HashSet<>();
		Map<IMicroOperatorConnectable, Map<Integer, IWindowBatch>> windowBatchesForProcessing = new HashMap<>();
		
		/*
		 * Init for most upstream micro operators
		 */
		for (IMicroOperatorConnectable c : this.subQueryConnectable.getSubQuery().getMostUpstreamMicroOperators()) {
			toProcess.add(c);
			windowBatchesForProcessing.put(c,this.windowBatches);
		}
		
		while (!toProcess.isEmpty()) {
			/*
			 * Select a micro operator to execute
			 */
			currentOperator = toProcess.iterator().next();
			currentWindowBatchResults = new HashMap<>();
			toProcess.remove(currentOperator);
			processed.add(currentOperator);
			
			/*
			 * Execute
			 * If the actual code is stateful, we need to obtain a new instance
			 */
			IMicroOperatorCode operatorCode = currentOperator.getMicroOperator().getOp();
			
			if (operatorCode instanceof IStatefulMicroOperator) {
				operatorCode = ((IStatefulMicroOperator) operatorCode).getNewInstance();
			}
			operatorCode.processData(windowBatchesForProcessing.get(currentOperator), this);
			
			/*
			 * We got the complete window batch result for the operator. So,
			 * we need to store the window batches for the subsequent operators 
			 * (if there are any)
			 */
			for (Integer streamId : this.currentOperator.getLocalDownstream().keySet()) {
				IMicroOperatorConnectable c = this.currentOperator.getLocalDownstream().get(streamId);
				if (!windowBatchesForProcessing.containsKey(c))
					windowBatchesForProcessing.put(c, new HashMap<Integer, IWindowBatch>());
				
				windowBatchesForProcessing.get(c).put(streamId, this.currentWindowBatchResults.get(streamId));
			}
			
			/*
			 * Check for further operators that are ready to be executed, i.e., 
			 * we have the window batch results for all their incoming streams
			 */
			for (IMicroOperatorConnectable c : this.subQueryConnectable.getSubQuery().getMicroOperators()) {
				if (c.equals(this.currentOperator))
					continue;
				if (processed.contains(c))
					continue;
				if (!windowBatchesForProcessing.containsKey(c))
					continue;
				
				if (windowBatchesForProcessing.get(c).keySet().containsAll(c.getLocalUpstream().keySet())) 
					toProcess.add(c);
			}
		}
		
		this.result.setResultStream(this.finalWindowBatchResult.getAllTuples());
		return this.result;
	}
	
	
	@Override
	public void outputWindowResult(List<DataTuple> windowResult) {
		if (this.subQueryConnectable.getSubQuery().getMostDownstreamMicroOperator().equals(this.currentOperator)) {
			/*
			 * the given stream id (-1) will be ignore when calling the following
			 * method for a most downstream operator
			 */
			outputWindowResult(-1, windowResult);
		}
		else {
			for (Integer streamId : this.currentOperator.getLocalDownstream().keySet()) 
				outputWindowResult(streamId, windowResult);
		}		
	}

	@Override
	public void outputWindowBatchResult(List<List<DataTuple>> windowBatchResult) {
		for (List<DataTuple> windowResult : windowBatchResult)
			outputWindowResult(windowResult);
	}

	@Override
	public void outputWindowResult(int streamID, List<DataTuple> windowResult) {
		/*
		 * Is the current operator one of the most downstream operators?
		 */
		if (this.subQueryConnectable.getSubQuery().getMostDownstreamMicroOperator().equals(this.currentOperator)) {
			/*
			 * Yes, so we keep the window batch results as parts of the subquery result
			 */
			this.finalWindowBatchResult.registerWindow(windowResult);
		}
		else {
			/*
			 * No, so we keep the window batch results only in a temporary data structure for the downstream operators
			 */
			if (!currentWindowBatchResults.containsKey(streamID))
				currentWindowBatchResults.put(streamID, new StaticWindowBatch());

			currentWindowBatchResults.get(streamID).registerWindow(windowResult);
		}		
	}

	@Override
	public void outputWindowBatchResult(int streamID,
			List<List<DataTuple>> windowBatchResult) {
		for (List<DataTuple> windowResult : windowBatchResult)
			outputWindowResult(streamID,windowResult);
		
	}

	
}
