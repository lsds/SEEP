package uk.ac.imperial.lsds.seep.operator.compose.window;

import java.util.Iterator;
import java.util.List;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;

public interface IWindowBatch extends Iterable<DataTuple> {
	
	/*
	 * Getters for values that define the window batch 
	 */
	public int getStart();
	public int getEnd();

	/*
	 * Get method for the definition of the window
	 */
	public IWindowDefinition getWindowDefinition();
	
	/*
	 * Methods to access the window content
	 * 
	 * Mimic iterator on the window level. Iterator
	 * on the element level is given by implementing
	 * Iterable<DataTuple> 
	 */
	public List<DataTuple> getAllTuples();
	public DataTuple get(int index);
	public Iterator<List<DataTuple>> windowIterator();

}
