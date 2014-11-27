package uk.ac.imperial.lsds.streamsql.expressions.eint;

import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.streamsql.expressions.ExpressionsUtil;
import uk.ac.imperial.lsds.streamsql.visitors.ValueExpressionVisitor;

public class IntSubtraction implements IntExpression {
	
	private IntExpression[] expressions = null;

	public IntSubtraction(IntExpression[] expressions) {
		this.expressions = expressions;
	}

	@Override
	public void accept(ValueExpressionVisitor vev) {
		vev.visit(this);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < expressions.length; i++) {
			sb.append("(").append(expressions[i]).append(")");
			if (i != expressions.length - 1)
				sb.append(" - ");
		}
		return sb.toString();
	}

	@Override
	public int eval(IQueryBuffer buffer, ITupleSchema schema, int offset) {
		int result = this.expressions[0].eval(buffer, schema, offset);
		for (int i = 1; i < expressions.length; i++) {
			result -= expressions[i].eval(buffer, schema, offset);
		}
		return result;
	}
	
	@Override
	public void appendByteResult(IQueryBuffer fromBuffer, ITupleSchema schema, int offset, IQueryBuffer toBuffer) {
		toBuffer.putInt(eval(fromBuffer, schema, offset));
	}

	@Override
	public void writeByteResult(IQueryBuffer fromBuffer, ITupleSchema schema,
			int fromBufferOffset, IQueryBuffer toBuffer, int toBufferOffset) {
		System.arraycopy(fromBuffer.array(), fromBufferOffset, ExpressionsUtil.intToByteArray(eval(fromBuffer, schema, fromBufferOffset)), 0, 4);
	}

	@Override
	public byte[] evalAsByteArray(IQueryBuffer buffer, ITupleSchema schema, int offset) {
		return ExpressionsUtil.intToByteArray(eval(buffer, schema, offset));
	}
}
