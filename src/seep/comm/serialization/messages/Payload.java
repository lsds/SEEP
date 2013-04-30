package seep.comm.serialization.messages;

import java.util.ArrayList;

public class Payload extends ArrayList<Object>{

	private static final long serialVersionUID = 1L;

	public Payload(){
		
	}

	public Payload(Object... attrValues){
		super(attrValues.length);
		for(Object o : attrValues){
			add(o);
		}
	}
	
	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("VAL ");
		for(Object o : this){
			sb.append(o+" ");
		}
		return sb.toString();
	}
}