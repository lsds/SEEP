package uk.ac.imperial.lsds.seep.gc14.operator;

import java.util.List;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;

public class Sink implements StatelessOperator{

	private static final long serialVersionUID = 1L;

	//time control stuff
	int c = 0;
	int lat_sampler = 0;
	int sec = 0;
	long init = System.currentTimeMillis();
	
	@Override
	public void processData(DataTuple data) {
		
		System.out.println("$: "+data);
		
		c++;
		long currentTime = System.currentTimeMillis();
		if((currentTime - init) > 1000){
			System.out.println("SNK "+sec+" "+c+" ");
			c = 0;
			sec++;
			init = System.currentTimeMillis();
		}
	}

	@Override
	public void processData(List<DataTuple> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setUp() {
		// TODO Auto-generated method stub
		
	}

}
