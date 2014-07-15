package uk.ac.imperial.lsds.seep.operator.compose.subquery;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.imperial.lsds.seep.operator.compose.multi.ISubQueryBufferHandler;
import uk.ac.imperial.lsds.seep.operator.compose.multi.IUpstreamSubQueryBufferHandler;
import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOperator;
import uk.ac.imperial.lsds.seep.operator.compose.multi.SubQueryBufferHandler;

public class SubQueryConnectable implements ISubQueryConnectable {

	private boolean mostDownstream;
	private boolean mostUpstream;
	private MultiOperator parent;
	
	private Map<Integer, ISubQueryConnectable> localDownstream;
	private Map<Integer, ISubQueryConnectable> localUpstream;

	private SubQuery sq;

	private  Set<ISubQueryBufferHandler> downstreamBuffers;
	private  Set<IUpstreamSubQueryBufferHandler> upstreamBuffers;
	

	public SubQueryConnectable() {
		this.localDownstream = new HashMap<>();
		this.localUpstream = new HashMap<>();
		this.downstreamBuffers = new HashSet<>();
		this.upstreamBuffers = new HashSet<>();
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
			IUpstreamSubQueryBufferHandler handler = new SubQueryBufferHandler(this, so);
			this.addLocalDownstreamBufferHandler(handler);
			so.addLocalUpstreamBufferHandler(handler);
		}
	}

	@Override
	public Set<ISubQueryBufferHandler> getLocalDownstreamBufferHandlers() {
		return this.downstreamBuffers;
	}

	@Override
	public Set<IUpstreamSubQueryBufferHandler> getLocalUpstreamBufferHandlers() {
		return this.upstreamBuffers;
	}

	@Override
	public void addLocalUpstreamBufferHandler(
			IUpstreamSubQueryBufferHandler handler) {
		this.upstreamBuffers.add(handler);
	}

	@Override
	public void addLocalDownstreamBufferHandler(ISubQueryBufferHandler handler) {
		this.downstreamBuffers.add(handler);
	}


}
