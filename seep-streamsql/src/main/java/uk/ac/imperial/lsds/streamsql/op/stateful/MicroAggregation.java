package uk.ac.imperial.lsds.streamsql.op.stateful;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;
import uk.ac.imperial.lsds.seep.multi.TupleSchema;
import uk.ac.imperial.lsds.seep.multi.UnboundedQueryBufferFactory;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.streamsql.expressions.Expression;
import uk.ac.imperial.lsds.streamsql.expressions.ExpressionsUtil;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatExpression;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntExpression;
import uk.ac.imperial.lsds.streamsql.expressions.elong.LongColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.elong.LongExpression;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.op.stateless.Selection;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;

public class MicroAggregation implements IStreamSQLOperator, IMicroOperatorCode {

	private Expression[] groupByAttributes;
	private ITupleSchema groupByAttributesSchema;
	
	private FloatColumnReference aggregationAttribute;
			
	private AggregationType aggregationType;
	
	private Selection havingSel;
	
	private ITupleSchema outSchema;
	
	private LongColumnReference timestampReference = new LongColumnReference(0);
	
	@SuppressWarnings("unchecked")
	public MicroAggregation(AggregationType aggregationType, FloatColumnReference aggregationAttribute) {
		this(aggregationType, aggregationAttribute, new Expression[0], null);
	}

	public MicroAggregation(AggregationType aggregationType, FloatColumnReference aggregationAttribute, Expression[] groupByAttributes, Selection havingSel) {
		this.aggregationType = aggregationType;
		this.aggregationAttribute = aggregationAttribute;
		this.groupByAttributes = groupByAttributes;
		this.havingSel = havingSel;
		
		this.groupByAttributesSchema = ExpressionsUtil.getTupleSchemaForExpressions(this.groupByAttributes);
	}

	public MicroAggregation(AggregationType aggregationType, FloatColumnReference aggregationAttribute, Expression[] groupByAttributes) {
		this(aggregationType, aggregationAttribute, groupByAttributes, null);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(aggregationType.asString(aggregationAttribute.toString()));
		return sb.toString();
	}
	
	private int getGroupByKey(IQueryBuffer buffer, ITupleSchema schema, int offset) {
		ByteBuffer result = ByteBuffer.allocate(groupByAttributesSchema.getByteSizeOfTuple());
		for (int i = 0; i < this.groupByAttributes.length; i++)
			result.put(this.groupByAttributes[i].evalAsByteArray(buffer, schema, offset));
		return result.hashCode();
	}
	
	@Override
	public void accept(OperatorVisitor ov) {
		ov.visit(this);
	}	
	
	@Override
	public void processData(WindowBatch windowBatch,
			IWindowAPI api) {
		
		switch (aggregationType) {
		case COUNT:
		case SUM:
		case AVG:
			processDataPerWindowIncrementally(windowBatch, api);
			break;
		case MAX:
		case MIN:
			processDataPerWindow(windowBatch, api);
			break;
		default:
			break;
		}
	}

