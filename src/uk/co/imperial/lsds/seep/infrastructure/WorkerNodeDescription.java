/*******************************************************************************
 * Copyright (c) 2013 Raul Castro Fernandez (Ra).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Ra - Design and initial implementation
 ******************************************************************************/
package uk.co.imperial.lsds.seep.infrastructure;

import java.net.InetAddress;
import java.util.UUID;

public class WorkerNodeDescription {
	
	private int nodeId;
	private InetAddress ip;
	private int ownPort;
	
	public int getNodeId() {
		return nodeId;
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
		this.nodeId = generateNodeIdFromIp(ip);
		this.ip = ip;
		this.ownPort = ownPort;
	}
	
	private int generateNodeIdFromIp(InetAddress ip){
		return UUID.randomUUID().hashCode();
	}
}
