package uk.ac.imperial.lsds.seep.multi;

public interface TupleSchema {

//	int[] columnTypes;
	
	public int getNumberOfAttributes();
	
	public int getOffsetForAttribute(int index);

	public int getByteSizeOfTuple();

}
