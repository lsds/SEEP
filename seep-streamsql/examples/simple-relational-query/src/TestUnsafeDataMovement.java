import java.lang.reflect.Field;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.TheGPU;
import uk.ac.imperial.lsds.seep.multi.TupleSchema;
import uk.ac.imperial.lsds.streamsql.op.gpu.stateless.DummyKernel;
import sun.nio.ch.DirectBuffer;

public class TestUnsafeDataMovement {
	
	private static final int _default_size = 1048576 / 1;
	private static long bytes = 0L;
	
	private static final int _nq_ = 1;
	
	private static final boolean isDirect = false;
	
	class MyMonitor implements Runnable {
		
		double dt, rate = 0.;
		
		double _1GB = 1073741824.;
		long t, _t = 0;
		long _bytes = 0;
		
		public MyMonitor () {
		}
		
		@Override
		public void run () {
			while (true) {
				
				try { 
					Thread.sleep(1000L); 
				} catch (Exception e) 
				{}
				
				t = System.nanoTime();
				if (_t > 0) {
					dt = (t - _t) / 1000000000.; /* In seconds */
					rate = ((bytes - _bytes) / _1GB) / dt;
					System.out.println(String.format("[DBG] %13d bytes %10.3f GB/s", bytes, rate));
				}
				_bytes = bytes;
				_t = t;
			}
		}
	}
	
	public static void main (String [] args) {
		
		int inputSize = _default_size;
		if (args.length > 0)
			inputSize = 1024 * Integer.parseInt(args[0]); /* Argument expressed in KB */
		
		TestUnsafeDataMovement test = new TestUnsafeDataMovement ();
		
		String filename = "/home/akolious/seep/seep-streamsql/examples/simple-relational-query/src/kernels/DummyKernel.cl";
		
		int [] offsets = {0, 8, 12, 16, 20, 24, 28 }; /* First attribute is the time stamp, followed by 6 integers */
		int byteSize = 32;
		ITupleSchema schema = new TupleSchema (offsets, byteSize);
		
		ByteBuffer []  inputs = new ByteBuffer [_nq_];
		ByteBuffer [] outputs = new ByteBuffer [_nq_];
		
		if (! isZeroCopySize(inputSize)) {
			System.err.println("error: invalid buffer size");
			System.exit(1);
		}
		/* Trying to find a zero copy address */
		for (int i = 0; i < _nq_; i++) {
			
			if (isDirect) {
				inputs [i] = ByteBuffer.allocateDirect(inputSize);
				/*
				System.out.print("Trying to find a zero-copy buffer...");
				inputs [i] = getZeroCopyByteBuffer(inputSize);
				System.out.println("OK");
				*/
				
				outputs[i] = ByteBuffer.allocateDirect(inputSize);
				/*
				System.out.print("Trying to find a zero-copy buffer...");
				outputs[i] = getZeroCopyByteBuffer(inputSize);
				System.out.println("OK");
				*/
			} else {
				inputs [i] = ByteBuffer.allocate(inputSize);
				outputs[i] = ByteBuffer.allocate(inputSize);
			}
		}
		
		/* 
		 * See late initialisation
		 * 
		 * while (input.hasRemaining()) input.putInt(1);
		 * input.flip();
		 */
		
		TheGPU.getInstance().init(_nq_);
		
		DummyKernel [] kernels = new DummyKernel [_nq_];
		int [] qids = new int [_nq_];
		for (int i = 0; i < _nq_; i++) {
			kernels[i] = new DummyKernel (schema, filename, inputSize);
			if (isDirect) 
				kernels[i].setup(inputs[i], outputs[i]);
			else
				kernels[i].setup(null, null);
			qids[i] = kernels[i].getQueryId();
		}
		
		if (! isDirect) {
			for (int i = 0; i < _nq_; i++) {
				TheGPU.getInstance().setInputBuffer(qids[i], 0, inputs[i].array(), 0, inputSize);
				TheGPU.getInstance().setOutputBuffer(qids[i], 0, outputs[i].array());
			}
		}
		/*
		if (isDirect) {
			for (int i = 0; i < _nq_; i++) {
				setZeroCopyByteBufferAddress(inputs [i], TheGPU.getInstance().getInputAddress (qids[i], 0));
				setZeroCopyByteBufferAddress(outputs[i], TheGPU.getInstance().getOutputAddress(qids[i], 0));
			}
		}
		*/
		/* Late initialisation */
		for (int i = 0; i < _nq_; i++) {
			while (inputs[i].hasRemaining())
				inputs[i].putInt(1);
			inputs[i].flip();
		}
		
		System.out.println(String.format("[DBG] %10d bytes", inputSize));
		
		TheGPU.getInstance().bind(1);
		
		Thread monitor = new Thread(test.new MyMonitor());
		monitor.setName("MyMonitor");
		monitor.start();
		
		long iterations = Long.MAX_VALUE; // 200000; // Long.MAX_VALUE;
		long count = 0;
		
		int threads = inputSize / 4;
		int threadsPerGroup = 256;
		
		System.out.println(String.format("[DBG] %10d threads", threads));
		System.out.println(String.format("[DBG] %10d threads/group", threadsPerGroup));
		
		/* Start experiment */
		
		/* Copy input */
		for (int i = 0; i < _nq_; i++) {
			TheGPU.getInstance().copyInputBuffers(qids[i]);
		}
		
		while (count < iterations) {
			
//			inputs[0].clear();
//			while (inputs[0].hasRemaining())
//				inputs[0].putInt((int) count);
//			inputs[0].flip();
			
			
			for (int i = 0; i < _nq_; i++) {
				
				// TheGPU.getInstance().moveInputAndOutputBuffers(qids[i]);
				TheGPU.getInstance().execute(qids[i], threads, threadsPerGroup);
			}
			
			count += 1;
			bytes += _nq_ * inputSize;
			
			/*
			inputs[0].clear();
			outputs[0].clear();
			if (inputs[0].equals(outputs[0])) System.out.println("OK");
			else System.out.println("Error");
			System.out.println(String.format("[DBG] output[0] = %d", outputs[0].getInt()));
			*/
//			outputs[0].clear();
//			// if (inputs[0].equals(outputs[0])) System.out.println("OK");
//			// else System.out.println("Error");
//			System.out.println(String.format("[DBG] output[0] = %d", outputs[0].getInt()));
		}
		
		TheGPU.getInstance().free();
		System.out.println("Bye.");
	}
	
	private static ByteBuffer getZeroCopyByteBuffer (int inputSize) {
		int size = inputSize + inputSize;
		ByteBuffer buffer = ByteBuffer.allocateDirect(size);
		long address = ((DirectBuffer) buffer).address();
		int offset = 0;
		while (! isZeroCopyAddress(address)) {
			address += 1;
			++ offset;
		}
		if (offset > inputSize) {
			System.err.println("error: invalid buffer address");
			System.exit(1);
		}
		System.out.print(String.format(" [offset %d] ", offset));
		buffer.position(offset);
		buffer.limit(offset + inputSize);
		return buffer;
	}
	
	private static void setZeroCopyByteBufferAddress (ByteBuffer b, long pinnedMemory) {
		Field address = null;
		try {
			address = Buffer.class.getDeclaredField("address");
		} catch (NoSuchFieldException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		address.setAccessible(true);
		try {
			address.setLong(b, pinnedMemory);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static boolean isZeroCopyAddress (long address) {
		if (address % 4 != 0) /* Page alignment*/
			return false;
		return true;
	}
	
	private static boolean isZeroCopySize (long size) {
		if (size % 64 != 0) /* Cache alignment */
			return false;
		return true;
	}
}
