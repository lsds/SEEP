import java.util.ArrayList;
import java.util.List;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOpTuple;
import uk.ac.imperial.lsds.seep.operator.compose.multi.SubQueryBuffer;

public class Runner {

	public class MyFiller implements Runnable {
		
		SubQueryBuffer buffer;
		
		public MyFiller(SubQueryBuffer buffer) {
			this.buffer = buffer;
		}
		
		public void run() {
			for (int i = 0; i < 2000; i++)
				buffer.add(MultiOpTuple.newInstance());
		}
		
	}
	
	public static void main(String[] args) {
		MultiOpTuple t1 = MultiOpTuple.newInstance();
		MultiOpTuple t2 = MultiOpTuple.newInstance();
		MultiOpTuple t3 = MultiOpTuple.newInstance();
		MultiOpTuple t4 = MultiOpTuple.newInstance();
		
		SubQueryBuffer b = new SubQueryBuffer(2);
		
//		(new Thread())
		
		b.add(t1);
		b.get(2);
		
		MultiOpTuple[] l = new MultiOpTuple[] {t3, t4};
		
		MultiOpTuple[] l2 = b.add(l);
		
		System.out.println(l2);
		
	}

}
