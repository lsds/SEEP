package uk.ac.imperial.lsds.streamsql.op.stateful;

public enum AggregationType {

	MAX, MIN, COUNT, SUM, AVG;
	
	public String asString(int s) {
		return this.toString() + "(" + s + ")";
	}

}
