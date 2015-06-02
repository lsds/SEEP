package net;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;
import uk.ac.imperial.lsds.seep.multi.IntMap;
import uk.ac.imperial.lsds.seep.multi.IntMapEntry;
import uk.ac.imperial.lsds.seep.multi.IntMapFactory;
import uk.ac.imperial.lsds.seep.multi.ThreadMap;
import uk.ac.imperial.lsds.seep.multi.UnboundedQueryBufferFactory;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntColumnReference;

public class LRBUDFCPU implements IMicroOperatorCode {

	private IntColumnReference vehicleAttribute;
	private ITupleSchema inputSchema;
	
	private static boolean debug = false;

	public LRBUDFCPU (ITupleSchema inputSchema, IntColumnReference vehicleAttribute) {
		/*
		 * The input stream is the output of a projection
		 * with the following attributes:
		 * 
		 * 0) long  timestamp
		 * 1) int     vehicle
		 * 2) float     speed
		 * 3) int     highway
		 * 4) int        lane
		 * 5) int   direction
		 * 6) int     segment
		 */
		
		this.inputSchema = inputSchema;
		/* this.vehicleAttribute = new IntColumnReference(1); */
		this.vehicleAttribute = vehicleAttribute;
	}
	
	@Override
	public void processData(WindowBatch windowBatch, IWindowAPI api) {

		windowBatch.initWindowPointers();
		/*
		 * If processing batch `i`, get the last window of the previous batch `i - 1`.
		 */
		windowBatch.initPrevWindowPointers();
		
		int lastPrevWindowStart = windowBatch.getPrevStartPointer();
		int lastPrevWindowEnd   = windowBatch.getPrevEndPointer();
		/*
		 * Do not free the last window of this batch (say `i`), so that the worker that 
		 * processes batch `i + 1` can get the last window of this one.
		 */
		windowBatch.moveFreePointerToNotFreeLastWindow();
		
		int [] startPointers = windowBatch.getWindowStartPointers();
		int []   endPointers = windowBatch.getWindowEndPointers();

		IQueryBuffer inBuffer = windowBatch.getBuffer();
		
		int byteSizeOfInTuple = inputSchema.getByteSizeOfTuple();
		
		IQueryBuffer outBuffer = UnboundedQueryBufferFactory.newInstance();
		
		int pid = ThreadMap.getInstance().get(Thread.currentThread().getId());
		if (debug)
			System.out.println(String.format("[DBG] %20s, thread id %03d pool id %03d", 
					Thread.currentThread().getName(), Thread.currentThread().getId(), pid));
		
		IntMap lastPerVehicleInLast30Sec = IntMapFactory.newInstance(pid);
		/* System.out.println("[DBG] keyOffsets " + keyOffsets); */
		
		/* Compute initial state based on the last window of the previous batch  */
		if (lastPrevWindowStart != -1) {
			for (int i = lastPrevWindowStart; i < lastPrevWindowEnd; i += byteSizeOfInTuple) {
			
				int vehicleValue = vehicleAttribute.eval(inBuffer, inputSchema, i);
				/* Store in the IntMap the position of the observed entry for a particular vehicle */
				lastPerVehicleInLast30Sec.put(vehicleValue, i);
			}
		}
		
		IntMap previous = IntMapFactory.newInstance(pid);
		copy (lastPerVehicleInLast30Sec, previous);
		
		int prevWindowStart = lastPrevWindowStart;
		int prevWindowEnd   = lastPrevWindowEnd;
		
		for (int currentWindow = 0; currentWindow < startPointers.length; currentWindow++) {
			
			int currWindowStart = startPointers[currentWindow];
			int currWindowEnd   = endPointers  [currentWindow];

			/* Is the window empty? */
			if (currWindowStart == -1) {
				
				if (prevWindowStart != -1) {
					/*
					 * The current window is empty, but the previous window was not empty.
					 * Thus, we clear the current state.
					 */
					lastPerVehicleInLast30Sec.clear();
				}
				
				evaluateWindow (api, 
						inBuffer, 
						lastPerVehicleInLast30Sec, 
						previous,
						outBuffer, 
						startPointers, 
						endPointers, 
						currentWindow,
						byteSizeOfInTuple);

			} else { /* Current window is not empty */
				/*
				 * Check for tuples in the previous window that are not in current window.
				 */
				if (prevWindowStart != -1) {
					
					for (int i = prevWindowStart; i < currWindowStart; i += byteSizeOfInTuple) {
						
						int vehicleValue = vehicleAttribute.eval(inBuffer, inputSchema, i);
						
						if (lastPerVehicleInLast30Sec.containsKey(vehicleValue)) {
							/* Get its position in the input buffer */
							int lastSeen = lastPerVehicleInLast30Sec.get(vehicleValue);
							if (lastSeen == i)
								lastPerVehicleInLast30Sec.remove(vehicleValue);
						}
					}
				}

				/*
				 * Check for tuples in current window that have not appeared in the previous window.
				 */
				if (prevWindowStart != -1) {
					
					for (int i = prevWindowEnd; i < currWindowEnd; i += byteSizeOfInTuple) {
						
						int vehicleValue = vehicleAttribute.eval(inBuffer, inputSchema, i);
						/* Update or insert */
						lastPerVehicleInLast30Sec.put(vehicleValue, i);
					}
				} else {
					/* The previous window was empty
					 * 
					 * TODO
					 * 
					 * What is the purpose of this else statement?
					 * 
					 * Isn't prevWindowEnd = currWindowStart? The answer is no, if  we 
					 * have a sliding window. That is, the previous if-branch iterates
					 * over the tuples added with the slide.
					 */
					for (int i = currWindowStart; i < currWindowEnd; i += byteSizeOfInTuple) {

						int vehicleValue = vehicleAttribute.eval(inBuffer, inputSchema, i);
						/* Update or insert */
						lastPerVehicleInLast30Sec.put(vehicleValue, i);
					}
				}

				evaluateWindow (api, 
						inBuffer, 
						lastPerVehicleInLast30Sec, 
						previous,
						outBuffer, 
						startPointers, 
						endPointers, 
						currentWindow,
						byteSizeOfInTuple);
			}
			/*
			 * TODO
			 * 
			 * Before, the following two statements were nested one level up.
			 *
			 */
			prevWindowStart = currWindowStart;
			prevWindowEnd   = currWindowEnd;
		}
		
		/* Release IntMap objects to the pool */
		lastPerVehicleInLast30Sec.release();
		previous.release();
		
		/* Release window batch buffer */
		inBuffer.release();
		/* Reuse window batch by setting the new buffer for the output data. 
		 * The schema does not change */
		windowBatch.setBuffer(outBuffer);
		
		if (debug)
			System.out.println("[DBG] output buffer position is " + outBuffer.position());
		
		api.outputWindowBatchResult(-1, windowBatch);
	}
	
