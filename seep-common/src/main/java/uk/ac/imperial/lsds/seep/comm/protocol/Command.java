package uk.ac.imperial.lsds.seep.comm.protocol;

public class Command {

	private short type;
	
	private BootstrapCommand bc;
	private CrashCommand cc;
	private CodeCommand coc;
	private QueryDeployCommand qdc;
	private StartRuntimeCommand src;
	private StartQueryCommand sqc;
	private StopQueryCommand stqc;
	
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
		else if(type == ProtocolAPI.STARTRUNTIME.type()){
			this.src = (StartRuntimeCommand)ct;
		}
		else if(type == ProtocolAPI.STARTQUERY.type()){
			this.sqc = (StartQueryCommand)ct;
		}
		else if(type == ProtocolAPI.STOPQUERY.type()){
			this.stqc = (StopQueryCommand)ct;
		}
		else{
			try {
				throw new Exception("NOT DEFINED CLASS HERE !!!");
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("ERROR: "+e.getMessage());
			}
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
	
	public StartRuntimeCommand getStartRuntimeCommand(){
		return src;
	}
	
	public StartQueryCommand getStartQueryCommand(){
		return sqc;
	}
	
	public StopQueryCommand getStopQueryCommand(){
		return stqc;
	}
}
