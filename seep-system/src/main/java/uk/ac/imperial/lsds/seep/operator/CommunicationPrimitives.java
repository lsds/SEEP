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
package uk.ac.imperial.lsds.seep.operator;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;

public interface CommunicationPrimitives {

	public void send(DataTuple dt);
	public void send_lowestCost(DataTuple dt);
	public void send_highestWeight(DataTuple dt);
	public void send_toIndex(DataTuple dt, int idx);
	public void send_splitKey(DataTuple dt, int key);
	public void send_toStreamId_splitKey(DataTuple dt, int streamId, int key);
	public void send_toStreamId_toAll(DataTuple dt, int streamId);
	public void send_all(DataTuple dt);
	public void send_toStreamId(DataTuple dt, int streamId);
	public void send_toStreamId_toAll_threadPool(DataTuple dt, int streamId);
        public void send_all_threadPool(DataTuple dt);
        public void send_to_OpId(DataTuple dt, int opId);
        public void send_to_OpIds(DataTuple[] dt, int[] opId);
        public void send_toIndices(DataTuple[] dts, int[] indices);
		public void ack(DataTuple dt);
}
