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
package uk.co.imperial.lsds.seep.comm.serialization.messages;

import java.io.Serializable;

public class TuplePayload implements Serializable{
	
	public static final long serialVersionUID = 1L;
	public long timestamp;
	public int schemaId;
	public Payload attrValues;
	
	public TuplePayload(){
		
	}
	
	@Override
	public String toString(){
		return attrValues.toString();
	}
}
