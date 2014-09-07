package uk.ac.imperial.lsds.seep.operator.compose.subquery;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import uk.ac.imperial.lsds.seep.operator.compose.micro.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.operator.compose.micro.IMicroOperatorConnectable;
import uk.ac.imperial.lsds.seep.operator.compose.micro.IStatefulMicroOperator;
import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOpTuple;
import uk.ac.imperial.lsds.seep.operator.compose.multi.SubQueryBufferWindowWrapper;
import uk.ac.imperial.lsds.seep.operator.compose.window.ArrayWindowBatch;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowAPI;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowBatch;

public class SubQueryTaskCPUCallable  implements ISubQueryTask, IWindowAPI {
		
	/*
	 * Input data for the actual execution of the task
	 */
	private ISubQueryConnectable subQueryConnectable;
	private Map<Integer, IWindowBatch> windowBatches;

	private ResultCollector collector;

	/*
	 * State
	 */
	private IMicroOperatorConnectable currentOperator;
	private Map<Integer, IWindowBatch> currentWindowBatchResults;
	private IWindowBatch finalWindowBatchResult;
	
	private long startTime, dt;
	
	public SubQueryTaskCPUCallable(ISubQueryConnectable subQueryConnectable, Map<Integer, IWindowBatch> windowBatches, int logicalOrderID, Map<SubQueryBufferWindowWrapper, Integer> freeUpToIndices) {
		this.subQueryConnectable = subQueryConnectable;
		this.windowBatches= windowBatches;
		this.collector = new ResultCollector(subQueryConnectable, logicalOrderID, freeUpToIndices);
	}
	
	@Override
	public void run() {
		
		try {
			
			startTime = System.currentTimeMillis();
			
			this.finalWindowBatchResult = new ArrayWindowBatch();
			Set<IMicroOperatorConnectable> processed = new HashSet<>();
			Queue<IMicroOperatorConnectable> toProcess = new LinkedList<>();
			Map<IMicroOperatorConnectable, Map<Integer, IWindowBatch>> windowBatchesForProcessing = new HashMap<>();
			
			/* Added by Alex */
			IWindowBatch batch = this.windowBatches.values().iterator().next();
			int [] startIndex = batch.getWindowStartPointers();
			int []   endIndex = batch.getWindowEndPointers();
			int batchSize = startIndex.length;
			int start = startIndex[0];
			int end = endIndex[batchSize - 1];
			int totalTuples = end - start + 1;
			
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
				currentOperator = toProcess.poll();
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
					if (processed.contains(c))
						continue;
					if (!windowBatchesForProcessing.containsKey(c))
						continue;
					
					if (windowBatchesForProcessing.get(c).keySet().containsAll(c.getLocalUpstream().keySet())) 
						toProcess.add(c);
				}
			}

			/*
			 * Code for only touching the input data
			 */
//			for (IWindowBatch windowBatch : this.windowBatches.values()) {
//				int i = windowBatch.getWindowStartPointers()[0];
//				int end = windowBatch.getWindowEndPointers()[windowBatch.getWindowEndPointers().length-1];
//				while (i < end) {
//					MultiOpTuple t = windowBatch.get(i++);
//				}
//			}
//			
			//int _taskcount = this.subQueryConnectable.getTaskDispatcher().taskcounter.getAndIncrement();
			//if (_taskcount < 7) {
			//	Thread.sleep(_taskcount * 200);
			//}
			/* Dummy computation */
//			Random random = new Random();
//			int result = 0;
//			for (int i = 0; i < 10000000; i++) {
//			 	result = random.nextInt(1024);
//				result *= result;
//			}
			
			// this.collector.pushResults(new MultiOpTuple[0]);
			this.collector.pushResults(this.finalWindowBatchResult.getArrayContent());
			
			dt = System.currentTimeMillis() - startTime;
			
			System.out.println(
				String.format("[DBG] task X %3d windows start @%6d end @%6d %8d %8d total %8d t_start %13d dt %6d (result size %10d)",
					batch.getWindowStartPointers().length,
					batch.getStartTimestamp(),
					batch.getEndTimestamp(),
					start,
					end,
					totalTuples,
					startTime,
					dt,
					this.finalWindowBatchResult.getArrayContent().length
				)
			);
			
			this.subQueryConnectable.getTaskDispatcher().taskFinished();			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void outputWindowResult(MultiOpTuple[] windowResult) {
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
	public void outputWindowBatchResult(MultiOpTuple[][] windowBatchResult) {
		for (MultiOpTuple[] windowResult : windowBatchResult)
			outputWindowResult(windowResult);
	}

	@Override
	public void outputWindowResult(int streamID, MultiOpTuple[] windowResult) {
		/*
		 * Is the current operator one of the most downstream operators?
		 */
		if (this.subQueryConnectable.getSubQuery().getMostDownstreamMicroOperator().equals(this.currentOperator)) {
			/*
			 * Yes, so we keep the window batch results as parts of the subquery result
			 */
			this.finalWindowBatchResult.addWindow(windowResult);
		}
		else {
			/*
			 * No, so we keep the window batch results only in a temporary data structure for the downstream operators
			 */
			if (!currentWindowBatchResults.containsKey(streamID))
				currentWindowBatchResults.put(streamID, new ArrayWindowBatch());

			currentWindowBatchResults.get(streamID).addWindow(windowResult);
		}		
	}

	@Override
	public void outputWindowBatchResult(int streamID,
			MultiOpTuple[][] windowBatchResult) {
		for (MultiOpTuple[] windowResult : windowBatchResult)
			outputWindowResult(streamID, windowResult);
		
	}
}