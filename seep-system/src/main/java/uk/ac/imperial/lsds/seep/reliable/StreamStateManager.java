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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.state.EmptyStateException;
import uk.ac.imperial.lsds.seep.state.Streamable;

public class StreamStateManager {
	
	final private Logger LOG = LoggerFactory.getLogger(StreamStateManager.class);

	private final int chunkSize;
	
	private Streamable state;
	private int totalNumberChunks;
	private Iterator<?> stateIterator;
	
	public StreamStateManager(Streamable state){
		this.state = state;
		this.chunkSize = new Integer(GLOBALS.valueFor("stateChunkSize"));
		try {
			this.totalNumberChunks = state.getTotalNumberOfChunks(chunkSize);
		} 
		catch (EmptyStateException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(totalNumberChunks == 1){
			LOG.error("-> State fits in less than one chunk. Fix");
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
