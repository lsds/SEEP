package uk.ac.imperial.lsds.streamsql.expressions;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;

public class Value<T extends Comparable<T>> implements IValue<T> {

	
	/*
	 * The actual value content
	 */
	T content;
	
	public T evaluate(DataTuple tuple) {
		return content;
	}
	
}
