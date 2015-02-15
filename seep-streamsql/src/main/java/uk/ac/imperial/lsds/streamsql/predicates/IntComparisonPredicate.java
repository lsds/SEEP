package uk.ac.imperial.lsds.streamsql.predicates;

import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntExpression;
import uk.ac.imperial.lsds.streamsql.visitors.PredicateVisitor;

public class IntComparisonPredicate implements IPredicate {

	/*
	 * Values compared by this predicate
	 */
	IntExpression v1;
	IntExpression v2;

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
	
	public IntComparisonPredicate(int comparisonOperation, IntExpression v1, IntExpression v2) {
		this.comparisonOperation = comparisonOperation;
		this.v1 = v1;
		this.v2 = v2;
	}

	public IntComparisonPredicate(IntExpression v1, IntExpression v2) {
		this(EQUAL_OP, v1, v2);
	}
	
	public String getComparisonOperator() {
		return getOperationString();
	}

	public int getOperator(boolean inverse) {
		int result = 0;
		if (inverse)
			switch (this.comparisonOperation) {
			case NONEQUAL_OP:
				result = NONEQUAL_OP;
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
			default:
				result = EQUAL_OP;
			}
		else
			result = this.comparisonOperation;
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
			default:
				result = " = ";
		}
		return result;
	}

	@Override
	public void accept(PredicateVisitor pv) {
		pv.visit(this);
	}

	@Override
	public boolean satisfied(IQueryBuffer buffer, ITupleSchema schema, int offset) {
		int val1 = v1.eval(buffer, schema, offset);
		int val2 = v2.eval(buffer, schema, offset);

		switch (this.comparisonOperation) {
			case EQUAL_OP:
				return val1 == val2;
			case NONEQUAL_OP:
				return val1 != val2;
			case LESS_OP:
				return val1 < val2;
			case NONLESS_OP:
				return val1 >= val2;
			case GREATER_OP:
				return val1 > val2;
			case NONGREATER_OP:
				return val1 <= val2;
			default:
				throw new RuntimeException("Unsupported operation " + this.comparisonOperation);
		}
	}

	@Override
	public boolean satisfied(IQueryBuffer firstBuffer,
			ITupleSchema firstSchema, int firstOffset,
			IQueryBuffer secondBuffer, ITupleSchema secondSchema,
			int secondOffset) {
		int val1 = v1.eval(firstBuffer, firstSchema, firstOffset);
		int val2 = v2.eval(secondBuffer, secondSchema, secondOffset);

		switch (this.comparisonOperation) {
			case EQUAL_OP:
				return val1 == val2;
			case NONEQUAL_OP:
				return val1 != val2;
			case LESS_OP:
				return val1 < val2;
			case NONLESS_OP:
				return val1 >= val2;
			case GREATER_OP:
				return val1 > val2;
			case NONGREATER_OP:
				return val1 <= val2;
			default:
				throw new RuntimeException("Unsupported operation " + this.comparisonOperation);
		}
	}

	@Override
	public int getNumPredicates() {
		return 0;
	}
}
