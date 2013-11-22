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
package uk.ac.imperial.lsds.seep.reliable;

import java.io.Serializable;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.processingunit.IProcessingUnit;
import uk.ac.imperial.lsds.seep.runtimeengine.TimestampTracker;

/**
* ACKWorker. This runnable object is in charge of watching to the last processed tuple and generating an ACK when this has changed.
*/

public class ACKWorker implements Runnable, Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private IProcessingUnit processingUnit = null;
	private boolean goOn = true;

	public void stopACKWorker(){
		this.goOn = false;
	}
	
	public ACKWorker(IProcessingUnit processingUnit){
		this.processingUnit = processingUnit;
	}
	
	public void run(){
		int sleep = new Integer(GLOBALS.valueFor("ackEmitInterval"));
		while(goOn){
			TimestampTracker currentTsV = processingUnit.getLastACK();
			System.out.println("ACKWorker: EmitACK");
			processingUnit.emitACK(currentTsV);
			try{
				Thread.sleep(sleep);
			}
			catch(InterruptedException ie){
				System.out.println("ACKWorker: while trying to sleep "+ie.getMessage());
				ie.printStackTrace();
			}
		}
	}
}
