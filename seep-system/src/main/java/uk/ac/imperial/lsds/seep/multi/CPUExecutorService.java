package uk.ac.imperial.lsds.seep.multi;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.RejectedExecutionException;

import java.util.List;
import java.util.ArrayList;

import java.lang.Runnable;

public class CPUExecutorService extends AbstractExecutorService {

	private final BlockingQueue<Runnable> [] queue;
	private final Worker [] worker;
	
	private final int capacity;
	private final int N; /* Number of workers */
	
	private long count = 0L;
	
	public CPUExecutorService (int N, int capacity) {
		this.N = N;
		this.capacity = capacity;
		
		queue = new BlockingQueue [N];
		worker = new Worker [N];
		for (int i = 0; i < N; i++) {
			queue [i] = new ArrayBlockingQueue<Runnable>(capacity);
			worker[i] = new Worker(i);
		}
		
		count = 0L;
	}
	
	public int getCapacity () { return capacity; }
	
	private void reject (Runnable command) {
		throw new RejectedExecutionException
			("GPUExecutorService queue is full!");
	}
	
	public void execute(Runnable command) {
		if (command == null)
			throw new NullPointerException();
		
		int index = (int) (count % N);
		count += 1;
		if (! queue[index].offer(command))
			reject(command);
		
		/* Debugging */
		if (count % 1000 == 0) {
			StringBuilder s = new StringBuilder(1024);
			for (int i = 0; i < N; i++)
				s.append(String.format("[%6d/%6d] ", queue[i].size(), worker[i].completed));
			System.out.println(s.toString());
		}
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
		for (int i = 0; i < N; i++) {
			Thread t = worker[i].thread;
			try {
				t.interrupt();
			} catch (SecurityException ignore) {}
			System.out.println(
			String.format("[Worker %3d] %6d tasks completed", 
			i, worker[i].completed));
		}
		return ;
	}
	
	public List<Runnable> shutdownNow () {
		List<Runnable> tasks = null;
		shutdown();
		tasks = new ArrayList<Runnable>();
		/* TODO: Drain all tasks */
		queue[0].drainTo(tasks);
		return tasks;
	}
	
	private Runnable getTask (int index) {
		Runnable task = null;
		try {
			task = queue[index].take();
		} catch (InterruptedException e) {}
		return task;
	}
	
	final void runWorker (Worker w) {
		Runnable task;
		while ((task = getTask(w.index)) != null) {
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
		
		int index;
		
		Worker (int index) {
			completed = 0L;
			this.index = index;
			thread = new Thread(this);
			thread.setName(String.format("Executor-%d", index));
			thread.start();
		}
		
		public void run () {
			runWorker(this);
		}
	}
}
