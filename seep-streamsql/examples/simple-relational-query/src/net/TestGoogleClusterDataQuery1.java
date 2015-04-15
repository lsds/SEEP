package net;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.MicroOperator;
import uk.ac.imperial.lsds.seep.multi.MultiOperator;
import uk.ac.imperial.lsds.seep.multi.QueryConf;
import uk.ac.imperial.lsds.seep.multi.SubQuery;
import uk.ac.imperial.lsds.seep.multi.TupleSchema;
import uk.ac.imperial.lsds.seep.multi.Utils;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition.WindowType;
import uk.ac.imperial.lsds.streamsql.expressions.Expression;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntConstant;
import uk.ac.imperial.lsds.streamsql.expressions.elong.LongColumnReference;
import uk.ac.imperial.lsds.streamsql.op.stateful.AggregationType;
import uk.ac.imperial.lsds.streamsql.op.stateful.MicroAggregation;
import uk.ac.imperial.lsds.streamsql.op.stateless.Projection;
import uk.ac.imperial.lsds.streamsql.op.stateless.Selection;
import uk.ac.imperial.lsds.streamsql.predicates.FloatComparisonPredicate;
import uk.ac.imperial.lsds.streamsql.predicates.IPredicate;
import uk.ac.imperial.lsds.streamsql.predicates.IntComparisonPredicate;

public class TestGoogleClusterDataQuery1 {
	
	private static final String usage = "usage: java TestGoogleClusterDataReduction";
	
	public static void main (String [] args) {
		
		String hostname = "localhost";
		int port = 6667;
		
		int bundle = 512;
		int tupleSize = 64;
		
		/* Parse command line arguments */
		int i, j;
		for (i = 0; i < args.length; ) {
			if ((j = i + 1) == args.length) {
				System.err.println(usage);
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
			} else {
				System.err.println(String.format("error: unknown flag %s %s", args[i], args[j]));
				System.exit(1);
			}
			i = j + 1;
		}
		
		int _BUFFER_ = bundle * tupleSize;
		System.out.println(String.format("[DBG] %6d bytes/buffer", _BUFFER_));
		
		/*
		 * Set up configuration of system
		 */
		Utils.CPU =  true;
		Utils.GPU = false;
		
		Utils.HYBRID = Utils.CPU && Utils.GPU;
		
		Utils.THREADS = 8;
		QueryConf queryConf = new QueryConf(60, 1024);
		/*
		 * Set up configuration of query
		 */
		WindowType windowType = WindowType.fromString("row");
		long windowRange = 100;
		long windowSlide =  10;
		int numberOfAttributesInSchema = 12;
		
		WindowDefinition window = 
			new WindowDefinition (windowType, windowRange, windowSlide);
		
		int [] offsets = new int [numberOfAttributesInSchema];
		/* First attribute is timestamp */
		offsets[ 0] =  0;
		offsets[ 1] =  8; /*       jobId */
		offsets[ 2] = 16; /*      taskId */
		offsets[ 3] = 24; /*   machineId */
		offsets[ 4] = 32; /*   eventType */
		offsets[ 5] = 36; /*      userId */
		offsets[ 6] = 40; /*    category */
		offsets[ 7] = 44; /*    priority */
		offsets[ 8] = 48; /*         cpu */
		offsets[ 9] = 52; /*         ram */
		offsets[10] = 56; /*        disk */
		offsets[11] = 60; /* constraints */
		
		int byteSize = 64;
		
		ITupleSchema schema = new TupleSchema (offsets, byteSize);
		
		IPredicate predicate =  new IntComparisonPredicate(
			IntComparisonPredicate.EQUAL_OP, 
			new IntColumnReference(4),
			new IntConstant(3)); /* FAIL == 3 */
		
		IMicroOperatorCode selectionCode = new Selection(predicate);
		MicroOperator uoperator1;
		uoperator1 = new MicroOperator (selectionCode, null, 1);
		
		/* After selection, apply aggregation */
		
		AggregationType aggregationType = AggregationType.fromString("count");
		
		Expression [] groupBy = new Expression [] {
			new IntColumnReference(6)
		};
		
		IMicroOperatorCode aggregationCode = new MicroAggregation (
			window,
			aggregationType,
			new FloatColumnReference(8), /* count(*), does not really matter */
			groupBy
			);
		
		MicroOperator uoperator2;
		uoperator2 = new MicroOperator (aggregationCode, null, 1);
		
		uoperator1.connectTo(6001, uoperator2);
		Set<MicroOperator> operators = new HashSet<MicroOperator>();
		operators.add(uoperator1);
		operators.add(uoperator2);
		
		Utils._CIRCULAR_BUFFER_ = 1024 * 1024 * 1024;
		Utils._UNBOUNDED_BUFFER_ = 128 * 1048576; /* 1MB */
		
		Set<SubQuery> queries = new HashSet<SubQuery>();
		SubQuery query = new SubQuery (0, operators, schema, window, queryConf);
		queries.add(query);
		MultiOperator operator = new MultiOperator(queries, 0);
		operator.setup();
		
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
							operator.processData (buffer.array(), bytes);
							
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
