package uk.ac.imperial.lsds.seep.multi.tmp;

import java.nio.ByteBuffer;

public class Key {

	public ByteBuffer buffer;
	public KeyType type;
	
	public int pid;
	
	public Key (KeyType type, int pid) {
		this.type = type;
		this.buffer = ByteBuffer.allocate(KeyType.size(type));
		
		this.pid = pid;
	}
	
	public void release () {
		
		KeyFactory.free(type, pid, this);
	}

	public int hash () {
		buffer.clear();
		return buffer.hashCode();
	}
	
    public boolean eq (Key key) {
    	
    	return this.buffer.equals(key.buffer);
    }
}
