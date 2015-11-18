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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.lsds.seep.comm.serialization.messages.Payload;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.TuplePayload;

public class DataTuple implements DataTupleI, Serializable{

	private static final long serialVersionUID = 1L;
	private TuplePayload payload;
	private final Map<String, Integer> idxMapper;

	public DataTuple(Map<String, Integer> idxMapper, TuplePayload payload){
		this.payload = payload;
		//this.attrValues = payload.attrValues;
		this.idxMapper = idxMapper;
	}
	
	/** DEBUG METHODS */
	
	public HashMap<String, Integer> getMap(){
		return (HashMap<String, Integer>) idxMapper;
	}
	
	public int size(){
//		if(payload == null || payload.attrValues == null) return 0;
		return payload.attrValues.size();
	}
	
	/** */
	
	public DataTuple(){
		idxMapper = new HashMap<String, Integer>();
	}
	
	public static DataTuple getNoopDataTuple(){
		return new DataTuple();
	}
	
	public TuplePayload getPayload(){
		return payload;
	}
	
	public void set(TuplePayload tuplePayload){
		this.payload = tuplePayload;
	}
	
	public void setValuesMutable(Object...objects){
		payload.attrValues = new Payload(objects);
	}
	
	/** EXPERIMENTAL **/
	public DataTuple setValues(Object...objects){
		TuplePayload tp = new TuplePayload();
		tp.attrValues = new Payload(objects);
		tp.timestamp = this.payload.timestamp;
		tp.instrumentation_ts = this.payload.instrumentation_ts;
		tp.local_ts = this.payload.local_ts;
		DataTuple dt = new DataTuple(idxMapper, tp);
		return dt;
	}
	
	// to be used by java2sdg
	@Deprecated
	public DataTuple _setValues(Object[] objects){
		TuplePayload tp = new TuplePayload();
		tp.attrValues = new Payload(objects);
		tp.timestamp = this.payload.timestamp;
		tp.instrumentation_ts = this.payload.instrumentation_ts;
		tp.local_ts = this.payload.local_ts;
		DataTuple dt = new DataTuple(idxMapper, tp);
		return dt;
	}
	
	public DataTuple newTuple(Object...objects){
		TuplePayload tp = new TuplePayload();
		tp.attrValues = new Payload(objects);
		tp.timestamp = System.currentTimeMillis();
		tp.instrumentation_ts =  tp.timestamp;
		tp.local_ts = tp.instrumentation_ts;
		DataTuple dt = new DataTuple(idxMapper, tp);
		return dt;
	}
	
	// to be used by java2sdg
	@Deprecated
	public DataTuple _newTuple(Object[] objects){
		TuplePayload tp = new TuplePayload();
		tp.attrValues = new Payload(objects);
		tp.timestamp = System.currentTimeMillis();
		tp.instrumentation_ts =  tp.timestamp;
		tp.local_ts = tp.instrumentation_ts;
		DataTuple dt = new DataTuple(idxMapper, tp);
		return dt;
	}
	
	@Override
	public Byte getByte(String attribute) {
		return (Byte)payload.attrValues.get(idxMapper.get(attribute));
	}

	@Override
	public byte[] getByteArray(String attribute) {
		return (byte[])payload.attrValues.get(idxMapper.get(attribute));
	}

	@Override
	public Character getChar(String attribute) {
		return (Character)payload.attrValues.get(idxMapper.get(attribute));
	}

	@Override
	public Double getDouble(String attribute) {
		return (Double)payload.attrValues.get(idxMapper.get(attribute));
	}

	@Override
	public double[] getDoubleArray(String attribute) {
		return (double[])payload.attrValues.get(idxMapper.get(attribute));
	}

	@Override
	public Float getFloat(String attribute) {
		return (Float)payload.attrValues.get(idxMapper.get(attribute));
	}

	@Override
	public Integer getInt(String attribute) {
		return (Integer)payload.attrValues.get(idxMapper.get(attribute));
	}

	@Override
	public int[] getIntArray(String attribute) {
		return (int[])payload.attrValues.get(idxMapper.get(attribute));
	}

	@Override
	public float[] getFloatArray(String attribute) {
		return (float[])payload.attrValues.get(idxMapper.get(attribute));
	}

	@Override
	public Long getLong(String attribute) {
		return (Long)payload.attrValues.get(idxMapper.get(attribute));
	}

	@Override
	public long[] getLongArray(String attribute) {
		return (long[])payload.attrValues.get(idxMapper.get(attribute));
	}

	@Override
	public Short getShort(String attribute) {
		return (Short)payload.attrValues.get(idxMapper.get(attribute));
	}

	@Override
	public String getString(String attribute) {
		return (String)payload.attrValues.get(idxMapper.get(attribute));
	}

	@Override
	public String[] getStringArray(String attribute) {
		return (String[])payload.attrValues.get(idxMapper.get(attribute));
	}

	@Override
	public Object getValue(String attribute) {
//		System.out.println("getValue = attrValues.size -> "+payload.attrValues.size()+" accessed in "+idxMapper.get(attribute));
		return (Object)payload.attrValues.get(idxMapper.get(attribute));
	}
	
	@Override
	public boolean getBoolean(String attribute){
//		System.out.println("getBoolean = attrValues.size -> "+payload.attrValues.size()+" accessed in "+idxMapper.get(attribute));
		return (Boolean)payload.attrValues.get(idxMapper.get(attribute));
	}
	
	@Override
	public String toString(){
		return payload.toString();
	}
	
}
