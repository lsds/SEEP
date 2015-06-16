//package uk.ac.imperial.lsds.streamsql.op.stateful;
//
//import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
//import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
//import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
//import uk.ac.imperial.lsds.seep.multi.IWindowAPI;
//import uk.ac.imperial.lsds.seep.multi.ThreadMap;
//import uk.ac.imperial.lsds.seep.multi.WindowBatch;
//import uk.ac.imperial.lsds.seep.multi.WindowDefinition;
//import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
//import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;
//
//public class TempPartialMicroAggregation implements IStreamSQLOperator, IMicroOperatorCode {
//	
//	private static boolean debug = false;
//	
//	WindowDefinition windowDefinition;
//
//	public TempPartialMicroAggregation (WindowDefinition windowDefinition) {
//		
//		this.windowDefinition = windowDefinition;
//	}
//	
//	@Override
//	public String toString() {
//		final StringBuilder sb = new StringBuilder();
//		sb.append("[Partial window u-aggregation]");
//		return sb.toString();
//	}
//	
//	@Override
//	public void accept(OperatorVisitor ov) {
//		ov.visit(this);
//	}
//	
//	@Override
//	public void processData (WindowBatch windowBatch, IWindowAPI api) {
//		
//		processDataPerPane(windowBatch, api);
//	}
//	
//	private void processDataPerPane(WindowBatch windowBatch, IWindowAPI api) {
//		
//		ITupleSchema inSchema = windowBatch.getSchema();
//		
//		int tupleSize = inSchema.getByteSizeOfTuple();
//		
//		long _pane = windowDefinition.getPaneSize();
//		
//		long pps = windowDefinition.panesPerSlide();
//		long ppw = windowDefinition.numberOfPanes(); 
//		
//		IQueryBuffer inBuffer = windowBatch.getBuffer();
//		
//		int taskId = windowBatch.getTaskId();
//		
//		long p = windowBatch.getBatchStartTime();
//		long q = windowBatch.getBatchEndTime();
//		long idx = p;
//		
//		int workerId = ThreadMap.getInstance().get(Thread.currentThread().getId());
//		
//		if (debug)
//			System.out.println(String.format("[DBG] %20s, thread id %03d pool id %03d", 
//				Thread.currentThread().getName(), Thread.currentThread().getId(), workerId));
//		
//		/* Compute first and last pane */
//		long fp =  p / tupleSize  / _pane;
//		long lp = (q - tupleSize) / tupleSize / _pane;
//		
//		long npanes = lp - fp + 1;
//		
//		long firstCompleteWindow = Long.MAX_VALUE;
//		long  lastCompleteWindow = Long.MIN_VALUE;
//		
//		long completeWindowFreePointer = -1;
//		
//		// System.out.println(String.format("[DBG] task %04d [%13d,%13d) %10d panes [%10d, %10d]", taskId, p, q, npanes, fp, lp));
//		
//		int cwpb = 0; /* Complete windows per batch */
//		int iwpb = 0; /* Incomplete windows / batch */
//		
//		boolean fpc = false; /* Is first pane complete? Unknown for time-based windows */
//		boolean lpc = false; /* Is last  pane complete? */
//				
//		long tid, pid = fp, _pid = fp;
//		long opensAt, closesAt;
//		long wid;
//		long tpp = 0; /* # tuples/pane */
//		
//		long tuplesProcessed = 0;
//		long panesProcessed  = 0;
//		
////		for (idx = p; idx < q; idx += tupleSize) {
////			// tid = idx / tupleSize; /* # tuple index */
////			pid = idx / tupleSize / _pane;
////			tuplesProcessed++;
////			if (pid > _pid) {
////				// panesProcessed += 1;
////				tpp = 0; /* Reset tuples/pane counter */
////				_pid = pid;
////			}
////			tpp += 1;
////		}
////		panesProcessed += 1;
////		
////		for (int i = 0; i < 32768; i++) {
////			pid = (p / tupleSize + (i)) / _pane;
////			tuplesProcessed++;
////			if (pid > _pid) {
////				panesProcessed += 1;
////				tpp = 0; /* Reset tuples/pane counter */
////				_pid = pid;
////			}
////			tpp += 1;
////		}
////		panesProcessed += 1;
//		
//		// System.out.println(String.format("%10d tuples processed; %10d panes", tuplesProcessed, panesProcessed));
//		
////		for (idx = p; idx < q; idx += tupleSize) {
////			
////			tid = idx / tupleSize; /* # tuple index */
////			pid = tid / _pane;
////			/*
////			 * System.out.println(String.format("[DBG] data index %13d tuple %13d pane %13d", dataIndex, tid, pid));
////			 */
////			if (pid > _pid) {
////				/* Pane `_pid` closed */
////				
////				/* If this is the first pane, check if it is closed */
////				if (_pid == fp) {
////					if (windowDef.isRowBased() == true && tpp == _pane) {
////						fpc = true;
////					}
////				}
////				
////				/* Count complete and incomplete windows */
////				
////				/* Given pane id `pid`, compute the window that
////				 * opens at `pid` and the window that closes at
////				 * it */
////				closesAt = _pid + ppw - 1;
////				opensAt  = _pid - ppw + 1;
////				
////				/* The window id (ordinal) */
////				wid = opensAt / pps;
////				
////				/*
////				 * Check windows that close at this particular pane id.
////				 *
////				 * System.out.println("[DBG] window " + wid); 
////				 *
////				 * The first pane of a batch is a special case. If incomplete, then
////				 * it must be merged together with last pane of the previous batch.
////				 */
////				if (opensAt == fp) {
////					if (fpc == false) {
////						/* The window that closes at pane `pid` is incomplete */
////						iwpb += 1;
////						// System.out.println(String.format("[DBG] window %04d [%10d, %10d] is incomplete", wid, opensAt, _pid));
////						/* incomplete_windows.append(wid) */
//////						api.outputWindowResult(wid, wid, windowBatch.getBuffer().normalise(idx - 1), null, false, true);
////					} else {
////						cwpb += 1;
////						/* System.out.println(String.format("[DBG] window %04d is complete [%4d,%4d]", wid, opensAt, _pid)); */
////						/* complete_windows.append(wid) */
////						if (firstCompleteWindow > wid)
////							firstCompleteWindow = wid;
////						if (lastCompleteWindow  < wid)
////							lastCompleteWindow  = wid;
////						
////						completeWindowFreePointer = windowBatch.getBuffer().normalise(idx - 1);
////							
////					}
////				} else {
////					/* Check windows that close within this batch at 
////					 * a pane id other than the first one.
////					 * 
////					 *  First, ensure that enough panes have passed. 
////					 */
////					if (wid >= 0) { 
////						if ((opensAt == fp && fpc == false) || opensAt < fp) {
////							iwpb += 1;
////							// System.out.println(String.format("[DBG] window %04d [%10d, %10d] is incomplete", wid, opensAt, _pid));
////							/* incomplete_windows.append(wid) */
//////							api.outputWindowResult(wid, wid, windowBatch.getBuffer().normalise(idx - 1), null, false, true);
////						} else {
////							cwpb += 1;
////							/* System.out.println(String.format("[DBG] window %04d is complete [%4d,%4d]", wid, opensAt, _pid)); */
////							/* complete_windows.append(wid) */
////							if (firstCompleteWindow > wid)
////								firstCompleteWindow = wid;
////							if (lastCompleteWindow  < wid)
////								lastCompleteWindow  = wid;
////							
////							completeWindowFreePointer = windowBatch.getBuffer().normalise(idx - 1);
////						}
////					}
////				}
////				
////				/* Check window that opens at pane `_pid` */
////				wid = _pid / pps;
////				
////				/*
////				 * There is no need to compute complete windows.
////				 * These have been computed in the previous set.
////				 */ 
////				if (closesAt == lp) {
////					/*
////					 * This is a special case. Since we scan panes sequentially, 
////					 * we do not yet if the last pane is closed or not.
////					 * 
////					 * For time-based windows we can never know; but for count-
////					 * based windows it might be the case that the last pane is
////					 * complete.
////					 *
////					 * Nonetheless, we count it as potentially incomplete.
////					 */
////					iwpb += 1;
////					// System.out.println(String.format("[DBG] window %04d [%10d, %10d] is (likely) incomplete", wid, _pid, closesAt));
////					/* likely.append(wid) */
//////					api.outputWindowResult(wid, wid, -1, null, true, false);
////				} else if (closesAt > lp) {
////					iwpb += 1;
////					/* These windows are certainly incomplete */
////					// System.out.println(String.format("[DBG] window %04d [%10d, %10d] is incomplete", wid, _pid, closesAt));
////					/* incomplete_windows.append(wid) */
//////					api.outputWindowResult(wid, wid, -1, null, true, false);
////				}
////				/*
////				 * System.out.println(String.format("[DBG] pane %13d %13d tuples", _pid, tpp));
////				 */
////				tpp = 0; /* Reset tuples/pane counter */
////				_pid = pid;
////			}
////			tpp += 1;
////		}
////		
////		/*
////		 * Once we have processed all tuples in the batch, there is one last pane
////		 * to check (the currently open one).
////		 */
////		if (pid == lp && windowDef.isRowBased() && tpp == _pane) {
////			lpc = true;
////		}
////		if (lpc == false) {
////			iwpb += 1;
////			// System.out.println(String.format("[DBG] window %04d [%10d, %10d] is incomplete", pid / pps, pid, pid + ppw - 1));
//////			api.outputWindowResult(pid / pps, pid / pps, -1, null, true, false);
////		} else {
////			/* 
////			 * Legacy code
////			 * 
////			 * Remove likely incomplete windows
////			 * iwpb -= len(likely)
////			 * cwpb += len(likely)
////			 * Merge the two lists
////			 * complete_windows += likely
////			 * likely = [] 
////			 */
////			
////			/* Close likely incomplete window */
////			wid = (pid - ppw + 1) / pps;
//////			api.outputWindowResult(wid, wid, windowBatch.getBuffer().normalise(idx - 1), null, false, true);
////			
////			/* Check window that opens at pid */ 
////			closesAt = pid + ppw - 1;
////			wid = pid / pps;
////			if (closesAt > lp) {
////				iwpb += 1;
////				// System.out.println(String.format("[DBG] window %04d [%10d, %10d] is incomplete", wid, pid, closesAt));
//////				api.outputWindowResult(pid / pps, pid / pps, -1, null, true, false);
////				/* incomplete_windows.append(wid) */
////			} else {
////				cwpb += 1;
////				/* complete_windows.append(wid) */
////				if (firstCompleteWindow > wid)
////					firstCompleteWindow = wid;
////				if (lastCompleteWindow  < wid)
////					lastCompleteWindow  = wid;
////				
////				completeWindowFreePointer = windowBatch.getBuffer().normalise(idx - 1);
////			}
////		}
//		/*
//		 * System.out.println(String.format("[DBG] pane %13d %13d tuples; complete? %5s", pid, tpp, lpc));
//		 */
//		// System.out.println(String.format("[DBG] %d complete windows; %d incomplete ones (first %5s last %s)", cwpb, iwpb, fpc, lpc));
//		// System.out.println(String.format("[DBG] complete windows [%10d,%10d]", firstCompleteWindow, lastCompleteWindow));
////		api.outputWindowResult(firstCompleteWindow, lastCompleteWindow, completeWindowFreePointer, null, true, true);
//		
//		/* 
//		 * Legacy code
//		 * 
//		 * print "> complete windows:", complete_windows
//		 * print "> incomplete windows:", incomplete_windows, likely 
//		 */
//		
//		// WindowResultList mylist = new WindowResultList();
//		
////		 for (int i = 0; i < 1024; i++) {
////			api.addToList(i);
////			// mylist.add(i);
////		 }
////		
////		for (int i = 0; i < 1024; i++) {
////		 	api.delToList(i);
////		}
//		
////		ArrayList<Long> list = new ArrayList<Long>();
////		for (int i = 0; i < 1024; i++) {
////			list.add((long) i);
////		}
////		
////		for (int i = 0; i < 1024; i++) {
////			list.remove((long) i);
////		}
//		
//		// System.out.println(String.format("[DBG] Task %10d finished; free %10d", windowBatch.getTaskId(), windowBatch.getFreeOffset()));
//		api.outputWindowResult(0, 0, windowBatch.getFreeOffset(), null, false, false);
//	}
//	
//	@Override
//	public void processData(WindowBatch firstWindowBatch, WindowBatch secondWindowBatch, IWindowAPI api) {
//		
//		throw new UnsupportedOperationException("MicroAggregation is single input operator and does not operate on two streams");
//	}
//}
