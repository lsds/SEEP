package uk.ac.imperial.lsds.streamsql.expressions.efloat;

import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.streamsql.expressions.ExpressionsUtil;
import uk.ac.imperial.lsds.streamsql.visitors.ValueExpressionVisitor;

public class FloatDivision implements FloatExpression {

	private FloatExpression[]	expressions	= null;

	public FloatDivision(FloatExpression... expressions) {
		this.expressions = expressions;
	}

	public FloatDivision(FloatExpression exp1, FloatExpression exp2) {
		this.expressions = new FloatExpression[2];
		this.expressions[0] = exp1;
		this.expressions[1] = exp2;
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
				sb.append(" / ");
		}
		return sb.toString();
	}

	@Override
	public float eval(IQueryBuffer buffer, ITupleSchema schema, int offset) {
		float result = this.expressions[0].eval(buffer, schema, offset);
		for (int i = 1; i < expressions.length; i++) {
			result /= expressions[i].eval(buffer, schema, offset);
		}
		return result;
	}

	@Override
	public void appendByteResult(IQueryBuffer fromBuffer, ITupleSchema schema,
			int offset, IQueryBuffer toBuffer) {
		toBuffer.putFloat(eval(fromBuffer, schema, offset));
	}

	@Override
	public void writeByteResult(IQueryBuffer fromBuffer, ITupleSchema schema,
			int fromBufferOffset, IQueryBuffer toBuffer, int toBufferOffset) {
		
		byte [] bytes = ExpressionsUtil.floatToByteArray(eval(fromBuffer, schema, fromBufferOffset));
		System.arraycopy (fromBuffer.array(), fromBuffer.normalise(fromBufferOffset), bytes, 0, 4);
	}

	@Override
	public byte[] evalAsByteArray(IQueryBuffer buffer, ITupleSchema schema, int offset) {
		
		return ExpressionsUtil.floatToByteArray(eval(buffer, schema, offset));
	}
	
	@Override
	public void evalAsByteArray(IQueryBuffer buffer, ITupleSchema schema,
			int offset, byte[] bytes) {
		
		ExpressionsUtil.floatToByteArray(eval(buffer, schema, offset), bytes);
	}
}
