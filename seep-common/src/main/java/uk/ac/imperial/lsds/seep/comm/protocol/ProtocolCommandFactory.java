package uk.ac.imperial.lsds.seep.comm.protocol;

import uk.ac.imperial.lsds.seep.api.PhysicalSeepQuery;

public class ProtocolCommandFactory {
	
	public static Command buildBootstrapCommand(String ip, int port){
		BootstrapCommand bc = new BootstrapCommand(ip, port);
		Command c = new Command(bc);
		return c;
	}
	
	public static Command buildCodeCommand(byte[] data){
		CodeCommand cc = new CodeCommand(data);
		Command c = new Command(cc);
		return c;
	}

	public static Command buildQueryDeployCommand(PhysicalSeepQuery originalQuery) {
		QueryDeployCommand qdc = new QueryDeployCommand(originalQuery);
		Command c = new Command(qdc);
		return c;
	}
	
	public static Command buildStartRuntimeCommand(){
		StartRuntimeCommand src = new StartRuntimeCommand();
		Command c = new Command(src);
		return c;
	}
	
	public static Command buildStartQueryCommand(){
		StartQueryCommand sqc = new StartQueryCommand();
		Command c = new Command(sqc);
		return c;
	}
	
	public static Command buildStopQueryCommand(){
		StopQueryCommand sqc = new StopQueryCommand();
		Command c = new Command(sqc);
		return c;
	}
	
}
