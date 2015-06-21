package uk.ac.imperial.lsds.seep.multi;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
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
	 * marking them as FREE (-1), meaning that
	 * all windows contained of this task  are
	 * complete and they have been forwarded.
	 */
	
	/* Individual slot states 
	 * 
	 * -1:  available  (FREE) 
	 *  0:   occupied  (IDLE)
	 *  1:      ready (READY)
	 *  2:  forwarded  (BUSY)
	 */
	private static final int  FREE = -1;
	private static final int  WAIT =  0; /* A thread is populating the slot */
	private static final int  GREX =  1; /* The slot can be aggregated with its next one */
	private static final int  NEAT =  2;
	private static final int  BUSY =  3; /* A thread is busy forwarding the results of the slot */
	
	private static class ResultAggregatorNode {
		
		int index;
		int freeOffset;
		
		ResultAggregatorNode next;
		
		AtomicBoolean left, right;
		
		PartialWindowResults closing, pending, opening, complete;
		
		public ResultAggregatorNode (int index) {
			
			this.index = index;
			
			/* Initialize windows */
			this.closing  = null;
			this.opening  = null;
			this.pending  = null;
			this.complete = null;
			
			next = null;
			
			left  = new AtomicBoolean (false);
			right = new AtomicBoolean (false); 
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
			
			 left.set(false);
			right.set(false); 
		}
		
		public void connectTo (ResultAggregatorNode node) {
			this.next = node;
		}
		
		public boolean isRightOpen() {
			
			return (this.opening != null && this.complete != null);
		}
		
		/* Aggregate this nodes opening windows with node
		 * p's closing or pending windows.
		 * 
		 * According to the aggregation rules, the output 
		 * of this operation will always produce complete 
		 * or opening windows - never pending or closing.
		 */
		public void aggregate (ResultAggregatorNode p) {
			
			/* Populate node p's complete or opening windows and 
			 * nullify its closing and pending ones.
			 */
			if (p.closing != null) {
				
				/* 
				 System.out.println(String.format(
				 "[DBG] %40s aggregate: %6d bytes in opening window %6d bytes in closing window",
				 Thread.currentThread(), this.opening.getBuffer().position(), p.closing.getBuffer().position())); 
				*/ 
				
				IQueryBuffer b1 = this.opening.getBuffer();
				IQueryBuffer b2 =    p.closing.getBuffer();
				
				for (int i = 0; i < b1.position(); i += 16) {
					b2.putFloat(i + 8, (b2.getFloat(i + 8) + b1.getFloat(i + 8)));
				}
				
				p.closing.release();
				p.closing = null;
			}
			
			if (p.pending != null) {
				System.err.println("error: aggregating pending windows is not supported yet");
				/*
				p.pending.release();
				p.pending = null;
				*/
			}
			
			p.setRight();
			
			/* Nullify this node's opening windows (the results
			 * have been stored in p's sets). 
			 */
			this.opening.release();
			this.opening = null;
			
			p.setLeft();
		}
		
		public boolean isReady() {
			/*
			 * Closing and pending windows may be managed 
			 * by a different worker.
			 * 
			 * Opening windows are managed by this worker. 
			 */
			return left.get() && right.get();
		}
		
		public void setLeft () {
			if (! left.compareAndSet(false, true)) {
				System.err.println("warning: unexpected state in ResultAggregator");
			}
		}
		
		public void setRight () {
			if (! right.compareAndSet(false, true)) {
				System.err.println("warning: unexpected state in ResultAggregator");
			}
		}
		
		public int getFreeOffset() {
			return freeOffset;
		}
		
		public String toString() {
			StringBuilder s = new StringBuilder();
			s.append("[");
			s.append(   "opening: "); s.append(   opening);
			s.append( ", closing: "); s.append(   closing);
			s.append( ", pending: "); s.append(   pending);
			s.append(", complete: "); s.append(  complete);
			s.append(    ", free: "); s.append(freeOffset);
			s.append("]");
			return s.toString();
		}

		public void releaseAll() {
			
			if ( this.closing != null)  this.closing.release();
			if ( this.opening != null)  this.opening.release();
			if ( this.pending != null)  this.pending.release();
			if (this.complete != null) this.complete.release();
		}
	}
	
	int size;
	AtomicIntegerArray slots;
	ResultAggregatorNode [] nodes;
	
	/* Sentinel pointers */
	int nextToAggregate;
	int nextToForward;
	
	int nextPointer; /* Temp. variable */
	
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
		/* System.out.println(node); */
		
		/* Check if the slot is ready to forward and free
		 * (i.e. there are no partial results).
		 * 
		 * If not, make slot available for aggregation.
		 */
		slots.set(idx, GREX);
		
		/* Aggregate, starting from `nextToAggregate` */
		ResultAggregatorNode p;
		ResultAggregatorNode q;
		
		while (true) {
			
			lock.lock();
		
			if (slots.get(nextToAggregate) == GREX) {
				
				nextPointer  = nextToAggregate + 1;
				nextPointer %= size;
				
				if (slots.get(nextPointer) == GREX) {
					
					p = nodes[nextToAggregate];
					q = nodes[nextPointer]; /* p.next; */
					
					/* Check whether p has any opening windows.
					 * 
					 * If the set of p's complete windows is not null
					 * then aggregate p's opening windows with q's. 
					 * 
					 */
					
					/* System.out.println(String.format("[DBG] aggregator thread %s current %4d next %4d", 
							Thread.currentThread(), p.index, q.index)); */
					
					if (p.isRightOpen()) {
						
						/* Increment pointer only if there is work to do */
						
						nextToAggregate = nextToAggregate + 1;
						nextToAggregate = nextToAggregate % size;
						
						/* Let other threads aggregate results as well, starting 
						 * from `nextToAggregate`, by releasing the lock.
						 * 
						 * However, we have to deal with a race condition:
						 * 
						 * This thread (say, thread A) will aggregate p's opening 
						 * windows with q's (p.next) closing windows. 
						 * 
						 * At the same time, we permit another thread (say, B) to 
						 * aggregate q's opening windows with q.next's closing ones.
						 * 
						 * If thread B finishes before A, then q will not be ready 
						 * (since thread A is working on q's closing windows).
						 * 
						 * So, thread B will never set q's slot status to NEAT; and, 
						 * neither will thread A. 
						 * 
						 * So q's slot will never be free'd.
						 */
						lock.unlock();
						
						/* System.out.println(String.format("[DBG] %s aggregate current %4d next %4d", 
						 * Thread.currentThread(), p.index, q.index)); */
						
						p.aggregate(q);
						
						if (p.isReady())
							slots.compareAndSet(p.index, GREX, NEAT);
						/* Also check node q, in case two workers raced together */
						if (q.isReady())
							slots.compareAndSet(p.index, GREX, NEAT);
						
						/* lock.unlock(); */
						
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
		
		/* System.out.println ("[DBG] next to forward is " + nextToForward); */
		
		/* Is slot `nextToForward` occupied? 
		 */
		if (! slots.compareAndSet(nextToForward, NEAT, BUSY)) {
			semaphore.release();
			return ;
		}
		
		boolean busy = true;
	
		while (busy) {
			
			/* Process (forward and free the current slot) */
			int offset = nodes[nextToForward].getFreeOffset();
			
			/* System.out.println(String.format("[DBG] forward and free results in slot %4d (%10d)", nextToForward, offset)); */
			
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
			if (! slots.compareAndSet(nextToForward, NEAT, BUSY)) {
				busy = false;
			}
		}
		
		/* Thread exit critical section */
		semaphore.release();
	}
}
