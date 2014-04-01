package uk.ac.imperial.lsds.seep.operator.compose;

public interface ComposedOperator {

	public int getMultiOpId();
	public int getNumberOfSubOperators();
	public boolean isComposedOperatorStateful();
	
}
