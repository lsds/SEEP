package uk.ac.imperial.lsds.java2sdg.bricks.sdg;

import uk.ac.imperial.lsds.java2sdg.bricks.SDGElement;

public class Edge<T extends SDGElement> {
	public Vertex<T> vertex;
	public ConnectionType connType;

	public Edge(Vertex<T> vertex, ConnectionType connType) {
		this.vertex = vertex;
		this.connType = connType;
	}
	
	public int getVertexId(){
		return vertex.getId();
	}
}