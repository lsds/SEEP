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
package uk.ac.imperial.lsds.seep.reliable;

import java.util.ArrayList;
import java.util.Iterator;

import uk.ac.imperial.lsds.seep.P;
import uk.ac.imperial.lsds.seep.infrastructure.NodeManager;
import uk.ac.imperial.lsds.seep.state.Streamable;

public class StreamStateManager {

	private final int chunkSize;
	
	private Streamable state;
	private int totalNumberChunks;
	private Iterator stateIterator;
	
	public StreamStateManager(Streamable state){
		this.state = state;
		this.chunkSize = new Integer(P.valueFor("stateChunkSize"));
		this.totalNumberChunks = state.getTotalNumberOfChunks(chunkSize);
		if(totalNumberChunks == 1){
			NodeManager.nLogger.severe("-> State fits in less than one chunk. Fix");
			try {
				throw new Exception("State fits in less than one chunk");
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		this.stateIterator = state.getIterator();
	}
	
	public int getTotalNumberChunks(){
		return totalNumberChunks;
	}
	
	public MemoryChunk getChunk(){
		MemoryChunk mc = null;
		if(stateIterator.hasNext()){
			ArrayList<Object> chunk = state.streamSplitState(chunkSize);
			if(chunk == null){
				mc = new MemoryChunk();
				mc.chunk = chunk;
				return mc;
			}
			mc = new MemoryChunk();
			mc.chunk = chunk;
		}
		return mc;
	}
}
