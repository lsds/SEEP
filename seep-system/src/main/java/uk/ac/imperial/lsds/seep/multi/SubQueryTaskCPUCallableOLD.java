package uk.ac.imperial.lsds.seep.multi;

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
import uk.ac.imperial.lsds.seep.operator.compose.subquery.ISubQueryConnectable;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.ISubQueryTask;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.ResultCollector;
import uk.ac.imperial.lsds.seep.operator.compose.window.ArrayWindowBatch;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowAPI;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowBatch;

public class SubQueryTaskCPUCallableOLD  implements Runnable, IWindowAPI {
		
	/*
	 * Input data for the actual execution of the task
	 */
	private ISubQueryConnectable subQueryConnectable;
	private Map<Integer, WindowBatch> windowBatches;

	private ResultCollector collector;

	/*
	 * State
	 */
	private IMicroOperatorConnectable currentOperator;
	private WindowBatch currentWindowBatchResult;
	private int mostDownstreamOperatorId;
	private int currentOperatorId;

	private long startTime, dt;
	
	
	public SubQueryTaskCPUCallableOLD(ISubQueryConnectable subQueryConnectable, Map<Integer, IWindowBatch> windowBatches, int logicalOrderID, Map<SubQueryBufferWindowWrapper, Integer> freeUpToIndices) {
		this.subQueryConnectable = subQueryConnectable;
		this.windowBatches= windowBatches;
		this.collector = new ResultCollector(subQueryConnectable, logicalOrderID, freeUpToIndices);
		this.mostDownstreamOperatorId = this.subQueryConnectable.getSubQuery().getMostDownstreamMicroOperator().getMicroOperator().getOpID();
	}
	
	@Override
	public void run() {
		
		try {
			
			startTime = System.currentTimeMillis();
			
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
				this.currentOperatorId = currentOperator.getMicroOperator().getOpID();
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
			this.collector.pushResults(this.finalWindowBatchResult.getContent());
			
//			dt = System.currentTimeMillis() - startTime;
//			
//			System.out.println(
//				String.format("[DBG] task X %3d windows start @%6d end @%6d %8d %8d total %8d t_start %13d dt %6d (result size %10d)",
//					batch.getWindowStartPointers().length,
//					batch.getStartTimestamp(),
//					batch.getEndTimestamp(),
//					start,
//					end,
//					totalTuples,
//					startTime,
//					dt,
//					this.finalWindowBatchResult.getArrayContent().length
//				)
//			);
			
			this.subQueryConnectable.getTaskDispatcher().taskFinished();			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void outputWindowBatchResult(int streamID,
			WindowBatch windowBatchResult) {
		this.currentWindowBatchResult = windowBatchResult;
	}


}
