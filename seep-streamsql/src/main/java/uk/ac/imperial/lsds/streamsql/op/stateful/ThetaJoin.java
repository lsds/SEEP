
package uk.ac.imperial.lsds.streamsql.op.stateful;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.multi.IJoinMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.predicates.IPredicate;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;

public class ThetaJoin implements IStreamSQLOperator, IJoinMicroOperatorCode {

	private static Logger LOG = LoggerFactory.getLogger(ThetaJoin.class);

	private IPredicate predicate;
	
	public ThetaJoin(IPredicate predicate) {
		this.predicate = predicate;
	}

	@Override
	public void accept(OperatorVisitor ov) {
		ov.visit(this);
	}

	@Override
	public void processData(WindowBatch firstWindowBatch,
			WindowBatch secondWindowBatch, IWindowAPI api) {
		// TODO Auto-generated method stub
		
	}
	
}
