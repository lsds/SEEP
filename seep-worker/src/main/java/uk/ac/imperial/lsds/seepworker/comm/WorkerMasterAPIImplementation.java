package uk.ac.imperial.lsds.seepworker.comm;

import com.esotericsoftware.kryo.Kryo;

import uk.ac.imperial.lsds.seep.comm.Comm;
import uk.ac.imperial.lsds.seep.comm.Connection;
import uk.ac.imperial.lsds.seep.comm.KryoFactory;
import uk.ac.imperial.lsds.seep.comm.protocol.Command;
import uk.ac.imperial.lsds.seep.comm.protocol.ProtocolCommandFactory;
import uk.ac.imperial.lsds.seepworker.WorkerConfig;

public class WorkerMasterAPIImplementation {

	private Comm comm;
	private Kryo k;
	private int retriesToMaster;
	private int retryBackOffMs;
	
	public WorkerMasterAPIImplementation(Comm comm, WorkerConfig wc){
		this.comm = comm;
		this.k = KryoFactory.buildKryoForMasterWorkerProtocol();
		this.retriesToMaster = wc.getInt(WorkerConfig.MASTER_CONNECTION_RETRIES);
		this.retryBackOffMs = wc.getInt(WorkerConfig.MASTER_RETRY_BACKOFF_MS);
	}
	
	public void bootstrap(Connection masterConn, String myIp, int myPort){		
		Command command = ProtocolCommandFactory.buildBootstrapCommand(myIp, myPort);
		
		for (int i = 0; i < retriesToMaster; i++) {
			System.out.println("sending bootstrap, attemps: "+i);
			boolean success = comm.send_object_async(command, masterConn, k);
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
