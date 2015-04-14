import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

import java.util.Arrays;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Test {
	
	public static final int _default_size = 1048576;
	
	public static String load (String filename) {
		
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
	
	public static void main (String [] mainArgs) {
		
		System.out.println("[Test]");
		
		TheGPU.getInstance().init(1);
		
		/* Configuration variables */
		int inputSize = _default_size;
		int outputSize = _default_size;
		int tupleSize = 32;
		int tuples = _default_size / tupleSize;
		int [] threads = new int [1];
		threads[0] = tuples;
		int [] threadsPerGroup = new int [1];
		threadsPerGroup[0] = 128;
		String filename = "Projection.cl";
		String source = load(filename);
		int qid = TheGPU.getInstance().getQuery(source, 1, 1, 1);
		TheGPU.getInstance().setInput(qid, 0, inputSize);
		TheGPU.getInstance().setOutput(qid, 0, outputSize, 1, 0, 0, 1);
		int localInputSize = threadsPerGroup[0] * tupleSize;
		int localOutputSize = localInputSize;
		int [] args = new int [4];
		args[0] = tuples;
		args[1] = inputSize;
		args[2] = localInputSize;
		args[3] = localOutputSize;
		TheGPU.getInstance().setKernelProject(qid, args);
		
		byte [] input = new byte [inputSize];
		ByteBuffer inputBuffer = ByteBuffer.wrap(input); /* The default order is BIG_ENDIAN */
		for (int i = 0; i < tuples; i++) {
			/*
			 * Assume input schema is <long, int, int, float, int, long>
			 */
			inputBuffer.putLong(System.nanoTime());
			inputBuffer.putInt(1);
			inputBuffer.putInt(1);
			inputBuffer.putFloat(1);
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
		
		TheGPU.getInstance().free();
	}
}

