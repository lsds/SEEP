package uk.ac.imperial.lsds.seep.multi;

import java.nio.ByteBuffer;

public interface IQueryBuffer {
	
	public int getInt (int offset);
	public float getFloat (int offset);
	public long getLong (int offset);
//	public byte [] array ();
//	public byte [] array (int offset, int length);
	public void appendBytesTo(int offset, int length, IQueryBuffer toBuffer);
//	public ByteBuffer getByteBuffer ();
	public int capacity ();
	public int remaining ();
	public int position ();
	public boolean hasRemaining ();
	public int limit ();
	public void close ();
	public void clear ();	
	public int putInt (int value);
	public int putFloat (float value);
	public int putLong (long value);
	public int put (byte [] values);
	public int put (byte [] source, int offset, int length);
	public int put (IQueryBuffer source, int offset, int length);
	public int put (IQueryBuffer values);
	public void resize ();
	public void resize (int size);
	public void free (int offset);
	
	public void release ();
	public void putFloat(int index, float value);
	public void putInt(int index, int value);
	public void putLong(int index, long value);
}
