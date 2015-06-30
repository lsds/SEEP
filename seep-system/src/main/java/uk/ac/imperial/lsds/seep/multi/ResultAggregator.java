package uk.ac.imperial.lsds.seep.multi;

import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

public class ResultAggregator {
	
	private static final boolean debug = false;
	
	/*
	 * A ResultAggregatorNode encapsulates the
	 * complete and partial state (windows) of
	 * a batch (the result of a query task).
	 * 
	 * Each such node is part of a linked list
	 * that links all nodes together from left
	 * to right. Nodes are statically linked.
	 */
	
	/* Individual slot states 
	 * 
	 * -1:  available  (FREE) 
	 *  0:   occupied  (IDLE)
	 *  1:      ready (READY)
	 *  2:  forwarded  (BUSY)
	 */
	private static final int  FREE  = -1;
	private static final int  WAIT  =  0; /* A thread is populating the slot */
	private static final int  GREX  =  1; /* The slot can be aggregated with its next one */
	private static final int  READY =  2;
	private static final int  BUSY  =  3; /* A thread is busy forwarding the results of this slot */
	
	private static class ResultAggregatorNode {
		
		int index;
		int latch;
		int freeOffset;
		
		boolean fromGPU = false;
		
		ResultAggregatorNode next;
		
		AtomicBoolean left, right;
		
		PartialWindowResults closing, pending, opening, complete;
		
		ByteBuffer w3;
		
		WindowHashTableWrapper hashtable;
		boolean [] found;
		
		public ResultAggregatorNode (int index) {
			
			this.index = index;
			this.latch = 0;
			
			/* Initialize windows */
			this.closing  = null;
			this.opening  = null;
			this.pending  = null;
			this.complete = null;
			
			next = null;
			
			left  = new AtomicBoolean (false);
			right = new AtomicBoolean (false);
			
			w3 = ByteBuffer.allocate(1048576);
			
			hashtable = new WindowHashTableWrapper();
			found = new boolean[1];
		}
		
		public void init (
				PartialWindowResults  opening,
				PartialWindowResults  closing, 
				PartialWindowResults  pending,
				PartialWindowResults complete,
				
				int freeOffset,
				boolean fromGPU
			) {
			
			/* Initialize windows */
			this.opening  =  opening;
			this.closing  =  closing;
			this.pending  =  pending;
			this.complete = complete;
			
			this.freeOffset = freeOffset;
			
			this.latch = 0;
			
			this.fromGPU = fromGPU;
			
			if (this.closing.numberOfWindows() == 0)  left.set(true); else  left.set(false);
			if (this.opening.numberOfWindows() == 0) right.set(true); else right.set(false);
		}
		
		public void connectTo (ResultAggregatorNode node) {
			this.next = node;
		}
		
		public boolean isRightOpen() {
			
			return 
				(this.pending.numberOfWindows() == 0);
		}
		
		/* Aggregate this nodes opening windows with node
		 * p's closing or pending windows.
		 * 
		 * According to the aggregation rules, the output 
		 * of this operation will always produce complete 
		 * or opening windows - never pending or closing.
		 */
		public void aggregate (ResultAggregatorNode p, IAggregateOperator operator) {
			
			if (this.opening.isEmpty()) {
				/* There is nothing to aggregate. */
				
				if (! p.closing.isEmpty() && ! p.pending.isEmpty())
				{
					System.err.println("error: invalid state in ResultAggregator (1)");
					System.exit(1);
				}
				/*
				this.opening.release();
				this.opening = null;
				
				p.closing.release();
				p.closing = null;
				
				p.pending.release();
				p.pending = null;
				*/
				this.opening.nullify();
				
				p.closing.nullify();
				p.pending.nullify();
				
				return;
			}
			
			if (p.closing.isEmpty() && p.pending.isEmpty())
			{
				System.err.println("error: invalid state in ResultAggregator (2)");
				System.exit(1);
			}
			
			if (operator.hasGroupBy()) aggregateMultipleKeys (p, operator);
			else
				aggregateSingleKey (p, operator);
		}
		
