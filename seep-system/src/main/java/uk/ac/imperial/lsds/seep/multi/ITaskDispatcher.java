package uk.ac.imperial.lsds.seep.multi;

public interface ITaskDispatcher {

	public void setup();

	public void dispatch(byte [] data);

	public void dispatchSecond(byte [] data);

	public IQueryBuffer getBuffer();
	
	public IQueryBuffer getSecondBuffer();
}
