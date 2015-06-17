package lrb;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;
import uk.ac.imperial.lsds.seep.multi.UnboundedQueryBufferFactory;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntColumnReference;

public class LRBQ2IStreamMicroOpCode implements IMicroOperatorCode {

	private IntColumnReference vehicleAttribute;

	public LRBQ2IStreamMicroOpCode() {
		this.vehicleAttribute = new IntColumnReference(3);
	}

	@Override
	public void processData(WindowBatch windowBatch, IWindowAPI api) {

		windowBatch.initWindowPointers();
		windowBatch.initPrevWindowPointers();
		windowBatch.moveFreePointerToNotFreeLastWindow();
		
		int [] startPointers = windowBatch.getWindowStartPointers();
		int [] endPointers = windowBatch.getWindowEndPointers();

		IQueryBuffer inBuffer = windowBatch.getBuffer();
		ITupleSchema inSchema = windowBatch.getSchema();
		int byteSizeOfInTuple = inSchema.getByteSizeOfTuple();

		IQueryBuffer outBuffer = UnboundedQueryBufferFactory.newInstance();

		Map<Integer, Integer> lastPerVehicleInLast30Sec = new HashMap<>();

		/*
		 * Create state
		 */
		int windowBeforeBatchStart = windowBatch.getPrevWindowStartPointer();
		int windowBeforeBatchEnd   = windowBatch.getPrevWindowEndPointer();
		for (int i = windowBeforeBatchStart; i < windowBeforeBatchEnd; i += byteSizeOfInTuple) {
			int vehicleValue = vehicleAttribute.eval(inBuffer,
					inSchema, i);
			lastPerVehicleInLast30Sec.put(vehicleValue, i);
		}
		Set<Integer> previous = new HashSet<>();
		copyToSet(lastPerVehicleInLast30Sec.keySet(), previous);
		
		int prevWindowStart = windowBeforeBatchStart;
		int prevWindowEnd = windowBeforeBatchEnd;

		for (int currentWindow = 0; currentWindow < startPointers.length; currentWindow++) {
			int inWindowStartOffset = startPointers[currentWindow];
			int inWindowEndOffset = endPointers[currentWindow];

			// empty window?
			if (inWindowStartOffset == -1) {
				if (prevWindowStart != -1) {
//					for (int i = prevWindowStart; i < inWindowStartOffset; i += byteSizeOfInTuple) {
//						int vehicleValue = vehicleAttribute.eval(inBuffer,
//								inSchema, i);
//						int lastSeen = lastPerVehicleInLast30Sec.get(vehicleValue);
//						if (lastSeen == i)
//							lastPerVehicleInLast30Sec.remove(vehicleValue);
//					}
					
					/*
					 * The current window is empty, the previous window was not empty.
					 * Thus, we clear the current state.
					 */
					lastPerVehicleInLast30Sec.clear();
				}

				evaluateWindow(api, inBuffer, lastPerVehicleInLast30Sec, previous,
						outBuffer, startPointers, endPointers, currentWindow,
						byteSizeOfInTuple);

			} else {

				/*
				 * Tuples in previous window that are not in current window
				 */
				if (prevWindowStart != -1) {
					for (int i = prevWindowStart; i < inWindowStartOffset; i += byteSizeOfInTuple) {
						int vehicleValue = vehicleAttribute.eval(inBuffer,
								inSchema, i);
						
						if (lastPerVehicleInLast30Sec.containsKey(vehicleValue)) {
							int lastSeen = lastPerVehicleInLast30Sec.get(vehicleValue);
							if (lastSeen == i)
								lastPerVehicleInLast30Sec.remove(vehicleValue);
							
						}
					}
				}

				/*
				 * Tuples in current window that have not been in the previous
				 * window
				 */
				if (prevWindowStart != -1) {
					for (int i = prevWindowEnd; i < inWindowEndOffset; i += byteSizeOfInTuple) {

						int vehicleValue = vehicleAttribute.eval(inBuffer,
								inSchema, i);
						lastPerVehicleInLast30Sec.put(vehicleValue, i);
					}
				} else {
					for (int i = inWindowStartOffset; i < inWindowEndOffset; i += byteSizeOfInTuple) {

						int vehicleValue = vehicleAttribute.eval(inBuffer,
								inSchema, i);
						lastPerVehicleInLast30Sec.put(vehicleValue, i);
					}
				}

				evaluateWindow(api, inBuffer, lastPerVehicleInLast30Sec, previous,
						outBuffer, startPointers, endPointers, currentWindow,
						byteSizeOfInTuple);

				prevWindowStart = inWindowStartOffset;
				prevWindowEnd = inWindowEndOffset;
			}
		}
	}

	//
	// private void evaluateWindow(IWindowAPI api) {
	//
	// MultiOpTuple[] windowResult = new
	// MultiOpTuple[this.lastPerVehicleInLast30Sec
	// .keySet().size()];
	//
	// int resultCount = 0;
	// for (Integer vehicle : this.lastPerVehicleInLast30Sec.keySet()) {
	//
	// MultiOpTuple old = this.lastPerVehicleInLast30Sec.get(vehicle);
	// MultiOpTuple t = new MultiOpTuple();
	//
	// t.values = new PrimitiveType[this.projectionIndices.length];
	// for (int i = 0; i < this.projectionIndices.length; i++)
	// t.values[i] = old.values[i];
	//
	// t.timestamp = old.timestamp;
	// t.instrumentation_ts = old.instrumentation_ts;
	//
	// windowResult[resultCount++] = t;
	// }
	//
	// api.outputWindowResult(windowResult);
	// }

	private void evaluateWindow(IWindowAPI api, IQueryBuffer inBuffer,
			Map<Integer, Integer> lastPerVehicleInLast30Sec, Set<Integer> previous,
			IQueryBuffer outBuffer, int[] startPointers, int[] endPointers,
			int currentWindow, int byteSizeOfTuple) {

		if (lastPerVehicleInLast30Sec.keySet().isEmpty()) {
			startPointers[currentWindow] = -1;
			endPointers[currentWindow] = -1;
			previous.clear();
		} else {
			int oldOutBufferPos = outBuffer.position();
			startPointers[currentWindow] = oldOutBufferPos;
			/*
			 *  Iterate over all vehicle keys that did not occur in the previous window
			 */
			for (Integer key : lastPerVehicleInLast30Sec.keySet()) {
				if (previous.contains(key))
					continue;
				
				int partitionOffset = lastPerVehicleInLast30Sec.get(key);
				outBuffer.put(inBuffer, partitionOffset, byteSizeOfTuple);
			}
			
			if (oldOutBufferPos == outBuffer.position()) {
				startPointers[currentWindow] = -1;
				endPointers[currentWindow] = -1;
				previous.clear();
			}
			else {
				copyToSet(lastPerVehicleInLast30Sec.keySet(), previous);
				endPointers[currentWindow] = outBuffer.position() - 1;
			}
		}
	}

	private void copyToSet(Set<Integer> srcSet, Set<Integer> tarSet) {
		tarSet.clear();
		tarSet.addAll(srcSet);
	}
	
	@Override
	public void processData(WindowBatch firstWindowBatch,
			WindowBatch secondWindowBatch, IWindowAPI api) {
		throw new UnsupportedOperationException("LRBQ2 is a single input operator and does not operate on two streams");
	}
}
