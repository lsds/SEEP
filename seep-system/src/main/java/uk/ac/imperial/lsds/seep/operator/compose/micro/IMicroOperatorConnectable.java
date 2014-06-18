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

import java.io.Serializable;
import java.util.Map;

import uk.ac.imperial.lsds.seep.operator.compose.subquery.SubQuery;

public interface IMicroOperatorConnectable extends Serializable {

	public IMicroOperatorCode getMicroOperator();
	
	public void setParentSubQuery(SubQuery parent); 
	public SubQuery getParentSubQuery();
	
	public boolean isMostLocalDownstream();
	public boolean isMostLocalUpstream();
	public void connectTo(int localStreamId, IMicroOperatorConnectable so);

	public Map<Integer, IMicroOperatorConnectable > getLocalDownstream();
	public Map<Integer, IMicroOperatorConnectable > getLocalUpstream();
	
}
