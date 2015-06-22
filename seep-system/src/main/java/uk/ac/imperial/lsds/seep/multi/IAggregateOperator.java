package uk.ac.imperial.lsds.seep.multi;

public interface IAggregateOperator {

	public boolean hasGroupBy ();
	
	public ITupleSchema getOutputSchema ();
	
	public int getKeyLength ();
	
	public int numberOfValues ();
	
	public AggregationType getAggregateType ();
}
