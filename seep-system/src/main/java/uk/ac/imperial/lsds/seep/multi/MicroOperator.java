package uk.ac.imperial.lsds.seep.multi;

import uk.ac.imperial.lsds.seep.multi.SubQuery;

public class MicroOperator {

	private int id;
	
	private SubQuery parent;
	
	private MicroOperator localDownstream;
	private MicroOperator localUpstream;
	
	private IMicroOperatorCode cpuCode;
	private IMicroOperatorCode gpuCode;

	public MicroOperator(IMicroOperatorCode cpuCode, IMicroOperatorCode gpuCode, int id) {
		this.cpuCode = cpuCode;
		this.gpuCode = gpuCode;
		this.id = id;
	}
	
	public MicroOperator(IMicroOperatorCode cpuCode, int id) {
		this(cpuCode, null, id);
	}
	
	public void setParentSubQuery(SubQuery parent) {
		this.parent = parent;
	}

	public SubQuery getParentSubQuery() {
		return this.parent;
	}

	public void connectTo(int localStreamId, MicroOperator so) {
		this.localDownstream = so;
		so.setLocalUpstream(this);
	}
	
	public boolean isMostUpstream() {
		return (this.localUpstream == null);
	}

	public boolean isMostDownstream() {
		return (this.localDownstream == null);
	}

	public void setLocalUpstream(MicroOperator localUpstream) {
		this.localUpstream = localUpstream;
	}

	public int getId() {
		return id;
	}

	public void process(WindowBatch windowBatch, IWindowAPI api, boolean GPU) {
		if (GPU)
			this.gpuCode.processData(windowBatch, api);
		else
			this.cpuCode.processData(windowBatch, api);
	}
	
}
