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
package uk.ac.imperial.lsds.seep.operator.compose.subquery;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import uk.ac.imperial.lsds.seep.operator.compose.multi.ISubQueryBufferHandler;
import uk.ac.imperial.lsds.seep.operator.compose.multi.IUpstreamSubQueryBufferHandler;
import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOperator;

public interface ISubQueryConnectable extends Serializable {

	public SubQuery getSubQuery();
	
	public void setParentMultiOperator(MultiOperator parent); 
	public MultiOperator getParentMultiOperator();
	
	public boolean isMostLocalDownstream();
	public boolean isMostLocalUpstream();
	public void connectTo(ISubQueryConnectable so, int streamID);

	public Map<Integer, ISubQueryConnectable> getLocalDownstream();
	public Map<Integer, ISubQueryConnectable> getLocalUpstream();
	
	public Set<ISubQueryBufferHandler> getLocalDownstreamBufferHandlers();
	public Set<IUpstreamSubQueryBufferHandler> getLocalUpstreamBufferHandlers();
	
	public void addLocalUpstreamBufferHandler(IUpstreamSubQueryBufferHandler handler);
	public void addLocalDownstreamBufferHandler(ISubQueryBufferHandler handler);

	public void addLocalUpstream(ISubQueryConnectable so, int streamID);
	public void addLocalDownstream(ISubQueryConnectable so, int streamID);
	
}
