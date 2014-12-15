package uk.ac.imperial.lsds.seep.multi;

import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SubQuery {

	private int id;

	private Set<MicroOperator> microOperators = null;

	private MicroOperator mostUpstreamMicroOperator = null;
	private MicroOperator mostDownstreamMicroOperator = null;

	private MultiOperator parent;

	private SubQuery upstreamSubQuery = null;
	private SubQuery downstreamSubQuery = null;

	private TaskDispatcher dispatcher;

	private WindowDefinition window;
	private ITupleSchema schema;

	public SubQuery(int id, Set<MicroOperator> microOperators,
			ITupleSchema schema, WindowDefinition window) {
		this.id = id;
		this.microOperators = microOperators;
		this.window = window;
		this.schema = schema;

		for (MicroOperator o : this.microOperators) {
			if (o.isMostUpstream())
				mostUpstreamMicroOperator = o;
			if (o.isMostDownstream())
				mostDownstreamMicroOperator = o;
		}

		this.dispatcher = new TaskDispatcher(this);
	}

	public int getId() {
		return this.id;
	}

	public boolean isMostUpstream() {
		return (this.upstreamSubQuery == null);
	}

	public boolean isMostDownstream() {
		return (this.downstreamSubQuery == null);
	}

	public MicroOperator getMostUpstreamMicroOperator() {
		return this.mostUpstreamMicroOperator;
	}

	public MicroOperator getMostDownstreamMicroOperator() {
		return this.mostDownstreamMicroOperator;
	}

	public MultiOperator getParent() {
		return parent;
	}

	public void setParent(MultiOperator parent) {
		this.parent = parent;
	}

	public TaskDispatcher getTaskDispatcher() {
		return dispatcher;
	}

	public void setTaskDispatcher(TaskDispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}

	public ConcurrentLinkedQueue<Task> getExecutorQueue() {
		return this.parent.getExecutorQueue();
	}
	
	public ConcurrentLinkedQueue<Task> getGPUExecutorQueue() {
		return this.parent.getGPUExecutorQueue();
	}
	
	public WindowDefinition getWindowDefinition() {
		return this.window;
	}

	public ITupleSchema getSchema() {
		return this.schema;
	}

	public void setup() {
		this.dispatcher.setup();
	}

	public void connectTo(int localStreamId, SubQuery sb) {
		this.downstreamSubQuery = sb;
		sb.setUpstreamSubQuery(this);
	}

	public SubQuery getUpstreamSubQuery() {
		return upstreamSubQuery;
	}

	public void setUpstreamSubQuery(SubQuery upstreamSubQuery) {
		this.upstreamSubQuery = upstreamSubQuery;
	}

	public SubQuery getDownstreamSubQuery() {
		return downstreamSubQuery;
	}

	public void setDownstreamSubQuery(SubQuery downstreamSubQuery) {
		this.downstreamSubQuery = downstreamSubQuery;
	}
}
