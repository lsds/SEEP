package seep.operator.collection.mapreduceexample;

import java.io.Serializable;

public class TopPosition implements Serializable{
	
	private static final long serialVersionUID = 1L;
	public String countryCodeString = null;
	public int visits = 0;
	
	public TopPosition(String countryCodeString, int visits){
		this.countryCodeString = countryCodeString;
		this.visits = visits;
	}
	
	public TopPosition(){}
}
