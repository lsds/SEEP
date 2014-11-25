package uk.ac.imperial.lsds.seep.multi;

import java.util.Set;
import java.util.concurrent.ExecutorService;


public class SubQuery {
	
	private int id;
	
	private Set<MicroOperator> microOperators;
	
	private MicroOperator mostUpstreamMicroOperator;
	private MicroOperator mostDownstreamMicroOperator;

	private MultiOperator parent;
	
	private SubQuery upstreamSubQuery = null;
	private SubQuery downstreamSubQuery = null;
	
	private TaskDispatcher inputDispatcher;
	
	private WindowDefinition windowDef;
	private ITupleSchema schema;
	
	public SubQuery(int id, Set<MicroOperator> microOperators, ITupleSchema schema, WindowDefinition windowDef) {
		this.id = id;
		this.microOperators = microOperators;
		this.windowDef = windowDef;
		this.schema = schema;
		
		for (MicroOperator o : this.microOperators) {
			if (o.isMostUpstream())
				mostUpstreamMicroOperator = o;
			if (o.isMostDownstream())
				mostDownstreamMicroOperator = o;
		}
		
		this.inputDispatcher = new TaskDispatcher(this);
	}
	
	public int getId() {
		return id;
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

	public TaskDispatcher getInputDispatcher() {
		return inputDispatcher;
	}

	public void setInputDispatcher(TaskDispatcher inputDispatcher) {
		this.inputDispatcher = inputDispatcher;
	}

	public ExecutorService getExecutorService() {
		return this.parent.getExecutorService();
	}

	public WindowDefinition getWindowDefinition() {
		return this.windowDef;
	}

	public ITupleSchema getSchema() {
		return this.schema;
	}

	public void setUp() {
		this.inputDispatcher.setUp();
	}
	
}
