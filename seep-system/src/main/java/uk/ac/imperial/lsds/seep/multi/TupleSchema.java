package uk.ac.imperial.lsds.seep.multi;

import java.util.Arrays;

public class TupleSchema implements ITupleSchema {
	
	private int [] types; /* 0:undefined 1:int, 2:float, 3:long */
	
	private int [] offsets;
	private int byteSize;
	private int contentByteSize;
	private int dummyByteSize;
	private byte [] dummyContent;
	
	public TupleSchema (int[] offsets, int contentByteSize) {
		
		this.offsets = offsets;
		this.contentByteSize = contentByteSize;
		
		this.dummyByteSize = 0;
		this.dummyContent = new byte [0];
		
		this.byteSize = this.contentByteSize + this.dummyByteSize;
		
		if (this.contentByteSize != 0) {
			/*
			 * Expand size if needed to ensure that tuple size 
			 * is a power of two.
			 */
			if ((contentByteSize & (contentByteSize - 1)) != 0) {
				
				this.byteSize = 1;
				while (this.contentByteSize > this.byteSize)
					this.byteSize *= 2;
				
				this.dummyByteSize = this.byteSize - this.contentByteSize;
				this.dummyContent = new byte[this.dummyByteSize];
				/* Initialise dummy content */
				for (int i = 0; i < this.dummyContent.length; i++)
					this.dummyContent[i] = 0;
			}
		}
		
		types = new int [offsets.length];
		Arrays.fill(types, 0);
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
	public byte [] getDummyContent() {
		
		return this.dummyContent;
	}

	@Override
	public int [] getOffsets() {
		
		return offsets;
	}

	@Override
	public void setType(int idx, int type) {
		if (idx < 0 || idx >= types.length)
			return;
		types[idx] = type;
	}
	
	@Override
	public int getType(int idx) {
		if (idx < 0 || idx >= types.length)
			return 0;
		return types[idx];
	}
}
