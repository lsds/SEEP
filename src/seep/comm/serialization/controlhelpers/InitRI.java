package seep.comm.serialization.controlhelpers;

import java.util.ArrayList;

public class InitRI {

	private int opId;
	private ArrayList<Integer> index;
	private ArrayList<Integer> key;
	
	public int getOpId() {
		return opId;
	}
	public void setOpId(int opId) {
		this.opId = opId;
	}
	public ArrayList<Integer> getIndex() {
		return index;
	}
	public void setIndex(ArrayList<Integer> index) {
		this.index = index;
	}
	public ArrayList<Integer> getKey() {
		return key;
	}
	public void setKey(ArrayList<Integer> key) {
		this.key = key;
	}

}
