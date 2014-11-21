package uk.ac.imperial.lsds.seep.multi;

public class TupleSchemaImpl implements TupleSchema {

	private int[] offsets;
	private int byteSize;
	
	public TupleSchemaImpl(int[] offsets, int byteSize) {
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
