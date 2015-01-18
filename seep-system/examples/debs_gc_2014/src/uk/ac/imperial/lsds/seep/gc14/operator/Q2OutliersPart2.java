package uk.ac.imperial.lsds.seep.gc14.operator;

import static uk.ac.imperial.lsds.seep.gc14.util.StaticSensorNetworkStructure.maxIndexHouseholds;
import static uk.ac.imperial.lsds.seep.gc14.util.StaticSensorNetworkStructure.maxIndexPlugs;
import static uk.ac.imperial.lsds.seep.gc14.util.StaticSensorNetworkStructure.numberHouses;
import static uk.ac.imperial.lsds.seep.gc14.util.StaticSensorNetworkStructure.numberPlugs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.gc14.util.SkipList;
import uk.ac.imperial.lsds.seep.gc14.util.StaticSensorNetworkStructure;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;

public class Q2OutliersPart2 implements StatelessOperator {

	private static final long serialVersionUID = 1L;

	static final int windowOneHourInSec = 3600;
	static final int windowOneDayInSec = 86400;

	//memory fixed
	Map<Integer, Float> currentMedianOneHourPerPlug = new HashMap<Integer, Float>();
	Map<Integer, Float> currentMedianOneDayPerPlug = new HashMap<Integer, Float>();

	//memory variable
	SkipList valuesSortedByValueOneHour = null;
	SkipList valuesSortedByValueOneDay = null;
	
	float currentGlobalMedianOneHour = -1;
	float currentGlobalMedianOneDay = -1;
	
	//memory fixed
	Map<Integer, List<Integer>> housePlugs = new HashMap<Integer, List<Integer>>();
	Map<Integer, Integer> plugHouses = new HashMap<Integer, Integer>();

	//memory fixed
	Map<Integer, Float> shareOfOutlierPlugsOneHourPerHouse = new HashMap<Integer, Float>();
	Map<Integer, Float> shareOfOutlierPlugsOneDayPerHouse = new HashMap<Integer, Float>();
	
	//time control stuff
	int c = 0;
	int sec = 0;
	long init = System.currentTimeMillis();
	int aggrRemove = 0;
	int aggrInsert = 0;
	
	@Override
	public void setUp() {
		//initialise lists here as transient methods become null after serialisation
		valuesSortedByValueOneHour = new SkipList();
		valuesSortedByValueOneDay = new SkipList();
		
		for (int i = 0; i < numberPlugs; i++) {
			currentMedianOneHourPerPlug.put(i, -1f);
			currentMedianOneDayPerPlug.put(i, -1f);
		}
		
		for (int i = 0; i < numberHouses; i++) {
			shareOfOutlierPlugsOneHourPerHouse.put(i,0f);
			shareOfOutlierPlugsOneDayPerHouse.put(i,0f);
		}
		
		housePlugs = StaticSensorNetworkStructure.getInstance().getHousePlugs(numberHouses, maxIndexHouseholds, maxIndexPlugs);
		plugHouses = StaticSensorNetworkStructure.getInstance().getPlugHouses(numberHouses, maxIndexHouseholds, maxIndexPlugs);
	}

	int lat_sampler = 0;
	
