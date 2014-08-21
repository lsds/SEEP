package uk.ac.imperial.lsds.seep.operator.compose.OLD;

import java.util.Map;

import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOpTuple;
import uk.ac.imperial.lsds.seep.operator.compose.multi.SubQueryBufferWindowWrapper;

public class SubQueryTaskResultOLD {

	private Map<SubQueryBufferWindowWrapper, Integer> freeUpToIndices;
		
	private int logicalOrderID;
	
	private MultiOpTuple[] resultStream;

	public SubQueryTaskResultOLD(int logicalOrderID, Map<SubQueryBufferWindowWrapper, Integer> freeUpToIndices) {
		this.logicalOrderID = logicalOrderID;
		this.freeUpToIndices = freeUpToIndices;
	}

	public void freeIndicesInBuffers() {
		for (SubQueryBufferWindowWrapper b : this.freeUpToIndices.keySet())
			b.freeUpToIndexInBuffer(this.freeUpToIndices.get(b));
	}
	
	public Map<SubQueryBufferWindowWrapper, Integer> getFreeUpToIndices() {
		return freeUpToIndices;
	}

	public int getLogicalOrderID() {
		return logicalOrderID;
	}

	public MultiOpTuple[] getResultStream() {
		return resultStream;
	}

	public void setResultStream(MultiOpTuple[] resultStream) {
		this.resultStream = resultStream;
	}
	
}
