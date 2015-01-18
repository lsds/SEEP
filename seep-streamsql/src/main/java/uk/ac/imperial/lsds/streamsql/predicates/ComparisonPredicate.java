package uk.ac.imperial.lsds.streamsql.predicates;

import java.util.ArrayList;
import java.util.List;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.streamsql.expressions.IValueExpression;
import uk.ac.imperial.lsds.streamsql.visitors.PredicateVisitor;

public class ComparisonPredicate<T extends Comparable<T>> implements IPredicate {

	/*
	 * Values compared by this predicate
	 */
	IValueExpression<T> v1;
	IValueExpression<T> v2;

	/*
	 * Code of the comparison operator
	 */
	int comparisonOperation;

	/*
	 * Codes of available comparison operator
	 */
	public static final int EQUAL_OP = 0;
	public static final int NONEQUAL_OP = 1;
	public static final int LESS_OP = 2;
	public static final int NONLESS_OP = 3;
	public static final int GREATER_OP = 4;
	public static final int NONGREATER_OP = 5;
	
	public ComparisonPredicate(int comparisonOperation, IValueExpression<T> v1, IValueExpression<T> v2) {
		this.comparisonOperation = comparisonOperation;
		this.v1 = v1;
		this.v2 = v2;
	}

	public ComparisonPredicate(IValueExpression<T> v1, IValueExpression<T> v2) {
		this(EQUAL_OP, v1, v2);
	}

	public int getOperator(boolean inverse) {
		int result = 0;
		if (inverse)
			switch (this.comparisonOperation) {
			case NONEQUAL_OP:
				result = NONEQUAL_OP;
				break;
			case EQUAL_OP:
				result = EQUAL_OP;
				break;
			case LESS_OP:
				result = GREATER_OP;
				break;
			case NONLESS_OP:
				result = NONGREATER_OP;
				break;
			case GREATER_OP:
				result = LESS_OP;
				break;
			case NONGREATER_OP:
				result = NONLESS_OP;
				break;
			}
		else
			result = this.comparisonOperation;
		return result;
	}

	@Override
	public boolean satisfied(DataTuple tuple) {
		Comparable val1 = v1.eval(tuple);
		Comparable val2 = v2.eval(tuple);

		// All the Numeric types are converted to double,
		// because different types cannot be compared
		if (val1 instanceof Long)
			val1 = (((Long) val1).doubleValue());
		if (val2 instanceof Long)
			val2 = (((Long) val2).doubleValue());
		
		final int compared = val1.compareTo(val2);

		boolean result = false;
		switch (this.comparisonOperation) {
			case EQUAL_OP:
				result = (compared == 0);
				break;
			case NONEQUAL_OP:
				result = (compared != 0);
				break;
			case LESS_OP:
				result = (compared < 0);
				break;
			case NONLESS_OP:
				result = (compared >= 0);
				break;
			case GREATER_OP:
				result = (compared > 0);
				break;
			case NONGREATER_OP:
				result = (compared <= 0);
				break;
			default:
				throw new RuntimeException("Unsupported operation " + this.comparisonOperation);
		}
		return result;
	}

	@Override
	public boolean satisfied(DataTuple firstTuple, DataTuple secondTuple) {
		final Comparable val1 = v1.eval(firstTuple);
		final Comparable val2 = v2.eval(secondTuple);
		final int compared = val1.compareTo(val2);

		boolean result = false;
		switch (this.comparisonOperation) {
		case EQUAL_OP:
			result = (compared == 0);
			break;
		case NONEQUAL_OP:
			result = (compared != 0);
			break;
		case LESS_OP:
			result = (compared < 0);
			break;
		case NONLESS_OP:
			result = (compared >= 0);
			break;
		case GREATER_OP:
			result = (compared > 0);
			break;
		case NONGREATER_OP:
			result = (compared <= 0);
			break;
		default:
			throw new RuntimeException("Unsupported operation " + this.comparisonOperation);
		}
		return result;
	}
	
	// used for direct key comparison
	public boolean test(T key1, T key2){
		final int compared = key1.compareTo(key2);
		boolean result = false;
		switch (this.comparisonOperation) {
		case EQUAL_OP:
			result = (compared == 0);
			break;
		case NONEQUAL_OP:
			result = (compared != 0);
			break;
		case LESS_OP:
			result = (compared < 0);
			break;
		case NONLESS_OP:
			result = (compared >= 0);
			break;
		case GREATER_OP:
			result = (compared > 0);
			break;
		case NONGREATER_OP:
			result = (compared <= 0);
			break;
		default:
			throw new RuntimeException("Unsupported operation " + this.comparisonOperation);
		}
		return result;
	}


	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(v1.toString());
		sb.append(getOperationString());
		sb.append(v2.toString());
		return sb.toString();
	}

	private String getOperationString() {
		String result = null;
		switch (this.comparisonOperation) {
			case NONEQUAL_OP:
				result = " != ";
				break;
			case EQUAL_OP:
				result = " = ";
				break;
			case LESS_OP:
				result = " < ";
				break;
			case NONLESS_OP:
				result = " >= ";
				break;
			case GREATER_OP:
				result = " > ";
				break;
			case NONGREATER_OP:
				result = " <= ";
				break;
		}
		return result;
	}

	@Override
	public void accept(PredicateVisitor pv) {
		pv.visit(this);
	}

	@Override
	public List<IPredicate> getInnerPredicates() {
		return new ArrayList<IPredicate>();
	}
}