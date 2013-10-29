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

public interface Streamable {

	public int getSize();
	public int getTotalNumberOfChunks(int chunkSize);
	public Iterator<?> getIterator();
	public ArrayList<Object> streamSplitState(int chunkSize);
	public void reset();
	public void appendChunk(ArrayList<Object> s);
	public Object getFromBackup(Object key);
	
}
