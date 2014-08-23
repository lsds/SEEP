package uk.ac.imperial.lsds.seep.gc14.operator;

import static uk.ac.imperial.lsds.seep.gc14.util.StaticSensorNetworkStructure.maxIndexHouseholds;
import static uk.ac.imperial.lsds.seep.gc14.util.StaticSensorNetworkStructure.maxIndexPlugs;
import static uk.ac.imperial.lsds.seep.gc14.util.StaticSensorNetworkStructure.numberHouses;
import static uk.ac.imperial.lsds.seep.gc14.util.StaticSensorNetworkStructure.numberOfSmallestSlices;
import static uk.ac.imperial.lsds.seep.gc14.util.StaticSensorNetworkStructure.numberPlugs;
import static uk.ac.imperial.lsds.seep.gc14.util.StaticSensorNetworkStructure.numberSmallestSlicesPerDay;
import static uk.ac.imperial.lsds.seep.gc14.util.StaticSensorNetworkStructure.numberSmallestSlicesPerHour;
import static uk.ac.imperial.lsds.seep.gc14.util.StaticSensorNetworkStructure.sliceSizeFactors;
import static uk.ac.imperial.lsds.seep.gc14.util.StaticSensorNetworkStructure.smallestSliceInSec;
import static uk.ac.imperial.lsds.seep.gc14.util.StaticSensorNetworkStructure.startOfEpocheInSec;
import static uk.ac.imperial.lsds.seep.gc14.util.StaticSensorNetworkStructure.updateIntervalInSec;

import java.util.Arrays;
import java.util.List;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.gc14.util.StaticSensorNetworkStructure;
import uk.ac.imperial.lsds.seep.operator.StatefulOperator;
import uk.ac.imperial.lsds.seep.state.StateWrapper;

public class Q1Prediction implements StatefulOperator {

	private static final long serialVersionUID = 1L;

	static final boolean useWorkForCorrections = true;
	static final int measurementsPerSmallestSliceThatJustifyCorrection = 10;

	/*
	 * Holds the average load per plug per slice
	 */
	float[][] loadAveragePerPlugPerSlice = new float[numberPlugs][numberOfSmallestSlices];

	/*
	 * Counts the number of load measurements received for the current slice.
	 * This is needed to update the current average without keeping track of all values.
	 */
	int[] valueCountForCurrentSlicePerPlug = new int[numberPlugs];

	/*
	 * Data structure to provide for a house and a household the index
	 * of the first plug in the two aforementioned arrays
	 */
	int[][][] plugPointer = new int[numberHouses][maxIndexHouseholds][maxIndexPlugs];
	
	int currentSlice = 0;
	int currentTime = 0;
	int lastUpdateSent = 0;

	/*
	 * First work value for each plug and slice
	 */
	float[][] workPerPlugPerSlice = new float[numberPlugs][numberOfSmallestSlices];
	
	/*
	 * Number of measurements used to determine avg load
	 */
	int[][] valueCountForOldSlicesPerPlug = new int[numberPlugs][numberOfSmallestSlices];
	
	public Q1Prediction() {
		
	}

	@Override
	public void setUp() {
		plugPointer = StaticSensorNetworkStructure.getInstance().getPlugPointer(numberHouses, 
				maxIndexHouseholds, maxIndexPlugs);
	}
	
	/**
	 * Interface for dynamically scaling out this operator
	 */
	@Override
	public StateWrapper getState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void replaceState(StateWrapper arg0) {
		// TODO Auto-generated method stub
		
	}
	/** */

