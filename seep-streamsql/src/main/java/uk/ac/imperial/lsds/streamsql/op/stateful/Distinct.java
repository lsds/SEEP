package uk.ac.imperial.lsds.streamsql.op.stateful;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.API;
import uk.ac.imperial.lsds.seep.operator.StatefulOperator;
import uk.ac.imperial.lsds.seep.operator.compose.micro.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowAPI;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowBatch;
import uk.ac.imperial.lsds.seep.state.State;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.op.WindowOperator;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;
import uk.ac.imperial.lsds.streamsql.windows.Window;

public class Distinct implements StatefulOperator, IStreamSQLOperator, WindowOperator {

	private static Logger LOG = LoggerFactory.getLogger(Distinct.class);

	private static final long serialVersionUID = 1L;

	private Window window;
	
	private transient DistinctState state;

	public class DistinctState {
		public Map<String, Integer> tupleCount = new HashMap<>();
		public Map<String, DataTuple> tupleRef = new HashMap<>();
	}
	
	public Distinct() {
		
	}
	
	@Override
	public void setUp() {
		this.window.registerCallbackEvaluateWindow(this);
		this.window.registerCallbackEnterWindow(this);
		this.window.registerCallbackExitWindow(this);
	}

	@Override
	public void processData(DataTuple data, API api) {
		this.window.updateWindow(data);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Distinct");
		return sb.toString();
	}

	@Override
	public void processData(List<DataTuple> dataList, API api) {
		this.window.registerAPI(this, api);
		this.window.updateWindow(dataList);
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
		for (DataTuple t : this.state.tupleRef.values())
			api.send(t);
	}

	@Override
	public void accept(OperatorVisitor ov) {
		ov.visit(this);
	}

	@Override
	public void setState(State state) {
		this.window = (Window) state;

		for (DataTuple tuple : this.window.getWindowContent()) 
			this.enteredWindow(tuple, null);
	}

	@Override
	public State getState() {
		return this.window;
	}

	@Override
	public void enteredWindow(DataTuple tuple, API api) {
		String stringPayload = tuple.getPayload().toString();
		if (this.state.tupleCount.containsKey(stringPayload)) {
			int currentCount = this.state.tupleCount.get(stringPayload);
			this.state.tupleCount.put(stringPayload, currentCount++);
		}
		else {
			this.state.tupleCount.put(stringPayload, 1);
			this.state.tupleRef.put(stringPayload, tuple);
		}
	}

	@Override
	public void exitedWindow(DataTuple tuple, API api) {
		String stringPayload = tuple.getPayload().toString();
		assert(this.state.tupleCount.containsKey(stringPayload));
		
		if (this.state.tupleCount.containsKey(stringPayload)) {
			int currentCount = this.state.tupleCount.get(stringPayload);
			if (currentCount == 1) {
				this.state.tupleCount.remove(stringPayload);
				this.state.tupleRef.remove(stringPayload);
			}
			else
				this.state.tupleCount.put(stringPayload, currentCount--);
		}
	}


}
