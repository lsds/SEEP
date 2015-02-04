package uk.ac.imperial.lsds.seep.multi;

import uk.ac.imperial.lsds.seep.multi.join.JoinResultHandler;

public class ResultCollector {

	public static void forwardAndFree(ResultHandler handler, SubQuery query,
			IQueryBuffer buffer, int taskid, int freeOffset, boolean GPU) {
		
		if (taskid < 0) /* Invalid task id */
			return ;
		
		int idx = taskid % handler.SLOTS;
		int index;
		int next;
		try {

			while (!handler.slots.compareAndSet(idx, 1, 0)) {
				/*
				 * System.err.println("warning: result collector blocked");
				 * Thread.sleep(1L);
				 */
				Thread.yield();
			}

			handler.offsets.set(idx, freeOffset);
			handler.results.set(idx, buffer);
			index = handler.offsets.getAndSet(handler.next, -1);
			// int count = 0;
			while (index != -1) {
				/*
				 * Do the actual result forwarding
				 */
				if (query.getDownstreamSubQuery() != null) {
					IQueryBuffer tBuffer = handler.results.get(handler.next);
					if (tBuffer == null)
						System.err.println(String.format("error (1): query %d index %d handler.next %d", query.getId(), index, handler.next));
					byte [] t = tBuffer.array();
					if (t == null) {
						System.err.println(String.format("error (2): query %d index %d handler.next %d", query.getId(), index, handler.next));
						System.exit(1);
					} else {
						query.getDownstreamSubQuery().getTaskDispatcher()
							.dispatch(t);
//						System.err.println(String.format("[BDG] release buffer %s query %d index %d handler.next %d", 
//								tBuffer, query.getId(), index, handler.next));
						tBuffer.release();
					}
				}
				else {
					/*
					 * Push to distributed API
					 */
					IQueryBuffer tBuffer = handler.results.get(handler.next);
					if (tBuffer == null)
						System.err.println(String.format("error: query %d index %d handler.next %d", query.getId(), index, handler.next));
					tBuffer.release();
				}
				
				// count ++;
				if (index != Integer.MIN_VALUE)
					handler.freeBuffer.free(index);
				handler.slots.lazySet(handler.next, 1);
				next = (handler.next + 1) % handler.SLOTS;
				index = handler.offsets.getAndSet(next, -1);
				handler.next = next;
			}
			// if (count > 0)
			//	System.out.println (String.format("[DBG] task %4d count %10d", taskid, count));
			
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