	@Override
	public void processData(DataTuple data) {
		int house = data.getInt("house_id");
		
		/*
		 * Check whether we received a heart beat
		 */
		long id = data.getLong("id");
		if (id == -1) {
			
			currentTime = data.getInt("timestamp");
			
			// should we send updates?
			if ((currentTime - lastUpdateSent) >= updateIntervalInSec)
				sendUpdate(data);
			
			
			// did the heart beat change the index of the current slice?
			// Note: cast to (int) implements floor
			int i = (int) ((currentTime - startOfEpocheInSec)/(float)StaticSensorNetworkStructure.smallestSliceInSec);
			if (i != currentSlice) {
				// assume constant load for all plugs for which we have not received any 
				// load updates
				for (int plug = 0; plug < numberPlugs; plug++) {
					if (valueCountForCurrentSlicePerPlug[plug] == 0)
						if (currentSlice > 0)
							loadAveragePerPlugPerSlice[plug][currentSlice] = loadAveragePerPlugPerSlice[plug][currentSlice-1];
					
					valueCountForOldSlicesPerPlug[plug][currentSlice] = valueCountForCurrentSlicePerPlug[plug];
				}
				currentSlice = i;
				valueCountForCurrentSlicePerPlug = new int[numberPlugs];
			}
			return;
		}
		
		// try to use work measurements for corrections the time being
		int property = data.getInt("property");
		if (property != 1) {
			if (useWorkForCorrections) {
				
				int timestamp = data.getInt("timestamp");
//				int house = data.getInt("house_id");
				int household = data.getInt("household_id");
				int plug = data.getInt("plug_id");
				float value = data.getFloat("value");
				int ppointer = plugPointer[house][household][plug];

				if (workPerPlugPerSlice[ppointer][currentSlice] == 0)
					workPerPlugPerSlice[ppointer][currentSlice] = value;

				doCorrectionsBasedOnWork(timestamp, workPerPlugPerSlice[ppointer][currentSlice], ppointer);

			}
			return; 
		}
		
		int household = data.getInt("household_id");
		int plug = data.getInt("plug_id");
		float value = data.getFloat("value");
		
		int ppointer = plugPointer[house][household][plug];
		
		// update load average for current slice
		float currentCount = valueCountForCurrentSlicePerPlug[ppointer];
		if (valueCountForCurrentSlicePerPlug[ppointer] == 0)
			loadAveragePerPlugPerSlice[ppointer][currentSlice] = value;
		else 
			loadAveragePerPlugPerSlice[ppointer][currentSlice] = 
				(currentCount/(currentCount+1f))*loadAveragePerPlugPerSlice[ppointer][currentSlice]
				+ (1f/(currentCount+1f))*value;
		
		valueCountForCurrentSlicePerPlug[ppointer] += 1;
	}
	
	private void doCorrectionsBasedOnWork(int timestamp, float value, int ppointer) {
		/*
		 * Is there need for corrections?
		 */
		if (currentSlice == 0)
			return;
		if (valueCountForOldSlicesPerPlug[ppointer][currentSlice-1] > measurementsPerSmallestSliceThatJustifyCorrection)
			return;
		
		/*
		 * Do the actual correction
		 */
		int back = currentSlice-1;
		// How long back in the past do we want to correct?
		// Get to the last slice for which we had a work measurement.
		while (back > 0 && workPerPlugPerSlice[ppointer][back] > 0)
			back--;

		// no value?
		if (workPerPlugPerSlice[ppointer][back] == 0)
			return;
		
		float workPerSlice = (value - workPerPlugPerSlice[ppointer][back])/(currentSlice - back);
		float avgLoadPerSlice = workPerSlice * 1000f / numberSmallestSlicesPerHour;
		
		for (int i = back; i < currentSlice; i++)
			loadAveragePerPlugPerSlice[ppointer][i] = avgLoadPerSlice;
		
	}
	
	private void sendUpdate(DataTuple data) {
		for (int i = 0; i < 40; i++)
			sendUpdateForHouse(data, i);
	}