	private void processDataPerWindow(WindowBatch windowBatch,
			IWindowAPI api) {
		
		assert(this.aggregationType == AggregationType.MAX || this.aggregationType == AggregationType.MIN);
	
		int[] startPointers = windowBatch.getWindowStartPointers();
		int[] endPointers = windowBatch.getWindowEndPointers();

		IQueryBuffer inBuffer = windowBatch.getBuffer();
		IQueryBuffer windowBuffer = UnboundedQueryBufferFactory.newInstance();
		IQueryBuffer outBuffer = UnboundedQueryBufferFactory.newInstance();
		
		ITupleSchema inSchema = windowBatch.getSchema();
		int byteSizeOfInTuple = inSchema.getByteSizeOfTuple();
		int byteSizeOfOutTuple = outSchema.getByteSizeOfTuple();

		int outWindowOffset = 0;
		
		int inWindowStartOffset;
		int inWindowEndOffset;
		
		int currentWindowBufferOffset = 0;
		
		Map<Integer, Integer> keyOffsets;
		
		for (int currentWindow = 0; currentWindow < startPointers.length; currentWindow++) {
			inWindowStartOffset = startPointers[currentWindow];
			inWindowEndOffset = endPointers[currentWindow];
			
			/*
			 * If the window is empty, we skip it 
			 */
			if (inWindowStartOffset != -1) {

				outWindowOffset = inWindowStartOffset;
				
				keyOffsets = new HashMap<>();
				
				int key, keyOffset;
				float newValue, oldValue;
				// for all the tuples in the window
				while (inWindowStartOffset <= inWindowEndOffset) {
					
					// get the key 
					key = getGroupByKey(inBuffer, inSchema, inWindowStartOffset);
					// get the value of the aggregation attribute in the current tuple
					newValue = this.aggregationAttribute.eval(inBuffer, inSchema, inWindowStartOffset);

					// check whether there is already an entry in the window buffer for this key
					if (!keyOffsets.containsKey(key)) {
						// record the offset for this key
						keyOffsets.put(key, currentWindowBufferOffset);
						// increment the current offset by size of tuple
						currentWindowBufferOffset += byteSizeOfOutTuple;
						
						// copy timestamp
						this.timestampReference.appendByteResult(inBuffer, inSchema, inWindowStartOffset, windowBuffer);
						// copy group-by attribute values
						for (int i = 0; i < groupByAttributes.length; i++) 
							this.groupByAttributes[i].appendByteResult(inBuffer, inSchema, inWindowStartOffset, windowBuffer);
						// write value for aggregation attribute 
						this.aggregationAttribute.appendByteResult(inBuffer, inSchema, inWindowStartOffset, windowBuffer);
					}
					else {
						// key exists already
						keyOffset = keyOffsets.get(key);
						// override timestamp
						this.timestampReference.writeByteResult(inBuffer, inSchema, inWindowStartOffset, windowBuffer, keyOffset);

						// check whether new value for aggregation attribute shall be written
						oldValue = this.aggregationAttribute.eval(windowBuffer, outSchema, keyOffset);
						
						if ((newValue > oldValue && this.aggregationType == AggregationType.MAX)
								|| (newValue < oldValue && this.aggregationType == AggregationType.MIN))
							this.aggregationAttribute.writeByteResult(inBuffer, inSchema, inWindowStartOffset, windowBuffer, keyOffset);
					}
					
					inWindowStartOffset += byteSizeOfInTuple;
				}
				
				
				/*
				 * we got the aggregation result for the window, check whether we have a selection to apply for each of the partitions
				 */
				if (this.havingSel == null) {
					for (Integer partitionOffset : keyOffsets.values()) 
						outBuffer.getByteBuffer().put(windowBuffer.array(), partitionOffset, byteSizeOfOutTuple);
					
					startPointers[currentWindow] = outWindowOffset;
					endPointers[currentWindow] = outWindowOffset + keyOffsets.size() * byteSizeOfOutTuple;
				}
				else {
					int outCount = 0;
					for (Integer partitionOffset : keyOffsets.values()) {
						if (this.havingSel.getPredicate().satisfied(windowBuffer, outSchema, partitionOffset)) {
							outBuffer.getByteBuffer().put(windowBuffer.array(), partitionOffset, byteSizeOfOutTuple);
							outCount++;
						}
					}
					startPointers[currentWindow] = outWindowOffset;
					endPointers[currentWindow] = outWindowOffset + outCount * byteSizeOfOutTuple;
				}
			}
		}

		// release window buffer (will return Unbounded Buffers to the pool)
		windowBuffer.release();
			
		// release old buffer (will return Unbounded Buffers to the pool)
		inBuffer.release();
		// reuse window batch by setting the new buffer and the new schema for the data in this buffer
		windowBatch.setBuffer(outBuffer);
		windowBatch.setSchema(outSchema);
		
		api.outputWindowBatchResult(-1, windowBatch);
	}

