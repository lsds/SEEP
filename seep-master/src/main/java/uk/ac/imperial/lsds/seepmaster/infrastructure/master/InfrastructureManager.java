package uk.ac.imperial.lsds.seepmaster.infrastructure.master;

public interface InfrastructureManager {

	public boolean addExecutionUnit();
	public boolean removeExecutionUnit();
	public int executionUnitsAvailable();
	
	public void claimExecutionUnits(int numExecutionUnits);
	public void decommisionExecutionUnits(int numExecutionUnits);
	public void decommisionExecutionNode(ExecutionUnit node);
	
}
