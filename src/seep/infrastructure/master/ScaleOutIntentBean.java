package seep.infrastructure.master;

import seep.operator.Operator;

public class ScaleOutIntentBean {

	private Operator opToScaleOut;
	private int newOpId;
	private Node newProvisionedNode;
	private Operator newInstantiation;
	
	public Operator getOpToScaleOut() {
		return opToScaleOut;
	}

	public void setOpToScaleOut(Operator opToScaleOut) {
		this.opToScaleOut = opToScaleOut;
	}

	public int getNewOpId() {
		return newOpId;
	}

	public void setNewOpId(int newOpId) {
		this.newOpId = newOpId;
	}

	public Node getNewProvisionedNode() {
		return newProvisionedNode;
	}

	public void setNewProvisionedNode(Node newProvisionedNode) {
		this.newProvisionedNode = newProvisionedNode;
	}
	
	public Operator getNewOperatorInstantiation(){
		return newInstantiation;
	}
	
	public void setNewReplicaInstantiation(Operator newInstantiation){
		this.newInstantiation = newInstantiation;
	}
	
	public ScaleOutIntentBean(Operator opToScaleOut, int newOpId, Node newProvisionedNode){
		this.opToScaleOut = opToScaleOut;
		this.newOpId = newOpId;
		this.newProvisionedNode = newProvisionedNode;
	}
	
	@Override
	public String toString(){
		return "OP: "+opToScaleOut.getOperatorId()+" scales to new OP-id: "+newOpId;
	}
	
}
