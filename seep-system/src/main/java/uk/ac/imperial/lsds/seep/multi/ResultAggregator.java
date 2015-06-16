package uk.ac.imperial.lsds.seep.multi;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

public class ResultAggregator {
	
	/*
	 * A ResultAggregatorNode encapsulates the
	 * complete and partial state (windows) of
	 * a batch - the result of a query task.
	 * 
	 * Each such node is part of a linked list
	 * that links all nodes together from left
	 * to right.
	 * 
	 * Nodes are statically linked.
	 * 
	 * Nodes are removed from the list only by 
	 * marking them as "complete", which means
	 * that all windows contained in this task
	 * are complete.
	 */
	
	/* Individual slot states 
	 * 
	 * -1:  available  (FREE) 
	 *  0:   occupied  (IDLE)
	 *  1:      ready (READY)
	 *  2:  forwarded  (BUSY)
	 */
	private static final int  FREE = -1;
	private static final int  WAIT =  0;
	private static final int  IDLE =  1;
	private static final int READY =  2;
	private static final int  BUSY =  3;
	
	private static class ResultAggregatorNode {
		
		int index;
		int freeOffset;
		
		ResultAggregatorNode next;
		
		PartialWindowResults closing, pending, opening, complete;
		
		public ResultAggregatorNode (int index) {
			
			this.index = index;
			
			/* Initialize windows */
			this.closing  = null;
			this.opening  = null;
			this.pending  = null;
			this.complete = null;
			
			next = null;
		}
		
		public void init (
				PartialWindowResults  opening,
				PartialWindowResults  closing, 
				PartialWindowResults  pending,
				PartialWindowResults complete,
				
				int freeOffset
			) {
			
			/* Initialize windows */
			this.opening  =  opening;
			this.closing  =  closing;
			this.pending  =  pending;
			this.complete = complete;
			
			this.freeOffset = freeOffset;
		}
		
		public void connectTo (ResultAggregatorNode node) {
			this.next = node;
		}
		
		public boolean isRightOpen() {
			
			return (this.opening != null && this.complete != null);
		}
		
		public void aggregate(ResultAggregatorNode p) {
			
			/* Aggregate this nodes opening windows with node
			 * p's closing or pending windows.
			 * 
			 * According to the aggregation rules, the output 
			 * of this operation will always produce complete 
			 * or opening windows - never pending or closing.
			 */
			
			/* Populate node p's complete or opening windows and 
			 * nullify its closing and pending ones. 
			 */
			if (p.closing != null) {
				p.closing.release();
				p.closing = null;
			}
			
			if (p.pending != null) {
				p.pending.release();
				p.pending = null;
			}
			
			/* Nullify this node's opening windows (the results
			 * are stored in p's sets). 
			 */
			
			this.opening.release();
			this.opening = null;
		}
		
		public boolean isReady() {
			/*
			 * Closing and pending windows are managed by a different 
			 * aggregation task.
			 * 
			 * Opening windows are managed by this task. 
			 */
			return (this.closing == null && this.pending == null && this.opening == null);
		}

		public int getFreeOffset() {
			return freeOffset;
		}
		
		public String toString() {
			StringBuilder s = new StringBuilder();
			s.append("[");
			s.append("opening: ");
			s.append(opening);
			s.append(", closing: ");
			s.append(closing);
			s.append(", pending: ");
			s.append(pending);
			s.append(", complete: ");
			s.append(complete);
			s.append(", free: ");
			s.append(freeOffset);
			s.append("]");
			return s.toString();
		}

		public void releaseAll() {
			
			if (this.closing != null)
				this.closing.release();
			if (this.opening != null)
				this.opening.release();
			if (this.pending != null)
				this.pending.release();
			if (this.complete != null)
				this.complete.release();
		}
	}
	
	int size;
	AtomicIntegerArray slots;
	ResultAggregatorNode [] nodes;
	
	/* Sentinel pointers */
	int nextToAggregate;
	int nextToForward;
	
	Semaphore semaphore;
	Lock lock;
	
	IQueryBuffer freeBuffer;
	SubQuery query;
	
	public ResultAggregator (int size, IQueryBuffer buffer, SubQuery query) {
		
		this.size = size;
		
		slots = new AtomicIntegerArray(size);
		nodes = new ResultAggregatorNode [size];
		for (int i = 0, j = i - 1; i < size; i++, j++) {
			slots.set(i, FREE);
			nodes[i] = new ResultAggregatorNode (i);
			if (j >= 0)
				nodes[j].connectTo(nodes[i]);
		}
		nodes[size - 1].connectTo(nodes[0]);
		nextToAggregate = 0;
		nextToForward   = 0;
		
		semaphore = new Semaphore(1, false);
		
		lock = new ReentrantLock();
		
		this.freeBuffer = buffer;
		this.query = query;
	}
	
