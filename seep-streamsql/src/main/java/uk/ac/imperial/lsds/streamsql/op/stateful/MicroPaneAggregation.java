package uk.ac.imperial.lsds.streamsql.op.stateful;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.imperial.lsds.seep.operator.compose.micro.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.operator.compose.micro.IStatefulMicroOperator;
import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOpTuple;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowAPI;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowBatch;
import uk.ac.imperial.lsds.streamsql.expressions.ColumnReference;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.op.stateless.Selection;
import uk.ac.imperial.lsds.streamsql.types.FloatType;
import uk.ac.imperial.lsds.streamsql.types.IntegerType;
import uk.ac.imperial.lsds.streamsql.types.PrimitiveType;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;

public class MicroPaneAggregation implements IStreamSQLOperator, IMicroOperatorCode, IStatefulMicroOperator {

	private int hashMultiplier;
	
	private ColumnReference<PrimitiveType>[] groupByAttributes;

	private ColumnReference<PrimitiveType> aggregationAttribute;
			
	private AggregationType aggregationType;
	
	private Selection havingSel;
	
	@SuppressWarnings("unchecked")
	public MicroPaneAggregation(AggregationType aggregationType, ColumnReference<PrimitiveType> aggregationAttribute) {
		this(aggregationType, aggregationAttribute, (ColumnReference<PrimitiveType>[]) new ColumnReference[0], null);
	}

	public MicroPaneAggregation(AggregationType aggregationType, ColumnReference<PrimitiveType> aggregationAttribute, ColumnReference<PrimitiveType>[] groupByAttributes, Selection havingSel) {
		this(aggregationType, aggregationAttribute, (ColumnReference<PrimitiveType>[]) new ColumnReference[0], null, 211);
	}
	
	public MicroPaneAggregation(AggregationType aggregationType, ColumnReference<PrimitiveType> aggregationAttribute, ColumnReference<PrimitiveType>[] groupByAttributes, Selection havingSel, int hashMultiplier) {
		this.aggregationType = aggregationType;
		this.aggregationAttribute = aggregationAttribute;
		this.groupByAttributes = groupByAttributes;
		this.havingSel = havingSel;
		this.hashMultiplier = hashMultiplier;
	}

	public MicroPaneAggregation(AggregationType aggregationType, ColumnReference<PrimitiveType> aggregationAttribute, ColumnReference<PrimitiveType>[] groupByAttributes) {
		this(aggregationType, aggregationAttribute, groupByAttributes, null);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(aggregationType.asString(aggregationAttribute.getColumn()) + " ");
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
			paneResult.objects.put(key, tuple);
				
			PrimitiveType newValue = (PrimitiveType) this.aggregationAttribute.eval(tuple);
			
			if (i == currentPaneEnd)
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
				if (paneResult.values.containsKey(key)) {
					if (paneResult.values.get(key).compareTo(newValue) < 0)
						paneResult.values.put(key, newValue);
				}
				else
					paneResult.values.put(key, newValue);

				break;
			case MIN:
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
		while (nextStart < startPointers.length && nextEnd < endPointers.length) {
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
				if (startPointers[nextStart] == currentOffset)
					nextStart++;
				if (endPointers[nextEnd] == currentOffset)
					nextEnd++;
			}
		}
		
		currentPane = 0;

		for (int currentWindow = 0; currentWindow < startPointers.length; currentWindow++) {
			int windowStart = startPointers[currentWindow];
			int windowEnd = endPointers[currentWindow];
			
			// empty window?
			if (windowStart == -1) {
				api.outputWindowResult(new MultiOpTuple[0]);
			}
			else {
				while (paneOffsets[currentPane] <= windowStart)
					currentPane++;

				int paneCount = 0;
				while (paneOffsets[currentPane + paneCount] <= windowEnd)
					paneCount++;
				
				MultiOpTuple[] windowResult = aggregatePanesForWindow(paneResults, currentPane, paneCount);
				
				api.outputWindowResult(windowResult);			
			}			
		}
	}
	
	private MultiOpTuple[] aggregatePanesForWindow(PaneResult[] paneResults, int currentPane, int paneCount) {

		Map<Integer, PrimitiveType> aggValues = new HashMap<>();
		Map<Integer, Integer> countInPartition = new HashMap<>();
		
		for (int i = currentPane; i <= currentPane + paneCount; i++) {
			for (Integer partitionKey : paneResults[i].values.keySet()) {
				PrimitiveType newValue = paneResults[i].values.get(partitionKey);
				switch (aggregationType) {
				case AVG:
					if (countInPartition.containsKey(partitionKey))
						countInPartition.put(partitionKey, countInPartition.get(partitionKey) + 1);
					else 
						countInPartition.put(partitionKey, 1);
					// no break!
				case COUNT:
				case SUM:
					if (aggValues.containsKey(partitionKey))
						aggValues.put(partitionKey, aggValues.get(partitionKey).add(newValue));
					else
						aggValues.put(partitionKey, newValue);
					break;
				case MAX:
					if (aggValues.containsKey(partitionKey)) {
						if (aggValues.get(partitionKey).compareTo(newValue) < 0)
							aggValues.put(partitionKey, newValue);
					}
					else
						aggValues.put(partitionKey, newValue);

					break;
				case MIN:
					if (aggValues.containsKey(partitionKey)) {
						if (aggValues.get(partitionKey).compareTo(newValue) > 0)
							aggValues.put(partitionKey, newValue);
					}
					else
						aggValues.put(partitionKey, newValue);
					
					break;
				default:
					break;
				}
			}
		}
		
		List<MultiOpTuple> windowResult = new ArrayList<>();
				
		for (Integer partitionKey : aggValues.keySet()) {
			MultiOpTuple refTuple = paneResults[currentPane + paneCount].objects.get(partitionKey);
			MultiOpTuple tuple = prepareOutputTuple(refTuple, aggValues.get(partitionKey), refTuple.timestamp, refTuple.instrumentation_ts);
			if (havingSel != null) {
				if (havingSel.getPredicate().satisfied(tuple))
					windowResult.add(tuple);
			}
			else {
				windowResult.add(tuple);
			}
		}
		
		return windowResult.toArray(new MultiOpTuple[0]);
	}


	private MultiOpTuple prepareOutputTuple(MultiOpTuple object, PrimitiveType partitionValue, long timestamp, long instrumentation_ts) {

		Object[] values = new Object[this.groupByAttributes.length + 1];
		for (int i = 0; i < this.groupByAttributes.length; i++)
			values[i] = this.groupByAttributes[i].eval(object);
		
		values[values.length - 1] = partitionValue;
		
		return new MultiOpTuple(values, timestamp, instrumentation_ts);
	}
	
	@Override
	public IMicroOperatorCode getNewInstance() {
		return new MicroPaneAggregation(this.aggregationType, this.aggregationAttribute, this.groupByAttributes, this.havingSel, this.hashMultiplier);
	}

}
