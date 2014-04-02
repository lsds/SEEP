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

public class Vertex<T extends SDGElement> {

	private final int vertexId;
	private List<Edge<T>> downstreamVertex;
	private List<Edge<T>> upstreamVertex;
	
	private T payload;
	
	Vertex(int vertexId){
		this.vertexId = vertexId;
	}
	
	Vertex(int vertexId, T payload){
		this.vertexId = vertexId;
		this.payload = payload;
		downstreamVertex = new LinkedList<Edge<T>>();
		upstreamVertex = new LinkedList<Edge<T>>();
	}
	
	public int getId(){
		return vertexId;
	}
	
	public T getPayload(){
		return payload;
	}
	
	public void setPayload(T payload){
		this.payload = payload;
	}
	
	public boolean addNewDownstream(Edge<T> downstream){
		return downstreamVertex.add(downstream);
	}
	
	public boolean addNewUpstream(Edge<T> upstream){
		return upstreamVertex.add(upstream);
	}
	
	public boolean equals(Edge<T> toCompare){
		return this.vertexId == toCompare.getVertexId();
	}
	
//	static class Edge<T>{
//		private uk.ac.imperial.lsds.java2sdg.bricks.Edge data = new uk.ac.imperial.lsds.java2sdg.bricks.Edge();
//
//		public Edge(Vertex<T> vertex, ConnectionType connType){
//			this.data.vertex = vertex;
//			this.data.connType = connType;
//		}
//	}
}
