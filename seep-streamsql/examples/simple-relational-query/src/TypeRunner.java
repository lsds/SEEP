

import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOpTuple;
import uk.ac.imperial.lsds.seep.operator.compose.multi.TupleObject;
import uk.ac.imperial.lsds.streamsql.expressions.Addition;
import uk.ac.imperial.lsds.streamsql.expressions.Constant;
import uk.ac.imperial.lsds.streamsql.expressions.IValueExpression;
import uk.ac.imperial.lsds.streamsql.types.FloatType;
import uk.ac.imperial.lsds.streamsql.types.IntegerType;
import uk.ac.imperial.lsds.streamsql.types.PrimitiveType;


public class TypeRunner {

	public static void main(String[] args) {

		PrimitiveType n = new IntegerType(1);
		PrimitiveType f = new FloatType(5f);

		MultiOpTuple tuple1 = new MultiOpTuple(new TupleObject[]{n,f}, 100, 101); 
		MultiOpTuple tuple2 = (MultiOpTuple) tuple1.clone(); 
		
		IValueExpression<IntegerType> v1 = new Constant<IntegerType>(new IntegerType(1));
		IValueExpression<IntegerType> v2 = new Constant<IntegerType>(new IntegerType(1));
		
		IValueExpression<IntegerType> [] t = new IValueExpression[2];
		t[0] = v1;
		t[1] = v2;
		
		IValueExpression<IntegerType> exp = new Addition<IntegerType>(t);
		
		System.out.println(exp.toString());
		System.out.println(exp.eval(null).toString());

	}

}
