package uk.ac.imperial.lsds.seepworker.core.input;

import java.util.List;

import uk.ac.imperial.lsds.seep.api.DataOrigin;
import uk.ac.imperial.lsds.seep.api.Operator;
import uk.ac.imperial.lsds.seep.api.data.Schema;
import uk.ac.imperial.lsds.seep.api.data.ITuple;

public class DataStream implements InputAdapter{

	final private short r_type = InputAdapterReturnType.ONE.ofType();
	
	final private int streamId;
	private DataOrigin dOrigin;
	private Schema expectedSchema;
	private List<Operator> ops;
	
	public DataStream(int streamId, DataOrigin dataOrigin, Schema expectedSchema, List<Operator> ops) {
		this.streamId = streamId;
		this.dOrigin = dataOrigin;
		this.expectedSchema = expectedSchema;
		this.ops = ops;
	}

	@Override
	public short rType() {
		return r_type;
	}

	@Override
	public ITuple pullDataItem() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ITuple pullDataItems() {
		// TODO Auto-generated method stub
		return null;
	}
}
