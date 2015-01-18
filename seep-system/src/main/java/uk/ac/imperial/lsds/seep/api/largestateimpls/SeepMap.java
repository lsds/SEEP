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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.state.EmptyStateException;
import uk.ac.imperial.lsds.seep.state.LargeState;
import uk.ac.imperial.lsds.seep.state.MalformedStateChunk;
import uk.ac.imperial.lsds.seep.state.NullChunkWhileMerging;
import uk.ac.imperial.lsds.seep.state.Streamable;
import uk.ac.imperial.lsds.seep.state.Versionable;
import uk.ac.imperial.lsds.seep.state.annotations.GlobalStateAccess;
import uk.ac.imperial.lsds.seep.state.annotations.OperatorState;
import uk.ac.imperial.lsds.seep.state.annotations.PartitionStateAccess;
import uk.ac.imperial.lsds.seep.state.annotations.PartitioningKey;
import uk.ac.imperial.lsds.seep.state.annotations.ReadAccess;
import uk.ac.imperial.lsds.seep.state.annotations.WriteAccess;

/**
 * SeepMap is an implementation of a standard java HashMap. It supports multi-versioning to enable lock-free operations and it
 * implements Streamable so that the system can handle it even when it grow large.
 * @author raulcf
 *
 * @param <K>
 * @param <V>
 */
@OperatorState(partitionable=true)
public class SeepMap<K, V> extends HashMap<Object, Object> implements Versionable, Streamable, LargeState{
	
	final Logger LOG = LoggerFactory.getLogger(SeepMap.class);

	private static final long serialVersionUID = 1L;
	// Keep updates and deletes in different structures.
	private HashMap<Object, Object> dirtyUpdates = new HashMap<Object, Object>();
	private HashMap<Object, Object> dirtyRemoves = new HashMap<Object, Object>();
	private boolean clearInVersion = false;
	
	// Flag to indicate the structure is currently on snapshot mode
	private AtomicBoolean snapshotMode = new AtomicBoolean();
	// Mutex lock to do while reconciliating
	private Semaphore mutex = new Semaphore(1);
	
	// For internal use only
	private Iterator<Object> iterator = null;
	
	public SeepMap(){
		super();
	}

	public SeepMap(int initialSize) {
		super(initialSize);
	}
	
	@GlobalStateAccess
	@WriteAccess
	public void clear(){
		this.lock();
		if(snapshotMode.get()){
			// Just reset all dirty structures and flag it so that snapshot is cleared out when reconciling
			dirtyUpdates.clear();
			dirtyRemoves.clear();
			clearInVersion = true;
		}
		// Fallback to snapshot
		super.clear();
		this.release();
	}
	
	@PartitionStateAccess(partitioningKeyPositionInArguments=0)
	@ReadAccess
	public boolean containsKey(@PartitioningKey Object key){
		this.lock();
		if(snapshotMode.get()){
			// If dirtyUpdates has the key, the it is true
			if(dirtyUpdates.containsKey(key)){
				this.release();
				return true;
			}
			// If it does not have the key, and this is in removes, then it is false
			else if(clearInVersion || dirtyRemoves.containsKey(key)){
				this.release();
				return false;
			}
		}
		// Fallback to snapshot
		boolean containsKey = super.containsKey(key);
		this.release();
		return containsKey;
	}
	
	@GlobalStateAccess
	@ReadAccess
	public boolean containsValue(Object value){
		this.lock();
		if(snapshotMode.get()){
			// Check if dirtyUpdates has the value
			if(dirtyUpdates.containsValue(value)){
				this.release();
				return true;
			}
			// if the value has been removed, then the answer is no
			else if(clearInVersion || dirtyRemoves.containsValue(value)){
				this.release();
				return false;
			}
		}
		// Otherwise, we fall back to the snapshot, in a read-only operation
		boolean containsValue = super.containsValue(value);
		this.release();
		return containsValue;
	}
	
	@GlobalStateAccess
	@ReadAccess
	public boolean isEmpty(){
		this.lock();
		boolean isEmpty;
		// Empty only if there are no updates in dirty state AND the snapshot was asked to be cleared
		if(snapshotMode.get()){
			isEmpty = (dirtyUpdates.isEmpty() && clearInVersion) ? true : false;
			this.release();
			return isEmpty;
		}
		isEmpty = super.isEmpty();
		this.release();
		return isEmpty;
	}
	
	@PartitionStateAccess(partitioningKeyPositionInArguments=0)
	@WriteAccess
	public Object remove(@PartitioningKey Object key){
		this.lock();
		Object oldValue;
		if(snapshotMode.get()){
			// Remove entry from dirtyUpdates
			oldValue = dirtyUpdates.remove(key);
			// Register the deletion in dirtyRemoves. Attach key and value
			dirtyRemoves.put(key, oldValue);
			this.release();
			return oldValue;
		}
		oldValue = super.remove(key);
		this.release();
		return oldValue;
	}
	
	@GlobalStateAccess
	@ReadAccess
	public Set<Map.Entry<Object,Object>> entrySet(){
		this.lock();
		if(snapshotMode.get()){
			LOG.warn("NOT IMPLEMENTED");
			System.exit(0);
		}
		Set<Map.Entry<Object,Object>> toReturn = super.entrySet();
		this.release();
		return toReturn;
	}
	
	@GlobalStateAccess
	@ReadAccess
	public Set<Object> keySet(){
		this.lock();
		if(snapshotMode.get()){
			LOG.warn("NOT IMPLEMENTED");
			System.exit(0);
		}
		Set<Object> toReturn = super.keySet();
		this.release();
		return toReturn;
	}
	
