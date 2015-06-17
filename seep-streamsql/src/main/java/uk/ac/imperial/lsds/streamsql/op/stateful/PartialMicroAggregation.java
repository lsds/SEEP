package uk.ac.imperial.lsds.streamsql.op.stateful;

import java.util.Arrays;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;
import uk.ac.imperial.lsds.seep.multi.PartialWindowResults;
import uk.ac.imperial.lsds.seep.multi.PartialWindowResultsFactory;
import uk.ac.imperial.lsds.seep.multi.ThreadMap;
import uk.ac.imperial.lsds.seep.multi.UnboundedQueryBufferFactory;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition;
import uk.ac.imperial.lsds.streamsql.expressions.Expression;
import uk.ac.imperial.lsds.streamsql.expressions.ExpressionsUtil;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.elong.LongColumnReference;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;

public class PartialMicroAggregation implements IStreamSQLOperator, IMicroOperatorCode {
	
	private static boolean debug = false;
	
	WindowDefinition windowDefinition;
	
	private AggregationType aggregationType;

	private FloatColumnReference aggregationAttribute;
	
	private LongColumnReference timestampReference;
	
	ITupleSchema outputSchema;

	public PartialMicroAggregation (WindowDefinition windowDefinition) {
		
		this.windowDefinition = windowDefinition;
		
		this.timestampReference = new LongColumnReference(0);
		
		this.aggregationType = AggregationType.COUNT;
		this.aggregationAttribute = null;
		
		/* Create output schema */
		Expression [] outputAttributes = new Expression[2];
		
		outputAttributes[0] = this.timestampReference;
		outputAttributes[1] = new FloatColumnReference(1);
		
		this.outputSchema = ExpressionsUtil.getTupleSchemaForExpressions(outputAttributes);
	}
	
	public PartialMicroAggregation (
			WindowDefinition windowDefinition, 
			AggregationType aggregationType, 
			FloatColumnReference aggregationAttribute
		) {
		
		this.windowDefinition = windowDefinition;
		
		this.timestampReference = new LongColumnReference(0);
		
		this.aggregationType = aggregationType;
		this.aggregationAttribute = aggregationAttribute;
		
		/* Create output schema */
		Expression [] outputAttributes = new Expression[2];
		
		outputAttributes[0] = this.timestampReference;
		outputAttributes[1] = new FloatColumnReference(1);
		
		this.outputSchema = ExpressionsUtil.getTupleSchemaForExpressions(outputAttributes);
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("[Partial window u-aggregation]");
		return sb.toString();
	}
	
	@Override
	public void accept(OperatorVisitor ov) {
		ov.visit(this);
	}
	
	@Override
	public void processData (WindowBatch windowBatch, IWindowAPI api) {
		
		processDataPerWindowIncrementally (windowBatch, api);
	}
	
	private void computePartialWindowPointers (WindowBatch windowBatch, IWindowAPI api) {
		
		int taskId = windowBatch.getTaskId();
		
		long p = windowBatch.getBatchStartPointer();
		long q = windowBatch.getBatchEndPointer();
		
		long b = windowBatch.getBufferStartPointer();
		long d = windowBatch.getBufferEndPointer();
		
		ITupleSchema inSchema = windowBatch.getSchema();
		int tupleSize = inSchema.getByteSizeOfTuple();
		
		long paneSize = windowDefinition.getPaneSize();
		
		int workerId = ThreadMap.getInstance().get(Thread.currentThread().getId());
		
		System.out.println(String.format("[DBG] MicroAggregation; batch starts at %10d (%10d) ends at %10d (%10d)", 
			b, p, d, q));
		
		PartialWindowResults closing  = PartialWindowResultsFactory.newInstance(workerId);
		PartialWindowResults pending  = PartialWindowResultsFactory.newInstance(workerId);
		PartialWindowResults complete = PartialWindowResultsFactory.newInstance(workerId);
		PartialWindowResults opening  = PartialWindowResultsFactory.newInstance(workerId);
		
		/* */
		int SIZE = 65536;
		
		int [] startPointers = new int [SIZE];
		int []   endPointers = new int [SIZE];
		
		Arrays.fill(startPointers, -1);
		Arrays.fill(  endPointers, -1);
		
		long streamIndex, bufferIndex;
		long pid, _pid = ((p / tupleSize) / paneSize) - 1; /* _pid is the previous pane id */
		
		long offset = -1; /* Undefined */
		
		long wid, opensAt;
		
		for (streamIndex = p, bufferIndex = b; streamIndex < q && bufferIndex < d; streamIndex += tupleSize, bufferIndex += tupleSize) {
			
			pid = (streamIndex / tupleSize) / paneSize; /* Current pane */
			
			if (_pid < pid) {
				
				/* Pane `_pid` closed; pane `pid` opened */
				
				/* Check if a window closes at `_pid` */
				if (_pid % windowDefinition.panesPerSlide() == 0) {
					
					opensAt = _pid - windowDefinition.numberOfPanes() + 1;
					wid = opensAt / windowDefinition.panesPerSlide();
					
					if (wid >= 0) {
						
						// System.out.println(String.format("[DBG] closing %05d; buffer index %10d", wid, bufferIndex));
						
						/* Calculate offset */
						if (offset < 0) {
							offset = wid;
							System.out.println(String.format("[DBG] window %05d is closing; offset %10d", wid, offset));
						}
						
						/* Store end pointer */
						endPointers[(int) (wid - offset)] = (int) bufferIndex;
					}
				}
				/* Check if a window opens at `pid` */
				if ( pid % windowDefinition.panesPerSlide() == 0) {
					
					wid = pid / windowDefinition.panesPerSlide();
					
					// System.out.println(String.format("[DBG] opening %05d; buffer index %10d", wid, bufferIndex));
					
					/* Calculate offset */
					if (offset < 0) {
						offset = wid;
						System.out.println(String.format("[DBG] window %05d is opening; offset %10d", wid, offset));
					}
					
					/* Store start pointer */
					startPointers[(int) (wid - offset)] = (int) bufferIndex;
				}
				_pid = pid;
			}
		}
		
		for (int i = 0; i < SIZE; i++) {
			if (startPointers[i] < 0 && endPointers[i] < 0)
				continue;
			System.out.println(String.format("[DBG] window %05d (%5d) start %10d end %10d", i, i + offset, startPointers[i], endPointers[i]));
		}
		
		/* At the end of processing, set window batch accordingly */
		if (! closing.isEmpty())
			windowBatch.setClosing (closing);
		else {
			windowBatch.setClosing(null);
			closing.release();
		}
		
		if (! pending.isEmpty())
			windowBatch.setPending (pending);
		else {
			windowBatch.setPending(null);
			pending.release();
		}
		
		if (! complete.isEmpty())
			windowBatch.setComplete (complete);
		else {
			windowBatch.setComplete(null);
			complete.release();
		}
		
		if (! opening.isEmpty())
			windowBatch.setOpening (opening);
		else {
			windowBatch.setOpening(null);
			opening.release();
		}
		
		if (debug)
			System.out.println(String.format("[DBG] Task %10d finished free pointer %10d", 
					taskId, windowBatch.getFreeOffset()));
		
		/*
		System.err.println("Disrupted.");
		System.exit(1);
		*/
	}
	
