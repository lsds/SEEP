package uk.ac.imperial.lsds.streamsql.windows;

import java.util.LinkedList;
import java.util.Queue;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.streamsql.expressions.Constants;
import uk.ac.imperial.lsds.streamsql.op.stateful.WindowOperator;

public class RangeWindow extends Window {

	/*
	 * Size of the window in terms of timestamp units, i.e.,
	 * what is the time span considered for selecting tuples into the window?
	 */
	private int size = 1;

	/*
	 * Slide of the window in terms of timestamp units, i.e., 
	 * what is the time span after which we trigger operator evaluation?
	 */
	private int slide = 1;
	private long currentTime = 0;
	private long lastTriggerTime = 0;

	private Queue<DataTuple> state;
	
	public RangeWindow(int size, int slide) {
		super();
		assert(size >= slide);
		
		this.size = size;
		this.slide = slide;
		
		this.state = new LinkedList<>();
	}

	public RangeWindow(int size) {
		this(size, size);
	}

	public RangeWindow() {
		this(1);
	}

	@Override
	public void updateWindow(DataTuple tuple) {
		/*
		 * Update the window with a new tuple 
		 */
		this.state.add(tuple);
		this.currentTime = tuple.getLong(Constants.TIMESTAMP);
		for (WindowOperator op : this.callBacksEnter)
			op.enteredWindow(tuple,this.callBackAPI.get(op));
		
		DataTuple head = this.state.peek();
		if (head != null) {
			long headTime = head.getLong(Constants.TIMESTAMP);
			
			while ((head != null) 
					&& (this.size >= this.currentTime - headTime)) {

				DataTuple removed = this.state.remove();
				for (WindowOperator op : this.callBacksExit)
					op.exitedWindow(removed, this.callBackAPI.get(op));
				
				head = this.state.peek();
				headTime = head.getLong(Constants.TIMESTAMP);
			}
		}

		/*
		 * Check whether operator evaluation shall be triggered
		 */
		if (this.currentTime - this.lastTriggerTime >= slide) {
			for (WindowOperator op : this.callBacksEvaluation)
				op.evaluateWindow(this.state, this.callBackAPI.get(op));
			
			this.lastTriggerTime = this.currentTime;
		}
	}

	@Override
	public Queue<DataTuple> getWindowContent() {
		return this.state;
	}
}
