package uk.ac.imperial.lsds.seep.comm.protocol;

public class CodeCommand implements CommandType {

	private int dataSize;
	private byte[] data;

	public CodeCommand(){}
	
	public CodeCommand(byte[] data) {
		this.dataSize = data.length;
		this.data = data;
	}

	@Override
	public short type() {
		return ProtocolAPI.CODE.type();
	}
	
	public int getDataSize(){
		return dataSize;
	}
	
	public byte[] getData(){
		return data;
	}
	
}
