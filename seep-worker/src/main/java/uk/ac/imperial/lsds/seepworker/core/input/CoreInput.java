package uk.ac.imperial.lsds.seepworker.core.input;

import java.util.List;


public class CoreInput {
	
	private List<InputAdapter> inputAdapters;
	
	public CoreInput(List<InputAdapter> inputAdapters){
		this.inputAdapters = inputAdapters;
	}
	
	public List<InputAdapter> getInputAdapters(){
		return inputAdapters;
	}
	
}
