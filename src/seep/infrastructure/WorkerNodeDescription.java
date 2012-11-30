package seep.infrastructure;

import java.net.InetAddress;

public class WorkerNodeDescription {
	
	private int nodeId;
	private InetAddress ip;
	private int ownPort;
	
	public int getNodeId() {
		return nodeId;
	}

	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}

	public InetAddress getIp() {
		return ip;
	}

	public void setIp(InetAddress ip) {
		this.ip = ip;
	}

	public int getOwnPort() {
		return ownPort;
	}

	public void setOwnPort(int ownPort) {
		this.ownPort = ownPort;
	}
	
	public WorkerNodeDescription(InetAddress ip, int ownPort){
		///\todo{generate a unique id for the node}
		this.ip = ip;
		this.ownPort = ownPort;
	}
}
