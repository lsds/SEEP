package uk.ac.imperial.lsds.seep.api;

import java.util.List;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;

public class BaseTest implements QueryComposer{

	@Override
	public LogicalSeepQuery compose() {
		// Declare Source
		LogicalOperator src = queryAPI.newStatelessSource(new Source(), -1);
		// Declare processor
		LogicalOperator p = queryAPI.newStatelessOperator(new Processor(), 1);
		// Declare sink
		LogicalOperator snk = queryAPI.newStatelessSink(new Sink(), -2);
		
		/** Connect operators **/
		src.connectTo(p, 0);
		p.connectTo(snk, 0);
		
		return QueryBuilder.build();
	}

	
	class Source implements SeepTask{
		@Override
		public void setUp() {
			// TODO Auto-generated method stub	
		}
		@Override
		public void processData(DataTuple data) {
			// TODO Auto-generated method stub	
		}
		@Override
		public void processData(List<DataTuple> dataList) {
			// TODO Auto-generated method stub	
		}
		@Override
		public void close() {
			// TODO Auto-generated method stub	
		}
	}
	
	class Processor implements SeepTask{
		@Override
		public void setUp() {
			// TODO Auto-generated method stub	
		}
		@Override
		public void processData(DataTuple data) {
			// TODO Auto-generated method stub	
		}
		@Override
		public void processData(List<DataTuple> dataList) {
			// TODO Auto-generated method stub	
		}
		@Override
		public void close() {
			// TODO Auto-generated method stub	
		}
	}
	
	class Sink implements SeepTask{
		@Override
		public void setUp() {
			// TODO Auto-generated method stub	
		}
		@Override
		public void processData(DataTuple data) {
			// TODO Auto-generated method stub	
		}
		@Override
		public void processData(List<DataTuple> dataList) {
			// TODO Auto-generated method stub	
		}
		@Override
		public void close() {
			// TODO Auto-generated method stub	
		}
	}
}
