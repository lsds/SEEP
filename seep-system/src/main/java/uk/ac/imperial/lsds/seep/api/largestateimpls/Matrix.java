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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;


public class Matrix implements MatrixI, Serializable{

	
	private static final long serialVersionUID = 1L;
	private int colSize = 0;
	// map the row tag with a row index
	protected HashMap<Integer, Integer> rowIds = new HashMap<Integer, Integer>();
	// the real rows
	protected ArrayList<ArrayList<Component>> rows = new ArrayList<ArrayList<Component>>();
	protected int rowSize = 0;
	
	
	public int size(){
		return rowSize;
	}
	
	
	public ArrayList<ArrayList<Component>> getRows(){
		return rows;
	}
	
	/** BUILDER METHODS **/
	
	public Matrix(){
		this.colSize = 0;
	}
	
	public HashMap<Integer, Integer> getRowIds(){
		return rowIds;
	}
	
	public Matrix(ArrayList<ArrayList<Component>> rows, int cols){
		// Assign row ids
		for(int i = 0; i<rows.size(); i++){
			rowIds.put(i, i);
		}
		this.rows = rows;
		this.colSize = cols;
	}
	
	/** OPERATIONS ON ROWS **/
	
	@Override
	public ArrayList<Component> getRowVectorWithTag(int rowTag) {
		if(rowIds.get(rowTag) == null){
			return null;
		}
		int rowIndex = rowIds.get(rowTag);
		
//		///\fixme{this should be safe higher in the stack}
//		if(rows.get(rowIndex) == null){
//			return null;
//		}
		
		return rows.get(rowIndex);
	}
	
	public void setRow(ArrayList<Component> component, int rowTag){
		int idx;
		if(rowIds.get(rowTag) != null){
			// Update
			idx = rowIds.get(rowTag);
			rows.set(idx, component);
		}
		else{
			// new
			rows.add(component);
			rowSize++;
			// Update the rowIds with the new id for this row
			rowIds.put(rowTag, rows.size()-1);
		}
		
		// Update cols
		if (colSize < component.get(component.size()-1).col + 1){
			colSize = component.get(component.size()-1).col + 1;
		}
	}
	
	public synchronized void updateMatrixByIncreasingValue(int rowTag, int col, int value){
		if(rowIds.get(rowTag) != null){
			// If it exists, then update val in col
			ArrayList<Component> row = getRowVectorWithTag(rowTag);
			//int rowIdx = rows.indexOf(row);
			int rowIdx = rowIds.get(rowTag);
if(row == null){
	return;
}
			// We go to the specific col to update
			for(int i = 0; i < row.size(); i++){
				// If the current col is equal or greater than col to update, insert col before this one
				int currentElCol = row.get(i).col;
				if(currentElCol > col){
					int insertIdx = (i-1) < 0 ? 0 : i-1;
					row.add(insertIdx, new Component(col, value));
					break;
				}
				else if(currentElCol == col){
					int current_value = row.get(i).value;
					row.set(i, new Component(col, (current_value+value)));
					break;
				}
				// else if the current col is lesser than col, and is the last element...
				else if(currentElCol < col && row.size() == i+1){
					// Insert at the end
					row.add(new Component(col, value));
					// This made the algo work.... shit
					break;
				}
			}
			// Finally insert the updated row
			rows.set(rowIdx, row);
		}
		else{
			// Non existent row, create a new one with the given value
			ArrayList<Component> c = new ArrayList<Component>();
//			System.out.println("Writing: row: "+c.size()+" col: "+col+" value: "+value);
			c.add(new Component(col, value));
			// Add to rows and update rowIds
			rows.add(c);
			rowSize++;
			rowIds.put(rowTag, rows.size()-1);
		}
		
		// Update cols
		ArrayList<Component> row = getRowVectorWithTag(rowTag);
		if (colSize < row.get(row.size()-1).col + 1){
			colSize = row.get(row.size()-1).col + 1;
		}
	}
	
