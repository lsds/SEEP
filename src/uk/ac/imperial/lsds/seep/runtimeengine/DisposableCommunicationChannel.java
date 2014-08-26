/*******************************************************************************
 * Copyright (c) 2014 Imperial College London
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial API and implementation
 ******************************************************************************/
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
	
	@Override
	public String toString(){
		return "OP-ID: "+opId+" -> "+ip.toString();
	}
}
