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
