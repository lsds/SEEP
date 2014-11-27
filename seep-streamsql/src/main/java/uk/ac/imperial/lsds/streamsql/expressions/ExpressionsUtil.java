package uk.ac.imperial.lsds.streamsql.expressions;

import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.TupleSchema;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatExpression;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntExpression;
import uk.ac.imperial.lsds.streamsql.expressions.elong.LongExpression;

public class ExpressionsUtil {

	public static final byte[] intToByteArray(int value) {
	    return new byte[] {
	            (byte)(value >>> 24),
	            (byte)(value >>> 16),
	            (byte)(value >>> 8),
	            (byte)value};
	}
	
	public static final byte[] floatToByteArray(float value) {
		int bits = Float.floatToIntBits(value);
	    return new byte [] {
	    		(byte)(bits & 0xff),
	    		(byte)((bits >> 8) & 0xff),
	    		(byte)((bits >> 16) & 0xff),
	    		(byte)((bits >> 24) & 0xff)
	    };
	}


	public static final byte[] longToByteArray(long value) {
		byte[] b = new byte[8];
		for (int i = 0; i < 8; ++i) {
		  b[i] = (byte) (value >> (8 - i - 1 << 3));
		}
		return b;
	}
	
	public static final ITupleSchema getTupleSchemaForExpressions(final Expression[] expressions) {
		int[] offsets = new int[expressions.length]; 
		int currentOffset = 0;
		for (int i = 0; i < expressions.length; i++) {
			Expression e = expressions[i];
			offsets[i] = currentOffset;
			if (e instanceof IntExpression) {
				currentOffset += 4;
			}
			else if (e instanceof LongExpression) {
				currentOffset += 8;
			}
			else if (e instanceof FloatExpression) {
				currentOffset += 4;
			}
		}
		
		return new TupleSchema(offsets, currentOffset);
	}


}
