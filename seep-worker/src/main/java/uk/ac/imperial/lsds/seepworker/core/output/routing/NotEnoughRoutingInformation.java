package uk.ac.imperial.lsds.seepworker.core.output.routing;

import uk.ac.imperial.lsds.seep.errors.SeepException;

public class NotEnoughRoutingInformation extends SeepException {

	private static final long serialVersionUID = 1L;

	public NotEnoughRoutingInformation(String str){
		super(str);
	}
	
}
