package uk.ac.imperial.lsds.seep.multi;

import java.nio.ByteBuffer;

public class WindowTuple {
	
	public int          hashcode; /* key */
	public IQueryBuffer   buffer; /* key */
	public int            offset; /* key */
	public int            length; /* key */
	public float           value;
	public int             count;
	
	public WindowTuple next;
	
	public WindowTuple (int hashcode, IQueryBuffer buffer, int offset, int length, float value, int count, WindowTuple next) {
		
		this.hashcode = hashcode;
		this.buffer   =   buffer;
		this.offset   =   offset;
		this.length   =   length;
		this.value    =    value;
		this.count    =    count;
		
		this.next = next;
	}
	
	public WindowTuple () {
		
		this.hashcode = -1;
		this.buffer   = null;
		this.offset   = -1;
		this.length   = -1;
		this.value    = Float.MIN_VALUE;
		this.count    = -1;
		
		this.next = null;
	}

	public void set (int hashcode, IQueryBuffer buffer, int offset, int length, float value, int count, WindowTuple next) {
		
		this.hashcode = hashcode;
		this.buffer   =   buffer;
		this.offset   =   offset;
		this.length   =   length;
		this.value    =    value;
		this.count    =    count;
		
		this.next = next;
	}
	
	public void release (int pid) {
		WindowTupleFactory.free(pid, this);
	}
	
	public int compareTo (int otherOffset) {
		
		int n = this.offset + this.length;
		
		for (int i = this.offset, j = otherOffset; i < n; i++, j++) {
			
			byte v1 = this.buffer.getByteBuffer().get(i);
			byte v2 = this.buffer.getByteBuffer().get(j);
			
			if (v1 == v2)
				continue;
			
			if ((v1 != v1) && (v2 != v2))
				continue;
			
			if (v1 < v2)
				return -1;
			
			return +1;
		}
		
		return 0;
	}
	
}
