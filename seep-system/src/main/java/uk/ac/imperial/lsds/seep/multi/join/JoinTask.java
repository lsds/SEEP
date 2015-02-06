package uk.ac.imperial.lsds.seep.multi.join;

import uk.ac.imperial.lsds.seep.multi.ITask;
import uk.ac.imperial.lsds.seep.multi.MicroOperator;
import uk.ac.imperial.lsds.seep.multi.ResultCollector;
import uk.ac.imperial.lsds.seep.multi.SubQuery;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.seep.multi.WindowBatchFactory;

public class JoinTask implements ITask {

	private WindowBatch		    firstBatch;
	private WindowBatch		    secondBatch;
	private JoinResultHandler	handler;
	private int				    taskid;
	private int				    firstFreeUpTo;
	private int				    secondFreeUpTo;
	private SubQuery		    query;

	private boolean			GPU	= false;

	@Override
	public void setGPU(boolean GPU) {
		this.GPU = GPU;
	}

	public JoinTask() {
		this(null, null, null, null, 0, 0, 0);
	}

	public JoinTask(SubQuery query, WindowBatch firstBatch,
			WindowBatch secondBatch, JoinResultHandler handler, int taskid,
			int firstFreeUpTo, int secondFreeUpTo) {
		this.query = query;
		this.firstBatch = firstBatch;
		this.secondBatch = secondBatch;
		this.handler = handler;
		this.taskid = taskid;
		this.firstFreeUpTo = firstFreeUpTo;
		this.secondFreeUpTo = secondFreeUpTo;
	}

	public void set(SubQuery query, WindowBatch firstBatch,
			WindowBatch secondBatch, JoinResultHandler handler, int taskid,
			int firstFreeUpTo, int secondFreeUpTo) {
		this.query = query;
		this.firstBatch = firstBatch;
		this.secondBatch = secondBatch;
		this.handler = handler;
		this.taskid = taskid;
		this.firstFreeUpTo = firstFreeUpTo;
		this.secondFreeUpTo = secondFreeUpTo;
	}

	@Override
	public int run() {
		
		MicroOperator next = query.getMostUpstreamMicroOperator();
		
		/*
		 * First micro operator must be a join style operator
		 */
		next.process(this.firstBatch, this.secondBatch, this, GPU);
		next = next.getLocalDownstream();
		
		/*
		 * All micro operators that follow the first one must be single-input operators
		 */
		while (next != null) {
			next.process(this.firstBatch, this, GPU);
			next = next.getLocalDownstream();
		}

		ResultCollector.forwardAndFree(handler, query, this.firstBatch.getBuffer(),
				taskid, firstFreeUpTo, secondFreeUpTo);

		// this.firstBatch.getBuffer().release();
		// this.secondBatch.getBuffer().release();
		
		WindowBatchFactory.free(this.firstBatch);
		WindowBatchFactory.free(this.secondBatch);

		return 0;
	}

	@Override
	public void outputWindowBatchResult(int streamID,
			WindowBatch windowBatchResult) {
		this.firstBatch = windowBatchResult;
		/* Control returns to run() method */
	}

	@Override
	public void free() {
		JoinTaskFactory.free(this);
	}
}
