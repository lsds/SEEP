package uk.ac.imperial.lsds.seepmaster.infrastructure.master;

public enum InfrastructureType {
	PHYSICAL_CLUSTER(0), YARN_CLUSTER(1), DOCKER_CLUSTER(2), VIRTUAL_CLUSTER(3), SHARED_PHYSICAL_CLUSTER(4);
	
	private int type;
	
	InfrastructureType(int type){
		this.type = type;
	}
	
	public int ofType(){
		return type;
	}
	
}
