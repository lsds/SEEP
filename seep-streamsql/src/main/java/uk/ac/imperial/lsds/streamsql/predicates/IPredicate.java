package uk.ac.imperial.lsds.streamsql.predicates;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;

public interface IPredicate {
	
	public boolean satisfied(DataTuple tuple);

	public boolean satisfied(DataTuple firstTuple, DataTuple secondTuple);

}
