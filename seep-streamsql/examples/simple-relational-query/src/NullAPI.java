import java.util.Map;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.Callback;
import uk.ac.imperial.lsds.seep.operator.MultiAPI;
import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOpTuple;


public class NullAPI implements MultiAPI {

	public int totalTuples;
	public long startTimestamp;
	public long waitForInstrumentationTimestamp;
	
	public long sum;
	
	private boolean printedResults = false;
	
	@Override
	public void setCallbackObject(Callback c) {
	}

	@Override
	public void send(DataTuple dt) {
//		System.out.println("SNK: " + dt.toString());
	}

	@Override
	public void send_toIndex(DataTuple dt, int idx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void send_splitKey(DataTuple dt, int key) {
		// TODO Auto-generated method stub

	}

	@Override
	public void send_toStreamId_splitKey(DataTuple dt, int streamId, int key) {
		// TODO Auto-generated method stub

	}

	@Override
	public void send_toStreamId_toAll(DataTuple dt, int streamId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void send_all(DataTuple dt) {
		// TODO Auto-generated method stub

	}

	@Override
	public void send_toStreamId(DataTuple dt, int streamId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void send_toStreamId_toAll_threadPool(DataTuple dt, int streamId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void send_all_threadPool(DataTuple dt) {
		// TODO Auto-generated method stub

	}

	@Override
	public void send_to_OpId(DataTuple dt, int opId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void send_to_OpIds(DataTuple[] dt, int[] opId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void send_toIndices(DataTuple[] dts, int[] indices) {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<String, Integer> getDataMapper() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void send(MultiOpTuple tuple) {
		//System.out.println("received " + tuple.instrumentation_ts);
	}

}
