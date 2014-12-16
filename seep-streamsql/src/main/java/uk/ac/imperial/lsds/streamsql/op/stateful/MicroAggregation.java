package uk.ac.imperial.lsds.streamsql.op.stateful;

import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;
import uk.ac.imperial.lsds.seep.multi.UnboundedQueryBufferFactory;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.streamsql.expressions.Expression;
import uk.ac.imperial.lsds.streamsql.expressions.ExpressionsUtil;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.elong.LongColumnReference;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.op.stateless.Selection;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;

public class MicroAggregation implements IStreamSQLOperator, IMicroOperatorCode {

	private Expression[] groupByAttributes;
	// private ITupleSchema groupByAttributesSchema;

	private FloatColumnReference aggregationAttribute;
	private int aggregationAttributeByteLength = 4;

	private AggregationType aggregationType;

	private Selection havingSel;

	private ITupleSchema outSchema;
	private int byteSizeOfOutTuple;

	private LongColumnReference timestampReference = new LongColumnReference(0);

	public MicroAggregation(AggregationType aggregationType,
			FloatColumnReference aggregationAttribute) {
		this(aggregationType, aggregationAttribute, new Expression[0], null);
	}

	public MicroAggregation(AggregationType aggregationType,
			FloatColumnReference aggregationAttribute,
			Expression[] groupByAttributes, Selection havingSel) {
		this.aggregationType = aggregationType;
		this.aggregationAttribute = aggregationAttribute;
		this.groupByAttributes = groupByAttributes;
		this.havingSel = havingSel;

		// this.groupByAttributesSchema = ExpressionsUtil
		// .getTupleSchemaForExpressions(this.groupByAttributes);

		Expression[] tmpAllOutAttributes = new Expression[(this.groupByAttributes.length + 2)];
		tmpAllOutAttributes[0] = this.timestampReference;
		for (int i = 0; i < this.groupByAttributes.length; i++)
			tmpAllOutAttributes[i + 1] = this.groupByAttributes[i];

		tmpAllOutAttributes[this.groupByAttributes.length + 1] = this.aggregationAttribute;

		this.outSchema = ExpressionsUtil
				.getTupleSchemaForExpressions(tmpAllOutAttributes);
		this.byteSizeOfOutTuple = outSchema.getByteSizeOfTuple();
	}

	public MicroAggregation(AggregationType aggregationType,
			FloatColumnReference aggregationAttribute,
			Expression[] groupByAttributes) {
		this(aggregationType, aggregationAttribute, groupByAttributes, null);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(aggregationType.asString(aggregationAttribute.toString()));
		return sb.toString();
	}

	private int getGroupByKey(IQueryBuffer buffer, ITupleSchema schema,
			int offset) {
		int result = 1;

		for (int i = 0; i < this.groupByAttributes.length; i++)
			result = 31
					* result
					+ this.groupByAttributes[i].evalAsByteArray(buffer, schema,
							offset).hashCode();

		return result;
	}

	@Override
	public void accept(OperatorVisitor ov) {
		ov.visit(this);
	}

