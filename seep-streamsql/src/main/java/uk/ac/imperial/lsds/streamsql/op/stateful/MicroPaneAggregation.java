package uk.ac.imperial.lsds.streamsql.op.stateful;

import uk.ac.imperial.lsds.seep.multi.AggregationType;
import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.streamsql.expressions.Expression;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatColumnReference;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.op.stateless.Selection;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;

public class MicroPaneAggregation implements IStreamSQLOperator, IMicroOperatorCode {

	private Expression[] groupByAttributes;

	private FloatColumnReference aggregationAttribute;
			
	private AggregationType aggregationType;
	
	private Selection havingSel;

	private ITupleSchema outSchema;

	@Override
	public void processData(WindowBatch windowBatch, IWindowAPI api) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void accept(OperatorVisitor ov) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processData(WindowBatch firstWindowBatch,
			WindowBatch secondWindowBatch, IWindowAPI api) {
		throw new UnsupportedOperationException("MicroAggregation is single input operator and does not operate on two streams");
	}
	
//	@SuppressWarnings("unchecked")
//	public MicroPaneAggregation(AggregationType aggregationType, FloatColumnReference aggregationAttribute) {
//		this(aggregationType, aggregationAttribute, new Expression[0]);
//	}
//
//	public MicroPaneAggregation(AggregationType aggregationType, FloatColumnReference aggregationAttribute, Expression[] groupByAttributes, Selection havingSel) {
//		this.aggregationType = aggregationType;
//		this.aggregationAttribute = aggregationAttribute;
//		this.groupByAttributes = groupByAttributes;
//		this.havingSel = havingSel;
//	}
//
//	public MicroPaneAggregation(AggregationType aggregationType, FloatColumnReference aggregationAttribute, Expression[] groupByAttributes) {
//		this(aggregationType, aggregationAttribute, groupByAttributes, null);
//	}
//
//	@Override
//	public String toString() {
//		final StringBuilder sb = new StringBuilder();
//		sb.append(aggregationType.asString(aggregationAttribute.toString()) + " ");
//		return sb.toString();
//	}
//	
//	private int getGroupByKey(MultiOpTuple tuple) {
//		int result = 1;
//		for (int i = 0; i < this.groupByAttributes.length; i++)
//			result = this.hashMultiplier * result + this.groupByAttributes[i].eval(tuple).hashCode();
//		
//		return result;
//	}
//	
////	private String getGroupByKey(MultiOpTuple tuple) {
////		StringBuilder sb = new StringBuilder();
////		
////		for (int i = 0; i < this.groupByAttributes.length; i++) {
////			sb.append(this.groupByAttributes[i].eval(tuple).hashCode());
////			sb.append('@');
////		}
////		
////		return sb.toString();
////	}
//	
//	@Override
//	public void accept(OperatorVisitor ov) {
//		ov.visit(this);
//	}	
//
//	private float computeForPane(WindowBatch batch, int currentPaneStart, int currentPaneEnd, Map<Integer, Map<Integer, Float>> paneResults) {
//
//		float paneResult = new PaneResult();
//		
//		Map<Integer, Integer> countInPartition = new HashMap<>();
//		
//		for (int i = currentPaneStart; i <= currentPaneEnd; i++) {
//			MultiOpTuple tuple = batch.get(i);
//			int key = getGroupByKey(tuple);
//				
//			PrimitiveType newValue;
//			paneResult.objects.put(key, tuple);
//
//			switch (aggregationType) {
//			case COUNT:
//				if (paneResult.values.containsKey(key))
//					paneResult.values.put(key,paneResult.values.get(key).add(new IntegerType(1)));
//				else 
//					paneResult.values.put(key, new IntegerType(1));
//				
//				break;
//			case SUM:
//				newValue = (PrimitiveType) this.aggregationAttribute.eval(tuple);
//				if (paneResult.values.containsKey(key))
//					paneResult.values.put(key,paneResult.values.get(key).add(newValue));
//				else 
//					paneResult.values.put(key,newValue);
//				
//				break;
//			case AVG:
//				newValue = (PrimitiveType) this.aggregationAttribute.eval(tuple);
//				if (paneResult.values.containsKey(key))
//					paneResult.values.put(key,paneResult.values.get(key).add(newValue));
//				else
//					paneResult.values.put(key,newValue);
//
//				if (countInPartition.containsKey(key))
//					countInPartition.put(key,countInPartition.get(key) + 1);
//				else 
//					countInPartition.put(key,1);
//
//				break;
//			case MAX:
//				newValue = (PrimitiveType) this.aggregationAttribute.eval(tuple);
//				if (paneResult.values.containsKey(key)) {
//					if (paneResult.values.get(key).compareTo(newValue) < 0)
//						paneResult.values.put(key, newValue);
//				}
//				else
//					paneResult.values.put(key, newValue);
//
//				break;
//			case MIN:
//				newValue = (PrimitiveType) this.aggregationAttribute.eval(tuple);
//				if (paneResult.values.containsKey(key)) {
//					if (paneResult.values.get(key).compareTo(newValue) > 0)
//						paneResult.values.put(key, newValue);
//				}
//				else
//					paneResult.values.put(key, newValue);
//				
//				break;
//			default:
//				break;
//			}
//		}
//		
//		/*
//		 * For avg, we still have to compute the actual results
//		 */
//		if (aggregationType.equals(AggregationType.AVG)) {
//			for (Integer partitionKey : countInPartition.keySet()) 
//				paneResult.values.put(partitionKey, paneResult.values.get(partitionKey).div(new FloatType(countInPartition.get(partitionKey))));
//		}
//		
//		return paneResult;
//	}
//
//	@Override
//	public void processData(WindowBatch windowBatch, IWindowAPI api) {
//		
//		int[] startPointers = windowBatch.getWindowStartPointers();
//		int[] endPointers = windowBatch.getWindowEndPointers();
//
//		Map<Integer, Map<Integer, Float>> paneAggResults = new HashMap<>();
//		int[] paneOffsets = new int[2*startPointers.length];
//		
//		int currentPane = 0;
//		int nextStart = 0;
//		int nextEnd = 0;
//
//		while (nextStart < startPointers.length && startPointers[nextStart] == -1)
//			nextStart++;
//		paneOffsets[0] = startPointers[nextStart++];
//		
//		int currentOffset = -1;
//		
//		/*
//		 * Compute results for panes
//		 */
//		while (nextEnd < endPointers.length) {
//			while (nextStart < startPointers.length && startPointers[nextStart] == -1)
//				nextStart++;
//
//			while (nextEnd < endPointers.length && endPointers[nextEnd] == -1)
//				nextEnd++;
//
//			// Is the next start pointer the end of the pane?
//			if (nextStart < startPointers.length) {
//				if (startPointers[nextStart] <= endPointers[nextEnd]) 
//					// End of pane is next start pointer
//					currentOffset = startPointers[nextStart];
//				else 
//					// End of pane is next end pointer (since nextStart < startPointers.length it also holds nextEnd < endPointers.length)
//					currentOffset = endPointers[nextEnd];
//			}
//			else {
//				// End of pane must be next end pointer (or we had an empty pane, which is checked later)
//				if (nextEnd < endPointers.length) 
//					// still not reached the end
//					currentOffset = endPointers[nextEnd];
//				else 
//					currentOffset = -1;
//			}
//			
//			if (currentOffset != -1) {
//				computeForPane(windowBatch, paneOffsets[currentPane], currentOffset, paneAggResults);
//				currentPane++;
//				paneOffsets[currentPane] = currentOffset;
//				if (nextStart < startPointers.length) 
//					if (startPointers[nextStart] == currentOffset)
//						nextStart++;
//				if (endPointers[nextEnd] == currentOffset)
//					nextEnd++;
//			}
//		}
//
//		/*
//		 * Do the aggregation of panes for windows
//		 */
//		switch (aggregationType) {
//		case COUNT:
//		case SUM:
//		case AVG:
//			performIncrementalAggregation(windowBatch, paneOffsets, paneAggResults, api);
//			break;
//		case MAX:
//		case MIN:
//			aggregateDataPerWindow(windowBatch, startPointers, endPointers, paneOffsets, paneAggResults, api);
//			break;
//		default:
//			break;
//		}
//
//	}
//	
//	private void aggregateDataPerWindow(
//			WindowBatch windowBatch, 
//			int[] startPointers, 
//			int[] endPointers, int[] paneOffsets, Map<Integer, Map<Integer, Float>> paneResults,
//			IWindowAPI api) {
//		
//		assert(this.aggregationType.equals(AggregationType.MAX)||this.aggregationType.equals(AggregationType.MIN));
//
//		IQueryBuffer inBuffer = windowBatch.getBuffer();
//		IQueryBuffer outBuffer = UnboundedQueryBufferFactory.newInstance();
//		ITupleSchema schema = windowBatch.getSchema();
//	
//		int outWindowOffset = 0;
//		int byteSizeOfTuple = schema.getByteSizeOfTuple();
//		int currentPane = 0;
//		
//		for (int currentWindow = 0; currentWindow < startPointers.length; currentWindow++) {
//			int inWindowStartOffset = startPointers[currentWindow];
//			int inWindowEndOffset = endPointers[currentWindow];
//
//			/*
//			 * If the window is empty, we skip it 
//			 */
//			if (inWindowStartOffset != -1) {
//				
//				startPointers[currentWindow] = outWindowOffset;
//				
//				while (paneOffsets[currentPane] < inWindowStartOffset)
//					currentPane++;
//
//				int paneCount = 0;
//				while (paneOffsets[currentPane + paneCount + 1] < inWindowEndOffset)
//					paneCount++;
//				
//				Map<Integer, Float> aggValues = new HashMap<>();
//				for (int i = currentPane; i <= currentPane + paneCount; i++) {
//					for (Integer partitionKey : paneResults.get(i).keySet()) {
//						Float newValue = paneResults.get(i).get(partitionKey);
//						
//						if (aggValues.containsKey(partitionKey)) {
//							if (aggValues.get(partitionKey).compareTo(newValue) < 0 && this.aggregationType == AggregationType.MAX)
//								aggValues.put(partitionKey, newValue);
//							if (aggValues.get(partitionKey).compareTo(newValue) > 0 && this.aggregationType == AggregationType.MIN)
//								aggValues.put(partitionKey, newValue);
//						}
//						else
//							aggValues.put(partitionKey, newValue);
//					}
//				}
//
//				boolean write = true;
//				for (Integer partitionKey : aggValues.keySet()) {
//					if (havingSel != null) {
//						if (!havingSel.getPredicate().satisfied(tuple))
//							write = false;
//					}
//
//					if (write) {
//
//						long timestamp = -1;
//						outBuffer.putLong(timestamp);
//						
//						for (int i = 0; i < this.groupByAttributes.length; i++)
//							this.groupByAttributes[i].writeByteResult(inBuffer, schema, , outBuffer);
//						
//						outBuffer.putFloat(aggValues.get(partitionKey));
//						outWindowOffset += outSchema.getByteSizeOfTuple();
//					}
//					write = true;
//				}
//				
//				
//			}
//		}
//		
//		// release old buffer (will return Unbounded Buffers to the pool)
//		inBuffer.release();
//		// reuse window batch by setting the new buffer and the new schema for the data in this buffer
//		windowBatch.setBuffer(outBuffer);
//		windowBatch.setSchema(outSchema);
//		
//		api.outputWindowBatchResult(-1, windowBatch);
//		
//	}
//	
//	private void performIncrementalAggregation(WindowBatch batch, int[] paneOffsets, Map<Integer, Map<Integer, Float>> paneResults,
//			IWindowAPI api) {
//		
//		assert(this.aggregationType.equals(AggregationType.AVG)||this.aggregationType.equals(AggregationType.COUNT)||this.aggregationType.equals(AggregationType.SUM));
//
//		int[] startPointers = batch.getWindowStartPointers();
//		int[] endPointers = batch.getWindowEndPointers();
//
//		int prevWindowStartPane = -1;
//		int prevWindowEndPane = -1;
//		
//		int currentWindowStartPane = 0;
//
//		for (int currentWindow = 0; currentWindow < startPointers.length; currentWindow++) {
//			int windowStart = startPointers[currentWindow];
//			int windowEnd = endPointers[currentWindow];
//			
//			while (paneOffsets[currentWindowStartPane] < windowStart)
//				currentWindowStartPane++;
//
//			int currentWindowEndPane = currentWindowStartPane;
//			while (paneOffsets[currentWindowEndPane + 1] < windowEnd)
//				currentWindowEndPane++;
//
//			// empty window?
//			if (windowStart == -1) {
//				if (prevWindowStartPane != -1)  {
//					for (int i = prevWindowStartPane; i < currentWindowStartPane; i++)
//						exitedWindow(paneResults[i]);
//				}				
//				evaluateWindow(api);
//			}
//			else {
//				/*
//				 * Panes in current window that have not been in the previous window
//				 */
//				if (prevWindowStartPane != -1) {
//					for (int i = prevWindowEndPane; i <= currentWindowEndPane; i++) 
//						enteredWindow(paneResults[i]);
//				}
//				else {
//					for (int i = currentWindowStartPane; i <= currentWindowEndPane; i++) 
//						enteredWindow(paneResults[i]);
//				}
//
//				/*
//				 * Tuples in previous window that are not in current window
//				 */
//				if (prevWindowStartPane != -1) 
//					for (int i = prevWindowStartPane; i < currentWindowStartPane; i++)
//						exitedWindow(paneResults[i]);
//			
//				evaluateWindow(api);
//			
//				prevWindowStartPane = currentWindowStartPane;
//				prevWindowEndPane = currentWindowEndPane;
//			}
//		}
//	}
//	
//	private void enteredWindow(PaneResult paneResult) {
//		
//		assert(this.aggregationType.equals(AggregationType.COUNT)
//				||this.aggregationType.equals(AggregationType.SUM)
//				||this.aggregationType.equals(AggregationType.AVG));
//
//		switch (aggregationType) {
//		case COUNT:
//			
//			for (Integer partitionKey : paneResult.values.keySet()) {
//				MultiOpTuple tuple = paneResult.objects.get(partitionKey);
//				objects.put(partitionKey, tuple);
//				lastTimestampInWindow = tuple.timestamp;
//				lastInstrumentationTimestampInWindow = tuple.instrumentation_ts;
//				
//				if (countInPartition.containsKey(partitionKey))
//					countInPartition.put(partitionKey, countInPartition.get(partitionKey) + 1);
//				else 
//					countInPartition.put(partitionKey, 1);
//			}
//			break;
//			
//		case SUM:
//		case AVG:
//			
//			for (Integer partitionKey : paneResult.values.keySet()) {
//				PrimitiveType newValue = paneResult.values.get(partitionKey);
//				MultiOpTuple tuple = paneResult.objects.get(partitionKey);
//				objects.put(partitionKey, tuple);
//				lastTimestampInWindow = tuple.timestamp;
//				lastInstrumentationTimestampInWindow = tuple.instrumentation_ts;
//				
//				if (countInPartition.containsKey(partitionKey))
//					countInPartition.put(partitionKey, countInPartition.get(partitionKey) + 1);
//				else 
//					countInPartition.put(partitionKey, 1);
//				
//				if (aggValues.containsKey(partitionKey))
//					aggValues.put(partitionKey, aggValues.get(partitionKey).add(newValue));
//				else
//					aggValues.put(partitionKey, newValue);
//
//			}
//			break;
//			
//		default:
//			break;
//		}
//	}
//
//	private void exitedWindow(PaneResult paneResult) {
//		
//		assert(this.aggregationType.equals(AggregationType.COUNT)
//				||this.aggregationType.equals(AggregationType.SUM)
//				||this.aggregationType.equals(AggregationType.AVG));
//		
//		switch (aggregationType) {
//		case COUNT:
//			
//			for (Integer partitionKey : paneResult.values.keySet()) {
//				if (countInPartition.containsKey(partitionKey)) {
//					countInPartition.put(partitionKey, countInPartition.get(partitionKey) - 1);
//					if (countInPartition.get(partitionKey) <= 0) {
//						countInPartition.remove(partitionKey);
//						aggValues.remove(partitionKey);
//						objects.remove(partitionKey);
//					}
//				}
//			}
//			break;
//			
//		case SUM:
//		case AVG:
//			
//			for (Integer partitionKey : paneResult.values.keySet()) {
//				PrimitiveType newValue = paneResult.values.get(partitionKey);				
//
//				if (aggValues.containsKey(partitionKey))
//					aggValues.put(partitionKey, aggValues.get(partitionKey).sub(newValue));
//
//				if (countInPartition.containsKey(partitionKey)) {
//					countInPartition.put(partitionKey, countInPartition.get(partitionKey) - 1);
//					if (countInPartition.get(partitionKey) <= 0) {
//						countInPartition.remove(partitionKey);
//						aggValues.remove(partitionKey);
//						objects.remove(partitionKey);
//					}
//				}
//			}
//			break;
//			
//		default:
//			break;
//		}
//	}
//
//	private void evaluateWindow(IWindowAPI api) {
//
//		assert(this.aggregationType.equals(AggregationType.COUNT)
//				||this.aggregationType.equals(AggregationType.SUM)
//				||this.aggregationType.equals(AggregationType.AVG));
//
//		MultiOpTuple[] windowResult = new MultiOpTuple[this.countInPartition.keySet().size()];
//
//		switch (aggregationType) {
//		case AVG:
//			int keyCount = 0;
//			for (Integer partitionKey : this.aggValues.keySet()) {
//				MultiOpTuple tuple = prepareOutputTuple(this.objects.get(partitionKey), this.aggValues.get(partitionKey).div(new FloatType(this.countInPartition.get(partitionKey))), this.lastTimestampInWindow, this.lastInstrumentationTimestampInWindow);
//				if (this.havingSel != null) {
//					if (this.havingSel.getPredicate().satisfied(tuple))
//						windowResult[keyCount++] = tuple;
//				}
//				else {
//					windowResult[keyCount++] = tuple;
//				}
//			}
//			
//			if (havingSel != null) 
//				api.outputWindowResult(Arrays.copyOf(windowResult, keyCount));
//			else
//				api.outputWindowResult(windowResult);
//
//			break;
//		case COUNT:
//			keyCount = 0;
//			for (Integer partitionKey : this.countInPartition.keySet()) {
//				MultiOpTuple tuple = prepareOutputTuple(this.objects.get(partitionKey), new IntegerType(this.countInPartition.get(partitionKey)), this.lastTimestampInWindow, this.lastInstrumentationTimestampInWindow);
//				if (havingSel != null) {
//					if (havingSel.getPredicate().satisfied(tuple))
//						windowResult[keyCount++] = tuple;
//				}
//				else {
//					windowResult[keyCount++] = tuple;
//				}
//			}
//			
//			if (havingSel != null) 
//				api.outputWindowResult(Arrays.copyOf(windowResult, keyCount));
//			else
//				api.outputWindowResult(windowResult);
//			
//			break;
//			
//		case SUM:
//			keyCount = 0;
//			for (Integer partitionKey : this.aggValues.keySet()) {
//				MultiOpTuple tuple = prepareOutputTuple(this.objects.get(partitionKey), this.aggValues.get(partitionKey), this.lastTimestampInWindow, this.lastInstrumentationTimestampInWindow);
//				if (havingSel != null) {
//					if (havingSel.getPredicate().satisfied(tuple))
//						windowResult[keyCount++] = tuple;
//				}
//				else {
//					windowResult[keyCount++] = tuple;
//				}
//			}
//			
//			if (havingSel != null) 
//				api.outputWindowResult(Arrays.copyOf(windowResult, keyCount));
//			else
//				api.outputWindowResult(windowResult);
//			
//			break;
//		default:
//			break;
//		}
//	}
//
//	
//	private MultiOpTuple prepareOutputTuple(MultiOpTuple object, PrimitiveType partitionValue, long timestamp, long instrumentation_ts) {
//
//		PrimitiveType[] values = new PrimitiveType[this.groupByAttributes.length + 1];
//		for (int i = 0; i < this.groupByAttributes.length; i++)
//			values[i] = this.groupByAttributes[i].eval(object);
//		
//		values[values.length - 1] = partitionValue;
//		
//		return new MultiOpTuple(values, timestamp, instrumentation_ts);
//	}
	
}
