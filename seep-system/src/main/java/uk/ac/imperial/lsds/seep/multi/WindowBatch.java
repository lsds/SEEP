package uk.ac.imperial.lsds.seep.multi;

import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition;

public class WindowBatch {
	
	private int batchSize;
	
	private IQueryBuffer buffer;
	private WindowDefinition windowDefinition;
	private ITupleSchema schema;
	
	private int batchStartPointer;
	private int batchEndPointer;
	
	private int [] windowStartPointers;
	private int [] windowEndPointers;
	private boolean initialised = false;
	
	public WindowBatch () {
		this(0, null, null, null, 0, 0);
	}
	
	public WindowBatch (int batchSize, 
                        IQueryBuffer buffer, 
                        WindowDefinition windowDefinition, 
                        ITupleSchema schema, 
                        int batchStartPointer, 
                        int batchEndPointer) {
		
		this.batchSize = batchSize;
		this.buffer = buffer;
		this.windowDefinition = windowDefinition;
		this.schema = schema;
		this.batchStartPointer = batchStartPointer;
		this.batchEndPointer = batchEndPointer;
		this.windowStartPointers = null;
		this.windowEndPointers = null;
	}
	
	public void set (int batchSize, 
                     IQueryBuffer buffer, 
                     WindowDefinition windowDefinition, 
                     ITupleSchema schema, 
                     int batchStartPointer, 
                     int batchEndPointer) {
		
		this.batchSize = batchSize;
		this.buffer = buffer;
		this.windowDefinition = windowDefinition;
		this.schema = schema;
		this.batchStartPointer = batchStartPointer;
		this.batchEndPointer = batchEndPointer;
		this.windowStartPointers = null;
		this.windowEndPointers = null;
	}
	
	
	public int getBatchSize () {
		return this.batchSize;
	}
	
	public void initWindowPointers () {
		if (initialised)
			return ;
		//TODO: Populate window start and end pointers 
		windowStartPointers = new int [batchSize];
		windowEndPointers   = new int [batchSize];
	}
	
	public void clear () {
		initialised = false;
		windowStartPointers = windowEndPointers = null;
	}
	
	public int getInt (int offset, int attribute) {
		int index = offset + schema.getOffsetForAttribute(attribute);
		return this.buffer.getInt (index);
	}
	
	public long getLong (int offset, int attribute) {
		int index = offset + schema.getOffsetForAttribute(attribute);
		return this.buffer.getLong (index);
	}
	
	public float getFloat (int offset, int attribute) {
		int index = offset + schema.getOffsetForAttribute(attribute);
		return this.buffer.getFloat (index);
	}
	
	public void putInt (int value) {
		this.buffer.putInt (value);
	}
	
	public void putLong (long value) {
		this.buffer.putLong (value);
	}
	
	public void putFloat (float value) {
		this.buffer.putFloat (value);
	}
	
	public int [] getWindowStartPointers () {
		return this.windowStartPointers;
	}
	
	public int [] getWindowEndPointers () {
		return this.windowEndPointers;
	}
	
	public int getBatchStartPointer () {
		return this.batchStartPointer;
	}
	
	public int getBatchEndPointer () {
		return this.batchEndPointer;
	}
	
	public void setBuffer (IQueryBuffer buffer) {
		this.buffer = buffer;
	}
	
	public IQueryBuffer getBuffer () {
		return this.buffer;
	}
	
	public ITupleSchema getSchema () {
		return this.schema;
	}
	
	public void setSchema (ITupleSchema schema) {
		this.schema = schema;
	}
}

