package uk.ac.imperial.lsds.streamsql.op.stateful;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatColumnReference;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.op.stateless.Selection;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;

public class MicroPaneAggregation implements IStreamSQLOperator, IMicroOperatorCode {

	private int[] groupByAttributes;

	private FloatColumnReference aggregationAttribute;
			
	private AggregationType aggregationType;
	
	private Selection havingSel;

	@SuppressWarnings("unchecked")
	public MicroPaneAggregation(AggregationType aggregationType, FloatColumnReference aggregationAttribute) {
		this(aggregationType, aggregationAttribute, new int[0]);
	}

	public MicroPaneAggregation(AggregationType aggregationType, FloatColumnReference aggregationAttribute, int[] groupByAttributes, Selection havingSel) {
		this.aggregationType = aggregationType;
		this.aggregationAttribute = aggregationAttribute;
		this.groupByAttributes = groupByAttributes;
		this.havingSel = havingSel;
	}

	public MicroPaneAggregation(AggregationType aggregationType, FloatColumnReference aggregationAttribute, int[] groupByAttributes) {
		this(aggregationType, aggregationAttribute, groupByAttributes, null);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(aggregationType.asString(aggregationAttribute.toString()) + " ");
		return sb.toString();
	}
	
	private int getGroupByKey(MultiOpTuple tuple) {
		int result = 1;
		for (int i = 0; i < this.groupByAttributes.length; i++)
			result = this.hashMultiplier * result + this.groupByAttributes[i].eval(tuple).hashCode();
		
		return result;
	}
	
//	private String getGroupByKey(MultiOpTuple tuple) {
//		StringBuilder sb = new StringBuilder();
//		
//		for (int i = 0; i < this.groupByAttributes.length; i++) {
//			sb.append(this.groupByAttributes[i].eval(tuple).hashCode());
//			sb.append('@');
//		}
//		
//		return sb.toString();
//	}
	
	@Override
	public void accept(OperatorVisitor ov) {
		ov.visit(this);
	}	

	private PaneResult computeForPane(IWindowBatch batch, int currentPaneStart, int currentPaneEnd) {

		PaneResult paneResult = new PaneResult();
		
		Map<Integer, Integer> countInPartition = new HashMap<>();
		
		for (int i = currentPaneStart; i <= currentPaneEnd; i++) {
			MultiOpTuple tuple = batch.get(i);
			int key = getGroupByKey(tuple);
				
			PrimitiveType newValue;
			paneResult.objects.put(key, tuple);

			switch (aggregationType) {
			case COUNT:
				if (paneResult.values.containsKey(key))
					paneResult.values.put(key,paneResult.values.get(key).add(new IntegerType(1)));
				else 
					paneResult.values.put(key, new IntegerType(1));
				
				break;
			case SUM:
				newValue = (PrimitiveType) this.aggregationAttribute.eval(tuple);
				if (paneResult.values.containsKey(key))
					paneResult.values.put(key,paneResult.values.get(key).add(newValue));
				else 
					paneResult.values.put(key,newValue);
				
				break;
			case AVG:
				newValue = (PrimitiveType) this.aggregationAttribute.eval(tuple);
				if (paneResult.values.containsKey(key))
					paneResult.values.put(key,paneResult.values.get(key).add(newValue));
				else
					paneResult.values.put(key,newValue);

				if (countInPartition.containsKey(key))
					countInPartition.put(key,countInPartition.get(key) + 1);
				else 
					countInPartition.put(key,1);

				break;
			case MAX:
				newValue = (PrimitiveType) this.aggregationAttribute.eval(tuple);
				if (paneResult.values.containsKey(key)) {
					if (paneResult.values.get(key).compareTo(newValue) < 0)
						paneResult.values.put(key, newValue);
				}
				else
					paneResult.values.put(key, newValue);

				break;
			case MIN:
				newValue = (PrimitiveType) this.aggregationAttribute.eval(tuple);
				if (paneResult.values.containsKey(key)) {
					if (paneResult.values.get(key).compareTo(newValue) > 0)
						paneResult.values.put(key, newValue);
				}
				else
					paneResult.values.put(key, newValue);
				
				break;
			default:
				break;
			}
		}
		
		/*
		 * For avg, we still have to compute the actual results
		 */
		if (aggregationType.equals(AggregationType.AVG)) {
			for (Integer partitionKey : countInPartition.keySet()) 
				paneResult.values.put(partitionKey, paneResult.values.get(partitionKey).div(new FloatType(countInPartition.get(partitionKey))));
		}
		
		return paneResult;
	}

