package uk.ac.imperial.lsds.streamsql.operator;

import java.util.ArrayList;

import uk.ac.imperial.lsds.seep.api.largestateimpls.SeepMap;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.StatefulOperator;
import uk.ac.imperial.lsds.seep.state.StateWrapper;
import uk.ac.imperial.lsds.streamsql.util.Util;

public class Distinct implements StatefulOperator {

	private static final long serialVersionUID = 1L;

	SeepMap<String, String> state;
	
	@Override
	public void setUp() {
		state = new SeepMap<>();
	}

	@Override
	public void processData(DataTuple data) {
		String key = Util.generateTupleString(data);
		
		/*
		 * Check whether tuple was observed already
		 */
		if (!state.containsKey(key))
			/*
			 * Send the respective tuple
			 */
			api.send(data);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Distinct");
		return sb.toString();
	}

	@Override
	public void processData(ArrayList<DataTuple> dataList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public StateWrapper getState() {
		return new StateWrapper(1, 1, this.state);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void replaceState(StateWrapper state) {
		this.state = (SeepMap<String, String>) state.getStateImpl();
	}

}
