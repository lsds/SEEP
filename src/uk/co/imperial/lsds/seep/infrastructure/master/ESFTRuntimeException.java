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
package uk.co.imperial.lsds.seep.infrastructure.master;

/**
* ESFTRuntimeException. This class models an exception ocurred during runtime phase
*/

/// \todo {this one is never used}

public class ESFTRuntimeException extends Exception{

	private static final long serialVersionUID = 1L;

	public ESFTRuntimeException(String msg){
		super(msg);
	}

}
