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
package uk.ac.imperial.lsds.seep.operator;
import uk.ac.imperial.lsds.seep.state.State;

/**
 * 
 * All stateful operators must implement the following three methods
 * @author raulcf
 *
 */

public interface StatefulOperator extends OperatorCode{

	/** 
	 * The system calls setState to pass a reference to 
	 * the state. A user should set this reference to the 
	 * state the op is using. 
	 * 
	 * In addition, on the event of a failure recovery or 
	 * scale out, the system will dynamically call this 
	 * method. The user can then implement custom logic to set 
	 * up the current reference to the new state in a 
	 * correct manner. 
	 **/
	public void setState(State state);
	/** This function should return the state the user has implemented in the operator **/
	public State getState();
}
