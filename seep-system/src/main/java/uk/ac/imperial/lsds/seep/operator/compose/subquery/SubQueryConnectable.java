package uk.ac.imperial.lsds.seep.operator.compose.subquery;

import java.util.Map;

import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOperator;

public class SubQueryConnectable implements ISubQueryConnectable {

	private boolean mostDownstream;
	private boolean mostUpstream;
	private MultiOperator parent;
	
	private Map<Integer, ISubQueryConnectable> localDownstream;
	private Map<Integer, ISubQueryConnectable> localUpstream;
	
	private SubQuery sq;

	public SubQueryConnectable(SubQuery sq) {
		this.sq = sq;
		sq.setParentSubQueryConnectable(this);
	}
	

	@Override
	public boolean isMostLocalDownstream() {
		return mostDownstream;
	}

	@Override
	public boolean isMostLocalUpstream() {
		return mostUpstream;
	}

	public void addLocalDownstream(int localStreamId, ISubQueryConnectable so){
		this.mostDownstream = false;
		if(!localDownstream.containsKey(localStreamId)){
			localDownstream.put(localStreamId, so);
		}
		else{
			// TODO: Throw error overwrite?
		}
	}
	
	public void addLocalUpstream(int localStreamId, ISubQueryConnectable so){
		this.mostUpstream = false;
		if(!localUpstream.containsKey(localStreamId)){
			localUpstream.put(localStreamId, so);
		}
		else{
			// TODO: Throw error overwrite?
		}
	}

	@Override
	public SubQuery getSubQuery() {
		return this.sq;
	}

	@Override
	public Map<Integer, ISubQueryConnectable> getLocalDownstream() {
		return localDownstream;
	}

	@Override
	public Map<Integer, ISubQueryConnectable> getLocalUpstream() {
		return localUpstream;
	}

	@Override
	public void setParentMultiOperator(MultiOperator parent) {
		this.parent = parent;
	}

	@Override
	public MultiOperator getParentMultiOperator() {
		return this.parent;
	}

	@Override
	public void connectTo(int localStreamId, ISubQueryConnectable so) {
		this.addLocalDownstream(localStreamId, so);
		so.addLocalUpstream(localStreamId, this);
	}


	@Override
	public void addLocalUpstream(int localStreamId,
			SubQueryConnectable subQueryConnectable) {
		// TODO Auto-generated method stub
		
	}

}
