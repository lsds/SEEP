package uk.ac.imperial.lsds.seep.multi;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Map;

import uk.ac.imperial.lsds.seep.multi.WindowDefinition.WindowType;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.ISubQueryConnectable;

public class QueryBufferWrapperOLD {
	
//	/*
//	 * Configuration of wrapper
//	 */
//	private ISubQueryConnectable connectable;
//	private IQueryBuffer buffer;
//	private WindowDefinition windowDefinition;
//	private int batchSize; /* # windows in batch */
//	
//	private long slide;
//	private long range;
//	
//	private int paneSize;
//	private int ppw; /* Panes per window */
//	private int pps; /* Panes per slide  */
//	private int ppb; /* Panes per batch  */
//	
//	WindowBatch start, end;
//	
//	/* Current window start and end pointer */
//	private int w_, _w;
//	
//	private Deque<WindowBatch> batches_;
//	private Deque<WindowBatch> _batches;
//	
//	private Deque<WindowBatch> next;
//		
//	private Map<WindowBatch, Integer> free;
//	
//	private static long gcd (long a, long b) {
//		if (b == 0) 
//			return a;
//		return 
//			gcd (b, a % b);
//	}
//	
//	private boolean __IS_COUNT_BASED, __IS__TIME_BASED, __IS_TUMBLING;
//	
//	long bufferWraps =  -1;
//	long tupleStamp  =  -1; /* Dummy; for debugging purposes */
//	
//	int  tupleSize;
//	int bundleSize;
//	
//	long __dummy_cnt;
//	long __dummy_tpb; /* Tuples per batch */
//	
//	TupleSchema schema;
//	
//	public QueryBufferWrapperOLD (ISubQueryConnectable connectable, int streamID) {
//		
//		this.connectable = connectable;
//		
//		this.windowDefinition = connectable.getWindowDefinitions().get(streamID);
//		
//		this.schema = this.windowDefinition.getSchema();
//		
//		this.buffer = new SubQueryBuffer();
//		
//		this.tupleSize  = this.buffer.getTupleSize();
//		
//		this.batchSize = this.connectable.getTaskDispatcher().getNumberOfWindowsInBatch();
//		
//		this.slide = this.windowDefinition.getSlide();
//		this.range = this.windowDefinition.getSize ();
//		
//		this.paneSize = (int) gcd(range, slide);
//		this.ppw  = (int) range / paneSize;
//		this.pps  = (int) slide / paneSize;
//		this.ppb  = this.pps * (this.batchSize - 1) + this.ppw;
//		
//		__IS_COUNT_BASED = 
//			(this.windowDefinition.getWindowType() ==   WindowType.ROW_BASED) ? true : false;
//		__IS__TIME_BASED = 
//			(this.windowDefinition.getWindowType() == WindowType.RANGE_BASED) ? true : false;
//		
//		__IS_TUMBLING = (this.slide == this.range);
//		
//		System.out.println(
//		String.format(
//		"[BDG] %15s range %3d slide %3d batch size %3d pane size %3d panes/slide %3d panes/window %3d panes/batch %3d", 
//		this.windowDefinition.getWindowType().toString(), range, slide, batchSize, paneSize, pps, ppw, ppb));
//		
//		batches_ = new ArrayDeque<IWindowBatch>();
//		_batches = new ArrayDeque<IWindowBatch>();
//		
//		next = new ArrayDeque<IWindowBatch>();
//		free = new IdentityHashMap<IWindowBatch, Integer>();
//		
//		IWindowBatch a_batch = new BufferWindowBatch(
//			this.buffer, this.schema,
//			freshInitializedArray(batchSize, -1), 
//			freshInitializedArray(batchSize, -1)
//		);
//		
//		batches_.add (a_batch);
//		_batches.add (a_batch);
//		
//		_w = w_ = -1;
//		
//		__dummy_cnt = 0L;
//		__dummy_tpb = paneSize * tupleSize * ppb;
//	}
//	
//	public boolean addToBuffer (byte[] data) {
//		int bundle = data.remaining() / tupleSize;
//		/* System.out.println(String.format("[DBG] bundle size %3d", bundle)); */
//		int idx = this.buffer.add(data);
//		if (idx == 0)
//			bufferWraps ++;
//		
//		/* updateCurrentWindow (idx, tupleStamp); */
//		
//		for (int i = 0; i < bundle; i++)
//			updateCurrentWindow (idx + i * tupleSize, 0L);
//		//updateCurrentWindow (idx, 0L);
//		
//		/* dummyUpdateCurrentWindow (idx + bundle * tupleSize, bundle * tupleSize, 0L); */
//		
//		return true;
//	}
//	
//	public void freeUpToIndexInBuffer(int i) {
//		
//		this.buffer.setFreeUpToIndex (i);
//	}
//	
//	public long getProcessedTuples () {
//		
//		return this.buffer.getProcessedTuples ();
//	}
//	
//	private void dummyUpdateCurrentWindow (int index, int bytes, long timestamp) {
//		
//		if (__IS_COUNT_BASED) {
//			
//			if (__IS_TUMBLING) {
//				
//				__dummy_cnt += bytes;
//				
//				System.out.println(
//				String.format("[DBG] index %10d bytes %10d count %10d", 
//				index, bytes, __dummy_cnt));
//				
//				if (__dummy_cnt % __dummy_tpb == 0) {
//					
//					System.out.println(
//					String.format("[DBG] batch [%10d %10d] free pointer is %10d", 
//					index - __dummy_tpb, index - 1, index - 1));
//					
//					// this.connectable.getTaskDispatcher().dummyAssembleAndDispatchTask ();
//				}
//			}
//		
//		} else
//		if (__IS__TIME_BASED) {
//		
//		} else {
//			System.err.println("error: unknown window definition");
//            System.exit(1);
//		}
//	}
//	
//	private void updateCurrentWindow (int index, long timestamp) {
//		
//		start = batches_.getLast();
//		end   = _batches.getLast();
//		
//		if (__IS_COUNT_BASED) {
//			
//			if (w_ < 0) {
//				w_ = 0;
//				start.getWindowStartPointers() [w_] = buffer.getStartIndex();
//				start.setStartTimestamp(0L);
//			}
//			
//			/* Open a new window? */
//			if (index >= start.getWindowStartPointers() [w_] + slide * tupleSize) {
//				w_++;
//				
//				/* Is new window part of the next window batch? */
//				if (w_ >= batchSize) {
//					/* Store `free` index for current batch */
//					if (index == 0) {
//						if (bufferWraps > 0)
//							free.put(start, -1);
//						else
//							free.put(start, this.buffer.capacity() - 1);
//					} else { 
//						free.put(start, index - 1);
//					}
//					
//					/* Create new window batch */
//					start = new BufferWindowBatch (
//						buffer, this.schema,
//						new int [batchSize], 
//						new int[batchSize]
//					);
//					start.setStartTimestamp (0L);
//					batches_.addLast (start);
//					next.addLast (start);
//					
//					w_ = 0;
//				}
//				start.getWindowStartPointers() [w_] = index;
//			}
//			
//			/* Close an old window? */
//			boolean close = false;
//			
//			if (_w < 0) {
//				close |= (index >= batches_.getFirst().getWindowStartPointers() [0] + (range - 1)* tupleSize);
//			}
//			else {
//				close |= (index >= end.getWindowEndPointers() [_w] + (slide - 1)* tupleSize);
//			}
//			
//			if (close) {
//				_w ++;
//				
//				/* Is new window part of the next window batch? */
//				if (_w >= batchSize) {
//					/* Set end time for current batch */
//					end.setEndTimestamp (0L);
//					
//					/* Move to new window batch */
//					end = next.poll();
//					_batches.add (end);
//					
//					_w = 0;
//					
//					this.connectable.getTaskDispatcher().assembleAndDispatchTask();
//				}
//				end.getWindowEndPointers() [_w] = index;
//			}
//			
//		} else 
//		if (__IS__TIME_BASED)  {
//			
//		} else {
//			System.err.println("error: unknown window definition");
//			System.exit(1);
//		}
//		
//		return ;
//	}
//	
//	public Deque<IWindowBatch> getFullWindowBatches () {
//		return _batches;
//	}
//	
//	public int getFreeIndexForBatchAndRemoveEntry (IWindowBatch batch) {
//		return 0;
//	}
//	
//	private int [] freshInitializedArray (int size, int init) {
//		
//		int [] result = new int[size];
//		
//		Arrays.fill(result, init);
//		return result;
//	}
}

