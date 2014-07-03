package uk.ac.imperial.lsds.seep.operator.compose.window;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;

public class StaticWindowBatch implements IStaticWindowBatch {

	private List<List<DataTuple>> inputList;
	private List<DataTuple> flatInputList;
	
	public StaticWindowBatch(List<List<DataTuple>> inputList) {
		this.inputList = inputList;
		this.flatInputList = new ArrayList<>();
		for (List<DataTuple> list : inputList) 
			this.flatInputList.addAll(list);
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
		return this.inputList.iterator();
	}

	
}
