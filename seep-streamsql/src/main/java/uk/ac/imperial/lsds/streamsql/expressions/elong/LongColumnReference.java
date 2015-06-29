package uk.ac.imperial.lsds.streamsql.expressions.elong;

import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.streamsql.expressions.ExpressionsUtil;
import uk.ac.imperial.lsds.streamsql.visitors.ValueExpressionVisitor;

public class LongColumnReference implements LongExpression {

	private int _column = -1;

	public LongColumnReference(int column) {
		_column = column;
	}
	
	public int getColumn () {
		return _column;
	}

	@Override
	public void accept (ValueExpressionVisitor vev) {
		vev.visit(this);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		if (_column != -1)
			sb.append("\"").append(_column).append("\"");
		return sb.toString();
	}

	@Override
	public long eval(IQueryBuffer buffer, ITupleSchema schema, int offset) {
		return buffer.getLong(offset + schema.getOffsetForAttribute(_column));
	}

	
	@Override
	public void appendByteResult(IQueryBuffer fromBuffer, ITupleSchema schema, int offset, IQueryBuffer toBuffer) {
		toBuffer.putLong(eval(fromBuffer, schema, offset));
	}

	@Override
	public void writeByteResult(IQueryBuffer fromBuffer, ITupleSchema fromSchema,
			int fromBufferOffset, IQueryBuffer toBuffer, int toBufferOffset) {
		System.arraycopy(fromBuffer.array(), fromBuffer.normalise(fromBufferOffset + fromSchema.getOffsetForAttribute(_column)), toBuffer.array(), toBufferOffset, 8);
	}

	@Override
	public byte [] evalAsByteArray(IQueryBuffer buffer, ITupleSchema schema, int offset) {
		
		return ExpressionsUtil.longToByteArray(eval(buffer, schema, offset));
	}
	
	@Override
	public void evalAsByteArray(IQueryBuffer buffer, ITupleSchema schema,
			int offset, byte[] bytes) {
		
		ExpressionsUtil.longToByteArray(eval(buffer, schema, offset), bytes);
	}

	@Override
	public int evalAsByteArray(IQueryBuffer buffer, ITupleSchema schema,
			int offset, byte[] bytes, int pivot) {
		
		return ExpressionsUtil.longToByteArray(eval(buffer, schema, offset), bytes, pivot);
	}
}
