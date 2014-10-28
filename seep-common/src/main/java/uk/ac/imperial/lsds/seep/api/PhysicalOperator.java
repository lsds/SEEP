package uk.ac.imperial.lsds.seep.api;

import uk.ac.imperial.lsds.seep.infrastructure.EndPoint;

public interface PhysicalOperator extends Operator{
	
	public int getIdOfWrappingExecutionUnit();
	public EndPoint getWrappingEndPoint();
	
}
