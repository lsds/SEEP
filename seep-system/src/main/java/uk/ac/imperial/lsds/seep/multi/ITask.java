package uk.ac.imperial.lsds.seep.multi;

public interface ITask extends IWindowAPI {

	public int run();

	public void free();
}
