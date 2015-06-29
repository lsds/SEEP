package uk.ac.imperial.lsds.streamsql.expressions;

import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.TupleSchema;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatExpression;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntExpression;
import uk.ac.imperial.lsds.streamsql.expressions.elong.LongExpression;

public class ExpressionsUtil {

	public static final byte [] intToByteArray(int value) {
		
		return new byte[] { (byte) (value >>> 24), (byte) (value >>> 16),
				(byte) (value >>> 8), (byte) value };
	}

	public static void intToByteArray(int value, byte [] bytes) {
		
		bytes[0] = (byte) (value >>> 24);
		bytes[1] = (byte) (value >>> 16);
		bytes[2] = (byte) (value >>>  8);
		bytes[3] = (byte) (value);	
	}
	
	public static int intToByteArray(int value, byte [] bytes, int pivot) {
		
		bytes[pivot + 0] = (byte) (value >>> 24);
		bytes[pivot + 1] = (byte) (value >>> 16);
		bytes[pivot + 2] = (byte) (value >>>  8);
		bytes[pivot + 3] = (byte) (value);
		
		return (pivot + 4);
	}
	
	public static final byte [] floatToByteArray (float value) {
		
		int bits = Float.floatToIntBits(value);
		
		return new byte [] { (byte) (bits & 0xff), (byte) ((bits >> 8) & 0xff),
				(byte) ((bits >> 16) & 0xff), (byte) ((bits >> 24) & 0xff) };
	}
	
	public static void floatToByteArray(float value, byte[] bytes) {
		
		int bits = Float.floatToIntBits(value);
		
		bytes[0] = (byte) ((bits)       & 0xff);
		bytes[1] = (byte) ((bits >>  8) & 0xff);
		bytes[2] = (byte) ((bits >> 16) & 0xff);
		bytes[3] = (byte) ((bits >> 24) & 0xff);
	}
	
	public static int floatToByteArray (float value, byte [] bytes, int pivot) {
		
		int bits = Float.floatToIntBits(value);
		
		bytes[pivot + 0] = (byte) ((bits)       & 0xff);
		bytes[pivot + 1] = (byte) ((bits >>  8) & 0xff);
		bytes[pivot + 2] = (byte) ((bits >> 16) & 0xff);
		bytes[pivot + 3] = (byte) ((bits >> 24) & 0xff);
		
		return (pivot + 4);
	}
	
	public static final byte [] longToByteArray(long value) {
		
		byte [] b = new byte[8];
		
		for (int i = 0; i < 8; ++i) {
			b[i] = (byte) (value >> (8 - i - 1 << 3));
		}
		
		return b;
	}
	
	public static void longToByteArray(long value, byte [] bytes) {
		
		for (int i = 0; i < 8; ++i) {
			
			bytes[i] = (byte) (value >> (8 - i - 1 << 3));
		}
	}
	
	public static int longToByteArray(long value, byte [] bytes, int pivot) {
		
		for (int i = 0; i < 8; ++i) {
			
			bytes[pivot + i] = (byte) (value >> (8 - i - 1 << 3));
		}
		return (pivot + 8);
	}

	public static final ITupleSchema getTupleSchemaForExpressions
		(final Expression[] expressions) {
		
		ITupleSchema outputSchema;
		
		int [] offsets = new int[expressions.length];
		
		int currentOffset = 0;
		
		for (int i = 0; i < expressions.length; i++) {
			
			Expression e = expressions[i];
			
			offsets[i] = currentOffset;
			if (e instanceof IntExpression) {
				currentOffset += 4;
			} else if (e instanceof LongExpression) {
				currentOffset += 8;
			} else if (e instanceof FloatExpression) {
				currentOffset += 4;
			}
		}
		
		outputSchema = new TupleSchema(offsets, currentOffset);
		/* Set types */
		for (int i = 0; i < expressions.length; i++) {
			Expression e = expressions[i];
			     if (e instanceof   IntExpression) outputSchema.setType(i, 1);
			else if (e instanceof  LongExpression) outputSchema.setType(i, 3);
			else if (e instanceof FloatExpression) outputSchema.setType(i, 2);
		}

		return outputSchema;
	}

	public static final ITupleSchema mergeTupleSchemas(
			final ITupleSchema first, final ITupleSchema second) {

		int[] offsets = new int[first.getNumberOfAttributes()
				+ second.getNumberOfAttributes()];

		for (int i = 0; i < first.getNumberOfAttributes(); i++)
			offsets[i] = first.getOffsetForAttribute(i);

		int last = first.getByteSizeOfTuple() - first.getDummyContent().length;
		for (int i = 0; i < second.getNumberOfAttributes(); i++)
			offsets[i + first.getNumberOfAttributes()] = last + second.getOffsetForAttribute(i);

		return new TupleSchema(offsets, first.getByteSizeOfTuple() - first.getDummyContent().length
				+ second.getByteSizeOfTuple() - second.getDummyContent().length);
	}

	
}