	@GlobalStateAccess
	@ReadAccess
	public Collection<Object> values(){
		this.lock();
		if(snapshotMode.get()){
			LOG.warn("NOT IMPLEMENTED");
			System.exit(0);
		}
		Collection<Object> toReturn = super.values();
		this.release();
		return toReturn;
	}
	
	@GlobalStateAccess
	@ReadAccess
	public int size(){
		return super.size();
	}
	
	@PartitionStateAccess(partitioningKeyPositionInArguments=0)
	@ReadAccess
	public Object get(@PartitioningKey Object key){
		this.lock();
		if(snapshotMode.get()){
			// Return from recent updates
			if(dirtyUpdates.containsKey(key)){
				Object toReturn = dirtyUpdates.get(key);
				this.release();
				return toReturn;
			}
			// It's been specifically removed
			else if(clearInVersion || dirtyRemoves.containsKey(key)){
				this.release();
				return null;
			}
		}
		Object toReturn = super.get(key);
		this.release();
		return toReturn;
	}
	
	@PartitionStateAccess(partitioningKeyPositionInArguments=0)
	@WriteAccess
	public Object put(@PartitioningKey Object key, Object value){
		this.lock();
		if(snapshotMode.get()){
			// Update value
			Object toReturn = dirtyUpdates.put(key, value);
			// Remove from dirtyRemoves to avoid inconsistencies when reconciling
			dirtyRemoves.remove(key);
			this.release();
			return toReturn;
		}
		// or fall back to the snapshot
		Object toReturn = super.put(key, value);
		this.release();
		return toReturn;
	}

	/** Implement Streamable interface **/
	
	@Override
	public int getSize() {
		LOG.warn("CALLING getSize of SeepMap, this method is not implemented...");
		return this.size();
	}

	/** Return the total number of chunks necessary to stream this snapshot given a @param chunkSize 
	 * @throws EmptyStateException **/
	@Override
	public int getTotalNumberOfChunks(int chunkSize) throws EmptyStateException {
		if(chunkSize == 0){
			throw new IllegalArgumentException("chunkSize must be > 0");
		}
		if(this.size() == 0){
			throw new EmptyStateException("State size is 0, this method should not be called in that state");
		}
		double chunks = (double)this.size()/chunkSize;
		System.out.println("$$$$ CHUNKS: "+chunks);
		int totalChunks = (int) Math.ceil((double)this.size()/chunkSize);
		System.out.println("MAP SIZE: "+this.size()+" so total chunks: "+totalChunks);
		return totalChunks;
	}
	
	@Override
	public Iterator<?> getIterator() {
		iterator = super.keySet().iterator();
		return iterator;
	}
	
	@Override
	public ArrayList<Object> streamSplitState(int chunkSize) {
		ArrayList<Object> chunk = new ArrayList<Object>();
		int sizeCounter = 0;
		while(iterator.hasNext()){
			String key = (String) iterator.next();
			chunk.add(key);
			chunk.add(this.getFromBackup(key));
			sizeCounter++; // new unit added
			if(sizeCounter >= chunkSize){
				return chunk;
			}
		}
		return null;
	}
	
	@Override
	public void reset(){
		this.clear();
		dirtyUpdates.clear();
		dirtyRemoves.clear();
		clearInVersion = false;
	}
	
	@Override
	public synchronized void appendChunk(ArrayList<Object> chunk) throws NullChunkWhileMerging, MalformedStateChunk {
		if(chunk == null){
			throw new NullChunkWhileMerging("Received a null chunk");
		}
		int chunkSize = chunk.size();
		if(chunkSize % 2 != 0 || chunkSize == 0){
			throw new MalformedStateChunk("Does not contain an even number of object or size is 0. Size->"+chunkSize);
		}
		System.out.println("Appending: "+chunk.size());
		for(int i = 0; i < chunk.size(); i++){
			Object key = chunk.get(i);
			i++;
			Object value = chunk.get(i);
			synchronized(this){
				this.put(key, value);
			}
		}
	}
	
	@Override
	public Object getFromBackup(Object key){
		return super.get(key);
	}
	
	/**
	 * Methods implementing the Versionable interface
	 */
	
	/** Flag this structure as Snapshot, so that new updates and reads happen in a new version **/
	@Override
	public void setSnapshotMode(boolean newValue){
		this.snapshotMode.set(newValue);
	}
	
	/** Reconcile changes kept in version with the original snapshot. **/
	@Override
	public synchronized void reconcile(){
		System.out.println("OR: "+super.size()+" DIRTY: "+dirtyUpdates.size());
		this.lock();
		// Either we need to delete everything and only add what is stored in dirtyUpdates
		if(clearInVersion){
			super.clear();
		}
		// Or we just need to remove certain entries
		else{
			for(Map.Entry<Object, Object> entry : dirtyRemoves.entrySet()){
				super.remove(entry.getKey());
			}
		}
		// In any case, we then add whatever is stored in dirtyUpdates
		for(Map.Entry<Object, Object> entry : dirtyUpdates.entrySet()){
			super.put(entry.getKey(), entry.getValue());
		}
		//We reset the structures used during the versioning
		clearInVersion = false;
		dirtyRemoves.clear();
		dirtyUpdates.clear();
		// We get out of snapshotMode
		snapshotMode.set(false);
		this.release();
	}
	
	/** Request mutual exclusion access to the structure **/
	@Override
	public void lock(){
		try {
			this.mutex.acquire();
		} 
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/** Release mutual exclusion access to the structure **/
	@Override
	public void release(){
		this.mutex.release();
	}

	@Override
	public Object getVersionableAndStreamableState() {
		return this;
	}
}