	private class PaneResult {
		Map<Integer, PrimitiveType> values = new HashMap<>();
		Map<Integer, MultiOpTuple> objects = new HashMap<>();
	}
	
	
	@Override
	public void processData(Map<Integer, IWindowBatch> windowBatches,
			IWindowAPI api) {
		
		assert(windowBatches.keySet().size() == 1);
		
		IWindowBatch batch = windowBatches.values().iterator().next();
		
		int[] startPointers = batch.getWindowStartPointers();
		int[] endPointers = batch.getWindowEndPointers();
		
		PaneResult[] paneResults = new PaneResult[2*startPointers.length-1];
		int[] paneOffsets = new int[2*startPointers.length];

		int currentPane = 0;
		int nextStart = 0;
		int nextEnd = 0;

		while (nextStart < startPointers.length && startPointers[nextStart] == -1)
			nextStart++;
		paneOffsets[0] = startPointers[nextStart++];
		
		int currentOffset = -1;
		
		/*
		 * Compute results for panes
		 */
		while (nextEnd < endPointers.length) {
			while (nextStart < startPointers.length && startPointers[nextStart] == -1)
				nextStart++;

			while (nextEnd < endPointers.length && endPointers[nextEnd] == -1)
				nextEnd++;

			// Is the next start pointer the end of the pane?
			if (nextStart < startPointers.length) {
				if (startPointers[nextStart] <= endPointers[nextEnd]) 
					// End of pane is next start pointer
					currentOffset = startPointers[nextStart];
				else 
					// End of pane is next end pointer (since nextStart < startPointers.length it also holds nextEnd < endPointers.length)
					currentOffset = endPointers[nextEnd];
			}
			else {
				// End of pane must be next end pointer (or we had an empty pane, which is checked later)
				if (nextEnd < endPointers.length) 
					// still not reached the end
					currentOffset = endPointers[nextEnd];
				else 
					currentOffset = -1;
			}
			
			if (currentOffset != -1) {
				paneResults[currentPane] = computeForPane(batch, paneOffsets[currentPane], currentOffset);
				currentPane++;
				paneOffsets[currentPane] = currentOffset;
				if (nextStart < startPointers.length) 
					if (startPointers[nextStart] == currentOffset)
						nextStart++;
				if (endPointers[nextEnd] == currentOffset)
					nextEnd++;
			}
		}
		
//		api.outputWindowResult(new MultiOpTuple[0]);

		/*
		 * Do the aggregation of panes for windows
		 */
		switch (aggregationType) {
		case COUNT:
		case SUM:
		case AVG:
			aggValues = new HashMap<>();;
			countInPartition = new HashMap<>();;
			objects = new HashMap<>();;
			performIncrementalAggregation(batch, paneOffsets, paneResults, api);
			break;
		case MAX:
		case MIN:
			aggregateDataPerWindow(startPointers, endPointers, paneOffsets, paneResults, api);
			break;
		default:
			break;
		}

	}
	
