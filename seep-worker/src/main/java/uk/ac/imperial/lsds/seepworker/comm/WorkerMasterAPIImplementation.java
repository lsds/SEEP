package uk.ac.imperial.lsds.seepworker.comm;

import uk.ac.imperial.lsds.seep.comm.BootstrapCommand;
import uk.ac.imperial.lsds.seep.comm.Comm;
import uk.ac.imperial.lsds.seep.comm.Connection;
import uk.ac.imperial.lsds.seepworker.WorkerConfig;

public class WorkerMasterAPIImplementation {

	private Comm comm;
	private int retriesToMaster;
	private int retryBackOffMs;
	
	public WorkerMasterAPIImplementation(Comm comm, WorkerConfig wc){
		this.comm = comm;
		this.retriesToMaster = wc.getInt(WorkerConfig.MASTER_CONNECTION_RETRIES);
		this.retryBackOffMs = wc.getInt(WorkerConfig.MASTER_RETRY_BACKOFF_MS);
	}
	
	public void bootstrap(Connection masterConn, String myIp, int myPort){
		String command = BootstrapCommand.buildBootstrapCommand(myIp, myPort);
		for (int i = 0; i < retriesToMaster; i++) {
			System.out.println("sending bootstrap, attemps: "+i);
			boolean success = comm.send_object_sync(command, masterConn);
			if(success){
				System.out.println("conn success");
				return;
			}
			try {
				Thread.sleep(retryBackOffMs);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		// TODO: throw exception here to indicate failure
	}
	
}
