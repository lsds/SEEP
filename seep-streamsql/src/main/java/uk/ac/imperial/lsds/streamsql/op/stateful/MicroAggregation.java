package uk.ac.imperial.lsds.streamsql.op.stateful;

import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.lsds.seep.operator.compose.micro.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.operator.compose.micro.IStatefulMicroOperator;
import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOpTuple;
import uk.ac.imperial.lsds.seep.operator.compose.window.IMicroIncrementalComputation;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowAPI;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowBatch;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.types.FloatType;
import uk.ac.imperial.lsds.streamsql.types.PrimitiveType;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;

public class MicroAggregation implements IStreamSQLOperator, IMicroOperatorCode, IMicroIncrementalComputation, IStatefulMicroOperator {

	public static String HASH_DELIMITER = "@";
	
	private int[] groupByAttributes;
	private int aggregationAttribute;
			
	private AggregationType aggregationType;
	
	/*
	 * State used for incremental computation
	 */
	private Map<String, PrimitiveType> values = new HashMap<>();
	private Map<String, Integer> countInPartition = new HashMap<>();
	private long lastTimestampInWindow = 0;
	private long lastInstrumentationTimestampInWindow = 0;
	
	public enum AggregationType {
		MAX, MIN, COUNT, SUM, AVG;
		
		public String asString(int s) {
			return this.toString() + "(" + s + ")";
		}
	}

	public MicroAggregation(AggregationType aggregationType, int aggregationAttribute) {
		this(aggregationType, aggregationAttribute, new int[0]);
	}

	public MicroAggregation(AggregationType aggregationType, int aggregationAttribute, int[] groupByAttributes) {
		this.aggregationType = aggregationType;
		this.aggregationAttribute = aggregationAttribute;
		this.groupByAttributes = groupByAttributes;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(aggregationType.asString(aggregationAttribute) + " ");
		return sb.toString();
	}
	
	private String getGroupByKey(MultiOpTuple tuple) {
		String result = "";
		for (int i = 0; i < this.groupByAttributes.length; i++)
			result += tuple.values[i].toString() + HASH_DELIMITER;
		
		return result;
	}
	
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
		
		long lastTimestampInWindow = 0;
		long lastInstrumentationTimestampInWindow = 0;

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

				Map<String, PrimitiveType> values = new HashMap<>();
	
				MultiOpTuple[] windowResult = new MultiOpTuple[windowEnd-windowStart+1];
				
				for (int i = 0; i < windowEnd-windowStart+1; i++) {
					
					MultiOpTuple tuple = batch.get(windowStart + i);
					String key = getGroupByKey(tuple);
					
					PrimitiveType newValue = (PrimitiveType) tuple.values[this.aggregationAttribute];
					
					lastTimestampInWindow = tuple.timestamp;
					lastInstrumentationTimestampInWindow = tuple.instrumentation_ts;
					
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
				for (String partitionKey : values.keySet()) 
					windowResult[keyCount++] = prepareOutputTuple(partitionKey, values.get(partitionKey), lastTimestampInWindow, lastInstrumentationTimestampInWindow);
	
				api.outputWindowResult(windowResult);
			}			
		}
	}

	
	@Override
	public void enteredWindow(MultiOpTuple tuple) {
		assert(this.aggregationType.equals(AggregationType.COUNT)
				||this.aggregationType.equals(AggregationType.SUM)
				||this.aggregationType.equals(AggregationType.AVG));

		String key = getGroupByKey(tuple);

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
			newValue = (PrimitiveType) tuple.values[this.aggregationAttribute];
			if (values.containsKey(key))
				values.put(key,values.get(key).add(newValue));
			else
				values.put(key,newValue);
			
			break;
		case AVG:
			newValue = (PrimitiveType) tuple.values[this.aggregationAttribute];
			if (countInPartition.containsKey(key)) {
				int currentCount = countInPartition.get(key);
				
				PrimitiveType r1 = new FloatType(currentCount*1f/(currentCount+1));
				PrimitiveType r2 = new FloatType(1f/(currentCount+1));
				PrimitiveType newAvg = 
					r1.mul(values.get(key)).add(r2.mul(newValue));				
//				double newAvg = (currentCount/(currentCount+1d))*
//						values.get(key) + (1d/(currentCount+1d))*newValue;
				values.put(key,newAvg);
			}
			else 
				values.put(key,newValue);
			break;

		default:
			break;
		}
		
		if (countInPartition.containsKey(key))
			countInPartition.put(key,countInPartition.get(key) + 1);
		else
			countInPartition.put(key,1);
	}

	@Override
	public void exitedWindow(MultiOpTuple tuple) {
		
		assert(this.aggregationType.equals(AggregationType.COUNT)
				||this.aggregationType.equals(AggregationType.SUM)
				||this.aggregationType.equals(AggregationType.AVG));
		
		String key = getGroupByKey(tuple);
		
		PrimitiveType newValue;
		
		switch (aggregationType) {
		case COUNT:
			/*
			 * Nothing to do here, since we get the value directly from the countInPartition map
			 */
			break;
		case SUM:
			newValue = (PrimitiveType) tuple.values[this.aggregationAttribute];
			if (values.containsKey(key)) {
				values.put(key,values.get(key).sub(newValue));
			}			
			break;
		case AVG:
			newValue = (PrimitiveType) tuple.values[this.aggregationAttribute];
			if (countInPartition.containsKey(key)) {
				int currentCount = countInPartition.get(key);
				
				if (currentCount > 1) {
					PrimitiveType r1 = new FloatType(currentCount*1f/(currentCount-1));
					PrimitiveType r2 = new FloatType(1f/(currentCount));
					PrimitiveType newAvg = 
						r1.mul(values.get(key)).sub(r2.mul(newValue));
//					double newAvg = (currentCount/(currentCount-1d))*
//							values.get(key) - (1d/(currentCount))*newValue;
					values.put(key,newAvg);
				}
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
			}
		}
	}

	private MultiOpTuple prepareOutputTuple(String partitionKey, PrimitiveType partitionValue, long timestamp, long instrumentation_ts) {

		String[] partitionKeys = partitionKey.split(HASH_DELIMITER);
		Object[] values = new Object[partitionKeys.length + 1];
		for (int i = 0; i < partitionKeys.length; i++)
			values[i] = partitionKeys[i];
		
		values[partitionKeys.length] = partitionValue;
		
		return MultiOpTuple.newInstance(values, timestamp, instrumentation_ts);
	}
	
	@Override
	public void evaluateWindow(IWindowAPI api) {
		MultiOpTuple[] windowResult = new MultiOpTuple[values.keySet().size()];

		int keyCount = 0;
		for (String partitionKey : values.keySet()) 
			windowResult[keyCount++] = prepareOutputTuple(partitionKey, this.values.get(partitionKey), this.lastTimestampInWindow, this.lastInstrumentationTimestampInWindow);

		api.outputWindowResult(windowResult);
	}

	@Override
	public IMicroOperatorCode getNewInstance() {
		return new MicroAggregation(this.aggregationType, this.aggregationAttribute, this.groupByAttributes);
	}

}
