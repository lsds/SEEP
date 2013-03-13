package seep.infrastructure;

import seep.operator.Operator;

public class ScaleOutIntentBean {

	private Operator opToScaleOut;
	private int newOpId;
	private Node newProvisionedNode;
	
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
	
	public ScaleOutIntentBean(Operator opToScaleOut, int newOpId, Node newProvisionedNode){
		this.opToScaleOut = opToScaleOut;
		this.newOpId = newOpId;
		this.newProvisionedNode = newProvisionedNode;
	}
	
}
