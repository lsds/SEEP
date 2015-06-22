package uk.ac.imperial.lsds.seep.multi;


public class MicroOperator {

	private int	id;
	
	private SubQuery parent;
	
	private MicroOperator localDownstream;
	private MicroOperator localUpstream;
	
	private IMicroOperatorCode cpuCode;
	private IMicroOperatorCode gpuCode;

	public MicroOperator (IMicroOperatorCode cpuCode, IMicroOperatorCode gpuCode, int id) {
		
		this.cpuCode = cpuCode;
		this.gpuCode = gpuCode;
		
		this.id = id;
	}
	
	public MicroOperator (IMicroOperatorCode cpuCode, int id) {
		
		this(cpuCode, null, id);
	}

	public void setParentSubQuery (SubQuery parent) {
		
		this.parent = parent;
	}

	public SubQuery getParentSubQuery () {
		
		return this.parent;
	}

	public void connectTo (int localStreamId, MicroOperator op) {
		
		this.localDownstream = op;
		op.setLocalUpstream (this);
	}

	public boolean isMostUpstream () {
		
		return (this.localUpstream == null);
	}

	public boolean isMostDownstream () {
		
		return (this.localDownstream == null);
	}

	public void setLocalUpstream (MicroOperator localUpstream) {
		
		this.localUpstream = localUpstream;
	}
	
	public MicroOperator getLocalDownstream () {
		
		return this.localDownstream;
	}

	public int getId() {
		
		return id;
	}

	public void process(WindowBatch windowBatch, IWindowAPI api, boolean GPU) {
		
		if (GPU) {
			this.gpuCode.processData(windowBatch, api);
		} else {
			this.cpuCode.processData(windowBatch, api);
		}
	}
	
	public void process(WindowBatch firstWindowBatch, WindowBatch secondWindowBatch, IWindowAPI api, boolean GPU) {
		
		if (GPU)
			this.gpuCode.processData(firstWindowBatch, secondWindowBatch, api);
		else
			this.cpuCode.processData(firstWindowBatch, secondWindowBatch, api);
	}
}
