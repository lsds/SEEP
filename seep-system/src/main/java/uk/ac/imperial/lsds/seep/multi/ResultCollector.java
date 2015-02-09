package uk.ac.imperial.lsds.seep.multi;

import uk.ac.imperial.lsds.seep.multi.join.JoinResultHandler;

public class ResultCollector {

	public static void forwardAndFree(ResultHandler handler, SubQuery query,
			IQueryBuffer buffer, int taskid, int freeOffset, boolean GPU) {
		
		if (taskid < 0) /* Invalid task id */
			return ;
		int idx = taskid % handler.SLOTS;

		try {
			
			while (! handler.slots.compareAndSet(idx, -1, 0)) {
				
				System.err.println(String.format("warning: result collector blocked at %s q %d t %4d", 
				Thread.currentThread(), query.getId(), taskid));
				System.err.flush();
			 	// Thread.sleep(100L);
				
				Thread.yield();
			}
			
			/* System.out.println(String.format("[DBG] %s get  slot qid %d idx %6d", 
			 * Thread.currentThread(), query.getId(), idx)); */

			handler.offsets[idx] = freeOffset;
			handler.results[idx] = buffer;
			
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
			
			int count = 0;
			
			while (busy) {
				
				/* System.out.println(String.format("[DBG] %s try  slot qid %d idx %6d", 
				 * Thread.currentThread(), query.getId(), nextone)); */

				IQueryBuffer buf = handler.results[handler.next];
				byte [] arr = buf.array();

				/*
				 * Do the actual result forwarding
				 */
				if (query.getDownstreamSubQuery() != null)
					query.getDownstreamSubQuery().getTaskDispatcher().dispatch(arr);
				
				/* Forward to the distributed API */

				/* Measure latency */
//				if (query.getId() == 0)
//					latencyMeasurement (buf);
				
				buf.release();

				/* Free input buffer */
				int offset = handler.offsets[handler.next];
				if (offset != Integer.MIN_VALUE) 
					handler.freeBuffer.free (offset);
				
				/* System.out.println(String.format("[DBG] %s free slot qid %d idx %6d", 
				 * Thread.currentThread(), query.getId(), nextone)); */
				
				/* Release the current slot */
				handler.slots.set(handler.next, -1);
				
				/* Increment next */
				handler.next = (handler.next + 1) % handler.SLOTS;
				
				count ++;
				
				/* Check if next is ready to be pushed */
				if (! handler.slots.compareAndSet(handler.next, 1, 2)) {
					busy = false;
				}
			}
			/* Thread exit critical section */
//			 System.out.println(String.format("[DBG] %s released %3d q%d buffers", 
//			  Thread.currentThread(), count, query.getId()));
			handler.semaphore.release();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void latencyMeasurement(IQueryBuffer buf) {
		long _t = buf.getLong(0);
		long t_ = System.nanoTime();
		long dt = t_ - _t;
		System.out.println(String.format("In result collector: latency %12d ns", dt));
	}

	public static void forwardAndFree(JoinResultHandler handler, SubQuery query,
			IQueryBuffer resultBuffer, int taskid, int firstOffset, int secondOffset) {
		int idx = taskid % handler.SLOTS;
		int index;
		int secondIndex;
		int next;
		try {

			while (!handler.slots.compareAndSet(idx, 1, 0)) {
				/*
				 * System.err.println("warning: result collector blocked");
				 * Thread.sleep(1L);
				 */
				Thread.yield();
			}

			handler.firstOffsets.set(idx, firstOffset);
			handler.secondOffsets.set(idx, secondOffset);
			handler.results.set(idx, resultBuffer);
			index = handler.firstOffsets.getAndSet(handler.next, -1);
			while (index != -1) {
				/*
				 * Do the actual result forwarding
				 */
				if (query.getDownstreamSubQuery() != null)
					query.getDownstreamSubQuery().getTaskDispatcher()
							.dispatch(handler.results.get(handler.next).array());

				if (index != Integer.MIN_VALUE)
					handler.firstFreeBuffer.free(index);
				
				secondIndex = handler.secondOffsets.get(handler.next);
				if (secondIndex != Integer.MIN_VALUE)
					handler.secondFreeBuffer.free(secondIndex);
				
				handler.slots.lazySet(handler.next, 1);
				next = (handler.next + 1) % handler.SLOTS;
				index = handler.firstOffsets.getAndSet(next, -1);
				handler.next = next;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
}
