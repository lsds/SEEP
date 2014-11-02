package uk.ac.imperial.lsds.seepmaster.infrastructure.master;

public class InfrastructureManagerFactory {

	public static InfrastructureManager createInfrastructureManager(int infType){
		if(infType == InfrastructureType.PHYSICAL_CLUSTER.ofType()) {
			return new PhysicalClusterManager();
		}
		return null;
	}
	
}
