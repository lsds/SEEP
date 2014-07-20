package uk.ac.imperial.lsds.streamsql.op.stateful;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.compose.micro.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.operator.compose.window.IMicroIncrementalComputation;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowAPI;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowBatch;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.op.stateful.Aggregation.AggregationType;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;

public class MicroAggregation implements IStreamSQLOperator, IMicroOperatorCode, IMicroIncrementalComputation {

	public static String HASH_DELIMITER = ";";
	
	private static Logger LOG = LoggerFactory.getLogger(Distinct.class);

	private static final long serialVersionUID = 1L;

	private String[] groupByAttributes;
	private String aggregationAttribute;
			
	private AggregationType aggregationType;
	
	/*
	 * State used for incremental computation
	 */
	private Map<String, Double> values = new HashMap<>();;
	private Map<String, Integer> countInPartition = new HashMap<>();;
	private Map<String, DataTuple> tupleRef = new HashMap<>();

	
	public enum AggregationType {
		MAX, MIN, COUNT, SUM, AVG
	}

	public MicroAggregation(AggregationType aggregationType, String aggregationAttribute) {
		this(aggregationType, aggregationAttribute, new String[0]);
	}

	public MicroAggregation(AggregationType aggregationType, String aggregationAttribute, String[] groupByAttributes) {
		this.aggregationType = aggregationType;
		this.aggregationAttribute = aggregationAttribute;
		this.groupByAttributes = groupByAttributes;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Distinct");
		return sb.toString();
	}
	
	private String getGroupByKey(DataTuple tuple) {
		String key = "";
		
		for (String att : this.groupByAttributes) 
			key += tuple.getValue(att).toString() + HASH_DELIMITER;

		return key;
	}
	
	private double toDouble(Object object) {
		return Double.parseDouble(object.toString());
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
			tupleRef = new HashMap<>();
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
		
		Iterator<List<DataTuple>> iter = windowBatches.values().iterator().next().windowIterator();
		while (iter.hasNext()) {
			List<DataTuple> windowResult = new ArrayList<>();
			Map<String, Double> values = new HashMap<>();
			Map<String, DataTuple> tupleRef = new HashMap<>();
			
			for (DataTuple tuple : iter.next()) {
				String key = getGroupByKey(tuple);
				tupleRef.put(key, tuple);
				Double newValue = toDouble(tuple.getValue(this.aggregationAttribute));
				if (values.containsKey(key)) {
					if (values.get(key) < newValue && this.aggregationType.equals(AggregationType.MAX))
						values.put(key, newValue);
					if (values.get(key) > newValue && this.aggregationType.equals(AggregationType.MIN))
						values.put(key, newValue);
				}
				else
					values.put(key, newValue);
			}
			for (String partitionKey : tupleRef.keySet()) {
				DataTuple output = tupleRef.get(partitionKey).newTuple(partitionKey,values.get(partitionKey));
				windowResult.add(output);
			}				
			api.outputWindowResult(windowResult);
		}
	}

	
	@Override
	public void enteredWindow(DataTuple tuple) {
		assert(this.aggregationType.equals(AggregationType.COUNT)
				||this.aggregationType.equals(AggregationType.SUM)
				||this.aggregationType.equals(AggregationType.AVG));

		String key = getGroupByKey(tuple);

		double newValue;
		
		switch (aggregationType) {
		case COUNT:
			if (values.containsKey(key))
				values.put(key,values.get(key) + 1d);
			else
				values.put(key,1d);
			
			break;
		case SUM:
			newValue = toDouble(tuple.getValue(this.aggregationAttribute));
			if (values.containsKey(key))
				values.put(key,values.get(key) + newValue);
			else
				values.put(key,newValue);
			
			break;
		case AVG:
			newValue = toDouble(tuple.getValue(this.aggregationAttribute));
			if (countInPartition.containsKey(key)) {
				int currentCount = countInPartition.get(key);
				double newAvg = (currentCount/(currentCount+1d))*
						values.get(key) + (1d/(currentCount+1d))*newValue;
				values.put(key,newAvg);
			}
			else 
				values.put(key,newValue);
			break;

		default:
			break;
		}
		
		tupleRef.put(key, tuple);
		if (countInPartition.containsKey(key))
			countInPartition.put(key,countInPartition.get(key) + 1);
		else
			countInPartition.put(key,1);
		
	}

	@Override
	public void exitedWindow(DataTuple tuple) {
		
		assert(this.aggregationType.equals(AggregationType.COUNT)
				||this.aggregationType.equals(AggregationType.SUM)
				||this.aggregationType.equals(AggregationType.AVG));
		
		String key = getGroupByKey(tuple);
		
		double newValue;
		
		switch (aggregationType) {
		case COUNT:
			if (values.containsKey(key)) {
				values.put(key,values.get(key) - 1d);
				if (values.get(key) <= 0) 
					values.remove(key);
			}
			
			break;
		case SUM:
			newValue = toDouble(tuple.getValue(this.aggregationAttribute));
			if (values.containsKey(key)) {
				values.put(key,values.get(key) - newValue);
				if (values.get(key) <= 0)
					values.remove(key);
			}			
			break;
		case AVG:
			newValue = toDouble(tuple.getValue(this.aggregationAttribute));
			if (countInPartition.containsKey(key)) {
				int currentCount = countInPartition.get(key);
				double newAvg = (currentCount/(currentCount-1d))*
						values.get(key) - (1d/(currentCount))*newValue;
				values.put(key,newAvg);
				
				if (countInPartition.get(key) <= 0) 
					values.remove(key);
			}
			break;

		default:
			break;
		}
		
		if (countInPartition.containsKey(key)) {
			countInPartition.put(key,countInPartition.get(key) - 1);
			if (countInPartition.get(key) <= 0) {
				countInPartition.remove(key);
				tupleRef.remove(key);
			}
		}
	}

	@Override
	public void evaluateWindow(IWindowAPI api) {
		List<DataTuple> windowResult = new ArrayList<>();

		for (String partitionKey : tupleRef.keySet()) {
			DataTuple output = tupleRef.get(partitionKey).setValues(partitionKey,values.get(partitionKey));
			windowResult.add(output);
		}

		api.outputWindowResult(windowResult);
	}

}
