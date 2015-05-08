package synth;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.ByteBuffer;

import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.TupleSchema;
import uk.ac.imperial.lsds.seep.multi.Utils;
import uk.ac.imperial.lsds.seep.multi.TheGPU;
import uk.ac.imperial.lsds.streamsql.op.gpu.KernelCodeGenerator;

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
		System.out.println("[DBG] buffer capacity is " + buffer.capacity());
		
		int tupleSize = 128;
		/*
		 * Construct schema
		 */
		int nattributes = (tupleSize - 8) / 4;
		int[] offsets = new int[nattributes + 1];
		offsets[0] = 0;
		int byteSize = 8;
		for (int i = 1; i < nattributes + 1; i++) {
			offsets[i] = byteSize;
			byteSize += 4;
		}
		ITupleSchema inputSchema = new TupleSchema (offsets, byteSize);
		/* 0:undefined 1:int, 2:float, 3:long */
		inputSchema.setType(0, 3);
		for (int i = 1; i < nattributes + 1; i++) {
			inputSchema.setType(i, 1);
		}
		
		/* Populate the buffer with dummy tuples */
		while(buffer.hasRemaining()) {
			/*
			 * Assume input schema is <long, int, int, ...>
			 */
			buffer.putLong(System.nanoTime());
			for (int j = 8; j < tupleSize; j += 4)
				buffer.putInt(1);
		}
		buffer.clear();
		
		int [] startPointers = new int [1];
		int []   endPointers = new int [1];
		
		/* Configuration variables for the query */
		int inputSize  = _default_size;
		int outputSize = _default_size;
		
		int tuples = _default_size / tupleSize;
		
		int [] threads = new int [1];
		threads[0] = tuples;
		
		int [] threadsPerGroup = new int [1];
		threadsPerGroup[0] = 128;
		
		String filename = Utils.SEEP_HOME + "/seep-system/clib/templates/DummyKernel.cl";
		String source = KernelCodeGenerator.getDummyOperator(inputSchema, inputSchema, filename);
		
		int qid = TheGPU.getInstance().getQuery(source, 1, 1, 1);
		
		int outputBufferId = TheGPU.getInstance().allocateBuffer (outputSize, 0); /* writeOnly buffer */
		System.out.println("[DBG] output buffer id is " + outputBufferId);
		ByteBuffer outputBuffer = (ByteBuffer) TheGPU.getInstance().getDirectByteBuffer (outputBufferId);
		
		TheGPU.getInstance().setDirectInput  (qid, 0, inputSize, bid);
		TheGPU.getInstance().setDirectOutput (qid, 0, outputSize, 1, 0, 0, 1, outputBufferId);
		
		TheGPU.getInstance().setKernelDummy(qid, null);
		
		int start = 0;
		int end = _default_size;
		while (true) {
			
			startPointers[0] = start;
			endPointers[0] = end;
			
			TheGPU.getInstance().executeDirect(qid, threads, threadsPerGroup, startPointers, endPointers);
			/* At this point, outputBuffer should contain a copy of a slice of the input buffer, 
			 * as determined by the start and end pointers. */
			ByteBuffer slicedInput = buffer.slice();
			slicedInput.position(startPointers[0]);
			slicedInput.limit(endPointers[0]);
			/* Reset outputBuffer */
			outputBuffer.clear();
			if (! outputBuffer.equals(slicedInput)) {
				System.err.println("Error");
				System.exit(1);
			}
			/*
			 * Slide the input window batch and test whether the DMA
			 * throughput is sustained
			 */
			start += tupleSize;
			end = start + _default_size;
			if (end >= buffer.capacity())
				break;
		}
		
		TheGPU.getInstance().free();
		System.out.println("Bye.");
	}
}

