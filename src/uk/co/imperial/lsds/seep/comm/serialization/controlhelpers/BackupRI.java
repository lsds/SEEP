package uk.co.imperial.lsds.seep.comm.serialization.controlhelpers;

import java.util.ArrayList;

public class BackupRI {

	private int opId;
	private String operatorType;
	private ArrayList<Integer> index;
	private ArrayList<Integer> key;
	
	public BackupRI(){
		
	}
	
	public BackupRI(int opId, ArrayList<Integer> index, ArrayList<Integer> key, String operatorType){
		this.opId = opId;
		this.index = index;
		this.key = key;
		this.operatorType = operatorType;
	}
	
	public int getOpId() {
		return opId;
	}
	public void setOpId(int opId) {
		this.opId = opId;
	}
	public String getOperatorType() {
		return operatorType;
	}
	public void setOperatorType(String operatorType) {
		this.operatorType = operatorType;
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
