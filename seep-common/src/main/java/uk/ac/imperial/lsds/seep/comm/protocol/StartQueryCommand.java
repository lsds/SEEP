package uk.ac.imperial.lsds.seep.comm.protocol;


public class StartQueryCommand implements CommandType {

	public StartQueryCommand(){}
	
	@Override
	public short type() {
		return ProtocolAPI.STARTQUERY.type();
	}

}