	@Override
	public void processData(WindowBatch windowBatch, IWindowAPI api) {

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
	
	private void processDataPerWindow(WindowBatch windowBatch, IWindowAPI api) {

		assert (this.aggregationType == AggregationType.MAX || this.aggregationType == AggregationType.MIN);

		int[] startPointers = windowBatch.getWindowStartPointers();
		int[] endPointers = windowBatch.getWindowEndPointers();

		IQueryBuffer inBuffer = windowBatch.getBuffer();
		IQueryBuffer windowBuffer = UnboundedQueryBufferFactory.newInstance();
		IQueryBuffer outBuffer = UnboundedQueryBufferFactory.newInstance();

		ITupleSchema inSchema = windowBatch.getSchema();
		int byteSizeOfInTuple = inSchema.getByteSizeOfTuple();

		int inWindowStartOffset;
		int inWindowEndOffset;

		// int currentWindowBufferOffset = 0;

		Map<Integer, Integer> keyOffsets;

		for (int currentWindow = 0; currentWindow < startPointers.length; currentWindow++) {
			inWindowStartOffset = startPointers[currentWindow];
			inWindowEndOffset = endPointers[currentWindow];

			/*
			 * If the window is empty, we skip it
			 */
			if (inWindowStartOffset != -1) {

				keyOffsets = new HashMap<>();

				int key, keyOffset;
				float newValue, oldValue;
				// for all the tuples in the window
				while (inWindowStartOffset <= inWindowEndOffset) {

					// get the key
					key = getGroupByKey(inBuffer, inSchema, inWindowStartOffset);
					// get the value of the aggregation attribute in the current
					// tuple
					newValue = this.aggregationAttribute.eval(inBuffer,
							inSchema, inWindowStartOffset);

					// check whether there is already an entry in the window
					// buffer for this key
					if (!keyOffsets.containsKey(key)) {
						// copy timestamp
						this.timestampReference.appendByteResult(inBuffer,
								inSchema, inWindowStartOffset, windowBuffer);
						// copy group-by attribute values
						for (int i = 0; i < groupByAttributes.length; i++)
							this.groupByAttributes[i].appendByteResult(
									inBuffer, inSchema, inWindowStartOffset,
									windowBuffer);
						// write value for aggregation attribute
						this.aggregationAttribute.appendByteResult(inBuffer,
								inSchema, inWindowStartOffset, windowBuffer);

						// record the offset for this key
						keyOffsets.put(key, windowBuffer.position());

					} else {
						// key exists already
						keyOffset = keyOffsets.get(key);
						// override timestamp
						this.timestampReference.writeByteResult(inBuffer,
								inSchema, inWindowStartOffset, windowBuffer,
								keyOffset);

						// check whether new value for aggregation attribute
						// shall be written
						oldValue = this.aggregationAttribute.eval(windowBuffer,
								outSchema, keyOffset);

						if ((newValue > oldValue && this.aggregationType == AggregationType.MAX)
								|| (newValue < oldValue && this.aggregationType == AggregationType.MIN))
							this.aggregationAttribute.writeByteResult(inBuffer,
									inSchema, inWindowStartOffset,
									windowBuffer, keyOffset);
					}

					inWindowStartOffset += byteSizeOfInTuple;
				}

				/*
				 * we got the aggregation result for the window, check whether
				 * we have a selection to apply for each of the partitions
				 */
				if (this.havingSel == null) {
					startPointers[currentWindow] = outBuffer.position();
					for (Integer partitionOffset : keyOffsets.values())
						outBuffer.put(windowBuffer,
								partitionOffset, this.byteSizeOfOutTuple);

					endPointers[currentWindow] = outBuffer.position() - 1;
				} else {
					startPointers[currentWindow] = outBuffer.position();
					for (Integer partitionOffset : keyOffsets.values())
						if (this.havingSel.getPredicate().satisfied(
								windowBuffer, outSchema, partitionOffset))
							outBuffer.put(windowBuffer,
									partitionOffset, this.byteSizeOfOutTuple);
					endPointers[currentWindow] = outBuffer.position() - 1;
				}
			}
		}
		
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

	private void processDataPerWindowIncrementally(WindowBatch windowBatch,
			IWindowAPI api) {

		assert (this.aggregationType == AggregationType.COUNT
				|| this.aggregationType == AggregationType.SUM || this.aggregationType == AggregationType.AVG);

		int[] startPointers = windowBatch.getWindowStartPointers();
		int[] endPointers = windowBatch.getWindowEndPointers();

		IQueryBuffer inBuffer = windowBatch.getBuffer();
		IQueryBuffer outBuffer = windowBatch.getBuffer();
		IQueryBuffer windowBuffer = UnboundedQueryBufferFactory.newInstance();

		ITupleSchema inSchema = windowBatch.getSchema();
		int byteSizeOfInTuple = inSchema.getByteSizeOfTuple();

		int inWindowStartOffset;
		int inWindowEndOffset;

		int prevWindowStart = -1;
		int prevWindowEnd = -1;

		Map<Integer, Integer> keyOffsets = new HashMap<>();

		// TODO: WE NEED TO HAVE THAT PER VEHICLE
		Map<Integer, Integer> windowTupleCount = new HashMap<>();

		for (int currentWindow = 0; currentWindow < startPointers.length; currentWindow++) {
			inWindowStartOffset = startPointers[currentWindow];
			inWindowEndOffset = endPointers[currentWindow];

			// empty window?
			if (inWindowStartOffset == -1) {
				if (prevWindowStart != -1) {
					for (int i = prevWindowStart; i < inWindowStartOffset; i += byteSizeOfInTuple) {
						exitedWindow(inBuffer, inSchema, i, windowBuffer,
								keyOffsets, windowTupleCount);
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
					for (int i = prevWindowEnd; i <= inWindowEndOffset; i += byteSizeOfInTuple) {
						enteredWindow(inBuffer, inSchema, i, windowBuffer,
								keyOffsets, windowTupleCount);
					}
				} else {
					for (int i = inWindowStartOffset; i <= inWindowEndOffset; i += byteSizeOfInTuple) {
						enteredWindow(inBuffer, inSchema, i, windowBuffer,
								keyOffsets, windowTupleCount);
					}
				}

				/*
				 * Tuples in previous window that are not in current window
				 */
				if (prevWindowStart != -1) {
					for (int i = prevWindowStart; i < inWindowStartOffset; i += byteSizeOfInTuple) {
						exitedWindow(inBuffer, inSchema, i, windowBuffer,
								keyOffsets, windowTupleCount);
					}
				}

				evaluateWindow(api, windowBuffer, keyOffsets, outBuffer,
						startPointers, endPointers, currentWindow,
						windowTupleCount);

				prevWindowStart = inWindowStartOffset;
				prevWindowEnd = inWindowEndOffset;
			}
		}

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

	private void enteredWindow(IQueryBuffer inBuffer, ITupleSchema inSchema,
			int enterOffset, IQueryBuffer windowBuffer,
			Map<Integer, Integer> keyOffsets,
			Map<Integer, Integer> windowTupleCount) {

		int key = getGroupByKey(inBuffer, inSchema, enterOffset);

		if (keyOffsets.keySet().contains(key)) {
			int currentValuePositionInWindowBuffer = keyOffsets.get(key)
					+ this.byteSizeOfOutTuple
					- this.aggregationAttributeByteLength;
			float currentValue = windowBuffer
					.getFloat(currentValuePositionInWindowBuffer);

			if (this.aggregationType == AggregationType.COUNT)
				currentValue += 1;
			else if (this.aggregationType == AggregationType.SUM
					|| this.aggregationType == AggregationType.AVG)
				currentValue += this.aggregationAttribute.eval(inBuffer,
						inSchema, enterOffset);

//			System.arraycopy(ExpressionsUtil.floatToByteArray(currentValue), 0,
//					windowBuffer.array(), currentValuePositionInWindowBuffer,
//					this.aggregationAttributeByteLength);
			windowBuffer.putFloat(currentValuePositionInWindowBuffer, currentValue);


			windowTupleCount.put(key, windowTupleCount.get(key) + 1);
		} else {

			// copy timestamp
			this.timestampReference.appendByteResult(inBuffer, inSchema,
					enterOffset, windowBuffer);
			// copy group-by attribute values
			for (int i = 0; i < groupByAttributes.length; i++)
				this.groupByAttributes[i].appendByteResult(inBuffer, inSchema,
						enterOffset, windowBuffer);
			// write value for aggregation attribute
			if (this.aggregationType == AggregationType.COUNT)
				windowBuffer.putFloat(1f);
			else if (this.aggregationType == AggregationType.SUM
					|| this.aggregationType == AggregationType.AVG)
				this.aggregationAttribute.appendByteResult(inBuffer, inSchema,
						enterOffset, windowBuffer);

			// record the offset for this key
			keyOffsets.put(key, windowBuffer.position());
			windowTupleCount.put(key, 1);
		}
	}

	private void exitedWindow(IQueryBuffer inBuffer, ITupleSchema inSchema,
			int removeOffset, IQueryBuffer windowBuffer,
			Map<Integer, Integer> keyOffsets,
			Map<Integer, Integer> windowTupleCount) {

		int key = getGroupByKey(inBuffer, inSchema, removeOffset);

		if (keyOffsets.keySet().contains(key)) {
			int currentValuePositionInWindowBuffer = keyOffsets.get(key)
					+ this.byteSizeOfOutTuple
					- this.aggregationAttributeByteLength;
			float currentValue = windowBuffer
					.getFloat(currentValuePositionInWindowBuffer);

			if (this.aggregationType == AggregationType.COUNT)
				currentValue -= 1;
			else if (this.aggregationType == AggregationType.SUM
					|| this.aggregationType == AggregationType.AVG)
				currentValue -= this.aggregationAttribute.eval(inBuffer,
						inSchema, removeOffset);

			// is the partition empty? (check with 0.0001 because of floating
			// point inaccuracy)
			if (currentValue < 0.0001) {
				// simply remove the key, no need to remove the data from the
				// window buffer
				keyOffsets.remove(key);
			} else {
				// write new current value
//				System.arraycopy(
//						ExpressionsUtil.floatToByteArray(currentValue), 0,
//						windowBuffer.array(),
//						currentValuePositionInWindowBuffer,
//						this.aggregationAttributeByteLength);
				windowBuffer.putFloat(currentValuePositionInWindowBuffer, currentValue);

			}
			int tupleCount = windowTupleCount.get(key);
			if (tupleCount > 1)
				windowTupleCount.put(key, tupleCount - 1);
			else
				windowTupleCount.remove(key);

		} else {
			throw new IllegalArgumentException(
					"Cannot remove tuple from window since it ");
		}
	}

	private void evaluateWindow(IWindowAPI api, IQueryBuffer windowBuffer,
			Map<Integer, Integer> keyOffsets, IQueryBuffer outBuffer,
			int[] startPointers, int[] endPointers, int currentWindow,
			Map<Integer, Integer> windowTupleCount) {

		if (keyOffsets.keySet().isEmpty()) {
			startPointers[currentWindow] = -1;
			endPointers[currentWindow] = -1;
		} else {
			if (this.havingSel == null) {
				startPointers[currentWindow] = outBuffer.position();
				for (Integer key : keyOffsets.keySet()) {
					int partitionOffset = keyOffsets.get(key);
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
								/ windowTupleCount.get(key);
//						System.arraycopy(ExpressionsUtil.floatToByteArray(avg),
//								0, outBuffer.array(), countPositionInOutBuffer,
//								this.aggregationAttributeByteLength);
						outBuffer.putFloat(countPositionInOutBuffer, avg);
					}
				}

				endPointers[currentWindow] = outBuffer.position() - 1;

			} else {
				int tmpStart = outBuffer.position();
				for (Integer key : keyOffsets.keySet()) {
					int partitionOffset = keyOffsets.get(key);
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
						float avg = count / windowTupleCount.get(key);
//						System.arraycopy(ExpressionsUtil.floatToByteArray(avg),
//								0, windowBuffer.array(),
//								countPositionInWindowBuffer,
//								this.aggregationAttributeByteLength);
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
//						System.arraycopy(
//								ExpressionsUtil.floatToByteArray(count), 0,
//								windowBuffer.array(),
//								countPositionInWindowBuffer,
//								this.aggregationAttributeByteLength);
						windowBuffer.putFloat(countPositionInWindowBuffer, count);

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
