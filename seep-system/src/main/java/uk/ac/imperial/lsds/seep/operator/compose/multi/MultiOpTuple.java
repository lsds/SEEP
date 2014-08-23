package uk.ac.imperial.lsds.seep.operator.compose.multi;

import java.util.Map;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.Payload;

public class MultiOpTuple {
	
	public long timestamp;
	public long instrumentation_ts;
	public Object[] values;

	public MultiOpTuple() { }
	
	public MultiOpTuple(MultiOpTuple tuple) {
		this.timestamp = tuple.timestamp;
		this.instrumentation_ts = tuple.instrumentation_ts;
		this.values = tuple.values;
	}

	public MultiOpTuple(Object[] values, long timestamp, long instrumentation_ts) {
		this.timestamp = timestamp;
		this.instrumentation_ts = instrumentation_ts;
		this.values = values;
	}

	public MultiOpTuple(DataTuple tuple) {
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

//	public Object clone() {
//		MultiOpTuple c = null;
//		try {
//			c = (MultiOpTuple) super.clone();
//		} catch (CloneNotSupportedException e) {
//			e.printStackTrace();
//		}
//		if (c != null) {
//			c.timestamp = timestamp;
//			c.instrumentation_ts = instrumentation_ts;
//			c.values = new Object[values.length];
//			for (int i = 0; i < values.length; i++)
//				c.values[i] = values[i];
//		}		
//			
//		return c;
//	}
	
}
