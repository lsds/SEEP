package uk.ac.imperial.lsds.seep.operator.compose.micro;

public class MicroOperator {

	private int opID;
	
	private IMicroOperatorCode op;
	
	private MicroOperator(IMicroOperatorCode op, int opId) {
		this.op = op;
		this.opID = opId;
	}
	
	public static MicroOperator newMicroOperator(IMicroOperatorCode op, int opId) {
		return new MicroOperator(op, opId);
	}

	public int getOpID() {
		return opID;
	}

	public IMicroOperatorCode getOp() {
		return op;
	}
	
}
