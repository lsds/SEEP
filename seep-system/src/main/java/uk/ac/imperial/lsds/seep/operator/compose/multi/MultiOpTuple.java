package uk.ac.imperial.lsds.seep.operator.compose.multi;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTupleI;

public class MultiOpTuple implements DataTupleI {
	
	public long timestamp;
	public long instrumentation_ts;
	
	private double[] numericValues;
	private String[] stringValues;
	private String[] stringKeys;
	private String[] numericKeys;
	
	@Override
	public Object getValue(String attribute) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getString(String attribute) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getStringArray(String attribute) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Character getChar(String attribute) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Byte getByte(String attribute) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getByteArray(String attribute) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getInt(String attribute) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[] getIntArray(String attribute) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Short getShort(String attribute) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getLong(String attribute) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Float getFloat(String attribute) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double getDouble(String attribute) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] getDoubleArray(String attribute) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float[] getFloatArray(String attribute) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getBoolean(String attribute) {
		// TODO Auto-generated method stub
		return false;
	}

}