	/** Profiling decomposition **/
	
	private ArrayList<Component> d(ArrayList<Component> row, int insertIdx, int col, int value){
		long s = System.currentTimeMillis();
		row.add(insertIdx, new Component(col, value));
		long e = System.currentTimeMillis();
		dtime += (e-s);
		return row;
	}
	
	private ArrayList<Component> e(ArrayList<Component> row, int i, int col, int current_value, int value){
		long s = System.currentTimeMillis();
		row.set(i, new Component(col, (current_value+value)));
		long e = System.currentTimeMillis();
		etime += (e-s);
		return row;
	}
	
	private ArrayList<Component> f(ArrayList<Component> row, int col, int value){
		long s = System.currentTimeMillis();
		row.add(new Component(col, value));
		long e = System.currentTimeMillis();
		ftime += (e-s);
		return row;
	}
	
	private ArrayList<Component> c(ArrayList<Component> row, int col, int value){
		long s = System.currentTimeMillis();
		for(int i = 0; i < row.size(); i++){
			// If the current col is equal or greater than col to update, insert col before this one
			int currentElCol = row.get(i).col;
			if(currentElCol > col){
				int insertIdx = (i-1) < 0 ? 0 : i-1;
				d(row, insertIdx, col, value);
				break;
			}
			else if(currentElCol == col){
				int current_value = row.get(i).value;
				e(row, i, col, current_value, value);
				break;
			}
			// else if the current col is lesser than col, and is the last element...
			else if(currentElCol < col && row.size() == i+1){
				// Insert at the end
				f(row, col, value);
				// This made the algo work.... shit
				break;
			}
		}
		long e = System.currentTimeMillis();
		ctime += (e-s);
		return row;
	}
	
	private void a(int rowTag, int col, int value){
		long s = System.currentTimeMillis();
		// If it exists, then update val in col
		ArrayList<Component> row = getRowVectorWithTag(rowTag); // a1
		long e1 = System.currentTimeMillis();
		a1 += (e1-s);
		int rowIdx = rowIds.get(rowTag);
		long e2 = System.currentTimeMillis();
		a2 += (e2-s);
		// We go to the specific col to update
		row = c(row, col, value);
		// Finally insert the updated row
		rows.set(rowIdx, row);
		long e3 = System.currentTimeMillis();
		a3 = (e3-s);
		long e = System.currentTimeMillis();
		atime += (e-s);
	}
	
	private void b(int rowTag, int col, int value){
		long s = System.currentTimeMillis();
		// Non existent row, create a new one with the given value
		ArrayList<Component> c = new ArrayList<Component>();
		c.add(new Component(col, value));
		// Add to rows and update rowIds
		rows.add(c);
		rowSize++;
		rowIds.put(rowTag, rows.size()-1);
		long e = System.currentTimeMillis();
		btime += (e-s);
	}
	
	
	public long totaltime = 0;
	public long atime = 0;
	public long a1 = 0;
	public long a2 = 0;
	public long a3 = 0;
	public long btime = 0;
	public long ctime = 0;
	public long dtime = 0;
	public long etime = 0;
	public long ftime = 0;
	
	public void _updateMatrixByReplacingValue(int rowTag, int col, int value){
		long s = System.currentTimeMillis();
		if(rowIds.get(rowTag) != null){
			a(rowTag, col, value);
		}
		else{
			b(rowTag, col, value);
		}
		long e = System.currentTimeMillis();
		totaltime += (e-s);
	}
	
public long reptime = 0;
	public synchronized void updateMatrixByReplacingValue(int rowTag, int col, int value){
//		System.out.println("surprise!");
//long s = System.currentTimeMillis();
		if(rowIds.get(rowTag) != null){
			// If it exists, then update val in col
			ArrayList<Component> row = this.getRowVectorWithTag(rowTag);
			//int rowIdx = rows.indexOf(row); // Looping through size
			int rowIdx = rowIds.get(rowTag);
			// We go to the specific col to update
if(row == null){
	return;
}
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
			// Finally insert the updated row
			rows.set(rowIdx, row);
		}
		else{
			// Non existent row, create a new one with the given value
			ArrayList<Component> c = new ArrayList<Component>();
			c.add(new Component(col, value));
			// Add to rows and update rowIds
			rows.add(c);
			rowSize++;
			rowIds.put(rowTag, rows.size()-1);
		}
		
		// Update cols
		ArrayList<Component> row = getRowVectorWithTag(rowTag);
		if (colSize < row.get(row.size()-1).col + 1){
			colSize = row.get(row.size()-1).col + 1;
		}
//long e = System.currentTimeMillis();
//reptime += (e-s);
	}
	
