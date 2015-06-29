package uk.ac.imperial.lsds.seep.multi;

// import java.util.Random;

public class HashCoding {
	
//	/*
//	 * Too many troubles:
//	 * 
//	 * A. The CPU and GPU version of the group-by operator
//	 * s
//	 * For each tuple t:
//	 * 		(h1, h2) = hash(key)
//	 * 		for i = 0 to MAX ATTEMPTS
//	 * 			h1 %= size
//	 * 			a = slot[h1]
//	 * 			if a == null
//	 * 				slot[h1] = t
//	 * 				return
//	 * 			else if a == t
//	 * 				update slot[h1] value
//	 * 				return
//	 * 			end if
//	 * 			h1 += i * h2;
//	 * 		end fors
//	 * 		return false
//	 * end for
//	 * 
//	 * - If there are complete windows, compact complete windows.
//	 * 
//	 * What else... Do a look up and merge - that is, pick a hash table, iterate over its elements
//	 * and look them up in the other table.
//	 * 
//	 * Note that the logic behind opening, closing, pending and complete is the same. That is,
//	 * 		#opening = #closing + #pending; 
//	 * 
//	 * and, windows are merged in order.
//	 * 
//	 * What's next?
//	 * 
//	 * - Check if there are collisions/failures when we build a hash table.
//	 * - Multiple values? OK
//	 * 
//	 * 0---------------------0
//	 * 		1---------------------1
//	 * 			2--------------------------2
//	 * 				3-------------------------3
//	 * 					4------------------------4
//	 * 						5---------------------------5
//	 * 
//	 * --------------------              0
//	 *  --------------------             1
//	 *    -------------------            2
//	 *      ------------------           3
//	 *        --------------------       4
//	 *          -------------------      5
//	 *            ---------------------- 6
//	 *             --------------------- 7 
//	 *              -------------------- 8
//	 *               ------------------- 9
//	 *                ------------------10
//	 *                 -----------------11
//	 *                  ----------------12
//	 *                   ---------------13
//	 *                    --------------14
//	 *                     -------------15
//	 *                      ------------16
//	 *                       -----------17
//	 *                        ----------18
//	 *                         ---------19
//	 *                         ---------20
//	 *                          --------21
//	 *                           -------22
//	 *                            ------23
//	 *                             -----24
//	 *                              ----25
//	 *                               ---26
//	 *                                --27
//	 *                                 -28
//	 * 
//	 * How to do incremental computation on the GPU?
//	 * 
//	 * Compute window 0
//	 * Compute window 1: 
//	 * 		process from 0's end until 1's end (tuples that enter)
//	 * 		process from 0's start until 1's start (tuples that exit)
//	 * Compute window 2:
//	 * 		process from 1's end until 2's end
//	 * 		process from 1's start until 2's start
//	 * 
//	 * and so on. But, can this be done in parallel?
//	 * 
//	 * process from 0's start until 1's start
//	 * process from 1's start until 2's start
//	 * process from 2's start until 3's start
//	 * ...
//	 * 
//	 * All this can be performed in parallel. At this end,
//	 * we have processed the entire batch, apart from the
//	 * entire window 5.
//	 * 
//	 * 
//	 */
//	
//	public static final int _HASH_FUNCTIONS_;
//	/*
//	public static int __s_major_hash (int x, int y, int k) {
//	    const unsigned int prime = 2147483647U;
//	    unsigned int xl = convert_uint(x);
//	    unsigned int yl = convert_uint(y);
//	    unsigned int kl = convert_uint(k);
//	    unsigned int result = ((xl ^ kl) + yl) % prime;
//	    return convert_int(result);
//	}
//	
//	inline int __s_minor_hash (int x, int y, int k) {
//	    const unsigned int stash_size = 101U;
//	    unsigned int xl = convert_uint(x);
//	    unsigned int yl = convert_uint(y);
//	    unsigned int kl = convert_uint(k);
//	    unsigned int result = ((xl ^ kl) + yl) % stash_size;
//	    return convert_int(result);
//	}
//	*/
//	
	/**
	 *  Gather an int from the specified index into the byte array (little endian)
	 */
	public static final int gatherInt (byte [] data, int index) {
		int i = data [index] & 0xFF;
		i |= (data[++index] & 0xFF) << 8;
		i |= (data[++index] & 0xFF) << 16;
		i |= (data[++index] << 24);
		return i;
	}
	
