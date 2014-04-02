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
package uk.ac.imperial.lsds.java2sdg.bricks.sdg;

import java.util.LinkedList;
import java.util.List;

import uk.ac.imperial.lsds.java2sdg.bricks.SDGElement;

public class SDG<T extends SDGElement> {
	
	private List<Vertex<T>> nodes;
	private int vertexId = 0;
	private Vertex<T> heading;
	
	public SDG(){
		nodes = new LinkedList<Vertex<T>>();
	}
	
	public Vertex<T> addNewVertex(T payload){
		Vertex<T> newVertex = new Vertex<T>(vertexId, payload);
		vertexId++;
		return newVertex;
	}
	
	public Vertex<T> addNewEmptyVertex(){
		Vertex<T> newVertex = new Vertex<T>(vertexId);
		vertexId++;
		return newVertex;
	}
	
	public boolean connectVertex(Vertex<T> origin, Vertex<T> destiny, ConnectionType connType){
		if(origin.equals(destiny)){
			throw new IllegalArgumentException("Origin and Destiny node cannot be the same node");
		}
		if(origin == null || destiny == null){
			throw new IllegalArgumentException("Origin and Destiny cannot be null");
		}
		
		Edge<T> downEdge = new Edge<T>(destiny, connType);
		boolean success = origin.addNewDownstream(downEdge);
		Edge<T> upEdge = new Edge<T>(origin, ConnectionType.UPSTREAM);
		boolean success2 = destiny.addNewUpstream(upEdge);
		return success && success2;
	}
	
	public int getNumberOfOperators(){
		return nodes.size();
	}
	
	public boolean isCyclicGraph(){
		// TODO: implement
		return false;
	}
	
}
