package uk.ac.imperial.lsds.streamsql.windows;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.API;
import uk.ac.imperial.lsds.streamsql.operator.WindowOperator;

public abstract class Window implements IWindow {

	protected Set<WindowOperator> callBacksEvaluation;
	protected Set<WindowOperator> callBacksEnter;
	protected Set<WindowOperator> callBacksExit;
	protected Map<WindowOperator, API> callBackAPI;

	public Window() {
		this.callBacksEvaluation = new HashSet<>();
		this.callBacksEnter = new HashSet<>();
		this.callBacksExit = new HashSet<>();
		this.callBackAPI = new HashMap<>();
	}

	@Override
	public abstract void updateWindow(DataTuple tuple);
	
	@Override
	public void updateWindow(List<DataTuple> tuples) {
		for (DataTuple tuple : tuples)
			updateWindow(tuple);
	}
	
	@Override
	public void registerCallbackEvaluateWindow(WindowOperator operator) {
		this.callBacksEvaluation.add(operator);
	}

	@Override
	public void registerCallbackEnterWindow(WindowOperator operator) {
		this.callBacksEnter.add(operator);
	}

	@Override
	public void registerCallbackExitWindow(WindowOperator operator) {
		this.callBacksExit.add(operator);
	}

	@Override
	public void registerAPI(WindowOperator operator, API api) {
		this.callBackAPI.put(operator, api);
	}

}
