package uk.ac.imperial.lsds.streamsql.expressions.eint;

import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
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
	public int eval(IQueryBuffer buffer, ITupleSchema schema, int offset) {
		return _constant;
	}

	@Override
	public void writeByteResult(IQueryBuffer fromBuffer, ITupleSchema schema, int offset, IQueryBuffer toBuffer) {
		toBuffer.putInt(eval(fromBuffer, schema, offset));
	}

}