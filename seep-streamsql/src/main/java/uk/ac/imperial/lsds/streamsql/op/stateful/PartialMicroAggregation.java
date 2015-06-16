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
		
		// processDataPerPane (windowBatch, api);
		
		processPartialWindows (windowBatch, api);
	}
	
	private void processPartialWindows (WindowBatch windowBatch, IWindowAPI api) {
		
		int taskId = windowBatch.getTaskId();
		long p = windowBatch.getBatchStartTime();
		
		int workerId = ThreadMap.getInstance().get(Thread.currentThread().getId());
		
		PartialWindowResults closing  = PartialWindowResultsFactory.newInstance(workerId);
		// PartialWindowResults pending  = new PartialWindowResults();
		PartialWindowResults complete = PartialWindowResultsFactory.newInstance(workerId);
		PartialWindowResults opening  = PartialWindowResultsFactory.newInstance(workerId);
		
		// windowBatch.setPending (pending);
		windowBatch.setPending  (null);
		
		if (p == 0)
			windowBatch.setClosing (null);
		else
			windowBatch.setClosing (closing);
			
		windowBatch.setComplete (complete);
		windowBatch.setOpening  ( opening);
		
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
