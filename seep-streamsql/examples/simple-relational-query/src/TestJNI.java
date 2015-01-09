import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

import uk.ac.imperial.lsds.streamsql.op.gpu.GPU;

/*
import sun.nio.ch.DirectBuffer;
import sun.misc.Unsafe;
import java.lang.reflect.Field;
*/

public class TestJNI {
	
	private static final int _default_size = 1048576;
	
	private static String load (String filename) {
		File file = new File(filename);
		try {
			byte [] bytes = Files.readAllBytes(file.toPath());
			return new String (bytes, "UTF8");
		} catch (FileNotFoundException e) {
			System.err.println(String.format("error: file %s not found", filename));
		} catch (IOException e) {
			System.err.println(String.format("error: cannot read file %s", filename));
		}
		return null;
	}
	
	/*
	public static Unsafe getUnsafeMemory () {
		try {
			Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
			theUnsafe.setAccessible(true);
			return (Unsafe) theUnsafe.get(null);
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}
	*/
	
	public static void main (String [] args) {
		
		String filename = args[0];
		String source = load (filename);
		ByteBuffer input = ByteBuffer.allocate(_default_size).order(ByteOrder.LITTLE_ENDIAN);
		ByteBuffer output = ByteBuffer.allocate(_default_size);
		while (input.hasRemaining())
			input.putInt(1);
		input.flip();
		
		byte [] inputArray = input.array();
		byte [] outputArray = output.array();
		
		/* Unsafe unsafe = getUnsafeMemory (); */
		
		int tuples = _default_size / 32;
		int threads = tuples;
		int threadsPerGroup = 128;
		int localSize = threadsPerGroup * 32;
		
		System.out.println(String.format("[DBG] %10d bytes", _default_size));
		System.out.println(String.format("[DBG] %10d tuples", tuples));
		System.out.println(String.format("[DBG] %10d threads", threads));
		System.out.println(String.format("[DBG] %10d threads/group", threadsPerGroup));
		
		GPU.getInstance().setInputDataBuffer(inputArray);
		GPU.getInstance().setOutputDataBuffer(outputArray);
		
		GPU.getInstance().getPlatform();
		GPU.getInstance().getDevice();
		GPU.getInstance().createContext();
		GPU.getInstance().createCommandQueue();
		GPU.getInstance().createProgram(source);
		GPU.getInstance().createKernel("project");
		long inputAddr = GPU.getInstance().createInputBuffer(_default_size);
		long outputAddr = GPU.getInstance().createOutputBuffer(_default_size);
		
		System.out.println("inputAddr = " + inputAddr);
		System.out.println("outputAddr = " + outputAddr);
		
		GPU.getInstance().setProjectionKernelArgs(tuples, localSize, false);
		
		int iterations = 10000;
		double dt, rate = 0.;
		double _1GB = 1073741824.;
		long t, _t = 0;
		long count = 0;
		long bytes = 0, _bytes = 0;
		/* Start experiment */
		_t = System.nanoTime();
		for (int i = 0; i < iterations; i++) {
			/* 
			 * Data movement is now handled by the JNI/C GPU code.
			 * 
			 * unsafe.copyMemory(inputArray, Unsafe.ARRAY_BYTE_BASE_OFFSET, null, inputAddr, _default_size); 
			 */
			GPU.getInstance().invokeKernel(threads, threadsPerGroup, true, false);
			
			/* unsafe.copyMemory(null, outputAddr, outputArray, Unsafe.ARRAY_BYTE_BASE_OFFSET, _default_size); */
			
			count += 1;
			bytes += _default_size;
			if (count % 100 == 0) {
				t = System.nanoTime();
				dt = (t - _t) / 1000000000.; /* In seconds */
				rate = ((bytes - _bytes) / _1GB) / dt;
				System.out.println(String.format("[DBG] %13d bytes %10.3f GB/s", bytes, rate));
				_bytes = bytes;
				_t = t;
			}
		}
		GPU.getInstance().releaseAll();
		
		input.clear();
		output.clear();
		if (input.equals(output))
			System.out.println("OK");
		else
			System.out.println("Error");
	}
}
