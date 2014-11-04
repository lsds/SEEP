package uk.ac.imperial.lsds.seep.comm.protocol;

public enum ProtocolAPI {
	BOOTSTRAP((short)0, new BootstrapCommand()), 
	CRASH((short)1, new CrashCommand()), 
	CODE((short)2, new CodeCommand()), 
	QUERYDEPLOY((short)3, new QueryDeployCommand()),
	STARTRUNTIME((short)4, new StartRuntimeCommand()),
	STARTQUERY((short)5, new StartQueryCommand()),
	STOPQUERY((short)6, new StopQueryCommand());
	
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
