package uk.ac.imperial.lsds.streamsql.expressions;

import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;

public interface Expression {

	public void writeByteResult(IQueryBuffer fromBuffer, ITupleSchema schema, int offset, IQueryBuffer toBuffer);

}