package seep.operator.collection.mapreduceexample;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;

import seep.comm.serialization.controlhelpers.StateI;
//import seep.operator.collection.mapreduceexample.Reduce.TopPosition;

public class TopKReduceState implements StateI {
	//HashMap storing the 5 most visited country codes
	public LinkedList<TopPosition> top = new LinkedList<TopPosition>();
	// integer storing the number of visits of the fifth member in top map
	public int top5Visits = 0;
	//map storing the number of visits per country code
	public HashMap<String, Integer> countryCode = new HashMap<String, Integer>();
	
}
