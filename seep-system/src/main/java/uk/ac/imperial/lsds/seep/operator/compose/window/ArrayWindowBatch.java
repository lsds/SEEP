package uk.ac.imperial.lsds.seep.operator.compose.window;

import java.util.Arrays;

import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOpTuple;
import uk.ac.imperial.lsds.seep.operator.compose.multi.SubQueryBuffer;

public class ArrayWindowBatch extends WindowBatch implements IWindowBatch {

	private MultiOpTuple[] array;

	public ArrayWindowBatch() {
		this.array = new MultiOpTuple[0];
		super.windowStartPointers = new int[0];
		super.windowEndPointers = new int[0];
	}
	
	public ArrayWindowBatch(MultiOpTuple[] flatInputList, int[] windowStartPointers) {
		this.array = flatInputList;
		super.windowStartPointers = windowStartPointers;
		
		for (int i = 0; i < super.windowStartPointers.length; i++) {
			if (i == super.windowStartPointers.length - 1)
				super.windowEndPointers[i] = this.array.length;
			else
				super.windowEndPointers[i] = super.windowStartPointers[i+1];
		}
		
		if (this.array.length > 0) {
			super.startTimestamp = this.array[0].timestamp;
			super.endTimestamp = this.array[this.array.length-1].timestamp;
		}
	}

//	public WindowBatch(MultiOpTuple[][] inputList) {
//		this.flatInputList = new ArrayList<>();
//		this.windowStartPointers = new int[inputList.size()];
//
//		for (int i = 0; i < inputList.size(); i++) {
//			this.windowStartPointers[i] = this.flatInputList.size();
//			this.flatInputList.addAll(inputList.get(i));
//		}
//	}

	@Override
	public MultiOpTuple get(int index) {
		return this.array[index];
	}

	@Override
	public void addWindow(MultiOpTuple[] window) {
		int[] newWindowStartPointers = Arrays.copyOf(this.windowStartPointers, this.windowStartPointers.length + 1);
		int[] newWindowEndPointers = Arrays.copyOf(this.windowEndPointers, this.windowEndPointers.length + 1);
		
		newWindowStartPointers[newWindowStartPointers.length - 1] = this.array.length;
		newWindowEndPointers[newWindowEndPointers.length - 1] = this.array.length + window.length - 1;
		
		MultiOpTuple[] newflatInputList = Arrays.copyOf(this.array, this.array.length + window.length);
		for (int i = 0; i < window.length; i++)
			newflatInputList[this.array.length + i] = window[i];
		
		this.array = newflatInputList;
		this.windowStartPointers = newWindowStartPointers;
		this.windowEndPointers = newWindowEndPointers;
	}

	@Override
	public SubQueryBuffer getBufferContent() {
		throw new UnsupportedOperationException("Window batch is backed by array, not by circular buffer.");
	}

	@Override
	public MultiOpTuple[] getArrayContent() {
		return this.array;
	}

}
