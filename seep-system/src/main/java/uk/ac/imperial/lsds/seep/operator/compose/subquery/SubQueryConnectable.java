package uk.ac.imperial.lsds.seep.operator.compose.subquery;

import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOperator;
import uk.ac.imperial.lsds.seep.operator.compose.multi.SubQueryBuffer;

public class SubQueryConnectable implements ISubQueryConnectable {

	private boolean mostDownstream;
	private boolean mostUpstream;
	private MultiOperator parent;
	
	private Map<Integer, ISubQueryConnectable> localDownstream;
	private Map<Integer, ISubQueryConnectable> localUpstream;

	private SubQuery sq;

	private Map<Integer, SubQueryBuffer> localDownstreamBuffers;
	private Map<Integer, SubQueryBuffer> localUpstreamBuffers;
	
	public SubQueryConnectable() {
		this.localDownstream = new HashMap<>();
		this.localUpstream = new HashMap<>();
		this.localDownstreamBuffers = new HashMap<>();
		this.localUpstreamBuffers = new HashMap<>();
		this.mostDownstream = true;
		this.mostUpstream = true;
	}

	public SubQueryConnectable(SubQuery sq) {
		this();
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

	@Override
	public void addLocalDownstream(ISubQueryConnectable so, int streamID){
		this.mostDownstream = false;
		this.localDownstream.put(streamID,so);
	}
	
	@Override
	public void addLocalUpstream(ISubQueryConnectable so, int streamID){
		this.mostUpstream = false;
		this.localUpstream.put(streamID, so);
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
	public void connectTo(ISubQueryConnectable so, int streamID) {
		this.addLocalDownstream(so, streamID);
		so.addLocalUpstream(this, streamID);
		
		/*
		 * Make sure that only one buffer is created for each downstream 
		 * sub query, even if multiple logical streams are defined
		 */
		if (!this.localDownstream.values().contains(so)) {
			SubQueryBuffer b = new SubQueryBuffer();
			this.localDownstreamBuffers.put(streamID, b);
			so.registerLocalUpstreamBuffer(b, streamID);
		}
	}

	@Override
	public Map<Integer, SubQueryBuffer> getLocalDownstreamBuffers() {
		return this.localDownstreamBuffers;
	}

	@Override
	public Map<Integer, SubQueryBuffer> getLocalUpstreamBuffers() {
		return this.localUpstreamBuffers;
	}

	@Override
	public void registerLocalUpstreamBuffer(SubQueryBuffer so, int streamID) {
		this.localUpstreamBuffers.put(streamID, so);
	}

	@Override
	public void registerLocalDownstreamBuffer(SubQueryBuffer so, int streamID) {
		this.localDownstreamBuffers.put(streamID, so);
	}


}
