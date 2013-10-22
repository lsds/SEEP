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
package uk.ac.imperial.lsds.seep.runtimeengine;

import java.net.InetAddress;

import uk.ac.imperial.lsds.seep.comm.serialization.ControlTuple;

public class JobBean {

	public InetAddress ip;
	public ControlTuple msg;
	
	public JobBean(InetAddress ip, ControlTuple msg){
		this.ip = ip;
		this.msg = msg;
	}
	
}
