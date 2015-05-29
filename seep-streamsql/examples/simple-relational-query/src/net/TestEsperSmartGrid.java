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


public class TestEsperSmartGrid extends TestEsper {

	public static void main (String [] args) {
		
		String hostname = "localhost";
		int port = 6667;
		
		int bundle = 512;
		int tupleSize = 64;
		int threads = 1;
				
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
			if (args[i].equals("-t")) {
				threads = Integer.parseInt(args[j]);
			} else {
				System.err.println(String.format("error: unknown flag %s %s", args[i], args[j]));
				System.exit(1);
			}
			i = j + 1;
		}
		
		int _BUFFER_ = bundle * tupleSize;
		System.out.println(String.format("[DBG] %6d bytes/buffer", _BUFFER_));
		
		TestEsper esper = new TestEsper();
		esper.initEngine(threads);
	
		Map<String, Object> bindingForEventType = new HashMap<String, Object>();
		bindingForEventType.put("timestamp", Long.class);
		bindingForEventType.put("value", Float.class);
		bindingForEventType.put("property", Integer.class);
		bindingForEventType.put("plug", Integer.class);
		bindingForEventType.put("household", Integer.class);
		bindingForEventType.put("house", Integer.class);
		
		esper.addEventType("input", bindingForEventType);
		
		Map<String, Object> bindingForEventType2 = new HashMap<String, Object>();
		bindingForEventType2.put("timestamp", Long.class);
		bindingForEventType2.put("load", Double.class);
		
		esper.addEventType("q1_out", bindingForEventType2);

		Map<String, Object> bindingForEventType3 = new HashMap<String, Object>();
		bindingForEventType3.put("timestamp", Long.class);
		bindingForEventType3.put("house", Integer.class);
		bindingForEventType3.put("load", Double.class);
		
		esper.addEventType("q2_out", bindingForEventType3);
		
		esper.setupEngine();
		
		/*
		 * Query 1
		 * 
		 * select avg (load)
		 * from <input stream> [range 3600 seconds slide 1 second]
		 */
		String query1 = ""
				+ "insert into q1_out "
				+ "select timestamp, avg(value) as load "
				+ "from input(property=1).win:ext_timed(timestamp, 3600) ";

		esper.addQuery(query1, 1);
		
		/*
		 * Query 2
		 * 
		 * select avg (load)
		 * from <input stream> [range 3600 seconds slide 1 second]
		 * group by house, household, plug
		 */
		String query2 = ""
				+ "insert into q2_out "
				+ "select timestamp, house, avg(value) as load "
				+ "from input(property=1).win:ext_timed(timestamp, 3600) "
				+ "group by house, household, plug " ;

		esper.addQuery(query2, 2);
		
		/*
		 * Query 3
		 * 
		 * select S1.timestamp, S2.timestamp, S2.house, count(*)
		 * from <query 1 output stream> as S1, <query 2 output stream> as S2
		 * group by S2.house
		 * where avg(S2.load) > avg(S1.load)
		 */
		String query3 = ""
				+ "select S1.timestamp, S2.timestamp, S2.house, count(*) "
				+ "from q1_out.win:ext_timed(timestamp, 1) as S1, q2_out.win:ext_timed(timestamp, 1) as S2 "
				+ "group by S2.house " 
				+ "having avg(S2.load) > avg(S1.load) " ;
		
		esper.addQuery(query3, 3);

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
							if (buffer.remaining() == 0) {
							
								buffer.rewind();
								
								item.clear();
								
								long nitems = 0L;
								
								while (buffer.hasRemaining()) {
									
									item.put("timestamp",   buffer.getLong());
									item.put("value",       buffer.getFloat());
									item.put("property",    buffer.getInt());
									item.put("plug",        buffer.getInt());
									item.put("household",   buffer.getInt());
									item.put("house",       buffer.getInt());
									// we have 4 bytes padding, thus, we need to move the pointer
									buffer.getInt();
									
									esper.sendEvent(item, "input");
									
									nitems ++;
								}
								
								/* System.out.println(String.format("[DBG] %d items sent", nitems)); */
								
								/* Measurements */
								Bytes += buffer.capacity();
								
								buffer.clear();
							}
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
