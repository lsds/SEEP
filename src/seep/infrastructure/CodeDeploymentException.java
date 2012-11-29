package seep.infrastructure;

public class CodeDeploymentException extends Exception {

	private static final long serialVersionUID = 1L;

	public CodeDeploymentException(String msg){
		super(msg);
	}
}
