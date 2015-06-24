package uk.ac.imperial.lsds.streamsql.op.stateful;

import uk.ac.imperial.lsds.seep.multi.AggregationType;
import uk.ac.imperial.lsds.seep.multi.IAggregateOperator;
import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;
import uk.ac.imperial.lsds.seep.multi.PartialWindowResults;
import uk.ac.imperial.lsds.seep.multi.PartialWindowResultsFactory;
import uk.ac.imperial.lsds.seep.multi.ThreadMap;
import uk.ac.imperial.lsds.seep.multi.TupleSet;
import uk.ac.imperial.lsds.seep.multi.UnboundedQueryBufferFactory;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition;
import uk.ac.imperial.lsds.seep.multi.WindowMap;
import uk.ac.imperial.lsds.seep.multi.WindowMapFactory;
import uk.ac.imperial.lsds.seep.multi.WindowTuple;
import uk.ac.imperial.lsds.streamsql.expressions.Expression;
import uk.ac.imperial.lsds.streamsql.expressions.ExpressionsUtil;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatExpression;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntExpression;
import uk.ac.imperial.lsds.streamsql.expressions.elong.LongColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.elong.LongExpression;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;

public class PartialMicroAggregation implements IStreamSQLOperator, IMicroOperatorCode, IAggregateOperator {
	
	private static boolean debug = true;
	
	WindowDefinition windowDefinition;
	
	private AggregationType aggregationType;

	private FloatColumnReference aggregationAttribute;
	
	private LongColumnReference timestampReference;
	
	ITupleSchema outputSchema;
	
	private Expression [] groupByAttributes = null;
	private boolean hasGroupBy = false;
	
	private int keyLength;
	
	private boolean incremental;

	public PartialMicroAggregation (WindowDefinition windowDefinition) {
		
		this.windowDefinition = windowDefinition;
		
		this.timestampReference = new LongColumnReference(0);
		
		this.aggregationType = AggregationType.CNT;
		this.aggregationAttribute = new FloatColumnReference(1);
		
		this.incremental = true;
		
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
		
		if (
			this.aggregationType == AggregationType.CNT || 
			this.aggregationType == AggregationType.SUM   || 
			this.aggregationType == AggregationType.AVG) {
			
			this.incremental = (windowDefinition.getSlide() < windowDefinition.getSize() / 2);
		}
		
		/* Create output schema */
		Expression [] outputAttributes = new Expression[2];
		
		outputAttributes[0] = this.timestampReference;
		outputAttributes[1] = new FloatColumnReference(1);
		
		this.outputSchema = ExpressionsUtil.getTupleSchemaForExpressions(outputAttributes);
	}
	
	public PartialMicroAggregation (
			WindowDefinition windowDefinition, 
			AggregationType aggregationType,
			FloatColumnReference aggregationAttribute,
			Expression [] groupByAttributes
		) {
		
		this.windowDefinition = windowDefinition;
		
		this.timestampReference = new LongColumnReference(0);
		
		this.aggregationType = aggregationType;
		this.aggregationAttribute = aggregationAttribute;
		
		this.groupByAttributes = groupByAttributes;
		
		if (this.groupByAttributes != null)
			this.hasGroupBy = true;
		
		if (
			this.aggregationType == AggregationType.CNT || 
			this.aggregationType == AggregationType.SUM || 
			this.aggregationType == AggregationType.AVG) {
				
			this.incremental = (windowDefinition.getSlide() < windowDefinition.getSize() / 2);
		}
		
		/* Create output schema */
		
		int n = this.groupByAttributes.length + 2; /* +1 for timestamp, +1 for value */
		Expression [] outputAttributes = 
				new Expression[n];
		
		/* The first attribute is the timestamp */
		outputAttributes[0] = new LongColumnReference(0);
		
		this.keyLength = 0;
		
		for (int i = 1; i <= this.groupByAttributes.length; i++) {
			
			Expression e = this.groupByAttributes[i - 1];
			
			if (e instanceof   IntExpression) { outputAttributes[i] = new   IntColumnReference(i); this.keyLength += 4;
			} else 
			if (e instanceof  LongExpression) { outputAttributes[i] = new  LongColumnReference(i); this.keyLength += 8;
			} else 
			if (e instanceof FloatExpression) { outputAttributes[i] = new FloatColumnReference(i); this.keyLength += 4;
			} 
			else {
				throw new IllegalArgumentException("error: unknown expression type in group-by operator");
			}
		}
		/* The last attribute is the value */
		outputAttributes[n - 1] = new FloatColumnReference(n - 1);
		
		this.outputSchema = ExpressionsUtil.getTupleSchemaForExpressions(outputAttributes);
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("[Partial window u-aggregation] ");
		sb.append(aggregationType.asString(aggregationAttribute.toString()));
		sb.append(" [incremental computation ? " + this.incremental + " ]");
		return sb.toString();
	}
	
