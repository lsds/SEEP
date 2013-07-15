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
package uk.co.imperial.lsds.seep.comm;

import java.io.Serializable;

import uk.co.imperial.lsds.seep.P;

/// This class ticks the dispatcher every maxLatencyAllowed to update the clocks of the downstream channels

@SuppressWarnings("serial")
public class DispatcherWorker implements Serializable, Runnable{

	private Dispatcher dispatcher;
	
	public DispatcherWorker(Dispatcher dispatcher){
		this.dispatcher = dispatcher;
	}
	
	@Override
	public void run() {
		try{
			int value = Integer.parseInt(P.valueFor("maxLatencyAllowed"));
			Thread.sleep(value);
			dispatcher.batchTimeOut();
		}
		catch(InterruptedException ie){
			
			ie.printStackTrace();
		}
	}
}
