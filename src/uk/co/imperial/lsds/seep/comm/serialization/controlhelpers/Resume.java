package uk.co.imperial.lsds.seep.comm.serialization.controlhelpers;

import java.util.ArrayList;

public class Resume {

	ArrayList<Integer> opId;

	public Resume(){
		
	}
	
	public Resume(ArrayList<Integer> opId){
		this.opId = opId;
	}
	
	public ArrayList<Integer> getOpId() {
		return opId;
	}

	public void setOpId(ArrayList<Integer> opId) {
		this.opId = opId;
	}
}
