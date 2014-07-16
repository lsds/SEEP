package uk.ac.imperial.lsds.streamsql.op;

import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;

public interface IStreamSQLOperator {
	
	public void accept(OperatorVisitor ov);
	
}
