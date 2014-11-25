package uk.ac.imperial.lsds.seep.multi;
public class ResultCollector {
	
	public static void free (ResultHandler handler, IQueryBuffer buffer, int taskid, int offset) {
		int idx = taskid % handler.SLOTS;
		int index;
		int next;
		/* int upto = -1; */
		try {
			
			while (! handler.slots.compareAndSet(idx, 1, 0))
			{
				/* System.err.println("warning: result collector blocked");
				Thread.sleep(1L); */
				Thread.yield();
			}
		
			handler.offsets.set (idx, offset);
			index = handler.offsets.getAndSet (handler.next, -1);
			while (index > 0)
			{
				buffer.free (index);
				/* upto = index; */
				handler.slots.lazySet (handler.next, 1);
				next = (handler.next + 1) % handler.SLOTS;
				index = handler.offsets.getAndSet (next, -1);
				handler.next = next;
			}
			/* if (upto < 0)
				return ;
			buffer.free(upto); */
		} catch (Exception e) { e.printStackTrace(); } 
	}
}
