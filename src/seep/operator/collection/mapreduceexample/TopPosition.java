package seep.operator.collection.mapreduceexample;

import java.io.Serializable;

public class TopPosition implements Serializable{
	
	private static final long serialVersionUID = 1L;
	public String countryCode = null;
	public int visits = 0;
	
	public TopPosition(String countryCode, int visits){
		this.countryCode = countryCode;
		this.visits = visits;
	}
	
	public TopPosition(){}
}