	private void processDataPerWindowIncrementally (WindowBatch windowBatch, IWindowAPI api) {

		assert (
			this.aggregationType == AggregationType.COUNT || 
			this.aggregationType == AggregationType.SUM   || 
			this.aggregationType == AggregationType.AVG
		);
		
		int workerId = ThreadMap.getInstance().get(Thread.currentThread().getId());
		
		int taskId = windowBatch.getTaskId();
		
		long b = windowBatch.getBufferStartPointer();
		long d = windowBatch.getBufferEndPointer();
		
		if (debug) {
			long p = windowBatch.getBatchStartPointer();
			long q = windowBatch.getBatchEndPointer();
		
			System.out.println(String.format("[DBG] MicroAggregation; batch starts at %10d (%10d) ends at %10d (%10d)", 
					b, p, d, q));
		}
		
		PartialWindowResults closing  = PartialWindowResultsFactory.newInstance(workerId);
		PartialWindowResults pending  = PartialWindowResultsFactory.newInstance(workerId);
		PartialWindowResults complete = PartialWindowResultsFactory.newInstance(workerId);
		PartialWindowResults opening  = PartialWindowResultsFactory.newInstance(workerId);
		
		windowBatch.initPartialWindowPointers();
		
		int [] startPointers = windowBatch.getWindowStartPointers();
		int [] endPointers   = windowBatch.getWindowEndPointers();

		IQueryBuffer inputBuffer  = windowBatch.getBuffer();
		
		IQueryBuffer closingOutputBuffer  = UnboundedQueryBufferFactory.newInstance();
		IQueryBuffer pendingOutputBuffer  = UnboundedQueryBufferFactory.newInstance();
		IQueryBuffer completeOutputBuffer = UnboundedQueryBufferFactory.newInstance();
		IQueryBuffer openingOutputBuffer  = UnboundedQueryBufferFactory.newInstance();
		
		IQueryBuffer outputBuffer;
		
		 closing.setBuffer( closingOutputBuffer);
		 pending.setBuffer( pendingOutputBuffer);
		complete.setBuffer(completeOutputBuffer);
		 opening.setBuffer( openingOutputBuffer);
		
		ITupleSchema inputSchema = windowBatch.getSchema();
		int inputTupleSize = inputSchema.getByteSizeOfTuple();
		
		int inWindowStartOffset;
		int inWindowEndOffset;

		int prevWindowStart = -1;
		int prevWindowEnd = -1;
		
		float windowValue = 0;
		int   windowTupleCount = 0;
		
		long  windowTimestamp;
		
		for (int currentWindow = 0; currentWindow < startPointers.length; currentWindow++) {
			
			inWindowStartOffset = startPointers[currentWindow];
			inWindowEndOffset = endPointers[currentWindow];
			
			if (inWindowStartOffset < 0 && inWindowEndOffset < 0)
				break;
			
//			System.out.println(String.format("[DBG] current window is %3d start %6d end %6d", 
//					currentWindow, inWindowStartOffset, inWindowEndOffset));
			
			if (inWindowStartOffset < 0) {
				outputBuffer = closing.getBuffer();
				inWindowStartOffset = (int) b;
			} else
			if (inWindowEndOffset < 0) {
				outputBuffer = opening.getBuffer();
				inWindowEndOffset = (int) d;
			} else {
				outputBuffer = complete.getBuffer();
			}
			
			/* Is the window empty? */
			
			if (inWindowStartOffset == -1) {
				
				if (prevWindowStart != -1) {
					
					for (int i = prevWindowStart; i < inWindowStartOffset; i += inputTupleSize) {
						
						windowTupleCount--;
						if (
							this.aggregationType == AggregationType.SUM || 
							this.aggregationType == AggregationType.AVG
						) {
							windowValue -= this.aggregationAttribute.eval(inputBuffer, inputSchema, i);
						}
					}
				}
				
				windowTimestamp = this.timestampReference.eval(inputBuffer, inputSchema, inWindowStartOffset - inputTupleSize);
				
				/* startPointers[currentWindow] = outputBuffer.position(); */
				
				outputBuffer.putLong(windowTimestamp);
				if (this.aggregationType == AggregationType.AVG) {
					windowValue = windowValue / windowTupleCount;
				}
				outputBuffer.putFloat(windowValue);
				outputBuffer.put(outputSchema.getDummyContent());
				
				/* endPointers[currentWindow] = outputBuffer.position() - 1; */
				
			} else {
				/*
				 * Process tuples in current window that have 
				 * not been in the previous window.
				 */
				if (prevWindowStart != -1) {
					
					for (int i = prevWindowEnd; i < inWindowEndOffset; i += inputTupleSize) {
						
						windowTupleCount++;
						
						if (
							this.aggregationType == AggregationType.SUM || 
							this.aggregationType == AggregationType.AVG
						) {
							windowValue += this.aggregationAttribute.eval(inputBuffer, inputSchema, i);
						}
					}
				} else {
					
					for (int i = inWindowStartOffset; i < inWindowEndOffset; i += inputTupleSize) {
						
						windowTupleCount++;
						
						if (
							this.aggregationType == AggregationType.SUM || 
							this.aggregationType == AggregationType.AVG
						) {
							windowValue += this.aggregationAttribute.eval(inputBuffer, inputSchema, i);
						}
					}
				}

				/*
				 * Process tuples in previous window that are not in current window
				 */
				if (prevWindowStart != -1) {
					
					for (int i = prevWindowStart; i < inWindowStartOffset; i += inputTupleSize) {
						
						windowTupleCount--;
						
						if (
							this.aggregationType == AggregationType.SUM || 
							this.aggregationType == AggregationType.AVG
						) {
							windowValue -= this.aggregationAttribute.eval(inputBuffer,inputSchema, i);
						}
					}
				}
				
				windowTimestamp = this.timestampReference.eval(inputBuffer, inputSchema, inWindowStartOffset);

				/* startPointers[currentWindow] = outputBuffer.position(); */
				
				outputBuffer.putLong(windowTimestamp);
				
				if (this.aggregationType == AggregationType.AVG)
					windowValue = windowValue / windowTupleCount;
				
				outputBuffer.putFloat(windowValue);
				outputBuffer.put(outputSchema.getDummyContent());
				
				/* endPointers[currentWindow] = outputBuffer.position() - 1; */

				prevWindowStart = inWindowStartOffset;
				prevWindowEnd = inWindowEndOffset;
			}
		}
		
		/* Release old buffer (will return Unbounded Buffers to the pool) */
		inputBuffer.release();
		windowBatch.setSchema(outputSchema);
		
		/* At the end of processing, set window batch accordingly */
		if (! closing.isEmpty()) {
			// System.out.println(String.format("[DBG] task %2d has closing windows", taskId));
			windowBatch.setClosing (closing);
		} else {
			windowBatch.setClosing(null);
			closing.release();
		}
		
		if (! pending.isEmpty()) {
			// System.out.println(String.format("[DBG] task %2d has pending windows", taskId));
			windowBatch.setPending (pending);
		} else {
			windowBatch.setPending(null);
			pending.release();
		}
		
		if (! complete.isEmpty()) {
			// System.out.println(String.format("[DBG] task %2d has complete windows", taskId));
			windowBatch.setComplete (complete);
		} else {
			windowBatch.setComplete(null);
			complete.release();
		}
		
		if (! opening.isEmpty()) {
			// System.out.println(String.format("[DBG] task %2d has opening windows", taskId));
			windowBatch.setOpening (opening);
		} else {
			windowBatch.setOpening(null);
			opening.release();
		}
		
		if (debug)
			System.out.println(String.format("[DBG] Task %10d finished free pointer %10d", 
					taskId, windowBatch.getFreeOffset()));

		api.outputWindowBatchResult(-1, windowBatch);
	}
	
	@Override
	public void processData(WindowBatch firstWindowBatch, WindowBatch secondWindowBatch, IWindowAPI api) {
		
		throw new UnsupportedOperationException("MicroAggregation is single input operator and does not operate on two streams");
	}
}
