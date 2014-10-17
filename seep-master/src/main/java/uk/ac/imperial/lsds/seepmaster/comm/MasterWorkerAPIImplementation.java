package uk.ac.imperial.lsds.seepmaster.comm;

import java.net.InetAddress;
import java.net.UnknownHostException;

import uk.ac.imperial.lsds.seep.infrastructure.Node;
import uk.ac.imperial.lsds.seepmaster.infrastructure.master.InfrastructureManager;
import uk.ac.imperial.lsds.seepmaster.query.QueryManager;

public class MasterWorkerAPIImplementation {

	private QueryManager qm;
	private InfrastructureManager inf;
	
	public MasterWorkerAPIImplementation(QueryManager qm, InfrastructureManager inf){
		this.qm = qm;
		this.inf = inf;
	}
	
	private void bootstrapCommand(String ip, int port) throws UnknownHostException{
//		InetAddress bootIp = InetAddress.getByName(ip);
//		Node n = new Node(bootIp, port);
//		//add node to the stack
//		inf.addNode(n);
//		if(inf.isSystemRunning()){
//			byte[] data = inf.getDataFromFile(inf.getPathToQueryDefinition());
//			LOG.debug("-> Sending code to recently added worker");
//			inf.sendCode(n, data);
//		}
	}
	
}
