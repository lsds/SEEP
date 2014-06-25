package uk.ac.imperial.lsds.seep.operator.compose.subquery;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.OperatorCode;

public class SubQueryTask implements RunnableFuture<List<DataTuple>> {
	
//	List<WindowOperatorCode> operators;
//	IWindowBatch window;
//	
//	public MicroOperatorTask(List<WindowOperatorCode> operators, IWindowBatch window) {
//		this.operators = operators;
//		this.window = window;
//	}

	private DataTuple lastProcessed;
	
	private ISubQueryConnectable subQueryConnectable;
	
	private int logicalOrderID;

	public int getLogicalOrderID() {
		return this.logicalOrderID;
	}
	
	
	public DataTuple getLastProcessed() {
		return this.lastProcessed;
	}
	
	public ISubQueryConnectable getSubQueryConnectable() {
		return this.subQueryConnectable;
	}
	
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<DataTuple> get() throws InterruptedException,
			ExecutionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<DataTuple> get(long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCancelled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDone() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void run() {
//		assert(this.operators.size()>0);
//		
//		Object op1 = this.operators.get(0);
//		
//		// pull
//		if (op1 instanceof WindowOperatorCode)
//			((WindowOperatorCode)op1).processData(this.window, null);
//		else {
//			// push
//			Iterator<DataTuple> iter = this.window.iterator();
//			while (iter.hasNext())
//				((OperatorCode)op1).processData(iter.next(), null);
//		}
	}
	

}
