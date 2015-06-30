package uk.ac.imperial.lsds.seep.multi;

public class HashCoding {
	
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
	
	public static int jenkinsHash (byte [] array, int offset, int length, int level) {
		
		/* Set up the internal state */
		int a, b, c;
		a = b = c = (0xdeadbeef + (length << 2) + level);
		
		/* Handle most of the key */
		int i = 0;
		while (length >= 12) {
			a += gatherInt(array, offset + i + 0);
			b += gatherInt(array, offset + i + 4);
			c += gatherInt(array, offset + i + 8);
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
		c += length;
		if (length > 0) {
			if (length >= 4) {
				a += gatherInt(array, offset + i);
				if (length >= 8) {
					b += gatherInt(array, offset + i + 4);
					if (length > 8) {
						c += (gatherPartialInt(array, offset + i + 8, length - 8) << 8);
					}
				} else if (length > 4) {
					b += gatherPartialInt(array, offset + i + 4, length - 4);
				}
			} else {
				a += gatherPartialInt(array, offset + i, length);
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
}