	private void evaluateWindow (IWindowAPI api, 
			IQueryBuffer inBuffer,
			IntMap lastPerVehicleInLast30Sec, 
			IntMap previous,
			IQueryBuffer outBuffer, 
			int[] startPointers, 
			int[] endPointers,
			int currentWindow, 
			int byteSizeOfTuple) {

		if (lastPerVehicleInLast30Sec.isEmpty()) {
			
			startPointers [currentWindow] = -1;
			endPointers   [currentWindow] = -1;
			
			previous.clear();
			
		} else {
			
			int currOutBufferPos = outBuffer.position();
			startPointers[currentWindow] = currOutBufferPos;
			/*
			 *  Iterate over all vehicle ids that did not occur in the previous window
			 *  and write them to output stream.
			 */
			IntMapEntry [] entries = lastPerVehicleInLast30Sec.getEntries();
			for (int k = 0; k < entries.length; k++) {
				IntMapEntry e = entries[k];
				/*
				 * TODO
				 * 
				 * Checking whether the previous window's key set contains a 
				 * key of this window is not sufficient, since a vehicle may 
				 * have traveled to a different segment of a highway. So, we
				 * would be preventing this new vehicle entry from appearing 
				 * in the output stream.
				 * 
				 * That's why `previous` is now an IntMap, storing also the
				 * position of tuples emitted in the output buffer.
				 * 
				 * We check whether a tuple is the same by looking into its
				 * position in the input stream. If they are not the same,
				 * we emit the tuple again.
				 */
				while (e != null) {
					if (previous.containsKey(e.key))
						if (previous.get(e.key) == e.value)
							continue;
					/* Copy tuple to output stream */
					outBuffer.put(inBuffer, e.value, byteSizeOfTuple);
					e = e.next;
				}
			}
			/*
			 * Check whether any tuples were written to output stream or not.
			 * If none, then the output window is empty.
			 */
			if (currOutBufferPos == outBuffer.position()) {
				/* Set empty window */
				startPointers [currentWindow] = -1;
				endPointers   [currentWindow] = -1;
				/* Clear state */
				previous.clear();
			
			} else {
				/* Update state and close output window */
				copy (lastPerVehicleInLast30Sec, previous);
				endPointers [currentWindow] = outBuffer.position() - 1;
			}
		}
	}
	
	private void copy (IntMap source, IntMap destination) {
		
		destination.clear();
		IntMapEntry [] entries = source.getEntries();
		for (int k = 0; k < entries.length; k++) {
			IntMapEntry e = entries[k];
			while (e != null) {
				destination.put(e.key, e.value);
				e = e.next;
			}
		}
		return ;
	}
	
	@Override
	public void processData(WindowBatch firstWindowBatch,
			WindowBatch secondWindowBatch, IWindowAPI api) {
		
		throw new UnsupportedOperationException
		("LRB UDF is a single input operator and does not operate on two streams");
	}
}