	@Override
	public void accept(OperatorVisitor ov) {
		ov.visit(this);
	}
	
	@Override
	public void processData (WindowBatch windowBatch, IWindowAPI api) {
		
		if (! this.hasGroupBy) {
			if (this.incremental) { 
				processDataPerWindowIncrementally (windowBatch, api);
			} else {
				processDataPerWindow (windowBatch, api);
			}
		} else {
			if (this.incremental) { 
				processDataPerWindowIncrementallyWithGroupBy (windowBatch, api);
			} else {
				processDataPerWindowWithGroupBy (windowBatch, api);
			}
		}
	}
	
	private void processDataPerWindow (WindowBatch windowBatch, IWindowAPI api) {
		
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
		
		/* Cumulative value and count */
		float windowValue = 0;
		int   windowTupleCount = 0;
		
		float value;
		
		long  windowTimestamp;

		for (int currentWindow = 0; currentWindow < startPointers.length; currentWindow++) {
			
			inWindowStartOffset = startPointers[currentWindow];
			inWindowEndOffset = endPointers[currentWindow];
			
			if (currentWindow > windowBatch.getLastWindowIndex())
				break;
			
			/*
			System.out.println(String.format("[DBG] current window is %6d start %13d end %13d", 
					currentWindow, inWindowStartOffset, inWindowEndOffset));
			*/
			
			if (inWindowStartOffset < 0 && inWindowEndOffset < 0) {
				
				if (windowBatch.getBatchStartPointer() == 0) {
					/* Treat this window as opening; there is no previous batch to open it */
					outputBuffer = opening.getBuffer();
					opening.increment();
					inWindowStartOffset = (int) b;
					inWindowEndOffset   = (int) d;
				} else {
				
					/* This is a pending window */
				
					/* Compute a pending window once */
					if (pending.numberOfWindows() > 0)
						continue;
				
					outputBuffer = pending.getBuffer();
					pending.increment();
					inWindowStartOffset = (int) b;
					inWindowEndOffset   = (int) d;
				}
			} else
			if (inWindowStartOffset < 0) {
				outputBuffer = closing.getBuffer();
				closing.increment();
				inWindowStartOffset = (int) b;
			} else
			if (inWindowEndOffset < 0) {
				outputBuffer = opening.getBuffer();
				opening.increment();
				inWindowEndOffset = (int) d;
			} else {
				
				if (inWindowStartOffset == inWindowEndOffset) /* Empty window */
					continue;
				
				outputBuffer = complete.getBuffer();
				complete.increment();
			}
			
			/* If the window is empty, skip it */
			if (inWindowStartOffset != -1) {

				/* First value */
				
				windowTimestamp = 
						this.timestampReference.eval(inputBuffer, inputSchema, inWindowStartOffset);
				
				if (this.aggregationType == AggregationType.CNT) {
					
					windowValue = 1;
					windowTupleCount = 1;
					
				} else {
					
					windowValue = 
							this.aggregationAttribute.eval(inputBuffer, inputSchema, inWindowStartOffset);
					windowTupleCount = 1;
				}
								
				inWindowStartOffset += inputTupleSize;

				/* For all remaining tuples in the window... */
				while (inWindowStartOffset < inWindowEndOffset)
				{
					
					if (this.aggregationType == AggregationType.CNT) {
						
						windowValue++;
					
					} else {
						
						value = this.aggregationAttribute.eval(inputBuffer, inputSchema, inWindowStartOffset);
						
						if (this.aggregationType == AggregationType.MAX) { windowValue = (value <= windowValue) ? windowValue : value; }
						else
						if (this.aggregationType == AggregationType.MIN) { windowValue = (value >= windowValue) ? windowValue : value; }
						else
						if (this.aggregationType == AggregationType.SUM) { windowValue = windowValue + value; }
						else
						if (this.aggregationType == AggregationType.AVG) { windowValue = windowValue + value; }
					
					}
					
					windowTupleCount++;
					
					inWindowStartOffset += inputTupleSize;
				}
				
				if (this.aggregationType == AggregationType.AVG) windowValue = windowValue / (float) windowTupleCount;
				
				outputBuffer.putLong(windowTimestamp);
				outputBuffer.putFloat(windowValue);
				outputBuffer.put(outputSchema.getDummyContent());
			}
		}
		
		/* Release old buffer (will return Unbounded Buffers to the pool) */
		inputBuffer.release();
		windowBatch.setSchema(outputSchema);
		
		/* At the end of processing, set window batch accordingly */
		windowBatch.setClosing  ( closing);
		windowBatch.setPending  ( pending);
		windowBatch.setComplete (complete);
		windowBatch.setOpening  ( opening);
		
		if (debug)
			System.out.println(String.format("[DBG] Task %10d finished free pointer %10d [%4d closing; %4d pending; %4d complete; and %4d opening windows]", 
					taskId, 
					windowBatch.getFreeOffset(),
					closing.numberOfWindows(),
					pending.numberOfWindows(),
					complete.numberOfWindows(),
					opening.numberOfWindows()));
		
		api.outputWindowBatchResult(-1, windowBatch);
	}
	
