import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

import java.util.Arrays;
import java.util.Random;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class TestAggregation {
	
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
		int tuple_,
		int window_,
		int slide_,
		int batchSize,
		int batchStartPointer, 
		int batchEndPointer
	) {
		
		ByteBuffer b = ByteBuffer.wrap(startPtrs).order(ByteOrder.LITTLE_ENDIAN);
		ByteBuffer d = ByteBuffer.wrap(  endPtrs).order(ByteOrder.LITTLE_ENDIAN);
		
		if (batchStartPointer < 0 && batchEndPointer < 0)
			return ;
		
		/* Bytes/window */
		int bpw = tuple_ * window_;
			
		int offset  = tuple_; /* In bytes */
		if (window_ == slide_)
			offset *= window_;
		else
			offset *= slide_;
		
		b.putInt(batchStartPointer - batchStartPointer);
		d.putInt(batchStartPointer + bpw - batchStartPointer);
			
		for (int i = 1; i < batchSize; i++) {
			b.putInt(b.getInt((i-1) * 4) + offset);
			d.putInt(d.getInt((i-1) * 4) + offset);
		}
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
	
	private static final int _hash_functions = 5;
	
	private static final float _scale_factor = 1.25F;
	
	private static final float _min_space_requirements [] = {
		Float.MAX_VALUE,
		Float.MAX_VALUE,
		2.01F,
		1.10F,
		1.03F,
		1.02F
	};
	
	/* Default stash table size (# tuples) */
	private static int _stash = 100;
	
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
	
	private static int computeIterations (int n) {
		int result = 7;
		float logn = (float) (Math.log(n) / Math.log(2.0));
		return (int) (result * logn);
	}
	
	private static void constants (int [] x, int [] y, int [] stash) {
		Random r = new Random();
		int prime = 2147483647;
		assert (x.length == y.length);
		int i, n = x.length;
		int t;
		for (i = 0; i < n; i++) {
			t = r.nextInt(prime);
			x[i] = (1 > t ? 1 : t);
			y[i] = r.nextInt(prime) % prime;
		}
		/* Stash hash constants */
		stash[0] = Math.max(1, r.nextInt(prime)) % prime;
		stash[1] = r.nextInt(prime) % prime;
	}
	
	public static void main (String [] mainArgs) {
		
		System.out.println("[Test]");
		
		TheGPU.getInstance().init(1);
		
		/* Configuration variables */
		int inputSize = _default_size;
		
		int inputTupleSize = 32;
		int tuples = inputSize / inputTupleSize;
		/* Window definition */
		int nwindows = 32;
		int range = 1024;
		int slide = 1024;
		/* Compute start and end pointers */
		byte [] startPtrs = new byte [nwindows * 4];
		byte []   endPtrs = new byte [nwindows * 4];
		initWindowPointers(startPtrs, endPtrs, 
			inputTupleSize, range, slide, nwindows, 0, inputSize);
		/* Debugging */
		printWindowPointers (startPtrs, endPtrs);
		
		/* Configure hash table constants */
		int [] _x = new int [_hash_functions];
		int [] _y = new int [_hash_functions];
		ByteBuffer x = ByteBuffer.allocate(4 * _hash_functions).order
			(ByteOrder.LITTLE_ENDIAN);
		ByteBuffer y = ByteBuffer.allocate(4 * _hash_functions).order
			(ByteOrder.LITTLE_ENDIAN);
		int [] stash = new int[2];
		constants (_x, _y, stash);
		int __stash_x = stash[0];
		int __stash_y = stash[1];
		for (int i = 0; i < _hash_functions; i++) {
			x.putInt(_x[i]);
			y.putInt(_y[i]);
		}
		int iterations = computeIterations (range);
		/* Determine an upper bound on # slots/table,
		 * such that we avoid collisions */
		System.out.println(String.format("[DBG] %d iterations\n", iterations));
		float alpha = 1.25f;
		if (alpha < _min_space_requirements[_hash_functions])
		{
			throw new IllegalArgumentException("error: invalid scale factor");
		}
		int tableSize  = (int) Math.ceil(range * alpha);
		int tableSlots = tableSize + _stash;
		System.out.println(String.format("[DBG] # slots (~2) is %4d", tableSlots));
		while (! isPowerOfTwo(tableSlots)) {
			tableSlots += 1;
		}
		System.out.println(String.format("[DBG] # slots (^2) is %4d", tableSlots));
		tableSize = tableSlots - _stash;
		
		/* Determine #threads */
		int [] threadsPerGroup = new int [4];
		threadsPerGroup[0] = 256; /* This is a constant; it must be a power of 2 */
		threadsPerGroup[1] = 256;
		threadsPerGroup[2] = 256;
		threadsPerGroup[3] = 256;
		
		int tuplesPerThread = 2;
		
		int [] threads = new int [4];
		threads[0] = (nwindows * tableSlots); /* Clear `indices` and `offsets` */
		threads[1] = nwindows * threadsPerGroup[0];
		/* Configure scan & compact kernels */
		threads[2] = (nwindows * tableSlots) / tuplesPerThread;
		threads[3] = (nwindows * tableSlots) / tuplesPerThread;
		int groups = (nwindows * tableSlots) / tuplesPerThread / threadsPerGroup[0];
		
		int outputTupleSize = 16;
		/* The output is simply a function of the number of windows 
 		 * 
 		 * Assume output tuple schema is <long, int key, float value> (16 bytes) 
 		 */
		int outputSize = tuples * outputTupleSize;
		/* Intermediate state */
		
		byte [] contents   = new byte [outputTupleSize * tableSlots * nwindows];
		byte [] stashed    = new byte [4 * nwindows];
		byte [] failed     = new byte [4 * nwindows];
		byte [] attempts   = new byte [4 *   tuples];
		byte [] indices    = new byte [4 * tableSlots * nwindows];
		byte [] offsets    = new byte [4 * tableSlots * nwindows];
		byte [] partitions = new byte [4 *   groups];
		
		String filename = "Aggregation.cl";
		String source = load(filename);
		
		int qid = TheGPU.getInstance().getQuery(source, 4, 5, 8);
		
		TheGPU.getInstance().setInput(qid, 0, inputSize);
		/* Start and end pointers */
		TheGPU.getInstance().setInput(qid, 1, startPtrs.length);
		TheGPU.getInstance().setInput(qid, 2,   endPtrs.length);
		/* Hash function constants, x & y */
		TheGPU.getInstance().setInput(qid, 3, x.array().length);
		TheGPU.getInstance().setInput(qid, 4, y.array().length);
		
		TheGPU.getInstance().setOutput(qid, 0,   contents.length, 0, 0, 0, 0);
		TheGPU.getInstance().setOutput(qid, 1,    stashed.length, 0, 0, 0, 0);
		TheGPU.getInstance().setOutput(qid, 2,     failed.length, 0, 0, 0, 0);
		TheGPU.getInstance().setOutput(qid, 3,   attempts.length, 0, 0, 0, 0);
		TheGPU.getInstance().setOutput(qid, 4,    indices.length, 0, 0, 1, 0);
		TheGPU.getInstance().setOutput(qid, 5,    offsets.length, 0, 0, 0, 0);
		TheGPU.getInstance().setOutput(qid, 6, partitions.length, 0, 0, 0, 0);
		TheGPU.getInstance().setOutput(qid, 7,        outputSize, 1, 0, 0, 1);
		
		int localInputSize = 4 * threadsPerGroup[0] * tuplesPerThread; 
		
		int [] args = new int [8];
		args[0] = tuples;
		args[1] = 0; /* bundle_; */
		args[2] = 0; /* bundles; */
		args[3] = tableSize;
		args[4] = __stash_x;
		args[5] = __stash_y;
		args[6] = iterations;
		args[7] = localInputSize;
		TheGPU.getInstance().setKernelAggregate(qid, args);
		
		/* Prepare data */
		byte [] input = new byte [inputSize];
		ByteBuffer inputBuffer = ByteBuffer.wrap(input); 
		/* The default order is BIG_ENDIAN */
		
		for (int i = 0; i < tuples; i++) {
			/*
			 * Assume input schema is <long, float, int, int, int, long>
			 */
			inputBuffer.putLong(System.nanoTime());
			inputBuffer.putFloat(1);
			inputBuffer.putInt((i % 128) + 1);
			inputBuffer.putInt(1);
			inputBuffer.putInt(1);
			inputBuffer.putLong(1);
		}
		inputBuffer.clear();
		for (int i = 0; i < 20; i++) {
			System.out.println(String.format("%02d: <%20d,%3.1f,%2d,%2d,%2d,%2d>", 
			i,
			inputBuffer.getLong(),
			inputBuffer.getFloat(),
			inputBuffer.getInt(),
			inputBuffer.getInt(),
			inputBuffer.getInt(),
			inputBuffer.getLong()
			));
		}
		byte [] output = new byte [outputSize];
		Arrays.fill(output, (byte) 0);
		TheGPU.getInstance().setInputBuffer(qid, 0, input);
		
		TheGPU.getInstance().setInputBuffer(qid, 1, startPtrs);
		TheGPU.getInstance().setInputBuffer(qid, 2,   endPtrs);
		TheGPU.getInstance().setInputBuffer(qid, 3, x.array());
		TheGPU.getInstance().setInputBuffer(qid, 4, y.array());
		
		TheGPU.getInstance().setOutputBuffer(qid, 0,   contents);
		TheGPU.getInstance().setOutputBuffer(qid, 1,    stashed);
		TheGPU.getInstance().setOutputBuffer(qid, 2,     failed);
		TheGPU.getInstance().setOutputBuffer(qid, 3,   attempts);
		TheGPU.getInstance().setOutputBuffer(qid, 4,    indices);
		TheGPU.getInstance().setOutputBuffer(qid, 5,    offsets);
		TheGPU.getInstance().setOutputBuffer(qid, 6, partitions);
		TheGPU.getInstance().setOutputBuffer(qid, 7,     output);
		
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
		/* For debugging purposes */
		long totalStashed = 0L;
		long totalFailed  = 0L;
		ByteBuffer stashedBuffer = ByteBuffer.wrap(stashed).order(ByteOrder.LITTLE_ENDIAN);
		ByteBuffer failedBuffer  = ByteBuffer.wrap( failed).order(ByteOrder.LITTLE_ENDIAN);
		for (int i = 0; i < nwindows; i++) {
			int _s = stashedBuffer.getInt();
			int _f =  failedBuffer.getInt();
			System.out.println(String.format("%02d: stashed %3d failed %3d",
				i, _s, _f));
			totalStashed += _s;
			totalFailed  += _f;
		}
		System.out.println(String.format("%3d stashed %3d failed", 
			totalStashed, totalFailed));
		
		/*
		ByteBuffer attemptsBuffer = 
			ByteBuffer.wrap(attempts).order(ByteOrder.LITTLE_ENDIAN);
		for (int i = 0; i < range; i++) {
			System.out.println(String.format("%06d: %3d attempts",
			i,
			attemptsBuffer.getInt()
			));
		}
		*/
		
		if (totalFailed > 0) {
			System.err.println("Fatal error.");
			System.exit(1);
		}
		
		/* Print output */
		ByteBuffer outputBuffer = ByteBuffer.wrap(output).order(ByteOrder.BIG_ENDIAN);
		for (int i = 0; i < 65; i++) {
			System.out.println(String.format("%02d: <%20d,%2d,%5.1f>", 
			i,
			outputBuffer.getLong(),
			outputBuffer.getInt(),
			outputBuffer.getFloat()
			));
		}
		
		ByteBuffer indicesBuffer = 
			ByteBuffer.wrap(indices).order(ByteOrder.LITTLE_ENDIAN);
		int cnt;
		for (int i = 0; i < nwindows; i++) {
			cnt = 0;
			for (int j = 0; j < tableSlots; j++) {
				int _idx = indicesBuffer.getInt();
				if (_idx > 0)
					cnt += 1;
			}
			System.out.println(String.format("window %02d: %3d items", i, cnt));
		}
		
		/*
		indicesBuffer.clear();
		ByteBuffer offsetsBuffer = ByteBuffer.wrap(offsets).order(ByteOrder.LITTLE_ENDIAN);
		for (int i = 0; i < tableSlots; i++) {
			System.out.println(String.format("%06d: index %6d offset %7d",
			i,
			indicesBuffer.getInt(),
			offsetsBuffer.getInt()
			));
		}
		*/
		
		/*
		ByteBuffer partitionsBuffer =
			ByteBuffer.wrap(partitions).order(ByteOrder.LITTLE_ENDIAN);
		for (int i = 0; i < groups; i++) {
			System.out.println(String.format("%03d: group offset %7d",
			i,
			partitionsBuffer.getInt()
			));
		}
		*/
		
		TheGPU.getInstance().free();
	}
}