	private void processDataPerWindowIncrementally(WindowBatch windowBatch,
			IWindowAPI api) {
		
		assert(this.aggregationType == AggregationType.COUNT
				|| this.aggregationType == AggregationType.SUM
				|| this.aggregationType == AggregationType.AVG);

		
		
		int[] startPointers = windowBatch.getWindowStartPointers();
		int[] endPointers = windowBatch.getWindowEndPointers();

		IQueryBuffer inBuffer = windowBatch.getBuffer();
		IQueryBuffer outBuffer = windowBatch.getBuffer();
		IQueryBuffer windowBuffer = UnboundedQueryBufferFactory.newInstance();
		
		ITupleSchema inSchema = windowBatch.getSchema();
		int byteSizeOfInTuple = inSchema.getByteSizeOfTuple();
		int byteSizeOfOutTuple = outSchema.getByteSizeOfTuple();

		int outWindowOffset = 0;
		
		int inWindowStartOffset;
		int inWindowEndOffset;
		
		int currentWindowBufferOffset = 0;
		
		int prevWindowStart = -1;
		int prevWindowEnd = -1;
		
		Map<Integer, Integer> keyOffsets = new HashMap<>();
		
		int windowTupleCount = 0;
		
		for (int currentWindow = 0; currentWindow < startPointers.length; currentWindow++) {
			inWindowStartOffset = startPointers[currentWindow];
			inWindowEndOffset = endPointers[currentWindow];

			// empty window?
			if (inWindowStartOffset == -1) {
				if (prevWindowStart != -1) {
					for (int i = prevWindowStart; i < inWindowStartOffset; i += byteSizeOfInTuple) {
						exitedWindow(inBuffer, i, windowBuffer, keyOffsets);
						windowTupleCount--;
					}
				}
				
				evaluateWindow(api, windowBuffer, keyOffsets, outBuffer, startPointers, endPointers, currentWindow, windowTupleCount);
			}
			else {
				/*
				 * Tuples in current window that have not been in the previous window
				 */
				if (prevWindowStart != -1) {
					for (int i = prevWindowEnd; i <= inWindowEndOffset; i += byteSizeOfInTuple) {
						enteredWindow(inBuffer, i, windowBuffer, keyOffsets);
						windowTupleCount++;
					}
				}
				else {
					for (int i = inWindowStartOffset; i <= inWindowEndOffset; i += byteSizeOfInTuple) {
						enteredWindow(inBuffer, i, windowBuffer, keyOffsets);
						windowTupleCount++;
					}
				}

				/*
				 * Tuples in previous window that are not in current window
				 */
				if (prevWindowStart != -1) {
					for (int i = prevWindowStart; i < inWindowStartOffset; i += byteSizeOfInTuple) {
						exitedWindow(inBuffer, i, windowBuffer, keyOffsets);
						windowTupleCount--;
					}
				}
			
				evaluateWindow(api, windowBuffer, keyOffsets, outBuffer, startPointers, endPointers, currentWindow, windowTupleCount);
			
				prevWindowStart = inWindowStartOffset;
				prevWindowEnd = inWindowEndOffset;
			}
		}
		
		// release window buffer (will return Unbounded Buffers to the pool)
		windowBuffer.release();
			
		// release old buffer (will return Unbounded Buffers to the pool)
		inBuffer.release();
		// reuse window batch by setting the new buffer and the new schema for the data in this buffer
		windowBatch.setBuffer(outBuffer);
		windowBatch.setSchema(outSchema);
		
		api.outputWindowBatchResult(-1, windowBatch);
	}

	private void enteredWindow(IQueryBuffer inBuffer, int removeOffset, IQueryBuffer windowBuffer, Map<Integer, Integer> keyOffsets)  {

		int key = getGroupByKey(tuple);

		lastTimestampInWindow = tuple.timestamp;
		lastInstrumentationTimestampInWindow = tuple.instrumentation_ts;
		
		PrimitiveType newValue;
		
		switch (aggregationType) {
		case COUNT:
			/*
			 * Nothing to do here, since we get the value directly from the countInPartition map
			 */
			break;
		case SUM:
		case AVG:
			newValue = (PrimitiveType) this.aggregationAttribute.eval(tuple);
			if (values.containsKey(key))
				values.put(key,values.get(key).add(newValue));
			else
				values.put(key,newValue);
			
			break;
			
		default:
			break;
		}
		
		if (countInPartition.containsKey(key))
			countInPartition.put(key,countInPartition.get(key) + 1);
		else {
			countInPartition.put(key,1);
			objectStore.put(key, tuple);
		}
	}

	private void exitedWindow(IQueryBuffer inBuffer, int removeOffset, IQueryBuffer windowBuffer, Map<Integer, Integer> keyOffsets) {
		
		int key = getGroupByKey(tuple);
		
		PrimitiveType newValue;
		
		switch (aggregationType) {
		case COUNT:
			/*
			 * Nothing to do here, since we get the value directly from the countInPartition map
			 */
			break;
		case AVG:
		case SUM:
			newValue = (PrimitiveType) this.aggregationAttribute.eval(tuple);
			if (values.containsKey(key)) {
				values.put(key,values.get(key).sub(newValue));
			}			
			break;
		default:
			break;
		}
		
		if (countInPartition.containsKey(key)) {
			countInPartition.put(key,countInPartition.get(key) - 1);
			if (countInPartition.get(key) <= 0) {
				countInPartition.remove(key);
				values.remove(key);
				objectStore.remove(key);
			}
		}
	}

