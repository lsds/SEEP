package uk.ac.imperial.lsds.streamsql.windows;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.streamsql.operator.WindowOperator;

public class RowWindow implements Window {

	/*
	 * Size of the window in terms of tuples, i.e.,
	 * how many tuples are in a window
	 */
	private int size = 1;

	/*
	 * Slide of the window in terms of tuples, i.e., 
	 * after how many tuples do we trigger operator evaluation
	 */
	private int slide = 1;
	private int tuplesSinceLastEvaluation = 0;

	private Queue<DataTuple> state;
	
	private Set<WindowOperator> callBacks;
	
	public RowWindow(int size, int slide) {
		
		assert(size >= slide);
		
		this.size = size;
		this.slide = slide;
		
		this.state = new LinkedList<>();
		this.callBacks = new HashSet<>();
	}

	public RowWindow(int size) {
		this(size, size);
	}

	public RowWindow() {
		this(1);
	}

	@Override
	public void updateWindow(DataTuple tuple) {
		/*
		 * Update the window with a new tuple 
		 */
		this.state.add(tuple);
		if (this.state.size() > this.size)
			this.state.remove();
		
		tuplesSinceLastEvaluation++;

		/*
		 * Check whether operator evaluation shall be triggered
		 */
		if (tuplesSinceLastEvaluation >= slide) {
			for (WindowOperator op : this.callBacks)
				op.evaluateWindow(this.state);
			
			tuplesSinceLastEvaluation = 0;
		}
		
	}

	@Override
	public void updateWindow(List<DataTuple> tuples) {
		for (DataTuple tuple : tuples)
			updateWindow(tuple);
	}

	@Override
	public void registerCallback(WindowOperator operator) {
		this.callBacks.add(operator);
	}

}
