import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.API;
import uk.ac.imperial.lsds.seep.operator.Callback;


public class FileAPI implements API {

	public static int BUFFER_SIZE = 1;
	
	private String dataPath = "";
	private List<String> fields = new ArrayList<String>();
	
	private List<String> buffer = new ArrayList<>();
 	

	public FileAPI(String dataPath, List<String> fields) {
		this.dataPath = dataPath;
		this.fields = fields;
	}

	private void process(DataTuple dt) {

//		System.out.println("SNK: " + dt.toString());
		
		StringBuilder sb = new StringBuilder();

		for (String key : fields) {
			sb.append(dt.getValue(key).toString());
			sb.append(',');
		}
		
		if (sb.length() >= 1)
			this.buffer.add(sb.substring(0, sb.length()-1));
		else
			this.buffer.add(sb.substring(0, sb.length()));

		if (this.buffer.size() >= BUFFER_SIZE) {
			try {
				FileWriter fw = new FileWriter(this.dataPath, true);
				BufferedWriter bw = new BufferedWriter(fw);
				for (String s : this.buffer)
					bw.write(s + "\n");
				bw.close();
				
				this.buffer.clear();
				
				System.out.println("SNK: wrote results to " + this.dataPath);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	
	@Override
	public void setCallbackObject(Callback c) {
	}

	@Override
	public void send(DataTuple dt) {
		process(dt);
	}

	@Override
	public void send_toIndex(DataTuple dt, int idx) {
		process(dt);
	}

	@Override
	public void send_splitKey(DataTuple dt, int key) {
		process(dt);
	}

	@Override
	public void send_toStreamId_splitKey(DataTuple dt, int streamId, int key) {
		process(dt);
	}

	@Override
	public void send_toStreamId_toAll(DataTuple dt, int streamId) {
		process(dt);
	}

	@Override
	public void send_all(DataTuple dt) {
		process(dt);
	}

	@Override
	public void send_toStreamId(DataTuple dt, int streamId) {
		process(dt);
	}

	@Override
	public void send_toStreamId_toAll_threadPool(DataTuple dt, int streamId) {
		process(dt);
	}

	@Override
	public void send_all_threadPool(DataTuple dt) {
		process(dt);
	}

	@Override
	public void send_to_OpId(DataTuple dt, int opId) {
		process(dt);
	}

	@Override
	public void send_to_OpIds(DataTuple[] dt, int[] opId) {
		for (DataTuple d : dt)
			process(d);
	}

	@Override
	public void send_toIndices(DataTuple[] dts, int[] indices) {
		for (DataTuple d : dts)
			process(d);
	}

	@Override
	public Map<String, Integer> getDataMapper() {
		return null;
	}

}
