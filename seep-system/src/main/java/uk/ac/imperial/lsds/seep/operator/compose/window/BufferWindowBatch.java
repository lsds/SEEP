package uk.ac.imperial.lsds.seep.operator.compose.window;

import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOpTuple;
import uk.ac.imperial.lsds.seep.operator.compose.multi.SubQueryBuffer;

public class BufferWindowBatch extends WindowBatch implements IWindowBatch {

	SubQueryBuffer buffer;

	public BufferWindowBatch(SubQueryBuffer buffer, int[] windowStartPointers, int[] windowEndPointers, long startTimestamp, long endTimestamp) {
		this.buffer = buffer;
		super.windowStartPointers = windowStartPointers;
		super.windowEndPointers = windowEndPointers;
		
		super.startTimestamp = startTimestamp;
		super.endTimestamp = endTimestamp;
	}

	public BufferWindowBatch(SubQueryBuffer buffer, int[] windowStartPointers, int[] windowEndPointers) {
		this.buffer = buffer;
		super.windowStartPointers = windowStartPointers;
		super.windowEndPointers = windowEndPointers;
	}
	
	@Override
	public MultiOpTuple get(int index) {
		return this.buffer.get(index);
	}

	@Override
	public void addWindow(MultiOpTuple[] window) {
		throw new UnsupportedOperationException("BufferWindowBatch is backed by SubQueryBuffer, which is immutable.");
	}

	@Override
	public SubQueryBuffer getBufferContent() {
		return this.buffer;
	}

	@Override
	public MultiOpTuple[] getArrayContent() {
		throw new UnsupportedOperationException("Window batch is backed by SubQueryBuffer, so we cannot return the content as an array reference.");
	}

//	public MultiOpTuple[] getContentCopy() {
//		return this.buffer.getArray(this.windowStartPointers[0], this.windowEndPointers[this.windowEndPointers.length-1]);
//	}

	
}
