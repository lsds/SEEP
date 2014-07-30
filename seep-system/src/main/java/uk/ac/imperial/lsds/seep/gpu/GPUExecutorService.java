package uk.ac.imperial.lsds.seep.gpu;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.RejectedExecutionException;

import java.util.List;
import java.util.ArrayList;

import java.lang.Runnable;

public class GPUExecutorService extends AbstractExecutorService {

	private final BlockingQueue<Runnable> queue;
	private final int capacity;
	private final Worker worker;
	
	public GPUExecutorService (int capacity) {
		this.capacity = capacity;
		queue = new ArrayBlockingQueue<Runnable>(capacity);
		worker = new Worker();
	}
	
	public int getCapacity () { return capacity; }
	
	private void reject (Runnable command) {
		throw new RejectedExecutionException
			("GPUExecutorService queue is full!");
	}
	
	public void execute(Runnable command) {
		if (command == null)
			throw new NullPointerException();
		
		if (! queue.offer(command))
			reject(command);
	}
	
	public boolean awaitTermination(long timeout, TimeUnit unit)
		throws InterruptedException {
		
		return false;
	}
	
	public boolean isTerminated () {
		return false;
	}
	
	public boolean isShutdown () {
		return false;
	}
	
	public void shutdown () {
		Thread t = worker.thread;
		try {
			t.interrupt();
		} catch (SecurityException ignore) {}
		GPUUtils.out(String.format("%3d tasks completed", worker.completed));
		return ;
	}
	
	public List<Runnable> shutdownNow () {
		List<Runnable> tasks = null;
		shutdown();
		tasks = new ArrayList<Runnable>();
		queue.drainTo(tasks);
		return tasks;
	}
	
	private Runnable getTask () {
		Runnable task = null;
		try {
			task = queue.take();
		} catch (InterruptedException e) {}
		return task;
	}
	
	final void runWorker (Worker w) {
		Runnable task;
		while ((task = getTask()) != null) {
			try {
				task.run();
			} 
			catch (Exception e) { throw e; }
			finally { w.completed++; }
		}
		return ;
	}
	
	private final class Worker implements Runnable {
		
		volatile long completed; /* Completed tasks */
		final Thread thread;
		
		Worker () {
			completed = 0L;
			thread = new Thread(this);
			thread.start();
		}
		
		public void run () {
			runWorker(this);
		}
	}
}
