package uk.ac.imperial.lsds.streamsql.op.stateful;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.lsds.seep.operator.compose.micro.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.operator.compose.micro.IStatefulMicroOperator;
import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOpTuple;
import uk.ac.imperial.lsds.seep.operator.compose.window.IMicroIncrementalComputation;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowAPI;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowBatch;
import uk.ac.imperial.lsds.streamsql.expressions.eint.ColumnReference;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.op.stateless.Selection;
import uk.ac.imperial.lsds.streamsql.types.FloatType;
import uk.ac.imperial.lsds.streamsql.types.IntegerType;
import uk.ac.imperial.lsds.streamsql.types.PrimitiveType;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;

public class MicroAggregation implements IStreamSQLOperator, IMicroOperatorCode, IMicroIncrementalComputation, IStatefulMicroOperator {

	private int hashMultiplier;
	
	private ColumnReference<PrimitiveType>[] groupByAttributes;

	private ColumnReference<PrimitiveType> aggregationAttribute;
			
	private AggregationType aggregationType;
	
	private Selection havingSel;
	
	/*
	 * State used for incremental computation
	 */
	private Map<Integer, PrimitiveType> values = new HashMap<>();
	private Map<Integer, Integer> countInPartition = new HashMap<>();
	private Map<Integer, MultiOpTuple> objectStore = new HashMap<>();

	private long lastTimestampInWindow = 0;
	private long lastInstrumentationTimestampInWindow = 0;
	
	@SuppressWarnings("unchecked")
	public MicroAggregation(AggregationType aggregationType, ColumnReference<PrimitiveType> aggregationAttribute) {
		this(aggregationType, aggregationAttribute, (ColumnReference<PrimitiveType>[]) new ColumnReference[0], null);
	}

	public MicroAggregation(AggregationType aggregationType, ColumnReference<PrimitiveType> aggregationAttribute, ColumnReference<PrimitiveType>[] groupByAttributes, Selection havingSel) {
		this(aggregationType, aggregationAttribute, (ColumnReference<PrimitiveType>[]) new ColumnReference[0], null, 211);
	}
	
	public MicroAggregation(AggregationType aggregationType, ColumnReference<PrimitiveType> aggregationAttribute, ColumnReference<PrimitiveType>[] groupByAttributes, Selection havingSel, int hashMultiplier) {
		this.aggregationType = aggregationType;
		this.aggregationAttribute = aggregationAttribute;
		this.groupByAttributes = groupByAttributes;
		this.havingSel = havingSel;
		this.hashMultiplier = hashMultiplier;
	}

	public MicroAggregation(AggregationType aggregationType, ColumnReference<PrimitiveType> aggregationAttribute, ColumnReference<PrimitiveType>[] groupByAttributes) {
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
	
	@Override
	public void processData(Map<Integer, IWindowBatch> windowBatches,
			IWindowAPI api) {
		
		assert(windowBatches.keySet().size() == 1);
		
		switch (aggregationType) {
		case COUNT:
		case SUM:
		case AVG:
			values = new HashMap<>();;
			countInPartition = new HashMap<>();;
			windowBatches.values().iterator().next().performIncrementalComputation(this, api);
			break;
		case MAX:
		case MIN:
			processDataPerWindow(windowBatches, api);
			break;
		default:
			break;
		}
	}

	private void processDataPerWindow(Map<Integer, IWindowBatch> windowBatches,
			IWindowAPI api) {
		
		assert(this.aggregationType.equals(AggregationType.MAX)||this.aggregationType.equals(AggregationType.MIN));
		
		IWindowBatch batch = windowBatches.values().iterator().next();
		
		int[] startPointers = batch.getWindowStartPointers();
		int[] endPointers = batch.getWindowEndPointers();
		
		for (int currentWindow = 0; currentWindow < startPointers.length; currentWindow++) {
			int windowStart = startPointers[currentWindow];
			int windowEnd = endPointers[currentWindow];
			
			// empty window?
			if (windowStart == -1) {
				api.outputWindowResult(new MultiOpTuple[0]);
			}
			else {

				Map<Integer, PrimitiveType> values = new HashMap<>();
				Map<Integer, MultiOpTuple> objects = new HashMap<>();
	
				MultiOpTuple[] windowResult = new MultiOpTuple[windowEnd-windowStart+1];
				
				for (int i = 0; i < windowEnd-windowStart+1; i++) {
					
					MultiOpTuple tuple = batch.get(windowStart + i);
					int key = getGroupByKey(tuple);
					objects.put(key, tuple);
					
					PrimitiveType newValue = (PrimitiveType) this.aggregationAttribute.eval(tuple);
					
					if (values.containsKey(key)) {
						if (values.get(key).compareTo(newValue) < 0 && this.aggregationType.equals(AggregationType.MAX))
							values.put(key, newValue);
						if (values.get(key).compareTo(newValue) > 0 && this.aggregationType.equals(AggregationType.MIN))
							values.put(key, newValue);
					}
					else
						values.put(key, newValue);
				}
	
				int keyCount = 0;
				for (Integer partitionKey : values.keySet()) {
					MultiOpTuple tuple = prepareOutputTuple(objects.get(partitionKey), values.get(partitionKey), batch.get(windowEnd-windowStart).timestamp, batch.get(windowEnd-windowStart).instrumentation_ts);
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
			}			
		}
	}

	
	@Override
	public void enteredWindow(MultiOpTuple tuple) {
		assert(this.aggregationType.equals(AggregationType.COUNT)
				||this.aggregationType.equals(AggregationType.SUM)
				||this.aggregationType.equals(AggregationType.AVG));

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

	@Override
	public void exitedWindow(MultiOpTuple tuple) {
		
		assert(this.aggregationType.equals(AggregationType.COUNT)
				||this.aggregationType.equals(AggregationType.SUM)
				||this.aggregationType.equals(AggregationType.AVG));
		
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

	private MultiOpTuple prepareOutputTuple(MultiOpTuple object, PrimitiveType partitionValue, long timestamp, long instrumentation_ts) {

		PrimitiveType[] values = new PrimitiveType[this.groupByAttributes.length + 1];
		for (int i = 0; i < this.groupByAttributes.length; i++)
			values[i] = this.groupByAttributes[i].eval(object);
		
		values[values.length - 1] = partitionValue;
		
		return new MultiOpTuple(values, timestamp, instrumentation_ts);
	}
	
	@Override
	public void evaluateWindow(IWindowAPI api) {

		assert(this.aggregationType.equals(AggregationType.COUNT)
				||this.aggregationType.equals(AggregationType.SUM)
				||this.aggregationType.equals(AggregationType.AVG));

		MultiOpTuple[] windowResult = new MultiOpTuple[countInPartition.keySet().size()];

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

	@Override
	public IMicroOperatorCode getNewInstance() {
		return new MicroAggregation(this.aggregationType, this.aggregationAttribute, this.groupByAttributes, this.havingSel, this.hashMultiplier);
	}

}
