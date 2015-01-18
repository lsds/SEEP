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
package uk.ac.imperial.lsds.seep.api.largestateimpls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import uk.ac.imperial.lsds.seep.state.LargeState;
import uk.ac.imperial.lsds.seep.state.Streamable;
import uk.ac.imperial.lsds.seep.state.Versionable;

public class SeepMatrix extends Matrix implements Versionable, Streamable, LargeState{

	private static final long serialVersionUID = 1L;
	
	private AtomicBoolean dirtyMode = new AtomicBoolean();
	private Semaphore mutex = new Semaphore(1);
	private Iterator<Integer> iterator = null;
	
	// We keep here the original index and the row. Original index to reduce the reconciliation time
	private HashMap<Integer, ArrayList<Component>> dirtyState = new HashMap<Integer, ArrayList<Component>>();
	//private ArrayList<ArrayList<Component>> dirtyState_newRows = new ArrayList<ArrayList<Component>>();
	private HashMap<Integer, ArrayList<Component>> dirtyState_newRows = new HashMap<Integer, ArrayList<Component>>();
	// here we store idx - tag, instead of tag-idx, so that we can do the reverse mapping when reconciliating
	private HashMap<Integer, Integer> dirtyState_IDX_TAG = new HashMap<Integer, Integer>();
	// and also keep this, so that rows are correctly read while in dirty state
	private HashMap<Integer, Integer> dirtyState_TAG_IDX = new HashMap<Integer, Integer>();
	
	public SeepMatrix(){
		super();
	}
	
