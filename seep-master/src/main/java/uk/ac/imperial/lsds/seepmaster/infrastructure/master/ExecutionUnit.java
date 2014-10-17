package uk.ac.imperial.lsds.seepmaster.infrastructure.master;

import uk.ac.imperial.lsds.seep.infrastructure.EndPoint;
import uk.ac.imperial.lsds.seep.infrastructure.ExecutionUnitType;

public interface ExecutionUnit {
	
	public ExecutionUnitType getType();
	public int getId();
	public String toString();
	public EndPoint getEndPoint();
	
}
