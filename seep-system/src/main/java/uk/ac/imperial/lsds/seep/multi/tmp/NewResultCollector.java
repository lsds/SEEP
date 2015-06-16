package uk.ac.imperial.lsds.seep.multi.tmp;

import java.util.concurrent.locks.LockSupport;

import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ResultHandler;
import uk.ac.imperial.lsds.seep.multi.SubQuery;
import uk.ac.imperial.lsds.seep.multi.join.JoinResultHandler;


public class NewResultCollector {

	public static void forwardAndFree (NewResultHandler handler, SubQuery query, IQueryBuffer buffer, 
			int taskid, int freeOffset, int latencyMark, boolean GPU, boolean isRStream) {
		
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
			
			if (! isRStream) {
				
				handler.slots.set(idx, 3);
			
			} else {
			
				/* Dummy aggregation of partial results */
			
				/* Assume that a window spans N = 4 batches.
				 * 
				 * 
				 */
				
				// System.out.println("______task index is " + idx);
				
				int closingTask = handler.next + 127;
				closingTask = closingTask % handler.SLOTS;
				if (closingTask == idx) {
					// System.out.println(String.format("[DBG] task id is %10d; idx is %6d", taskid, idx));
					/* Close this task */
					handler.slots.set(idx, 3);
					
					UnsafeIntMap w = new UnsafeIntMap (0, 0);
					IQueryBuffer b = handler.results[idx];
					for (int pos = 0; pos < b.position(); pos += 16) {
						int k = b.getInt(pos + 8);
						float v = b.getFloat(pos + 12);
						// System.out.println(String.format("[DBG] k = %10d v = %10.1f", k, v));
						w.put(k, (int) v);
					}
					
					/* Look backwards, starting from handler.next */
					int firstTask = handler.next;
					while (firstTask < closingTask) {
						
						if (! handler.slots.compareAndSet(firstTask, 1, 3)) {
							handler.semaphore.release();
							return ;
						}
						
						/* Aggregate results */
						// UnsafeIntMap intermediate = new UnsafeIntMap (0, 0);
						IQueryBuffer d = handler.results[firstTask];
						for (int pos = 0; pos < d.position(); pos += 16) {
							int k = d.getInt(pos + 8);
							float v = d.getFloat(pos + 12);
							/* Merge the results of intermediate with key */
							w.update(k,  (int) v);
						}
						
						firstTask = firstTask + 1;
						firstTask = firstTask % handler.SLOTS;
					}
					/* Write hash table \w\ to output stream */
					b.clear();
					b.position(0);
					
					UnsafeIntMapEntry [] entries = w.getEntries();
					
					for (int k = 0; k < entries.length; k++) {
						
						UnsafeIntMapEntry e = entries[k];
						
						while (e != null) {
						
							b.putLong(0);
							b.putInt(e.key);
							b.putFloat((float) e.value);
							
							e = e.next;
						}
					}
					// System.out.println(String.format("[DBG] new position is %d", b.position()));
				}
			}
			
			/* Is slot `index` occupied? 
			 */
			if (! handler.slots.compareAndSet(handler.next, 3, 2)) {
				handler.semaphore.release();
				return ;
			}
			
			boolean busy = true;
			
			while (busy) {

				IQueryBuffer buf = handler.results[handler.next];
				byte [] arr = buf.array();
				
				// System.out.println(String.format("[DBG] free %6d (%16d bytes)", handler.next, buf.position()));
				
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
								
								return;
							}
						}
					}
				}
				
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
				
				if (! handler.slots.compareAndSet(handler.next, 3, 2)) {
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
		
		// System.out.println(String.format("[DBG] task %d free offsets 1/%d 2/%d", taskid, freeOffset1, freeOffset2));
		
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
				// buf.close();
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
								result = query.getDownstreamSubQuery(i).getTaskDispatcher().tryDispatchFirst( arr, buf.position()); // arr.length);
							} else {
								result = query.getDownstreamSubQuery(i).getTaskDispatcher().tryDispatchSecond(arr, buf.position()); // arr.length);
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
