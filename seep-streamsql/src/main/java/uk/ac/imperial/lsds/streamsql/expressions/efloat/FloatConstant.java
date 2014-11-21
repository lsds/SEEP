package uk.ac.imperial.lsds.streamsql.expressions.efloat;

import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.TupleSchema;
import uk.ac.imperial.lsds.streamsql.expressions.ExpressionsUtil;
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
	public float eval(IQueryBuffer buffer, TupleSchema schema, int offset) {
		return _constant;
	}

	@Override
	public byte[] evalAsByte(IQueryBuffer buffer, TupleSchema schema, int offset) {
		return ExpressionsUtil.floatToByteArray(eval(buffer, schema, offset));
	}

	@Override
	public void accept(ValueExpressionVisitor vev) {
		vev.visit(this);
	}
}