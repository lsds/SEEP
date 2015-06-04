package uk.ac.imperial.lsds.seep.multi;

public class IntermediateTuple {

	public long timestamp;
	
	public Key key;
	
	public float value;
	public int count;
	
	public IntermediateTuple next;
	
	public IntermediateTuple () {
		
		set (-1, null, Float.MIN_VALUE, Integer.MIN_VALUE, null);
	}
	
	public IntermediateTuple (long timestamp, Key key, float value, int count, IntermediateTuple next) {
		
		set (timestamp, key, value, count, next);
	}
	
	public void set(long timestamp, Key key, float value, int count, IntermediateTuple next) {
		
		this.timestamp = timestamp;

		this.key = key;
		
		this.value = value;
		this.count = count;
		
		this.next = next;
	}
	
	public void release (int pid) {
		
		key.release();
		IntermediateTupleFactory.free(pid, this);
	}
}