		public void aggregateSingleKey (ResultAggregatorNode p, IAggregateOperator operator) {
			
			/* Populate node this node's complete windows or p's opening windows; and, 
			 * nullify this node's opening windows p's closing and pending ones.
			 */
			
			int w = 0; /* Buffer index */
			IQueryBuffer b1 = this.opening.getBuffer();
			int outputTupleSize = operator.getOutputSchema().getByteSizeOfTuple();
			int valueIdx;
			
			if (p.closing != null) {
				
				if (debug) {
					System.out.println(
					String.format("[DBG] %40s aggregate %6d bytes (%3d opening windows) with %6d bytes (%3d closing windows)",
						Thread.currentThread(), 
						this.opening.getBuffer().position(),
						this.opening.count, 
						p.closing.getBuffer().position(),
						p.closing.count
					)); 
				}
				
				IQueryBuffer b2 = p.closing.getBuffer();
				
				for (w = 0; w < b2.position(); w += outputTupleSize)
				{
					valueIdx = w + 8;
					
					if (
						operator.getAggregateType() == AggregationType.CNT ||
						operator.getAggregateType() == AggregationType.SUM) {
						
						b2.putFloat(valueIdx, (b1.getFloat(valueIdx) + b2.getFloat(valueIdx)));
						
					} else
					if (
						operator.getAggregateType() == AggregationType.MIN) {
						
						if (b1.getFloat(valueIdx) > b2.getFloat(valueIdx)) b2.putFloat(valueIdx, b2.getFloat(valueIdx));
						
					} else
					if (operator.getAggregateType() == AggregationType.MAX) {
						
						if (b1.getFloat(valueIdx) < b2.getFloat(valueIdx)) b2.putFloat(valueIdx, b2.getFloat(valueIdx));
						
					} else
					if (operator.getAggregateType() == AggregationType.AVG) {
						
						throw new UnsupportedOperationException 
							("error: AggregationType.AVG is not supported yet in ResultAggregator"); 
					}
				}
				
				/* We append closing at the end of the complete windows of this node. */
				this.complete.getBuffer().put(b2.array(), 0, b2.position());
				
				/* Free b2 */
				/*
				p.closing.release();
				p.closing = null;
				*/
				p.closing.nullify();
			}
			
			/* There may be more opening windows; in which case 
			 * they should be aggregated with p's pending ones */
			if (w < b1.position()) {
			
				if (p.pending.numberOfWindows() == 0) {
					System.err.println("error: there exist opening windows that are neither closing nor pending");
					System.exit(1);
				}
				
				IQueryBuffer b2 = p.pending.getBuffer();
				
				int count = 0;
				
				for (int i = w; i < b1.position(); i += outputTupleSize)
				{
					count ++;
					
					valueIdx = i + 8;
					if (
						operator.getAggregateType() == AggregationType.CNT ||
						operator.getAggregateType() == AggregationType.SUM) {
						
						/* The value in b2 is fixed */
						b1.putFloat(valueIdx, (b1.getFloat(valueIdx) + b2.getFloat(8)));
						
					} else
					if (
						operator.getAggregateType() == AggregationType.MIN) {
						
						if (b1.getFloat(valueIdx) > b2.getFloat(8)) b1.putFloat(valueIdx, b2.getFloat(8));
						
					} else
					if (operator.getAggregateType() == AggregationType.MAX) {
						
						if (b1.getFloat(valueIdx) < b2.getFloat(8)) b1.putFloat(valueIdx, b2.getFloat(8));
						
					} else
					if (operator.getAggregateType() == AggregationType.AVG) {
						
						throw new UnsupportedOperationException
							("error: AggregationType.AVG is not supported yet in ResultAggregator"); 
					}
				}
				/* Prepend opening windows of this node (starting from w until b1.position()) to 
				 * node p's opening windows.
				 * 
				 * We have to shift the start pointers of p's opening windows.
				 * 
				 * There are `count` new windows, each with a size of `outputTupleSize`.
				 */
				p.opening.shiftLeft (count, outputTupleSize);
				p.opening.prepend(b1.getByteBuffer(), w, b1.position(), w3);
				/*
				p.pending.release();
				p.pending = null;
				*/
				p.pending.nullify();
			} else {
				
				if (p.pending.numberOfWindows() > 0) {
					System.err.println("error: there exist pending windows that have never opened");
					System.exit(1);
				}
			}
			
			this.setRight();
			
			/* Nullify this node's opening windows (the results
			 * have been stored in p's sets). 
			 */
			/*
			this.opening.release();
			this.opening = null;
			*/
			this.opening.nullify();
			
			p.setLeft();
		}
		
