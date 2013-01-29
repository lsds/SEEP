package seep.runtimeengine;

import java.util.ArrayList;

import seep.comm.serialization.DataTuple;

public class DataConsumer implements Runnable {

	private CoreRE owner;
//	private InputQueue inputQueue;
	private DataStructureAdapter dataAdapter;
	private boolean doWork = true;
	private boolean block = false;
	
	public void setDoWork(boolean doWork){
		this.doWork = doWork;
	}
	
	public DataConsumer(CoreRE owner, DataStructureAdapter dataAdapter){
		this.owner = owner;
		this.dataAdapter = dataAdapter;
	}

	@Override
	public void run() {
		int mode = 0;
		if(dataAdapter.getDSO() instanceof InputQueue){
			mode = 1;
		}
		else if(dataAdapter.getDSO() instanceof Barrier){
			mode = 2;
		}
		while(doWork){
			if(mode == 1){
				DataTuple data = dataAdapter.pull();
				if(owner.checkSystemStatus()){
					owner.forwardData(data);
				}
			}
			else if(mode == 2){
//				System.out.println("### Yes, im in mode2, so using the barrier...");
				ArrayList<DataTuple> ldata = dataAdapter.pullBarrier();
//				System.out.println("### Unblocked, got the data");
				if(owner.checkSystemStatus()){
					owner.forwardData(ldata);
				}
			}
		}
	}
}
