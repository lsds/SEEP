package uk.ac.imperial.lsds.seep.comm.protocol;



public class StopQueryCommand implements CommandType {

	public StopQueryCommand(){}
	
	@Override
	public short type() {
		return ProtocolAPI.STOPQUERY.type();
	}

}


