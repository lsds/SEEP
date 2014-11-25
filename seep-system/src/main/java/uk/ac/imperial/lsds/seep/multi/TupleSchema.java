package uk.ac.imperial.lsds.seep.multi;

public class TupleSchema implements ITupleSchema {

	private int[] offsets;
	private int byteSize;
	
	public TupleSchema(int[] offsets, int byteSize) {
		this.offsets = offsets;
		this.byteSize = byteSize;
	}
	
	@Override
	public int getNumberOfAttributes() {
		return offsets.length;
	}

	@Override
	public int getOffsetForAttribute(int index) {
		return offsets[index];
	}

	@Override
	public int getByteSizeOfTuple() {
		return byteSize;
	}



}
