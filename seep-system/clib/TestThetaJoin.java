import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

import java.util.Arrays;
import java.util.Random;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class TestThetaJoin {
	
	public static final int _default_size = 32768;
	
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
	
	/* 
 	 * TBD items:
 	 *
 	 * 1. Make sure start and end pointers are always normalised
 	 *
 	 * 2. Compute start and end pointers for range-based windows  
 	 *
 	 */
	public static void initWindowPointers (
		byte [] startPtrs, 
		byte [] endPtrs,
		int batchStartPointer,
		int batchEndPointer
	) {
		
		ByteBuffer b = ByteBuffer.wrap(startPtrs).order(ByteOrder.LITTLE_ENDIAN);
		ByteBuffer d = ByteBuffer.wrap(  endPtrs).order(ByteOrder.LITTLE_ENDIAN);
		
		if (batchStartPointer < 0 && batchEndPointer < 0)
			return ;
		for (int i = 0; i < startPtrs.length; i += 4) {
			b.putInt(batchStartPointer);
			d.putInt(batchEndPointer);
		}
		
		return ;
	}
	
	public static void printWindowPointers(byte [] startPtrs, byte [] endPtrs) {
		
		ByteBuffer b = ByteBuffer.wrap(startPtrs).order(ByteOrder.LITTLE_ENDIAN);
		ByteBuffer d = ByteBuffer.wrap(  endPtrs).order(ByteOrder.LITTLE_ENDIAN);
		int wid = 0;
		while (b.hasRemaining() && d.hasRemaining()) {
			System.out.println(String.format("w %02d: starts %10d ends %10d", 
				wid, b.getInt(), d.getInt()));
				wid ++;
		}
	}
	
	private static boolean isPowerOfTwo (int n) {
		if (n == 0)
			return false;
		while (n != 1) {
			if (n % 2 != 0)
				return false;
			n = n / 2;
		}
		return true;
	}
	
	public static void main (String [] mainArgs) {
		
		System.out.println("[Test]");
		
		TheGPU.getInstance().init(1);
		
		/* Configuration variables */
		int l_inputSize = _default_size;
		int r_inputSize = _default_size;
		
		int l_inputTupleSize = 32;
		int r_inputTupleSize = 32;
		
		int l_tuples = l_inputSize / l_inputTupleSize;
		int r_tuples = r_inputSize / r_inputTupleSize;
		
		System.out.println(String.format("[DBG] %10d left tuples",  l_tuples));
		System.out.println(String.format("[DBG] %10d right tuples", r_tuples));
		
		/* Compute start and end pointers 
 		 * 
 		 * Match every tuple in left stream with a window in right stream 
 		 */
		byte [] startPtrs = new byte [l_tuples * 4];
		byte []   endPtrs = new byte [l_tuples * 4];
		
		initWindowPointers(startPtrs, endPtrs, 0, r_inputSize);
		/* Debugging */
		/* printWindowPointers (startPtrs, endPtrs); */
		
		/* Determine #threads */
		int [] threadsPerGroup = new int [3];
		threadsPerGroup[0] = 256;
		/* For scan and compact kernels */
		threadsPerGroup[1] = 256;
		threadsPerGroup[2] = 256;
		
		int tuplesPerThread = 2;

		int l_tuples_ = l_tuples;
		while (! isPowerOfTwo(l_tuples_))
			l_tuples_++;
		
		int [] threads = new int [3];
		
		threads[0] = l_tuples_;
		
		int product = l_tuples * r_tuples;
		System.out.println(String.format("[DBG] product is %10d (~2)", product));
		while (! isPowerOfTwo(product))
			product++;
		System.out.println(String.format("[DBG] product is %10d (^2)", product));
		
		/* For scan and compact kernels */
		threads[1] = product / tuplesPerThread;
		threads[2] = product / tuplesPerThread;
		
		System.out.println(String.format("[DBG] %10d threads[0]", threads[0]));
		System.out.println(String.format("[DBG] %10d threads[1]", threads[1]));
		System.out.println(String.format("[DBG] %10d threads[2]", threads[2]));
		
		int ngroups = threads[1] / threadsPerGroup[1];
		System.out.println(String.format("[DBG] %10d groups", ngroups));
		
		int outputTupleSize = l_inputTupleSize + r_inputTupleSize;
		if (! isPowerOfTwo(outputTupleSize))
			outputTupleSize++; 
		
		int outputSize = outputTupleSize * product;
		
		/* Intermediate state */
		
		byte [] flags      = new byte [4 * product];
		byte [] offsets    = new byte [4 * product];
		byte [] partitions = new byte [4 * ngroups];
		
		String filename = "ThetaJoin.cl";
		String source = load(filename);
		
		int qid = TheGPU.getInstance().getQuery(source, 3, 4, 4);
		
		TheGPU.getInstance().setInput(qid, 0, l_inputSize);
		TheGPU.getInstance().setInput(qid, 1, r_inputSize);
		/* Start and end pointers */
		TheGPU.getInstance().setInput(qid, 2, startPtrs.length);
		TheGPU.getInstance().setInput(qid, 3,   endPtrs.length);
		
		System.out.println(String.format("[DBG] %10d bytes",      flags.length));
		System.out.println(String.format("[DBG] %10d bytes",    offsets.length));
		System.out.println(String.format("[DBG] %10d bytes", partitions.length));
		System.out.println(String.format("[DBG] %10d bytes",        outputSize));
		
		TheGPU.getInstance().setOutput(qid, 0,      flags.length, 0, 0, 1, 0);
		TheGPU.getInstance().setOutput(qid, 1,    offsets.length, 0, 0, 0, 0);
		TheGPU.getInstance().setOutput(qid, 2, partitions.length, 0, 0, 0, 0);
		TheGPU.getInstance().setOutput(qid, 3,        outputSize, 1, 0, 0, 1);
		
		int localInputSize = 4 * threadsPerGroup[1] * tuplesPerThread;
		
		int [] args = new int [3];
		args[0] = l_tuples;
		args[1] = r_tuples; 
		args[2] = localInputSize;
		
		TheGPU.getInstance().setKernelThetaJoin(qid, args);
		
		/* Prepare data */
		byte [] l_input = new byte [l_inputSize];
		byte [] r_input = new byte [r_inputSize];
		
		ByteBuffer l_inputBuffer = ByteBuffer.wrap(l_input); 
		ByteBuffer r_inputBuffer = ByteBuffer.wrap(r_input); 
		/* The default order is BIG_ENDIAN */
		
		/* Populate left stream */
		for (int i = 0; i < l_tuples; i++) {
			/*
			 * Assume input schema is <long, float, int, int, int, long>
			 */
			l_inputBuffer.putLong(System.nanoTime());
			l_inputBuffer.putFloat(1);
			l_inputBuffer.putInt(1);
			l_inputBuffer.putInt(1);
			l_inputBuffer.putInt(1);
			l_inputBuffer.putLong(1);
		}
		l_inputBuffer.clear();
		System.out.println("Left stream");
		for (int i = 0; i < 20; i++) {
			System.out.println(String.format("%02d: <%20d,%3.1f,%2d,%2d,%2d,%2d>", 
			i,
			l_inputBuffer.getLong(),
			l_inputBuffer.getFloat(),
			l_inputBuffer.getInt(),
			l_inputBuffer.getInt(),
			l_inputBuffer.getInt(),
			l_inputBuffer.getLong()
			));
		}
		
		/* Populate right stream */
		for (int i = 0; i < r_tuples; i++) {
			/*
			 * Assume input schema is <long, float, int, int, int, long>
			 */
			r_inputBuffer.putLong(System.nanoTime());
			r_inputBuffer.putFloat(1);
			r_inputBuffer.putInt(1);
			r_inputBuffer.putInt(1);
			r_inputBuffer.putInt(1);
			r_inputBuffer.putLong(1);
		}
		r_inputBuffer.clear();
		System.out.println("Right stream");
		for (int i = 0; i < 20; i++) {
			System.out.println(String.format("%02d: <%20d,%3.1f,%2d,%2d,%2d,%2d>", 
			i,
			r_inputBuffer.getLong(),
			r_inputBuffer.getFloat(),
			r_inputBuffer.getInt(),
			r_inputBuffer.getInt(),
			r_inputBuffer.getInt(),
			r_inputBuffer.getLong()
			));
		}
		
		byte [] output = new byte [outputSize];
		Arrays.fill(output, (byte) 0);
		
		TheGPU.getInstance().setInputBuffer(qid, 0,   l_input);
		TheGPU.getInstance().setInputBuffer(qid, 1,   r_input);
		TheGPU.getInstance().setInputBuffer(qid, 2, startPtrs);
		TheGPU.getInstance().setInputBuffer(qid, 3,   endPtrs);
		
		TheGPU.getInstance().setOutputBuffer(qid, 0,      flags);
		TheGPU.getInstance().setOutputBuffer(qid, 1,    offsets);
		TheGPU.getInstance().setOutputBuffer(qid, 2, partitions);
		TheGPU.getInstance().setOutputBuffer(qid, 3,     output);
		
		TheGPU.getInstance().execute(qid, threads, threadsPerGroup);
		
		/* Print output */
		ByteBuffer outputBuffer = ByteBuffer.wrap(output).order(ByteOrder.BIG_ENDIAN);
		for (int i = 0; i < 20; i++) {
			System.out.println(String.format("%02d: <%20d,%2.1f,%2d,%2d,%2d,%2d,%2.1f,%2d,%2d,%2d,%2d,%2d>", 
			i,
			outputBuffer.getLong(),
			outputBuffer.getFloat(),
			outputBuffer.getInt(),
			outputBuffer.getInt(),
			outputBuffer.getInt(),
			outputBuffer.getLong(),
			outputBuffer.getFloat(),
			outputBuffer.getInt(),
			outputBuffer.getInt(),
			outputBuffer.getInt(),
			outputBuffer.getLong(),
			outputBuffer.getLong() /* Padding */
			));
		}
		
		TheGPU.getInstance().free();
	}
}

