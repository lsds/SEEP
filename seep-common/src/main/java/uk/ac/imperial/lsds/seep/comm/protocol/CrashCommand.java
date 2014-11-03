package uk.ac.imperial.lsds.seep.comm.protocol;

public class CrashCommand implements CommandType {
	
	public CrashCommand(){}
	
	@Override
	public short type() {
		return ProtocolAPI.CRASH.type();
	}

}
