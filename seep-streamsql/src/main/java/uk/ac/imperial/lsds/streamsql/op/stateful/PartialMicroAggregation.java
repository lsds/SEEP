package uk.ac.imperial.lsds.streamsql.op.stateful;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;
import uk.ac.imperial.lsds.seep.multi.PartialWindowResults;
import uk.ac.imperial.lsds.seep.multi.PartialWindowResultsFactory;
import uk.ac.imperial.lsds.seep.multi.ThreadMap;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;

public class PartialMicroAggregation implements IStreamSQLOperator, IMicroOperatorCode {
	
	private static boolean debug = false;
	
	WindowDefinition windowDefinition;

	public PartialMicroAggregation (WindowDefinition windowDefinition) {
		
		this.windowDefinition = windowDefinition;
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("[Partial window u-aggregation]");
		return sb.toString();
	}
	
	@Override
	public void accept(OperatorVisitor ov) {
		ov.visit(this);
	}
	
	@Override
	public void processData (WindowBatch windowBatch, IWindowAPI api) {
		
		processPartialWindows (windowBatch, api);
	}
	
	private void processPartialWindows (WindowBatch windowBatch, IWindowAPI api) {
		
		int taskId = windowBatch.getTaskId();
		long p = windowBatch.getBatchStartTime();
		
		int workerId = ThreadMap.getInstance().get(Thread.currentThread().getId());
		
		PartialWindowResults closing  = PartialWindowResultsFactory.newInstance(workerId);
		PartialWindowResults pending  = PartialWindowResultsFactory.newInstance(workerId);
		PartialWindowResults complete = PartialWindowResultsFactory.newInstance(workerId);
		PartialWindowResults opening  = PartialWindowResultsFactory.newInstance(workerId);
		
		/* Iterate over the window sets */
		
		/* 1. Compute windows that close */
		
		// for each tuple
		// // store tuple in memory
		// // compute pane id
		// // compute window id that closes at pane id
		// // put tuple in window id buffer
		// // compute window id that opens at pane id
		// // put tuple in window id buffer
		// // if batch has pending windows
		// // // for each pending window id
		// // // // store tuple in pending window buffer
		// // 
		
		/* 2. Compute windows that are pending (if any) */
		
		/* 3. Compute complete windows (if there are no pending windows). */
		
		/* For every window that closes, there may be a complete window. */
		
		/* 4. Compute opening windows */
		
		/* For every window that closes, there is one window opening.
		 * If the opening window is not complete, then process it. */
		
		/* */
		
		/* At the end of processing, set window batch accordingly */
		if (closing != null)
			windowBatch.setClosing (closing);
		else {
			windowBatch.setClosing(null);
			closing.release();
		}
		
		if (pending != null)
			windowBatch.setPending (pending);
		else {
			windowBatch.setPending(null);
			pending.release();
		}
		
		if (complete != null)
			windowBatch.setComplete (complete);
		else {
			windowBatch.setComplete(null);
			complete.release();
		}
		
		if (opening != null)
			windowBatch.setOpening (opening);
		else {
			windowBatch.setOpening(null);
			opening.release();
		}
		
		if (debug)
			System.out.println(String.format("[DBG] Task %10d finished free pointer %10d", 
					taskId, windowBatch.getFreeOffset()));
	}
	
	private void processDataPerPane(WindowBatch windowBatch, IWindowAPI api) {
		/*
		ITupleSchema inSchema = windowBatch.getSchema();
		
		int tupleSize = inSchema.getByteSizeOfTuple();
		
		long _pane = windowDefinition.getPaneSize();
		
		long pps = windowDefinition.panesPerSlide();
		long ppw = windowDefinition.numberOfPanes(); 
		
		IQueryBuffer inputBuffer = windowBatch.getBuffer();
		*/
		int taskId = windowBatch.getTaskId();
		
		long p = windowBatch.getBatchStartTime();
		long q = windowBatch.getBatchEndTime();
		
		int workerId = ThreadMap.getInstance().get(Thread.currentThread().getId());
		
		if (debug)
			System.out.println(String.format("[DBG] %20s, thread id %03d pool id %03d", 
					Thread.currentThread().getName(), Thread.currentThread().getId(), workerId));
		
		/* Compute first window pointer, assuming:
		 * 
		 * a) all windows within a batch are complete; and
		 * 
		 * b) there are `nwindows` (1024) within the batch
		 */
		long windowSize = 32768 / 256;
		
		long wid = p / windowSize;
		long end = q / windowSize;
		
		while (wid < end - 1) {
			if (debug)
				System.out.println(String.format("[DBG] window %010d", wid));
			
			api.outputWindowResult(wid, -1, null);
			
			wid++;
		}
		/* The last window */
		if (debug)
			System.out.println(String.format("[DBG] window %010d", wid));
		
		api.outputWindowResult(wid, windowBatch.getFreeOffset(), null);
		
		if (debug)
			System.out.println(String.format("[DBG] Task %10d finished free pointer %10d", 
					taskId, windowBatch.getFreeOffset()));
	}
	
	@Override
	public void processData(WindowBatch firstWindowBatch, WindowBatch secondWindowBatch, IWindowAPI api) {
		
		throw new UnsupportedOperationException("MicroAggregation is single input operator and does not operate on two streams");
	}
}
