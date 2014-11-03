package uk.ac.imperial.lsds.seep.comm.protocol;

public enum ProtocolAPI {
	BOOTSTRAP((short)0, new BootstrapCommand()), 
	CRASH((short)1, new CrashCommand()), 
	CODE((short)2, new CodeCommand()), 
	QUERYDEPLOY((short)3, new QueryDeployCommand());
	
	private short type;
	private CommandType c;
	
	ProtocolAPI(short type, CommandType c){
		this.type = type;
		this.c = c;
	}
	
	public short type(){
		return type;
	}
	
	public CommandType clazz(){
		return c;
	}
}
