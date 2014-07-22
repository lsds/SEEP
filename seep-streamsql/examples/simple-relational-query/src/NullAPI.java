import java.util.Map;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.API;
import uk.ac.imperial.lsds.seep.operator.Callback;


public class NullAPI implements API {

	@Override
	public void setCallbackObject(Callback c) {
		// TODO Auto-generated method stub

	}

	@Override
	public void send(DataTuple dt) {
		// TODO Auto-generated method stub

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

}
