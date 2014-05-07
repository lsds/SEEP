package uk.ac.imperial.lsds.streamsql.expressions;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;

public interface IValue<T extends Comparable<T>> {

	public T evaluate(DataTuple tuple);

}
