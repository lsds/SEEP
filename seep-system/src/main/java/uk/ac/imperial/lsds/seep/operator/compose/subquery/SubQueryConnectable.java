package uk.ac.imperial.lsds.seep.operator.compose.subquery;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.imperial.lsds.seep.operator.compose.multi.ISubQueryTaskResultForwarder;
import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOperator;
import uk.ac.imperial.lsds.seep.operator.compose.multi.SubQueryBufferWindowWrapper;
import uk.ac.imperial.lsds.seep.operator.compose.multi.SubQueryTaskDispatcher;
import uk.ac.imperial.lsds.seep.operator.compose.multi.SubQueryTaskResultBufferForwarder;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowDefinition;

public class SubQueryConnectable implements ISubQueryConnectable {

	private boolean mostDownstream;
	private boolean mostUpstream;
	private MultiOperator parent;
	
	private SubQuery sq;
	private Map<Integer, IWindowDefinition>  windowDefinitions;
	private SubQueryTaskDispatcher taskDispatcher;
	private Set<ISubQueryTaskResultForwarder> resultForwarders;
	
	private Map<Integer, SubQueryBufferWindowWrapper> localDownstreamBuffers;
	private Map<Integer, SubQueryBufferWindowWrapper> localUpstreamBuffers;
	
	public SubQueryConnectable() {
		this.localDownstreamBuffers = new HashMap<>();
		this.localUpstreamBuffers = new HashMap<>();
		this.mostDownstream = true;
		this.mostUpstream = true;
		
		this.resultForwarders = new HashSet<>();
		this.taskDispatcher = new SubQueryTaskDispatcher(this);

	}

	public SubQueryConnectable(SubQuery sq, Map<Integer, IWindowDefinition> windowDefs) {
		this();
		this.sq = sq;
		sq.setParent(this);
		this.windowDefinitions = windowDefs;
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
	public SubQuery getSubQuery() {
		return this.sq;
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
		SubQueryBufferWindowWrapper bw = new SubQueryBufferWindowWrapper(so, streamID);
		this.registerLocalDownstreamBuffer(bw, streamID);
		so.registerLocalUpstreamBuffer(bw, streamID);
		this.resultForwarders.add(new SubQueryTaskResultBufferForwarder(bw));
	}
	
	@Override
	public Set<ISubQueryTaskResultForwarder> getResultForwarders() {
		return resultForwarders;
	}

	@Override
	public void addResultForwarder(ISubQueryTaskResultForwarder forwarder) {
		this.resultForwarders.add(forwarder);
	}

	@Override
	public Map<Integer, SubQueryBufferWindowWrapper> getLocalDownstreamBuffers() {
		return this.localDownstreamBuffers;
	}

	@Override
	public Map<Integer, SubQueryBufferWindowWrapper> getLocalUpstreamBuffers() {
		return this.localUpstreamBuffers;
	}

	@Override
	public void registerLocalUpstreamBuffer(SubQueryBufferWindowWrapper so, int streamID) {
		this.mostUpstream = false;
		this.localUpstreamBuffers.put(streamID, so);
	}

	@Override
	public void registerLocalDownstreamBuffer(SubQueryBufferWindowWrapper so, int streamID) {
		this.mostDownstream = false;
		this.localDownstreamBuffers.put(streamID, so);
	}

//	@Override
//	public void processData(MultiOpTuple tuple) {
//		for (SubQueryBufferWindowWrapper bw : this.localUpstreamBuffers.values()) {
//			while (!bw.addToBuffer(tuple)) {
//				try {
//					synchronized (bw.getExternalBufferLock()) {
//						bw.getExternalBufferLock().wait();
//					}
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//	}

	@Override
	public Map<Integer, IWindowDefinition> getWindowDefinitions() {
		return windowDefinitions;
	}

	@Override
	public SubQueryTaskDispatcher getTaskDispatcher() {
		return taskDispatcher;
	}

	@Override
	public void setUp() {
		this.taskDispatcher.setUp();
	}

	
}
