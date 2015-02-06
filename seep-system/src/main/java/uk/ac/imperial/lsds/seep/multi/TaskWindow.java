package uk.ac.imperial.lsds.seep.multi;

class TaskWindow {
	
	public Task pred, curr;
	
	public TaskWindow (Task pred, Task curr) {
		this.pred = pred;
		this.curr = curr;
	}
	
	public static TaskWindow find (Task head, int taskid) {
		Task pred = null;
		Task curr = null;
		Task succ = null;
		boolean [] marked = { false };
		boolean snip;
		retry: while (true) {
			pred = head;
			curr = pred.next.getReference();
			while (true) {
				succ = curr.next.get(marked);
				while (marked[0]) {
					snip = pred.next.compareAndSet(curr, succ, false, false);
					if (! snip)
						continue retry;
					curr = pred.next.getReference();
					succ = curr.next.get(marked);
				}
				if (curr.taskid >= taskid)
					return new TaskWindow (pred, curr);
				pred = curr;
				curr = succ;
			}
		}
	}
}

