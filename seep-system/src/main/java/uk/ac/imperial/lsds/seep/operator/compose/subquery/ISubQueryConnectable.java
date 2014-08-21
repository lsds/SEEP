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

import java.util.Map;

import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOpTuple;
import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOperator;
import uk.ac.imperial.lsds.seep.operator.compose.multi.SubQueryBuffer;

public interface ISubQueryConnectable {

	public SubQuery getSubQuery();
	
	public void setParentMultiOperator(MultiOperator parent); 
	public MultiOperator getParentMultiOperator();
	
	public boolean isMostLocalDownstream();
	public boolean isMostLocalUpstream();
	public void connectTo(ISubQueryConnectable so, int streamID);

	public Map<Integer, ISubQueryConnectable> getLocalDownstream();
	public Map<Integer, ISubQueryConnectable> getLocalUpstream();

	public Map<Integer, SubQueryBuffer> getLocalDownstreamBuffers();
	public Map<Integer, SubQueryBuffer> getLocalUpstreamBuffers();

	public void addLocalUpstream(ISubQueryConnectable so, int streamID);
	public void addLocalDownstream(ISubQueryConnectable so, int streamID);

	public void registerLocalUpstreamBuffer(SubQueryBuffer so, int streamID);
	public void registerLocalDownstreamBuffer(SubQueryBuffer so, int streamID);
	
	public void processData(MultiOpTuple tuple);

}
