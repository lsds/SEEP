package uk.ac.imperial.lsds.seepmaster.infrastructure.master;

import uk.ac.imperial.lsds.seep.infrastructure.ExecutionUnitType;

public interface InfrastructureManager {

	public ExecutionUnitType getExecutionUnitType();
	
	public boolean addExecutionUnit(ExecutionUnit eu);
	public boolean removeExecutionUnit(int id);
	public int executionUnitsAvailable();
	
	public void claimExecutionUnits(int numExecutionUnits);
	public void decommisionExecutionUnits(int numExecutionUnits);
	public void decommisionExecutionUnit(ExecutionUnit node);
	
}
