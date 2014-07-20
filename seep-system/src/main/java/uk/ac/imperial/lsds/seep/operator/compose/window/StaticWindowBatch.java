package uk.ac.imperial.lsds.seep.operator.compose.window;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;

public class StaticWindowBatch implements IStaticWindowBatch {

	private List<DataTuple> flatInputList;
	private int[] windowStartPointers;

	public StaticWindowBatch() {
		this.flatInputList = new ArrayList<>();
		this.windowStartPointers = new int[0];
	}

	
	public StaticWindowBatch(List<DataTuple> flatInputList, int[] windowStartPointers) {
		this.flatInputList = new ArrayList<>();
		this.windowStartPointers = windowStartPointers;
	}

	public StaticWindowBatch(List<List<DataTuple>> inputList) {
		this.flatInputList = new ArrayList<>();
		this.windowStartPointers = new int[inputList.size()];

		for (int i = 0; i < inputList.size(); i++) {
			this.windowStartPointers[i] = this.flatInputList.size();
			this.flatInputList.addAll(inputList.get(i));
		}
	}

	@Override
	public void registerWindow(List<DataTuple> window) {
		int[] newWindowStartPointers = Arrays.copyOf(this.windowStartPointers, this.windowStartPointers.length + 1);
		
		newWindowStartPointers[newWindowStartPointers.length - 1] = this.flatInputList.size();
		this.flatInputList.addAll(window);
		this.windowStartPointers = newWindowStartPointers;
	}
	
	@Override
	public Iterator<DataTuple> iterator() {
		return this.flatInputList.iterator();
	}

	@Override
	public List<DataTuple> getAllTuples() {
		return this.flatInputList;
	}

	@Override
	public DataTuple get(int index) {
		return this.flatInputList.get(index);
	}

	@Override
	public Iterator<List<DataTuple>> windowIterator() {
		return new StaticWindowBatchIterator(this.flatInputList, this.windowStartPointers);
	}


	/**
	 * Just provided for compatibility reasons. However, the static window definition
	 * cannot contain overlapping windows, hence, there is no point in incremental 
	 * computation.
	 */
	@Override
	public void performIncrementalComputation(
			IMicroIncrementalComputation incrementalComputation, IWindowAPI api) {
		
		Iterator<List<DataTuple>> iter = this.windowIterator();
		
		while (iter.hasNext()) {
			List<DataTuple> window = iter.next();
			for (DataTuple tuple : window )
				incrementalComputation.enteredWindow(tuple);
			incrementalComputation.evaluateWindow(api);
			for (DataTuple tuple : window )
				incrementalComputation.exitedWindow(tuple);
		}
	}

}
