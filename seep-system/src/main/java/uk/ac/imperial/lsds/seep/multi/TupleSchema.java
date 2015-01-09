package uk.ac.imperial.lsds.seep.multi;

public class TupleSchema implements ITupleSchema {

	private int[] offsets;
	private int   byteSize;
	private int   contentByteSize;
	private int   dummyByteSize;
	private byte[] dummyContent;
	
	public TupleSchema(int[] offsets, int contentByteSize) {
		
		this.offsets = offsets;
		this.contentByteSize = contentByteSize;
		this.dummyByteSize = 0;
		this.dummyContent = new byte[0];
		this.byteSize = this.contentByteSize + this.dummyByteSize;
		
		if (contentByteSize != 0) {
			/*
			 * Expand size if needed to ensure that byte size 
			 * is power of two
			 */
			if ((contentByteSize & (contentByteSize - 1)) != 0) {
				
				this.byteSize = 1;
				while (this.contentByteSize > this.byteSize)
					this.byteSize *= 2;
				
				this.dummyByteSize = this.byteSize - contentByteSize;
				this.dummyContent  = new byte[this.dummyByteSize];
				for (int i = 0; i < this.dummyContent.length; i++)
					this.dummyContent[i] = 0;
			}
		}
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

	@Override
	public byte[] getDummyContent() {
		return this.dummyContent;
	}



}
