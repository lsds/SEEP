package uk.ac.imperial.lsds.seep.multi;

import java.util.Set;

import uk.ac.imperial.lsds.seep.multi.join.JoinTaskDispatcher;

public class SubQuery {
	
	/* Since we implement an N-way join as a series of binary joins,
	 * the maximum number of upstream operators should be 2.  
	 */
	private static final int _max_upstream_subqueries = 2;
	
	private static final int _max_downstream_subqueries = 2;
	
	private long timestampReference = 0L;
	
	private int   freeUpstreamIdx = 0;
	private int freeDownstreamIdx = 0;
	
	private int id;

	private Set<MicroOperator> microOperators = null;

	private MicroOperator   mostUpstreamMicroOperator = null;
	private MicroOperator mostDownstreamMicroOperator = null;

	private MultiOperator parent;

	private SubQuery []   upstreamSubQuery = null;
	private SubQuery [] downstreamSubQuery = null;

	private ITaskDispatcher dispatcher;

	private WindowDefinition firstWindow;
	private ITupleSchema firstSchema;

	private WindowDefinition secondWindow;
	private ITupleSchema secondSchema;
	
	private QueryConf queryConf;
	
	private LatencyMonitor latencyMonitor;

	private boolean isLeft = false;
	
	public SubQuery (
			int id, 
			Set<MicroOperator> microOperators, 
			ITupleSchema schema, 
			WindowDefinition window, 
			QueryConf queryConf) {
		
		this(id, microOperators, schema, window, queryConf, 0L);
	}
	
	public SubQuery (
			int id, 
			Set<MicroOperator> microOperators, 
			ITupleSchema schema, 
			WindowDefinition window, 
			QueryConf queryConf,
			long timestampReference) {
		
		this.id = id;
		
		this.microOperators = microOperators;
		
		this.firstWindow = window;
		this.firstSchema = schema;
		
		this.queryConf = queryConf;

		for (MicroOperator op: this.microOperators) {
			if (op.isMostUpstream())
				mostUpstreamMicroOperator = op;
			if (op.isMostDownstream())
				mostDownstreamMicroOperator = op;
		}
		
		this.upstreamSubQuery   = new SubQuery[  _max_upstream_subqueries];
		this.downstreamSubQuery = new SubQuery[_max_downstream_subqueries];
		
		for (int i = 0; i < _max_upstream_subqueries; i++)
			this.upstreamSubQuery[i] = null;
		
		for (int i = 0; i < _max_downstream_subqueries; i++)
			this.downstreamSubQuery[i] = null;
		
		this.timestampReference = timestampReference;
		this.latencyMonitor = new LatencyMonitor(this.timestampReference);
		if (! Utils.LATENCY_ON)
			this.latencyMonitor.disable();
		
		this.dispatcher = new TaskDispatcher(this);
	}
	
	public SubQuery(
			int id, 
			Set<MicroOperator> microOperators,
			ITupleSchema firstSchema, 
			WindowDefinition firstWindow, 
			QueryConf queryConf,
			ITupleSchema secondSchema, 
			WindowDefinition secondWindow) {
		
		this(id, microOperators, firstSchema, firstWindow, queryConf, secondSchema, secondWindow, 0L);
	}
	
	public SubQuery(
			int id, 
			Set<MicroOperator> microOperators,
			ITupleSchema firstSchema, 
			WindowDefinition firstWindow, 
			QueryConf queryConf,
			ITupleSchema secondSchema, 
			WindowDefinition secondWindow,
			long timestampReference) {
		
		this.id = id;
		
		this.microOperators = microOperators;
		
		this.firstWindow = firstWindow;
		this.firstSchema = firstSchema;
		
		this.queryConf = queryConf;

		for (MicroOperator op: this.microOperators) {
			if (op.isMostUpstream())
				mostUpstreamMicroOperator = op;
			if (op.isMostDownstream())
				mostDownstreamMicroOperator = op;
		}
		
		this.upstreamSubQuery   = new SubQuery[  _max_upstream_subqueries];
		this.downstreamSubQuery = new SubQuery[_max_downstream_subqueries];
		
		for (int i = 0; i < _max_upstream_subqueries; i++)
			this.upstreamSubQuery[i] = null;
		
		for (int i = 0; i < _max_downstream_subqueries; i++)
			this.downstreamSubQuery[i] = null;
		
		this.secondWindow = secondWindow;
		this.secondSchema = secondSchema;
		
		this.timestampReference = timestampReference;
		this.latencyMonitor = new LatencyMonitor(this.timestampReference);
		if (! Utils.LATENCY_ON)
			this.latencyMonitor.disable();
		
		this.dispatcher = new JoinTaskDispatcher(this);
	}

