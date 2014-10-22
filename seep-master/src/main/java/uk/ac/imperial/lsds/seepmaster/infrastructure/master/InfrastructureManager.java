package uk.ac.imperial.lsds.seepmaster.infrastructure.master;

import uk.ac.imperial.lsds.seep.infrastructure.EndPoint;

public interface InfrastructureManager {
	
	public ExecutionUnit buildExecutionUnit(EndPoint ep);
	
	public void addExecutionUnit(ExecutionUnit eu);
	public ExecutionUnit getExecutionUnit();
	public boolean removeExecutionUnit(int id);
	public int executionUnitsAvailable();
	
	public void claimExecutionUnits(int numExecutionUnits);
	public void decommisionExecutionUnits(int numExecutionUnits);
	public void decommisionExecutionUnit(ExecutionUnit node);
	
}
