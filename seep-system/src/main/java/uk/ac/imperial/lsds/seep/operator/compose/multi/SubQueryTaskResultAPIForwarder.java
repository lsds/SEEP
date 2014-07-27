package uk.ac.imperial.lsds.seep.operator.compose.multi;

import uk.ac.imperial.lsds.seep.operator.compose.subquery.ISubQueryConnectable;

public class SubQueryTaskResultAPIForwarder implements ISubQueryTaskResultForwarder {

	private ISubQueryConnectable subQueryConnectable;
//	private Map<String, Integer> idxMapper;

	public SubQueryTaskResultAPIForwarder(ISubQueryConnectable subQueryConnectable) {
		this.subQueryConnectable = subQueryConnectable;
	}

//	public SubQueryTaskResultAPIForwarder(ISubQueryConnectable subQueryConnectable, Map<String, Integer> idxMapper) {
//		this.subQueryConnectable = subQueryConnectable;
//		this.idxMapper = idxMapper;
//	}
	
	@Override
	public void forwardResult(MultiOpTuple[] result) {
		
		
		/*
		 *  Send the result using the API of the parent MultiOperator
		 */
		for (MultiOpTuple tuple : result) {
			this.subQueryConnectable.getParentMultiOperator().getAPI().send(tuple);
//			this.subQueryConnectable.getParentMultiOperator().getAPI().send(tuple.toDataTuple(idxMapper));
		}
	}

}