	public int getId() {
		
		return this.id;
	}

	public boolean isMostUpstream () {
		
		return (this.upstreamSubQuery[0] == null);
		/* Or, freeUpstreamIdx == 0 */
	}

	public boolean isMostDownstream () {
		
		return (this.downstreamSubQuery[0] == null);
		/* Or, freeDownstreamIdx == 0 */
	}
	
	public MicroOperator getMostUpstreamMicroOperator() {
		
		return this.mostUpstreamMicroOperator;
	}

	public MicroOperator getMostDownstreamMicroOperator() {
		
		return this.mostDownstreamMicroOperator;
	}

	public MultiOperator getParent () {
		
		return this.parent;
	}

	public void setParent (MultiOperator parent) {
		
		this.parent = parent;
	}

	public ITaskDispatcher getTaskDispatcher() {
		
		return this.dispatcher;
	}

	public void setTaskDispatcher(TaskDispatcher dispatcher) {
		
		this.dispatcher = dispatcher;
	}
	
	public TaskQueue getExecutorQueue() {
		
		return this.parent.getExecutorQueue();
	}

	public WindowDefinition getWindowDefinition() {
		
		return this.firstWindow;
	}

	public ITupleSchema getSchema() {
		
		return this.firstSchema;
	}

	public void setup() {
		
		this.dispatcher.setup();
	}

	public void connectTo (int localStreamId, SubQuery sq) {
		
		this.downstreamSubQuery [freeDownstreamIdx++] = sq;
		sq.setUpstreamSubQuery (this);
	}

	public SubQuery getUpstreamSubQuery () {
		
		return this.upstreamSubQuery[0];
	}
	
	public SubQuery getUpstreamSubQuery (int idx) {
		
		return this.upstreamSubQuery[idx];
	}
	
	public void setUpstreamSubQuery (SubQuery sq) {
		
		/* If this is the first upstream subquery that we 
		 * register, then set it to be the first one (out
		 * of the two in a two-way join).
		 */
		if (freeUpstreamIdx == 0)
			sq.setLeft(true);
		
		this.upstreamSubQuery[freeUpstreamIdx++] = sq;
	}
	
	private void setLeft(boolean isLeft) {
		
		this.isLeft = isLeft;
	}

	public int getNumberOfUpstreamSubQueries () {
		
		return this.freeUpstreamIdx;
	}

	public SubQuery getDownstreamSubQuery () {
		
		return this.downstreamSubQuery[0];
	}
	
	public SubQuery getDownstreamSubQuery (int idx) {
		
		return this.downstreamSubQuery[idx];
	}
	
	public void setDownstreamSubQuery (SubQuery sq) {
		
		this.downstreamSubQuery[freeDownstreamIdx++] = sq;
	}
	
	public int getNumberOfDownstreamSubQueries () {
		
		return this.freeDownstreamIdx;
	}

	public WindowDefinition getSecondWindowDefinition() {
		
		return this.secondWindow;
	}

	public ITupleSchema getSecondSchema() {
		
		return this.secondSchema;
	}

	public QueryConf getQueryConf() {
		
		return this.queryConf;
	}

	public LatencyMonitor getLatencyMonitor() {
		
		return this.latencyMonitor;
	}

	public boolean isLeft() {
		
		return this.isLeft;
	}
}
