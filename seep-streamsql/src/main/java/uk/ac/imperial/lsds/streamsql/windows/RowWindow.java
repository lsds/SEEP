package uk.ac.imperial.lsds.streamsql.windows;

import java.util.LinkedList;
import java.util.Queue;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.streamsql.operator.WindowOperator;

public class RowWindow extends Window {

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

	public RowWindow(int size, int slide) {
		super();
		assert(size >= slide);
		
		this.size = size;
		this.slide = slide;
		
		this.state = new LinkedList<>();
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
		for (WindowOperator op : this.callBacksEnter)
			op.enteredWindow(tuple, this.callBackAPI.get(op));

		if (this.state.size() > this.size) {
			DataTuple removed = this.state.remove();
			for (WindowOperator op : this.callBacksExit)
				op.exitedWindow(removed, this.callBackAPI.get(op));
		}
		
		tuplesSinceLastEvaluation++;

		/*
		 * Check whether operator evaluation shall be triggered
		 */
		if (tuplesSinceLastEvaluation >= slide) {
			for (WindowOperator op : this.callBacksEvaluation)
				op.evaluateWindow(this.state, this.callBackAPI.get(op));
			
			tuplesSinceLastEvaluation = 0;
		}
	}
	
	
	@Override
	public Queue<DataTuple> getWindowContent() {
		return this.state;
	}

}
