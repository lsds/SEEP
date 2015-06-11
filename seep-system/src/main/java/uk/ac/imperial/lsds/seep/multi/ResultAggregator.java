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
				PartialWindowResults complete
			) {
			
			/* Initialize windows */
			this.closing  =  opening;
			this.opening  =  closing;
			this.pending  =  pending;
			this.complete = complete;
		}
		
		public void connectTo (ResultAggregatorNode node) {
			this.next = node;
		}
		
		public boolean isRightOpen() {
			/*
			 * If the next to aggregate node is this node, then it must have opening
			 * windows (partial windows "to the right" of complete windows).
			 * 
			 * If it does not have opening windows but it has complete windows, then
			 * no further aggregation task is required.
			 * 
			 * (Note that closing and partial windows are handled by a previous task
			 * that may be already running.)
			 */
			return (this.opening != null || (this.opening == null && this.complete != null));
		}

		public void aggregate(ResultAggregatorNode p) {
			/* 
			 * This means that the next node to aggregate was this node
			 * and is now set to this->next.
			 * 
			 * Keep in mind the different window result sets:
			 * 
			 * this-> closing	p-> closing
			 * this-> opening	p-> opening
			 * this-> pending	p-> pending
			 * this->complete	p->complete
			 * 
			 * This function call is responsible for merging:
			 * 
			 * this->pending and (p->pending or p->closing)
			 * this->opening and (p->pending or p->closing)
			 * 
			 * The previous function call (from q) is responsible for merging:
			 * 
			 * q->pending and (this->pending or this->closing) 
			 * q->opening and (this->pending or this->closing)
			 * 
			 * The next function call (p) is responsible for merging:
			 * 
			 * p->pending and (p->next->pending or p->next->closing) 
			 * p->opening and (p->next->pending or p->next->closing)
			 * 
			 * So, it is always safe to merge the closing windows.
			 * But, there is an overlap between those pending.
			 * 
			 * Pending window must open somewhere (if not in the previous task);
			 * so 
			 *  
			 */
			
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
			if (j < 0)
				nodes[size - 1].connectTo(nodes[0]);
			else
				nodes[j].connectTo(nodes[i]);
		}
		nextToAggregate = 0;
		nextToForward   = 0;
		
		semaphore = new Semaphore(1, false);
		
		lock = new ReentrantLock();
		
		this.freeBuffer = freeBuffer;
		this.query = query;
	}
	
	public void add (
			int                    taskid,
			PartialWindowResults  opening,
			PartialWindowResults  closing, 
			PartialWindowResults  pending,
			PartialWindowResults complete
			) {
		
		if (taskid < 0) { /* Invalid task id */
			return ;
		}
		int idx = ((taskid - 1) % size);
		while (! slots.compareAndSet(idx, FREE, WAIT)) {
			
			System.err.println(String.format("warning: result aggregator blocked at %s q %d t %4d idx %4d", 
				Thread.currentThread(), taskid, idx));
			LockSupport.parkNanos(1L);
		}
		
		/* Slot `idx` has been reserved for this task id */
		ResultAggregatorNode node = nodes[idx];
		node.init (opening, closing, pending, complete);
		
		/* Check if the slot is ready to forward and free
		 * (i.e. there are no partial results).
		 * 
		 * If not, make slot available for aggregation.
		 */
		
		slots.set(idx, IDLE);
		
		/* Aggregate, starting from `nextToAggregate` */
		
		ResultAggregatorNode p;
		ResultAggregatorNode q;
		
		/* c|p > p > p p|c|p ... 
		 * 
		 * This is the case where the slide is
		 * larger than 1MB (the batch size).
		 * 
		 * Consider the following:
		 * 
		 * p = get(`next`)
		 * aggregate (p, p.next)
		 * `next` = `next` + 1
		 * p = get(`next`)
		 * 
		 * Cannot increment pointer at this point 
		 * until a closing set is found.
		 * 
		 * `next` now points to p.next.
		 * 
		 * The only solution is to make sure that
		 * opening or complete windows are stored 
		 * at p.next (i.e. always to the right).
		 * 
		 */
		
		/* The case that enables the most parallelism seem to be 
		 * 
		 * c|p > p|c|p > p|c|p > ...
		 *  
		 */
		
		while (true) {
			
			lock.lock();
		
			if (slots.get(nextToAggregate) == IDLE) {
			
				/* It should never be the case that p contains partial results 
				 * and q does not. Otherwise, something is terribly wrong. */
				
				if (slots.get(nextToAggregate + 1) == IDLE) {
					
					p = nodes[nextToAggregate];
					q = p.next;
					
					/* Aggregate p's opening with q's closing and/or partial. */
					
					/* Increment pointer only if there is work to do */
					if (p.isRightOpen()) {
						
						nextToAggregate = nextToAggregate + 1;
						nextToAggregate = nextToAggregate % size;
						
						/* Let other threads aggregate results as well, 
						 * picking up from `nextToAggregate`.
						 */
						lock.unlock();
						
						/* Aggregate */
						p.aggregate(q);
						
					} else {
						/*
						 * This means that there are no opening results
						 * in this task. 
						 */
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
		
		/* Is slot `nextToForward` occupied? 
		 */
		if (! slots.compareAndSet(nextToForward, READY, BUSY)) {
			semaphore.release();
			return ;
		}
		
		boolean busy = true;
	
		while (busy) {
			
			/* Process (forward and free the current slot) */
			int offset = nodes[nextToForward].complete.getFreeOffset();
			
			if (offset != Integer.MIN_VALUE) {
				
				if (offset >= 0)
					freeBuffer.free (offset);
			
			} else {
				
				System.err.println(String.format("[DBG] %s skip slot qid %d idx %6d", 
						Thread.currentThread(), query.getId(), nextToForward));
				System.exit(1);
			}
			
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