	public void updateMatrix(ArrayList<Component> vector, int rowTag){
		// If the row already existed
		if(rowIds.get(rowTag) != null){
			rows.set(rowIds.get(rowTag), vector);
		}
		// If its a new row
		else{
			rows.add(vector);
			rowSize++;
			rowIds.put(rowTag, rows.size()-1);
		}
		// Update cols
		if (colSize < vector.get(vector.size()-1).col + 1){
			colSize = vector.get(vector.size()-1).col + 1;
		}
	}
		

	/** OPERATIONS OVER MATRICES **/
	
	private ArrayList<ArrayList<Integer>> getZeroSquaredMatrix(int n){
		ArrayList<ArrayList<Integer>> cMatrix = new ArrayList<ArrayList<Integer>>(n);
		for(int i = 0; i<n; i++){
			ArrayList<Integer> cRow = new ArrayList<Integer>();
			for(int j = 0; j<n; j++){
				cRow.add(0);
			}
			cMatrix.add(cRow);
		}
		return cMatrix;
	}
	
//	

	public Matrix LILcoOccurrence(ArrayList<ArrayList<Component>> m) {
		if(m.isEmpty()){
			return null;
		}
		Matrix co = new Matrix();
		// Loop through rows
		for(int i = 0; i<m.size(); i++){
			// Item selector, only picking non-zero elements, the relevant ones
			for(Component item : m.get(i)){
				// We iterate over the elements, also non-zero values always	
				for(Component iter : m.get(i)){
					// Match, we want to increase the counter of co-occurrence for item-iter
					// update for row i, col item, with value++
					co.updateMatrixByIncreasingValue(item.col, iter.col, 1);
				}
			}
		}
		return co;
	}
	
	public long totalco = 0;
	public long aco = 0;
	public long bco = 0;
	public long cco = 0;
	public long dco = 0;
	
	private Matrix dco(Matrix currentCO, Component item, Component iter, HashMap<Integer, Boolean> mem){
		long s = System.currentTimeMillis();
		int key = item.col*10+iter.col;
		if(mem.get(key) == null){
			currentCO.updateMatrixByReplacingValue(item.col, iter.col, 0);
			mem.put(key, true);
		}
		currentCO.updateMatrixByIncreasingValue(item.col, iter.col, 1);
		long e = System.currentTimeMillis();
		dco += (e-s);
		return currentCO;
	}
	
	private Matrix cco(ArrayList<ArrayList<Component>> m, Matrix currentCO, int itemId, HashMap<Integer, Boolean> mem, int i, Component item, ArrayList<Component> row, int j){
		long s = System.currentTimeMillis();
		// We iterate over the elements, also non-zero values always
		//for(Component iter : row){
		for(int k = j ; j<row.size(); j++){
			Component iter = row.get(k);
			// Match, we want to increase the counter of co-occurrence for item-iter
			// update for row i, col item, with value++
			if(item.col == itemId || iter.col == itemId){
				currentCO = dco(currentCO, item, iter, mem);
				break;
			}
		}
		long e = System.currentTimeMillis();
		cco += (e-s);
		return currentCO;
	}
	
