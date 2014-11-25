package uk.ac.imperial.lsds.seep.multi;

public interface ITupleSchema {

	public int getNumberOfAttributes();
	
	public int getOffsetForAttribute(int index);

	public int getByteSizeOfTuple();

}
