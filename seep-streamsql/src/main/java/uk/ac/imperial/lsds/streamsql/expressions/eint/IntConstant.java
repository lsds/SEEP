package uk.ac.imperial.lsds.streamsql.expressions.eint;

import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.TupleSchema;
import uk.ac.imperial.lsds.streamsql.expressions.ExpressionsUtil;
import uk.ac.imperial.lsds.streamsql.visitors.ValueExpressionVisitor;

public class IntConstant implements IntExpression {

	private int _constant;

	public IntConstant(int constant) {
		_constant = constant;
	}

	@Override
	public void accept(ValueExpressionVisitor vev) {
		vev.visit(this);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Constant ").append(_constant);
		return sb.toString();
	}

	@Override
	public int eval(IQueryBuffer buffer, TupleSchema schema, int offset) {
		return _constant;
	}

	@Override
	public byte[] evalAsByte(IQueryBuffer buffer, TupleSchema schema, int offset) {
		return ExpressionsUtil.intToByteArray(eval(buffer, schema, offset));
	}
}