		private int compare (IQueryBuffer b1, int offset1, IQueryBuffer b2, int offset2, int length) {
			
			int n = offset1 + length;
			
			for (int i = offset1, j = offset2; i < n; i++, j++) {
				
				byte v1 = b1.getByteBuffer().get(i);
				byte v2 = b2.getByteBuffer().get(j);
				
				if (v1 == v2)
					continue;
				
				if (v1 < v2)
					return -1;
				
				return +1;
			}
			
			return 0;
		}
		
		private void aggregateHashTables (IQueryBuffer a, int f1, int l1, IQueryBuffer b, int f2, int l2, IAggregateOperator operator) {
			
			/* System.out.println(String.format("[DBG] aggregate hashtables [%6d,%6d) and [%6d,%6d)", f1, l1, f2, l2)); */
			
			w3.clear();
			
			int size = (l1 - f1) + (l2 - f2);
			
			if (w3.capacity() < size)
				throw new IndexOutOfBoundsException("error: insuffiecient buffer space for aggregation");
			
			int tupleSize = operator.getIntermediateTupleLength();
			
			/* System.out.println("[DBG] tuple size is " + tupleSize); */
			
			/* Wrap second buffer in a hash table
			 */
			hashtable.configure(b.getByteBuffer(), f2, l2, tupleSize);
			
			/* Iterate over tuples in the first table, and 
			 * merge them with tuples in the second one if 
			 * present. */
			
			for (int idx = f1; idx < l1; idx += tupleSize) {
				
				if (a.getByteBuffer().get(idx) != 1) /* Skip empty slots */
					continue;
				/*
				System.out.println(String.format(" look-up <%d, %06d, %3d, %5.1f, %3d>",
						a.getByteBuffer().get(idx),
						a.getByteBuffer().getLong(idx + 1),
						a.getByteBuffer().getInt(idx + 9),
						a.getByteBuffer().getFloat(idx + 13),
						a.getByteBuffer().getInt(idx + 17)
						));
				*/
				
				/* Look-up second table */
				found[0] = false;
				int pos = hashtable.getIndex(a.array(), idx + 9, operator.getKeyLength(), found);
				if (! found[0]) {
					/* Append buffer a's tuple to `w3` */
					if (operator.getAggregateType() == AggregationType.AVG) {
						int valueOffset = idx + 9 + operator.getKeyLength();
						int countOffset = idx + 9 + operator.getKeyLength() + operator.getValueLength();
						/* Compute average */
						float value = a.getFloat(valueOffset);
						float count = a.getInt(countOffset);
						/* Overwrite value */
						a.putFloat(valueOffset, value / (float) count);
						/* Write tuple */
						w3.put(a.array(), idx + 1, 8 + operator.getKeyLength() + operator.getValueLength());
						w3.put(operator.getOutputSchema().getDummyContent());
					} else {
						/* Write tuple */
						w3.put(a.array(), idx + 1, 8 + operator.getKeyLength() + operator.getValueLength());
						w3.put(operator.getOutputSchema().getDummyContent());
					}
				} else {
					/* Set mark in the hash table */
					b.getByteBuffer().put(pos, (byte) 0);
					/* Merge tuples and append them to `w3` */
					float value1 = a.getFloat(idx + 9 + operator.getKeyLength());
					float value2 = b.getFloat(pos + 9 + operator.getKeyLength());
					
					if (operator.getAggregateType() == AggregationType.AVG) {
						
						int count1 = a.getInt(idx + 9 + operator.getKeyLength() + operator.getKeyLength());
						int count2 = b.getInt(pos + 9 + operator.getKeyLength() + operator.getKeyLength());
						
						/* Compute average */
						float value = (value1 + value2) / (count1 + count2); 
						
						/* Write tuple */
						w3.put(a.array(), idx + 1, 8 + operator.getKeyLength());
						w3.putFloat(value);
						w3.put(operator.getOutputSchema().getDummyContent());
					} else
					if (operator.getAggregateType() == AggregationType.MIN) {
						if (value1 < value2) {
							/* Write a's tuple */
							w3.put(a.array(), idx + 1, 8 + operator.getKeyLength() + operator.getValueLength());
						} else {
							/* Write b's tuple */
							w3.put(b.array(), pos + 1, 8 + operator.getKeyLength() + operator.getValueLength());
						}
						w3.put(operator.getOutputSchema().getDummyContent());
					} else
					if (operator.getAggregateType() == AggregationType.MAX) {
						if (value1 > value2) {
							/* Write a's tuple */
							w3.put(a.array(), idx + 1, 8 + operator.getKeyLength() + operator.getValueLength());
						} else {
							/* Write b's tuple */
							w3.put(b.array(), pos + 1, 8 + operator.getKeyLength() + operator.getValueLength());
						}
						w3.put(operator.getOutputSchema().getDummyContent());
					} else
					if (operator.getAggregateType() == AggregationType.SUM) {
						w3.put(a.array(), idx + 1, 8 + operator.getKeyLength());
						w3.putFloat(value1 + value2);
						w3.put(operator.getOutputSchema().getDummyContent());
					} else
					if (operator.getAggregateType() == AggregationType.CNT) {
						w3.put(a.array(), idx + 1, 8 + operator.getKeyLength());
						w3.putFloat(value1 + value2);
						w3.put(operator.getOutputSchema().getDummyContent());
					}
				}
			}
			/* Write the remaining elements from the second buffer */
			for (int idx = f2; idx < l2; idx += tupleSize) {
				
				if (b.getByteBuffer().get(idx) != 1) /* Skip empty slots */
					continue;
				/*
				System.out.println(String.format("write-up <%d, %06d, %3d, %5.1f, %3d>",
						b.getByteBuffer().get(idx),
						b.getByteBuffer().getLong(idx + 1),
						b.getByteBuffer().getInt(idx + 9),
						b.getByteBuffer().getFloat(idx + 13),
						b.getByteBuffer().getInt(idx + 17)
						));
				*/
				
				/* Append buffer a's tuple to `w3` */
				if (operator.getAggregateType() == AggregationType.AVG) {
					int valueOffset = idx + 9 + operator.getKeyLength();
					int countOffset = idx + 9 + operator.getKeyLength() + operator.getValueLength();
					/* Compute average */
					float value = a.getFloat(valueOffset);
					float count = a.getInt(countOffset);
					/* Overwrite value */
					b.putFloat(valueOffset, value / (float) count);
					/* Write tuple */
					w3.put(b.array(), idx + 1, 8 + operator.getKeyLength() + operator.getValueLength());
					w3.put(operator.getOutputSchema().getDummyContent());
				} else {
					/* Write tuple */
					w3.put(b.array(), idx + 1, 8 + operator.getKeyLength() + operator.getValueLength());
					w3.put(operator.getOutputSchema().getDummyContent());
				}	
			}
		}
		
