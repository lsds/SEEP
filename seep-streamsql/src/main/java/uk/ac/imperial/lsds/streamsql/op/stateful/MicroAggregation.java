package uk.ac.imperial.lsds.streamsql.op.stateful;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.Payload;
import uk.ac.imperial.lsds.seep.operator.compose.micro.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.operator.compose.micro.IStatefulMicroOperator;
import uk.ac.imperial.lsds.seep.operator.compose.window.IMicroIncrementalComputation;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowAPI;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowBatch;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;

public class MicroAggregation implements IStreamSQLOperator, IMicroOperatorCode, IMicroIncrementalComputation, IStatefulMicroOperator {

	public static String HASH_DELIMITER = ";";
	
	private static Logger LOG = LoggerFactory.getLogger(Distinct.class);

	private static final long serialVersionUID = 1L;

	private String[] groupByAttributes;
	private String aggregationAttribute;
			
	private AggregationType aggregationType;
	
	private  Map<String, Integer> idxMapper = null;
	
	/*
	 * State used for incremental computation
	 */
	private Map<String, Double> values = new HashMap<>();
	private Map<String, Integer> countInPartition = new HashMap<>();
	
	public enum AggregationType {
		MAX, MIN, COUNT, SUM, AVG;
		
		public String asString(String s) {
			return this.toString() + "(" + s + ")";
		}
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
		sb.append(aggregationType.asString(aggregationAttribute) + " ");
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
				windowResult.add(prepareOutputTuple(partitionKey, values.get(partitionKey)));
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
			}
			
			break;
		case SUM:
			newValue = toDouble(tuple.getValue(this.aggregationAttribute));
			if (values.containsKey(key)) {
				values.put(key,values.get(key) - newValue);
			}			
			break;
		case AVG:
			newValue = toDouble(tuple.getValue(this.aggregationAttribute));
			if (countInPartition.containsKey(key)) {
				int currentCount = countInPartition.get(key);
				if (currentCount > 1) {
					double newAvg = (currentCount/(currentCount-1d))*
							values.get(key) - (1d/(currentCount))*newValue;
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

	private DataTuple prepareOutputTuple(String partitionKey, Double partitionValue) {
		if (this.idxMapper == null) {
			this.idxMapper = new HashMap<>();
			for (int i = 0 ; i < groupByAttributes.length; i++)
				this.idxMapper.put(groupByAttributes[i],i);
			this.idxMapper.put(aggregationType.asString(aggregationAttribute),groupByAttributes.length);
		}
		String[] partitionKeys = partitionKey.split(";");
		Object[] objects = new Object[partitionKeys.length + 1];
		for (int i = 0; i < partitionKeys.length; i++)
			objects[i] = partitionKeys[i];
		
		objects[objects.length-1] = (Object)partitionValue;
		
		DataTuple t = new DataTuple(this.idxMapper, new Payload(objects));
//		t.getPayload().timestamp = tuple.getPayload().timestamp;
//		t.getPayload().instrumentation_ts = tuple.getPayload().instrumentation_ts;
		return t;
	}
	
	@Override
	public void evaluateWindow(IWindowAPI api) {
		List<DataTuple> windowResult = new ArrayList<>();

		for (String partitionKey : values.keySet()) {
			windowResult.add(prepareOutputTuple(partitionKey, values.get(partitionKey)));
		}

		api.outputWindowResult(windowResult);
	}

	@Override
	public IMicroOperatorCode getNewInstance() {
		return new MicroAggregation(this.aggregationType, this.aggregationAttribute, this.groupByAttributes);
	}

}
