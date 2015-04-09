package uk.ac.imperial.lsds.seep.multi.join;

import uk.ac.imperial.lsds.seep.multi.CircularQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITaskDispatcher;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.SubQuery;
import uk.ac.imperial.lsds.seep.multi.TaskQueue;
import uk.ac.imperial.lsds.seep.multi.Utils;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.seep.multi.WindowBatchFactory;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition;

public class JoinTaskDispatcher implements ITaskDispatcher {
	
	/* private ConcurrentLinkedQueue<ITask> workerQueue, _workerQueue; */
	private TaskQueue workerQueue;
	private IQueryBuffer firstBuffer;
	private IQueryBuffer secondBuffer;
	private WindowDefinition firstWindow;
	private WindowDefinition secondWindow;
	private ITupleSchema firstSchema;
	private ITupleSchema secondSchema;
	private JoinResultHandler handler;
	private SubQuery parent;
	
	private int batch;
	
	private int nextTask = 1;
	
	private int firstStartIndex      = 0;
	private int firstNextIndex       = 0;
	private int firstEndIndex        = 0;
	private int firstLastEndIndex    = 0;
	private int firstToProcessCount  = 0;
	private int firstTupleSize;
	private long firstNextTime;
	private long firstEndTime;
	private int firstMask;

	private long secondNextTime;
	private long secondEndTime;
	private int secondTupleSize;
	private int secondStartIndex     = 0;
	private int secondNextIndex      = 0;
	private int secondLastEndIndex   = 0;
	private int secondEndIndex       = 0;
	private int secondToProcessCount = 0;
	private int secondMask;
	
	private Object lock = new Object();

	public JoinTaskDispatcher (SubQuery parent) {
		
		this.parent = parent;
		this.firstBuffer = new CircularQueryBuffer(parent.getId(), Utils._CIRCULAR_BUFFER_, false);
		this.secondBuffer = new CircularQueryBuffer(parent.getId(), Utils._CIRCULAR_BUFFER_, false);
		
		this.firstWindow = this.parent.getWindowDefinition();
		this.firstSchema = this.parent.getSchema();
		this.secondWindow = this.parent.getSecondWindowDefinition();
		this.secondSchema = this.parent.getSecondSchema();
		this.handler = new JoinResultHandler (this.firstBuffer, this.secondBuffer);
		
		this.firstTupleSize = firstSchema.getByteSizeOfTuple();
		this.secondTupleSize = secondSchema.getByteSizeOfTuple();
		
		this.firstMask = this.firstBuffer.capacity() - 1;
		this.secondMask = this.secondBuffer.capacity() - 1;
		
		this.batch     = this.parent.getQueryConf().BATCH;
	}
	
	@Override
	public void setup () {
		/* The default task queue for either CPU or GPU executor */
		this.workerQueue = this.parent.getExecutorQueue();
	}
	
	@Override
	public void dispatch (byte [] data, int length) {
		
		/*
		 * write the data to the buffer
		 */
		int idx;
		while ((idx = firstBuffer.put(data, length)) < 0) 
			Thread.yield();

		this.firstEndIndex = idx + length - firstTupleSize;
		
		synchronized (lock) {
			if (firstEndIndex < firstStartIndex)
				firstEndIndex += firstBuffer.capacity();
			
			firstToProcessCount = (firstEndIndex - firstStartIndex + this.firstTupleSize) / this.firstTupleSize;
					
			/*
			 * check whether we have to move the pointer that indicates the oldest
			 * window in this buffer that has not yet been closed. If we grab 
			 * the data to create a task, the start-pointer will be set to this next-pointer
			 */
			if (firstWindow.isRowBased()) {
				
				while ((firstNextIndex + firstWindow.getSize() * firstTupleSize) < firstEndIndex)
					firstNextIndex += firstTupleSize * firstWindow.getSlide();
			
			} else if (firstWindow.isRangeBased()) {
				firstNextTime = firstBuffer.getLong(firstNextIndex);
				firstEndTime = firstBuffer.getLong(firstEndIndex);
				
				while ((firstNextTime + firstWindow.getSize()) < firstEndTime) {
					firstNextIndex += firstTupleSize;
					firstNextTime = firstBuffer.getLong(firstNextIndex);
				}
			} else {
				throw new UnsupportedOperationException("error: window is neither row-based nor range-based");
			}
			
			/*
			 * check whether we have enough data to create a task
			 */
			if (firstToProcessCount >= this.batch || secondToProcessCount >= this.batch)
				createTask(true);
		}
	}
	
	
	@Override
	public void dispatchSecond (byte [] data, int length) {
		/*
		 * write the data to the buffer
		 */
		int idx;
		while ((idx = secondBuffer.put(data, length)) < 0) 
			Thread.yield();

		this.secondEndIndex = idx + length - secondTupleSize;
		
		synchronized (lock) {
			if (secondEndIndex < secondStartIndex)
				secondEndIndex += secondBuffer.capacity();
			
			secondToProcessCount = (secondEndIndex - secondStartIndex + this.secondTupleSize) / this.secondTupleSize;
					
			/*
			 * check whether we have to move the pointer that indicates the oldest
			 * window in this buffer that has not yet been closed. If we grab 
			 * the data to create a task, the start-pointer will be set to this next-pointer
			 */
			if (secondWindow.isRowBased()) {
				
				while ((secondNextIndex + secondWindow.getSize() * secondTupleSize) < secondEndIndex)
					secondNextIndex += secondTupleSize * secondWindow.getSlide();
			
			} else if (secondWindow.isRangeBased()) {
				secondNextTime = secondBuffer.getLong(secondNextIndex);
				secondEndTime = secondBuffer.getLong(secondEndIndex);
				
				while ((secondNextTime + secondWindow.getSize()) < secondEndTime) {
					secondNextIndex += secondTupleSize;
					secondNextTime = secondBuffer.getLong(secondNextIndex);
				}
			} else {
				throw new UnsupportedOperationException("error: window is neither row-based nor range-based");
			}
			
			/*
			 * check whether we have enough data to create a task
			 */
			if (firstToProcessCount >= this.batch || secondToProcessCount >= this.batch)
				createTask(false);
		}
	}
	
