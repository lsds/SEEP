package uk.ac.imperial.lsds.seep.operator.compose.micro;

import java.util.Map;

import uk.ac.imperial.lsds.seep.operator.compose.subquery.SubQuery;

public class MicroOperatorConnectable implements IMicroOperatorConnectable {

	public MicroOperatorConnectable(MicroOperator op) {
		
	}
	
	@Override
	public IMicroOperatorCode getMicroOperator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setParentSubQuery(SubQuery parent) {
		// TODO Auto-generated method stub

	}

	@Override
	public SubQuery getParentSubQuery() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isMostLocalDownstream() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isMostLocalUpstream() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void connectTo(int localStreamId, IMicroOperatorConnectable so) {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<Integer, IMicroOperatorConnectable> getLocalDownstream() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Integer, IMicroOperatorConnectable> getLocalUpstream() {
		// TODO Auto-generated method stub
		return null;
	}

}
