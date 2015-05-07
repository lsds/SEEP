package synth;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.nio.ByteBuffer;

import uk.ac.imperial.lsds.seep.multi.TheGPU;

public class TestDMA {
	
	public static int _default_size = 1048576;
	
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
		
		int bid = TheGPU.getInstance().allocateBuffer (1073741824, 1); /* readOnly buffer */
		System.out.println("[DBG] buffer id is " + bid);
		ByteBuffer buffer = (ByteBuffer) TheGPU.getInstance().getDirectByteBuffer (bid);
		
		int tupleSize = 512;
		
		/* Populate the buffer with dummy tuples */
		for (int i = 0; i < buffer.capacity(); i++) {
			/*
			 * Assume input schema is <long, int, int, ...>
			 */
			buffer.putLong(System.nanoTime());
			for (int j = 8; j < tupleSize; j += 4)
				buffer.putInt(1);
		}
		buffer.clear();
		
		/* Configuration variables for the query */
		int inputSize  = _default_size;
		int outputSize = _default_size;
		
		int tuples = _default_size / tupleSize;
		
		int [] threads = new int [1];
		threads[0] = tuples;
		
		int [] threadsPerGroup = new int [1];
		threadsPerGroup[0] = 128;
		
		String filename = "templates/DummyKernel.cl";
		String source = load(filename);
		
		int qid = TheGPU.getInstance().getQuery(source, 1, 1, 1);
		
		int outputBufferId = TheGPU.getInstance().allocateBuffer (outputSize, 0); /* writeOnly buffer */
		System.out.println("[DBG] output buffer id is " + outputBufferId);
		ByteBuffer outputBuffer = (ByteBuffer) TheGPU.getInstance().getDirectByteBuffer (outputBufferId);
		
		TheGPU.getInstance().setDirectInput  (qid, 0, inputSize, bid);
		TheGPU.getInstance().setDirectOutput (qid, 0, outputSize, 1, 0, 0, 1, outputBufferId);
		
		TheGPU.getInstance().setKernelDummy(qid, null);
		
		TheGPU.getInstance().execute(qid, threads, threadsPerGroup);
		
		TheGPU.getInstance().free();
	}
}

