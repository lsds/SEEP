import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

import uk.ac.imperial.lsds.seep.multi.TupleSchema;
import uk.ac.imperial.lsds.seep.multi.Utils;
import uk.ac.imperial.lsds.streamsql.op.gpu.deprecated.GPU;

public class TestByteBuffer {
	
	private static final int _default_size = 1048576 * 2;
	
	public static void main (String [] args) {
		
		ByteBuffer q = ByteBuffer.allocate(_default_size);
		ByteBuffer input = ByteBuffer.allocateDirect(_default_size);
		
		ByteBuffer output = ByteBuffer.allocateDirect(_default_size);
		ByteBuffer p = ByteBuffer.allocate(_default_size);
		
		/* Populate Java array */
		while (q.hasRemaining())
			q.putInt(1);
		q.flip();
		/* Move data to JNI land (unsafe memory) */
		/* input.put(q); */
		
		int iterations = 100000;
		double dt, rate = 0.;
		double _1GB = 1073741824.;
		long t, _t = 0;
		long count = 0;
		long bytes = 0, _bytes = 0;
		_t = System.nanoTime();
		for (int i = 0; i < iterations; i++) {
			
			input.clear();
			q.clear();
			input.put(q);
			
			output.clear();
			input.clear();
			output.put(input);
			
			p.clear();
			output.clear();
			p.put(output);
			/*
			p.clear();
			q.clear();
			if (! p.equals(q)) {
				System.err.println("Error.");
				break;
			}
			*/
			count += 1;
			bytes += _default_size;
			if (count % 100 == 0) {
				t = System.nanoTime();
				dt = (t - _t) / 1000000000.;
				rate = ((bytes - _bytes) / _1GB) / dt;
				System.out.println(String.format("[DBG] %13d bytes %10.3f GB/s", bytes, rate));
				_bytes = bytes;
				_t = t;
			}
		}
	}
}
