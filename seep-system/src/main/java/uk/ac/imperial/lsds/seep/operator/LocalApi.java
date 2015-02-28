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

import java.io.Serializable;

import javax.sound.sampled.UnsupportedAudioFileException;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.compose.SubOperator;

public class LocalApi implements API, CommunicationPrimitives, Serializable{

	private static final long serialVersionUID = 1L;
	private static LocalApi instance = new LocalApi();
	private SubOperator so;
	
	@Override
	public void setCallbackObject(Callback c) {
		this.so = (SubOperator)c;
	}
	
	public static LocalApi getInstance(){
		return instance;
	}
	
	@Override
	public void send(DataTuple dt) {
		so.send(dt);
	}

	@Override
	public void send_all(DataTuple dt) {
		so.send_all(dt);
	}

	@Override
	public void send_splitKey(DataTuple dt, int key) {
		so.send_splitKey(dt, key);
	}

	@Override
	public void send_toIndex(DataTuple dt, int idx) {
		so.send_toIndex(dt, idx);
	}

	@Override
	public void send_toStreamId(DataTuple dt, int streamId) {
		so.send_toStreamId(dt, streamId);
	}

	@Override
	public void send_toStreamId_splitKey(DataTuple dt, int streamId, int key) {
		so.send_toStreamId_splitKey(dt, streamId, key);	
	}

	@Override
	public void send_toStreamId_toAll(DataTuple dt, int streamId) {
		so.send_toStreamId_toAll(dt, streamId);
	}

        @Override
        public void send_toStreamId_toAll_threadPool(DataTuple dt, int streamId) {
                so.send_toStreamId_toAll_threadPool(dt, streamId);
        }

        @Override
        public void send_all_threadPool(DataTuple dt) {
                so.send_all_threadPool(dt);
        }

        @Override
        public void send_to_OpId(DataTuple dt, int opId) {
            
        }

        @Override
        public void send_to_OpIds(DataTuple[] dt, int[] opId) {
            
        }

        @Override
        public void send_toIndices(DataTuple[] dts, int[] indices) {
            
        }

		@Override
		public void send_lowestCost(DataTuple dt) {
			throw new UnsupportedOperationException("TODO");			
		}
		

		@Override
		public synchronized void send_highestWeight(DataTuple dt)
		{
			throw new UnsupportedOperationException("TODO");
		}
        
		@Override
		public synchronized void ack(DataTuple dt)
		{
			throw new UnsupportedOperationException("TODO");
		}
        
}
