package uk.ac.imperial.lsds.seepmaster.ui;

import uk.ac.imperial.lsds.seepmaster.infrastructure.master.InfrastructureManager;
import uk.ac.imperial.lsds.seepmaster.query.QueryManager;

public class UIFactory {

	public static UI createUI(UIType uiType, QueryManager qm, InfrastructureManager inf){
		if(uiType == UIType.CONSOLE) {
			return new SimpleConsole(qm, inf);
		}
		return null;
	}
	
}