	public void add (
			int                    taskid,
			PartialWindowResults  opening,
			PartialWindowResults  closing, 
			PartialWindowResults  pending,
			PartialWindowResults complete,
			int                freeOffset
			) {
		
		if (taskid < 0) { /* Invalid task id */
			return ;
		}
		int idx = ((taskid - 1) % size);
		while (! slots.compareAndSet(idx, FREE, WAIT)) {
			
			System.err.println(String.format("warning: result aggregator blocked at %s q %d t %4d idx %4d", 
				Thread.currentThread(), query.getId(), taskid, idx));
			LockSupport.parkNanos(1L);
		}
		
		/* Slot `idx` has been reserved for this task id */
		ResultAggregatorNode node = nodes[idx];
		
		node.init (opening, closing, pending, complete, freeOffset);
//		System.out.println(node);
		
		/* Check if the slot is ready to forward and free
		 * (i.e. there are no partial results).
		 * 
		 * If not, make slot available for aggregation.
		 */
		slots.set(idx, IDLE);
		
		/* Aggregate, starting from `nextToAggregate` */
		
		ResultAggregatorNode p;
		ResultAggregatorNode q;
		
		while (true) {
			
			lock.lock();
		
			if (slots.get(nextToAggregate) == IDLE) {
				
				if (slots.get(nextToAggregate + 1) == IDLE) {
					
					p = nodes[nextToAggregate];
					q = p.next;
					/* Check whether p has any opening windows.
					 * 
					 * If the set of p's complete windows is not null
					 * then aggregate p's opening windows with q's. 
					 * 
					 */
					
//					System.out.println(String.format("[DBG] aggregator thread %s current %4d next %4d", 
//							Thread.currentThread(), p.index, q.index));
					
					/* Increment pointer only if there is work to do */
					if (p.isRightOpen()) {
						
						nextToAggregate = nextToAggregate + 1;
						nextToAggregate = nextToAggregate % size;
						
						/* Let other threads aggregate results as well, 
						 * picking up from `nextToAggregate`.
						 */
						lock.unlock();
//						System.out.println(String.format("[DBG] aggregate current %4d next %4d", p.index, q.index));
						/* Aggregate */
						p.aggregate(q);
//						System.out.println("[DBG] After aggregation, current is " + p);
						if (p.isReady()) {
							
							slots.set(p.index, READY);
						}
						
					} else {
						/*
						 * This means that node p is locked from the
						 * left. 
						 */
						System.err.println ("warning: current node is locked from the left: " + p);
						lock.unlock();
						break;
					}
					
				} else {
					lock.unlock();
					break;
				}
				
			} else {
				lock.unlock();
				break;
			}
		}
		
		/* Forward and free */
		
		if (! semaphore.tryAcquire())
			return;
		
		/* No other thread can enter this section */
//		System.out.println ("[DBG] next to forward is " + nextToForward);
		
		/* Is slot `nextToForward` occupied? 
		 */
		if (! slots.compareAndSet(nextToForward, READY, BUSY)) {
			semaphore.release();
//			System.out.println ("[DBG] failed to forward " + nextToForward);
			return ;
		}
		
		boolean busy = true;
	
		while (busy) {
			
//			System.out.println(String.format("[DBG] __________FREE %4d", nextToForward));
			
			/* Process (forward and free the current slot) */
			int offset = nodes[nextToForward].getFreeOffset();
			
			if (offset != Integer.MIN_VALUE) {
				
				if (offset >= 0)
					freeBuffer.free (offset);
			
			} else {
				
				System.err.println(String.format("[DBG] %s skip slot qid %d idx %6d", 
						Thread.currentThread(), query.getId(), nextToForward));
				System.exit(1);
			}
			
			nodes[nextToForward].releaseAll ();
			/* Release the current slot */
			slots.set(nextToForward, FREE);
			
			/* Increment next */
			nextToForward = nextToForward + 1;
			nextToForward = nextToForward % size;
			
			/* Check if next is ready to be pushed */
			if (! slots.compareAndSet(nextToForward, READY, BUSY)) {
				busy = false;
			}
		}
		
		/* Thread exit critical section */
		semaphore.release();
	}
}
