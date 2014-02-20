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
package uk.ac.imperial.lsds.seep.comm.serialization;

public interface DataTupleI {

	/**
	 * For maximum performance we can provide direct access methods, otherwise we will always do a map lookup first
	 */
	
	public Object getValue(String attribute);
	public String getString(String attribute);
	public String[] getStringArray(String attribute);
	public Character getChar(String attribute);
	public Byte getByte(String attribute);
	public byte[] getByteArray(String attribute);
	public Integer getInt(String attribute);
	public int[] getIntArray(String attribute);
	public Short getShort(String attribute);
	public Long getLong(String attribute);
	public Float getFloat(String attribute);
	public Double getDouble(String attribute);
	public double[] getDoubleArray(String attribute);
	public float[] getFloatArray(String attribute);
	public boolean getBoolean(String attribute);
	
}
