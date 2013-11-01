package uk.ac.imperial.lsds.seep.state;

public class EmptyStateException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public EmptyStateException(String msg){
		super(msg);
	}

}
