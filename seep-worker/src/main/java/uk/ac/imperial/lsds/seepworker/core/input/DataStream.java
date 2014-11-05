package uk.ac.imperial.lsds.seepworker.core.input;

import java.util.List;

import uk.ac.imperial.lsds.seep.api.DataOrigin;
import uk.ac.imperial.lsds.seep.api.Operator;
import uk.ac.imperial.lsds.seep.api.data.Schema;
import uk.ac.imperial.lsds.seepworker.data.Data;

public class DataStream implements InputAdapter{

	final private short r_type = InputAdapterReturnType.ONE.ofType();
	
	final private int streamId;
	private DataOrigin dOrigin;
	private Schema expectedSchema;
	private Operator op;
	
	public DataStream(int streamId, DataOrigin dataOrigin, Schema expectedSchema, Operator op){
		this.streamId = streamId;
		this.dOrigin = dataOrigin;
		this.expectedSchema = expectedSchema;
		this.op = op;
	}

	@Override
	public short rType() {
		return r_type;
	}

	@Override
	public Data pullDataItem() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Data> pullDataItems() {
		// TODO Auto-generated method stub
		return null;
	}
}
