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
package uk.ac.imperial.lsds.seep.comm.serialization.messages;

import java.io.Serializable;

public class TuplePayload implements Serializable{
	
	public static final long serialVersionUID = 1L;
	public long timestamp;
	public int schemaId;
	public Payload attrValues;
	public long instrumentation_ts;
	
	public TuplePayload(){
		
	}
	
	@Override
	public String toString(){
		if(attrValues.size() > 0){
			return attrValues.toString();
		}
		else{
			return "empty tuple";
		}
	}
}
