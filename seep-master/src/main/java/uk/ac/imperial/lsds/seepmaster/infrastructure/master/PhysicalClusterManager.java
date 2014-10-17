package uk.ac.imperial.lsds.seepmaster.infrastructure.master;

import java.util.Set;

import uk.ac.imperial.lsds.seep.infrastructure.ExecutionUnitType;

public class PhysicalClusterManager implements InfrastructureManager {
	
	private static final ExecutionUnitType executionUnitType = ExecutionUnitType.PHYSICAL_NODE;
	private Set<ExecutionUnit> physicalNodes;

	@Override
	public ExecutionUnitType getExecutionUnitType() {
		return executionUnitType;
	}
	
	@Override
	public boolean addExecutionUnit(ExecutionUnit eu) {
		return physicalNodes.add(eu);
	}

	@Override
	public boolean removeExecutionUnit(int id) {
		for(ExecutionUnit eu : physicalNodes){
			if(eu.getId() == id){
				physicalNodes.remove(eu);
				return true;
			}
		}
		return false;
	}

	@Override
	public int executionUnitsAvailable() {
		return physicalNodes.size();
	}

	@Override
	public void claimExecutionUnits(int numExecutionUnits) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void decommisionExecutionUnits(int numExecutionUnits) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void decommisionExecutionUnit(ExecutionUnit eu) {
		// TODO Auto-generated method stub
		
	}

}
