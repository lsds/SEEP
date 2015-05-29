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
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatConstant;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntConstant;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntDivision;
import uk.ac.imperial.lsds.streamsql.expressions.elong.LongColumnReference;
import uk.ac.imperial.lsds.streamsql.op.stateful.AggregationType;
import uk.ac.imperial.lsds.streamsql.op.stateful.MicroAggregation;
import uk.ac.imperial.lsds.streamsql.op.stateless.Projection;
import uk.ac.imperial.lsds.streamsql.op.stateless.Selection;
import uk.ac.imperial.lsds.streamsql.predicates.FloatComparisonPredicate;

public class TestLinearRoadBenchmarkData {
	
	private static final String usage = "usage: java TestLinearRoadBenchmarkData";
	
	public static void main (String [] args) {
		
		String hostname = "localhost";
		int port = 6667;
		
		int bundle = 512;
		int tupleSize = 32;
		
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
		
		Utils.THREADS = 1;
		
		/*
		 * Query 1 (Projection)
		 * 
		 * select (timestamp), vehicle, speed, highway, (lane), direction, position / 5280 as segment 
		 * from <input stream>
		 * 
		 * Note that we introduce `lane` in the output stream instead of padding
		 */
		
		QueryConf queryConf1 = new QueryConf(1048576, 1024);
		
		WindowType windowType1 = WindowType.fromString("row");
		long windowRange1 = 1024;
		long windowSlide1 = 1024;
		int numberOfAttributesInSchema1 = 7;
		
		WindowDefinition window1 = 
			new WindowDefinition (windowType1, windowRange1, windowSlide1);
		
		int [] offsets1 = new int [numberOfAttributesInSchema1];
		/* First attribute is timestamp */
		offsets1[ 0] =  0;
		offsets1[ 1] =  8; /*   vehicle */ 
		offsets1[ 2] = 12; /*     speed */
		offsets1[ 3] = 16; /*   highway */
		offsets1[ 4] = 20; /*      lane */
		offsets1[ 5] = 24; /* direction */
		offsets1[ 6] = 28; /*  position */
		
		int byteSize1 = 32;
		
		ITupleSchema schema1 = new TupleSchema (offsets1, byteSize1);
		
		Expression[] expression1 = new Expression [] {
			new  LongColumnReference(0), /* timestamp */
			new   IntColumnReference(1), /*   vehicle */
			new FloatColumnReference(2), /*     speed */
			new   IntColumnReference(3), /*   highway */
			new   IntColumnReference(4), /*      lane */
			new   IntColumnReference(5), /* direction */
			new IntDivision (
					new IntColumnReference(6), new IntConstant(5280)
			) 
		};
		
		IMicroOperatorCode q1code = new Projection(expression1);
		System.out.println(String.format("[DBG] %s", q1code));
		
		MicroOperator q1op = new MicroOperator(q1code, null, 1);

		Set<MicroOperator> q1operators = new HashSet<>();
		q1operators.add(q1op);
		
		SubQuery q1 = new SubQuery (0, q1operators, schema1, window1, queryConf1);
		
		/*
		 * Query 2 (UDF)
		 * 
		 * a) select distinct B.vehicle, B.segment, B.direction, B.highway
		 *    from <query 1 output stream> [range 30 seconds] as A
		 *    from <query 1 output stream> [partition by vehicle rows 1] as B
		 *    where A.vehicle = B.vehicle
		 * 
		 * b) select IStream(*) from <query 2a) output stream>
		 * 
		 */
		
//		QueryConf queryConf2 = new QueryConf(10, 1024);
//		
//		WindowType windowType2 = WindowType.fromString("range");
//		long windowRange2 = 30;
//		long windowSlide2 =  1;
//		int numberOfAttributesInSchema2 = 7;
//		
//		WindowDefinition window2 = 
//			new WindowDefinition (windowType2, windowRange2, windowSlide2);
//		
//		int [] offsets2 = new int [numberOfAttributesInSchema2];
//		/* First attribute is timestamp */
//		offsets2[ 0] =  0;
//		offsets2[ 1] =  8; /*   vehicle */ 
//		offsets2[ 2] = 12; /*     speed */
//		offsets2[ 3] = 16; /*   highway */
//		offsets2[ 4] = 20; /*      lane */
//		offsets2[ 5] = 24; /* direction */
//		offsets2[ 6] = 28; /*   segment */
//		
//		int byteSize2 = 32;
//		
//		ITupleSchema schema2 = new TupleSchema (offsets2, byteSize2);
//		
//		IMicroOperatorCode q2code = new LRBUDFCPU(schema2, new IntColumnReference(1));
//		System.out.println(String.format("[DBG] %s", q2code));
//		
//		MicroOperator q2op = new MicroOperator(q2code, null, 1);
//
//		Set<MicroOperator> q2operators = new HashSet<>();
//		q2operators.add(q2op);
//		
//		SubQuery q2 = new SubQuery (0, q2operators, schema2, window2, queryConf2);
		
		/*
		 * Query 3 (Aggregation)
		 * 
		 * select segment, direction, highway
		 * from <query 1 output stream> [range 5 minutes]
		 * group by segment, direction, highway
		 * having AVG(speed) < 40
		 */
		
		QueryConf queryConf3 = new QueryConf(1048576, 1024);
		/*
		 * Set up configuration of query
		 */
		WindowType windowType3 = WindowType.fromString("range");
		long windowRange3 = 300;
		long windowSlide3 =   1;
		int numberOfAttributesInSchema3 = 7;
		
		WindowDefinition window3 = 
			new WindowDefinition (windowType3, windowRange3, windowSlide3);
		
		int [] offsets3 = new int [numberOfAttributesInSchema3];
		/* First attribute is timestamp */
		offsets3[ 0] =  0;
		offsets3[ 1] =  8; /*   vehicle */ 
		offsets3[ 2] = 12; /*     speed */
		offsets3[ 3] = 16; /*   highway */
		offsets3[ 4] = 20; /*      lane */
		offsets3[ 5] = 24; /* direction */
		offsets3[ 6] = 28; /*   segment */
		
		int byteSize3 = 32;
		
		ITupleSchema schema3 = new TupleSchema (offsets3, byteSize3);
		
		/*
		 * The selection predicate is applied to the output of the
		 * aggregation which is:
		 * 
		 * 0:timestamp, 1:highway, 2:direction, 3:segment, 4:avg(speed)
		 */
		Selection having = new Selection(
			new FloatComparisonPredicate(
				FloatComparisonPredicate.LESS_OP, 
				new FloatColumnReference(4), 
				new FloatConstant(40F)
			)
		);
		
		IMicroOperatorCode q3code = new MicroAggregation(
			window3,
			AggregationType.AVG, 
			new FloatColumnReference(2), /* speed */
			new Expression [] { 
				new IntColumnReference(3), /*   highway */
				new IntColumnReference(5), /* direction */
				new IntColumnReference(6)  /*   segment */
			},
			having
		);
		
		System.out.println(String.format("[DBG] %s", q3code));
		
		MicroOperator q3op = new MicroOperator(q3code, null, 1);

		Set<MicroOperator> q3operators = new HashSet<>();
		q3operators.add(q3op);
		
		SubQuery q3 = new SubQuery (0, q3operators, schema3, window3, queryConf3);
		
		/*
		 * Query 4 (Aggregation in two steps)
		 * 
		 * The input stream consists of:
		 * 
		 * 0:timestamp, 1:vehicle, 2:speed, 3:highway, 4:lane, 5:direction, 6:segment
		 * 
		 * select timestamp, vehicle, highway, direction, segment, count(*)
		 * from SegSpeedStr [range 30 slide 1]
		 * group by highway, direction, segment, vehicle
		 */
		
//		QueryConf queryConf4 = new QueryConf(10, 1024);
//		/*
//		 * Set up configuration of query
//		 */
//		WindowType windowType4 = WindowType.fromString("range");
//		long windowRange4 = 30;
//		long windowSlide4 =  1;
//		int numberOfAttributesInSchema4 = 7;
//		
//		WindowDefinition window4 = 
//			new WindowDefinition (windowType4, windowRange4, windowSlide4);
//		
//		int [] offsets4 = new int [numberOfAttributesInSchema4];
//		/* First attribute is timestamp */
//		offsets4[ 0] =  0;
//		offsets4[ 1] =  8; /*   vehicle */ 
//		offsets4[ 2] = 12; /*     speed */
//		offsets4[ 3] = 16; /*   highway */
//		offsets4[ 4] = 20; /*      lane */
//		offsets4[ 5] = 24; /* direction */
//		offsets4[ 6] = 28; /*   segment */
//		
//		int byteSize4 = 32;
//		
//		ITupleSchema schema4 = new TupleSchema (offsets4, byteSize4);
//		
//		IMicroOperatorCode q4code1 = new MicroAggregation (
//			window4,
//			AggregationType.COUNT, 
//			new FloatColumnReference(1), /* Does it matter, since this is a count? No */
//			new Expression [] { 
//				new IntColumnReference(1),  /*  vehicle */
//				new IntColumnReference(3), /*   highway */
//				new IntColumnReference(5), /* direction */
//				new IntColumnReference(6)  /*   segment */
//			}
//		);
//		System.out.println(String.format("[DBG] %s", q4code1));
//		
//		MicroOperator q4op1 = new MicroOperator(q4code1, null, 1);
//		
//		/* The output schema of the first microoperator is: 
//		 * 
//		 * 0: timestamp, 1: vehicle, 2: highway, 3: direction, 4: segment, 5: count
//		 */
//		IMicroOperatorCode q4code2 = new MicroAggregation (
//			window4,
//			AggregationType.COUNT, 
//			new FloatColumnReference(1), /* vehicle */
//			new Expression [] { 
//				new IntColumnReference(2), /*   highway */
//				new IntColumnReference(3), /* direction */
//				new IntColumnReference(4)  /*   segment */
//			}
//		);
//		System.out.println(String.format("[DBG] %s", q4code2));
//		
//		MicroOperator q4op2 = new MicroOperator(q4code2, null, 1);
//		
//		q4op1.connectTo(6001, q4op2);
//		Set<MicroOperator> q4operators = new HashSet<>();
//		q4operators.add(q4op1);
//		
//		SubQuery q4 = new SubQuery (0, q4operators, schema4, window4, queryConf4);
		
		Utils._CIRCULAR_BUFFER_ = 1024 * 1024 * 1024;
		Utils._UNBOUNDED_BUFFER_ = 8 * 1048576; /* 1MB */
		
		/* Query graph */
		// q1.connectTo(10000, q2);
		q1.connectTo(10001, q3);
		// q1.connectTo(10002, q4);
		
		Set<SubQuery> queries = new HashSet<SubQuery>();
		queries.add(q1);
		// queries.add(q2);
		queries.add(q3);
		// queries.add(q4);
		
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
							if (! buffer.hasRemaining()) {
								buffer.rewind();
								operator.processData (buffer.array(), buffer.capacity());
							
								/* Measurements */
								Bytes += buffer.capacity();
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
