package uk.ac.imperial.lsds.streamsql.types;

import uk.ac.imperial.lsds.seep.operator.compose.multi.TupleObject;

public interface PrimitiveType extends Comparable<PrimitiveType>, Cloneable, TupleObject {
	
	public PrimitiveType add(PrimitiveType toAdd);
	public PrimitiveType sub(PrimitiveType toAdd);
	public PrimitiveType mul(PrimitiveType toAdd);
	public PrimitiveType div(PrimitiveType toAdd);
	
	public void setFromString(String s);
	public PrimitiveType parseFromString(String s);
	
}
