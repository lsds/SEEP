package uk.ac.imperial.lsds.streamsql.op.stateful;

import java.util.List;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.API;
import uk.ac.imperial.lsds.seep.operator.StatefulOperator;
import uk.ac.imperial.lsds.seep.state.State;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.op.WindowOperator;
import uk.ac.imperial.lsds.streamsql.predicates.IPredicate;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;
import uk.ac.imperial.lsds.streamsql.windows.Window;

public class ThetaJoin implements StatefulOperator, IStreamSQLOperator, WindowOperator {

	private static Logger LOG = LoggerFactory.getLogger(Distinct.class);

	private static final long serialVersionUID = 1L;

	private int stream1Id;

	private int stream2Id;

	private Window window1;
	
	private Window window2;
	
//	private transient JoinState state;

	private IPredicate predicate;

	public ThetaJoin() {
		
	}
	
	@Override
	public void setUp() {
		this.window1.registerCallbackEvaluateWindow(this);
		this.window1.registerCallbackEnterWindow(this);
		this.window1.registerCallbackExitWindow(this);
		this.window2.registerCallbackEvaluateWindow(this);
		this.window2.registerCallbackEnterWindow(this);
		this.window2.registerCallbackExitWindow(this);
	}


	@Override
	public void accept(OperatorVisitor ov) {
		ov.visit(this);
	}

	@Override
	public void processData(DataTuple data, API api) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processData(List<DataTuple> dataList, API api) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enteredWindow(DataTuple tuple, API api) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitedWindow(DataTuple tuple, API api) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void evaluateWindow(Queue<DataTuple> dataList, API api) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setState(State state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public State getState() {
		// TODO Auto-generated method stub
		return null;
	}

	
	
}
