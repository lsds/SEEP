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
