import java.nio.ByteBuffer;
import java.util.Random;

import uk.ac.imperial.lsds.seep.multi.Utils;

public class TestLRBQ4 {

	public static void main(String[] args) {

		Random r = new Random();

		/*
		 * INPUT STREAM timestamp, vehicleID, speed, highway, direction,
		 * dummyInt, dummyInt
		 */
		LRBQ4 q = new LRBQ4();
		q.setup();

		byte[] data = new byte[Utils.BUNDLE];
		ByteBuffer b = ByteBuffer.wrap(data);
		long timestamp = -1L;
		long tuples = 0L;
		/*
		 * Utils.BUNDLE holds 32,768 tuples; or, given 32KB/s, 32 seconds of
		 * data
		 */
		long tps = 1L;
		while (b.hasRemaining()) {
			if (tuples++ % tps == 0)
				timestamp += 1;
			b.putLong(timestamp);
			/*
			 * Fill in tuple
			 */
			b.putInt(r.nextInt(20000));
			b.putFloat(r.nextFloat() * 60f);
			b.putInt(r.nextInt(10));
			b.putInt(r.nextInt(1));
			b.putInt(-1);
			b.putInt(-1);
		}
		timestamp++;
		b.flip();
		try {
			while (true) {
				q.processData(data);
				/* Increment timestamps */
				/*
				 * for (int i = 0; i < Utils.BUNDLE; i += Utils._TUPLE_)
				 * b.putLong(i, b.getLong(i) + timestamp); b.clear();
				 */
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

	}
}
