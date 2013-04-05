package seep.infrastructure.master;

public class CodeDeploymentException extends Exception {

	private static final long serialVersionUID = 1L;

	public CodeDeploymentException(String msg){
		super(msg);
	}
}
