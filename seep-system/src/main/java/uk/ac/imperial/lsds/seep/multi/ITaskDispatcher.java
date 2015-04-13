package uk.ac.imperial.lsds.seep.multi;

public interface ITaskDispatcher {

	public void setup();

	public void dispatch(byte [] data, int length);
	
	public boolean tryDispatch (byte [] data, int length);

	public void dispatchSecond (byte [] data, int length);

	public IQueryBuffer getBuffer();
	
	public IQueryBuffer getSecondBuffer();

	public boolean tryDispatchFirst (byte[] data, int length);
	
	public boolean tryDispatchSecond (byte[] data, int length);
}
