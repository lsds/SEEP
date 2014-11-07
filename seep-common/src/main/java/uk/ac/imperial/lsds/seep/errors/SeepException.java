package uk.ac.imperial.lsds.seep.errors;

public class SeepException extends RuntimeException{

	private static final long serialVersionUID = 1L;

    public SeepException(String message, Throwable cause) {
        super(message, cause);
    }

    public SeepException(String message) {
        super(message);
    }

    public SeepException(Throwable cause) {
        super(cause);
    }

    public SeepException() {
        super();
    }
	
}
