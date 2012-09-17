package seep.infrastructure;

/**
* DeploymentException. This class models an exception ocurred during deployment phase
*/

/// \todo {this one is never used}
public class DeploymentException extends Exception{
	
	private static final long serialVersionUID = 1L;

	public DeploymentException(String msg){
		super(msg);
	}
}
