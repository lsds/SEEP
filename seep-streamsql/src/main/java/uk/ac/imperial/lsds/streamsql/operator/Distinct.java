package uk.ac.imperial.lsds.streamsql.operator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.API;
import uk.ac.imperial.lsds.seep.operator.StatefulOperator;
import uk.ac.imperial.lsds.seep.state.State;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;
import uk.ac.imperial.lsds.streamsql.windows.WindowState;

public class Distinct implements StatefulOperator, IStreamSQLOperator, WindowOperator {

	private static final long serialVersionUID = 1L;

	private WindowState<DistinctState> windowState;

	public class DistinctState {
		public Map<String, Integer> tupleCount = new HashMap<>();
		public Map<String, DataTuple> tupleRef = new HashMap<>();
	}
	
	public Distinct() {
		
	}
	
	@Override
	public void setUp() {
		this.windowState.getWindow().registerCallbackEvaluateWindow(this);
		this.windowState.getWindow().registerCallbackEnterWindow(this);
		this.windowState.getWindow().registerCallbackExitWindow(this);
	}

	@Override
	public void processData(DataTuple data, API api) {
		this.windowState.getWindow().updateWindow(data);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Distinct");
		return sb.toString();
	}

	@Override
	public void processData(List<DataTuple> dataList, API api) {
		this.windowState.getWindow().registerAPI(this, api);
		this.windowState.getWindow().updateWindow(dataList);
	}

	@Override
	public void evaluateWindow(Queue<DataTuple> dataList, API api) {
		/*
		 * We use this callback only as a trigger to output 
		 * result. The actual result selection is done based on 
		 * the state of this operator, which is updated
		 * using the callbacks for events entering and existing
		 * the window.
		 */
		for (DataTuple t : this.windowState.getState().tupleRef.values())
			api.send(t);
	}

	@Override
	public void accept(OperatorVisitor ov) {
		ov.visit(this);
	}

	@Override
	public void setState(State state) {
		this.windowState = (WindowState<DistinctState>) state;
	}

	@Override
	public State getState() {
		return this.windowState;
	}

	@Override
	public void replaceState(State state) {
		this.windowState = (WindowState<DistinctState>) state;
	}

	@Override
	public void enteredWindow(DataTuple tuple, API api) {
		String stringPayload = tuple.getPayload().toString();
		if (this.windowState.getState().tupleCount.containsKey(stringPayload)) {
			int currentCount = this.windowState.getState().tupleCount.get(stringPayload);
			this.windowState.getState().tupleCount.put(stringPayload, currentCount++);
		}
		else {
			this.windowState.getState().tupleCount.put(stringPayload, 1);
			this.windowState.getState().tupleRef.put(stringPayload, tuple);
		}
	}

	@Override
	public void exitedWindow(DataTuple tuple, API api) {
		String stringPayload = tuple.getPayload().toString();
		assert(this.windowState.getState().tupleCount.containsKey(stringPayload));
		
		if (this.windowState.getState().tupleCount.containsKey(stringPayload)) {
			int currentCount = this.windowState.getState().tupleCount.get(stringPayload);
			if (currentCount == 1) {
				this.windowState.getState().tupleCount.remove(stringPayload);
				this.windowState.getState().tupleRef.remove(stringPayload);
			}
			else
				this.windowState.getState().tupleCount.put(stringPayload, currentCount--);
		}
	}

}
