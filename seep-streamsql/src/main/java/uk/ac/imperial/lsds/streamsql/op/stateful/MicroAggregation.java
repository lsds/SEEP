package uk.ac.imperial.lsds.streamsql.op.stateful;

import java.nio.FloatBuffer;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;
import uk.ac.imperial.lsds.seep.multi.IntMap;
import uk.ac.imperial.lsds.seep.multi.IntMapEntry;
import uk.ac.imperial.lsds.seep.multi.IntMapFactory;
import uk.ac.imperial.lsds.seep.multi.ThreadMap;
import uk.ac.imperial.lsds.seep.multi.UnboundedQueryBufferFactory;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition;
import uk.ac.imperial.lsds.streamsql.expressions.Expression;
import uk.ac.imperial.lsds.streamsql.expressions.ExpressionsUtil;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatExpression;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntExpression;
import uk.ac.imperial.lsds.streamsql.expressions.elong.LongColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.elong.LongExpression;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.op.stateless.Selection;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;

public class MicroAggregation implements IStreamSQLOperator, IMicroOperatorCode {
	
	private static boolean debug = false;

	private Expression[] groupByAttributes;

	private FloatColumnReference aggregationAttribute;

	private int aggregationAttributeByteLength = 4;

	private AggregationType aggregationType;

	private Selection havingSel;

	private ITupleSchema outSchema;
	
	private int byteSizeOfOutTuple;

	private LongColumnReference timestampReference = new LongColumnReference(0);

	private boolean hasGroupBy;
	private boolean doIncremental;

	public MicroAggregation(WindowDefinition windowDef, 
			AggregationType aggregationType, 
			FloatColumnReference aggregationAttribute) {

		this.aggregationType = aggregationType;
		this.aggregationAttribute = aggregationAttribute;
		this.groupByAttributes = new Expression[0];
		this.havingSel = null;
		this.hasGroupBy = false;
		
		if (this.aggregationType == AggregationType.COUNT
				|| this.aggregationType == AggregationType.SUM || this.aggregationType == AggregationType.AVG) {
			this.doIncremental = (windowDef.getSlide() < windowDef.getSize() / 2);	
		}
		
		if (this.doIncremental)
			System.out.println("[DBG] incremental computation");
		else
			System.out.println("[DBG] non-incremental computation");
		
		Expression[] tmpAllOutAttributes = new Expression[2];
		tmpAllOutAttributes[0] = this.timestampReference;
		tmpAllOutAttributes[1] = this.aggregationAttribute;
		this.outSchema = ExpressionsUtil
				.getTupleSchemaForExpressions(tmpAllOutAttributes);
		this.byteSizeOfOutTuple = outSchema.getByteSizeOfTuple();
	}

	public MicroAggregation(WindowDefinition windowDef, 
			AggregationType aggregationType,
			FloatColumnReference aggregationAttribute,
			Expression[] groupByAttributes, Selection havingSel) {
		this.aggregationType = aggregationType;
		this.aggregationAttribute = aggregationAttribute;
		this.groupByAttributes = groupByAttributes;
		this.havingSel = havingSel;
		this.hasGroupBy = true;
		
		if (this.aggregationType == AggregationType.COUNT
				|| this.aggregationType == AggregationType.SUM || this.aggregationType == AggregationType.AVG) {
			this.doIncremental = (windowDef.getSlide() < windowDef.getSize() / 2);
			// this.doIncremental = true;
		}
		
		if (this.doIncremental)
			System.out.println("[DBG] incremental computation");
		else
			System.out.println("[DBG] non-incremental computation");
		
		/*
		Expression [] tmpAllOutAttributes = new Expression[(this.groupByAttributes.length + 2)];
		tmpAllOutAttributes[0] = this.timestampReference;
		for (int i = 0; i < this.groupByAttributes.length; i++)
			tmpAllOutAttributes[i + 1] = this.groupByAttributes[i];
		
		tmpAllOutAttributes[this.groupByAttributes.length + 1] = this.aggregationAttribute;
		*/
		
		Expression [] tmpAllOutAttributes = new Expression[(this.groupByAttributes.length + 2)];
		tmpAllOutAttributes[0] = new LongColumnReference(0);
		for (int i = 1; i <= this.groupByAttributes.length; i++) {
			Expression e = this.groupByAttributes[i - 1];
			if (e instanceof IntExpression) {
				tmpAllOutAttributes[i] = new IntColumnReference(i);
			} else if (e instanceof LongExpression) {
				tmpAllOutAttributes[i] = new LongColumnReference(i);
			} else if (e instanceof FloatExpression) {
				tmpAllOutAttributes[i] = new FloatColumnReference(i);
			} else {
				throw new IllegalArgumentException("unknown expression type");
			}
		}
		tmpAllOutAttributes[this.groupByAttributes.length + 1] = 
				new FloatColumnReference(this.groupByAttributes.length + 1);
		
		this.outSchema = ExpressionsUtil.getTupleSchemaForExpressions(tmpAllOutAttributes);
		this.byteSizeOfOutTuple = outSchema.getByteSizeOfTuple();
	}

