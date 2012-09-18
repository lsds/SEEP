package seep.infrastructure;

import java.io.Serializable;

/**
* Location. Location class models the endpoints of a given operator, providing node and port information.
*/

public class OperatorStaticInformation implements Serializable{

	private static final long serialVersionUID = 1L;

	private Node myNode;

	private int inC;
	private int inD;
	
	private boolean isStatefull;

	public Node getMyNode(){
		return myNode;
	}

	public void setMyNode(Node myNode){
		this.myNode = myNode;
	}

	public int getInC(){
		return inC;
	}

	public int getInD(){
		return inD;
	}

	public void setInD(int inD){
		this.inD = inD;
	}
	
	public void setInC(int inC){
		this.inC = inC;
	}
	
	public boolean isStatefull() {
		return isStatefull;
	}

	public void setStatefull(boolean isStatefull) {
		this.isStatefull = isStatefull;
	}

	public OperatorStaticInformation setNode(Node newNode){
		return new OperatorStaticInformation(newNode, inC, inD, isStatefull);
	}
	
	@Override public String toString() {
		return "node: " + myNode + "inC: " + inC + "inD: " + inD;
	}

	public OperatorStaticInformation(Node myNode, int inC, int inD, boolean isStatefull){
		this.myNode = myNode;
		this.inC = inC;
		this.inD = inD;
		this.isStatefull = isStatefull;
	}
}