	private void evaluateWindow(
			IWindowAPI api, 
			IQueryBuffer windowBuffer, 
			Map<Integer, Integer> keyOffsets, 
			IQueryBuffer outBuffer, 
			int[] startPointers, 
			int[] endPointers, 
			int currentWindow,
			int windowTupleCount
			) {

		
		if (this.havingSel == null) {
			for (Integer partitionOffset : keyOffsets.values()) 
				outBuffer.getByteBuffer().put(windowBuffer.array(), partitionOffset, byteSizeOfOutTuple);
			
			startPointers[currentWindow] = outWindowOffset;
			endPointers[currentWindow] = outWindowOffset + keyOffsets.size() * byteSizeOfOutTuple;
		}
		else {
			int outCount = 0;
			for (Integer partitionOffset : keyOffsets.values()) {
				if (this.havingSel.getPredicate().satisfied(windowBuffer, outSchema, partitionOffset)) {
					outBuffer.getByteBuffer().put(windowBuffer.array(), partitionOffset, byteSizeOfOutTuple);
					outCount++;
				}
			}
			startPointers[currentWindow] = outWindowOffset;
			endPointers[currentWindow] = outWindowOffset + outCount * byteSizeOfOutTuple;
		}
		
		
		
		switch (aggregationType) {
		case AVG:
			int keyCount = 0;
			for (Integer partitionKey : values.keySet()) {
//				PrimitiveType partitionValue = this.values.get(partitionKey).div(new FloatType(countInPartition.get(partitionKey)));
//				windowResult[keyCount++] = prepareOutputTuple(this.objectStore.get(partitionKey), partitionValue, this.lastTimestampInWindow, this.lastInstrumentationTimestampInWindow);
				MultiOpTuple tuple = prepareOutputTuple(this.objectStore.get(partitionKey), this.values.get(partitionKey).div(new FloatType(countInPartition.get(partitionKey))), this.lastTimestampInWindow, this.lastInstrumentationTimestampInWindow);
				if (havingSel != null) {
					if (havingSel.getPredicate().satisfied(tuple))
						windowResult[keyCount++] = tuple;
				}
				else {
					windowResult[keyCount++] = tuple;
				}
			}
			
			if (havingSel != null) 
				api.outputWindowResult(Arrays.copyOf(windowResult, keyCount));
			else
				api.outputWindowResult(windowResult);

			break;
		case COUNT:
			keyCount = 0;
			for (Integer partitionKey : countInPartition.keySet()) {
				//windowResult[keyCount++] = prepareOutputTuple(this.objectStore.get(partitionKey), this.values.get(partitionKey), this.lastTimestampInWindow, this.lastInstrumentationTimestampInWindow);
				MultiOpTuple tuple = prepareOutputTuple(this.objectStore.get(partitionKey), new IntegerType(this.countInPartition.get(partitionKey)), this.lastTimestampInWindow, this.lastInstrumentationTimestampInWindow);
				if (havingSel != null) {
					if (havingSel.getPredicate().satisfied(tuple))
						windowResult[keyCount++] = tuple;
				}
				else {
					windowResult[keyCount++] = tuple;
				}
			}
			
			if (havingSel != null) 
				api.outputWindowResult(Arrays.copyOf(windowResult, keyCount));
			else
				api.outputWindowResult(windowResult);
			
			break;
			
		case SUM:
			keyCount = 0;
			for (Integer partitionKey : values.keySet()) {
				//windowResult[keyCount++] = prepareOutputTuple(this.objectStore.get(partitionKey), this.values.get(partitionKey), this.lastTimestampInWindow, this.lastInstrumentationTimestampInWindow);
				MultiOpTuple tuple = prepareOutputTuple(this.objectStore.get(partitionKey), this.values.get(partitionKey), this.lastTimestampInWindow, this.lastInstrumentationTimestampInWindow);
				if (havingSel != null) {
					if (havingSel.getPredicate().satisfied(tuple))
						windowResult[keyCount++] = tuple;
				}
				else {
					windowResult[keyCount++] = tuple;
				}
			}
			
			if (havingSel != null) 
				api.outputWindowResult(Arrays.copyOf(windowResult, keyCount));
			else
				api.outputWindowResult(windowResult);
			
			break;
		default:
			break;
		}
	}
}