	@Override
	public void lock(){
		try {
			this.mutex.acquire();
		} 
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void release(){
		this.mutex.release();
	}
	
	// WRITE
	public synchronized void updateMatrixByReplacingValue(int rowTag, int col, int value){
		try {
			this.mutex.acquire();
		}
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		if(dirtyMode.get()){
//			System.out.println("AA1");

			if(rowIds.get(rowTag) != null){
				// If it exists, then update val in col
//System.out.println("a1");
				ArrayList<Component> row = super.getRowVectorWithTag(rowTag);
//System.out.println("a2");
				//int rowIdx = rows.indexOf(row); // Looping through size
if(row == null){
	return;
}
				int rowIdx = rowIds.get(rowTag);
				// We go to the specific col to update
				for(int i = 0; i< row.size(); i++){
					// If the current col is equal or greater than col to update, insert col before this one
					int currentElCol = row.get(i).col;
					if(currentElCol > col){
						int insertIdx = (i-1) < 0 ? 0 : i-1;
						row.add(insertIdx, new Component(col, value));
						break;
					}
					else if(currentElCol == col){
						row.set(i, new Component(col, value));
						break;
					}
					// else if the current col is lesser than col, and is the last element...
					else if(currentElCol < col && row.size() == i+1){
						// Insert at the end
						row.add(new Component(col, value));
						break;
					}
				}
				// Finally leave the updated row in the dirty state
				dirtyState.put(rowIdx, row);
				
//				// Finally insert the updated row
//				rows.set(rowIdx, row);
			}
			else{
				// Non existent row, create a new one with the given value
				ArrayList<Component> c = new ArrayList<Component>();
				c.add(new Component(col, value));
				// Add to rows and update rowIds
				
				dirtyState_newRows.put(rowTag, c);
				
				// We add the new row to our artificial matrix
//				dirtyState_newRows.add(c);
//				// fuck the gods...
//				dirtyState_TAG_IDX.put(rowTag, dirtyState_newRows.size());
//				dirtyState_IDX_TAG.put(dirtyState_newRows.size(), rowTag);
				
//				dirtyState_newRows(rowTag, c);
			}
		}
		else{
//			System.out.println("AA2");
			mutex.release();
			super.updateMatrixByReplacingValue(rowTag, col, value);
		}
		mutex.release();
	}
	
	//READ
	public ArrayList<Component> getRowVectorWithTag(int rowTag) {
		ArrayList<Component> toReturn = null;
		try {
			this.mutex.acquire();
		} 
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(dirtyMode.get()){
			if(dirtyState_TAG_IDX.get(rowTag) == null){
				// if not in dirty, we check in the original one
				toReturn = super.getRowVectorWithTag(rowTag);
				mutex.release();
				return toReturn;
			}
			int rowIndex = dirtyState_TAG_IDX.get(rowTag);
			toReturn = dirtyState_newRows.get(rowIndex-1);
			mutex.release();
			return toReturn;
		}
		else{
			toReturn = super.getRowVectorWithTag(rowTag);
			mutex.release();
			return toReturn;
		}
	}
	
	/** IFACE methods **/
	
	public void setSnapshotMode(boolean newValue){
		this.dirtyMode.set(newValue);
	}
	
	public synchronized void reconcile(){
		try {
			this.mutex.acquire();
		} 
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//First we update the previously already existent
		for(Map.Entry<Integer, ArrayList<Component>> entry : dirtyState.entrySet()){
			int idx = entry.getKey();
			rows.set(idx, dirtyState.get(entry.getValue()));
		}
		
		// and now we add the new stuff
		for(int i = 0; i<dirtyState_newRows.size(); i++){
			
			for(Integer rowId : dirtyState_newRows.keySet()){
				ArrayList<Component> newRow = dirtyState_newRows.get(rowId);
				rows.add(newRow);
				rowSize++;
				int rowIdx = rows.size()-1;
				rowIds.put(rowId, rowIdx);
			}
			
//			ArrayList<Component> newRow = dirtyState_newRows.get(i);
//			rows.add(newRow);
//			rowSize++;
//			if(dirtyState_IDX_TAG == null){
//				System.out.println("dirtyState tag null");
//			}
//			else{
//				System.out.println("dirtyState_IDX_TAG.get(i) is null");
//			}
//			int rowTag = rows.size();
//			//int rowTag = dirtyState_IDX_TAG.get(i);
//			rowIds.put(rowTag, rows.size()-1);
		}
		
		dirtyState.clear();
		dirtyState_newRows.clear();
		dirtyState_IDX_TAG.clear();
		dirtyState_TAG_IDX.clear();
		dirtyMode.set(false);
		this.mutex.release();
	}
	
	int realIndexWhileAppendingChunks = 0;
	
	@Override
	public void appendChunk(ArrayList<Object> chunk) {
		if(chunk == null){
			System.out.println("RECREATED STATE SIZE: "+this.rows.size());
			this.rowSize = rows.size();
			realIndexWhileAppendingChunks = 0; // For next merging
			return;
		}
		System.out.println("Appending: "+chunk.size());
		for(int i = 0; i<chunk.size(); i++){
			int rowId = (Integer)chunk.get(i);
			i++;
			ArrayList<Component> row = (ArrayList<Component>)chunk.get(i);
			//this.rowIds.put(rowId, rowId);
			this.rowIds.put(rowId, realIndexWhileAppendingChunks);
			realIndexWhileAppendingChunks++;
			this.rows.add(row);
		}
	}

	@Override
	public Object getFromBackup(Object key) {
		int idx = this.rowIds.get(key);
		return this.rows.get(idx);
	}

	@Override
	public Iterator getIterator() {
		iterator = this.rowIds.keySet().iterator();
		return iterator;
	}

	@Override
	public int getSize() {
		return rowSize;
	}

	@Override
	public int getTotalNumberOfChunks(int chunkSize) {
		double chunks = (double)this.rows.size()/chunkSize;
		System.out.println("$$$$ CHUNKS: "+chunks);
		int totalChunks = (int) Math.ceil((double)this.size()/chunkSize);
		System.out.println("MAP SIZE: "+this.size()+" so total chunks: "+totalChunks);
		return totalChunks;
	}

	@Override
	public void reset() {
		this.dirtyState.clear();
		this.dirtyState_IDX_TAG.clear();
		this.dirtyState_newRows.clear();
		this.dirtyState_TAG_IDX.clear();
		this.rowIds.clear();
		this.rows.clear();
		this.rowSize = 0;
	}

	@Override
	public ArrayList<Object> streamSplitState(int chunkSize) {
		ArrayList<Object> chunk = new ArrayList<Object>();
		int sizeCounter = 0;
		while(iterator.hasNext()){
			int rowId = iterator.next();
			chunk.add(rowId); // add tag
			Object row = this.getFromBackup(rowId);
			chunk.add(row);
			sizeCounter++;
			if(sizeCounter >= chunkSize){
				return chunk;
			}
		}
		return null;
	}

	@Override
	public Object getVersionableAndStreamableState() {
		return this;
	}
}
