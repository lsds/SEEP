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
}
