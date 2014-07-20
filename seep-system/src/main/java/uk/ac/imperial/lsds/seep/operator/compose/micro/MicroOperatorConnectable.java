package uk.ac.imperial.lsds.seep.operator.compose.micro;

import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.lsds.seep.operator.compose.subquery.SubQuery;

public class MicroOperatorConnectable implements IMicroOperatorConnectable {

	private boolean mostDownstream = true;
	private boolean mostUpstream = true;
	private SubQuery parent;
	
	private Map<Integer, IMicroOperatorConnectable> localDownstream;
	private Map<Integer, IMicroOperatorConnectable> localUpstream;
	
	private MicroOperator sq;

	public MicroOperatorConnectable(MicroOperator sq) {
		this.sq = sq;
		this.localDownstream = new HashMap<>();
		this.localUpstream = new HashMap<>();
	}

	@Override
	public boolean isMostLocalDownstream() {
		return mostDownstream;
	}

	@Override
	public boolean isMostLocalUpstream() {
		return mostUpstream;
	}

	@Override
	public void addLocalDownstream(int localStreamId, IMicroOperatorConnectable so){
		this.mostDownstream = false;
		this.localDownstream.put(localStreamId, so);
	}
	
	@Override
	public void addLocalUpstream(int localStreamId, IMicroOperatorConnectable so){
		this.mostUpstream = false;
		this.localUpstream.put(localStreamId, so);
	}

	@Override
	public MicroOperator getMicroOperator() {
		return this.sq;
	}

	@Override
	public Map<Integer, IMicroOperatorConnectable> getLocalDownstream() {
		return localDownstream;
	}

	@Override
	public Map<Integer, IMicroOperatorConnectable> getLocalUpstream() {
		return localUpstream;
	}

	@Override
	public void setParentSubQuery(SubQuery parent) {
		this.parent = parent;
	}

	@Override
	public SubQuery getParentSubQuery() {
		return this.parent;
	}

	@Override
	public void connectTo(int localStreamId, IMicroOperatorConnectable so) {
		this.addLocalDownstream(localStreamId, so);
		so.addLocalUpstream(localStreamId, this);
	}
}
