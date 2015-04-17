package uk.ac.imperial.lsds.seep.multi;

public interface ITupleSchema {

	public int getNumberOfAttributes();
	
	public int [] getOffsets();
	
	public int getOffsetForAttribute(int index);

	public int getByteSizeOfTuple();

	public byte [] getDummyContent();
	
	public void setType (int idx, int type);
	
	public int getType (int idx);

}
