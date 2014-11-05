package uk.ac.imperial.lsds.seepworker.core.input;

import java.util.List;

import uk.ac.imperial.lsds.seepworker.data.Data;


public interface InputAdapter {

	public short rType();
	
	public Data pullDataItem();
	public List<Data> pullDataItems();
	
}