	private void aggregateDataPerWindow(int[] startPointers, int[] endPointers, int[] paneOffsets, PaneResult[] paneResults,
			IWindowAPI api) {
		
		assert(this.aggregationType.equals(AggregationType.MAX)||this.aggregationType.equals(AggregationType.MIN));

		Map<Integer, MultiOpTuple> objects = new HashMap<>();
		Map<Integer, PrimitiveType> aggValues = new HashMap<>();
		
		int currentPane = 0;
		for (int currentWindow = 0; currentWindow < startPointers.length; currentWindow++) {
			int windowStart = startPointers[currentWindow];
			int windowEnd = endPointers[currentWindow];
			
			// empty window?
			if (windowStart == -1) {
				api.outputWindowResult(new MultiOpTuple[0]);
			}
			else {
				while (paneOffsets[currentPane] < windowStart)
					currentPane++;

				int paneCount = 0;
				while (paneOffsets[currentPane + paneCount + 1] < windowEnd)
					paneCount++;
				
				for (int i = currentPane; i <= currentPane + paneCount; i++) {
					for (Integer partitionKey : paneResults[i].values.keySet()) {
						PrimitiveType newValue = paneResults[i].values.get(partitionKey);
						objects.put(partitionKey, paneResults[i].objects.get(partitionKey));
						
						if (aggValues.containsKey(partitionKey)) {
							if (aggValues.get(partitionKey).compareTo(newValue) < 0 && this.aggregationType.equals(AggregationType.MAX))
								aggValues.put(partitionKey, newValue);
							if (aggValues.get(partitionKey).compareTo(newValue) > 0 && this.aggregationType.equals(AggregationType.MIN))
								aggValues.put(partitionKey, newValue);
						}
						else
							aggValues.put(partitionKey, newValue);
					}
				}

				List<MultiOpTuple> windowResult = new ArrayList<>();
						
				for (Integer partitionKey : aggValues.keySet()) {
					MultiOpTuple refTuple = objects.get(partitionKey);
					MultiOpTuple tuple = prepareOutputTuple(refTuple, aggValues.get(partitionKey), refTuple.timestamp, refTuple.instrumentation_ts);
					if (havingSel != null) {
						if (havingSel.getPredicate().satisfied(tuple))
							windowResult.add(tuple);
					}
					else {
						windowResult.add(tuple);
					}
				}
				
				api.outputWindowResult(windowResult.toArray(new MultiOpTuple[0]));			
//				api.outputWindowResult(new MultiOpTuple[0]);						
			}			
		}		
	}
	
	private void performIncrementalAggregation(IWindowBatch batch, int[] paneOffsets, PaneResult[] paneResults,
			IWindowAPI api) {
		
		assert(this.aggregationType.equals(AggregationType.AVG)||this.aggregationType.equals(AggregationType.COUNT)||this.aggregationType.equals(AggregationType.SUM));

		int[] startPointers = batch.getWindowStartPointers();
		int[] endPointers = batch.getWindowEndPointers();

		int prevWindowStartPane = -1;
		int prevWindowEndPane = -1;
		
		int currentWindowStartPane = 0;

		for (int currentWindow = 0; currentWindow < startPointers.length; currentWindow++) {
			int windowStart = startPointers[currentWindow];
			int windowEnd = endPointers[currentWindow];
			
			while (paneOffsets[currentWindowStartPane] < windowStart)
				currentWindowStartPane++;

			int currentWindowEndPane = currentWindowStartPane;
			while (paneOffsets[currentWindowEndPane + 1] < windowEnd)
				currentWindowEndPane++;

			// empty window?
			if (windowStart == -1) {
				if (prevWindowStartPane != -1)  {
					for (int i = prevWindowStartPane; i < currentWindowStartPane; i++)
						exitedWindow(paneResults[i]);
				}				
				evaluateWindow(api);
			}
			else {
				/*
				 * Panes in current window that have not been in the previous window
				 */
				if (prevWindowStartPane != -1) {
					for (int i = prevWindowEndPane; i <= currentWindowEndPane; i++) 
						enteredWindow(paneResults[i]);
				}
				else {
					for (int i = currentWindowStartPane; i <= currentWindowEndPane; i++) 
						enteredWindow(paneResults[i]);
				}

				/*
				 * Tuples in previous window that are not in current window
				 */
				if (prevWindowStartPane != -1) 
					for (int i = prevWindowStartPane; i < currentWindowStartPane; i++)
						exitedWindow(paneResults[i]);
			
				evaluateWindow(api);
			
				prevWindowStartPane = currentWindowStartPane;
				prevWindowEndPane = currentWindowEndPane;
			}
		}
	}
	