	public MicroAggregation(WindowDefinition windowDef, AggregationType aggregationType,
			FloatColumnReference aggregationAttribute,
			Expression[] groupByAttributes) {
		this(windowDef, aggregationType, aggregationAttribute, groupByAttributes, null);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(aggregationType.asString(aggregationAttribute.toString()));
		return sb.toString();
	}

	private int getGroupByKey(IQueryBuffer buffer, ITupleSchema schema, int offset, byte [] bytes) {
		/*
		int result = 1;
		
		for (int i = 0; i < this.groupByAttributes.length; i++) {
			
			this.groupByAttributes[i].evalAsByteArray(buffer, schema, offset, bytes);
			int __result = 1;
			for (int j = 0; j < bytes.length; j++)
				__result =  31 * __result + bytes[j];
			result = 31 * result + __result;
			
		}
		return result;
		*/
		return ((IntColumnReference) this.groupByAttributes[0]).eval(buffer, schema, offset);
		// return (int) ((LongColumnReference) this.groupByAttributes[0]).eval(buffer, schema, offset);
	}
	
	@Override
	public void accept(OperatorVisitor ov) {
		ov.visit(this);
	}

	@Override
	public void processData(WindowBatch windowBatch, IWindowAPI api) {
		
		/*
		 * Make sure the batch is initialised
		 */
		// System.out.println("[DBG] aggregation task " + windowBatch.getTaskId());
		windowBatch.initWindowPointers();
//		System.out.println("RUN aggregration");

		switch (aggregationType) {
		case COUNT:
		case SUM:
		case AVG:
			if (this.hasGroupBy && this.doIncremental) 
				processDataPerWindowIncrementallyWithGroupBy(windowBatch, api);
			else if (!this.hasGroupBy && this.doIncremental)
				processDataPerWindowIncrementally(windowBatch, api);
			else if (this.hasGroupBy && !this.doIncremental)
				processDataPerWindowWithGroupBy(windowBatch, api);
			else if (!this.hasGroupBy && !this.doIncremental)
				processDataPerWindow(windowBatch, api);
			break;
		case MAX:
		case MIN:
			if (this.hasGroupBy)
				processDataPerWindowWithGroupBy(windowBatch, api);
			else 
				processDataPerWindow(windowBatch, api);
				
			break;
		default:
			break;
		}
	}

	private void processDataPerWindow(WindowBatch windowBatch, IWindowAPI api) {
		// initialise pointers
		windowBatch.initWindowPointers();
		
		int[] startPointers = windowBatch.getWindowStartPointers();
		int[] endPointers = windowBatch.getWindowEndPointers();

		IQueryBuffer inBuffer = windowBatch.getBuffer();
		IQueryBuffer outBuffer = UnboundedQueryBufferFactory.newInstance();

		ITupleSchema inSchema = windowBatch.getSchema();
		int byteSizeOfInTuple = inSchema.getByteSizeOfTuple();

		int inWindowStartOffset;
		int inWindowEndOffset;
		
		float windowValue, newWindowValue;
		int   windowTupleCount;
		long  windowTimestamp;

		for (int currentWindow = 0; currentWindow < startPointers.length; currentWindow++) {
			inWindowStartOffset = startPointers[currentWindow];
			inWindowEndOffset = endPointers[currentWindow];

			windowTupleCount = 0;
			windowValue = 0;
			
			/*
			 * If the window is empty, we skip it
			 */
			if (inWindowStartOffset != -1) {

				/*
				 * First value
				 */
				// copy timestamp
				windowTimestamp = this.timestampReference.eval(inBuffer, inSchema, inWindowStartOffset);
						
				if (this.aggregationType == AggregationType.MAX || this.aggregationType == AggregationType.MIN) {
					// write value for aggregation attribute
					windowValue = this.aggregationAttribute.eval(inBuffer, inSchema, inWindowStartOffset);
				}
				else if (this.aggregationType == AggregationType.COUNT) {
					windowValue++;
				}
				else if (this.aggregationType == AggregationType.SUM || this.aggregationType == AggregationType.AVG) {
					// write value for aggregation attribute
					windowValue += this.aggregationAttribute.eval(inBuffer, inSchema, inWindowStartOffset);
					windowTupleCount++;
				}
								
				inWindowStartOffset += byteSizeOfInTuple;

				// for all remaining tuples in the window
				while (inWindowStartOffset < inWindowEndOffset) {
					
					if (this.aggregationType == AggregationType.MAX || this.aggregationType == AggregationType.MIN) {
						
						// get the value of the aggregation attribute in the current tuple
						newWindowValue = this.aggregationAttribute.eval(inBuffer, inSchema, inWindowStartOffset);
						
						if ((newWindowValue > windowValue && this.aggregationType == AggregationType.MAX)
								|| (newWindowValue < windowValue && this.aggregationType == AggregationType.MIN))
							windowValue = newWindowValue;
					}
					else if (this.aggregationType == AggregationType.COUNT) {
						windowValue++;
					}
					else if (this.aggregationType == AggregationType.SUM || this.aggregationType == AggregationType.AVG) {
						// write value for aggregation attribute
						windowValue += this.aggregationAttribute.eval(inBuffer, inSchema, inWindowStartOffset);
						windowTupleCount++;
					}

					inWindowStartOffset += byteSizeOfInTuple;
				}
				
				startPointers[currentWindow] = outBuffer.position();
				outBuffer.putLong(windowTimestamp);
				if (this.aggregationType == AggregationType.AVG)
					windowValue = windowValue / windowTupleCount;
				outBuffer.putFloat(windowValue);
				outBuffer.put(outSchema.getDummyContent());
				endPointers[currentWindow] = outBuffer.position() - 1;
			}
		}
		
		// System.out.println("[DBG] count " + count);
		
		/* Let's set the timestamp from the first tuple of the window batch */
		//outBuffer.putLong(0, windowBatch.getBuffer().getLong(windowBatch.getBatchStartPointer()));
		
		//System.out.println("In operator, set timestamp to be " + outBuffer.getLong(0) + " (" + windowBatch.getBatchStartPointer() + ")");
		
		// release old buffer (will return Unbounded Buffers to the pool)
		inBuffer.release();
		// reuse window batch by setting the new buffer and the new schema for
		// the data in this buffer
		windowBatch.setBuffer(outBuffer);
		windowBatch.setSchema(outSchema);

		api.outputWindowBatchResult(-1, windowBatch);
	}
	
