package uk.ac.imperial.lsds.seep.operator.compose.multi;

import java.util.concurrent.BlockingQueue;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.compose.subquery.ISubQueryConnectable;

public class InputBufferHandler implements Runnable {

	private ISubQueryConnectable mostUpstreamSubQuery;
	
	private BlockingQueue<DataTuple> input;
	
	public InputBufferHandler(BlockingQueue<DataTuple> input, ISubQueryConnectable mostUpstreamSubQuery) {
		this.input = input;
		this.mostUpstreamSubQuery = mostUpstreamSubQuery;
	}

	@Override
	public void run() {
		
		while (true) {
			try {
				/*
				 * Take a tuple from the input queue (blocking)
				 */
				DataTuple tuple = this.input.take();
				/*
				 * Get the upstream 
				 */
				mostUpstreamSubQuery.getLocalUpstreamBufferHandlers().iterator().next()
				
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
