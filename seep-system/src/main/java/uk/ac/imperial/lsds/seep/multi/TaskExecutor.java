package uk.ac.imperial.lsds.seep.multi;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TaskExecutor 
	extends ThreadPoolExecutor {
	
	public TaskExecutor (int threads_, int _threads, long timeout, TimeUnit unit, BlockingQueue<Runnable> queue) {
		super(threads_, _threads, timeout, unit, queue);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected <V> RunnableFuture<V> newTaskFor (Callable<V> callable) {
		return (RunnableFuture<V>) new TaskFuture ((Callable<Integer>)callable);
	}
	
	@Override
	protected void afterExecute(Runnable runnable, Throwable throwable) {
		return ;
	}
}