	/**
	*  Gather a partial int from the specified index using the specified number of bytes into the byte array (little endian)
	*/
	public static final int gatherPartialInt (byte [] data, int index, int available) {
		int i = data[index] & 0xFF;
		if (available > 1) {
			i |= (data[++index] & 0xFF) << 8;
			if (available > 2) {
				i |= (data[++index] & 0xFF) << 16;
			}
		}
		return i;
	}
	
	public static final int rotateInt(int val, int bits) {
		return (val >> bits) | (val << (32 - bits));
	}
	
	public static int jenkinsHash (byte [] key, int level) {
		
		/* Set up the internal state */
		int a, b, c;
		a = b = c = (0xdeadbeef + (key.length << 2) + level);
		
		int length = key.length;
		
		/* Handle most of the key */
		int i = 0;
		while (length >= 12) {
			a += gatherInt(key, i + 0);
			b += gatherInt(key, i + 4);
			c += gatherInt(key, i + 8);
			/* mix(a, b, c); */
			a -= c; a ^= rotateInt(c, 4); c += b;
			b -= a; b ^= rotateInt(a, 6); a += c;
			c -= b; c ^= rotateInt(b, 8); b += a;
			a -= c; a ^= rotateInt(c,16); c += b;
			b -= a; b ^= rotateInt(a,19); a += c;
			c -= b; c ^= rotateInt(b, 4); b += a;
			/* mix(a, b, c); */
			i += 12;
			length -= 12;
		}
		
		/* Handle the last 23 bytes */
		c += key.length;
		if (length > 0) {
			if (length >= 4) {
				a += gatherInt(key, i);
				if (length >= 8) {
					b += gatherInt(key, i + 4);
					if (length > 8) {
						c += (gatherPartialInt(key, i + 8, length - 8) << 8);
					}
				} else if (length > 4) {
					b += gatherPartialInt(key, i + 4, length - 4);
				}
			} else {
				a += gatherPartialInt(key, i, length);
			}
		}
		/* final(a, b, c); */
		c ^= b; c -= rotateInt(b,14);
		a ^= c; a -= rotateInt(c,11);
		b ^= a; b -= rotateInt(a,25);
		c ^= b; c -= rotateInt(b,16);
		a ^= c; a -= rotateInt(c, 4);
		b ^= a; b -= rotateInt(a,14);
		c ^= b; c -= rotateInt(b,24);
		/* final(a, b, c); */
		
		return c;
	}
//	
//	public static int __s_major_hash (int x, int y, byte [] key) {
//		
//		int k = jenkinsHash (key, 1);
//		
//        long xl = x & 0xffffffffL;
//        long yl = y & 0xffffffffL;
//        long kl = k & 0xffffffffL;
//        long prime  = 2147483647L;
//        long result = ((xl ^ kl) + yl) % prime;
//        return (int) result;
//    }
//	
//    public static void constants (int [] x, int [] y) {
//        Random r = new Random();
//        int prime = 2147483647;
//        assert (x.length == y.length);
//        int i, n = x.length;
//        int t;
//        for (i = 0; i < n; i++) {
//            t = r.nextInt(prime);
//            x[i] = (1 > t ? 1 : t);
//            y[i] = r.nextInt(prime) % prime;
//        }
//    }
//    
//	inline int getNextLocation(int size,
//	                           int  key,
//	                           int prev,
//	           __global const int*    x,
//	           __global const int*    y){
//
//		/* Identify possible locations for key */
//		int locations[_HASH_FUNCTIONS_];
//		int i;
//		#pragma unroll
//		for (i = 0; i < _HASH_FUNCTIONS_; i++) {
//			locations[i] = __s_major_hash (x[i], y[i], key) % size;
//		}
//
//		int next = locations[0];
//		#pragma unroll
//		for (i = _HASH_FUNCTIONS_ - 2; i >= 0; --i) {
//			next = (prev == locations[i] ? locations[i+1] : next);
//		}
//		return next;
//	}
}
