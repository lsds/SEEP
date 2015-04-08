package uk.ac.imperial.lsds.seep.multi;

class TaskWindow {
	
	public ITask pred, curr;
	
	public TaskWindow (ITask pred, ITask curr) {
		this.pred = pred;
		this.curr = curr;
	}
	
	public static TaskWindow find (ITask head, int taskid) {
		ITask pred = null;
		ITask curr = null;
		ITask succ = null;
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
				if ((curr.taskid >= taskid))
					return new TaskWindow (pred, curr);
				pred = curr;
				curr = succ;
			}
		}
	}

	public static TaskWindow findTail (ITask head) {
		ITask pred = null;
		ITask curr = null;
		ITask succ = null;
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
				if ((curr.taskid >= Integer.MAX_VALUE))
					return new TaskWindow (pred, curr);
				pred = curr;
				curr = succ;
			}
		}
	}

	public static TaskWindow findHead(ITask head) {
		ITask pred = null;
		ITask curr = null;
		ITask succ = null;
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
				return new TaskWindow (pred, curr);
			}
		}
	}

	public static TaskWindow findNext(ITask head, int taskid1, int taskid2) {
		ITask pred = null;
		ITask curr = null;
		ITask succ = null;
		boolean [] marked = { false };
		boolean snip;
		retry: while (true) {
			pred = head;
			curr = pred.next.getReference();
			if (curr.taskid == Integer.MAX_VALUE)
				return new TaskWindow (pred, curr);
			while (true) {
				succ = curr.next.get(marked);
				while (marked[0]) {
					snip = pred.next.compareAndSet(curr, succ, false, false);
					if (! snip)
						continue retry;
					curr = pred.next.getReference();
					succ = curr.next.get(marked);
				}
				if ((curr.queryid == 0 && curr.taskid >= taskid1) || (curr.queryid == 1 && curr.taskid >= taskid2) || curr.taskid == Integer.MAX_VALUE)
					return new TaskWindow (pred, curr);
				pred = curr;
				curr = succ;
			}
		}
	}
	
	
	public static TaskWindow findNextSkipCost(ITask head, int[][] policy, int p) {
		ITask pred = null;
		ITask curr = null;
		ITask succ = null;
		boolean [] marked = { false };
		boolean snip;
		int _p = (p + 1) % 2; /* The other processor */
		double skip_cost = 0.;
		retry: while (true) {
			pred = head;
			curr = pred.next.getReference();
			if (curr.taskid == Integer.MAX_VALUE)
				return new TaskWindow (pred, curr);
			while (true) {
				succ = curr.next.get(marked);
				while (marked[0]) {
					snip = pred.next.compareAndSet(curr, succ, false, false);
					if (! snip)
						continue retry;
					curr = pred.next.getReference();
					succ = curr.next.get(marked);
				}
				
				if (curr.taskid == Integer.MAX_VALUE)
					return new TaskWindow (pred, curr);
				
				if (
						policy[p][curr.queryid] >= policy[_p][curr.queryid] || 
						(
							(skip_cost >= 1. / (double) policy[p][curr.queryid])
						)
					) {
					return new TaskWindow (pred, curr);
				}
				
				skip_cost += 1. / (double) policy[_p][curr.queryid];
				
				pred = curr;
				curr = succ;
			}
		}
	}
}

