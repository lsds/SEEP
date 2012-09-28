package seep.comm.serialization;

public class DataTuple {

	private long timestamp;
	private int id;
	
	public long getTs() {
		return timestamp;
	}

	public void setTs(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public void setId(int id){
		this.id = id;
	}
	
	public int getId(){
		return id;
	}
	
	public DataTuple(){
		
	}
	
	public DataTuple(long timestamp, int id){
		this.timestamp = timestamp;
		this.id = id;
	}
	
}
