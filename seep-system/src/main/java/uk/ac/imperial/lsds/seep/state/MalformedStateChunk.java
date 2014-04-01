package uk.ac.imperial.lsds.seep.state;

public class MalformedStateChunk extends Exception {

	private static final long serialVersionUID = 1L;

	public MalformedStateChunk(String msg){
		super(msg);
	}
}
