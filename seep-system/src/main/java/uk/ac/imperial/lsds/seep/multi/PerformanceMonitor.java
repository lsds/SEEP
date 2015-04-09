package uk.ac.imperial.lsds.seep.multi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PerformanceMonitor implements Runnable {
	
	int counter = 0;
	
	private long time, _time = 0L;
	private long dt;
		
	private MultiOperator operator;
	private int size;
		
	private Measurement [] measurements;
	
	private long [][] _tasksProcessed;
	private int  [][] policy_; /* New policy */
		
	static final Comparator<SubQuery> ordering = 
		new Comparator<SubQuery>() {
			
		@Override
		public int compare(SubQuery q, SubQuery p) {
			return (q.getId() < p.getId()) ? -1 : 1;
		}
	};
	
	public PerformanceMonitor (MultiOperator operator) {
		this.operator = operator;
			
		size = operator.getSubQueries().size();
		measurements = new Measurement [size];
		List<SubQuery> L = new ArrayList<SubQuery>(operator.getSubQueries());
		Collections.sort(L, ordering);
		int idx = 0;
		for (SubQuery query : L) {
			System.out.println(String.format("[DBG] [MultiOperator] S %3d", query.getId()));
			measurements[idx++] = 
				new Measurement (query.getId(), 
						query.getTaskDispatcher().getBuffer(), 
						query.getTaskDispatcher().getSecondBuffer(),
						query.getLatencyMonitor());
		}
		
		_tasksProcessed = new long [Utils.THREADS][size];
		for (int i = 0; i < _tasksProcessed.length; i++)
			for (int j = 0; j < size; j++)
				_tasksProcessed[i][j] = 0L;
		
		policy_ = new int [2][size];
		for (int j = 0; j < size; j++) {
			policy_[0][j] = 0;
			policy_[1][j] = 0;
		}
	}
	
	@Override
	public void run () {
		while (true) {
			
			try { 
				Thread.sleep(1000L); 
			} catch (Exception e) 
			{}
			
			time = System.currentTimeMillis();
			StringBuilder b = new StringBuilder();
			b.append("[DBG]");
			dt = time - _time;
			for (int i = 0; i < size; i++)
				b.append(measurements[i].info(dt));
			b.append(String.format(" q %6d", operator.getExecutorQueueSize()));
			
			/* Reset CPU tasks/sec per query since it is not accumulative */
			for (int j = 0; j < size; j++)
				policy_[1][j] = 0;
			
			/* Iterate over worker threads */
			for (int i = 0; i < _tasksProcessed.length; i++) {
				
				/*
				b.append(String.format(" p%02d avg %10.1fns std %10.1fns", 
						i,
						operator.getTaskProcessorPool().mean(i),
						operator.getTaskProcessorPool().stdv(i)
				));
				*/
				
				/* Iterate over queries */
				for (int j = 0; j < size; j++) {
					long tasksProcessed_ = operator.getTaskProcessorPool().getProcessedTasks(i, j);
					long delta = tasksProcessed_ - _tasksProcessed[i][j];
					double tps = (double) delta / (dt / 1000.);
					b.append(String.format(" p%02d q%d %5.1f", i, j, tps));
					if (Utils.HYBRID && i == 0) {
						policy_[0][j] = (int) Math.floor(tps);
					} else {
						policy_[1][j] += (int) Math.floor(tps);
					}
					_tasksProcessed[i][j] = tasksProcessed_;
				}
				operator.updatePolicy(policy_);
			}
			
			/* Append factory sizes */
			b.append(String.format(" %20s", operator.policyToString()));
			b.append(String.format(" t %6d", TaskFactory.count.get()));
			b.append(String.format(" w %6d", WindowBatchFactory.count.get()));
			b.append(String.format(" b %6d", UnboundedQueryBufferFactory.count.get()));
						
			System.out.println(b);
			
			_time = time;
			
			if (counter++ > 60) {
				System.out.println("Done.");
				for (int i = 0; i < size; i++)
					measurements[i].stop();
				break;
			}
		}
	}
		
	class Measurement {
		
		int id;
		IQueryBuffer buffer;
		IQueryBuffer secondBuffer;
		
		LatencyMonitor monitor;
		
		long bytes, _bytes = 0;
		double Dt, MBps;
		double MB, _1MB_ = 1048576.0;

		public Measurement (int id, IQueryBuffer buffer, IQueryBuffer secondBuffer, LatencyMonitor monitor) {
			this.id = id;
			this.buffer = buffer;	
			this.secondBuffer = secondBuffer;	
			
			this.monitor = monitor;
		}
			
		public void stop() {
			monitor.stop();
		}

		@Override
		public String toString () {
			return null;
		}
			
		public String info(long delta) {
			
			String s = "";
			bytes = buffer.getBytesProcessed();
			
			if (secondBuffer != null)
				bytes += secondBuffer.getBytesProcessed();
			
			if (_bytes > 0) {
				Dt = (delta / 1000.0);
				MB = (bytes - _bytes) / _1MB_;
				MBps = MB / Dt;
				s = String.format(" S%03d %10.3f MB/s %10.3f Gbps [%s] ", 
						id, MBps, ((MBps / 1024.) * 8.), monitor);
			}
			_bytes = bytes;
			return s;
		}
	}
}
