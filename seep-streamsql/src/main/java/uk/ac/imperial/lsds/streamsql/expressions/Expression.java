package uk.ac.imperial.lsds.streamsql.expressions;

import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.TupleSchema;

public interface Expression {

	public byte[] evalAsByte(IQueryBuffer buffer, TupleSchema schema, int offset);

}