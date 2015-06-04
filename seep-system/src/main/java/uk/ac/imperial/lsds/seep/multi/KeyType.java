package uk.ac.imperial.lsds.seep.multi;

public enum KeyType {
	
	_16, _32, _64;
	
	public static int numTypes () {
		
		return 3;
	}
	
	public static int index (KeyType type) {
		switch (type) {
			case _16: return 0;
			case _32: return 1;
			case _64: return 2;
			default:
				throw new IllegalArgumentException
				("Unknown key type");
		}
	}
	
	public static KeyType type (int index) {
		switch (index) {
			case 0: return _16;
			case 1: return _32;
			case 2: return _64;
			default:
				throw new IllegalArgumentException
				("Unknown key type");
		}
	}
	
	public static int size (KeyType type) {
		switch (type) {
			case _16: return 16;
			case _32: return 32;
			case _64: return 64;
			default:
				throw new IllegalArgumentException
				("Unknown key type");
		}
	}
	
	public static int size (int index) {
		switch (index) {
			case 0: return 16;
			case 1: return 32;
			case 2: return 64;
			default:
				throw new IllegalArgumentException
				("Unknown key type");
		}
	}
}