	private void enteredWindow(PaneResult paneResult) {
		
		assert(this.aggregationType.equals(AggregationType.COUNT)
				||this.aggregationType.equals(AggregationType.SUM)
				||this.aggregationType.equals(AggregationType.AVG));

		switch (aggregationType) {
		case COUNT:
			
			for (Integer partitionKey : paneResult.values.keySet()) {
				MultiOpTuple tuple = paneResult.objects.get(partitionKey);
				objects.put(partitionKey, tuple);
				lastTimestampInWindow = tuple.timestamp;
				lastInstrumentationTimestampInWindow = tuple.instrumentation_ts;
				
				if (countInPartition.containsKey(partitionKey))
					countInPartition.put(partitionKey, countInPartition.get(partitionKey) + 1);
				else 
					countInPartition.put(partitionKey, 1);
			}
			break;
			
		case SUM:
		case AVG:
			
			for (Integer partitionKey : paneResult.values.keySet()) {
				PrimitiveType newValue = paneResult.values.get(partitionKey);
				MultiOpTuple tuple = paneResult.objects.get(partitionKey);
				objects.put(partitionKey, tuple);
				lastTimestampInWindow = tuple.timestamp;
				lastInstrumentationTimestampInWindow = tuple.instrumentation_ts;
				
				if (countInPartition.containsKey(partitionKey))
					countInPartition.put(partitionKey, countInPartition.get(partitionKey) + 1);
				else 
					countInPartition.put(partitionKey, 1);
				
				if (aggValues.containsKey(partitionKey))
					aggValues.put(partitionKey, aggValues.get(partitionKey).add(newValue));
				else
					aggValues.put(partitionKey, newValue);

			}
			break;
			
		default:
			break;
		}
	}

	private void exitedWindow(PaneResult paneResult) {
		
		assert(this.aggregationType.equals(AggregationType.COUNT)
				||this.aggregationType.equals(AggregationType.SUM)
				||this.aggregationType.equals(AggregationType.AVG));
		
		switch (aggregationType) {
		case COUNT:
			
			for (Integer partitionKey : paneResult.values.keySet()) {
				if (countInPartition.containsKey(partitionKey)) {
					countInPartition.put(partitionKey, countInPartition.get(partitionKey) - 1);
					if (countInPartition.get(partitionKey) <= 0) {
						countInPartition.remove(partitionKey);
						aggValues.remove(partitionKey);
						objects.remove(partitionKey);
					}
				}
			}
			break;
			
		case SUM:
		case AVG:
			
			for (Integer partitionKey : paneResult.values.keySet()) {
				PrimitiveType newValue = paneResult.values.get(partitionKey);				

				if (aggValues.containsKey(partitionKey))
					aggValues.put(partitionKey, aggValues.get(partitionKey).sub(newValue));

				if (countInPartition.containsKey(partitionKey)) {
					countInPartition.put(partitionKey, countInPartition.get(partitionKey) - 1);
					if (countInPartition.get(partitionKey) <= 0) {
						countInPartition.remove(partitionKey);
						aggValues.remove(partitionKey);
						objects.remove(partitionKey);
					}
				}
			}
			break;
			
		default:
			break;
		}
	}

	private void evaluateWindow(IWindowAPI api) {

		assert(this.aggregationType.equals(AggregationType.COUNT)
				||this.aggregationType.equals(AggregationType.SUM)
				||this.aggregationType.equals(AggregationType.AVG));

		MultiOpTuple[] windowResult = new MultiOpTuple[this.countInPartition.keySet().size()];

		switch (aggregationType) {
		case AVG:
			int keyCount = 0;
			for (Integer partitionKey : this.aggValues.keySet()) {
				MultiOpTuple tuple = prepareOutputTuple(this.objects.get(partitionKey), this.aggValues.get(partitionKey).div(new FloatType(this.countInPartition.get(partitionKey))), this.lastTimestampInWindow, this.lastInstrumentationTimestampInWindow);
				if (this.havingSel != null) {
					if (this.havingSel.getPredicate().satisfied(tuple))
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
			for (Integer partitionKey : this.countInPartition.keySet()) {
				MultiOpTuple tuple = prepareOutputTuple(this.objects.get(partitionKey), new IntegerType(this.countInPartition.get(partitionKey)), this.lastTimestampInWindow, this.lastInstrumentationTimestampInWindow);
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
			for (Integer partitionKey : this.aggValues.keySet()) {
				MultiOpTuple tuple = prepareOutputTuple(this.objects.get(partitionKey), this.aggValues.get(partitionKey), this.lastTimestampInWindow, this.lastInstrumentationTimestampInWindow);
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

	
	private MultiOpTuple prepareOutputTuple(MultiOpTuple object, PrimitiveType partitionValue, long timestamp, long instrumentation_ts) {

		PrimitiveType[] values = new PrimitiveType[this.groupByAttributes.length + 1];
		for (int i = 0; i < this.groupByAttributes.length; i++)
			values[i] = this.groupByAttributes[i].eval(object);
		
		values[values.length - 1] = partitionValue;
		
		return new MultiOpTuple(values, timestamp, instrumentation_ts);
	}
	
}
