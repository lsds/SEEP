package seep.operator.collection.lrbenchmark.beans;

public class VehiclePosition {

	public int vid;
	public int segment;
	public int dir;
	public int lane;
	public int pos;
	
	public VehiclePosition(int vid, int segment, int dir, int lane, int pos) {
		this.vid = vid;
		this.segment = segment;
		this.dir = dir;
		this.lane = lane;
		this.pos = pos;
	}
}
