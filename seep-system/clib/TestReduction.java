import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

import java.util.Arrays;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class TestReduction {
	
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
	
	public static void main (String [] mainArgs) {
		
		System.out.println("[Test]");
		
		TheGPU.getInstance().init(1);
		
		/* Configuration variables */
		int inputSize = _default_size;
		
		int inputTupleSize = 32;
		int tuples = _default_size / inputTupleSize;
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
		
		int outputTupleSize = 16;
		/* The output is simply a function of the number of windows 
 		 * 
 		 * Assume output tuple schema is <long, float, int> (16 bytes) 
 		 */
		int outputSize = nwindows * outputTupleSize;
		
		int [] threadsPerGroup = new int [1];
		threadsPerGroup[0] = 128; /* This is a constant */
		int [] threads = new int [1];
		threads[0] = nwindows * threadsPerGroup[0];
		
		String filename = "Reduction.cl";
		String source = load(filename);
		
		int qid = TheGPU.getInstance().getQuery(source, 1, 3, 1);
		
		TheGPU.getInstance().setInput(qid, 0, inputSize);
		/* Start and end pointers are also inputs */
		TheGPU.getInstance().setInput(qid, 1, startPtrs.length);
		TheGPU.getInstance().setInput(qid, 2,   endPtrs.length);
		
		TheGPU.getInstance().setOutput(qid, 0, outputSize, 1, 0, 0, 1);
		
		int localOutputSize = threadsPerGroup[0] * 4; /* 1 reduced value per group */
		
		int [] args = new int [3];
		args[0] = tuples;
		args[1] = inputSize;
		args[2] = localOutputSize;
		TheGPU.getInstance().setKernelReduce(qid, args);
		
		/* Prepare data */
		byte [] input = new byte [inputSize];
		ByteBuffer inputBuffer = ByteBuffer.wrap(input); 
		/* The default order is BIG_ENDIAN */
		
		for (int i = 0; i < tuples; i++) {
			/*
			 * Assume input schema is <long, int, int, float, int, long>
			 */
			inputBuffer.putLong(System.nanoTime());
			inputBuffer.putInt(1);
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
		
		TheGPU.getInstance().setInputBuffer(qid, 1, startPtrs);
		TheGPU.getInstance().setInputBuffer(qid, 2,   endPtrs);
		
		TheGPU.getInstance().setOutputBuffer(qid, 0, output);
		
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
		for (int i = 0; i < nwindows; i++) {
			System.out.println(String.format("%02d: <%20d,%5.1f,%2d>", 
			i,
			outputBuffer.getLong(),
			outputBuffer.getFloat(),
			outputBuffer.getInt()
			));
		}
		
		TheGPU.getInstance().free();
	}
}

