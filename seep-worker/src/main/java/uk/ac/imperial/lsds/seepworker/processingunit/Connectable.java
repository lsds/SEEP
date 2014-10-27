package uk.ac.imperial.lsds.seepworker.processingunit;
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

import uk.ac.imperial.lsds.seep.api.ConnectionType;
import uk.ac.imperial.lsds.seepworker.operator.OperatorContext;

public interface Connectable {

	public int getOperatorId();

	public OperatorContext getOpContext();
	
	public void setOpContext(OperatorContext opContext);
	
	public void connectTo(Connectable down, boolean originalQuery);
	public void connectTo(Connectable down, boolean originalQuery, int streamId);
	public void connectTo(Connectable down, ConnectionType mode, boolean originalQuery, int streamId);
	
}

