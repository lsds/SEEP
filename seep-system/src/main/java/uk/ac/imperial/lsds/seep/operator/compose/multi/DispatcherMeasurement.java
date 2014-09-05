package uk.ac.imperial.lsds.seep.operator.compose.multi;

import uk.ac.imperial.lsds.seep.operator.compose.subquery.ISubQueryConnectable;

public class DispatcherMeasurement implements Runnable {
	
	SubQueryTaskDispatcher dispatcher;
	ISubQueryConnectable subquery;
	
	private long current_tuples, previous_tuplecount = 0L;
	private long current_time, previous_time = 0L;
	private long ntuples, dt;
	private long current_tasks;
	private double rate;
	
	
	public DispatcherMeasurement (SubQueryTaskDispatcher dispatcher, ISubQueryConnectable subquery) {
		this.dispatcher = dispatcher;
		this.subquery = subquery;
		//this.rates = new LinkedList<Double>();
	}
	
	public void run () {
		while (true) {
			try { Thread.sleep(1000); } catch (Exception e) {}
			current_time = System.currentTimeMillis();
			// current_tuples = dispatcher.current_tuplecount.get();
			
			current_tuples = subquery.getLocalUpstreamBuffers().values().iterator().next().getProcessedTuples();
			
			current_tasks = dispatcher.num_tasks.get();
			// if (current_tasks > 2000) {
			//	for (Double d: rates) {
			//		System.out.println(d.doubleValue());
			//	}
			//	break;
			//}
			
			if (previous_tuplecount > 0) {
				ntuples = current_tuples - previous_tuplecount;
				dt = current_time - previous_time;
				rate = ntuples / (dt / 1000.);
				System.out.println(String.format("[DBG] [Dispatcher] task %3d %10d tuples %10.1f tuples/s queue size %d tstamp %13d", 
				current_tasks, ntuples, rate, current_tasks - dispatcher.getFinishedTasks(), current_time));
				// rates.add(new Double(rate));
			}
			previous_tuplecount = current_tuples;
			previous_time = current_time;
		}
	}
}