package uk.ac.imperial.lsds.seep.multi;

import java.util.concurrent.ConcurrentLinkedQueue;

import java.util.concurrent.atomic.AtomicLong;

public class KeyFactory {
	
	private static final int N = 2 * Runtime.getRuntime().availableProcessors() + 1;
	
	private static final int M = KeyType.numTypes();
	
	private static int _pool_size = 1;
	
	public static AtomicLong [] count = new AtomicLong [M];
	
	@SuppressWarnings("unchecked")
	public static ConcurrentLinkedQueue<Key> [][] pool = 
		(ConcurrentLinkedQueue<Key> [][]) new ConcurrentLinkedQueue [M][N];
	
	static {
		for (int m = 0; m < M; m++) {
			KeyType type = KeyType.type(m);
			for (int n = 0; n < N; n++) {
				pool[m][n] = new ConcurrentLinkedQueue<Key>();
				int i = _pool_size;
				while (i-- > 0) {
					Key key = new Key (type, n);
					pool[m][n].add (key);
				}
			}
			count[m] = new AtomicLong(_pool_size * N);
		}
	}
	
	public static Key newInstance (KeyType type, int pid) {
		int tid = KeyType.index(type); /* Type index */
		Key key = pool[tid][pid].poll();
		if (key == null) {
			key = new Key(type, pid);
			count[tid].incrementAndGet();
		}
		return key;
	}
	
	public static void free (KeyType type, int pid, Key key) {
		int tid = KeyType.index(type);
		pool[tid][pid].offer (key);
	}
}
