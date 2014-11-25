package uk.ac.imperial.lsds.seep.multi;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

public class TaskFuture extends FutureTask<Integer> implements RunnableFuture<Integer> {
	
	private Callable<Integer> callable;
	
	public TaskFuture(Callable<Integer> callable) {
		super(callable);
		this.callable = callable;
	}
	
	public Callable<Integer> getCallable () {
		return callable;
	}
	
	@Override
	protected void done () {
		TaskFactory.free(callable);
	}
}
