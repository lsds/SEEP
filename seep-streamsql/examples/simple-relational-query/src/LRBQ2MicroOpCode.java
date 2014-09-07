

import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.lsds.seep.operator.compose.micro.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.operator.compose.micro.IStatefulMicroOperator;
import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOpTuple;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowAPI;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowBatch;
import uk.ac.imperial.lsds.streamsql.types.IntegerType;
import uk.ac.imperial.lsds.streamsql.types.PrimitiveType;

public class LRBQ2MicroOpCode implements IMicroOperatorCode, IStatefulMicroOperator {

	private int vehicleIndex;
	private int[] projectionIndices;
	
	private Map<Integer, MultiOpTuple> lastPerVehicleInLast30Sec = new HashMap<>();
	
	public LRBQ2MicroOpCode(int vehicleIndex, int[] projectionIndices) {
		this.vehicleIndex = vehicleIndex;
		this.projectionIndices = projectionIndices;
	}
	
	@Override
	public void processData(Map<Integer, IWindowBatch> windowBatches,
			IWindowAPI api) {

		assert(windowBatches.values().size() == 1);

		IWindowBatch batch = windowBatches.values().iterator().next();
		
		int[] startPointers = batch.getWindowStartPointers();
		int[] endPointers = batch.getWindowEndPointers();

		int prevWindowStart = -1;
		int prevWindowEnd = -1;
		
		for (int currentWindow = 0; currentWindow < startPointers.length; currentWindow++) {
			int windowStart = startPointers[currentWindow];
			int windowEnd = endPointers[currentWindow];

			// empty window?
			if (windowStart == -1) {
				if (prevWindowStart != -1) 
					for (int i = prevWindowStart; i < windowStart; i++)
						this.lastPerVehicleInLast30Sec.remove(batch.get(i).values[this.vehicleIndex]);
				
				evaluateWindow(api);
			}
			else {
				/*
				 * Tuples in current window that have not been in the previous window
				 */
				if (prevWindowStart != -1) {
					for (int i = prevWindowEnd; i <= windowEnd; i++) {
						MultiOpTuple t = batch.get(i);
						this.lastPerVehicleInLast30Sec.put(((IntegerType)t.values[this.vehicleIndex]).value, t);
					}
				}
				else {
					for (int i = windowStart; i <= windowEnd; i++) {
						MultiOpTuple t = batch.get(i);
						this.lastPerVehicleInLast30Sec.put(((IntegerType)t.values[this.vehicleIndex]).value, t);
					}
				}

				/*
				 * Tuples in previous window that are not in current window
				 */
				if (prevWindowStart != -1) 
					for (int i = prevWindowStart; i < windowStart; i++)
						this.lastPerVehicleInLast30Sec.remove(batch.get(i).values[this.vehicleIndex]);
			
				evaluateWindow(api);
			
				prevWindowStart = windowStart;
				prevWindowEnd = windowEnd;
			}
		}
	}
	
	private void evaluateWindow(IWindowAPI api) {
		
		MultiOpTuple[] windowResult = new MultiOpTuple[this.lastPerVehicleInLast30Sec.keySet().size()];
		
		int resultCount = 0;
		for (Integer vehicle : this.lastPerVehicleInLast30Sec.keySet()) {
			
			MultiOpTuple old = this.lastPerVehicleInLast30Sec.get(vehicle);
			MultiOpTuple t = new MultiOpTuple();
			
			t.values = new PrimitiveType[this.projectionIndices.length];
			for (int i = 0; i < this.projectionIndices.length; i++) 
				t.values[i] = old.values[i];
			
			t.timestamp = old.timestamp;
			t.instrumentation_ts = old.instrumentation_ts;
			
			windowResult[resultCount++] = t;
		}
		
		api.outputWindowResult(windowResult);
	}

	@Override
	public IMicroOperatorCode getNewInstance() {
		return new LRBQ2MicroOpCode(this.vehicleIndex, this.projectionIndices);
	}

}
