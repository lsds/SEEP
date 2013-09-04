package uk.ac.imperial.lsds.seep.runtimeengine;

import java.io.Serializable;
import java.net.InetAddress;

import uk.ac.imperial.lsds.seep.operator.EndPoint;

public class DisposableCommunicationChannel implements EndPoint, Serializable {

	private static final long serialVersionUID = 1L;
	private int opId;
	private InetAddress ip;
	
	public DisposableCommunicationChannel(int opId, InetAddress ip) {
		this.opId = opId;
		this.ip = ip;
	}

	@Override
	public int getOperatorId() {
		return opId;
	}
	
	public InetAddress getIp(){
		return ip;
	}
}
