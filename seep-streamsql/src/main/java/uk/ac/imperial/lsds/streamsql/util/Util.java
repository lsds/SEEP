package uk.ac.imperial.lsds.streamsql.util;

import java.util.ArrayList;
import java.util.List;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.streamsql.conversion.DoubleConversion;
import uk.ac.imperial.lsds.streamsql.conversion.TypeConversion;
import uk.ac.imperial.lsds.streamsql.expressions.IValueExpression;
import uk.ac.imperial.lsds.streamsql.expressions.ValueExpression;

public class Util {
	
	public static String generateTupleString(DataTuple tuple) {
		return tuple.getPayload().toString();
	}

	public static TypeConversion getDominantNumericType(List<IValueExpression> veList) {
		TypeConversion wrapper = veList.get(0).getType();
		for (int i = 1; i < veList.size(); i++) {
			final TypeConversion currentType = veList.get(1).getType();
			if (isDominant(currentType, wrapper))
				wrapper = currentType;
		}
		return wrapper;
	}

	/*
	 * Does bigger dominates over smaller? For (bigger, smaller) = (double,
	 * long) answer is yes.
	 */
	private static boolean isDominant(TypeConversion bigger, TypeConversion smaller) {
		// for now we only have two numeric types: double and long
		if (bigger instanceof DoubleConversion)
			return true;
		else
			return false;
	}
	

	public static <T extends Comparable<T>> List<IValueExpression> listTypeErasure(
			List<IValueExpression<T>> input) {
		final List<IValueExpression> result = new ArrayList<IValueExpression>();
		for (final IValueExpression ve : input)
			result.add(ve);
		return result;
	}
}