	private Matrix bco(ArrayList<ArrayList<Component>> m, Matrix currentCO, int itemId, HashMap<Integer, Boolean> mem, int i){
		long s = System.currentTimeMillis();
		// Item selector, only picking non-zero elements, the relevant ones
		ArrayList<Component> row = m.get(i);
		for(Component item : row){
			int j = i;
			// Constraint on the item id to modify
			currentCO = cco(m, currentCO, itemId, mem, i, item, row, j);
		}
		long e = System.currentTimeMillis();
		bco += (e-s);
		return currentCO;
	}
	
	private Matrix aco(ArrayList<ArrayList<Component>> m, Matrix currentCO, int itemId){
		long s = System.currentTimeMillis();
		int rowSize = m.size();
		HashMap<Integer, Boolean> mem = new HashMap<Integer, Boolean>();
		// Loop through rows
		for(int i = 0; i<rowSize; i++){
			currentCO = bco(m, currentCO, itemId, mem, i);
		}
		long e = System.currentTimeMillis();
		aco += (e-s);
		return currentCO;
	}
	
	public Matrix _incLILcoOccurrence(ArrayList<ArrayList<Component>> m, Matrix currentCO, int itemId){
		long s = System.currentTimeMillis();
		if(m.isEmpty()){
			return null;
		}
		
		aco(m, currentCO, itemId);
		long e = System.currentTimeMillis();
		totalco += (e-s);
		return currentCO;
	}

	
public long cotime = 0;
public int its = 0;
	public Matrix incLILcoOccurrence(ArrayList<ArrayList<Component>> m, Matrix currentCO, int itemId){
//long s = System.currentTimeMillis();
//its++;
		if(m.isEmpty()){
			return null;
		}
		int rowSize = m.size();
		HashMap<Integer, Boolean> mem = new HashMap<Integer, Boolean>();
		// Loop through rows
		for(int i = 0; i<rowSize; i++){
			// Item selector, only picking non-zero elements, the relevant ones
			for(Component item : m.get(i)){
				// Constraint on the item id to modify
				// We iterate over the elements, also non-zero values always
				for(Component iter : m.get(i)){
					// Match, we want to increase the counter of co-occurrence for item-iter
					// update for row i, col item, with value++
					if(item.col == itemId || iter.col == itemId){
						int key = item.col*10+iter.col;
						if(mem.get(key) == null){
							currentCO.updateMatrixByReplacingValue(item.col, iter.col, 0);
							mem.put(key, true);
						}
						currentCO.updateMatrixByIncreasingValue(item.col, iter.col, 1);
					}
				}
			}
		}
//long e = System.currentTimeMillis();
//cotime += (e-s);
		return currentCO;
	}
	
	public Matrix incLILcoOccurrence2(ArrayList<ArrayList<Component>> m, Matrix currentCO, int itemId){
		if(m.isEmpty()){
			return null;
		}
		int rowSize = m.size();
		HashMap<Integer, Boolean> mem = new HashMap<Integer, Boolean>();
		// Loop through rows
		for(int i = 0; i<rowSize; i++){
			// Item selector, only picking non-zero elements, the relevant ones
			ArrayList<Component> row = m.get(i);
			for(Component item : row){
				int j = i;
				// Constraint on the item id to modify
				// We iterate over the elements, also non-zero values always
				//for(Component iter : row){
				for(int k = j; j<row.size(); j++){
					Component iter = row.get(k);
					// Match, we want to increase the counter of co-occurrence for item-iter
					// update for row i, col item, with value++
					if(item.col == itemId || iter.col == itemId){
						int key = item.col*10+iter.col;
						if(mem.get(key) == null){
							currentCO.updateMatrixByReplacingValue(item.col, iter.col, 0);
							mem.put(key, true);
						}
						currentCO.updateMatrixByIncreasingValue(item.col, iter.col, 1);
					}
				}
			}
		}
		return currentCO;
	}
	
