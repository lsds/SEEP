package uk.co.imperial.lsds.seep.infrastructure;

/**
* DeploymentException. This class models an exception ocurred during deployment phase
*/

/// \todo {this one is never used}
public class OperatorDeploymentException extends Exception{
	
	private static final long serialVersionUID = 1L;

	public OperatorDeploymentException(String msg){
		super(msg);
	}
}
