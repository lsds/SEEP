package uk.ac.imperial.lsds.streamsql.expressions.eint;

import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.streamsql.expressions.ExpressionsUtil;
import uk.ac.imperial.lsds.streamsql.visitors.ValueExpressionVisitor;

public class IntConstant implements IntExpression {

	private int _constant;

	private byte [] _constantBytes;

	public IntConstant(int constant) {
		_constant = constant;
		_constantBytes = ExpressionsUtil.intToByteArray(_constant);
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
	public void appendByteResult(IQueryBuffer fromBuffer, ITupleSchema schema, int offset, IQueryBuffer toBuffer) {
		toBuffer.putInt(_constant);
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
	
	@Override
	public void evalAsByteArray(IQueryBuffer buffer, ITupleSchema schema,
			int offset, byte[] bytes) {
		System.arraycopy(_constantBytes, 0, bytes, 0, _constantBytes.length);
	}
}