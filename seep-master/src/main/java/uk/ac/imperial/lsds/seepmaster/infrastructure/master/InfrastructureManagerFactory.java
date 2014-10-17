package uk.ac.imperial.lsds.seepmaster.infrastructure.master;

public class InfrastructureManagerFactory {

	public static InfrastructureManager createInfrastructureManager(InfrastructureType type){
		if(type == InfrastructureType.PHYSICAL_CLUSTER) {
			return new PhysicalCluster();
		}
		return null;
	}
	
}
