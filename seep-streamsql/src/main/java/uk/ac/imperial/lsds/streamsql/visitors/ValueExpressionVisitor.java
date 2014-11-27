package uk.ac.imperial.lsds.streamsql.visitors;

import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatConstant;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntAddition;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntConstant;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntDivision;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntMultiplication;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntSubtraction;
import uk.ac.imperial.lsds.streamsql.expressions.elong.LongColumnReference;

public interface ValueExpressionVisitor {

	public void visit(FloatConstant floatConstant);

	public void visit(IntAddition add);

	public void visit(IntColumnReference cr);

	public void visit(IntDivision dvsn);

	public void visit(IntMultiplication mult);

	public void visit(IntSubtraction sub);

	public void visit(IntConstant vs);

	public void visit(FloatColumnReference cr);

	public void visit(LongColumnReference cr);
}