		private void aggregateBuffers (IQueryBuffer a, int f1, int l1, IQueryBuffer b, int f2, int l2, IAggregateOperator operator) {
			
			w3.clear();
			
			int size = (l1 - f1) + (l2 - f2);
			
			if (w3.capacity() < size)
				throw new IndexOutOfBoundsException("error: insuffiecient buffer space for aggregation");
			
			int keyLength = operator.getKeyLength();
			int tupleSize = operator.getOutputSchema().getByteSizeOfTuple();
			
			int k1, k2;
			int v1, v2;
			
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
				
				k1 = f1 + 8;
				k2 = f2 + 8;
				
				if (compare(a, k1, b, k2, keyLength) < 0) {
					/*
					System.out.println(String.format("[DBG] put %3d", k1));
					*/
					w3.put(a.array(), f1, tupleSize);
					f1 += tupleSize;
				} else
				if (compare(a, k1, b, k2, keyLength) > 0) {
					/*
					System.out.println(String.format("[DBG] put %3d", k2));
					 */
					w3.put(b.array(), f2, tupleSize);
					f2 += tupleSize;
				} else
				{
					/*
					System.out.println(String.format("[DBG] put %3d", k1));
					 */
					
					/* Merge the two tuples */
					w3.put(a.array(), f1, 8 + keyLength);
					
					if (operator.numberOfValues() > 1)
						throw new UnsupportedOperationException
							("error: aggregation of multiple values is not supported yet in ResultAggregator");
					
					v1 = f1 + 8 + keyLength;
					v2 = f2 + 8 + keyLength;
					
					if (
						operator.getAggregateType() == AggregationType.CNT ||
						operator.getAggregateType() == AggregationType.SUM) {
						
						w3.putFloat((a.getFloat(v1) + b.getFloat(v2)));
						w3.put(operator.getOutputSchema().getDummyContent());
							
					} else
					if (
						operator.getAggregateType() == AggregationType.MIN) {
							
						if (a.getFloat(v1) > b.getFloat(v2))
							w3.putFloat(b.getFloat(v2));
						else
							w3.putFloat(a.getFloat(v1));
							
						w3.put(operator.getOutputSchema().getDummyContent());
							
					} else
					if (operator.getAggregateType() == AggregationType.MAX) {
							
						if (a.getFloat(v1) < b.getFloat(v2))
							w3.putFloat(b.getFloat(v2));
						else
							w3.putFloat(a.getFloat(v1));
							
						w3.put(operator.getOutputSchema().getDummyContent());
							
					} else
					if (operator.getAggregateType() == AggregationType.AVG) {
							
						throw new UnsupportedOperationException
							("error: AggregationType.AVG is not supported yet in ResultAggregator"); 
					}
					
					f1 += tupleSize; f2 += tupleSize;
				}
			}
		}
		
