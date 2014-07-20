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
package uk.ac.imperial.lsds.seep.operator.compose.micro;

import java.util.Map;

import uk.ac.imperial.lsds.seep.operator.compose.subquery.SubQuery;

public interface IMicroOperatorConnectable {

	public MicroOperator getMicroOperator();
	
	public void setParentSubQuery(SubQuery parent); 
	public SubQuery getParentSubQuery();
	
	public boolean isMostLocalDownstream();
	public boolean isMostLocalUpstream();
	public void connectTo(int streamID, IMicroOperatorConnectable so);

	public Map<Integer,IMicroOperatorConnectable> getLocalDownstream();
	public Map<Integer,IMicroOperatorConnectable> getLocalUpstream();

	public void addLocalUpstream(int streamID, IMicroOperatorConnectable so);
	public void addLocalDownstream(int streamID, IMicroOperatorConnectable so);
	
}
