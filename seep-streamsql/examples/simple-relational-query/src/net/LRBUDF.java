package net;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition;
import uk.ac.imperial.lsds.streamsql.expressions.Expression;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatColumnReference;


public class LRBUDF implements IMicroOperatorCode {

	public LRBUDF (WindowDefinition window, FloatColumnReference floatColumnReference, Expression [] expressions) {
	}

	@Override
	public void processData(WindowBatch windowBatch, IWindowAPI api) {
	}

	@Override
	public void processData(WindowBatch firstWindowBatch,
			WindowBatch secondWindowBatch, IWindowAPI api) {
		throw new UnsupportedOperationException("LRBUDF is a single input operator and does not operate on two streams");
	}
}
