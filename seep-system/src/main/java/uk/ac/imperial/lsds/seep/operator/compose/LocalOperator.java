package uk.ac.imperial.lsds.seep.operator.compose;

import java.util.Map;

public class LocalOperator implements LocalConnectable {

	private boolean mostDownstream;
	private boolean mostUpstream;
	private MultiOperator parent;
	
	private Map<Integer, LocalOperator> localDownstream;
	private Map<Integer, LocalOperator> localUpstream;
	
	private MicroOperator mo;

	public LocalOperator(MicroOperator mo) {
		this.mo = mo;
		mo.setParentLocalConnectable(this);
	}
	
	@Override
	public void connectSubOperatorTo(int localStreamId, LocalOperator so){
		this.addLocalDownstream(localStreamId, so);
		so.addLocalUpstream(localStreamId, this);
	}

	@Override
	public boolean isMostLocalDownstream() {
		return mostDownstream;
	}

	@Override
	public boolean isMostLocalUpstream() {
		return mostUpstream;
	}

	private void addLocalDownstream(int localStreamId, LocalOperator so){
		this.mostDownstream = false;
		if(!localDownstream.containsKey(localStreamId)){
			localDownstream.put(localStreamId, so);
		}
		else{
			// TODO: Throw error overwrite?
		}
	}
	
	private void addLocalUpstream(int localStreamId, LocalOperator so){
		this.mostUpstream = false;
		if(!localUpstream.containsKey(localStreamId)){
			localUpstream.put(localStreamId, so);
		}
		else{
			// TODO: Throw error overwrite?
		}
	}

	@Override
	public MicroOperator getMicroOperator() {
		return this.mo;
	}

	@Override
	public Map<Integer, LocalOperator> getLocalDownstream() {
		return localDownstream;
	}

	@Override
	public Map<Integer, LocalOperator> getLocalUpstream() {
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

}
