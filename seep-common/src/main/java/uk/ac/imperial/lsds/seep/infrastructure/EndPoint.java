package uk.ac.imperial.lsds.seep.infrastructure;

import java.net.InetAddress;

public final class EndPoint {

	private final InetAddress ip;
	private final int port;
	
	public EndPoint(InetAddress ip, int port){
		this.ip = ip;
		this.port = port;
	}

	public InetAddress getIp() {
		return ip;
	}

	public int getPort() {
		return port;
	}
}
