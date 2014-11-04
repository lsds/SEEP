package uk.ac.imperial.lsds.seepmaster.ui;

import uk.ac.imperial.lsds.seepmaster.infrastructure.master.InfrastructureManager;
import uk.ac.imperial.lsds.seepmaster.query.QueryManager;

public class UIFactory {

	public static String nameUIOfType(int uiType){
		String name = null;
		if(uiType == UIType.SIMPLECONSOLE.ofType()) {
			name = UIType.SIMPLECONSOLE.name();
		}
		return name;
	}
	
	public static UI createUI(int uiType, QueryManager qm, InfrastructureManager inf){
		if(uiType == UIType.SIMPLECONSOLE.ofType()) {
			return new SimpleConsoleUI(qm, inf);
		}
		return null;
	}
	
}
