package uk.ac.imperial.lsds.streamsql.op.gpu.stateless;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;

import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;

public class SelectionKernel implements IStreamSQLOperator, IMicroOperatorCode {

	@Override
	public void processData (WindowBatch windowBatch, IWindowAPI api) {
	}

	@Override
	public void accept (OperatorVisitor visitor) {
		visitor.visit(this);
	}	
}
