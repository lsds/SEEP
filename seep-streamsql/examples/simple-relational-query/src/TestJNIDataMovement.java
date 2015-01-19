import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import uk.ac.imperial.lsds.streamsql.op.gpu.deprecated.GPU;

public class TestJNIDataMovement {
	
	private static final int _default_size = 1048576;
	
	public static void main (String [] args) {
		
		ByteBuffer input = ByteBuffer.allocate(_default_size).order(ByteOrder.LITTLE_ENDIAN);
		ByteBuffer output = ByteBuffer.allocate(_default_size);
		while (input.hasRemaining())
			input.putInt(1);
		input.flip();
		
		byte [] inputArray = input.array();
		byte [] outputArray = output.array();
		
		System.out.println(String.format("[DBG] %10d bytes", _default_size));
		
		GPU.getInstance().setInputDataBuffer(inputArray);
		GPU.getInstance().setOutputDataBuffer(outputArray);
		
		GPU.getInstance().getPlatform();
		GPU.getInstance().getDevice();
		GPU.getInstance().createContext();
		GPU.getInstance().createCommandQueue();
		GPU.getInstance().createInputBuffer(_default_size);
		GPU.getInstance().createOutputBuffer(_default_size);
		
		int iterations = 100000;
		double dt, rate = 0.;
		double _1GB = 1073741824.;
		long t, _t = 0;
		long count = 0;
		long bytes = 0, _bytes = 0;
		/* Start experiment */
		_t = System.nanoTime();
		for (int i = 0; i < iterations; i++) {
			
			/* Data movement in */
			
			// GPU.getInstance().invokeInputDataMovementCallback();
			// GPU.getInstance().invokeGPUWrite();
			
			// GPU.getInstance().invokeAlternativeInputDataMovementCallback();
			// GPU.getInstance().invokeAlternativeGPUWrite();
			
			/* Data movement out */
			
			// GPU.getInstance().invokeGPURead();
			// GPU.getInstance().invokeOutputDataMovementCallback();
			
			// GPU.getInstance().invokeAlternativeGPURead();
			// GPU.getInstance().invokeAlternativeOutputDataMovementCallback();
			
			GPU.getInstance().invokeNullKernel();
			
			count += 1;
			bytes += _default_size;
			if (count % 1000 == 0) {
				t = System.nanoTime();
				dt = (t - _t) / 1000000000.; /* In seconds */
				rate = ((bytes - _bytes) / _1GB) / dt;
				System.out.println(String.format("[DBG] %13d bytes %10.3f GB/s", bytes, rate));
				_bytes = bytes;
				_t = t;
			}
		}
		GPU.getInstance().releaseAll();
		System.out.println("Bye.");
		/*
		input.clear();
		output.clear();
		if (input.equals(output))
			System.out.println("OK");
		else
			System.out.println("Error");
		*/
	}
}
