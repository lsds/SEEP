package uk.ac.imperial.lsds.streamsql.operator;

import java.util.List;
import java.util.Queue;

import uk.ac.imperial.lsds.seep.api.largestateimpls.SeepMap;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.StatefulOperator;
import uk.ac.imperial.lsds.seep.state.StateWrapper;
import uk.ac.imperial.lsds.streamsql.util.Util;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;
import uk.ac.imperial.lsds.streamsql.windows.Window;

public class Distinct implements StatefulOperator, IStreamSQLOperator, WindowOperator {

	private static final long serialVersionUID = 1L;

	private SeepMap<String, String> state;
	
	private Window window;
	
	public Distinct(Window window) {
		this.window = window;
	}
	
	@Override
	public void setUp() {
		state = new SeepMap<>();
		this.window.registerCallback(this);
	}

	@Override
	public void processData(DataTuple data) {
		this.window.updateWindow(data);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Distinct");
		return sb.toString();
	}

	@Override
	public void processData(List<DataTuple> dataList) {
		this.window.updateWindow(dataList);
	}

	@Override
	public void evaluateWindow(Queue<DataTuple> dataList) {
//		String key = Util.generateTupleString(data);
//		
//		/*
//		 * Check whether tuple was observed already
//		 */
//		if (!state.containsKey(key))
//			/*
//			 * Send the respective tuple
//			 */
//			api.send(data);

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
	
	@Override
	public void accept(OperatorVisitor ov) {
		ov.visit(this);
	}

}
