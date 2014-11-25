package uk.ac.imperial.lsds.streamsql.expressions.efloat;

import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.streamsql.visitors.ValueExpressionVisitor;

public class FloatConstant implements FloatExpression {

	private float _constant;

	public FloatConstant(float constant) {
		_constant = constant;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Constant ").append(_constant);
		return sb.toString();
	}

	@Override
	public float eval(IQueryBuffer buffer, ITupleSchema schema, int offset) {
		return _constant;
	}

	@Override
	public void writeByteResult(IQueryBuffer fromBuffer, ITupleSchema schema, int offset, IQueryBuffer toBuffer) {
		toBuffer.putFloat(eval(fromBuffer, schema, offset));
	}

	@Override
	public void accept(ValueExpressionVisitor vev) {
		vev.visit(this);
	}
}