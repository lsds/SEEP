package uk.ac.imperial.lsds.seep.operator.compose.subquery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.compose.micro.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.operator.compose.micro.IMicroOperatorConnectable;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowAPI;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowBatch;
import uk.ac.imperial.lsds.seep.operator.compose.window.PeriodicWindowBatch;

public class SubQueryTask implements RunnableFuture<List<DataTuple>>, IWindowAPI {
	
	private int lastProcessed;
	
	private ISubQueryConnectable subQueryConnectable;
	
	private int logicalOrderID;

	private Map<Integer, IWindowBatch> windowBatches;
	
	public SubQueryTask(ISubQueryConnectable subQueryConnectable, Map<Integer, IWindowBatch> windowBatches, int logicalOrderID, int lastProcessed) {
		this.subQueryConnectable = subQueryConnectable;
		this.logicalOrderID = logicalOrderID;
		this.lastProcessed = lastProcessed;
		this.windowBatches = windowBatches;
	}
	
	public int getLogicalOrderID() {
		return this.logicalOrderID;
	}
	
	
	public int getLastProcessed() {
		return this.lastProcessed;
	}
	
	public ISubQueryConnectable getSubQueryConnectable() {
		return this.subQueryConnectable;
	}
	
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<DataTuple> get() throws InterruptedException,
			ExecutionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<DataTuple> get(long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCancelled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDone() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void run() {
		
		Set<IMicroOperatorConnectable> toProcess = new HashSet<>();
		Set<IMicroOperatorConnectable> waitForProcessing = new HashSet<>();
		Map<IMicroOperatorConnectable, Map<Integer, IWindowBatch>> windowBatchesForToProcess = new HashMap<>();
		
		/*
		 * Init for most upstream micro operators
		 */
		for (IMicroOperatorConnectable c : this.subQueryConnectable.getSubQuery().getMostUpstreamMicroOperators()) {
			toProcess.add(c);
			windowBatchesForToProcess.put(c,this.windowBatches);
		}
		
		while (!toProcess.isEmpty()) {
			/*
			 * Select a micro operator to execute
			 */
			currentOperator = toProcess.iterator().next();
			toProcess.remove(currentOperator);
			currentWindowBatchResults = new HashMap<>();
			
			/*
			 * Execute
			 */
			currentOperator.getMicroOperator().processData(windowBatchesForToProcess.get(currentOperator), this);
			
			/*
			 * We got the complete window batch result for the operator. So,
			 * we need to create the window batch for the subsequent operator 
			 * (if there is any)
			 */
			
			
			
			
		}
		
	}
	
	private IMicroOperatorConnectable currentOperator;
	private Map<Integer, List<List<DataTuple>>> currentWindowBatchResults;
	
	@Override
	public void outputWindowResult(List<DataTuple> windowResult) {
		for (Integer streamId : this.currentOperator.getLocalDownstream().keySet()) {
			// Store the window result
			currentWindowBatchResults.get(streamId).add(windowResult);
		}
	}

	@Override
	public void outputWindowBatchResult(List<List<DataTuple>> windowBatchResult) {
		for (List<DataTuple> windowResult : windowBatchResult)
			outputWindowResult(windowResult);
	}

	@Override
	public void outputWindowResult(int streamID, List<DataTuple> windowResult) {
		currentWindowBatchResults.get(streamID).add(windowResult);
	}

	@Override
	public void outputWindowBatchResult(int streamID,
			List<List<DataTuple>> windowBatchResult) {
		for (List<DataTuple> windowResult : windowBatchResult)
			outputWindowResult(streamID,windowResult);
		
	}
	

}
