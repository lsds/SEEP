package uk.ac.imperial.lsds.seep.operator.compose.multi;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.Payload;

public class MultiOpTuple {
	
	public static final int MULTIOP_TUPLE_POOL_SIZE = Integer.valueOf(GLOBALS.valueFor("multiOpTuplePoolSize"));

	private static Queue<MultiOpTuple> pool = new LinkedList<>();
	
	static {
		int i = MULTIOP_TUPLE_POOL_SIZE;
		while (i-- > 0)
			pool.add(new MultiOpTuple());
		
	}
	
	public long timestamp;
	public long instrumentation_ts;
	public Object[] values;

	public static MultiOpTuple newInstance(Object[] values, long timestamp, long instrumentation_ts) {
		MultiOpTuple instance = pool.poll();
		if (instance == null) 
			return new MultiOpTuple(values, timestamp, instrumentation_ts);
		
		instance.timestamp = timestamp;
		instance.instrumentation_ts = instrumentation_ts;
		instance.values = values;
		return instance;
	}
	
	public static MultiOpTuple newInstance(DataTuple tuple) {
		MultiOpTuple instance = pool.poll();
		if (instance == null) 
			return new MultiOpTuple(tuple);
		
		instance.timestamp = tuple.getPayload().timestamp;
		instance.instrumentation_ts = tuple.getPayload().instrumentation_ts;
		instance.values = tuple.getPayload().attrValues.toArray();
		return instance;
	}

	public static MultiOpTuple newInstance() {
		MultiOpTuple instance = pool.poll();
		if (instance == null) 
			return new MultiOpTuple();
		
		return instance;
	}
	
	public void free() {
		if (pool.size() < MULTIOP_TUPLE_POOL_SIZE)
			pool.add(this);
	}
	
	private MultiOpTuple() {
		
	}

	private MultiOpTuple(Object[] values, long timestamp, long instrumentation_ts) {
		this.timestamp = timestamp;
		this.instrumentation_ts = instrumentation_ts;
		this.values = values;
	}

	private MultiOpTuple(DataTuple tuple) {
		this.timestamp = tuple.getPayload().timestamp;
		this.instrumentation_ts = tuple.getPayload().instrumentation_ts;
		this.values = tuple.getPayload().attrValues.toArray();
	}

	public DataTuple toDataTuple(Map<String, Integer> idxMapper) {
		DataTuple result = new DataTuple(idxMapper, new Payload((Object[])values));
		result.getPayload().timestamp = this.timestamp;
		result.getPayload().instrumentation_ts = this.instrumentation_ts;
		return result;
	}

}
