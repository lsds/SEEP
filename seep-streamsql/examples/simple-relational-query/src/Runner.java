import java.util.ArrayList;
import java.util.List;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.compose.multi.SubQueryBuffer;

public class Runner {

	public class MyFiller implements Runnable {
		
		SubQueryBuffer buffer;
		
		public MyFiller(SubQueryBuffer buffer) {
			this.buffer = buffer;
		}
		
		public void run() {
			for (int i = 0; i < 2000; i++)
				buffer.add(new DataTuple());
		}
		
	}
	
	public static void main(String[] args) {
		DataTuple t1 = new DataTuple();
		DataTuple t2 = new DataTuple();
		DataTuple t3 = new DataTuple();
		DataTuple t4 = new DataTuple();
		
		SubQueryBuffer b = new SubQueryBuffer(2000);
		
//		(new Thread())
		
		b.add(t1);
		b.get(2);
		
		List<DataTuple> l = new ArrayList<>();
		l.add(t3);
		l.add(t4);
		
		List<DataTuple> l2 = b.add(l);
		
		System.out.println(l2);
		
	}

}
