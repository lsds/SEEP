package uk.ac.imperial.lsds.seep.multi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Pusher implements Runnable {
	
	
	private ResultHandler handler;
	private SubQuery query;
	
	private boolean stop = false;

		
	public Pusher (ResultHandler handler, SubQuery query) {
		this.handler = handler;
		this.query = query;
	}
	
	public void stopWorking () {
		stop = true;
	}
	
	@Override
	public void run () {
		
		while (! stop) {
		
			/* Forward and free */
		
			/* Is slot `index` occupied? 
			 */
		
			if (! handler.slots.compareAndSet(handler.next, 1, 2)) {
				continue;
			}
		
			boolean busy = true;
		
			int count = 0;
		
			while (busy) {
			
//				System.out.println(String.format("[DBG] %s try  slot qid %d idx %6d", 
//				Thread.currentThread(), query.getId(), nextone)); 

				IQueryBuffer buf = handler.results[handler.next];
				byte [] arr = buf.array();

				/*
				 * Do the actual result forwarding
				 */
				if (query.getDownstreamSubQuery() != null) {
//					System.out.println(String.format("[DBG] %s try free qid %d idx %6d", 
//					Thread.currentThread(), query.getId(), handler.next));
					// if (! query.getDownstreamSubQuery().getTaskDispatcher().tryDispatch(arr)) {
//						System.err.println(String.format("[DBG] %s failed to free qid %d idx %6d", 
//						Thread.currentThread(), query.getId(), handler.next));
						// handler.slots.set(handler.next, 1);
						// break;
					// }
					// query.getDownstreamSubQuery().getTaskDispatcher().dispatch(arr);
					
					query.getDownstreamSubQuery().getTaskDispatcher().dispatch(arr);
				}
				
				/* Forward to the distributed API */

				/* Measure latency */
//				if (query.getId() == 0)
//					latencyMeasurement (buf);
			
//					System.out.println(String.format("[DBG] %s free slot qid %d idx %6d", 
//					Thread.currentThread(), query.getId(), handler.next));
			
				// query.getLatencyMonitor().monitor(buf);
			
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
			
//				if (query.getId() ==  1)
//					System.out.println(String.format("[DBG] %s free slot qid %d idx %6d", 
//					Thread.currentThread(), query.getId(), handler.next));
			
				/* Release the current slot */
				handler.slots.set(handler.next, -1);
			
				/* Increment next */
				// handler.next = (handler.next + 1) % handler.SLOTS;
				handler.next = handler.next + 1;
				handler.next = handler.next % handler.SLOTS;
				// if (handler.wraps > 0)
				//	handler.next += 1;
				//
				//if (handler.next == 0) {
				//	handler.wraps ++;
				//	handler.next += 1; /* We avoid zero */
				//}
			
				// if (taskid >= handler.SLOTS)
				//	handler.next += 1;
				// if (handler.next == 0)
				//	handler.next ++;
			
				count ++;
			
				/* Check if next is ready to be pushed */
				if (! handler.slots.compareAndSet(handler.next, 1, 2)) {
					busy = false;
				}
			}
			/* Thread exit critical section */
//			if (count > 0) 
//				System.out.println(String.format("[DBG] %60s released %3d q%d buffers", Thread.currentThread(), count, query.getId()));
		// handler.semaphore.release();
		}
	}
}
