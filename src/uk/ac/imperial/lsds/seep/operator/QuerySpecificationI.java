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


public interface QuerySpecificationI {

	public enum InputDataIngestionMode{
		ONE_AT_A_TIME, WINDOW, ORDERED, UPSTREAM_SYNC_BARRIER
	}
	
	public int getOperatorId();

	OperatorContext getOpContext();
	
	public void setOpContext(OperatorContext opContext);

	public void connectTo(QuerySpecificationI down, boolean originalQuery);
	
	public void connectTo(QuerySpecificationI down, InputDataIngestionMode mode, boolean originalQuery);

}
