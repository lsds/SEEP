package uk.ac.imperial.lsds.streamsql.op.stateful;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.API;
import uk.ac.imperial.lsds.seep.operator.StatefulOperator;
import uk.ac.imperial.lsds.seep.state.State;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;
import uk.ac.imperial.lsds.streamsql.windows.Window;

public class Aggregation implements StatefulOperator, IStreamSQLOperator, WindowOperator {

	public static String HASH_DELIMITER = ";";
	
	private static Logger LOG = LoggerFactory.getLogger(Distinct.class);

	private static final long serialVersionUID = 1L;

	Window window;
	
	List<String> groupByAttributes;
	String aggregationAttribute;
	
	private transient AggregationState state;
	
	public class AggregationState {
		public Map<String, Double> values = new HashMap<>();;
		public Map<String, Integer> countInPartition = new HashMap<>();;
		public Map<String, DataTuple> tupleRef = new HashMap<>();
	}
	
	private AggregationType aggregationType;
	
	public enum AggregationType {
		MAX, MIN, COUNT, SUM, AVG
	}


	public Aggregation(AggregationType aggregationType, String aggregationAttribute) {
		this(aggregationType, aggregationAttribute, new ArrayList<String>());
	}

	public Aggregation(AggregationType aggregationType, String aggregationAttribute, List<String> groupByAttributes) {
		this.aggregationType = aggregationType;
		this.aggregationAttribute = aggregationAttribute;
		this.groupByAttributes = groupByAttributes;
	}
	
	@Override
	public void setUp() {
		this.window.registerCallbackEvaluateWindow(this);
		this.window.registerCallbackEnterWindow(this);
		this.window.registerCallbackExitWindow(this);
	}

	@Override
	public void processData(DataTuple data, API api) {
		this.window.updateWindow(data);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Distinct");
		return sb.toString();
	}

	@Override
	public void processData(List<DataTuple> dataList, API api) {
		this.window.registerAPI(this, api);
		this.window.updateWindow(dataList);
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
	public void enteredWindow(DataTuple tuple, API api) {
		
		String key = getGroupByKey(tuple);

		double newValue;
		
		switch (aggregationType) {
		case COUNT:
			if (this.state.values.containsKey(key))
				this.state.values.put(key,this.state.values.get(key) + 1d);
			else
				this.state.values.put(key,1d);
			
			break;
		case SUM:
			newValue = toDouble(tuple.getValue(this.aggregationAttribute));
			if (this.state.values.containsKey(key))
				this.state.values.put(key,this.state.values.get(key) + newValue);
			else
				this.state.values.put(key,newValue);
			
			break;
		case AVG:
			newValue = toDouble(tuple.getValue(this.aggregationAttribute));
			if (this.state.countInPartition.containsKey(key)) {
				int currentCount = this.state.countInPartition.get(key);
				double newAvg = (currentCount/(currentCount+1d))*
						this.state.values.get(key) + (1d/(currentCount+1d))*newValue;
				this.state.values.put(key,newAvg);
			}
			else 
				this.state.values.put(key,newValue);
			break;

		default:
			break;
		}
		
		this.state.tupleRef.put(key, tuple);
		if (this.state.countInPartition.containsKey(key))
			this.state.countInPartition.put(key,this.state.countInPartition.get(key) + 1);
		else
			this.state.countInPartition.put(key,1);

	}

	@Override
	public void exitedWindow(DataTuple tuple, API api) {
		
		String key = getGroupByKey(tuple);
		
		double newValue;
		
		switch (aggregationType) {
		case COUNT:
			if (this.state.values.containsKey(key)) {
				this.state.values.put(key,this.state.values.get(key) - 1d);
				if (this.state.values.get(key) <= 0) 
					this.state.values.remove(key);
			}
			
			break;
		case SUM:
			newValue = toDouble(tuple.getValue(this.aggregationAttribute));
			if (this.state.values.containsKey(key)) {
				this.state.values.put(key,this.state.values.get(key) - newValue);
				if (this.state.values.get(key) <= 0)
					this.state.values.remove(key);
			}			
			break;
		case AVG:
			newValue = toDouble(tuple.getValue(this.aggregationAttribute));
			if (this.state.countInPartition.containsKey(key)) {
				int currentCount = this.state.countInPartition.get(key);
				double newAvg = (currentCount/(currentCount-1d))*
						this.state.values.get(key) - (1d/(currentCount))*newValue;
				this.state.values.put(key,newAvg);
				
				if (this.state.countInPartition.get(key) <= 0) 
					this.state.values.remove(key);
			}
			break;

		default:
			break;
		}
		
		if (this.state.countInPartition.containsKey(key)) {
			this.state.countInPartition.put(key,this.state.countInPartition.get(key) - 1);
			if (this.state.countInPartition.get(key) <= 0) {
				this.state.countInPartition.remove(key);
				this.state.tupleRef.remove(key);
			}
		}
	}

	@Override
	public void evaluateWindow(Queue<DataTuple> dataList, API api) {

		switch (aggregationType) {
		case COUNT:
		case SUM:
		case AVG:
			for (String partitionKey : this.state.tupleRef.keySet()) {
				DataTuple output = this.state.tupleRef.get(partitionKey).setValues(partitionKey,this.state.values.get(partitionKey));
				api.send(output);
			}
			break;
		case MAX:
		case MIN:
			Map<String, Double> value = new HashMap<>();

			for (DataTuple tuple : dataList) {
				String key = getGroupByKey(tuple);
				Double newValue = toDouble(tuple.getValue(this.aggregationAttribute));
				if (value.containsKey(key)) {
					if (value.get(key) < newValue && this.aggregationType.equals(AggregationType.MAX))
						value.put(key, newValue);
					if (value.get(key) > newValue && this.aggregationType.equals(AggregationType.MIN))
						value.put(key, newValue);
				}
				else
					value.put(key, newValue);
			}
			for (String partitionKey : this.state.tupleRef.keySet()) {
				DataTuple output = this.state.tupleRef.get(partitionKey).setValues(partitionKey,value.get(partitionKey));
				api.send(output);
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void accept(OperatorVisitor ov) {
		ov.visit(this);
	}

	@Override
	public void setState(State state) {
		this.window = (Window) state;

		for (DataTuple tuple : this.window.getWindowContent()) 
			this.enteredWindow(tuple, null);
	}

	@Override
	public State getState() {
		return this.window;
	}

}
