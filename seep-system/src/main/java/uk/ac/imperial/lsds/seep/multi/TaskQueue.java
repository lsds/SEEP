package uk.ac.imperial.lsds.seep.multi;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicInteger;

/* 
 * Based on the non-blocking queue of M. Herlihy and N. Shavit
 * ("The Art of Multiprocessor programming").
 */

public class TaskQueue {
	
	ConcurrentLinkedQueue<Task> queue;
	
	public TaskQueue (int p, int q) {
		queue = new ConcurrentLinkedQueue<Task>();
	}
	
	public boolean add (Task task) {
		return queue.add(task);
	}
	
	private AtomicBoolean _switch = new AtomicBoolean(false);
	public Task raul_s_poll (int [][] policy, int p, int q) {
		if(_switch.get()){
			_switch.set(false);
			synchronized(this){
				return queue.poll();
			}
		} else{
			_switch.set(true);
			return queue.poll();
		}
	}
	
	public synchronized Task synch_poll (int [][] policy, int p, int q) {
		return queue.poll();
	}
	
	public Task poll (int [][] policy, int p, int q) {
		return queue.poll();
	}
	
	public int size () { return queue.size(); }
	
	
//	private AtomicInteger [][] offsets;
//	private AtomicInteger min;
//	
//	private Task head;
//	
//	public TaskQueue (int p, int q) {
//		offsets = new AtomicInteger [p][q];
//		for (int i = 0; i < p; i++)
//			for (int j = 0; j < q; j++)
//				offsets[i][j] = new AtomicInteger(0);
//		min = new AtomicInteger(Integer.MAX_VALUE);
//		head = new Task ();
//		Task tail = new Task (null, null, null, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
//		while (! head.next.compareAndSet(null, tail, false, false));
//	}
//	
//	/* Lock-free */ /* Insert sorted */
////	public boolean add (Task task) {
////		// System.out.println("[DBG] add " + task.queryid + ":" + task.taskid);
////		int taskid = task.taskid;
////		while (true) {
////			TaskWindow window = TaskWindow.find(head, taskid);
////			Task pred = window.pred;
////			Task curr = window.curr;
////			if (curr.taskid == taskid) {
////				return false;
////			} else {
////				task.next.set(curr, false);
////				if (pred.next.compareAndSet(curr, task, false, false)) {
////					return true; 
////				}
////			}
////		}
////	}
//	
//	/* Lock-free */ /* Insert at the end of the queue */
//	public boolean add (Task task) {
//		while (true) {
//			TaskWindow window = TaskWindow.findTail(head);
//			Task pred = window.pred;
//			Task curr = window.curr;
//			if (curr.taskid != Integer.MAX_VALUE) {
//				return false;
//			} else {
//				task.next.set(curr, false);
//				if (pred.next.compareAndSet(curr, task, false, false)) {
//					return true; 
//				}
//			}
//		}
//	}
//	
//	/* Lock-free */
//	public Task remove (int taskid) {
//		boolean snip;
//		while (true) {
//			TaskWindow window = TaskWindow.find(head, taskid);
//			/* Returns pred and curr */
//			Task pred = window.pred;
//			Task curr = window.curr;
//			/* Check if curr matches taskid */
//			if (curr.taskid != taskid) {
//				return null;
//			} else {
//				/* Mark curr as logically removed */
//				Task succ = curr.next.getReference();
//				snip = curr.next.attemptMark(succ, true); 
//				if (!snip)
//					continue;
//				pred.next.compareAndSet(curr, succ, false, false); 
//				return curr;
//			}
//		}
//	}
//	
//	/* Wait-free */
//	public boolean containsTask (int taskid) {
//		boolean [] marked = { false };
//		Task curr = head;
//		while (curr.taskid < taskid) {
//			curr = curr.next.getReference();
//			Task succ = curr.next.get(marked);
//		}
//		return (curr.taskid == taskid && !marked[0]);
//	}
//	
//	/* Wait-free, but approximate */
//	public void dump () {
//		boolean [] marked = { false };
//		int count = 0;
//		System.out.print("Q: ");
//		Task t;
//		for (t = head.next.getReference(); t != null && !marked[0]; t = t.next.get(marked)) {
//			if (t.taskid < Integer.MAX_VALUE) {
//		 		System.out.print(String.format("%s ", t.toString()));
//				count ++;
//			}
//		}
//		System.out.println(String.format("(%d tasks)", count));
//	}
//	
//	/* 
//	 * Policies:
//	 * 
//	 * Q1: [20] [ 5] => [1] [4]
//	 * Q2: [ 4] [12] => [3] [1]
//	 *      P1   P2
//	 */
//	
//	/* Return the first task (taskid, because this is a try) 
//	 * of the first query that is available to processor `p`.
//	 */
//	public int tryNext (int [][] policy, int p) {
//		return 0;
//	}
//	
//	/* Return the first query (queryid, because this is a try)
//	 * whose task is available to processor `p`.
//	 */
//	public int tryNextQuery (int [][] policy, int p) {	
//		return -1;
//	}
//	
//	/* Return the first task (taskid, because this is a try)
//	 * of query `q` that is available to processor `p`.
//	 */
//	public int tryNextTask (int [][] policy, int p, int q) {
//		boolean [] marked = { false };
//		Task curr = head;
//		while (curr.taskid < Integer.MAX_VALUE) {
//			curr = curr.next.getReference();
//			Task succ = curr.next.get(marked);
//			if (curr.taskid >= policy[p][q] && !marked[0]) {
//				return curr.taskid;
//			}
//		}
//		return -1;
//	}
//	
//	public Task getNextTask (int [][] policy, int p, int q) {
//		boolean snip;
//		int _p = (p + 1) % 2; /* The other processor */
//		while (true) {
//			/* Estimate task id */
//			int offset;
//			int taskid1 = 1, taskid2 = 1;
//			if (p != 0) {
//				taskid1 = offsets[0][0].get() + 100;
//				taskid2 = offsets[0][1].get() + 100;
//				// return null;
//			}
//			//if (p == 1)
//			//	taskid = policy[p][q] + offsets[_p][q].get();
//			//else
//			//	taskid = 1;
//			// int skip = policy[p][q]; /* 1 if p the fastest processor for q */
//			// if (skip > 1 && policy.length > 1) {
//				// offset = Math.min(offsets[_p][q].get(), offsets[p][q].get());
//			// 	offset = offsets[_p][q].get();
//			// } else {
//			//	offset = offsets[p][q].get();
//			//}
//			/* Last task that _p took */
//			/* If the p is faster that _p, then p processes the first available task. */
//			// int taskid = (skip == 1) ? offset : (offset + skip);
//			// System.out.println(String.format("[DBG] p %d looks for task %d", p, taskid));
//			
//			/* The find() method takes a head node and a `taskid`, 
//			 * and traverses the list, seeking to set pred to the 
//			 * task with the largest id less than `taskid`, and curr 
//			 * to the task with the least id greater than or equal to 
//			 * `taskid`.
//			 * 
//			 * Returns pred and curr.
//			 */
//			TaskWindow window;
//			if (p == 0)
//				window = TaskWindow.findHead(head);
//			else {
//				// System.out.println(String.format("[DBG] p %d findNext (%d, %d)", p, taskid1, taskid2));
//				window = TaskWindow.findNext(head, taskid1, taskid2);
//			}
//			Task pred = window.pred;
//			Task curr = window.curr;
//			// System.out.println(String.format("[DBG] p %2d pred %3d curr %3d", p, pred.taskid, curr.taskid));
//			/* Check if curr is not the tail of the queue */
//			if (curr.taskid == Integer.MAX_VALUE) {
//				return null;
//			} else {
//				/* Mark curr as logically removed */
//				Task succ = curr.next.getReference();
//				// snip = curr.next.attemptMark(succ, true);
//				snip = curr.next.compareAndSet(succ, succ, false, true);
//				if (!snip)
//					continue;
//				pred.next.compareAndSet(curr, succ, false, false); 
//				/* Nodes are rewired */
//				offsets[p][q].lazySet(curr.taskid);
//				// System.out.println(String.format("[DBG] p %2d runs task %3d from query %d", p, curr.taskid, curr.queryid));
//				return curr;
//			}
//		}
//	}
//	
//	public Task getFirstTask (int [][] policy, int p, int q) {
//		boolean snip;
//		while (true) {
//			/* The find() method takes a head node and a `taskid`, 
//			 * and traverses the list, seeking to set pred to the 
//			 * task with the largest id less than `taskid`, and curr 
//			 * to the task with the least id greater than or equal to 
//			 * `taskid`.
//			 * 
//			 * Returns pred and curr.
//			 */
//			TaskWindow window = TaskWindow.findHead(head);
//			Task pred = window.pred;
//			Task curr = window.curr;
//			// System.out.println(String.format("[DBG] p %2d pred %3d curr %3d", p, pred.taskid, curr.taskid));
//			/* Check if curr is not the tail of the queue */
//			if (curr.taskid == Integer.MAX_VALUE) {
//				return null;
//			} else {
//				/* Mark curr as logically removed */
//				Task succ = curr.next.getReference();
//				// snip = curr.next.attemptMark(succ, true);
//				snip = curr.next.compareAndSet(succ, succ, false, true);
//				if (!snip)
//					continue;
//				pred.next.compareAndSet(curr, succ, false, false); 
//				/* Nodes are rewired */
//				// offsets[p][q].lazySet(curr.taskid);
//				// System.out.println(String.format("[DBG] p %2d runs task %3d from query %d", p, curr.taskid, curr.queryid));
//				return curr;
//			}
//		}
//	}
//	
//	public Task poll (int [][] policy, int p, int q) {
//		return getNextTask(policy, p, q);
//		// return getFirstTask(policy, p, q);
//	}
//	
//	public int size () {
//		boolean [] marked = { false };
//		int count = 0;
//		// System.out.print("Q: ");
//		Task t;
//		for (t = head.next.getReference(); t != null && !marked[0]; t = t.next.get(marked)) {
//			if (t.taskid < Integer.MAX_VALUE) {
//		 		// System.out.print(String.format("%s ", t.toString()));
//				count ++;
//			}
//		}
//		// System.out.println(String.format("(%d tasks)", count));
//		return count;
//	}
}

