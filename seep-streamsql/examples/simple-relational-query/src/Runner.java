import java.util.ArrayList;
import java.util.List;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.compose.multi.SubQueryBuffer;

public class Runner {

	public static void main(String[] args) {
		DataTuple t1 = new DataTuple();
		DataTuple t2 = new DataTuple();
		DataTuple t3 = new DataTuple();
		DataTuple t4 = new DataTuple();
		
		SubQueryBuffer b = new SubQueryBuffer(2);
		
		b.add(t1);
		
		List<DataTuple> l = new ArrayList<>();
		l.add(t3);
		l.add(t4);
		
		List<DataTuple> l2 = b.add(l);
		
		System.out.println(l2);
		
	}

}
