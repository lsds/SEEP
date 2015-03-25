import java.nio.ByteBuffer;

import uk.ac.imperial.lsds.seep.multi.CircularQueryBuffer;

public class TestCircularBufferBounds {
	
	public static void main (String [] args) {
		CircularQueryBuffer buffer = new CircularQueryBuffer (0, 1024, false);
		byte [] data = new byte [256];
		ByteBuffer b = ByteBuffer.wrap(data);
		while (b.hasRemaining())
			b.putInt(1);
		/*
		buffer.put(data);
		buffer.debug();
		buffer.put(data);
		buffer.debug();
		buffer.put(data);
		buffer.debug();
		buffer.put(data);
		buffer.debug();
		buffer.free(255);
		buffer.debug();
		buffer.put(data);
		buffer.debug();
		*/
		buffer.put(data);
		buffer.debug();
		buffer.free(255);
		buffer.debug();
		buffer.put(data);
		buffer.debug();
		buffer.free(511);
		buffer.debug();
		buffer.put(data);
		buffer.debug();
		buffer.free(767);
		buffer.debug();
		buffer.put(data);
		buffer.debug();
		buffer.free(1023);
		buffer.debug();
		buffer.put(data);
		buffer.debug();
		buffer.free(255);
		buffer.debug();
		buffer.put(data);
		buffer.debug();
		buffer.put(data);
		buffer.debug();
		buffer.put(data);
		buffer.debug();
		buffer.put(data);
		buffer.debug();
		buffer.free(511);
		buffer.debug();
		buffer.free(767);
		buffer.debug();
	}
}
