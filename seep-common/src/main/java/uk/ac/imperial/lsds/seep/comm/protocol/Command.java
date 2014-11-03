package uk.ac.imperial.lsds.seep.comm.protocol;

public class Command {

	private short type;
	
	private BootstrapCommand bc;
	private CrashCommand cc;
	private CodeCommand coc;
	private QueryDeployCommand qdc;
	
	public Command(){}
	
	public Command(CommandType ct){
		short type = ct.type();
		this.type = type;
		if(type == ProtocolAPI.BOOTSTRAP.type()){
			this.bc = (BootstrapCommand)ct;
		}
		else if(type == ProtocolAPI.CRASH.type()){
			this.cc = (CrashCommand)ct;
		}
		else if(type == ProtocolAPI.CODE.type()){
			this.coc = (CodeCommand)ct;
		}
		else if(type == ProtocolAPI.QUERYDEPLOY.type()){
			this.qdc = (QueryDeployCommand)ct;
		}
		else{
			// TODO: throw error
		}
	}
	
	public short type(){
		return type;
	}
	
	public BootstrapCommand getBootstrapCommand(){
		return bc;
	}
	
	public CrashCommand getCrashCommand(){
		return cc;
	}
	
	public CodeCommand getCodeCommand(){
		return coc;
	}
	
	public QueryDeployCommand getQueryDeployCommand(){
		return qdc;
	}
}
