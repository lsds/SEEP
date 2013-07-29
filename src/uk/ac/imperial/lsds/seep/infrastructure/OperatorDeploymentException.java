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
package uk.ac.imperial.lsds.seep.infrastructure;

/**
* DeploymentException. This class models an exception ocurred during deployment phase
*/

/// \todo {this one is never used}
public class OperatorDeploymentException extends Exception{
	
	private static final long serialVersionUID = 1L;

	public OperatorDeploymentException(String msg){
		super(msg);
	}
}
