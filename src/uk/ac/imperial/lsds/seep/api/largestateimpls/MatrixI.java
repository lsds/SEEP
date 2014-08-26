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


public interface MatrixI {

	// Splits a matrix evenly by rows
	public Matrix[] splitByRows(Matrix m);
	
	// Splits a matrix evely by columns
	public Matrix[] splitByCol(Matrix m);
	
	// Returns an integer vector given the row index
	public ArrayList<Component> getRowVectorWithTag(int rowTag);
	
	// Computes the coOccurrence matrix (AAt) of a given matrix
	public ArrayList<ArrayList<Integer>> coOccurrence(ArrayList<ArrayList<Integer>> m);
	
//	// Multipy a vector by a matrix and returns the result vector
//	public static ArrayList<Integer> multipyVectorByDiagonalMatrix(ArrayList<Integer> vector, ArrayList<ArrayList<Integer>> m);
	
	public ArrayList<ArrayList<Integer>> getExpandedRepr();
	
}
