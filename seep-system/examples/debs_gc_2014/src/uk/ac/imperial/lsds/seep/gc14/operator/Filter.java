package uk.ac.imperial.lsds.seep.gc14.operator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.imperial.lsds.seep.comm.routing.Router;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;

public class Filter implements StatelessOperator {

	private static final long serialVersionUID = 1L;
	
	// We need to store the operatorId
	private int operatorId = 0;
	private static final int designatedOpId = 1;
	
	public Boolean filterNotNew = true;
	public Boolean filterNoChangeInValue = true;
	public float   filterWhenChangeLessThan = 5;

	/* 
	 * Setting the heart beat interval to less than zero 
	 * disables sending of heart beats
	 */
	public int heartBeatIntervalInSec = 30;
	private int lastHeartBeat = -1;
	
	private Map<String, Integer> lastLoadTimeStamp = new HashMap<String, Integer>();
	private Map<String, Integer> lastWorkTimeStamp = new HashMap<String, Integer>();

	private Map<String, Float> lastLoadValue = new HashMap<String, Float>();

	private Map<String, Float> lastWorkValue = new HashMap<String, Float>();
	private Map<String, Integer> toSelectWorkValue = new HashMap<String, Integer>();

	public Filter() {
		
	}
	
	@Override
	public void setUp() { 
		this.operatorId = api.getOperatorId();
	}

	boolean first = true;
	
	@Override
	public void processData(DataTuple data) {
		long id_int = data.getLong("id");
		int timestamp = data.getInt("timestamp");
		float value = data.getFloat("value");
		int property = data.getInt("property");
		int plug_id = data.getInt("plug_id");
		int household_id = data.getInt("household_id");
		int house_id = data.getInt("house_id");
		
		if(lastHeartBeat == -1){
			lastHeartBeat = timestamp;
		}

		DataTuple output = data.setValues(id_int, timestamp, value, property, plug_id, household_id, house_id);
		
		String id = plug_id
				+ "&" 
				+ household_id
				+ "&" 
				+ house_id;

		/*
		 * Filter measurements that are not new, i.e., have a timestamp equal or 
		 * smaller than the last observed measurement for this particular plug
		 */
		if (this.filterNotNew) {
			if (property == 0)  {
				if (!dataIsNew(lastWorkTimeStamp,data, id))
					output = null;
				lastWorkTimeStamp.put(id, timestamp);
			}
			else {
				if (!dataIsNew(lastLoadTimeStamp,data, id))
					output = null;
				lastLoadTimeStamp.put(id, timestamp);
			}
		}
		
		/*
		 * Filter measurements when there is no change in value w.r.t. the 
		 * last measurement for the very same plug
		 */
		if (this.filterNoChangeInValue) {
			if (property != 0) {
				if (!dataValueChanged(lastLoadValue,data, id)) {
					output = null;
				}
				else {
					lastLoadValue.put(id, value);
					// check whether we also need to forward a work measurement
					if (lastWorkTimeStamp.containsKey(id)) {
						if (lastWorkTimeStamp.get(id) == timestamp) {
							DataTuple outputWork = data.setValues(id_int, timestamp, lastWorkValue.get(id), 0, plug_id, household_id, house_id);
							try {
								// We are splitting the stream by house id
								api.send_splitKey(outputWork, Router.customHash(house_id));
							}
							catch (Exception e) {
								e.printStackTrace();
							}

						}
					}
				}
			}
			else {
				// should we forward the work value?
				if (toSelectWorkValue.containsKey(id)) {
					if (!(toSelectWorkValue.get(id) == timestamp))
						output = null;
				}
				else {
					output = null;
				}	
				// store value in case we need to forward it later (work comes before load)
				lastWorkValue.put(id, value);
				lastWorkTimeStamp.put(id, timestamp);
			}
		}
		
		/*
		 * If the item has not been filtered, we send it 
		 * to downstream operators
		 */
		if (output != null) {
			try {
				// We are splitting the stream by house id
				api.send_splitKey(output, Router.customHash(house_id));
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		/**
		 * We want to make sure that even if scaled out, only one filter is in charge of generating heartbeats 
		 */
		if(operatorId == designatedOpId){
			/*
			 * If requested, check whether a heart beat should be sent. In order 
			 * to send one, we rely on the same tuple structure than for the actual 
			 * input data.
			 */
			if (this.heartBeatIntervalInSec >= 0) {
				int current = timestamp;
				if(current-this.lastHeartBeat >= this.heartBeatIntervalInSec){
					DataTuple beat = data.setValues(-1L,current,0f,0,0,0,0,0);
					// We need to notify all downstream ops with the heartbeat
					api.send_all(beat);
					this.lastHeartBeat = current;
				}
			}
		}
	}

	private boolean dataIsNew(Map<String, Integer> oldTimeStamp, DataTuple data, String id) {
		
		int dataTimeStamp = data.getInt("timestamp");
		if (!oldTimeStamp.containsKey(id)) {
			oldTimeStamp.put(id, dataTimeStamp);
			return true;
		}
		if (dataTimeStamp > oldTimeStamp.get(id)) {
			oldTimeStamp.put(id, dataTimeStamp);
			return true;
		}
		return false;
	}

	private boolean dataValueChanged(Map<String, Float> lastValue, DataTuple data, String id) {
		float dataValue = data.getFloat("value");
		if (!lastValue.containsKey(id)) {
			lastValue.put(id, dataValue);
			return true;
		}
		if (Math.abs(lastValue.get(id)-dataValue)>filterWhenChangeLessThan) {
			lastValue.put(id, dataValue);
			return true;
		}
		return false;
	}

	@Override
	public void processData(List<DataTuple> dataList) {
	}

}