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
package uk.ac.imperial.lsds.seep.infrastructure.api.datastructure;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class SeepMap<K, V> extends HashMap<Object, Object> {

	private static final long serialVersionUID = 1L;
	private HashMap<Object, Object> dirtyState = new HashMap<Object, Object>();
	
	private AtomicBoolean dirtyMode = new AtomicBoolean();
	private Semaphore mutex = new Semaphore(1);
	
	public SeepMap(){
		super();
	}
	
	public SeepMap(int initialSize) {
		super(initialSize);
	}

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
	
	public Object getBackup(Object key){
		return super.get(key);
	}

	public void reset(){
		this.clear();
		dirtyState.clear();
	}
}
