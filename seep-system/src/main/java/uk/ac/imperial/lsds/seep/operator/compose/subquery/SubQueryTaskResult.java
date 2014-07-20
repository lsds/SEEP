package uk.ac.imperial.lsds.seep.operator.compose.subquery;

import java.util.List;
import java.util.Map;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.compose.multi.SubQueryBuffer;

public class SubQueryTaskResult {

	private Map<SubQueryBuffer, Integer> freeUpToIndices;
		
	private int logicalOrderID;
	
	private List<DataTuple> resultStream;

	public SubQueryTaskResult(int logicalOrderID, Map<SubQueryBuffer, Integer> freeUpToIndices) {
		this.logicalOrderID = logicalOrderID;
		this.freeUpToIndices = freeUpToIndices;
	}

	public SubQueryTaskResult(List<DataTuple> resultStream, int logicalOrderID, Map<SubQueryBuffer, Integer> freeUpToIndices) {
		this(logicalOrderID, freeUpToIndices);
		this.resultStream = resultStream;
	}
	
	public void freeIndicesInBuffers() {
		for (SubQueryBuffer b : this.freeUpToIndices.keySet())
			b.freeUpToIndex(b.normIndex(this.freeUpToIndices.get(b)));
	}
	
	public Map<SubQueryBuffer, Integer> getFreeUpToIndices() {
		return freeUpToIndices;
	}

	public int getLogicalOrderID() {
		return logicalOrderID;
	}

	public List<DataTuple> getResultStream() {
		return resultStream;
	}

	public void setResultStream(List<DataTuple> resultStream) {
		this.resultStream = resultStream;
	}
	


}
