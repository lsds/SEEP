package uk.ac.imperial.lsds.streamsql.op.stateful;

import java.nio.ByteBuffer;

import uk.ac.imperial.lsds.seep.multi.AggregationType;
import uk.ac.imperial.lsds.seep.multi.IAggregateOperator;
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
import uk.ac.imperial.lsds.seep.multi.WindowHashTable;
import uk.ac.imperial.lsds.seep.multi.WindowHashTableFactory;
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
	
	private static boolean debug = false;
	
	WindowDefinition windowDefinition;
	
	private AggregationType [] aggregationType;

	private FloatColumnReference [] aggregationAttribute;
	
	private LongColumnReference timestampReference;
	
	ITupleSchema outputSchema;
	
	private Expression [] groupByAttributes = null;
	private boolean hasGroupBy = false;
	
	private int keyLength, valueLength;
	
	private boolean incremental;
	
	public PartialMicroAggregation (WindowDefinition windowDefinition) {
		
		this.windowDefinition = windowDefinition;
		
		this.timestampReference = new LongColumnReference(0);
		
		this.aggregationType = new AggregationType [1];
		this.aggregationType[0] = AggregationType.CNT;
		
		this.aggregationAttribute = new FloatColumnReference [1];
		this.aggregationAttribute[0] = new FloatColumnReference(1);
		
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
		
		this.aggregationType = new AggregationType [1];
		this.aggregationType[0] = aggregationType;
		
		this.aggregationAttribute = new FloatColumnReference [1];
		this.aggregationAttribute[0] = aggregationAttribute;
		
		if (
			this.aggregationType[0] == AggregationType.CNT || 
			this.aggregationType[0] == AggregationType.SUM || 
			this.aggregationType[0] == AggregationType.AVG) {
			
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
		
		this.aggregationType = new AggregationType [1];
		this.aggregationType[0] = aggregationType;
		
		this.aggregationAttribute = new FloatColumnReference [1];
		this.aggregationAttribute[0] = aggregationAttribute;
		
		this.groupByAttributes = groupByAttributes;
		
		if (this.groupByAttributes != null)
			this.hasGroupBy = true;
		
		if (
			this.aggregationType[0] == AggregationType.CNT || 
			this.aggregationType[0] == AggregationType.SUM || 
			this.aggregationType[0] == AggregationType.AVG) {
				
			this.incremental = (windowDefinition.getSlide() < windowDefinition.getSize() / 2);
		}
		
		/* Create output schema */
		
		int n = this.groupByAttributes.length 
				+ 1 + this.aggregationAttribute.length; /* +1 for timestamp, +n for value(s) */
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
		/* Set the value attribute(s) */
		for (int i = this.groupByAttributes.length + 1; i < n; i++)
			outputAttributes[i] = new FloatColumnReference(i);
		
		this.valueLength = 4 * this.aggregationType.length;
		
		this.outputSchema = ExpressionsUtil.getTupleSchemaForExpressions(outputAttributes);
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("[Partial window u-aggregation] ");
		for (int i = 0; i < aggregationType.length; i++)
			sb.append(aggregationType[i].asString(aggregationAttribute[i].toString()) + " ");
		sb.append("[incremental computation ? " + this.incremental + " ]");
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
				
				if (this.aggregationType[0] == AggregationType.CNT) {
					
					windowValue = 1;
					windowTupleCount = 1;
					
				} else {
					
					windowValue = 
							this.aggregationAttribute[0].eval(inputBuffer, inputSchema, inWindowStartOffset);
					windowTupleCount = 1;
				}
								
				inWindowStartOffset += inputTupleSize;

				/* For all remaining tuples in the window... */
				while (inWindowStartOffset < inWindowEndOffset)
				{
					
					if (this.aggregationType[0] == AggregationType.CNT) {
						
						windowValue++;
					
					} else {
						
						value = this.aggregationAttribute[0].eval(inputBuffer, inputSchema, inWindowStartOffset);
						
						if (this.aggregationType[0] == AggregationType.MAX) { windowValue = (value <= windowValue) ? windowValue : value; }
						else
						if (this.aggregationType[0] == AggregationType.MIN) { windowValue = (value >= windowValue) ? windowValue : value; }
						else
						if (this.aggregationType[0] == AggregationType.SUM) { windowValue = windowValue + value; }
						else
						if (this.aggregationType[0] == AggregationType.AVG) { windowValue = windowValue + value; }
					
					}
					
					windowTupleCount++;
					
					inWindowStartOffset += inputTupleSize;
				}
				
				if (this.aggregationType[0] == AggregationType.AVG) 
					windowValue = windowValue / (float) windowTupleCount;
				
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
			this.aggregationType[0] == AggregationType.CNT || 
			this.aggregationType[0] == AggregationType.SUM || 
			this.aggregationType[0] == AggregationType.AVG
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
		
		long windowTimestamp;
		
		for (int currentWindow = 0; currentWindow < startPointers.length; currentWindow++) {
			
			inWindowStartOffset = startPointers[currentWindow];
			inWindowEndOffset = endPointers[currentWindow];
			
			if (currentWindow > windowBatch.getLastWindowIndex())
				break;
			
			if (inWindowStartOffset < 0 && inWindowEndOffset < 0) {
				
				if (windowBatch.getBatchStartPointer() == 0) {
					/* Treat this window as opening; there is no previous batch to open it */
					outputBuffer = opening.getBuffer();
					opening.increment();
					inWindowStartOffset = (int) b;
					inWindowEndOffset = (int) d;
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
							this.aggregationType[0] == AggregationType.SUM || 
							this.aggregationType[0] == AggregationType.AVG
						) {
							windowValue -= this.aggregationAttribute[0].eval(inputBuffer, inputSchema, i);
						}
					}
				}
				
				windowTimestamp = this.timestampReference.eval(inputBuffer, inputSchema, inWindowStartOffset - inputTupleSize);
				
				/* startPointers[currentWindow] = outputBuffer.position(); */
				
				outputBuffer.putLong(windowTimestamp);
				if (this.aggregationType[0] == AggregationType.AVG) {
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
							this.aggregationType[0] == AggregationType.SUM || 
							this.aggregationType[0] == AggregationType.AVG
						) {
							windowValue += this.aggregationAttribute[0].eval(inputBuffer, inputSchema, i);
						}
					}
				} else {
					
					for (int i = inWindowStartOffset; i < inWindowEndOffset; i += inputTupleSize) {
						
						windowTupleCount++;
						
						if (
							this.aggregationType[0] == AggregationType.SUM || 
							this.aggregationType[0] == AggregationType.AVG
						) {
							windowValue += this.aggregationAttribute[0].eval(inputBuffer, inputSchema, i);
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
							this.aggregationType[0] == AggregationType.SUM || 
							this.aggregationType[0] == AggregationType.AVG
						) {
							windowValue -= this.aggregationAttribute[0].eval(inputBuffer,inputSchema, i);
						}
					}
				}
				
				windowTimestamp = this.timestampReference.eval(inputBuffer, inputSchema, inWindowStartOffset);

				/* startPointers[currentWindow] = outputBuffer.position(); */
				
				outputBuffer.putLong(windowTimestamp);
				
				if (this.aggregationType[0] == AggregationType.AVG)
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
	
	private void setGroupByKey(IQueryBuffer buffer, ITupleSchema schema, int offset, byte [] bytes) {
		int pivot = 0;
		for (int i = 0; i < this.groupByAttributes.length; i++) {
			pivot = this.groupByAttributes[i].evalAsByteArray (buffer, schema, offset, bytes, pivot);
		}
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
		
		int inWindowStartOffset;
		int inWindowEndOffset;
		
		WindowHashTable windowTuples;
		byte [] tupleKey = new byte [this.keyLength];
		boolean [] found = new boolean[1];
		
		boolean pack = false;
		
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
				
					/* As an optimization, compute a pending window once */
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
				
				if (inWindowStartOffset == inWindowEndOffset) /* Skip empty windows */
					continue;
				
				outputBuffer = complete.getBuffer();
				complete.increment();
				
				pack = true;
			}
			
			/*
			System.out.println(String.format("[DBG] current window is %6d start %13d end %13d (%10d bytes)", 
					currentWindow, inWindowStartOffset, inWindowEndOffset, inWindowEndOffset - inWindowStartOffset));
			*/
			
			/* If the window is empty, skip it */
			if (inWindowStartOffset != -1) {
				
				windowTuples = WindowHashTableFactory.newInstance(workerId);
				windowTuples.setTupleLength(this.keyLength, this.valueLength);
				/* System.out.println(windowTuples); */
				
				float value;
				
				/* For all the tuples in the window... */
				while (inWindowStartOffset < inWindowEndOffset) {
					
					/*
					System.out.println(String.format("[DBG] enter <%06d, %5.1f, %3d>", 
							inputBuffer.getLong(inWindowStartOffset), 
							inputBuffer.getFloat(inWindowStartOffset + 8), 
							inputBuffer.getInt(inWindowStartOffset + 12)));
					*/
					
					/* Get the group-by key hash code */
					setGroupByKey (inputBuffer, inputSchema, inWindowStartOffset, tupleKey);
					
					if (this.aggregationType[0] == AggregationType.CNT)
						value = 1;
					else
						value = this.aggregationAttribute[0].eval(inputBuffer, inputSchema, inWindowStartOffset);
					
					/* Check whether there is already an entry 
					 * in `windowTuples` for this key. If not,
					 * create a new entry */
					found[0] = false;
					int idx = windowTuples.getIndex(tupleKey, found);
					
					if (idx < 0) {
						System.out.println("fatal error: open-adress hash table is full");
						System.exit(1);
					} else {
						
						ByteBuffer hashtable = windowTuples.getBuffer();
						
						if (! found[0]) {
							/* Create a new entry */
							hashtable.position(idx);
							hashtable.put((byte) 1);
							/* Copy timestamp */
							hashtable.putLong(inputBuffer.getLong(inWindowStartOffset));
							hashtable.put(tupleKey);
							hashtable.putFloat(value);
							/* Set tuple count */
							hashtable.putInt(1);
						} else {
							/* Update existing entry */
							
							int valueOffset = idx + 1 + 8 + this.keyLength;
							int countOffset = valueOffset + this.valueLength;
							
							float prevValue = hashtable.getFloat(valueOffset);
							
							/* Increment tuple count */
							hashtable.putInt(countOffset, hashtable.getInt(countOffset) + 1);
							
							if (this.aggregationType[0] == AggregationType.SUM) { value += prevValue;
							}else
							if (this.aggregationType[0] == AggregationType.SUM) { value += prevValue;
							} else 
							if (this.aggregationType[0] == AggregationType.AVG) { value += prevValue;
							} else
							if (this.aggregationType[0] == AggregationType.MAX) { value = (prevValue < value) ? value : prevValue;  
							} else
							if (this.aggregationType[0] == AggregationType.MIN) { value = (prevValue > value) ? value : prevValue;  
							}
							
							hashtable.putFloat(valueOffset, value);
						}
					}
					
					inWindowStartOffset += inputTupleSize;
				}
				
				/* Iterate over `windowTuples` and write window results
				 * to output buffer */
				evaluateWindow (windowTuples, outputBuffer, pack);
				
				/* Release hash maps */
				windowTuples.release();
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
		
		/*
		System.err.println("Disrupted");
		System.exit(-1);
		*/
	}

	private void processDataPerWindowIncrementallyWithGroupBy (
			WindowBatch windowBatch,
			IWindowAPI api) {

		assert (
			this.aggregationType[0] == AggregationType.CNT || 
			this.aggregationType[0] == AggregationType.SUM || 
			this.aggregationType[0] == AggregationType.AVG);
		
		int workerId = ThreadMap.getInstance().get(Thread.currentThread().getId());
		
		int taskId = windowBatch.getTaskId();
		
		long b = windowBatch.getBufferStartPointer();
		long d = windowBatch.getBufferEndPointer();
		
		windowBatch.initPartialWindowPointers();
		
		if (debug) {
			long p = windowBatch.getBatchStartPointer();
			long q = windowBatch.getBatchEndPointer();
		
			System.out.println(String.format("[DBG] MicroAggregation; batch starts at %10d (%10d) ends at %10d (%10d) %10d windows", 
					b, p, d, q, windowBatch.getLastWindowIndex()));
		}
		
		PartialWindowResults closing  = PartialWindowResultsFactory.newInstance(workerId);
		PartialWindowResults pending  = PartialWindowResultsFactory.newInstance(workerId);
		PartialWindowResults complete = PartialWindowResultsFactory.newInstance(workerId);
		PartialWindowResults opening  = PartialWindowResultsFactory.newInstance(workerId);
		
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
		
		int currWindowStartOffset;
		int currWindowEndOffset;

		int prevWindowStartOffset = -1;
		int prevWindowEndOffset = -1;
		
		WindowHashTable windowTuples;
		byte [] tupleKey = new byte [this.keyLength];
		boolean [] found = new boolean[1];
		
		boolean pack = false;
		
		windowTuples = WindowHashTableFactory.newInstance(workerId);
		windowTuples.setTupleLength(this.keyLength, this.valueLength);
		
		for (int currentWindow = 0; currentWindow < startPointers.length; currentWindow++) {
			
			currWindowStartOffset = startPointers[currentWindow];
			currWindowEndOffset = endPointers[currentWindow];
			
			if (currentWindow > windowBatch.getLastWindowIndex())
				break;
			
			pack = false;
			
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
				
				outputBuffer = complete.getBuffer();
				complete.increment();
				
				pack = true;
			}
			
			if (currWindowStartOffset == currWindowEndOffset) {
				currWindowStartOffset = -1;
			}
			/*
			System.out.println(String.format("[DBG] current window is %6d start %13d end %13d (%10d bytes)", 
					currentWindow, currWindowStartOffset, currWindowEndOffset, currWindowEndOffset - currWindowStartOffset));
			*/
			
			/* Is the window empty? */
			if (currWindowStartOffset == -1) {
				
				if (prevWindowStartOffset != -1) {
					
					for (int i = prevWindowStartOffset; i < currWindowStartOffset; i += inputTupleSize) {
						
						System.out.println(String.format("[DBG] exit <%06d, %5.1f, %3d>", 
								inputBuffer.getLong(i), inputBuffer.getFloat(i + 8), inputBuffer.getInt(i + 12)));
						
						exitWindow(
								inputBuffer, 
								inputSchema, 
								i, 
								windowTuples, 
								tupleKey,
								found);
					}
				}
				
				evaluateWindow (windowTuples, outputBuffer, pack);
				
			} else {
				/*
				 * Tuples in current window that have not been in the previous
				 * window
				 */
				if (prevWindowStartOffset != -1) {
					
					for (int i = prevWindowEndOffset; i < currWindowEndOffset; i += inputTupleSize) {
						/*
						System.out.println(String.format("[DBG] enter <%06d, %5.1f, %3d>", 
								inputBuffer.getLong(i), inputBuffer.getFloat(i + 8), inputBuffer.getInt(i + 12)));
						*/
						enterWindow (
								inputBuffer, 
								inputSchema, 
								i, 
								windowTuples, 
								tupleKey,
								found);
					}
				} else {
					
					for (int i = currWindowStartOffset; i < currWindowEndOffset; i += inputTupleSize) {
						/*
						System.out.println(String.format("[DBG] enter <%06d, %5.1f, %3d>", 
								inputBuffer.getLong(i), inputBuffer.getFloat(i + 8), inputBuffer.getInt(i + 12)));
						*/
						enterWindow (
								inputBuffer, 
								inputSchema, 
								i, 
								windowTuples, 
								tupleKey,
								found);
					}
				}
				
				/*
				 * Tuples in previous window that are not in current window
				 */
				if (prevWindowStartOffset != -1) {
					
					for (int i = prevWindowStartOffset; i < currWindowStartOffset; i += inputTupleSize) {
						/*
						System.out.println(String.format("[DBG] exit <%06d, %5.1f, %3d>", 
								inputBuffer.getLong(i), inputBuffer.getFloat(i + 8), inputBuffer.getInt(i + 12)));
						*/
						exitWindow (
								inputBuffer, 
								inputSchema, 
								i, 
								windowTuples, 
								tupleKey,
								found);
					}
				}
				
				evaluateWindow (windowTuples, outputBuffer, pack);

				prevWindowStartOffset = currWindowStartOffset;
				prevWindowEndOffset = currWindowEndOffset;
			}
		}
		
		/*Wrap-up operator */
		windowTuples.release();
		
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
		/*
		System.err.println("Disrupted");
		System.exit(-1);
		*/
	}
	
	private void exitWindow (
			IQueryBuffer inputBuffer, 
			ITupleSchema inputSchema,
			int exitOffset, 
			WindowHashTable windowTuples,
			byte [] tupleKey,
			boolean [] found) {
		
		setGroupByKey (inputBuffer, inputSchema, exitOffset, tupleKey);
		
		/* Check whether there is already an entry 
		 * in `windowTuples` for this key. If not,
		 * create a new entry */
		found[0] = false;
		int idx = windowTuples.getIndex(tupleKey, found);
		
		if (idx < 0 || ! found[0]) {
			throw new IllegalArgumentException
				("error: attempting to remove a tuple from a window to which it does not belong");
		} else {
			
			ByteBuffer hashtable = windowTuples.getBuffer();
			/* Update existing entry */
			
			int valueOffset = idx + 1 + 8 + this.keyLength;
			int countOffset = valueOffset + this.valueLength;
			
			/* Decrement tuple count */
			int count = hashtable.getInt(countOffset);
			count--;
			/*
			 * If we are linearly probing the hash table to find an
			 * entry, we cannot simply remove elements because this
			 * might brake a chain.
			 * 
			 * We should rather rely on a `tombstone mark` (2).
			 */
			if (count <= 0) {
				hashtable.put(idx, (byte) 2);
			} else {
				/* Update count */
				hashtable.putInt(countOffset, count);
				/* Update value */
				float prevValue = hashtable.getFloat(valueOffset); 
				
				if (this.aggregationType[0] != AggregationType.CNT) {
					prevValue -= this.aggregationAttribute[0].eval(inputBuffer, inputSchema, exitOffset);
				} else {
					prevValue -= 1; 
				}
				hashtable.putFloat(valueOffset, prevValue);
			}
		}
	}
	
	private void enterWindow (
			IQueryBuffer inputBuffer, 
			ITupleSchema inputSchema,
			int enterOffset, 
			WindowHashTable windowTuples,
			byte[] tupleKey,
			boolean [] found) {
		
		setGroupByKey(inputBuffer, inputSchema, enterOffset, tupleKey);
		
		float value;
		if (this.aggregationType[0] == AggregationType.CNT)
			value = 0;
		else
			value = this.aggregationAttribute[0].eval(inputBuffer, inputSchema, enterOffset);
		
		/* Check whether there is already an entry 
		 * in `windowTuples` for this key. If not,
		 * create a new entry */
		found[0] = false;
		int idx = windowTuples.getIndex(tupleKey, found);
		
		if (idx < 0) {
			System.out.println("fatal error: open-adress hash table is full");
			System.exit(1);
		} else {
			
			ByteBuffer hashtable = windowTuples.getBuffer();
			
			if (! found[0]) {
				/* Create a new entry */
				hashtable.position(idx);
				hashtable.put((byte) 1);
				/* Copy time stamp */
				hashtable.putLong(inputBuffer.getLong(enterOffset));
				hashtable.put(tupleKey);
				hashtable.putFloat(value);
				/* Set tuple count */
				hashtable.putInt(1);
			} else {
				/* Update existing entry */
				int valueOffset = idx + 1 + 8 + this.keyLength;
				int countOffset = valueOffset + this.valueLength;
				
				float prevValue = hashtable.getFloat(valueOffset);
				
				/* Increment tuple count */
				hashtable.putInt(countOffset, hashtable.getInt(countOffset) + 1);
				
				/* CNT, SUM, AVG */
				value += prevValue;
				
				hashtable.putFloat(valueOffset, value);
			}
		}
	}
	
	private void evaluateWindow (WindowHashTable windowTuples, IQueryBuffer outputBuffer, boolean pack) {
		
		/* Write current window results to output buffer */
		ByteBuffer hashtable = windowTuples.getBuffer();
		hashtable.clear();
		if (! pack) {
			outputBuffer.getByteBuffer().put(windowTuples.getBuffer());
		} else {
			/* Pack the elements of the buffer */
			for (int idx = 0; idx < hashtable.capacity(); idx += this.getIntermediateTupleLength()) {
				
				if (hashtable.get(idx) != 1) /* Skip empty slots */
					continue;
				/*
				System.out.println(String.format("write-up <%d, %06d, %3d, %5.1f, %3d>",
						hashtable.get(idx),
						hashtable.getLong(idx + 1),
						hashtable.getInt(idx + 9),
						hashtable.getFloat(idx + 13),
						hashtable.getInt(idx + 17)
						));
				*/
				
				/* Append buffer a's tuple to `w3` */
				if (aggregationType[0] == AggregationType.AVG) {
					int valueOffset = idx + 9 + keyLength;
					int countOffset = idx + 9 + keyLength + valueLength;
					/* Compute average */
					float value = hashtable.getFloat(valueOffset);
					float count = hashtable.getInt(countOffset);
					/* Overwrite value */
					hashtable.putFloat(valueOffset, value / (float) count);
					/* Write tuple */
					outputBuffer.put(hashtable.array(), idx + 1, 8 + keyLength + valueLength);
					outputBuffer.put(outputSchema.getDummyContent());
				} else {
					/* Write tuple */
					outputBuffer.put(hashtable.array(), idx + 1, 8 + keyLength + valueLength);
					outputBuffer.put(outputSchema.getDummyContent());
				}	
			}
		}
	}
	
	@Override
	public void processData(WindowBatch firstWindowBatch, WindowBatch secondWindowBatch, IWindowAPI api) {
		
		throw new UnsupportedOperationException
			("MicroAggregation is single input operator and does not operate on two streams");
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
	public int getValueLength () {
		return this.valueLength;
	}

	@Override
	public int numberOfValues () {
		return this.aggregationAttribute.length;
	}

	@Override
	public AggregationType getAggregateType() {
		return this.aggregationType[0];
	}

	@Override
	public int getIntermediateTupleLength () {
		
		return (1 << (32 - Integer.numberOfLeadingZeros((this.keyLength + this.valueLength + 15) - 1)));
	}
}
