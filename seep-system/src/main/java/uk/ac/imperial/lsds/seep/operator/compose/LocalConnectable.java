/*******************************************************************************
 * Copyright (c) 2014 Imperial College London
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial API and implementation
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.operator.compose;

import java.io.Serializable;
import java.util.Map;

public interface LocalConnectable extends Serializable {

	public MicroOperator getMicroOperator();
	
	public void setParentMultiOperator(MultiOperator parent); 
	public MultiOperator getParentMultiOperator();
	
	public boolean isMostLocalDownstream();
	public boolean isMostLocalUpstream();
	public void connectSubOperatorTo(int localStreamId, LocalOperator so);

	public Map<Integer, LocalOperator> getLocalDownstream();
	public Map<Integer, LocalOperator> getLocalUpstream();
	
}