	private void processDataPerWindowIncrementally (WindowBatch windowBatch, IWindowAPI api) {

		assert (
			this.aggregationType == AggregationType.CNT || 
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
			
			if (currentWindow > windowBatch.getLastWindowIndex())
				break;
			
			/*
			System.out.println(String.format("[DBG] current window is %6d start %13d end %13d", 
					currentWindow, inWindowStartOffset, inWindowEndOffset));
			*/
			
			if (inWindowStartOffset < 0 && inWindowEndOffset < 0) {
				
				if (windowBatch.getBatchStartPointer() == 0) {
					/* Treat this window as opening; there is no previous batch to open it */
					outputBuffer = opening.getBuffer();
					opening.increment();
					inWindowStartOffset = (int) b;
					inWindowEndOffset   = (int) d;
				} else {
				
					/* This is a pending window */
				
					/* Compute a pending window once */
					if (pending.numberOfWindows() > 0)
						continue;
				
					outputBuffer = pending.getBuffer();
					pending.increment();
					inWindowStartOffset = (int) b;
					inWindowEndOffset   = (int) d;
				}
			} else
			if (inWindowStartOffset < 0) {
				outputBuffer = closing.getBuffer();
				closing.increment();
				inWindowStartOffset = (int) b;
			} else
			if (inWindowEndOffset < 0) {
				outputBuffer = opening.getBuffer();
				opening.increment();
				inWindowEndOffset = (int) d;
			} else {
				
				if (inWindowStartOffset == inWindowEndOffset) /* Empty window */
					continue;
				
				outputBuffer = complete.getBuffer();
				complete.increment();
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
		windowBatch.setClosing  ( closing);
		windowBatch.setPending  ( pending);
		windowBatch.setComplete (complete);
		windowBatch.setOpening  ( opening);
		
		if (debug)
			System.out.println(String.format("[DBG] Task %10d finished free pointer %10d [%4d closing; %4d pending; %4d complete; and %4d opening windows]", 
					taskId, 
					windowBatch.getFreeOffset(),
					closing.numberOfWindows(),
					pending.numberOfWindows(),
					complete.numberOfWindows(),
					opening.numberOfWindows()));

		api.outputWindowBatchResult(-1, windowBatch);
	}
	
	private int getGroupByKey (IQueryBuffer buffer, ITupleSchema schema, int offset, byte [] bytes) {
		int result = 1;
		for (int i = 0; i < this.groupByAttributes.length; i++) {
			this.groupByAttributes[i].evalAsByteArray (buffer, schema, offset, bytes);
			int __result = 1;
			for (int j = 0; j < bytes.length; j++)
				__result =  31 * __result + bytes[j];
			result = 31 * result + __result;
		}
		return result;
	}
	
	private void processDataPerWindowWithGroupBy(WindowBatch windowBatch,
			IWindowAPI api) {
		
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
		
		/* This is a temporary buffer holding the timestamp and key for each
		 * unique tuple in a window.
		 * 
		 * The buffer is reused as we iterate over windows 
		 */
		IQueryBuffer windowKeys = UnboundedQueryBufferFactory.newInstance();
		
		int inWindowStartOffset;
		int inWindowEndOffset;
		
		WindowMap windowTuples;
		byte [] tupleKey = new byte [8];
		
		for (int currentWindow = 0; currentWindow < startPointers.length; currentWindow++) {
			
			inWindowStartOffset = startPointers[currentWindow];
			inWindowEndOffset   =   endPointers[currentWindow];
			
			if (currentWindow > windowBatch.getLastWindowIndex())
				break;
			
			if (inWindowStartOffset < 0 && inWindowEndOffset < 0) {
				
				if (windowBatch.getBatchStartPointer() == 0) {
					/* Treat this window as opening; there is no previous batch to open it */
					outputBuffer = opening.getBuffer();
					opening.increment();
					inWindowStartOffset = (int) b;
					inWindowEndOffset   = (int) d;
				} else {
				
					/* This is a pending window */
				
					/* Compute a pending window once */
					if (pending.numberOfWindows() > 0)
						continue;
				
					outputBuffer = pending.getBuffer();
					pending.increment();
					inWindowStartOffset = (int) b;
					inWindowEndOffset   = (int) d;
				}
			} else
			if (inWindowStartOffset < 0) {
				outputBuffer = closing.getBuffer();
				closing.increment();
				inWindowStartOffset = (int) b;
			} else
			if (inWindowEndOffset < 0) {
				outputBuffer = opening.getBuffer();
				opening.increment();
				inWindowEndOffset = (int) d;
			} else {
				
				if (inWindowStartOffset == inWindowEndOffset) /* Empty window */
					continue;
				
				outputBuffer = complete.getBuffer();
				complete.increment();
			}
			
			/*
			System.out.println(String.format("[DBG] current window is %6d start %13d end %13d (%10d bytes)", 
					currentWindow, inWindowStartOffset, inWindowEndOffset, inWindowEndOffset - inWindowStartOffset));
			*/
			
			/* If the window is empty, skip it */
			if (inWindowStartOffset != -1) {
				
				windowTuples = WindowMapFactory.newInstance(workerId);
				
				windowKeys.clear();
				
				int keyOffset;
				float value;
				
				int prevPos;
				
				/* For all the tuples in the window... */
				while (inWindowStartOffset < inWindowEndOffset) {
					
					/* Get the group-by key hash code */
					int keyHashCode = getGroupByKey (inputBuffer, inputSchema, inWindowStartOffset, tupleKey);
					
					/* Create new entry for this tuple in temp. byte buffer.
					 * It will be overwritten if the entry exists.
					 */
					
					prevPos = windowKeys.position();
					
					/* Copy timestamp */
					this.timestampReference.appendByteResult(
							inputBuffer, 
							inputSchema, 
							inWindowStartOffset, 
							windowKeys);
					
					keyOffset = windowKeys.position();
					
					/* Copy group-by attributes */
					for (int i = 0; i < groupByAttributes.length; i++) {
						this.groupByAttributes[i].appendByteResult(
								inputBuffer, 
								inputSchema, 
								inWindowStartOffset, 
								windowKeys);
					}
					
					if (this.aggregationType == AggregationType.CNT)
						value = 0;
					else
						value = this.aggregationAttribute.eval(inputBuffer, inputSchema, inWindowStartOffset);
					
					/* Check whether there is already an entry 
					 * in `windowTuples` for this key. If not,
					 * create a new entry */
					if (! windowTuples.containsKey (keyHashCode)) {
						
						/* Create a new entry */
						windowTuples.put(keyHashCode, windowKeys, keyOffset, this.keyLength, value, 1);
						
					} else {
						/* The key hash code already exists, but the match 
						 * might be the result of a collision. */
						
						WindowTuple t = windowTuples.containsKey(keyHashCode, keyOffset);
						
						if (t == null) {
							
							/* Create a new entry */
							windowTuples.put(keyHashCode, windowKeys, keyOffset, this.keyLength, value, 1);
							
						} else {
							
							/* Update existing entry */
							t.count += 1;
							if (this.aggregationType == AggregationType.SUM) { t.value += value;
							} else 
							if (this.aggregationType == AggregationType.AVG) { t.value += value;
							} else
							if (this.aggregationType == AggregationType.MAX) { t.value = (t.value < value) ? value : t.value;  
							} else
							if (this.aggregationType == AggregationType.MIN) { t.value = (t.value > value) ? value : t.value;  
							}
							
							/* Reset `windowKeys` position */
							windowKeys.position(prevPos);
						}
					}
					
					inWindowStartOffset += inputTupleSize;
				}
				
				/* System.out.println(String.format("[DBG] finished processing window %3d; %d values", currentWindow, windowTuples.size()));
				windowTuples.getHeap().dump(); */
				
				/* Iterate over `windowTuples` and write window results
				 * to output buffer */
				evaluateWindow (windowKeys, windowTuples, outputBuffer);
				
				/* Release hash maps */
				windowTuples.release();
			}
		}

		/*Wrap-up operator */
		windowKeys.release();
		
		/* Release old buffer (will return Unbounded Buffers to the pool) */
		inputBuffer.release();
		windowBatch.setSchema(outputSchema);
		
		/* At the end of processing, set window batch accordingly */
		windowBatch.setClosing  ( closing);
		windowBatch.setPending  ( pending);
		windowBatch.setComplete (complete);
		windowBatch.setOpening  ( opening);
		
		if (debug)
			System.out.println(String.format("[DBG] Task %10d finished free pointer %10d [%4d closing; %4d pending; %4d complete; and %4d opening windows]", 
					taskId, 
					windowBatch.getFreeOffset(),
					closing.numberOfWindows(),
					pending.numberOfWindows(),
					complete.numberOfWindows(),
					opening.numberOfWindows()));
		
		/* Print tuples
		outBuffer.close();
		int tid = 1;
		while (outBuffer.hasRemaining()) {
			// Each tuple is 16-bytes long
			System.out.println(String.format("%04d: %2d,%4d,%4.1f", 
			tid++,
			outBuffer.getByteBuffer().getLong (),
			outBuffer.getByteBuffer().getInt  (),
			outBuffer.getByteBuffer().getFloat()
			));
		}
		*/
		
		api.outputWindowBatchResult(-1, windowBatch);
		
		/*
		System.err.println("Disrupted");
		System.exit(-1);
		*/
	}
	
	private void processDataPerWindowIncrementallyWithGroupBy (
			WindowBatch windowBatch,
			IWindowAPI api) {

		assert (
			this.aggregationType == AggregationType.CNT || 
			this.aggregationType == AggregationType.SUM   || 
			this.aggregationType == AggregationType.AVG);
		
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
		
		/* This is a temporary buffer holding the timestamp and key for each
		 * unique tuple in a window.
		 * 
		 * The buffer is reused as we iterate over windows 
		 */
		IQueryBuffer windowKeys = UnboundedQueryBufferFactory.newInstance();
		
		int currWindowStartOffset;
		int currWindowEndOffset;

		int prevWindowStartOffset = -1;
		int prevWindowEndOffset = -1;
		
		WindowMap windowTuples;
		byte [] tupleKey = new byte [8];
		
		windowTuples = WindowMapFactory.newInstance(workerId);
		
		windowKeys.clear();
		
		for (int currentWindow = 0; currentWindow < startPointers.length; currentWindow++) {
			
			currWindowStartOffset = startPointers[currentWindow];
			currWindowEndOffset = endPointers[currentWindow];
			
			if (currentWindow > windowBatch.getLastWindowIndex())
				break;
			
			/*
			System.out.println(String.format("[DBG] current window is %6d start %13d end %13d", 
					currentWindow, currWindowStartOffset, currWindowEndOffset));
			*/
			
			if (currWindowStartOffset < 0 && currWindowEndOffset < 0) {
				
				if (windowBatch.getBatchStartPointer() == 0) {
					/* Treat this window as opening; there is no previous batch to open it */
					outputBuffer = opening.getBuffer();
					opening.increment();
					currWindowStartOffset = (int) b;
					currWindowEndOffset   = (int) d;
				} else {
				
					/* This is a pending window */
				
					/* Compute a pending window once */
					if (pending.numberOfWindows() > 0)
						continue;
				
					outputBuffer = pending.getBuffer();
					pending.increment();
					currWindowStartOffset = (int) b;
					currWindowEndOffset   = (int) d;
				}
				
			} else
			if (currWindowStartOffset < 0) {
				outputBuffer = closing.getBuffer();
				closing.increment();
				currWindowStartOffset = (int) b;
			} else
			if (currWindowEndOffset < 0) {
				outputBuffer = opening.getBuffer();
				opening.increment();
				currWindowEndOffset = (int) d;
			} else {
				
				if (currWindowStartOffset == currWindowEndOffset) /* Empty window */
					continue;
				
				outputBuffer = complete.getBuffer();
				complete.increment();
			}
			
			/* Is the window empty? */
			if (currWindowStartOffset == -1) {
				
				if (prevWindowStartOffset != -1) {
					
					for (int i = prevWindowStartOffset; i < currWindowStartOffset; i += inputTupleSize) {
						
						exitWindow(
								inputBuffer, 
								inputSchema, 
								i, 
								windowKeys,
								windowTuples, 
								tupleKey);
					}
				}
				
				evaluateWindow (
						windowKeys, 
						windowTuples, 
						outputBuffer);
			} else {
				/*
				 * Tuples in current window that have not been in the previous
				 * window
				 */
				if (prevWindowStartOffset != -1) {
					
					for (int i = prevWindowEndOffset; i < currWindowEndOffset; i += inputTupleSize) {
						
						enterWindow (
								inputBuffer, 
								inputSchema, 
								i, 
								windowKeys,
								windowTuples, 
								tupleKey);
					}
				} else {
					
					for (int i = currWindowStartOffset; i < currWindowEndOffset; i += inputTupleSize) {
						
						enterWindow (
								inputBuffer, 
								inputSchema, 
								i, 
								windowKeys,
								windowTuples, 
								tupleKey);
					}
				}
				
				/*
				 * Tuples in previous window that are not in current window
				 */
				if (prevWindowStartOffset != -1) {
					
					for (int i = prevWindowStartOffset; i < currWindowStartOffset; i += inputTupleSize) {
						
						exitWindow (
								inputBuffer, 
								inputSchema, 
								i, 
								windowKeys,
								windowTuples, 
								tupleKey);
					}
				}

				evaluateWindow (
						windowKeys, 
						windowTuples, 
						outputBuffer);

				prevWindowStartOffset = currWindowStartOffset;
				prevWindowEndOffset = currWindowEndOffset;
			}
		}
		
		/*Wrap-up operator */
		windowKeys.release();
		
		/* Release old buffer (will return Unbounded Buffers to the pool) */
		inputBuffer.release();
		windowBatch.setSchema(outputSchema);
		
		/* At the end of processing, set window batch accordingly */
		windowBatch.setClosing  ( closing);
		windowBatch.setPending  ( pending);
		windowBatch.setComplete (complete);
		windowBatch.setOpening  ( opening);
		
		if (debug)
			System.out.println(String.format("[DBG] Task %10d finished free pointer %10d [%4d closing; %4d pending; %4d complete; and %4d opening windows]", 
					taskId, 
					windowBatch.getFreeOffset(),
					closing.numberOfWindows(),
					pending.numberOfWindows(),
					complete.numberOfWindows(),
					opening.numberOfWindows()));
		
		/* Print tuples
		outBuffer.close();
		int tid = 1;
		while (outBuffer.hasRemaining()) {
			// Each tuple is 16-bytes long
			System.out.println(String.format("%04d: %2d,%4d,%4.1f", 
			tid++,
			outBuffer.getByteBuffer().getLong (),
			outBuffer.getByteBuffer().getInt  (),
			outBuffer.getByteBuffer().getFloat()
			));
		}
		*/
		
		api.outputWindowBatchResult(-1, windowBatch);
		/*
		System.err.println("Disrupted");
		System.exit(-1);
		*/
	}
	
	private void exitWindow (
			IQueryBuffer inputBuffer, 
			ITupleSchema inputSchema,
			int exitOffset, 
			IQueryBuffer windowKeys,
			WindowMap windowTuples,
			byte [] tupleKey) {
		
		int keyHashCode = getGroupByKey (inputBuffer, inputSchema, exitOffset, tupleKey);
		
		int prevPos = windowKeys.position();
		/* Copy group-by attributes temporarily */
		for (int i = 0; i < groupByAttributes.length; i++) {
			this.groupByAttributes[i].appendByteResult(
					inputBuffer, 
					inputSchema, 
					exitOffset, 
					windowKeys);
		}
		int keyOffset = prevPos;
		WindowTuple t = windowTuples.containsKey (keyHashCode, keyOffset);
		if (t == null) {
			throw new IllegalArgumentException
			("error: attempting to remove a tuple from a window to which it does not belong");
		}
		/* Decrement count since we are removing a tuple */
		t.count -= 1;
		if (this.aggregationType != AggregationType.CNT) {
			t.value -= this.aggregationAttribute.eval(inputBuffer, inputSchema, exitOffset);
		}
		/* Reset position of window keys */
		windowKeys.position(prevPos);
	}
	
	private void enterWindow (
			IQueryBuffer inputBuffer, 
			ITupleSchema inputSchema,
			int enterOffset, 
			IQueryBuffer windowKeys,
			WindowMap windowTuples,
			byte[] tupleKey) {
		
		int keyHashCode = getGroupByKey(inputBuffer, inputSchema, enterOffset, tupleKey);
		
		/* Create new entry for this tuple in temp. byte buffer.
		 * It will be overwritten if the entry exists.
		 */
		
		int prevPos = windowKeys.position();
		
		/* Copy time stamp */
		this.timestampReference.appendByteResult(
				inputBuffer, 
				inputSchema, 
				enterOffset, 
				windowKeys);
		
		int keyOffset = windowKeys.position();
		
		/* Copy group-by attributes */
		for (int i = 0; i < groupByAttributes.length; i++) {
			this.groupByAttributes[i].appendByteResult(
					inputBuffer, 
					inputSchema, 
					enterOffset, 
					windowKeys);
		}
		
		float value;
		
		if (this.aggregationType == AggregationType.CNT)
			value = 0;
		else
			value = this.aggregationAttribute.eval(inputBuffer, inputSchema, enterOffset);
		
		/* Check whether there is already an entry 
		 * in `windowTuples` for this key. If not,
		 * create a new entry */
		if (! windowTuples.containsKey (keyHashCode)) {
			
			/* Create a new entry */
			windowTuples.put(keyHashCode, windowKeys, keyOffset, this.keyLength, value, 1);
			
		} else {
			/* The key hash code already exists, but the match 
			 * might be the result of a collision. */
			
			WindowTuple t = windowTuples.containsKey(keyHashCode, keyOffset);
			
			if (t == null) {
				
				/* Create a new entry */
				windowTuples.put(keyHashCode, windowKeys, keyOffset, this.keyLength, value, 1);
				
			} else {
				
				/* Update existing entry */
				t.count += 1;
				if (this.aggregationType != AggregationType.CNT) {
					t.value += value;
				}
				/* Reset `windowKeys` position */
				windowKeys.position(prevPos);
			}
		}
	}
	
	private void evaluateWindow (
			IQueryBuffer windowKeys,
			WindowMap windowTuples, 
			IQueryBuffer outputBuffer
			) {
		
		/* Write current window results to output buffer */
		TupleSet heap = windowTuples.getHeap();
		for (int i = 1; i < heap.next; i++) {
			WindowTuple t = heap.getTuple(i);
			if (t.count > 0) {
				/* Write tuple t to output buffer
				 * 
				 * The time stamp and key are stored in `windowKeys`. 
				 * The value is stored in `t`.
				 */
				outputBuffer.put(windowKeys, t.offset - 8, t.length + 8);
				if (this.aggregationType == AggregationType.AVG)
					outputBuffer.putFloat((t.value / (float) t.count));
				else
					outputBuffer.putFloat(t.value);
				outputBuffer.put(outputSchema.getDummyContent());
			}
		}
	}
	
	@Override
	public void processData(WindowBatch firstWindowBatch, WindowBatch secondWindowBatch, IWindowAPI api) {
		
		throw new UnsupportedOperationException("MicroAggregation is single input operator and does not operate on two streams");
	}

	@Override
	public boolean hasGroupBy() {
		return this.hasGroupBy;
	}

	@Override
	public ITupleSchema getOutputSchema () {
		return this.outputSchema;
	}

	@Override
	public int getKeyLength () {
		return this.keyLength;
	}

	@Override
	public int numberOfValues () {
		return 1;
	}

	@Override
	public AggregationType getAggregateType() {
		return this.aggregationType;
	}
}
