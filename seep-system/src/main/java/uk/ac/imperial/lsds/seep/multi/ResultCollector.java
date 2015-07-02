package uk.ac.imperial.lsds.seep.multi;

import java.util.concurrent.locks.LockSupport;

import uk.ac.imperial.lsds.seep.multi.join.JoinResultHandler;

public class ResultCollector {
	
	public static void forwardAndFree (ResultHandler handler, SubQuery query, IQueryBuffer buffer, 
			int taskid, int freeOffset, int latencyMark, boolean GPU) {
		
		if (taskid < 0) { /* Invalid task id */
			return ;
		}
		int idx = ((taskid - 1) % handler.SLOTS);
		
		try {
			
			while (! handler.slots.compareAndSet(idx, -1, 0)) {
				
				System.err.println(String.format("warning: result collector blocked at %s q %d t %4d idx %4d", 
						Thread.currentThread(), query.getId(), taskid, idx));
				LockSupport.parkNanos(1L);
			}
			
			handler.offsets[idx] = freeOffset;
			handler.results[idx] = buffer;
			
			handler.latch [idx] = 0;
			handler.mark  [idx] = latencyMark;
			
			/* No other thread can modify this slot. */
			handler.slots.set(idx, 1);
			
			/* Forward and free */
			
			if (! handler.semaphore.tryAcquire())
				return;
			
			/* No other thread can enter this section */
			
			/* Is slot `index` occupied? 
			 */
			if (! handler.slots.compareAndSet(handler.next, 1, 2)) {
				handler.semaphore.release();
				return ;
			}
			
			boolean busy = true;
			
			while (busy) {

				IQueryBuffer buf = handler.results[handler.next];
				byte [] arr = buf.array();
				
				if (arr == null || buf.position() == 0) {
					System.err.println("warning: output of task " + handler.next + " is empty");
					// System.exit(1);
				} else {
				
				/*
				 * Do the actual result forwarding
				 */
				if (query.getNumberOfDownstreamSubQueries() > 0) {
					int pos = handler.latch[handler.next];
					for (int i = pos; i < query.getNumberOfDownstreamSubQueries(); i++) {
						if (query.getDownstreamSubQuery(i) != null) {
							boolean result = false;
							if (query.isLeft()) {
								result = query.getDownstreamSubQuery(i).getTaskDispatcher().tryDispatchFirst( arr, buf.position()); // arr.length);
							} else {
								result = query.getDownstreamSubQuery(i).getTaskDispatcher().tryDispatchSecond(arr, buf.position()); // arr.length);
							}
							if (! result) {
								handler.latch[handler.next] = i;
								handler.slots.set(handler.next, 1);
								/* We need to release the semaphore as well, right? */
								handler.semaphore.release();
								return;
							}
						}
					}
				}
				
				} // non-empty array
				
				/* Forward to the distributed API */

				/* Measure latency */
				if (handler.mark[handler.next] != -1)
					query.getLatencyMonitor().monitor(handler.freeBuffer, handler.mark[handler.next]);
				
				/* Before releasing the buffer, count how many bytes are in the output.
				 * 
				 * It is important that all operators set the position of the buffer accordingly.
				 * 
				 * The assumption is that `buf` is an intermediate buffer and that the start
				 * position is 0.
				 */
				
				// System.out.println(String.format("[DBG] output %10d bytes", buf.position()));
				
				handler.incTotalOutputBytes(buf.position());
				buf.release();

				/* Free input buffer */
				int offset = handler.offsets[handler.next];
				if (offset != Integer.MIN_VALUE) {
					
					handler.freeBuffer.free (offset);
				} else {
					System.err.println(String.format("[DBG] %s skip slot qid %d idx %6d", 
							Thread.currentThread(), query.getId(), handler.next));
					System.exit(1);
				}
				
				/* Release the current slot */
				handler.slots.set(handler.next, -1);
				
				/* Increment next */
				handler.next = handler.next + 1;
				handler.next = handler.next % handler.SLOTS;
				
				/* Check if next is ready to be pushed */
				
				if (! handler.slots.compareAndSet(handler.next, 1, 2)) {
					busy = false;
				 }
				
			}
			/* Thread exit critical section */
			handler.semaphore.release();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void forwardAndFree (
		JoinResultHandler handler, 
		SubQuery query,
		IQueryBuffer buffer, 
		int taskid, 
		int freeOffset1, 
		int freeOffset2,
		int latencyMark
	) {
		
		/* System.out.println(String.format("[DBG] task %d free offsets 1/%d 2/%d", 
		 * taskid, freeOffset1, freeOffset2)); */
		
		if (taskid < 0) { /* Invalid task id */
			return ;
		}
		
		int idx = ((taskid - 1) % handler.SLOTS);
		
		try {
			
			while (! handler.slots.compareAndSet(idx, -1, 0)) {
				
				System.err.println(String.format("warning: result collector blocked at %s q %d t %4d idx %4d", 
						Thread.currentThread(), query.getId(), taskid, idx));
				LockSupport.parkNanos(1L);
			}
			
			handler.firstOffsets[idx] =  freeOffset1;
			handler.secondOffsets[idx] = freeOffset2;
			
			handler.results[idx] = buffer;
			
			handler.latch [idx] = 0; 
			handler.mark  [idx] = latencyMark;
			
			/* No other thread can modify this slot. */
			handler.slots.set(idx, 1);
			
			/* Forward and free */
			
			if (! handler.semaphore.tryAcquire())
				return;
			
			/* No other thread can enter this section */
			
			/* Is slot `index` occupied? 
			 */
			if (! handler.slots.compareAndSet(handler.next, 1, 2)) {
				handler.semaphore.release();
				return ;
			}
			
			boolean busy = true;
			
			while (busy) {

				IQueryBuffer buf = handler.results[handler.next];
				byte [] arr = buf.array();
				
				/*
				 * Do the actual result forwarding
				 */
				if (query.getNumberOfDownstreamSubQueries() > 0) {
					int pos = handler.latch[handler.next];
					for (int i = pos; i < query.getNumberOfDownstreamSubQueries(); i++) {
						if (query.getDownstreamSubQuery(i) != null) {
							boolean result = false;
							if (query.isLeft()) {
								result = query.getDownstreamSubQuery(i).getTaskDispatcher().tryDispatchFirst( arr, buf.position());
							} else {
								result = query.getDownstreamSubQuery(i).getTaskDispatcher().tryDispatchSecond(arr, buf.position());
							}
							if (! result) {
								handler.latch[handler.next] = i;
								handler.slots.set(handler.next, 1);
								
								return;
							}
						}
					}
				}
				
				/* Forward to the distributed API */

				/* Measure latency */
				if (handler.mark[handler.next] != -1)
					query.getLatencyMonitor().monitor(handler.firstFreeBuffer, handler.mark[handler.next]);
				
				/* Before releasing the buffer, count how many bytes are in the output.
				 * 
				 * It is important that all operators set the position of the buffer accordingly.
				 * 
				 * The assumption is that `buf` is an intermediate buffer and that the start
				 * position is 0.
				 */
				handler.incTotalOutputBytes(buf.position());
				buf.release();
				
				/* Free first input buffer */
				int offset1 = handler.firstOffsets[handler.next];
				if (offset1 != Integer.MIN_VALUE) {
					handler.firstFreeBuffer.free (offset1);
				}
				
				/* Free second input buffer */
				int offset2 = handler.secondOffsets[handler.next];
				if (offset2 != Integer.MIN_VALUE) {
					handler.secondFreeBuffer.free (offset2);
				}
				
				/* Release the current slot */
				handler.slots.set(handler.next, -1);
				
				/* Increment next */
				handler.next = handler.next + 1;
				handler.next = handler.next % handler.SLOTS;
				
				/* Check if next is ready to be pushed */
				
				if (! handler.slots.compareAndSet(handler.next, 1, 2)) {
					busy = false;
				 }
				
			}
			/* Thread exit critical section */
			handler.semaphore.release();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
}
