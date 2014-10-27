package uk.ac.imperial.lsds.seep.gc14.operator;

import static uk.ac.imperial.lsds.seep.gc14.util.StaticSensorNetworkStructure.maxIndexHouseholds;
import static uk.ac.imperial.lsds.seep.gc14.util.StaticSensorNetworkStructure.maxIndexPlugs;
import static uk.ac.imperial.lsds.seep.gc14.util.StaticSensorNetworkStructure.numberHouses;
import static uk.ac.imperial.lsds.seep.gc14.util.StaticSensorNetworkStructure.numberPlugs;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.gc14.util.StaticSensorNetworkStructure;
import uk.ac.imperial.lsds.seep.operator.StatefulOperator;
import uk.ac.imperial.lsds.seep.state.StateWrapper;

public class Q2OutliersPart1 implements StatefulOperator {

	private static final long serialVersionUID = 1L;

	static final int windowOneHourInSec = 3600;
	static final int windowOneDayInSec = 86400;
	

	Map<Integer,Deque<Float>> valuesOneHourPerPlug = new HashMap<Integer,Deque<Float>>();
	Map<Integer,Deque<Float>> valuesOneDayPerPlug = new HashMap<Integer,Deque<Float>>();
	
	Map<Integer,Deque<Integer>> timestampsOneHourPerPlug = new HashMap<Integer,Deque<Integer>>();
	Map<Integer,Deque<Integer>> timestampsOneDayPerPlug = new HashMap<Integer,Deque<Integer>>();

	Map<Integer,List<Float>> valuesSortedByValueOneHourPerPlug = new HashMap<Integer,List<Float>>();
	Map<Integer,List<Float>> valuesSortedByValueOneDayPerPlug = new HashMap<Integer,List<Float>>();
	
	int[][][] plugPointer = new int[numberHouses][maxIndexHouseholds][maxIndexPlugs];
	
	@Override
	public void setUp() {
		
		plugPointer = StaticSensorNetworkStructure.getInstance().getPlugPointer(numberHouses, 
				maxIndexHouseholds, maxIndexPlugs);

		for (int i = 0; i < numberPlugs; i++) {
			valuesOneHourPerPlug.put(i, new LinkedList<Float>());
			valuesOneDayPerPlug.put(i, new LinkedList<Float>());
			timestampsOneHourPerPlug.put(i, new LinkedList<Integer>());
			timestampsOneDayPerPlug.put(i, new LinkedList<Integer>());
			valuesSortedByValueOneHourPerPlug.put(i, new ArrayList<Float>());
			valuesSortedByValueOneDayPerPlug.put(i, new ArrayList<Float>());
		}
		
	}
	
	/** State mngt methods **/
	@Override
	public StateWrapper getState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void replaceState(StateWrapper arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processData(DataTuple data) {
//		System.out.println("Data: "+data);
		/*
		 *  ignore heart beats
		 */
		if (data.getLong("id") == -1)
			return;
		
		/*
		 *  ignore work measurements for the time being
		 */
		if (data.getInt("property") != 1)
			return;

		int timestamp = data.getInt("timestamp");
//		System.out.println("ts0: "+timestamp);
		int house = data.getInt("house_id");
		int household = data.getInt("household_id");
		int plug = data.getInt("plug_id");
		float value = data.getFloat("value");
		
		int ppointer = plugPointer[house][household][plug];

		/*
		 * Update hour window with new measurement
		 * and send update to aggregator if needed
		 */
//		System.out.println("ts1: "+timestamp);
		handleWindow(data, ppointer, timestamp, value, windowOneHourInSec, valuesOneHourPerPlug, timestampsOneHourPerPlug, valuesSortedByValueOneHourPerPlug);

		/*
		 * Update day window with new measurement
		 * and send update to aggregator if needed
		 */
		handleWindow(data, ppointer, timestamp, value, windowOneDayInSec, valuesOneDayPerPlug, timestampsOneDayPerPlug, valuesSortedByValueOneDayPerPlug);
		
	}
	
	
	private void handleWindow( DataTuple data,	int ppointer, int timestamp, float value, int window,
			Map<Integer,Deque<Float>> valuesPerPlug,
			Map<Integer,Deque<Integer>> timestampsPerPlug,
			Map<Integer,List<Float>> valuesSortedByValuePerPlug) {
//		System.out.println("ts2: "+timestamp);
		/*
		 * Enqueue new event
		 */
		valuesPerPlug.get(ppointer).addLast(value);
		timestampsPerPlug.get(ppointer).addLast(timestamp);
		
		/*
		 * Insert into sorted values 
		 */
		if (valuesSortedByValuePerPlug.get(ppointer).isEmpty()) {
			valuesSortedByValuePerPlug.get(ppointer).add(value);
		}
		else {
			List<Float> valuesSortedByValue = valuesSortedByValuePerPlug.get(ppointer);
			// not exact median, but lower value of the two values around the median if list contains even number of entries
			int middle = (int)(valuesSortedByValue.size()/2f);
			float currentMedian = valuesSortedByValue.get(middle);
			
			if (currentMedian < value) {
				// new sorted list
				valuesSortedByValue = QUtils.insertIntoSortedFloatList(valuesSortedByValue, value, middle, valuesSortedByValue.size()-1);
				// store new sorted list
				valuesSortedByValuePerPlug.put(ppointer, valuesSortedByValue);
			}
			else {
				// new sorted list
				valuesSortedByValue = QUtils.insertIntoSortedFloatList(valuesSortedByValue, value, 0, middle);
				// store new sorted list
				valuesSortedByValuePerPlug.put(ppointer, valuesSortedByValue);
			}
		}
		
		/*
		 * Dequeue events
		 */
		int tsToRemove = timestampsPerPlug.get(ppointer).getFirst();
		tsToRemove = timestampsPerPlug.get(ppointer).getFirst();
		List<Float> removedValues = new ArrayList<Float>();
		while (tsToRemove < (timestamp - window)) {
			timestampsPerPlug.get(ppointer).removeFirst();
			float removedValue = valuesPerPlug.get(ppointer).removeFirst();
			removedValues.add(removedValue);
			valuesSortedByValuePerPlug.get(ppointer).remove(removedValue);
			tsToRemove = timestampsPerPlug.get(ppointer).getFirst();
		}
//		if(in>0){
//			System.out.println("has to remove: "+in);
//			System.out.println("gonna remove: "+removedValues.size());
//		}
		
		/*
		 * Send update 
		 */
		if (removedValues.remove(value))
			value = -1;
		if (!(value == -1 && removedValues.isEmpty())) {
			float plugMedian = QUtils.medianOfSortedList(valuesSortedByValuePerPlug.get(ppointer));
			
			float[] primitiveArray = QUtils.toPrimitiveArray(removedValues);
//			if(removedValues.size() != primitiveArray.length){
//				System.out.println("removedValues: "+removedValues.size());
//				System.out.println("primitiveArray: "+primitiveArray.length);
//			}
			DataTuple output = data.setValues(timestamp, ppointer, window, plugMedian, value, primitiveArray);
			api.send(output);
		}
	}
	
	@Override
	public void processData(List<DataTuple> dataList) { }

}
