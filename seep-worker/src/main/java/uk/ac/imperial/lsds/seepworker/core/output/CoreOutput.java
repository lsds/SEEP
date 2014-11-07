package uk.ac.imperial.lsds.seepworker.core.output;

import java.util.List;


public class CoreOutput {
	
	private List<OutputAdapter> outputAdapters;
		
	public CoreOutput(List<OutputAdapter> outputAdapters){
		this.outputAdapters = outputAdapters;
	}
	
	public List<OutputAdapter> getOutputAdapters(){
		return outputAdapters;
	}	
	
}