	private void createTask(boolean dispatchedInFirst) {
		
		int taskid = this.getTaskNumber();
		
		int firstFree = Integer.MIN_VALUE;
		int secondFree = Integer.MIN_VALUE;
		
		if (firstNextIndex != firstStartIndex)
			firstFree = (firstNextIndex - firstTupleSize) & firstMask;
		if (secondNextIndex != secondStartIndex)
			secondFree = (secondNextIndex - secondTupleSize) & secondMask;
		
		WindowBatch firstBatch = WindowBatchFactory.newInstance(this.batch, taskid, firstFree, firstBuffer, firstWindow, firstSchema);
		WindowBatch secondBatch = WindowBatchFactory.newInstance(this.batch, taskid, secondFree, secondBuffer, secondWindow, secondSchema);
	
		if (dispatchedInFirst) {
			firstBatch.setBatchPointers(firstLastEndIndex, firstEndIndex);
			secondBatch.setBatchPointers(secondStartIndex, secondEndIndex);
			this.firstLastEndIndex = this.firstEndIndex;
			this.secondLastEndIndex = this.secondEndIndex;
		}
		else {
			firstBatch.setBatchPointers(firstStartIndex, firstEndIndex);
			secondBatch.setBatchPointers(secondLastEndIndex, secondEndIndex);
			this.firstLastEndIndex = this.firstEndIndex;
			this.secondLastEndIndex = this.secondEndIndex;
		}
		
		JoinTask task = JoinTaskFactory.newInstance(parent, firstBatch, secondBatch, handler, taskid, firstFree, secondFree);

//		System.out.println(String.format("[DBG] 1st window batch starts at %10d ends at %10d free at %10d", 
//		firstBatch.getBatchStartPointer()/firstTupleSize, firstBatch.getBatchEndPointer()/firstTupleSize, firstFree/firstTupleSize)); 
//		
//		System.out.println(String.format("[DBG] 2nd window batch starts at %10d ends at %10d free at %10d", 
//		secondBatch.getBatchStartPointer()/secondTupleSize, secondBatch.getBatchEndPointer()/secondTupleSize, secondFree/secondTupleSize)); 
		
		workerQueue.add(task);
		
		/*
		 * First, reduce the number of tuples that are ready for processing by the 
		 * number of tuples that are fully processed in the task that was just created
		 */
		if (secondNextIndex != secondStartIndex)
			secondToProcessCount -= (secondNextIndex - secondStartIndex + secondTupleSize) / secondTupleSize;
		if (firstNextIndex != firstStartIndex)
			firstToProcessCount -= (firstNextIndex - firstStartIndex + firstTupleSize) / firstTupleSize;
		
		/*
		 * Second, move the start-pointer for the next task to the next-pointer
		 */
		if (secondNextIndex > secondMask)
			secondNextIndex = secondNextIndex & secondMask;
		if (firstNextIndex > firstMask)
			firstNextIndex = firstNextIndex & firstMask;
		
		secondStartIndex = secondNextIndex;
		firstStartIndex = firstNextIndex;
	}
	
	private int getTaskNumber () {
		int id = nextTask ++;
		if (nextTask == Integer.MAX_VALUE)
			nextTask = 1;
		return id;
	}

	@Override
	public IQueryBuffer getBuffer() {
		return this.firstBuffer;
	}

	@Override
	public IQueryBuffer getSecondBuffer() {
		return this.secondBuffer;
	}

	@Override
	public boolean tryDispatch (byte[] data, int length) {
		
		return false;
	}
}

