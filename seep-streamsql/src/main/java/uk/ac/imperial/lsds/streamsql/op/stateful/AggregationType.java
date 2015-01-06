package uk.ac.imperial.lsds.streamsql.op.stateful;

public enum AggregationType {

	MAX, MIN, COUNT, SUM, AVG;

	public static AggregationType fromString(String s) {
		if (s.toLowerCase().contains("avg"))
			return AVG;
		else if (s.toLowerCase().contains("sum"))
			return SUM;
		else if (s.toLowerCase().contains("count"))
			return COUNT;
		else if (s.toLowerCase().contains("min"))
			return MIN;
		else if (s.toLowerCase().contains("max"))
			return MAX;
		else 
			return null;
	}

	public String asString(String s) {
		return this.toString() + "(" + s + ")";
	}

}
