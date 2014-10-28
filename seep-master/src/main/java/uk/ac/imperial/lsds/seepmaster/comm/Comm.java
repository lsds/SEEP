package uk.ac.imperial.lsds.seepmaster.comm;

import java.util.Set;

import uk.ac.imperial.lsds.seep.comm.Connection;

public interface Comm {

	// Raw data commands
	public boolean send_sync(byte[] data, Connection c);
	public void send_async(byte[] data, Connection c);
	public void send_sync(byte[] data, Set<Connection> cs);
	public void send_async(byte[] data, Set<Connection> cs);
	public boolean send_sync_parallel(byte[] data, Set<Connection> cs);
	public void send_async_parallel(byte[] data, Set<Connection> cs);
	
	// String commands
	public void send_sync(String data, Connection c);
	public void send_async(String data, Connection c);
	public void send_sync(String data, Set<Connection> cs);
	public void send_async(String data, Set<Connection> cs);
	public void send_sync_parallel(String data, Set<Connection> cs);
	public void send_async_parallel(String data, Set<Connection> cs);
	
	// Object commands
	public void send_sync(Object data, Connection c);
	public void send_async(Object data, Connection c);
	public void send_sync(Object data, Set<Connection> cs);
	public void send_async(Object data, Set<Connection> cs);
	public void send_sync_parallel(Object data, Set<Connection> cs);
	public void send_async_parallel(Object data, Set<Connection> cs);
	
	// Object serialization commands
	public boolean send_object_sync(Object data, Connection c);
	public void send_object_async(Object data, Connection c);
	public void send_object_sync(Object data, Set<Connection> cs);
	public void send_object_async(Object data, Set<Connection> cs);
	public boolean send_object_sync_parallel(Object data, Set<Connection> cs);
	public void send_object_async_parallel(Object data, Set<Connection> cs);
}
