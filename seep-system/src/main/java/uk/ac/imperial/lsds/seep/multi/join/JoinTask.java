package uk.ac.imperial.lsds.seep.multi.join;

import java.util.concurrent.atomic.AtomicMarkableReference;

import uk.ac.imperial.lsds.seep.multi.ITask;
import uk.ac.imperial.lsds.seep.multi.MicroOperator;
import uk.ac.imperial.lsds.seep.multi.ResultCollector;
import uk.ac.imperial.lsds.seep.multi.SubQuery;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.seep.multi.WindowBatchFactory;

public class JoinTask extends ITask {

	private WindowBatch batch1;
	private WindowBatch batch2;
	
	private JoinResultHandler handler;
	
	private int freeIndex1;
	private int freeIndex2;
	
	private SubQuery query;
	
	public JoinTask() {
		
		this(null, null, null, null, 0, 0, 0, 0);
	}

	public JoinTask (SubQuery query, WindowBatch batch1, WindowBatch batch2, JoinResultHandler handler, 
		int taskid, int freeIndex1, int freeIndex2, int queryid) {
		
		this.query = query;
		
		this.batch1 = batch1;
		this.batch2 = batch2;
		
		this.handler = handler;
		
		this.freeIndex1 = freeIndex1;
		this.freeIndex2 = freeIndex2;
		
		this.taskid = taskid;
		this.queryid = queryid;
		this.next = new AtomicMarkableReference<ITask>(null, false);
	}

	public void set(SubQuery query, WindowBatch batch1, WindowBatch batch2, JoinResultHandler handler, 
		int taskid, int freeIndex1, int freeIndex2) {
		
		this.query = query;
		
		this.batch1 = batch1;
		this.batch2 = batch2;
		
		this.handler = handler;
		
		this.freeIndex1 = freeIndex1;
		this.freeIndex2 = freeIndex2;
		
		this.taskid = taskid;
		this.queryid = query.getId();
		this.next.set(null, false);
	}

	@Override
	public int run() {
		
		MicroOperator next = query.getMostUpstreamMicroOperator();
		
		/*
		 * First micro operator must be a join style operator
		 */
		next.process(this.batch1, this.batch2, this, GPU);
		next = next.getLocalDownstream();
		
		/*
		 * All micro operators that follow the first one must be single-input operators
		 */
		while (next != null) {
			next.process(this.batch1, this, GPU);
			next = next.getLocalDownstream();
		}
		
		/* ResultCollector.forwardAndFree(handler, query, this.batch1.getBuffer(),
				taskid, freeIndex1, freeIndex2); */
		
		if (GPU) {
			ResultCollector.forwardAndFree (handler, _query, this.batch1.getBuffer(),
					this.batch1.getTaskId(), this.batch1.getFreeOffset(), this.batch2.getFreeOffset());
		} else {
			ResultCollector.forwardAndFree (handler,  query, this.batch1.getBuffer(),
					this.batch1.getTaskId(), this.batch1.getFreeOffset(), this.batch2.getFreeOffset());
		}
		
		WindowBatchFactory.free(this.batch1);
		WindowBatchFactory.free(this.batch2);
		return 0;
	}

	@Override
	public void outputWindowBatchResult(int streamID, WindowBatch windowBatchResult) {
		
		this.batch1 = windowBatchResult;
		/* Control returns to run() method */
	}

	@Override
	public void free() {
		JoinTaskFactory.free(this);
	}

	@Override
	public SubQuery getQuery() {
		return query;
	}
}
