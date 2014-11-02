package uk.ac.imperial.lsds.seepmaster.ui;

import uk.ac.imperial.lsds.seepmaster.infrastructure.master.InfrastructureManager;
import uk.ac.imperial.lsds.seepmaster.query.QueryManager;

public class UIFactory {

	public static UI createUI(int uiType, QueryManager qm, InfrastructureManager inf){
		if(uiType == UIType.SIMPLECONSOLE.ofType()) {
			return new SimpleConsole(qm, inf);
		}
		return null;
	}
	
}
