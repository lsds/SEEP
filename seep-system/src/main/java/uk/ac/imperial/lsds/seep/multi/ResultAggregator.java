package uk.ac.imperial.lsds.seep.multi;

import java.nio.ByteBuffer;
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
		
		ByteBuffer w3;
		
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
			
			w3 = ByteBuffer.allocate(1048576);
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
			
			if (this.closing == null) {
				// System.out.println(String.format("[DBG] init %d left=true", index));
				left.set(true);
			} else {
				// System.out.println(String.format("[DBG] init %d left=false", index));
				left.set(false);
			}
			
			if (this.opening == null) {
				// System.out.println(String.format("[DBG] init %d right=true", index));
				right.set(true);
			} else {
				// System.out.println(String.format("[DBG] init %d right=false", index));
				right.set(false);
			}
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
		public void aggregateSingleKey (ResultAggregatorNode p) {
			
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
			
			this.setRight();
			
			/* Nullify this node's opening windows (the results
			 * have been stored in p's sets). 
			 */
			this.opening.release();
			this.opening = null;
			
			p.setLeft();
		}
		
		private void aggregateBuffers (IQueryBuffer a, int f1, int l1, IQueryBuffer b, int f2, int l2) {
			w3.clear();
			int size = (l1 - f1) + (l2 - f2);
			if (w3.capacity() < size)
				throw new IndexOutOfBoundsException("error: insuffiecient buffer space for aggregation");
			
			/* Assumptions:
			 * 
			 * The output schema is [long timestamp (8), int key (4), float value (4). */
			int k1, k2;
			while (true) {
				if (f1 == l1) {
					/* Copy remaining elements from buffer b */
					w3.put(b.array(), f2, l2 - f2);
					return;
				}
				if (f2 == l2) {
					/* Copy remaining elements from buffer 1 */
					w3.put(a.array(), f1, l1 - f1);
					return;
				}
				k1 = a.getInt(f1 + 8);
				k2 = b.getInt(f2 + 8);
				if (k1 < k2) {
					// System.out.println(String.format("[DBG] put %3d", k1));
					w3.put(a.array(), f1, 16);
					f1 += 16;
				} else
				if (k2 < k1) {
					// System.out.println(String.format("[DBG] put %3d", k2));
					w3.put(b.array(), f2, 16);
					f2 += 16;
				} else
				{
					// System.out.println(String.format("[DBG] put %3d", k1));
					w3.put(a.array(), f1, 12);
					w3.putFloat((a.getFloat(f1 + 12) + b.getFloat(f2 + 12)));
					f1 += 16; f2 += 16;
				}
			}
		}
		
		public void aggregateMultipleKeys (ResultAggregatorNode p) {
			
			// System.out.println(this);
			// System.out.println(p);
			
			/* Populate this node's complete or opening windows and 
			 * nullify p's closing and pending ones.
			 */
			if (p.closing != null) {
				/*
				System.out.println(String.format(
				"[DBG] %40s aggregate multiple keys: %6d bytes (%6d windows) in opening set %6d bytes (%6d windows) in closing set",
				Thread.currentThread(), 
				this.opening.getBuffer().position(),
				this.opening.count,
				p.closing.getBuffer().position(),
				p.closing.count)); 
				*/
				IQueryBuffer b1 = this.opening.getBuffer();
				IQueryBuffer b2 =    p.closing.getBuffer();
				
				int f1, l1, f2, l2;
				int edge = this.opening.count - 1;
				/* For each window result... */
				for (int w = 0; w < this.opening.count; w ++) {
					
					f1 = this.opening.startPointers[w]; 
					l1 = (w == edge) ? b1.position() : this.opening.startPointers[w + 1];
					
					f2 = p.closing.startPointers[w];
					l2 = (w == edge) ? b2.position() : p.closing.startPointers[w + 1];
					/*
					System.out.println(String.format("[DBG] [%7d,%7d) (+) [%7d,%7d)", f1, l1, f2, l2));
					*/
					if (f2 == l2)
						continue;
					
					/* Aggregate the two windows */
					aggregateBuffers (b1, f1, l1, b2, f2, l2);
					// System.out.println(String.format("[DBG] w3.position() = %10d", w3.position()));
					w3.flip();
					complete.buffer.getByteBuffer().put(w3);
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
			
			this.setRight();
			
			/* Nullify this node's opening windows (the results
			 * have been stored in p's sets). 
			 */
			this.opening.release();
			this.opening = null;
			
			p.setLeft();
			
			// System.exit(1);
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
			// System.out.println(String.format("[DBG] set left %d", index));
			if (! left.compareAndSet(false, true)) {
				// System.err.println("warning: unexpected state in ResultAggregator");
			}
		}
		
		public void setRight () {
			// System.out.println(String.format("[DBG] set right %d", index));
			if (! right.compareAndSet(false, true)) {
				// System.err.println("warning: unexpected state in ResultAggregator");
			}
		}
		
		public int getFreeOffset () {
			return freeOffset;
		}
		
		public String toString () {
			StringBuilder s = new StringBuilder();
			s.append(String.format("%010d [", index));
			s.append(   "opening: "); s.append(   opening);
			s.append( ", closing: "); s.append(   closing);
			s.append( ", pending: "); s.append(   pending);
			s.append(", complete: "); s.append(  complete);
			s.append(    ", free: "); s.append(freeOffset);
			s.append("]");
			s.append(String.format( " left: %5s", left.get()));
			s.append(String.format(" right: %5s", right.get()));
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
	
	boolean hasGroupBy = true;
	AggregationType aggregationType = AggregationType.SUM;
	ITupleSchema outputSchema = null;
	
	IAggregateOperator operator = null;
	
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
					
					/*
					System.out.println(String.format("[DBG] aggregator thread %s current %4d next %4d", 
							Thread.currentThread(), p.index, q.index));
					*/
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
						// lock.unlock();
						/*
						System.out.println(String.format("[DBG] %s aggregate current %4d next %4d", 
						Thread.currentThread(), p.index, q.index));
						*/
						if (! hasGroupBy) {
							p.aggregateSingleKey(q);
						} else {
							p.aggregateMultipleKeys(q);
						}
						
						if (p.isReady())
							slots.compareAndSet(p.index, GREX, NEAT);
						/* Also check node q, in case two workers raced together */
						if (q.isReady())
							slots.compareAndSet(p.index, GREX, NEAT);
						
						lock.unlock();
						
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
			/* System.out.println(nodes[nextToForward]); */
			semaphore.release();
			return ;
		}
		
		boolean busy = true;
	
		while (busy) {
			
			/* Process (forward and free the current slot) */
			int offset = nodes[nextToForward].getFreeOffset();
			/*
			System.out.println(String.format("[DBG] forward and free results in slot %4d (%10d)", nextToForward, offset));
			*/
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

	public void setOperator(IAggregateOperator operator) {
		
		this.operator = operator;
		/*
		System.out.println("[DBG] ResultAggregator: group-by ? " + operator.hasGroupBy());
		System.out.println("[DBG] ResultAggregator: output tuple size = " + operator.getOutputSchema().getByteSizeOfTuple());
		System.out.println("[DBG] ResultAggregator: key length = " + operator.getKeyLength());
		System.out.println("[DBG] ResultAggregator: number of values is " + operator.numberOfValues());
		*/
	}
}
