package uk.ac.imperial.lsds.seepmaster.comm;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seepmaster.infrastructure.master.ExecutionUnit;
import uk.ac.imperial.lsds.seepmaster.infrastructure.master.InfrastructureManager;
import uk.ac.imperial.lsds.seepmaster.query.QueryManager;


public class MasterWorkerAPIImplementation {

	final private Logger LOG = LoggerFactory.getLogger(MasterWorkerAPIImplementation.class.getName());
	
	private QueryManager qm;
	private InfrastructureManager inf;
	
	public MasterWorkerAPIImplementation(QueryManager qm, InfrastructureManager inf) {
		this.qm = qm;
		this.inf = inf;
	}
	
//	public void bootstrapCommand(Map<String, String> arguments) throws UnknownHostException {
//		InetAddress bootIp = InetAddress.getByName(arguments.get(BootstrapCommand.Arguments.IP.argName()));
//		int port = new Integer(arguments.get(BootstrapCommand.Arguments.PORT.argName()));
//		// Create execution unit of the necessary type, i.e. the one inf knows how to handle
//		ExecutionUnit eu = inf.buildExecutionUnit(bootIp, port);
//		inf.addExecutionUnit(eu);
//		
////		Node n = new Node(bootIp, port);
////		//add node to the stack
////		inf.addNode(n);
////		if(inf.isSystemRunning()){
////			byte[] data = inf.getDataFromFile(inf.getPathToQueryDefinition());
////			LOG.debug("-> Sending code to recently added worker");
////			inf.sendCode(n, data);
////		}
//	}
	
	public void bootstrapCommand(uk.ac.imperial.lsds.seep.comm.protocol.BootstrapCommand bc){
		InetAddress bootIp = null;
		try {
			String ipStr = bc.getIp();
			bootIp = InetAddress.getByName(ipStr);
		} 
		catch (UnknownHostException e) {
			e.printStackTrace();
		}
		int port = bc.getPort();
		LOG.info("New worker node in {}:{}", bootIp.toString(), port);
		ExecutionUnit eu = inf.buildExecutionUnit(bootIp, port);
		inf.addExecutionUnit(eu);
	}
	
}
