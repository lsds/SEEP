package uk.ac.imperial.lsds.seep.operator.compose;

import java.io.Serializable;

public interface SubOperatorAPI extends Serializable{

	public boolean isMostLocalDownstream();
	public boolean isMostLocalUpstream();
	public void connectSubOperatorTo(int localStreamId, SubOperator so);
	
}
