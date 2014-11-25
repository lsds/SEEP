package uk.ac.imperial.lsds.seep.multi;

import uk.ac.imperial.lsds.seep.operator.compose.subquery.SubQuery;

public class MicroOperator {

	private int id;
	
	private SubQuery parent;
	
	private MicroOperator localDownstream;
	private MicroOperator localUpstream;
	
	private IMicroOperatorCode code;

	public MicroOperator(IMicroOperatorCode code, int id) {
		this.code = code;
		this.id = id;
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

	public IMicroOperatorCode getCode() {
		return code;
	}

	public void process(WindowBatch windowBatch, IWindowAPI api) {
		this.code.processData(windowBatch, api);
	}
	
}
