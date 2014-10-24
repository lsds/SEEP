package uk.ac.imperial.lsds.seep.infrastructure;

import java.net.InetAddress;

public final class EndPoint {

	private final int id;
	private final InetAddress ip;
	private final int port;
	
	public EndPoint(int id, InetAddress ip, int port){
		this.id = id;
		this.ip = ip;
		this.port = port;
	}
	
	public int getId(){
		return id;
	}

	public InetAddress getIp() {
		return ip;
	}

	public int getPort() {
		return port;
	}
}
