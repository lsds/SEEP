package uk.ac.imperial.lsds.seep.operator.compose2;

import java.util.List;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;

public interface Window extends List<DataTuple>, Iterable<DataTuple> {
	
	public int getStart();

	public int getEnd();

	public boolean isCountBased();

	public boolean isRangeBased();

	public long getSize();
	
	public long getSlide();
	
	public List<DataTuple> getInputList();
	
}
