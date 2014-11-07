package uk.ac.imperial.lsds.seep.api;

import uk.ac.imperial.lsds.seep.api.data.OTuple;

public interface API {

	public void send(OTuple o);
	public void sendAll(OTuple o);
	public void sendKey(OTuple o, int key);
	public void sendKey(OTuple o, String key);
	public void sendStreamid(int streamId, OTuple o);
	public void sendStreamidAll(int streamId, OTuple o);
	public void sendStreamidKey(int streamId, OTuple o, int key);
	public void sendStreamidKey(int streamId, OTuple o, String key);
	public void send_index(int index, OTuple o);
	public void send_opid(int opId, OTuple o);
	
}
