/*******************************************************************************
 * Copyright (c) 2013 Imperial College London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial design and implementation
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.infrastructure;

import java.net.InetAddress;
import java.util.UUID;

public class WorkerNodeDescription {
	
	private int nodeId;
	private InetAddress ip;
	private InetAddress controlIp;
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

	public InetAddress getControlIp() {
		return controlIp;
	}

	public void setControlIp(InetAddress controlIp) {
		this.controlIp = controlIp;
	}

	public int getOwnPort() {
		return ownPort;
	}

	public void setOwnPort(int ownPort) {
		this.ownPort = ownPort;
	}
	
	public WorkerNodeDescription(InetAddress ip, InetAddress controlIp, int ownPort){
		this.nodeId = generateNodeIdFromIp(ip);
		this.ip = ip;
		this.controlIp = controlIp;
		this.ownPort = ownPort;
	}
	
	private int generateNodeIdFromIp(InetAddress ip){
		return UUID.randomUUID().hashCode();
	}
}
