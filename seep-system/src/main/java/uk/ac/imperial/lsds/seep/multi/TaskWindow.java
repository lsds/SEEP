package uk.ac.imperial.lsds.seep.multi;

import java.util.concurrent.atomic.AtomicInteger;

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
				if ((curr.taskid >= taskid))
					return new TaskWindow (pred, curr);
				pred = curr;
				curr = succ;
			}
		}
	}

	public static TaskWindow findTail (Task head) {
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
				if ((curr.taskid >= Integer.MAX_VALUE))
					return new TaskWindow (pred, curr);
				pred = curr;
				curr = succ;
			}
		}
	}

	public static TaskWindow findHead(Task head) {
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
				return new TaskWindow (pred, curr);
			}
		}
	}

	public static TaskWindow findNext(Task head, int taskid1, int taskid2) {
		Task pred = null;
		Task curr = null;
		Task succ = null;
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
	
	
	public static TaskWindow findNextSkipCost(Task head, int[][] policy, int p, AtomicInteger [][] offsets) {
		Task pred = null;
		Task curr = null;
		Task succ = null;
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
								/* (curr.taskid - offsets[_p][curr.queryid].get() > 20) && */ 
								(skip_cost >= 1. / (double) policy[p][curr.queryid])
						)
					) {
					// System.out.println(String.format("[FND] policy [%d][%4d] = %2d policy[%d][%4d] = %2d cost %1.2f (taskid %4d)", 
					// 		p, curr.queryid, policy[p][curr.queryid], _p, curr.queryid, policy[_p][curr.queryid], skip_cost, curr.taskid));
					return new TaskWindow (pred, curr);
				}

				// skip_cost += 1.0 / policy[p][curr.queryid];
				skip_cost += 1. / (double) policy[_p][curr.queryid];
				
				pred = curr;
				curr = succ;
			}
		}
	}
}

