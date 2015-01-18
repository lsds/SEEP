package uk.ac.imperial.lsds.java2sdg.bricks;

import soot.Type;

public class Variable {

	private final Type type;
	private final String name;
	
	private Variable(Type type, String name){
		this.type = type;
		this.name = name;
	}
	
	public static Variable var(Type type2, String name){
		return new Variable(type2, name);
	}
	
	public Type getRawType(){
		return type;
	}
	
	public String getType(){
		return type.toString();
	}
	
	public String getName(){
		return name;
	}
	
}
