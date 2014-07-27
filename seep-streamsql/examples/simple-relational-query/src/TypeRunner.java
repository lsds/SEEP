

import uk.ac.imperial.lsds.streamsql.expressions.Addition;
import uk.ac.imperial.lsds.streamsql.expressions.Constant;
import uk.ac.imperial.lsds.streamsql.expressions.IValueExpression;
import uk.ac.imperial.lsds.streamsql.types.IntegerType;


public class TypeRunner {

	public static void main(String[] args) {
		
		IValueExpression<IntegerType> v1 = new Constant<IntegerType>(new IntegerType(1));
		IValueExpression<IntegerType> v2 = new Constant<IntegerType>(new IntegerType(1));
		
		IValueExpression<IntegerType> exp = new Addition<IntegerType>(new IValueExpression[] {v1, v2});
		
		System.out.println(exp.toString());
		System.out.println(exp.eval(null).toString());

	}

}