	/*
	 * For each house
	 */
	private void sendUpdateForHouse(DataTuple data, int house) {

		/*
		 * For each slice
		 */
		for (int i = 0; i < sliceSizeFactors.length; i++) {
			int sliceFactor = sliceSizeFactors[i];
			int actualSliceIndex = (int) (((float)currentSlice) / sliceFactor);
			int startTimeForPrediction = startOfEpocheInSec + ((actualSliceIndex+2) * smallestSliceInSec * sliceFactor);

			/*
			 * For each household
			 */
			float plugSum = 0;
			int plugCount = 0;
			int numberHistoricSlices = (int) ((actualSliceIndex+2f) / numberSmallestSlicesPerDay * sliceFactor);
			float[] historicLoadAverageForHousePerSlice = new float[numberHistoricSlices];
			int[] historicLoadCountForHousePerSlice = new int[numberHistoricSlices];
			
			for (int household = 0; household < maxIndexHouseholds; household++) {
				/*
				 * For each plug
				 */
				for (int plug = 0; plug < maxIndexPlugs; plug++) {
					int pp = plugPointer[house][household][plug];
					if (pp == -1)
						continue;
					
					float averageLoadForPlug = averageOfValues(loadAveragePerPlugPerSlice[pp], actualSliceIndex * sliceFactor, currentSlice);
					
					float medianLoadForPlug = 0;
					if (numberHistoricSlices != 0) {
						float[] historicLoadValuesForPlugPerSlice = selectHistoricValuesPerPlug(loadAveragePerPlugPerSlice[pp], numberHistoricSlices, actualSliceIndex, sliceFactor);
						medianLoadForPlug = medianOfValues(historicLoadValuesForPlugPerSlice);
						
						for (int slice = 0; slice < historicLoadValuesForPlugPerSlice.length; slice++) {
							if (historicLoadCountForHousePerSlice[slice] == 0)
								historicLoadAverageForHousePerSlice[slice] = historicLoadValuesForPlugPerSlice[slice];
							else 
								historicLoadAverageForHousePerSlice[slice] = 
								(historicLoadCountForHousePerSlice[slice]/(historicLoadCountForHousePerSlice[slice]+1f))*historicLoadAverageForHousePerSlice[slice]   
								+ (1f/(historicLoadCountForHousePerSlice[slice]+1f))*historicLoadValuesForPlugPerSlice[slice];
						
							historicLoadCountForHousePerSlice[slice] += 1;						
						}
					}
					
					float predictedLoadForPlug = (averageLoadForPlug + medianLoadForPlug)/2;

					/*
					 * Send update for plug
					 */
					DataTuple output = data.setValues(startTimeForPrediction,house,household,plug,predictedLoadForPlug);
					api.send(output);
					
					plugCount++;
					plugSum += averageLoadForPlug;
				}
			}

			float averageLoadForHouse = plugSum / plugCount;
			float medianLoadForHouse = medianOfValues(historicLoadAverageForHousePerSlice);
			
			float predictedLoadForHouse= (averageLoadForHouse + medianLoadForHouse)/2;

			/*
			 * Send update for house
			 */
			DataTuple output = data.setValues(startTimeForPrediction,house,-1,-1,predictedLoadForHouse);
			api.send(output);

		}
	}
	
	private float[] selectHistoricValuesPerPlug(float[] loadAveragePerSlice, int numberHistoricSlices, int actualSliceIndex, int sliceFactor) {
		float[] selectedValues = new float[numberHistoricSlices];
		
		for (int i = 0; i < numberHistoricSlices; i++) {
			int startSlice = actualSliceIndex + 2 - ((i+1) * (int) ((float)numberSmallestSlicesPerDay / sliceFactor));
			float historicLoadOneDayForPlug = averageOfValues(loadAveragePerSlice, startSlice, sliceFactor);
			selectedValues[i] = historicLoadOneDayForPlug;
		}
		return selectedValues;
	}
	
	private float medianOfValues(float[] values) {
		if (values.length < 1)
			return 0;
		if (values.length == 1)
			return values[0];
		
		Arrays.sort(values);

		float median = ((values.length % 2) == 0) ? 
				(values[(int)(values.length/2f)] + values[(int)(values.length/2f)-1])/2f :
				values[(int)((values.length - 1)/2f)];

		return median;
	}

	private float averageOfValues(float[] values, int start, int end) {
		float sum = 0;
		for (int i = start; i <= end; i++)
			sum += values[i];
		return sum / (end-start+1);
	}
	
	@Override
	public void processData(List<DataTuple> dataList) { }

}
