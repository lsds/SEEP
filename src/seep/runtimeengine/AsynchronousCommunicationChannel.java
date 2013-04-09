package seep.runtimeengine;

import seep.buffer.Buffer;
import seep.operator.EndPoint;

public class AsynchronousCommunicationChannel implements EndPoint{

	private int opId;
	private Buffer buf;
	
	public AsynchronousCommunicationChannel(int opId, Buffer buf){
		this.opId = opId;
		this.buf = buf;
	}
	
	@Override
	public int getOperatorId() {
		return opId;
	}

}