	private void processDataPerWindowWithGroupBy(WindowBatch windowBatch, IWindowAPI api) {
		
		// windowBatch.initWindowPointers();
		
		
		
		int [] startPointers = windowBatch.getWindowStartPointers();
		int [] endPointers   = windowBatch.getWindowEndPointers();
		
//		System.out.println(String.format("[DBG] MicroAggregation; batch starts at %10d ends at %10d", 
//				startPointers[0], endPointers[endPointers.length - 1]));
		
		IQueryBuffer inBuffer = windowBatch.getBuffer();
		
		IQueryBuffer windowBuffer = UnboundedQueryBufferFactory.newInstance();
		IQueryBuffer outBuffer    = UnboundedQueryBufferFactory.newInstance();

		ITupleSchema inSchema = windowBatch.getSchema();
		
		int byteSizeOfInTuple = inSchema.getByteSizeOfTuple();
		
		/*
		 * The aggregate of the output stream is its last attribute; and it is a float (4 bytes)
		 */
		int offsetOutAggAttribute = outSchema.getByteSizeOfTuple() - outSchema.getDummyContent().length - 4;
		
		int inWindowStartOffset;
		int inWindowEndOffset;
		
		IntMap keyOffsets;
		IntMap windowTupleCount = null;
		
		int pid = ThreadMap.getInstance().get(Thread.currentThread().getId());
		
		if (debug)
			System.out.println(String.format("[DBG] %20s, thread id %03d pool id %03d", 
					Thread.currentThread().getName(), Thread.currentThread().getId(), pid));
		
		byte [] l_bytes = new byte [8];

		for (int currentWindow = 0; currentWindow < startPointers.length; currentWindow++) {
			
			inWindowStartOffset = startPointers[currentWindow];
			inWindowEndOffset   = endPointers  [currentWindow];
			
//			System.out.println(String.format("[DBG] MicroAggregation; window starts at %10d ends at %10d", 
//					inWindowStartOffset, inWindowEndOffset));

			/*
			 * If the window is empty, we skip it.
			 */
			if (inWindowStartOffset != -1) {

				keyOffsets = IntMapFactory.newInstance(pid);
				/* System.out.println("[DBG] keyOffsets " + keyOffsets); */
				
				/* In case of an average, get a second hash table to store the `count` per entry. */
				if (this.aggregationType == AggregationType.AVG) 
					windowTupleCount = IntMapFactory.newInstance(pid);
				
				windowBuffer.position(0);
				
				int keyOffset;
				float newValue, oldValue;

				/* For all the tuples in the window... */
				while (inWindowStartOffset < inWindowEndOffset) {
					
					/* Get the key */
					int key = getGroupByKey(inBuffer, inSchema, inWindowStartOffset, l_bytes);
					
					/* Check whether there is already an entry in `windowBuffer` for this key.
					 * If not, create a new entry */
					if (! keyOffsets.containsKey(key)) {
						
						keyOffset = windowBuffer.position();
						
						/* Copy timestamp */
						this.timestampReference.appendByteResult(inBuffer, inSchema, inWindowStartOffset, windowBuffer);
						
						/* Copy group-by attribute values */
						for (int i = 0; i < groupByAttributes.length; i++) {
							
							this.groupByAttributes[i].appendByteResult(inBuffer, inSchema, inWindowStartOffset, windowBuffer);
						}
						
						/* Write value for aggregation attribute */
						if (this.aggregationType == AggregationType.MAX || 
							this.aggregationType == AggregationType.MIN || 
							this.aggregationType == AggregationType.SUM ){
							
							this.aggregationAttribute.appendByteResult(inBuffer, inSchema, inWindowStartOffset, windowBuffer);
							
						} else 
						if (this.aggregationType == AggregationType.AVG) {
							
							windowTupleCount.put(key, 1);
							
							this.aggregationAttribute.appendByteResult(inBuffer, inSchema, inWindowStartOffset, windowBuffer);
						}
						else 
						if (this.aggregationType == AggregationType.COUNT) {
							
							windowBuffer.putFloat(1f);
						}
						
						/* Write dummy content if needed */ 
						windowBuffer.put(outSchema.getDummyContent());

						/* Record the offset for this key */
						keyOffsets.put(key, keyOffset);
						
					} else { /* Key exists already */
						
						keyOffset = keyOffsets.get(key);

						/* Check whether a new value for the aggregation attribute shall be written */
						
						int oldValuePositionInWindowBuffer = keyOffset + offsetOutAggAttribute;
						
						oldValue = windowBuffer.getFloat(oldValuePositionInWindowBuffer);
						
						if (this.aggregationType == AggregationType.COUNT) {
							
							windowBuffer.putFloat(keyOffset + offsetOutAggAttribute, oldValue + 1);
						} else {
							
							newValue = this.aggregationAttribute.eval(inBuffer, inSchema, inWindowStartOffset);
							
							if (this.aggregationType == AggregationType.SUM) {
								
								windowBuffer.putFloat(keyOffset + offsetOutAggAttribute, oldValue + newValue);
							} else 
							if (this.aggregationType == AggregationType.AVG) {
								
								windowBuffer.putFloat(keyOffset + offsetOutAggAttribute, oldValue + newValue);
								/* Inc. key count */
								windowTupleCount.put(key, windowTupleCount.get(key) + 1);
							} else 
							if ((newValue > oldValue && this.aggregationType == AggregationType.MAX) || 
								(newValue < oldValue && this.aggregationType == AggregationType.MIN)) {
								
								windowBuffer.putFloat(keyOffset + offsetOutAggAttribute, newValue);
							}
						}
					}
					
					inWindowStartOffset += byteSizeOfInTuple;
				}
					
				/*
				 * We got the aggregation result for this window. 
				 * Check whether we have a selection to apply for 
				 * each of the output partitions
				 */
				
				if (this.havingSel == null) {
					
					startPointers[currentWindow] = outBuffer.position();
					
					IntMapEntry [] entries = keyOffsets.getEntries();
					
					for (int k = 0; k < entries.length; k++) {
						
						IntMapEntry e = entries[k];
						
						while (e != null) {
							
							int partitionOffset = e.value;
						
							outBuffer.put(windowBuffer, partitionOffset, this.byteSizeOfOutTuple);
							
							if (aggregationType == AggregationType.AVG) {
								
								int countPositionInOutBuffer = outBuffer.position() - offsetOutAggAttribute;
								float avg = outBuffer.getFloat(countPositionInOutBuffer) / (float) windowTupleCount.get(e.key);
								/* Rewrite value */
								outBuffer.putFloat(countPositionInOutBuffer, avg);
							}
							e = e.next;
						}
					}
					
					endPointers[currentWindow] = outBuffer.position() - 1;
					
				} else {
					
					int tmpStart = outBuffer.position();
					
					IntMapEntry[] entries = keyOffsets.getEntries();
					
					for (int k = 0; k < entries.length; k++) {
						
						IntMapEntry e = entries[k];
						
						while (e != null) {
							
							int partitionOffset = e.value;
							
							/* Update aggregation value, in case of an average */
							float sum = -1;
							if (aggregationType == AggregationType.AVG) {
								
								sum = windowBuffer.getFloat(partitionOffset + offsetOutAggAttribute);
								float avg = sum / (float) windowTupleCount.get(e.key);
								
								windowBuffer.putFloat(partitionOffset + offsetOutAggAttribute, avg);
							}
							
							/* Apply selection predicate */
							if (this.havingSel.getPredicate().satisfied(windowBuffer, outSchema, partitionOffset)) {
								
								outBuffer.put(windowBuffer, partitionOffset, byteSizeOfOutTuple);
							}
							
							if (aggregationType == AggregationType.AVG) {
								
								/* Restore the sum in the window buffer */
								windowBuffer.putFloat(partitionOffset + offsetOutAggAttribute, sum);
							}
							e = e.next;
						}
					}

					/* Did we actually write something?
					 * 
					 * If not, set current window to be empty
					 */
					if (tmpStart == outBuffer.position()) {
						
						startPointers[currentWindow] = -1;
						endPointers  [currentWindow] = -1;
					} else {
						
						startPointers[currentWindow] = tmpStart;
						endPointers  [currentWindow] = outBuffer.position() - 1;
					}
				}
				
				/* Release hash maps */
				// System.out.println("[DBG] #keys is " + keyOffsets.size());
				keyOffsets.release();
				if (aggregationType == AggregationType.AVG) 
					windowTupleCount.release();
			}
		}
		
		/* Release window buffer (will return UnboundedQueryBuffer to the pool) */
		windowBuffer.release();

		/* Release old buffer */
		inBuffer.release();
		
		/* Reuse window batch by setting the new buffer and the new schema for
		 * the output data  */
		windowBatch.setBuffer(outBuffer);
		windowBatch.setSchema(outSchema);
		
		if (debug)
			System.out.println("[DBG] output buffer position is " + outBuffer.position());
		
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
	
	private void processDataPerWindowIncrementallyWithGroupBy(WindowBatch windowBatch,
			IWindowAPI api) {

		assert (this.aggregationType == AggregationType.COUNT
				|| this.aggregationType == AggregationType.SUM || this.aggregationType == AggregationType.AVG);

		int[] startPointers = windowBatch.getWindowStartPointers();
		int[] endPointers = windowBatch.getWindowEndPointers();

		IQueryBuffer inBuffer = windowBatch.getBuffer();
		IQueryBuffer outBuffer = UnboundedQueryBufferFactory.newInstance();
		IQueryBuffer windowBuffer = UnboundedQueryBufferFactory.newInstance();

		ITupleSchema inSchema = windowBatch.getSchema();
		int byteSizeOfInTuple = inSchema.getByteSizeOfTuple();

		int inWindowStartOffset;
		int inWindowEndOffset;

		int prevWindowStart = -1;
		int prevWindowEnd = -1;
		
		int pid = ThreadMap.getInstance().get(Thread.currentThread().getId());
		
		IntMap keyOffsets = IntMapFactory.newInstance(pid);
		IntMap windowTupleCount = null;

		if (this.aggregationType == AggregationType.SUM || this.aggregationType == AggregationType.AVG) 
			windowTupleCount = IntMapFactory.newInstance(pid);
		
		byte[] l_bytes = new byte[8];
		
		for (int currentWindow = 0; currentWindow < startPointers.length; currentWindow++) {
			inWindowStartOffset = startPointers[currentWindow];
			inWindowEndOffset = endPointers[currentWindow];

			// empty window?
			if (inWindowStartOffset == -1) {
				if (prevWindowStart != -1) {
					for (int i = prevWindowStart; i < inWindowStartOffset; i += byteSizeOfInTuple) {
						exitedWindow(inBuffer, inSchema, i, windowBuffer,
								keyOffsets, windowTupleCount, l_bytes);
					}
				}
				
				evaluateWindow(api, windowBuffer, keyOffsets, outBuffer,
						startPointers, endPointers, currentWindow,
						windowTupleCount);
			} else {
				/*
				 * Tuples in current window that have not been in the previous
				 * window
				 */
				if (prevWindowStart != -1) {
					for (int i = prevWindowEnd; i < inWindowEndOffset; i += byteSizeOfInTuple) {
						enteredWindow(inBuffer, inSchema, i, windowBuffer,
								keyOffsets, windowTupleCount, l_bytes);
					}
				} else {
					for (int i = inWindowStartOffset; i < inWindowEndOffset; i += byteSizeOfInTuple) {
						enteredWindow(inBuffer, inSchema, i, windowBuffer,
								keyOffsets, windowTupleCount, l_bytes);
					}
				}

				/*
				 * Tuples in previous window that are not in current window
				 */
				if (prevWindowStart != -1) {
					for (int i = prevWindowStart; i < inWindowStartOffset; i += byteSizeOfInTuple) {
						exitedWindow(inBuffer, inSchema, i, windowBuffer,
								keyOffsets, windowTupleCount, l_bytes);
					}
				}

				evaluateWindow(api, windowBuffer, keyOffsets, outBuffer,
						startPointers, endPointers, currentWindow,
						windowTupleCount);

				prevWindowStart = inWindowStartOffset;
				prevWindowEnd = inWindowEndOffset;
			}
		}
		
		// System.out.println("[DBG] IntMap size is " + keyOffsets.size());
		
		keyOffsets.release();
		if (aggregationType == AggregationType.AVG) 
			windowTupleCount.release();

		/* Let's set the timestamp from the first tuple of the window batch */
		// outBuffer.putLong(0, windowBatch.getBuffer().getLong(windowBatch.getBatchStartPointer()));
		// System.out.println("In operator, set timestamp to be " + outBuffer.getLong(0) + " (" + windowBatch.getBatchStartPointer() + ")");

		// release window buffer (will return Unbounded Buffers to the pool)
		windowBuffer.release();

		// release old buffer (will return Unbounded Buffers to the pool)
		inBuffer.release();
		// reuse window batch by setting the new buffer and the new schema for
		// the data in this buffer
		windowBatch.setBuffer(outBuffer);
		windowBatch.setSchema(outSchema);

		api.outputWindowBatchResult(-1, windowBatch);
	}
	
	private void processDataPerWindowIncrementally_TheOtherWay(WindowBatch windowBatch, IWindowAPI api) {

		// assert (this.aggregationType == AggregationType.SUM);
		
		int [] startPointers = windowBatch.getWindowStartPointers();
		int [] endPointers = windowBatch.getWindowEndPointers();
		
		/* Panes per batch */
		int ppb = 557056;
//				(int) windowBatch.getWindowDefinition().panesPerSlide() * windowBatch.getBatchSize() + 
//				(int) windowBatch.getWindowDefinition().numberOfPanes();
		
		float [] paneResult = new float [ppb];
		
		int windowSize = (int) windowBatch.getWindowDefinition().getSize();
		
		// System.out.println(String.format("[DBG] panes/batch %10d window size %10d", ppb, windowSize));

		IQueryBuffer inBuffer = windowBatch.getBuffer();
		IQueryBuffer outBuffer = UnboundedQueryBufferFactory.newInstance();

		ITupleSchema inSchema = windowBatch.getSchema();
		int byteSizeOfInTuple = inSchema.getByteSizeOfTuple();

		float paneValue = 0;
		
		int start = startPointers[0];
		int end = endPointers[startPointers.length - 1];
		
		// System.out.println(String.format("[DBG] batch starts at %10d ends at %10d", start, end));
		
		int currentPane = 0;
		
		int nextWindow = windowSize;
		
		for (int p = start; p < end; p += byteSizeOfInTuple) {
			
			paneValue = this.aggregationAttribute.eval(inBuffer, inSchema, p);
			
			if (currentPane > 0)
				paneResult[currentPane] = paneResult[currentPane - 1] + paneValue;
			else
				paneResult[currentPane] = paneValue;
			
			currentPane ++;
			
			if (currentPane == nextWindow) {
				
				// System.out.println(String.format("[DBG] current pane is %10d next window ends at %10d", currentPane, nextWindow));
				
				// float windowValue = 0;
				
				// for (int j = currentPane - windowSize; j < currentPane; j++)
				// 	windowValue += paneResult[j];
				
				// outBuffer.putLong(1);
				// outBuffer.putFloat(paneResult[currentPane - 1]);
				
				// outBuffer.put(outSchema.getDummyContent());
				
				// System.out.println("[DBG] window value is " + windowValue);
				
				nextWindow += 1;
				
				// System.out.print(".");
			}
		}
		
		outBuffer.getByteBuffer().asFloatBuffer().put(paneResult);
		
		inBuffer.release();
		
		windowBatch.setBuffer(outBuffer);
		windowBatch.setSchema(outSchema);

		api.outputWindowBatchResult(-1, windowBatch);
		
		// System.out.println("Done.");
	}

	private void processDataPerWindowIncrementally(WindowBatch windowBatch,
			IWindowAPI api) {

		assert (this.aggregationType == AggregationType.COUNT
				|| this.aggregationType == AggregationType.SUM || this.aggregationType == AggregationType.AVG);
		
		int [] startPointers = windowBatch.getWindowStartPointers();
		int [] endPointers = windowBatch.getWindowEndPointers();
		
		// System.out.println("[DBG] #windows/batch = " + startPointers.length);

		IQueryBuffer inBuffer = windowBatch.getBuffer();
		IQueryBuffer outBuffer = UnboundedQueryBufferFactory.newInstance();

		ITupleSchema inSchema = windowBatch.getSchema();
		int byteSizeOfInTuple = inSchema.getByteSizeOfTuple();

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

			// empty window?
			if (inWindowStartOffset == -1) {
				if (prevWindowStart != -1) {
					for (int i = prevWindowStart; i < inWindowStartOffset; i += byteSizeOfInTuple) {
						windowTupleCount--;
						if (this.aggregationType == AggregationType.SUM || this.aggregationType == AggregationType.AVG)
							windowValue -= this.aggregationAttribute.eval(inBuffer, inSchema, i);
					}
				}
				
				windowTimestamp = this.timestampReference.eval(inBuffer, inSchema, inWindowStartOffset - byteSizeOfInTuple);
				
				startPointers[currentWindow] = outBuffer.position();
				outBuffer.putLong(windowTimestamp);
				if (this.aggregationType == AggregationType.AVG)
					windowValue = windowValue / windowTupleCount;
				outBuffer.putFloat(windowValue);
				outBuffer.put(outSchema.getDummyContent());
				endPointers[currentWindow] = outBuffer.position() - 1;
				
			} else {
				/*
				 * Tuples in current window that have not been in the previous
				 * window
				 */
				if (prevWindowStart != -1) {
					for (int i = prevWindowEnd; i < inWindowEndOffset; i += byteSizeOfInTuple) {
						windowTupleCount++;
						if (this.aggregationType == AggregationType.SUM || this.aggregationType == AggregationType.AVG)
							windowValue += this.aggregationAttribute.eval(inBuffer, inSchema, i);
					}
				} else {
					for (int i = inWindowStartOffset; i < inWindowEndOffset; i += byteSizeOfInTuple) {
						windowTupleCount++;
						if (this.aggregationType == AggregationType.SUM || this.aggregationType == AggregationType.AVG)
							windowValue += this.aggregationAttribute.eval(inBuffer, inSchema, i);
					}
				}

				/*
				 * Tuples in previous window that are not in current window
				 */
				if (prevWindowStart != -1) {
					for (int i = prevWindowStart; i < inWindowStartOffset; i += byteSizeOfInTuple) {
						windowTupleCount--;
						// windowTimestamp = this.timestampReference.eval(inBuffer, inSchema, i);
						if (this.aggregationType == AggregationType.SUM || this.aggregationType == AggregationType.AVG)
							windowValue -= this.aggregationAttribute.eval(inBuffer,inSchema, i);
					}
				}

				windowTimestamp = this.timestampReference.eval(inBuffer, inSchema, inWindowStartOffset);

				startPointers[currentWindow] = outBuffer.position();
				outBuffer.putLong(windowTimestamp);
				if (this.aggregationType == AggregationType.AVG)
					windowValue = windowValue / windowTupleCount;
				outBuffer.putFloat(windowValue);
				outBuffer.put(outSchema.getDummyContent());
				endPointers[currentWindow] = outBuffer.position() - 1;

				prevWindowStart = inWindowStartOffset;
				prevWindowEnd = inWindowEndOffset;
			}
		}
		
		/* Let's set the timestamp from the first tuple of the window batch */
		// outBuffer.putLong(0, windowBatch.getBuffer().getLong(windowBatch.getBatchStartPointer()));
		// System.out.println("In operator, set timestamp to be " + outBuffer.getLong(0) + " (" + windowBatch.getBatchStartPointer() + ")");

		// release old buffer (will return Unbounded Buffers to the pool)
		inBuffer.release();
		// reuse window batch by setting the new buffer and the new schema for
		// the data in this buffer
		windowBatch.setBuffer(outBuffer);
		windowBatch.setSchema(outSchema);

		api.outputWindowBatchResult(-1, windowBatch);
	}
	
	
	private void enteredWindow(IQueryBuffer inBuffer, ITupleSchema inSchema,
			int enterOffset, IQueryBuffer windowBuffer,
			IntMap keyOffsets,
			IntMap windowTupleCount, byte[] l_byte) {

		int key = getGroupByKey(inBuffer, inSchema, enterOffset, l_byte);

		if (keyOffsets.containsKey(key)) {
			int currentValuePositionInWindowBuffer = keyOffsets.get(key)
					+ this.byteSizeOfOutTuple
					- this.aggregationAttributeByteLength;
			float currentValue = windowBuffer
					.getFloat(currentValuePositionInWindowBuffer);

			if (this.aggregationType == AggregationType.COUNT) {
				currentValue += 1;
				windowBuffer.putFloat(currentValuePositionInWindowBuffer, currentValue);
			} else if (this.aggregationType == AggregationType.SUM || this.aggregationType == AggregationType.AVG) {
				currentValue += this.aggregationAttribute.eval(inBuffer, inSchema, enterOffset);
				windowBuffer.putFloat(currentValuePositionInWindowBuffer, currentValue);
				windowTupleCount.put(key, windowTupleCount.get(key) + 1);
			}
		} else {

			int keyOffset = windowBuffer.position();
			
			// copy timestamp
			this.timestampReference.appendByteResult(inBuffer, inSchema,
					enterOffset, windowBuffer);
			
			// copy group-by attribute values
			for (int i = 0; i < groupByAttributes.length; i++)
				this.groupByAttributes[i].appendByteResult(inBuffer, inSchema,
						enterOffset, windowBuffer);
			
			// write value for aggregation attribute
			if (this.aggregationType == AggregationType.COUNT) {
				windowBuffer.putFloat(1f);
			} else if (this.aggregationType == AggregationType.SUM || this.aggregationType == AggregationType.AVG) {
				this.aggregationAttribute.appendByteResult(inBuffer, inSchema,
						enterOffset, windowBuffer);
				windowTupleCount.put(key, 1);
			}
			
			// write dummy content if needed 
			windowBuffer.put(outSchema.getDummyContent());

			// record the offset for this key
			keyOffsets.put(key, keyOffset);
		}
	}
	
