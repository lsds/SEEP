import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.TheGPU;
import uk.ac.imperial.lsds.seep.multi.TupleSchema;
import uk.ac.imperial.lsds.streamsql.op.gpu.stateless.DummyKernel;

public class TestGPUDataMovement {
	
	private static final int _default_size = 1048576;
	
	public static void main (String [] args) {
		
		int inputSize = _default_size;
		if (args.length > 0)
			inputSize = Integer.parseInt(args[0]);
		
		String filename = "/home/akolious/seep/seep-streamsql/examples/simple-relational-query/src/kernels/DummyKernel.cl";
		
		int [] offsets = {0, 8, 12, 16, 20, 24, 28 }; /* First attribute is the timestamp, followed by 6 integers */
		int byteSize = 32;
		ITupleSchema schema = new TupleSchema (offsets, byteSize);
		
		ByteBuffer input = ByteBuffer.allocate(inputSize).order(ByteOrder.LITTLE_ENDIAN);
		ByteBuffer output = ByteBuffer.allocate(inputSize);
		while (input.hasRemaining())
			input.putInt(1);
		input.flip();
		
		byte []  inputArray =  input.array();
		byte [] outputArray = output.array();
		
		TheGPU.getInstance().init(1);
		
		DummyKernel kernel = new DummyKernel (schema, filename, inputSize);
		kernel.setup(null, null);
		int qid = kernel.getQueryId();
		
		TheGPU.getInstance().setInputBuffer(qid, 0, inputArray, 0, inputSize);
		TheGPU.getInstance().setOutputBuffer(qid, 0, outputArray);
		
		System.out.println(String.format("[DBG] %10d bytes", inputSize));
		
		TheGPU.getInstance().copyInputBuffers(qid);
		
		int iterations = 1000000;
		double dt, rate = 0.;
		double _1GB = 1073741824.;
		long t, _t = 0;
		long count = 0;
		long bytes = 0, _bytes = 0;
		/* Start experiment */
		_t = System.nanoTime();
		for (int i = 0; i < iterations; i++) {
		
			// TheGPU.getInstance().moveInputAndOutputBuffers(qid);
			
			count += 1;
			bytes += inputSize;
			if (count % 10000 == 0) {
				t = System.nanoTime();
				dt = (t - _t) / 1000000000.; /* In seconds */
				rate = ((bytes - _bytes) / _1GB) / dt;
				System.out.println(String.format("[DBG] %13d bytes %10.3f GB/s", bytes, rate));
				_bytes = bytes;
				_t = t;
			}
		}
		TheGPU.getInstance().free();
		System.out.println("Bye.");
	}
}
