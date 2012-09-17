package seep.operator.collection.testing;

import seep.comm.tuples.Seep;
import seep.comm.tuples.Seep.DataTuple.Builder;
import seep.operator.Operator;
import seep.operator.StatelessOperator;

@SuppressWarnings("serial")
public class TestSource extends Operator implements StatelessOperator{

	public TestSource(int opID) {
		super(opID);
		subclassOperator = this;
		
		/*
		ContentBasedFilter cbf = new ContentBasedFilter("getInt");
		// we know a priori for this test that there are three downstreams
		cbf.routeValueToDownstream(RouteOperator.EQ, 0, 0);
		cbf.routeValueToDownstream(RouteOperator.EQ, 1, 1);
		setDispatchPolicy(DispatchPolicy.CONTENT_BASED, cbf);
		*/
		
	}

	@Override
	public void processData(Seep.DataTuple dt) {
		int value = 0;
		int c = 0;
		int fake = 0;
		while(true){
			Seep.DataTuple.Builder data = Seep.DataTuple.newBuilder(dt);
			if(c == 2){
				value = 1;
				c = 0;
			}
//			data.setInt(value);
			value = 0;
			c++;
			sendDown(data.build());
			fake++;
			System.out.println("Sent: "+fake);
			try{
				Thread.sleep(500);
			} 
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean isOrderSensitive() {
		// TODO Auto-generated method stub
		return false;
	}
}
