package uk.ac.imperial.lsds.streamsql.op.stateful;

import java.nio.ByteBuffer;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;
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

public class MultiMicroAggregation 
	implements IStreamSQLOperator, IMicroOperatorCode {
	
	private static boolean debug = false;

	private Expression [] groupByAttributes;
	
	/* There can be more than one aggregation attributes in a single query */
	private FloatColumnReference [] aggregationAttribute;
	
	/* There can be more than one aggregation types in a single query (e.g. min, max, sum) */
	private AggregationType [] aggregationType;
	
	private Expression [] aggregationExpression;
	
	private int aggregationAttributeByteLength = 4;

	private Selection havingSel;
	
	private Selection filterSel;
	
	private ITupleSchema outSchema;
	
	private int byteSizeOfOutTuple;

	private LongColumnReference timestampReference = new LongColumnReference(0);

	private boolean hasGroupBy;
	private boolean doIncremental;

	public MultiMicroAggregation(
			WindowDefinition windowDef, 
			AggregationType [] aggregationType, 
			FloatColumnReference [] aggregationAttribute) {

		this.aggregationType = aggregationType;
		this.aggregationAttribute = aggregationAttribute;
		
		this.groupByAttributes = new Expression [0];
		
		this.havingSel = null;
		this.filterSel = null;
		
		this.aggregationExpression = null;
		
		this.hasGroupBy = false;
		
		this.doIncremental = true;
		for (int i = 0; i < this.aggregationType.length; i++) {
			if (
				this.aggregationType[i] != AggregationType.COUNT && 
				this.aggregationType[i] != AggregationType.SUM   && 
				this.aggregationType[i] != AggregationType.AVG  ) {
			
				this.doIncremental = false;
			}
		}
		/* Check window definition */
		if (this.doIncremental)
			this.doIncremental = (windowDef.getSlide() < windowDef.getSize() / 2);	
		
		if (this.doIncremental)
			System.out.println("[DBG] incremental computation");
		else
			System.out.println("[DBG] non-incremental computation");
		
		Expression [] tmpAllOutAttributes = new Expression[1 + this.aggregationType.length];
		tmpAllOutAttributes[0] = this.timestampReference;
		for (int i = 1; i < tmpAllOutAttributes.length; i++) {
			tmpAllOutAttributes[i] = new FloatColumnReference(i);
		}
		this.outSchema = ExpressionsUtil.getTupleSchemaForExpressions(tmpAllOutAttributes);
		
		this.byteSizeOfOutTuple = outSchema.getByteSizeOfTuple();
	}
	
	public MultiMicroAggregation (
			WindowDefinition windowDef, 
			AggregationType [] aggregationType,
			FloatColumnReference [] aggregationAttribute,
			Expression [] aggregationExpression,
			Expression [] groupByAttributes, 
			Selection havingSel,
			Selection filterSel) {
		
		this.aggregationType = aggregationType;
		this.aggregationAttribute = aggregationAttribute;
		
		this.aggregationExpression = aggregationExpression;
		
		this.groupByAttributes = groupByAttributes;
		
		this.havingSel = havingSel;
		this.filterSel = filterSel;
		
		this.hasGroupBy = true;
		
		this.doIncremental = true;
		for (int i = 0; i < this.aggregationType.length; i++) {
			if (
				this.aggregationType[i] != AggregationType.COUNT && 
				this.aggregationType[i] != AggregationType.SUM   && 
				this.aggregationType[i] != AggregationType.AVG  ) {
			
				this.doIncremental = false;
			}
		}
		/* Check window definition */
		if (this.doIncremental)
			this.doIncremental = (windowDef.getSlide() < windowDef.getSize() / 2);
		
		Expression [] tmpAllOutAttributes = 
				new Expression[1 + this.groupByAttributes.length + this.aggregationType.length];
		
		tmpAllOutAttributes[0] = new LongColumnReference(0);
		
		for (int i = 1; i <= this.groupByAttributes.length; i++) {
			
			Expression e = this.groupByAttributes[i - 1];
			
			if (e instanceof   IntExpression) { tmpAllOutAttributes[i] = new   IntColumnReference(i);
			} else 
			if (e instanceof  LongExpression) { tmpAllOutAttributes[i] = new  LongColumnReference(i);
			} else 
			if (e instanceof FloatExpression) { tmpAllOutAttributes[i] = new FloatColumnReference(i);
			} else 
			{
				throw new IllegalArgumentException("unknown expression type");
			}
		}
		for (int i = this.groupByAttributes.length + 1; i < tmpAllOutAttributes.length; i++) {
			
			tmpAllOutAttributes[i] = new FloatColumnReference(i);
		}
		this.outSchema = ExpressionsUtil.getTupleSchemaForExpressions(tmpAllOutAttributes);
		
		this.byteSizeOfOutTuple = outSchema.getByteSizeOfTuple();
	}

	public MultiMicroAggregation(
			WindowDefinition windowDef, 
			AggregationType [] aggregationType,
			FloatColumnReference [] aggregationAttribute,
			Expression [] aggregationExpression,
			Expression [] groupByAttributes) {
		
		this(windowDef, aggregationType, aggregationAttribute, aggregationExpression, groupByAttributes, null, null);
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < this.aggregationType.length; i++) {
			sb.append(aggregationType[i].asString(aggregationAttribute[i].toString()));
			if (i != this.aggregationType.length - 1)
				sb.append(",");
		}
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
		*/
		// this.groupByAttributes[0].evalAsByteArray(buffer, schema, offset, bytes);
		// ByteBuffer b = ByteBuffer.wrap(bytes);
		return ((IntColumnReference) this.groupByAttributes[0]).eval(buffer, schema, offset);
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
		windowBatch.initWindowPointers();

		if (  this.hasGroupBy &&   this.doIncremental) processDataPerWindowIncrementallyWithGroupBy(windowBatch, api);
		else 
		if (! this.hasGroupBy &&   this.doIncremental) processDataPerWindowIncrementally(windowBatch, api);
		else 
		if (  this.hasGroupBy && ! this.doIncremental) processDataPerWindowWithGroupBy(windowBatch, api);
		else 
		if (! this.hasGroupBy && ! this.doIncremental) processDataPerWindow(windowBatch, api);
	}

	private void processDataPerWindow (WindowBatch windowBatch, IWindowAPI api) {
		
		/* windowBatch.initWindowPointers(); */
		
		int [] startPointers = windowBatch.getWindowStartPointers();
		int []   endPointers = windowBatch.getWindowEndPointers();

		IQueryBuffer  inBuffer = windowBatch.getBuffer();
		IQueryBuffer outBuffer = UnboundedQueryBufferFactory.newInstance();

		ITupleSchema inSchema = windowBatch.getSchema();
		int byteSizeOfInTuple = inSchema.getByteSizeOfTuple();
		
		int inWindowStartOffset;
		int inWindowEndOffset;
		
		float []    windowValue = new float [this.aggregationAttribute.length];
		float [] newWindowValue = new float [this.aggregationAttribute.length];
		
		int   [] windowTupleCount = new int [this.aggregationAttribute.length];
		
		long windowTimestamp;

		for (int currentWindow = 0; currentWindow < startPointers.length; currentWindow++) {
			
			inWindowStartOffset = startPointers[currentWindow];
			inWindowEndOffset   = endPointers  [currentWindow];

			/* Initialise local state */
			for (int i = 0; i < this.aggregationAttribute.length; i++) {
				windowValue[i] = 0;
				newWindowValue[i] = 0;
				windowTupleCount[i] = 0;
			}
			
			/*
			 * If the window is empty, we skip it
			 */
			if (inWindowStartOffset != -1) {
				/* Copy timestamp */
				windowTimestamp = this.timestampReference.eval(inBuffer, inSchema, inWindowStartOffset);
				
				for (int i = 0; i < this.aggregationAttribute.length; i++) {
					
					if (
						this.aggregationType[i] == AggregationType.MAX || 
						this.aggregationType[i] == AggregationType.MIN) {
						
						windowValue[i] = this.aggregationAttribute[i].eval(inBuffer, inSchema, inWindowStartOffset);
					}
					else 
					if (this.aggregationType[i] == AggregationType.COUNT) {
						
						windowValue[i]++;
					}
					else 
					if (
						this.aggregationType[i] == AggregationType.SUM || 
						this.aggregationType[i] == AggregationType.AVG) {
					
						windowValue[i] += this.aggregationAttribute[i].eval(inBuffer, inSchema, inWindowStartOffset);
						windowTupleCount[i]++;
					}
				}
				
				inWindowStartOffset += byteSizeOfInTuple;

				/* For all remaining tuples in the window... */
				while (inWindowStartOffset < inWindowEndOffset) {
					
					for (int i = 0; i < this.aggregationAttribute.length; i++) {
						
						if (
							this.aggregationType[i] == AggregationType.MAX || 
							this.aggregationType[i] == AggregationType.MIN) {
						
							/* Get the value of the aggregation attribute in the current tuple */
							newWindowValue[i] = this.aggregationAttribute[i].eval(inBuffer, inSchema, inWindowStartOffset);
						
							if (
								(newWindowValue[i] > windowValue[i] && this.aggregationType [i] == AggregationType.MAX) || 
								(newWindowValue[i] < windowValue[i] && this.aggregationType [i] == AggregationType.MIN)) {
								
								windowValue[i] = newWindowValue[i];
							}
						}
						else 
						if (this.aggregationType[i] == AggregationType.COUNT) {
							windowValue[i]++;
						}
						else
						if (
							this.aggregationType[i] == AggregationType.SUM || 
							this.aggregationType[i] == AggregationType.AVG) {
						
							windowValue[i] += this.aggregationAttribute[i].eval(inBuffer, inSchema, inWindowStartOffset);
							windowTupleCount[i]++;
						}
					}

					inWindowStartOffset += byteSizeOfInTuple;
				}
				
				startPointers[currentWindow] = outBuffer.position();
				/* Write output tuple */
				outBuffer.putLong(windowTimestamp);
				for (int i = 0; i < this.aggregationAttribute.length; i++) {
					
					if (this.aggregationType[i] == AggregationType.AVG)
						windowValue[i] = windowValue[i] / windowTupleCount[i];
					
					outBuffer.putFloat(windowValue[i]);
				}
				outBuffer.put(outSchema.getDummyContent());
				endPointers[currentWindow] = outBuffer.position() - 1;
			}
		}
		
		inBuffer.release();
		
		windowBatch.setBuffer(outBuffer);
		windowBatch.setSchema(outSchema);

		api.outputWindowBatchResult(-1, windowBatch);
	}
	
	private void processDataPerWindowWithGroupBy (WindowBatch windowBatch, IWindowAPI api) {
		
		/* windowBatch.initWindowPointers(); */
		
		int [] startPointers = windowBatch.getWindowStartPointers();
		int [] endPointers   = windowBatch.getWindowEndPointers();
		
		IQueryBuffer inBuffer = windowBatch.getBuffer();
		
		IQueryBuffer windowBuffer = UnboundedQueryBufferFactory.newInstance();
		IQueryBuffer outBuffer    = UnboundedQueryBufferFactory.newInstance();
		
		ITupleSchema inSchema = windowBatch.getSchema();
		
		int byteSizeOfInTuple = inSchema.getByteSizeOfTuple();
		
		/*
		 * The aggregate of the output stream is its last attribute; and it is a float (4 bytes)
		 */
		int offsetOutAggAttribute = outSchema.getByteSizeOfTuple() - outSchema.getDummyContent().length - 4 * aggregationAttribute.length;
		
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

			/*
			 * If the window is empty, we skip it.
			 */
			if (inWindowStartOffset != -1) {

				keyOffsets = IntMapFactory.newInstance(pid);
				/* System.out.println("[DBG] keyOffsets " + keyOffsets); */
				
				/* In case of an average, get a second hash table to store the `count` per entry. */
				boolean hasAvg = false;
				for (int i = 0; i < this.aggregationAttribute.length; i++)
					if (this.aggregationType[i] == AggregationType.AVG)
						hasAvg = true;
				if (hasAvg)
					windowTupleCount = IntMapFactory.newInstance(pid);
				
				windowBuffer.position(0);
				
				int keyOffset;
				float newValue, oldValue;
				
				/* For all the tuples in the window... */
				while (inWindowStartOffset < inWindowEndOffset) {
					
					/* Get the key */
					int key = getGroupByKey(inBuffer, inSchema, inWindowStartOffset, l_bytes);
					
					// System.out.println("[DBG] new key is " + inBuffer.getInt(inWindowStartOffset + 8 + 4 * (aggregationAttribute.length)));
					
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
						
						/* Write values for aggregation attributes */
						
						boolean tallied = false; /* We want to increment the window tuple count once. */
						
						for (int i = 0; i < this.aggregationAttribute.length; i++) {
							
							if (this.aggregationType[i] == AggregationType.MAX || 
								this.aggregationType[i] == AggregationType.MIN || 
								this.aggregationType[i] == AggregationType.SUM ){
							
								this.aggregationAttribute[i].appendByteResult(inBuffer, inSchema, inWindowStartOffset, windowBuffer);
							
							} else 
							if (this.aggregationType[i] == AggregationType.AVG) {
								if (! tallied) {
									windowTupleCount.put(key, 1);
									tallied = true;
								}
								this.aggregationAttribute[i].appendByteResult(inBuffer, inSchema, inWindowStartOffset, windowBuffer);
							}
							else 
							if (this.aggregationType[i] == AggregationType.COUNT) {
							
								windowBuffer.putFloat(1f);
							}
						}
						/* Write dummy content if needed */ 
						windowBuffer.put(outSchema.getDummyContent());

						/* Record the offset for this key */
						keyOffsets.put(key, keyOffset);
						
					} else { /* Key exists already */
						
						keyOffset = keyOffsets.get(key);
						
						boolean tallied = false; /* We want to increment the window tuple count once. */

						/* Check whether a new value for the aggregation attributes shall be written */
						for (int i = 0; i < this.aggregationAttribute.length; i++) {
						
							int oldValuePositionInWindowBuffer = keyOffset + offsetOutAggAttribute + (i * 4);
						
							oldValue = windowBuffer.getFloat(oldValuePositionInWindowBuffer);
						
							if (this.aggregationType[i] == AggregationType.COUNT) {
							
								windowBuffer.putFloat(oldValuePositionInWindowBuffer, oldValue + 1);
							} else {
						
								newValue = this.aggregationAttribute[i].eval(inBuffer, inSchema, inWindowStartOffset);
								
								if (this.aggregationType[i] == AggregationType.SUM) {
								
									windowBuffer.putFloat(oldValuePositionInWindowBuffer, oldValue + newValue);
								} else 
								if (this.aggregationType[i] == AggregationType.AVG) {
								
									windowBuffer.putFloat(oldValuePositionInWindowBuffer, oldValue + newValue);
									/* Inc. key count */
									if (! tallied) {
										windowTupleCount.put(key, windowTupleCount.get(key) + 1);
										tallied = true;
									}
								} else 
								if (
								(newValue > oldValue && this.aggregationType[i] == AggregationType.MAX) || 
								(newValue < oldValue && this.aggregationType[i] == AggregationType.MIN)) {
								
									windowBuffer.putFloat(oldValuePositionInWindowBuffer, newValue);
								}
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
					
					// int count = 0;
					
					for (int k = 0; k < entries.length; k++) {
						
						IntMapEntry e = entries[k];
						
						while (e != null) {
							
							int partitionOffset = e.value;
						
							outBuffer.put(windowBuffer, partitionOffset, this.byteSizeOfOutTuple);
							
//							for (int i = 0; i < aggregationExpression.length; i++) {
//								aggregationExpression[i].appendByteResult(windowBuffer, outSchema, partitionOffset, outBuffer);
//							}
							
							for (int i = 0; i < this.aggregationAttribute.length; i++) {
								if (aggregationType[i] == AggregationType.AVG) {
									int countPositionInOutBuffer = outBuffer.position() - offsetOutAggAttribute + (i * 4);
									float avg = outBuffer.getFloat(countPositionInOutBuffer) / (float) windowTupleCount.get(e.key);
									/* Rewrite value */
									outBuffer.putFloat(countPositionInOutBuffer, avg);
								}
							}
							e = e.next;
							
							// count ++;
						}
					}
					
					// System.out.println("[DBG] window count is " + count + " " +  keyOffsets.size() + " entries");
					
					endPointers[currentWindow] = outBuffer.position() - 1;
					
				} else {
					
					int tmpStart = outBuffer.position();
					
					IntMapEntry[] entries = keyOffsets.getEntries();
					
					for (int k = 0; k < entries.length; k++) {
						
						IntMapEntry e = entries[k];
						
						while (e != null) {
							
							int partitionOffset = e.value;
							
							/* Update aggregation value, in case of an average */
							for (int i = 0; i < this.aggregationAttribute.length; i++) {
								
								if (aggregationType[i] == AggregationType.AVG) {
									
									float sum = windowBuffer.getFloat(partitionOffset + offsetOutAggAttribute + (i * 4));
									float avg = sum / (float) windowTupleCount.get(e.key);
									windowBuffer.putFloat(partitionOffset + offsetOutAggAttribute + (i * 4), avg);
								}
							}
							
							/* Apply selection predicate */
							if (this.havingSel.getPredicate().satisfied(windowBuffer, outSchema, partitionOffset)) {
								
								outBuffer.put(windowBuffer, partitionOffset, byteSizeOfOutTuple);
								
//								for (int i = 0; i < aggregationExpression.length; i++) {
//									aggregationExpression[i].appendByteResult(windowBuffer, outSchema, partitionOffset, outBuffer);
//								}
							}
							
							/* TODO
							 * 
							 * Why do we have to restore the sum value to the window buffer?
							 * Does it matter?
							 * 
							 */
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
				keyOffsets.release();
				if (hasAvg) 
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
	
	private void processDataPerWindowIncrementallyWithGroupBy(WindowBatch windowBatch, IWindowAPI api) {
		
		throw new UnsupportedOperationException("Incremental computation for multi-aggregate queries is not supported yet.");
	}

	private void processDataPerWindowIncrementally (WindowBatch windowBatch, IWindowAPI api) {
		
		throw new UnsupportedOperationException("Incremental computation for multi-aggregate queries is not supported yet.");
	}

	@Override
	public void processData(WindowBatch firstWindowBatch, WindowBatch secondWindowBatch, IWindowAPI api) {
		
		throw new UnsupportedOperationException
		("MultiMicroAggregation is single input operator and does not operate on two streams");
	}

}