	//TODO: implement the optimised method using accessIdx (to compute only the diagonal)
	@Override
	@Deprecated
	public ArrayList<ArrayList<Integer>> coOccurrence(ArrayList<ArrayList<Integer>> m) {
		if(m.isEmpty()){
			return null;
		}
		int numItems = m.get(0).size();
		// Build a zeroed co-occurrence matrix
		ArrayList<ArrayList<Integer>> cMatrix = getZeroSquaredMatrix(numItems);
		// loop through rows
		for(int i = 0; i<m.size(); i++){
			// Pick a given item
			for(int j = 0; j<numItems; j++){
				if(getValue(i, j, m) != 0){
				// Check if the current item co-occurs with the rest of items
					for(int k = 0; k<numItems; k++){
						if(getValue(i, k, m) != 0){
							int value = getValue(j, k, cMatrix);
							ArrayList<Integer> rowToUpdate = cMatrix.get(j);
							rowToUpdate.set(k, ++value);
							cMatrix.set(j, rowToUpdate);
						}
					}
				}
			}
		}
		return cMatrix;
	}
	
	private int getValue(int row, int col, ArrayList<ArrayList<Integer>> m){
		return m.get(row).get(col);
	}

	public ArrayList<Component> LILmultiplyVectorByDiagonalMatrix(ArrayList<Component> v, Matrix m){
		if(v == null || m == null){
			return null;
		}
		ArrayList<Component> r = new ArrayList<Component>();
		// Every row in the matrix m
		for(int i = 0; i<m.getRows().size(); i++){
			// Pick the row
			ArrayList<Component> toBeMultiplied = m.getRowVectorWithTag(i);
			if(toBeMultiplied != null){
				// Now, multiply that row by the vector and get 1 value.
				int partialSum = 0;
				// Every non zero component in the vector
				for(Component cv : v){
					// Multiplied by every non/zero component in the toBeMultiplied vector
					for(Component cm : toBeMultiplied){
						// If the col matches
						if(cv.col == cm.col){
//							System.out.println("Multiply: "+cv.col+" by "+cm.col);
//							System.out.println("For a total value of : "+(cv.value * cm.value));
							partialSum = partialSum + (cv.value * cm.value);
							break;
						}
					}
				}
				if(partialSum != 0){
					Component newComponent = new Component(i, partialSum);
					r.add(newComponent);
				}
			}
		}
		return r;
	}
	
	@Deprecated
	public static ArrayList<Integer> multipyVectorByDiagonalMatrix(ArrayList<Integer> vector, ArrayList<ArrayList<Integer>> m) {
		int cols = vector.size();
		// A value per item
		ArrayList<Integer> rVector = new ArrayList<Integer>(cols);
		for(ArrayList<Integer> row : m){
			int v = 0;
			for(int i = 0; i < cols; i++){
				v += vector.get(i) * row.get(i);
			}
			rVector.add(v);
		}
		return rVector;
	}
	
	/** OPERATIONS TO SPLIT MATRICES **/

	@Override
	public Matrix[] splitByCol(Matrix m) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix[] splitByRows(Matrix m) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/** OPERATIONS RELATED TO MATRICES/VECTOR REPRESENTATIONS **/
	
	public static ArrayList<Integer> representVectorAsBest(ArrayList<Component> v){
		int estimatedColSize;
		ArrayList<Integer> r = new ArrayList<Integer>();
		if(!v.isEmpty()){
			estimatedColSize = v.get(v.size()-1).col;
		}
		else{
			return null;
		}
		estimatedColSize++; // Add additional column
		for(int i = 0; i<estimatedColSize; i++){
			r.add(0);
		}
		for(Component c : v){
			//int idx = (c.col-1 < 0) ? 0 : c.col-1;
			int idx = c.col;
			r.set(idx, c.value);
		}
		return r;
	}
	
