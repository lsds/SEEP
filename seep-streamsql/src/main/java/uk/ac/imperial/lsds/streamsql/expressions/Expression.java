package uk.ac.imperial.lsds.streamsql.expressions;

import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;

public interface Expression {

	public void appendByteResult (IQueryBuffer fromBuffer, ITupleSchema schema, int offset, IQueryBuffer toBuffer);
	
	public void writeByteResult(
			IQueryBuffer fromBuffer, 
			ITupleSchema schema, 
			int fromBufferOffset, 
			IQueryBuffer toBuffer,
			int toBufferOffset);
	
	public byte [] evalAsByteArray (IQueryBuffer buffer, ITupleSchema schema, int offset);

	public void evalAsByteArray(IQueryBuffer buffer, ITupleSchema schema, int offset, byte[] bytes);

	public int evalAsByteArray (IQueryBuffer buffer, ITupleSchema schema, int offset, byte[] bytes, int pivot);
}
