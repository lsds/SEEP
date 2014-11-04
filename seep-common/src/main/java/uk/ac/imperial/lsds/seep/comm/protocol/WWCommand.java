package uk.ac.imperial.lsds.seep.comm.protocol;

public class WWCommand {

	private short type;
	
	private AckCommand ac;
	private CrashCommand cc;
	
	public WWCommand(){}
	
	public WWCommand(CommandType ct){
		short type = ct.type();
		this.type = type;
		if(type == WWProtocolAPI.ACK.type()){
			this.ac = (AckCommand)ct;
		}
		else if(type == WWProtocolAPI.CRASH.type()){
			this.cc = (CrashCommand)ct;
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
	
	public AckCommand getAckCommand(){
		return ac;
	}
	
	public CrashCommand getCrashCommand(){
		return cc;
	}
}