	private void exitedWindow(IQueryBuffer inBuffer, ITupleSchema inSchema,
			int removeOffset, IQueryBuffer windowBuffer,
			IntMap keyOffsets,
			IntMap windowTupleCount, byte[] l_byte) {

		int key = getGroupByKey(inBuffer, inSchema, removeOffset, l_byte);

		if (keyOffsets.containsKey(key)) {
			int currentValuePositionInWindowBuffer = keyOffsets.get(key)
					+ this.byteSizeOfOutTuple
					- this.aggregationAttributeByteLength;
			float currentValue = windowBuffer
					.getFloat(currentValuePositionInWindowBuffer);

			if (this.aggregationType == AggregationType.COUNT) {
				currentValue -= 1;
				
				// is the partition empty? (check with 0.0001 because of floating
				// point inaccuracy)
				if (currentValue < 0.0001) {
					// simply remove the key, no need to remove the data from the
					// window buffer
					keyOffsets.remove(key);
				} else {
					// write new current value
					windowBuffer.putFloat(currentValuePositionInWindowBuffer, currentValue);
				}
			} else if (this.aggregationType == AggregationType.SUM || this.aggregationType == AggregationType.AVG) {
				int tupleCount = windowTupleCount.get(key);
				if (tupleCount > 1) {
					currentValue -= this.aggregationAttribute.eval(inBuffer,
							inSchema, removeOffset);
					// write new current value
					windowBuffer.putFloat(currentValuePositionInWindowBuffer, currentValue);

					windowTupleCount.put(key, tupleCount - 1);
				}
				else {
					// simply remove the key, no need to remove the data from the
					// window buffer
					keyOffsets.remove(key);
					windowTupleCount.remove(key);
				}
			}
		} else {
			throw new IllegalArgumentException(
					"Cannot remove tuple from window since it is not part of the window");
		}
	}
	