		public void aggregateMultipleKeys (ResultAggregatorNode p, IAggregateOperator operator) {
			
			/* Populate this node's complete or opening windows and 
			 * nullify p's closing and pending ones.
			 */
			
			int w = 0;
			IQueryBuffer b1 = this.opening.getBuffer();
			
			/* Start and end pointers for two current windows */
			int f1, l1, f2, l2;
			
			int edge1 = this.opening.count - 1;
			
			if (p.closing != null) {
				
				if (debug) {
					System.out.println(
					String.format("[DBG] %40s aggregate %6d bytes (%3d opening windows) with %6d bytes (%3d closing windows)",
						Thread.currentThread(), 
						this.opening.getBuffer().position(),
						this.opening.count, 
						p.closing.getBuffer().position(),
						p.closing.count
					)); 
				}
				
				IQueryBuffer b2 = p.closing.getBuffer();
				int edge2 = p.closing.count - 1;
				
				/* For each window result `w`... */
				for (w = 0; w < p.closing.count; w ++) {
					
					f1 = this.opening.startPointers[w];
					l1 = (w == edge1) ? b1.position() : this.opening.startPointers[w + 1];
					
					f2 = p.closing.startPointers[w];
					l2 = (w == edge2) ? b2.position() : p.closing.startPointers[w + 1];
					/*
					System.out.println(String.format("[DBG] [%7d,%7d) (+) [%7d,%7d)", f1, l1, f2, l2));
					*/
					if (f2 == l2) {
						
						//TODO: pack hashtable
						continue;
					}
					
					/* Aggregate the two windows */
					
					/* aggregateBuffers (b1, f1, l1, b2, f2, l2, operator); */
					
					aggregateHashTables (b1, f1, l1, b2, f2, l2, operator);
					
					/*
					System.out.println(String.format("[DBG] w3.position() = %10d", w3.position()));
					*/
					
					w3.flip();
					complete.buffer.getByteBuffer().put(w3);
				}
				/*
				p.closing.release();
				p.closing = null;
				*/
				p.closing.nullify();
			}
			
			/* There may be more opening windows; in which case 
			 * they should be aggregated with p's pending ones */
			if (w < this.opening.count) {
			
				if (p.pending.numberOfWindows() == 0) {
					System.err.println("error: there exist opening windows that are neither closing nor pending");
					System.exit(1);
				}
				
				IQueryBuffer b2 = p.pending.getBuffer();
				
				int count = 0;
				
				/* Aggregate the remaining opening windows with the pending result set */
				
				f2 = p.pending.startPointers[0];
				l2 = b2.position();
				
				for (int k = w; k < this.opening.count; k ++)
				{
					count ++;
					
					f1 = this.opening.startPointers[k]; 
					l1 = (k == edge1) ? b1.position() : this.opening.startPointers[k + 1];
					/*
					System.out.println(String.format("[DBG] [%7d,%7d) (+) [%7d,%7d)", f1, l1, f2, l2));
					*/
					
					/* Aggregate the two windows */
					aggregateBuffers (b1, f1, l1, b2, f2, l2, operator);
					
					/* System.out.println(String.format("[DBG] w3.position() = %10d", w3.position())); */
					w3.flip();
					p.pending.increment();
					p.pending.buffer.getByteBuffer().put(w3);
				}
				
				/* Prepend opening windows of this node (starting from w until b1.position()) to 
				 * node p's opening windows.
				 * 
				 * We have to shift the start pointers of p's opening windows.
				 * 
				 * There are `count` new windows, each with a size of `outputTupleSize`.
				 */
				int offset = b2.position() - l2;
				p.opening.shiftLeft (count, offset, p.pending.startPointers);
				p.opening.prepend(b2.getByteBuffer(), l2, b2.position(), w3);
				/*
				p.pending.release();
				p.pending = null;
				*/
				p.pending.nullify();
				
			} else {
				
				if (p.pending.numberOfWindows() > 0) {
					System.err.println("error: there exist pending windows that have never opened");
					System.exit(1);
				}
			}
			
			this.setRight();
			
			/* Nullify this node's opening windows (the results
			 * have been stored in p's sets). 
			 */
			
			/*
			this.opening.release();
			this.opening = null;
			*/
			this.opening.nullify();
			
			p.setLeft();
			
			/* System.err.println("Disrupted.");
			System.exit(1); */
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
				/* System.err.println("warning: unexpected state in ResultAggregator"); */
			}
		}
		
		public void setRight () {
			// System.out.println(String.format("[DBG] set right %d", index));
			if (! right.compareAndSet(false, true)) {
				/* System.err.println("warning: unexpected state in ResultAggregator"); */
			}
		}
		
		public int getFreeOffset () {
			return freeOffset;
		}
		
		public String toString () {
			StringBuilder s = new StringBuilder();
			s.append(String.format("%010d [", index));
			s.append(   "opening: "); s.append(   opening.count);
			s.append( ", closing: "); s.append(   closing.count);
			s.append( ", pending: "); s.append(   pending.count);
			s.append(", complete: "); s.append(  complete.count);
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
	
	IAggregateOperator operator = null;
	
	ResultHandler handler = null;
	
	public ResultAggregator (int size, IQueryBuffer buffer, SubQuery query, ResultHandler parent) {
		
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
		
		this.handler = parent;
	}
	
	public void add (
			int                    taskid,
			PartialWindowResults  opening,
			PartialWindowResults  closing, 
			PartialWindowResults  pending,
			PartialWindowResults complete,
			int                freeOffset,
			int               latencyMark,
			boolean                   GPU
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
		
		node.init (opening, closing, pending, complete, freeOffset, GPU);
		/* System.out.println(node); */
		
		/* Latency mark */
		this.handler.mark[idx] = latencyMark;
		
		/* Make slot available for aggregation */
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
					q = p.next;
					/*
					System.out.println(String.format("[DBG] aggregator thread %s current %4d next %4d", 
							Thread.currentThread(), p.index, q.index));
					*/
					
					/* If p's set of complete windows is not null, then
					 * aggregate p's opening windows set with node  q's 
					 * closing or pending windows.
					 */
					if (p.isRightOpen()) {
						
						/* We increment the `next to aggregate` pointer 
						 * only if there is work to do */
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
						 * So, thread B will never set q's slot status to READY; and, 
						 * neither will thread A. 
						 * 
						 * So q's slot will never be free'd.
						 */
						
						// lock.unlock();
						
						/*
						System.out.println(String.format("[DBG] %40s aggregate current %s next %s", 
						Thread.currentThread(), p, q));
						*/
						p.aggregate(q, operator);
						
						if (p.isReady())
							slots.compareAndSet(p.index, GREX, READY);
						
						/* Also check node q, in case two workers raced together */
						if (q.isReady())
							slots.compareAndSet(p.index, GREX, READY);
						
						lock.unlock();
						
					} else {
						/*
						 * This means that node p is `locked from the left`,
						 * i.e., some other thread may populate its opening
						 * windows.
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
		
		/* Is slot `nextToForward` occupied? */
		if (! slots.compareAndSet(nextToForward, READY, BUSY)) {
			/* System.out.println(nodes[nextToForward]); */
			semaphore.release();
			return ;
		}
		
		boolean busy = true;
		
		while (busy) {
			
			IQueryBuffer buf = nodes[nextToForward].complete.getBuffer();
			byte [] arr = buf.array();
			
			/*
			 * Do the actual result forwarding
			 */
			if (query.getNumberOfDownstreamSubQueries() > 0) {
				int pos = nodes[nextToForward].latch;
				for (int i = pos; i < query.getNumberOfDownstreamSubQueries(); i++)
				{
					if (query.getDownstreamSubQuery(i) != null)
					{
						boolean result = false;
						if (query.isLeft()) 
						{
							result = query.getDownstreamSubQuery(i).getTaskDispatcher().tryDispatchFirst( arr, buf.position());
						}
						else 
						{
							result = query.getDownstreamSubQuery(i).getTaskDispatcher().tryDispatchSecond(arr, buf.position());
						}
						if (! result) {
							
							nodes[nextToForward].latch = i;
							/* Back to ready state */
							slots.set(nextToForward, READY);
							/* We need to release the semaphore as well, right? */
							semaphore.release();
							return;
						}
					}
				}
			}
			
			/* Forward to the distributed API */
			
			/* Measure latency */
			if (handler.mark[nextToForward] != -1)
				query.getLatencyMonitor().monitor(freeBuffer, handler.mark[nextToForward]);
			
			/* Before releasing the buffer, count how many bytes are in the output.
			 * 
			 * It is important that all operators set the position of the buffer accordingly.
			 * 
			 * The assumption is that `buf` is an intermediate buffer and that the start
			 * position is 0.
			 */
			handler.incTotalOutputBytes(buf.position());
			
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
			if (! slots.compareAndSet(nextToForward, READY, BUSY)) {
				busy = false;
			}
		}
		
		/* Thread exit critical section */
		semaphore.release();
	}

	public void setOperator(IAggregateOperator operator) {
		
		this.operator = operator;
		
		if (debug) {
			System.out.println(
				String.format("[DBG] [ResultAggregator] multiple keys? %5s tuple size %4d key size %4d %2d values/tuple",
					operator.hasGroupBy(),
					operator.getOutputSchema().getByteSizeOfTuple(),
					operator.getKeyLength(),
					operator.numberOfValues()
				)
			);
		}
	}
}
