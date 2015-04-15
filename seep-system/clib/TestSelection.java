import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

import java.util.Arrays;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class TestSelection {
	
	public static final int _default_size = 1048576;
	
	public static String load (String filename) {
		
		File file = new File(filename);
		try {
			byte [] bytes = Files.readAllBytes(file.toPath());
			return new String (bytes, "UTF8");
		} catch (FileNotFoundException e) {
			System.err.println(String.format("error: file %s not found", 
				filename));
		} catch (IOException e) {
			System.err.println(String.format("error: cannot read file %s", 
				filename));
		}
		return null;
	}
	
	private static boolean isPowerOfTwo (int n) {
		if (n == 0)
			return false;
		while (n != 1) {
			if (n % 2 != 0)
				return false;
			n = n/2;
		}
		return true;
    }
	
	public static void main (String [] mainArgs) {
		
		System.out.println("[Test]");
		
		TheGPU.getInstance().init(1);
		
		/* Configuration variables */
		int inputSize = _default_size;
		
		int tupleSize = 32;
		int tuples = inputSize / tupleSize;
		/* GPU kernel state size must be a power of two */
		int tuples_ = tuples;
		while (! isPowerOfTwo(tuples_))
			tuples_ += 1;
		System.out.println(String.format("[DBG] #tuples (~2) = %6d", tuples ));
		System.out.println(String.format("[DBG] #tuples (^2) = %6d", tuples_));
		
		int outputSize = inputSize;
		
		int [] threadsPerGroup = new int [2];
		threadsPerGroup[0] = 256; /* This is a constant */
		threadsPerGroup[1] = 256;
		
		int tuplesPerThread = 2;
		
		int [] threads = new int [2];
		threads[0] = tuples_ / tuplesPerThread;
		threads[1] = tuples_ / tuplesPerThread;
		
		int [] args = new int[3];
		args[0] = inputSize;
		args[1] = tuples;
		args[2] = 4 * threadsPerGroup[0] * tuplesPerThread;
		
		String filename = "Selection.cl";
		String source = load(filename);
		
		int qid = TheGPU.getInstance().getQuery(source, 2, 1, 4);
		
		TheGPU.getInstance().setInput(qid, 0, inputSize);
		
		int ngroups = threads[0] / threadsPerGroup[0];
		TheGPU.getInstance().setOutput(qid, 0, 4 * tuples_, 0, 0, 1, 0); /* flags     */
		TheGPU.getInstance().setOutput(qid, 1, 4 * tuples_, 0, 1, 0, 0); /* offsets   */
		TheGPU.getInstance().setOutput(qid, 2, 4 * ngroups, 0, 1, 0, 0); /* paritions */
		TheGPU.getInstance().setOutput(qid, 3,  outputSize, 1, 0, 0, 1);
		
		/* For debugging purposes */
		/*
		byte [] flags      = new byte [4 * tuples_];
		byte [] offsets    = new byte [4 * tuples_];
		byte [] partitions = new byte [4 * ngroups];
		*/
		
		TheGPU.getInstance().setKernelSelect (qid, args);
		/* Prepare data */
		byte [] input = new byte [inputSize];
		ByteBuffer inputBuffer = ByteBuffer.wrap(input); 
		/* The default order is BIG_ENDIAN */
		
		for (int i = 0; i < tuples; i++) {
			/*
			 * Assume input schema is <long, int, int, float, int, long>
			 */
			inputBuffer.putLong(System.nanoTime());
			inputBuffer.putInt(i);
			inputBuffer.putInt(1);
			inputBuffer.putFloat(2);
			inputBuffer.putInt(1);
			inputBuffer.putLong(1);
		}
		inputBuffer.clear();
		for (int i = 0; i < 20; i++) {
			System.out.println(String.format("%02d: <%20d,%2d,%2d,%3.1f,%2d,%2d>", 
			i,
			inputBuffer.getLong(),
			inputBuffer.getInt(),
			inputBuffer.getInt(),
			inputBuffer.getFloat(),
			inputBuffer.getInt(),
			inputBuffer.getLong()
			));
		}
		byte [] output = new byte [outputSize];
		Arrays.fill(output, (byte) 0);
		TheGPU.getInstance().setInputBuffer(qid, 0, input);
		/* For debugging purposes */
		/*
		TheGPU.getInstance().setOutputBuffer(qid, 0, flags);
		TheGPU.getInstance().setOutputBuffer(qid, 1, offsets);
		TheGPU.getInstance().setOutputBuffer(qid, 2, partitions);
		*/
		TheGPU.getInstance().setOutputBuffer(qid, 3, output);
		
		TheGPU.getInstance().execute(qid, threads, threadsPerGroup);
		
		if (Arrays.equals(input, output)) {
			System.out.println("OK");
		} else {
			System.err.println("Error");
			for (int i = 0; i < inputSize; i++) {
				if (input[i] != output[i]) {
					System.err.println("@" + i);
					break;
				}
			}
		}
		/* Print output */
		ByteBuffer outputBuffer = ByteBuffer.wrap(output).order(ByteOrder.BIG_ENDIAN);
		for (int i = 0; i < 20; i++) {
			System.out.println(String.format("%02d: <%20d,%2d,%2d,%3.1f,%2d,%2d>", 
			i,
			outputBuffer.getLong(),
			outputBuffer.getInt(),
			outputBuffer.getInt(),
			outputBuffer.getFloat(),
			outputBuffer.getInt(),
			outputBuffer.getLong()
			));
		}
		
		/* For debugging purposes */
		/*
		ByteBuffer flagsBuffer   = ByteBuffer.wrap(flags  ).order(ByteOrder.LITTLE_ENDIAN);
		ByteBuffer offsetsBuffer = ByteBuffer.wrap(offsets).order(ByteOrder.LITTLE_ENDIAN);
		for (int i = 0; i < tuples_; i++) {
			System.out.println(String.format("%02d: flag %2d offset %7d",
			i,
			flagsBuffer.getInt(),
			offsetsBuffer.getInt()
			));
		}
		ByteBuffer partitionsBuffer = 
			ByteBuffer.wrap(partitions).order(ByteOrder.LITTLE_ENDIAN);
		for (int i = 0; i < ngroups; i++) {
			System.out.println(String.format("%02d: group offset %7d",
			i,
			partitionsBuffer.getInt()
			));
		}
		*/
		TheGPU.getInstance().free();
	}
}

