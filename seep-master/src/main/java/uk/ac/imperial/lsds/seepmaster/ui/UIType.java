package uk.ac.imperial.lsds.seepmaster.ui;

public enum UIType {
	SIMPLECONSOLE(0), CONSOLE(1), WEB(2);
	
	private int type;
	
	UIType(int type){
		this.type = type;
	}
	
	public int ofType(){
		return type;
	}
	
}
