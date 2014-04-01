package uk.ac.imperial.lsds.seep.operator;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;

public interface CommunicationPrimitives {

	public void send(DataTuple dt);
	public void send_toIndex(DataTuple dt, int idx);
	public void send_splitKey(DataTuple dt, int key);
	public void send_toStreamId_splitKey(DataTuple dt, int streamId, int key);
	public void send_toStreamId_toAll(DataTuple dt, int streamId);
	public void send_all(DataTuple dt);
	public void send_toStreamId(DataTuple dt, int streamId);
	
}
