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
package uk.ac.imperial.lsds.seep.buffer;

import uk.ac.imperial.lsds.seep.comm.serialization.messages.BatchTuplePayload;

public class OutputBuffer {

	public BatchTuplePayload batch;
	public int channelOpId; // the opId in the other side
	
	public OutputBuffer(){}
	
	public OutputBuffer(BatchTuplePayload batch, int channelOpId){
		this.batch = batch;
		this.channelOpId = channelOpId;
	}
}
