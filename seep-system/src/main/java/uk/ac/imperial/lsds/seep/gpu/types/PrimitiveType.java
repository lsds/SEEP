package uk.ac.imperial.lsds.seep.gpu.types;

public interface PrimitiveType extends Comparable<PrimitiveType> {
	
	public PrimitiveType add(PrimitiveType toAdd);
	public PrimitiveType sub(PrimitiveType toAdd);
	public PrimitiveType mul(PrimitiveType toAdd);
	public PrimitiveType div(PrimitiveType toAdd);
	
	public void setFromString(String s);
	public PrimitiveType parseFromString(String s);
}
