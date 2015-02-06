package uk.ac.imperial.lsds.seep.multi;

import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import uk.ac.imperial.lsds.seep.multi.join.JoinTaskDispatcher;

public class SubQuery {

	private int					id;

	private Set<MicroOperator>	microOperators				= null;

	private MicroOperator		mostUpstreamMicroOperator	= null;
	private MicroOperator		mostDownstreamMicroOperator	= null;

	private MultiOperator		parent;

	private SubQuery			upstreamSubQuery			= null;
	private SubQuery			downstreamSubQuery			= null;

	private ITaskDispatcher		dispatcher;

	private WindowDefinition	firstWindow;
	private ITupleSchema		firstSchema;

	private WindowDefinition	secondWindow;
	private ITupleSchema		secondSchema;
	
	private QueryConf           queryConf;

	public SubQuery(int id, Set<MicroOperator> microOperators,
			ITupleSchema schema, WindowDefinition window, QueryConf queryConf) {
		this.id = id;
		this.microOperators = microOperators;
		this.firstWindow = window;
		this.firstSchema = schema;
		this.queryConf   = queryConf;

		for (MicroOperator o : this.microOperators) {
			if (o.isMostUpstream())
				mostUpstreamMicroOperator = o;
			if (o.isMostDownstream())
				mostDownstreamMicroOperator = o;
		}

		this.dispatcher = new TaskDispatcher(this);
	}

	public SubQuery(int id, Set<MicroOperator> microOperators,
			ITupleSchema firstSchema, WindowDefinition firstWindow, 
			QueryConf queryConf,
			ITupleSchema secondSchema, WindowDefinition secondWindow) {
		
		this(id, microOperators, firstSchema, firstWindow, queryConf);
		
		this.secondWindow = secondWindow;
		this.secondSchema = secondSchema;

		this.dispatcher = new JoinTaskDispatcher(this);
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

	public ITaskDispatcher getTaskDispatcher() {
		return dispatcher;
	}

	public void setTaskDispatcher(TaskDispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}
	
	/*
	public ConcurrentLinkedQueue<ITask> getExecutorQueue() {
		return this.parent.getExecutorQueue();
	}
	*/
	
	public TaskQueue getExecutorQueue() {
		return this.parent.getExecutorQueue();
	}

//	public ConcurrentLinkedQueue<ITask> getGPUExecutorQueue() {
//		return this.parent.getGPUExecutorQueue();
//	}

	public WindowDefinition getWindowDefinition() {
		return this.firstWindow;
	}

	public ITupleSchema getSchema() {
		return this.firstSchema;
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

	public WindowDefinition getSecondWindowDefinition() {
		return secondWindow;
	}

	public ITupleSchema getSecondSchema() {
		return secondSchema;
	}

	public QueryConf getQueryConf() {
		return queryConf;
	}
}
