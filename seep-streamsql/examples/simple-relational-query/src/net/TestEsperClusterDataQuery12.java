package net;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class TestEsperClusterDataQuery12 extends TestEsper {

	public static void main (String [] args) {
		
		String hostname = "localhost";
		int port = 6667;
		
		int bundle = 512;
		int tupleSize = 64;
		
		String query = "";
		
		/* Parse command line arguments */
		int i, j;
		for (i = 0; i < args.length; ) {
			if ((j = i + 1) == args.length) {
				System.exit(1);
			}
			if (args[i].equals("-h")) {
				hostname = args[j];
			} else
			if (args[i].equals("-p")) {
				port = Integer.parseInt(args[j]);
			} else
			if (args[i].equals("-b")) {
				bundle = Integer.parseInt(args[j]);
			} else
			if (args[i].equals("-q")) {
				if (Integer.parseInt(args[j]) == 1) 
					query = ""
							+ "select timestamp, category, count(*) as failedEvents "
							+ "from input(eventType=3).win:length(100)"
							+ "group by category";
				else if (Integer.parseInt(args[j]) == 2) 
					query = ""
							+ "select timestamp, jobId, max(cpu) as maxCpu "
//							+ "from input.win:length(60)"
							+ "from input.win:ext_timed(timestamp, 60)"
							+ "group by jobId " ;
//							+ "having max(cpu) > 1 ";
				else {
					System.err.println(String.format("error: unknown query flag %s", args[j]));
					System.exit(1);
				}
				
			} else {
				System.err.println(String.format("error: unknown flag %s %s", args[i], args[j]));
				System.exit(1);
			}
			i = j + 1;
		}
		
		int _BUFFER_ = bundle * tupleSize;
		System.out.println(String.format("[DBG] %6d bytes/buffer", _BUFFER_));
		
		TestEsper esper = new TestEsper();
		esper.initEngine();
	
		Map<String, Object> bindingForEventType = new HashMap<String, Object>();
		bindingForEventType.put("timestamp", Long.class);
		bindingForEventType.put("jobId", Long.class);
		bindingForEventType.put("taskId", Long.class);
		bindingForEventType.put("machineId", Long.class);
		bindingForEventType.put("eventType", Integer.class);
		bindingForEventType.put("userId", Integer.class);
		bindingForEventType.put("category", Integer.class);
		bindingForEventType.put("priority", Integer.class);
		bindingForEventType.put("cpu", Float.class);
		bindingForEventType.put("ram", Float.class);
		bindingForEventType.put("disk", Float.class);
		bindingForEventType.put("constraints", Integer.class);
		
		esper.addEventType("input", bindingForEventType);
		esper.setupEngine();
		esper.addQuery(query);
		
		/* Measurements */
		long Bytes = 0L;
		/* 
		long count = 0L;
		long previous = 0L; 
		long t, _t = 0L;
		double dt, rate, MB;
		double _1MB = 1000000.0; // Or, MiB 1048576.0;
		*/
		try {
		
			ServerSocketChannel server = ServerSocketChannel.open();
		
			server.bind(new InetSocketAddress(hostname, port));
			server.configureBlocking(false);
		
			Selector selector = Selector.open();
			/* (SelectionKey) */ server.register(selector, SelectionKey.OP_ACCEPT);
			
			System.out.println("[DBG] ^");

			ByteBuffer buffer = ByteBuffer.allocate(_BUFFER_);
			
			Map<String, Object> item = new HashMap<String, Object>();
			while (true) {
			
				if (selector.select() == 0)
					continue;
				Set<SelectionKey> keys = selector.selectedKeys();
				
				Iterator<SelectionKey> iterator = keys.iterator();
				while (iterator.hasNext()) {
				
					SelectionKey key = iterator.next();
					
					if (key.isAcceptable()) {
						
						System.out.println("[DBG] key is acceptable");
						ServerSocketChannel _server = 
							(ServerSocketChannel) key.channel();
						SocketChannel client = _server.accept();
						if (client != null) {
							System.out.println("[DBG] accepted client");
							client.configureBlocking(false);
							/* (SelectionKey) */ client.register(selector, SelectionKey.OP_READ);
						}
					
					} else if (key.isReadable()) {
						
						SocketChannel client = (SocketChannel) key.channel();
						
						int bytes = 0;
						if ((bytes = client.read(buffer)) > 0) {
							
							/*
							 * System.out.println(String.format("[DBG] %6d bytes received; %6d bytes remain", 
							 *		bytes, buffer.remaining()));
							 *
							 * Make sure the buffer is rewind before reading.
							 */
							buffer.rewind();
							
							item.clear();
							while (buffer.hasRemaining()) {
								item.put("timestamp", buffer.getLong());
								item.put("jobId", buffer.getLong());
								item.put("taskId", buffer.getLong());
								item.put("machineId", buffer.getLong());
								item.put("eventType", buffer.getInt());
								item.put("userId", buffer.getInt());
								item.put("category", buffer.getInt());
								item.put("priority", buffer.getInt());
								item.put("cpu", buffer.getFloat());
								item.put("ram", buffer.getFloat());
								item.put("disk", buffer.getFloat());
								item.put("constraints", buffer.getInt());
							}
							
							esper.sendEvent(item, "input");
							
							buffer.clear();
							
							/* Measurements */
							Bytes += bytes;
							/*
							count += 1;
							if (count % 10000 == 0) {
								t = System.currentTimeMillis();
								if (_t > 0 && previous > 0) {
									dt = ((double) (t - _t)) / 1000.;
									MB = ((double) (Bytes - previous)) / _1MB;
									rate = MB / dt;
									System.out.println(String.format("%10.3f MB %10.3f sec %10.3f MB/s %10.3f Gbps", 
									MB, dt, rate, ((rate * 8.)/1000.)));
								}
								_t = t;
								previous = Bytes;
							}
							*/
						}
						if (bytes < 0) {
							System.out.println("[DBG] client connection closed");
							System.out.println(String.format("[DBG] %d bytes received", Bytes));
							client.close();
						}
						
					} else {
						
						System.err.println("error: unknown selection key");
						System.exit(1);
					}
					
					iterator.remove();
				}
			}
		} catch (Exception e) {
			System.err.println(String.format("error: %s", e.getMessage()));
			e.printStackTrace();
			System.exit(1);
		}
	}
	
}