	private void evaluateWindow(IWindowAPI api, IQueryBuffer windowBuffer,
			IntMap keyOffsets, IQueryBuffer outBuffer,
			int[] startPointers, int[] endPointers, int currentWindow,
			IntMap windowTupleCount) {

		if (keyOffsets.size() == 0) {
			startPointers[currentWindow] = -1;
			endPointers[currentWindow] = -1;
		} else {
			if (this.havingSel == null) {
				startPointers[currentWindow] = outBuffer.position();
//				for (Integer key : keyOffsets.keySet()) {
//					int partitionOffset = keyOffsets.get(key);
				IntMapEntry[] entries = keyOffsets.getEntries();
				for (int k = 0; k < entries.length; k++) {
					IntMapEntry e = entries[k];
					
					while (e != null) {
						int partitionOffset = e.value;
						outBuffer.put(windowBuffer,
								partitionOffset, byteSizeOfOutTuple);
	
						/*
						 * The window buffer contains either the sum or count for
						 * the aggregation attribute, depending on the aggregation
						 * type (sum for SUM and AVG, count for COUNT). Thus, for
						 * AVG, we still need to divide by the tuple count in order
						 * to get the average
						 */
						if (aggregationType == AggregationType.AVG) {
							int countPositionInOutBuffer = outBuffer.position()
									- this.aggregationAttributeByteLength;
							float avg = outBuffer
									.getFloat(countPositionInOutBuffer)
									/ windowTupleCount.get(e.key);
							outBuffer.putFloat(countPositionInOutBuffer, avg);
						}
						e = e.next;
					}
				}

				endPointers[currentWindow] = outBuffer.position() - 1;

			} else {
				int tmpStart = outBuffer.position();
//				for (Integer key : keyOffsets.keySet()) {
//					int partitionOffset = keyOffsets.get(key);
				
				IntMapEntry[] entries = keyOffsets.getEntries();
				for (int k = 0; k < entries.length; k++) {
					IntMapEntry e = entries[k];
					
					while (e != null) {
						int partitionOffset = e.value;

						/*
						 * The window buffer contains either the sum or count for
						 * the aggregation attribute, depending on the aggregation
						 * type (sum for SUM and AVG, count for COUNT). Thus, for
						 * AVG, we still need to divide by the tuple count in order
						 * to get the average
						 * 
						 * Since we also need to check a having clause, we derive
						 * the actual avg in the window buffer, but restore the sum
						 * after the check since it is needed for subsequent windows
						 */
						float count = -1;
						if (aggregationType == AggregationType.AVG) {
							int countPositionInWindowBuffer = partitionOffset
									+ this.byteSizeOfOutTuple
									- this.aggregationAttributeByteLength;
							count = windowBuffer
									.getFloat(countPositionInWindowBuffer);
							float avg = count / windowTupleCount.get(e.key);
							windowBuffer.putFloat(countPositionInWindowBuffer, avg);
		
						}
						if (this.havingSel.getPredicate().satisfied(windowBuffer,
								outSchema, partitionOffset)) {
							outBuffer.put(windowBuffer,
									partitionOffset, byteSizeOfOutTuple);
						}
						if (aggregationType == AggregationType.AVG) {
							// restore the count in the window buffer
							int countPositionInWindowBuffer = partitionOffset
									+ this.byteSizeOfOutTuple
									- this.aggregationAttributeByteLength;
							windowBuffer.putFloat(countPositionInWindowBuffer, count);
		
						}
						e = e.next;
					}
				}

				// did we actually write something?
				if (tmpStart == outBuffer.position()) {
					startPointers[currentWindow] = -1;
					endPointers[currentWindow] = -1;
				} else {
					startPointers[currentWindow] = tmpStart;
					endPointers[currentWindow] = outBuffer.position() - 1;
				}
			}
		}
	}

	@Override
	public void processData(WindowBatch firstWindowBatch,
			WindowBatch secondWindowBatch, IWindowAPI api) {
		throw new UnsupportedOperationException("MicroAggregation is single input operator and does not operate on two streams");
	}

}
