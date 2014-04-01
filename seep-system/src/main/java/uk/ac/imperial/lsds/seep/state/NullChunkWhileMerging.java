package uk.ac.imperial.lsds.seep.state;

public class NullChunkWhileMerging extends Exception {

	private static final long serialVersionUID = 1L;

	public NullChunkWhileMerging(String msg){
		super(msg);
	}
	
}
