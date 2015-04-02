package uk.ac.imperial.lsds.seep.multi;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/* 
 * Based on the non-blocking queue of M. Herlihy and N. Shavit
 * ("The Art of Multiprocessor programming").
 */

public class TaskQueue {
	
//	ConcurrentLinkedQueue<Task> queue;
//	
//	public TaskQueue (int p, int q) {
//		queue = new ConcurrentLinkedQueue<Task>();
//	}
//	
//	public boolean add (Task task) {
//		return queue.add(task);
//	}
//	
//	private AtomicBoolean _switch = new AtomicBoolean(false);
//	public Task raul_s_poll (int [][] policy, int p, int q) {
//		if(_switch.get()){
//			_switch.set(false);
//			synchronized(this){
//				return queue.poll();
//			}
//		} else{
//			_switch.set(true);
//			return queue.poll();
//		}
//	}
//	
//	public synchronized Task synch_poll (int [][] policy, int p, int q) {
//		return queue.poll();
//	}
//	
//	public Task poll (int [][] policy, int p, int q) {
//		return queue.poll();
//	}
//	
//	public int size () { return queue.size(); }
//	
// #### default
	
	private Task head;
	
	public TaskQueue (int p, int q) {
		
		head = new Task ();
		Task tail = new Task (null, null, null, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
		while (! head.next.compareAndSet(null, tail, false, false));
	}
		
	/* Lock-free */ /* Insert at the end of the queue */
	public boolean add (Task task) {
		while (true) {
			TaskWindow window = TaskWindow.findTail(head);
			Task pred = window.pred;
			Task curr = window.curr;
			if (curr.taskid != Integer.MAX_VALUE) {
				return false;
			} else {
				task.next.set(curr, false);
				if (pred.next.compareAndSet(curr, task, false, false)) {
					return true; 
				}
			}
		}
	}
	
	/* Lock-free */
	public Task remove (int taskid) {
		boolean snip;
		while (true) {
			TaskWindow window = TaskWindow.find(head, taskid);
			/* Returns pred and curr */
			Task pred = window.pred;
			Task curr = window.curr;
			/* Check if curr matches taskid */
			if (curr.taskid != taskid) {
				return null;
			} else {
				/* Mark curr as logically removed */
				Task succ = curr.next.getReference();
				snip = curr.next.attemptMark(succ, true); 
				if (!snip)
					continue;
				pred.next.compareAndSet(curr, succ, false, false); 
				return curr;
			}
		}
	}
	
	/* Wait-free */
	public boolean containsTask (int taskid) {
		boolean [] marked = { false };
		Task curr = head;
		while (curr.taskid < taskid) {
			curr = curr.next.getReference();
			Task succ = curr.next.get(marked);
		}
		return (curr.taskid == taskid && !marked[0]);
	}
	
	/* Wait-free, but approximate */
	public void dump () {
		boolean [] marked = { false };
		int count = 0;
		System.out.print("Q: ");
		Task t;
		for (t = head.next.getReference(); t != null && !marked[0]; t = t.next.get(marked)) {
			if (t.taskid < Integer.MAX_VALUE) {
		 		System.out.print(String.format("%s ", t.toString()));
				count ++;
			}
		}
		System.out.println(String.format("(%d tasks)", count));
	}
	
	public Task getNextTask (int [][] policy, int p, int q) {
		boolean snip;
		while (true) {
			/* The find() method takes a head node and a `taskid`, 
			 * and traverses the list, seeking to set pred to the 
			 * task with the largest id less than `taskid`, and curr 
			 * to the task with the least id greater than or equal to 
			 * `taskid`.
			 * 
			 * Returns pred and curr.
			 */
			TaskWindow window = TaskWindow.findNextSkipCost(head, policy, p);
			Task pred = window.pred;
			Task curr = window.curr;
			
			/* System.out.println(String.format("[DBG] p %2d pred %3d curr %3d", 
			 * p, pred.taskid, curr.taskid));
			 */
			
			/* Check if curr is not the tail of the queue */
			if (curr.taskid == Integer.MAX_VALUE) {
				return null;
			} else {
				/* Mark curr as logically removed */
				Task succ = curr.next.getReference();
				snip = curr.next.compareAndSet(succ, succ, false, true);
				if (!snip)
					continue;
				pred.next.compareAndSet(curr, succ, false, false); 
				/* Nodes are rewired */
				return curr;
			}
		}
	}
	
	public Task getFirstTask (int [][] policy, int p, int q) {
		boolean snip;
		while (true) {
			/* The find() method takes a head node and a `taskid`, 
			 * and traverses the list, seeking to set pred to the 
			 * task with the largest id less than `taskid`, and curr 
			 * to the task with the least id greater than or equal to 
			 * `taskid`.
			 * 
			 * Returns pred and curr.
			 */
			TaskWindow window = TaskWindow.findHead(head);
			Task pred = window.pred;
			Task curr = window.curr;
			/* Check if curr is not the tail of the queue */
			if (curr.taskid == Integer.MAX_VALUE) {
				return null;
			} else {
				/* Mark curr as logically removed */
				Task succ = curr.next.getReference();
				snip = curr.next.compareAndSet(succ, succ, false, true);
				if (!snip)
					continue;
				pred.next.compareAndSet(curr, succ, false, false); 
				return curr;
			}
		}
	}
	
	public Task poll (int [][] policy, int p, int q) {
		// return getNextTask(policy, p, q);
		return getFirstTask(policy, p, q);
	}
	
	public int size () {
		boolean [] marked = { false };
		int count = 0;
		// System.out.print("Q: ");
		Task t;
		for (t = head.next.getReference(); t != null && !marked[0]; t = t.next.get(marked)) {
			if (t.taskid < Integer.MAX_VALUE) {
		 		// System.out.print(String.format("%s ", t.toString()));
				count ++;
			}
		}
		// System.out.println(String.format("(%d tasks)", count));
		return count;
	}
}
