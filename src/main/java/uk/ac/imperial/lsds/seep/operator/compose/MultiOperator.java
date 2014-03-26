package uk.ac.imperial.lsds.seep.operator.compose;

import java.util.ArrayList;
import java.util.Set;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.OperatorCode;

public class MultiOperator implements OperatorCode, ComposedOperator{

	private static final long serialVersionUID = 1L;

	private final int id;
	
	private Set<SubOperator> subOperators;
	private SubOperator mostUpstream;
	private SubOperator mostDownstream;
	
	private MultiOperator(Set<SubOperator> subOperators, int multiOpId){
		this.id = multiOpId;
		if(checkConstraints(subOperators)){
			this.subOperators = subOperators;
		}
		else{
			//TODO throw error
		}
	}
	
	private boolean checkConstraints(Set<SubOperator> subOperators){
		// TODO:
		// - constains:
		// - 1 single upstream and 1 single downstream
		// - ...
		return true;
	}
	
	/** Implementation of OperatorCode interface **/
	
	@Override
	public void processData(DataTuple data) {
		// TODO Grab mostUpstream and execute, get data back and call its downstream... and so on
		// Once the next downstream is mostDownstream, grab data and make use of api in OperatorCode to
		// forward the data to the next node
	}

	@Override
	public void processData(ArrayList<DataTuple> dataList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setUp() {
		// TODO Auto-generated method stub
		
	}

	/** Implementation of ComposedOperator interface **/

	public int getMultiOpId(){
		return id;
	}
	
	public static MultiOperator synthesizeFrom(Set<SubOperator> subOperators, int multiOpId){
		return new MultiOperator(subOperators, multiOpId);
	}
	
	@Override
	public int getNumberOfSubOperators() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isComposedOperatorStateful() {
		// TODO Auto-generated method stub
		return false;
	}

}