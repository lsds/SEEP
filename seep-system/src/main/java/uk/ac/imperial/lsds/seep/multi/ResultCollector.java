package uk.ac.imperial.lsds.seep.multi;

import uk.ac.imperial.lsds.seep.multi.join.JoinResultHandler;

public class ResultCollector {

	public static void forwardAndFree(ResultHandler handler, SubQuery query,
			IQueryBuffer buffer, int taskid, int freeOffset) {
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
			while (index > 0) {
				/*
				 * Do the actual result forwarding
				 */
				if (query.getDownstreamSubQuery() != null)
					query.getDownstreamSubQuery().getTaskDispatcher()
							.dispatch(handler.results.get(index).array());

				handler.freeBuffer.free(index);
				handler.slots.lazySet(handler.next, 1);
				next = (handler.next + 1) % handler.SLOTS;
				index = handler.offsets.getAndSet(next, -1);
				handler.next = next;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void forwardAndFree(JoinResultHandler handler, SubQuery query,
			IQueryBuffer resultBuffer, int taskid, int firstOffset, int secondOffset) {
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

			handler.firstOffsets.set(idx, firstOffset);
			handler.secondOffsets.set(idx, secondOffset);
			handler.results.set(idx, resultBuffer);
			index = handler.firstOffsets.getAndSet(handler.next, -1);
			while (index > 0) {
				/*
				 * Do the actual result forwarding
				 */
				if (query.getDownstreamSubQuery() != null)
					query.getDownstreamSubQuery().getTaskDispatcher()
							.dispatch(handler.results.get(index).array());

				handler.firstFreeBuffer.free(index);
				handler.secondFreeBuffer.free(handler.secondOffsets.get(handler.next));
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
