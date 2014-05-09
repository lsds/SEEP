package uk.ac.imperial.lsds.streamsql.windows;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.streamsql.expressions.Constants;
import uk.ac.imperial.lsds.streamsql.operator.WindowOperator;

public class RangeWindow implements Window {

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
	
	private Set<WindowOperator> callBacks;
	
	public RangeWindow(int size, int slide) {
		
		assert(size >= slide);
		
		this.size = size;
		this.slide = slide;
		
		this.state = new LinkedList<>();
		this.callBacks = new HashSet<>();
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
		
		DataTuple head = this.state.peek();
		if (head != null) {
			long headTime = head.getLong(Constants.TIMESTAMP);
			
			while ((head != null) 
					&& (this.size >= this.currentTime - headTime)) {
				this.state.remove();
				head = this.state.peek();
				headTime = head.getLong(Constants.TIMESTAMP);
			}
		}

		/*
		 * Check whether operator evaluation shall be triggered
		 */
		if (this.currentTime - this.lastTriggerTime >= slide) {
			for (WindowOperator op : this.callBacks)
				op.evaluateWindow(this.state);
			
			this.lastTriggerTime = this.currentTime;
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
