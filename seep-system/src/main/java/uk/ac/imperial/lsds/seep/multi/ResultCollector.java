package uk.ac.imperial.lsds.seep.multi;

import uk.ac.imperial.lsds.seep.multi.join.JoinResultHandler;

public class ResultCollector {

	public static void forwardAndFree(ResultHandler handler, SubQuery query,
			IQueryBuffer buffer, int taskid, int freeOffset, boolean GPU) {
		
		boolean [] marked = { false };
		
		if (taskid < 0) /* Invalid task id */
			return ;
		int idx = taskid % handler.SLOTS;

		try {
			
			while (! handler.slots.compareAndSet(idx, -1, 0)) {
				/*
				System.err.println("warning: result collector blocked");
			 	Thread.sleep(1L);
				 */
				Thread.yield();
			}
			
			/* System.out.println(String.format("[DBG] %s get  slot qid %d idx %6d", Thread.currentThread(), query.getId(), idx)); */

			handler.offsets[idx] = freeOffset;
			handler.results[idx] = buffer;
			
			/* No other thread can modify this slot. */
			handler.slots.set(idx, 1);
			
			/* Forward and free */
			Integer index = handler.next.get(marked);
			if (! marked[0])
				if (! handler.next.compareAndSet(index, index, false, true))
					return ;
			
			/* No other thread can modify next.
			 * 
			 * Is slot `index` occupied? 
			 */
			int nextone = index.intValue();
			
			if (! handler.slots.compareAndSet(nextone, 1, 2))
				return ;
			
			boolean busy = true;
			
			while (busy) {
				
				/* System.out.println(String.format("[DBG] %s try  slot qid %d idx %6d", Thread.currentThread(), query.getId(), nextone)); */

				IQueryBuffer buf = handler.results[nextone];
				byte [] arr = buf.array();

				/*
				 * Do the actual result forwarding
				 */
				if (query.getDownstreamSubQuery() != null)
					query.getDownstreamSubQuery().getTaskDispatcher().dispatch(arr);
				
				/* Forward to the distributed API */

				/* Release the result buffer */
				buf.release();

				/* Free input buffer */
				int offset = handler.offsets[nextone];
				if (offset != Integer.MIN_VALUE) 
					handler.freeBuffer.free (offset);
				
				/* System.out.println(String.format("[DBG] %s free slot qid %d idx %6d", Thread.currentThread(), query.getId(), nextone)); */
				
				/* Release the current slot */
				handler.slots.set(nextone, -1);
				
				/* Increment next */
				nextone = (nextone + 1) % handler.SLOTS;
				
				/* Check if next is ready to be pushed */
				if (! handler.slots.compareAndSet(nextone, 1, 2)) {
					/* System.out.println(String.format("[DBG] %s next slot %d is unavailable", Thread.currentThread(), nextone)); */
					/* Exit gracefully */
					if (! handler.next.compareAndSet(index, new Integer(nextone), true, false)) {
						System.err.println ("Fatal error.");
						System.exit(1);
					}
					busy = false;
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
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