	@Override
	public void processData(DataTuple data) { 
		c++;
//		System.out.println("Data: "+data);
		int timestamp = data.getInt("timestamp");
		int ppointer = data.getInt("ppointer");
		int window = data.getInt("window");
		float plugMedian = data.getFloat("plugMedian");
		float insertedValue = data.getFloat("value");
		int[] removedValues = data.getIntArray("removedValues");
		
		/*
		 * Distinguish windows
		 */
		if (window == windowOneHourInSec) {
			// insert new values
			if (insertedValue != -1) {
				aggrInsert++;
				valuesSortedByValueOneHour.add(insertedValue);
			}
			
			/*
			 * NOTE: cannot use removeAll since this would remove 
			 * ALL INSTANCES of the respective values
			 */
//			int sizeBeforeRemoval = valuesSortedByValueOneHour.size();
//			int sizeOfElementsToRemove = removedValues.length;
//			boolean in = false;
			for (float toRemove : removedValues){
//				in = true;
				aggrRemove++;
				valuesSortedByValueOneHour.remove(toRemove);
			}
//			if(in){
//				if(sizeBeforeRemoval-sizeOfElementsToRemove != valuesSortedByValueOneHour.size()){
//					System.out.println((valuesSortedByValueOneHour.size()-(sizeBeforeRemoval-sizeOfElementsToRemove))+" noremoved");
//				}
//			}

			// new global median
			float newCurrentGlobalMedianOneHour = valuesSortedByValueOneHour.getMedian();
			int onlyOneHouseToCheck = -1;
			if (newCurrentGlobalMedianOneHour == currentGlobalMedianOneHour)
				onlyOneHouseToCheck = plugHouses.get(ppointer);
			
			currentGlobalMedianOneHour = newCurrentGlobalMedianOneHour;
			
			
			
			lat_sampler++;
			long currentTime = System.currentTimeMillis();
			if(lat_sampler > 2000){
				long incomingInstTs = data.getPayload().instrumentation_ts;
				long latency = (currentTime - incomingInstTs);
				System.out.println("! "+latency);
				lat_sampler = 0;
			}
			
			
			/*
			 * If global did not change and local did not change there is
			 * nothing to do and we return
			 */
			if (onlyOneHouseToCheck != -1) {
				if (plugMedian == currentMedianOneHourPerPlug.get(ppointer))
					return;
			}

			// store plug median
			currentMedianOneHourPerPlug.put(ppointer, plugMedian);

			if (onlyOneHouseToCheck != -1) 
				checkHouseOneHour(data, timestamp, onlyOneHouseToCheck);
			else {
				// for each house
				for (int house : housePlugs.keySet()) 
					checkHouseOneHour(data, timestamp, house);
			}
		}
		else {
			// insert new values
			if (insertedValue != -1) 
				valuesSortedByValueOneDay.add(insertedValue);
			
			/*
			 * NOTE: cannot use removeAll since this would remove 
			 * ALL INSTANCES of the respective values
			 */
			for (float toRemove : removedValues) 
				valuesSortedByValueOneDay.remove(toRemove);
			
			// new global median
			float newCurrentGlobalMedianOneDay = valuesSortedByValueOneDay.getMedian();
			int onlyOneHouseToCheck = -1;
			if (newCurrentGlobalMedianOneDay == currentGlobalMedianOneDay)
				onlyOneHouseToCheck = plugHouses.get(ppointer);
			
			currentGlobalMedianOneDay = newCurrentGlobalMedianOneDay;
			
			/*
			 * If global did not change and local did not change there is
			 * nothing to do and we return
			 */
			if (onlyOneHouseToCheck != -1) {
				if (plugMedian == currentMedianOneDayPerPlug.get(ppointer))
					return;
			}

			// store plug median
			currentMedianOneDayPerPlug.put(ppointer, plugMedian);
			
			if (onlyOneHouseToCheck != -1) 
				checkHouseOneDay(data, timestamp, onlyOneHouseToCheck);
			else {
				// for each house
				for (int house : housePlugs.keySet()) 
					checkHouseOneDay(data, timestamp, house);
			}
		}
		
		if((System.currentTimeMillis() - init) > 1000){
			System.out.println("Q22 "+c+" ");
			System.out.println("aggrRemove size: "+aggrRemove);
			System.out.println("aggrInsert size: "+aggrInsert);
			aggrRemove = 0;
			aggrInsert = 0;
			System.out.println("valuesSortedByValueOneHour: "+valuesSortedByValueOneHour.size());
			c = 0;
			sec++;
			init = System.currentTimeMillis();
		}
	}
	
	private void checkHouseOneDay(DataTuple data, int timestamp, int house) {
		int countOutliers = 0;
		for (int plug : housePlugs.get(house)) {
			if (currentMedianOneDayPerPlug.get(plug) > currentGlobalMedianOneDay)
				countOutliers++;
		}
		float shareOutliers = ((float)countOutliers)/ housePlugs.get(house).size();
//		System.out.println("1d, mem: "+shareOfOutlierPlugsOneDayPerHouse.get(house)+" cur: "+shareOutliers);
		if (shareOfOutlierPlugsOneDayPerHouse.get(house) != shareOutliers) {
			shareOfOutlierPlugsOneDayPerHouse.put(house, shareOutliers);

			DataTuple output = data.setValues(timestamp - windowOneDayInSec, timestamp, house, shareOutliers);
			api.send(output);
		}
	}
	
	private void checkHouseOneHour(DataTuple data, int timestamp, int house) {
		int countOutliers = 0;
		for (int plug : housePlugs.get(house)) {
			if (currentMedianOneHourPerPlug.get(plug) > currentGlobalMedianOneHour)
				countOutliers++;
		}
		float shareOutliers = ((float)countOutliers)/ housePlugs.get(house).size();
//		System.out.println("1h, mem: "+shareOfOutlierPlugsOneHourPerHouse.get(house)+" cur: "+shareOutliers);
		if (shareOfOutlierPlugsOneHourPerHouse.get(house) != shareOutliers) {
			shareOfOutlierPlugsOneHourPerHouse.put(house, shareOutliers);

			DataTuple output = data.setValues(timestamp - windowOneHourInSec, timestamp, house, shareOutliers);
			api.send(output);
		}

	}

	@Override
	public void processData(List<DataTuple> dataList) { }

}

