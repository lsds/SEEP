package uk.ac.imperial.lsds.seep.multi;


public class SubQueryTaskDispatcher {
	
//	private static final int SUB_QUERY_WINDOW_BATCH_COUNT = 
//		Integer.valueOf(GLOBALS.valueFor("subQueryWindowBatchCount"));
//	
//	/*
//	 * Counter for dispatched tasks 
//	 */
//	private int logicalOrderID = -1;
//	
//	private ISubQueryConnectable subQueryConnectable;
//	
//	private AtomicLong finished  = new AtomicLong(0L);
//	private AtomicLong submitted = new AtomicLong(0L);
//		
//	public SubQueryTaskDispatcher (ISubQueryConnectable subQueryConnectable) {
//
//		assert (SUB_QUERY_WINDOW_BATCH_COUNT > 0);
//		this.subQueryConnectable = subQueryConnectable;
//	}
//	
//	public void setUp () {
//		/* */
//	}
//	
//	public void assembleAndDispatchTask () {
//		/*
//		 * We can assemble a new task if there is at least one fully filled
//		 * window batch for all the input streams of this query 
//		 */
////		boolean canAssemble = true;
////		
////		for (SubQueryBufferWindowWrapper bufferWrapper: this.subQueryConnectable.getLocalUpstreamBuffers().values())
////			canAssemble &= bufferWrapper.getFullWindowBatches().size() > 1;
////			
////		if (canAssemble) {
//			
//			Map<SubQueryBufferWindowWrapper, Integer> freeUpToIndices = new HashMap<SubQueryBufferWindowWrapper, Integer>();
//			Map<Integer, IWindowBatch> windowBatchesForStreams = new HashMap<Integer, IWindowBatch>();
//			
//			for (Integer streamID: this.subQueryConnectable.getLocalUpstreamBuffers().keySet()) {
//				
//				SubQueryBufferWindowWrapper bufferWrapper = this.subQueryConnectable.getLocalUpstreamBuffers().get(streamID);
//				IWindowBatch batch = bufferWrapper.getFullWindowBatches().poll();
//				
//				int freeUpToIndex = bufferWrapper.getFreeIndexForBatchAndRemoveEntry(batch);
//				
//				if (freeUpToIndex != -1)
//					freeUpToIndices.put(bufferWrapper, freeUpToIndex);
//					
//				windowBatchesForStreams.put(streamID, batch);
//			}
//			/*
//			 * Create task 
//			 */
//			ISubQueryTask task;
//			task = new SubQueryTaskCPUCallableOLD(subQueryConnectable, windowBatchesForStreams, freshLogicalOrderID(), freeUpToIndices);
//			try {
//				this.subQueryConnectable.getParentMultiOperator().getExecutorService().submit(task);
//			} catch (Exception e) {
//				System.err.println(e);
//				e.printStackTrace();	
//			}
//			
////			submitted.incrementAndGet();
////		}
//	}
//	/* 
//	public void dummyAssembleAndDispatchTask () {
//		ISubQueryTask task;
//		task = new SubQueryTaskCPUCallable(subQueryConnectable, windowBatchesForStreams, freshLogicalOrderID(), freeUpToIndices);
//		try {
//			this.subQueryConnectable.getParentMultiOperator().getExecutorService().submit(task);
//		} catch (Exception e) {
//			System.err.println(e);
//			e.printStackTrace();
//		}
//	}
//	*/
//	
//	public void taskFinished () {
//		
//		this.finished.incrementAndGet();
//	}
//	
//	public long getFinishedTasks () {
//		
//		return finished.get ();
//	}
//	
//	public long getSubmittedTasks () {
//		return submitted.get ();
//	}
//	
//	private int freshLogicalOrderID () {
//		
//		if (this.logicalOrderID == Integer.MAX_VALUE) 
//			this.logicalOrderID = 0;
//		
//		this.logicalOrderID++;
//		return this.logicalOrderID;
//	}
//	
//	public int getNumberOfWindowsInBatch () {
//		
//		return SUB_QUERY_WINDOW_BATCH_COUNT;
//	}
//	
//	public int getLogicalOrderID () {
//		
//		return this.logicalOrderID;
//	}
}