	@Override
	public ArrayList<ArrayList<Integer>> getExpandedRepr(){
		// Build a zeroed matrix
		ArrayList<ArrayList<Integer>> xMatrix = new ArrayList<ArrayList<Integer>>(this.rows.size());
		for(int j = 0; j<this.rows.size(); j++){
			ArrayList<Integer> xRow = new ArrayList<Integer>(colSize);
			for(int i = 0; i< colSize; i++){
				xRow.add(0);
			}
			int insertIdx = rowIds.get(j);
			xMatrix.add(insertIdx, xRow);
		}
		
		for(ArrayList<Component> row : rows){
			int rowIdx = rows.indexOf(row);
			ArrayList<Integer> pivot = xMatrix.get(rowIdx);
			for(Component c : row){
				pivot.set(c.col, c.value);
			}
			xMatrix.set(rowIdx, pivot);
		}
		return xMatrix;
	}
	
	public ArrayList<Integer> getExpandedVectorRepresentation(ArrayList<Component> vector){
		// Create zeroed vector
		ArrayList<Integer> toReturn = new ArrayList<Integer>();
		for(int i = 0; i<colSize; i++){
			toReturn.add(0);
		}
		for(Component c : vector){
			toReturn.set(c.col, c.value);
		}
		return toReturn;
	}
	
	/** PRINT/OUTPUT METHODS **/
	
	public void printVector(ArrayList<Component> vector){
		// Create zeroed vector
		ArrayList<Integer> toPrint = new ArrayList<Integer>();
		for(int i = 0; i<colSize; i++){
			toPrint.add(0);
		}
		for(Component c : vector){
			toPrint.set(c.col, c.value);
		}
		for(Integer i : toPrint){
			System.out.print(i+", ");
		}
		System.out.println("");
	}
	
	
	public void printIntegerVector(ArrayList<Integer> vector){
		// One print per colum
		for(int i = 0; i<vector.size(); i++){
			System.out.print(vector.get(i)+", ");
		}
		System.out.println("");
	}
	
	public void printMatrix(){
		// how many rows are there?
		int rows = this.getRows().size();
		for(int i = 0; i<rows; i++){
			int rowIdx = rowIds.get(i);
			ArrayList<Component> r = this.getRows().get(rowIdx);
			printVector(r);
		}
		System.out.println();
	}
	
	public void printXMatrix(ArrayList<ArrayList<Integer>> x){
		for(ArrayList<Integer> row : x){
			for(Integer i : row){
				System.out.print(i+", ");
			}
			System.out.println("");
		}
	}
	
	public void printDimensions(){
		System.out.println("DIM: "+rowSize+"x"+colSize);
	}

	public void printDimensionsReliably() {
		System.out.println("DIM-rows: "+this.rows.size());
		
	}
}

//@Deprecated
//public Matrix LILcoOccurrence2(ArrayList<ArrayList<Component>> m, Matrix co) {
//	if(m.isEmpty()){
//		return null;
//	}
//	// Loop through rows
//	for(int i = 0; i<m.size(); i++){
//		// Item selector, only picking non-zero elements, the relevant ones
//		for(Component item : m.get(i)){
//			// We iterate over the elements, also non-zero values always
//			for(Component iter : m.get(i)){
//				// Match, we want to increase the counter of co-occurrence for item-iter
//				// update for row i, col item, with value++
//				co.updateMatrixByIncreasingValue(item.col, iter.col, 1);
//			}
//		}
//	}
//	return co;
//}
//
//@Deprecated
//public Matrix LILcoOccurrence3_gitan(ArrayList<ArrayList<Component>> m, Matrix co, int row, int col) {
//	if(m.isEmpty()){
//		return null;
//	}
//	// Loop through rows
//	for(int i = 0; i<m.size(); i++){
//		// Item selector, only picking non-zero elements, the relevant ones
//		for(Component item : m.get(i)){
//			// We iterate over the elements, also non-zero values always
//			for(Component iter : m.get(i)){
//				// Match, we want to increase the counter of co-occurrence for item-iter
//				// update for row i, col item, with value++
//				if(item.col == row && iter.col == col){
//					co.updateMatrixByIncreasingValue(item.col, iter.col, 1);
//				}
//			}
//		}
//	}
//	return co;
//}
