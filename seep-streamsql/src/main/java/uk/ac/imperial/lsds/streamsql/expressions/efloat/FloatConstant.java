package uk.ac.imperial.lsds.streamsql.expressions.efloat;

import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.streamsql.expressions.ExpressionsUtil;
import uk.ac.imperial.lsds.streamsql.visitors.ValueExpressionVisitor;

public class FloatConstant implements FloatExpression {

	private float _constant;
	
	private byte[] _constantBytes;

	public FloatConstant(float constant) {
		_constant = constant;
		_constantBytes = ExpressionsUtil.floatToByteArray(_constant);
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
	public void appendByteResult(IQueryBuffer fromBuffer, ITupleSchema schema, int offset, IQueryBuffer toBuffer) {
		toBuffer.putFloat(_constant);
	}

	@Override
	public void accept(ValueExpressionVisitor vev) {
		vev.visit(this);
	}

	@Override
	public void writeByteResult(IQueryBuffer fromBuffer, ITupleSchema schema,
			int fromBufferOffset, IQueryBuffer toBuffer, int toBufferOffset) {
//		System.arraycopy(_constantBytes, 0, toBuffer.array(), toBufferOffset, 4);
		toBuffer.put(_constantBytes, toBufferOffset, 4);
	}

	@Override
	public byte[] evalAsByteArray(IQueryBuffer buffer, ITupleSchema schema, int offset) {
		return _constantBytes;
	}

}