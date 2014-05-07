package uk.ac.imperial.lsds.streamsql.operator;

import java.util.ArrayList;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.StatefulOperator;
import uk.ac.imperial.lsds.seep.state.StateWrapper;
import uk.ac.imperial.lsds.streamsql.predicates.IPredicate;

public class Selection implements StatefulOperator {

	private static final long serialVersionUID = 1L;

	IPredicate predicate;

	public Selection(IPredicate predicate) {
		this.predicate = predicate;
	}

	@Override
	public void processData(DataTuple data) {

		/*
		 * Check whether predicate is satisfied for tuple 
		 */
		if (this.predicate.satisfied(data)) 
			/*
			 * Send the selected tuple
			 */
			api.send(data);
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Selection (");
		sb.append(predicate.toString());
		sb.append(")");
		return sb.toString();
	}

	@Override
	public void setUp() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processData(ArrayList<DataTuple> dataList) {
		for (DataTuple tuple : dataList)
			processData(tuple);
	}

	@Override
	public StateWrapper getState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void replaceState(StateWrapper state) {
		// TODO Auto-generated method stub
		
	}

}
