package uk.ac.imperial.lsds.seep.multi;

import java.nio.ByteBuffer;

public interface IQueryBuffer {
	
	public int getInt (int offset);
	public float getFloat (int offset);
	public long getLong (int offset);
	public byte [] array ();
	public byte [] array (int offset, int length);
	public void appendBytesTo (int start, int end, byte [] destination);
	public void appendBytesTo (int offset, int length, IQueryBuffer destination);
	public ByteBuffer getByteBuffer ();
	public int capacity ();
	public int remaining ();
	public boolean hasRemaining ();
	public void position (int index);
	public int position ();
	public int limit ();
	public void close ();
	public void clear ();	
	public int putInt (int value);
	public int putInt(int index, int value);
	public int putFloat (float value);
	public int putFloat(int index, float value);
	public int putLong (long value);
	public int putLong(int index, long value);
	public int put (byte [] values);
	public int put (byte [] source, int offset, int length);
	public int put (IQueryBuffer values);
	public int put (IQueryBuffer source, int offset, int length);
	public void resize ();
	public void resize (int size);
	public void free (int offset);
	
	public void release ();
}
