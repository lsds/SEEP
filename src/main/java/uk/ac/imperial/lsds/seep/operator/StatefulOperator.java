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

import uk.ac.imperial.lsds.seep.operator.compose.SubOperatorCode;
import uk.ac.imperial.lsds.seep.state.StateWrapper;

public interface StatefulOperator extends OperatorCode, SubOperatorCode{

	// FIXME: State (state impl) instead of StateWrapper
	public StateWrapper getState();
	public void replaceState(StateWrapper state);

}
