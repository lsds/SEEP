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
package uk.ac.imperial.lsds.seep.state;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Streamable interface has methods to split and merge LargeState, and to retrieve size, numbers of chunks and other information
 * required by the system
 * @author raulcf
 *
 */
public interface Streamable {

	public int getSize();
	public int getTotalNumberOfChunks(int chunkSize) throws EmptyStateException;
	public Iterator<?> getIterator();
	public ArrayList<Object> streamSplitState(int chunkSize);
	public void reset();
	public void appendChunk(ArrayList<Object> s) throws NullChunkWhileMerging, MalformedStateChunk;
	public Object getFromBackup(Object key);
	
}
