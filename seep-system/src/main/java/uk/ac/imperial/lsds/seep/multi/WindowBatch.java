package uk.ac.imperial.lsds.seep.multi;



public class WindowBatch {

	private int[] windowStartPointers;
	private int[] windowEndPointers;

	private int batchStartPointer;
	private int batchEndPointer;
	
	private IQueryBuffer buffer;
	
	private WindowDefinition windowDefinition;
	
	private TupleSchema schema;
	
	public WindowBatch(
			IQueryBuffer buffer, 
			WindowDefinition windowDefinition, 
			TupleSchema schema,
			int batchStartPointer,
			int batchEndPointer) {
		this.buffer = buffer;
		this.windowDefinition = windowDefinition;
		this.schema = schema;
		this.batchStartPointer = batchStartPointer;
		this.batchEndPointer = batchEndPointer;
	}
	
	public void initWindowPointers() {
		
	}
	
	public int getInt(int tupleOffset, int attributeIndex) {
		return this.buffer.getInt(tupleOffset + this.schema.getOffsetForAttribute(attributeIndex));
	}
	
	public long getLong(int tupleOffset, int attributeIndex) {
		return this.buffer.getLong(tupleOffset + this.schema.getOffsetForAttribute(attributeIndex));
	}
	
	public float getFloat(int tupleOffset, int attributeIndex) {
		return this.buffer.getFloat(tupleOffset + this.schema.getOffsetForAttribute(attributeIndex));
	}
	
	public void putInt(int value) {
		this.buffer.putInt(value);
	}
	
	public void putLong(long value) {
		this.buffer.putLong(value);
	}

	public void putFloat(float value) {
		this.buffer.putFloat(value);
	}

	public int[] getWindowStartPointers() {
		return this.windowStartPointers;
	}
	
	public int[] getWindowEndPointers() {
		return this.windowEndPointers;
	}

	public void setBuffer(IQueryBuffer buffer) {
		this.buffer = buffer;
	}

	public IQueryBuffer getBuffer() {
		return this.buffer;
	}

	public TupleSchema getSchema() {
		return schema;
	}

	public void setSchema(TupleSchema schema) {
		this.schema = schema;
	}

}
