package uk.ac.imperial.lsds.seep.multi;

public enum AttributeType { 
	INT(1), 
	FLOAT(2), 
	LONG(4); 
	
	int type;
	private AttributeType(int i) { type = i; }
}