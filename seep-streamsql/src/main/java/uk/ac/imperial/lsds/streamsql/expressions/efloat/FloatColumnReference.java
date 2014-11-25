package uk.ac.imperial.lsds.streamsql.expressions.efloat;

import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.streamsql.visitors.ValueExpressionVisitor;

public class FloatColumnReference implements FloatExpression {

	private int _column = -1;

	public FloatColumnReference(int column) {
		_column = column;
	}

	@Override
	public void accept(ValueExpressionVisitor vev) {
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
	public float eval(IQueryBuffer buffer, ITupleSchema schema, int offset) {
		return buffer.getInt(offset + schema.getOffsetForAttribute(_column));
	}
	
	@Override
	public void writeByteResult(IQueryBuffer fromBuffer, ITupleSchema schema, int offset, IQueryBuffer toBuffer) {
		toBuffer.putFloat(eval(fromBuffer, schema, offset));
	}
}
