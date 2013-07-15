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

import java.util.ArrayList;

public class Payload extends ArrayList<Object>{

	private static final long serialVersionUID = 1L;

	public Payload(){
		
	}

	public Payload(Object... attrValues){
		super(attrValues.length);
		for(Object o : attrValues){
			add(o);
		}
	}
	
	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("VAL ");
		for(Object o : this){
			sb.append(o+" ");
		}
		return sb.toString();
	}
}
