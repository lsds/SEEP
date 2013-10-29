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

import uk.ac.imperial.lsds.seep.state.Streamable;
import uk.ac.imperial.lsds.seep.state.Versionable;

public class SeepMap<K, V> extends HashMap<Object, Object> implements Versionable, Streamable{

	private static final long serialVersionUID = 1L;
	private HashMap<Object, Object> dirtyState = new HashMap<Object, Object>();
	
	private AtomicBoolean dirtyMode = new AtomicBoolean();
	private Semaphore mutex = new Semaphore(1);
	
	private Iterator<Object> iterator = null;
	
	public SeepMap(){
		super();
	}
	
	public SeepMap(int initialSize) {
		super(initialSize);
	}
	
	@Override
	public void lockStateAccess(){
		try {
			this.mutex.acquire();
		} 
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void releaseStateAccess(){
		this.mutex.release();
	}
	
	public Object put(Object key, Object value){
		try {
			this.mutex.acquire();
		} 
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(dirtyMode.get()){
			Object toReturn = dirtyState.put(key, value);
			this.mutex.release();
			return toReturn;
		}
		else{
			Object toReturn = super.put(key, value);
			this.mutex.release();
			return toReturn;
		}
	}
	
	public Object get(Object key){
		try {
			this.mutex.acquire();
		} 
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(dirtyMode.get() && dirtyState.containsKey(key)){
			Object toReturn = dirtyState.get(key);
			this.mutex.release();
			return toReturn;
		}
		else{
			Object toReturn = super.get(key);
			this.mutex.release();
			return toReturn;
		}
	}
	
	/** Implement Versionable interface **/
	
	public void setDirtyMode(boolean newValue){
		this.dirtyMode.set(newValue);
	}
	
	public synchronized void reconcile(){
		System.out.println("OR: "+super.size()+" DIRTY: "+dirtyState.size());
		try {
			this.mutex.acquire();
		} 
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(Map.Entry<Object, Object> entry : dirtyState.entrySet()){
			super.put(entry.getKey(), entry.getValue());
		}
		dirtyState.clear();
		dirtyMode.set(false);
		this.mutex.release();
	}

	/** Implement Streamable interface **/
	
	@Override
	public int getSize() {
		return this.size();
	}

	@Override
	public int getTotalNumberOfChunks(int chunkSize) {
		double chunks = (double)this.size()/chunkSize;
		System.out.println("$$$$ CHUNKS: "+chunks);
		int totalChunks = (int) Math.ceil((double)this.size()/chunkSize);
		System.out.println("MAP SIZE: "+this.size()+" so total chunks: "+totalChunks);
		return totalChunks;
	}
	
	@Override
	public Iterator getIterator() {
		iterator = this.keySet().iterator();
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
		dirtyState.clear();
	}
	
	@Override
	public synchronized void appendChunk(ArrayList<Object> chunk) {
		if(chunk == null){
			System.out.println("RECREATED STATE SIZE: "+this.size());
			return;
		}
		System.out.println("Appending: "+chunk.size());
		for(int i = 0; i<chunk.size(); i++){
			String key = (String)chunk.get(i);
			i++;
			Integer value = (Integer)chunk.get(i);
			synchronized(this){
				this.put(key, value);
			}
		}
	}
	
	public Object getFromBackup(Object key){
		return super.get(key);
	}
}
