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
import uk.ac.imperial.lsds.streamsql.expressions.elong.LongColumnReference;
import uk.ac.imperial.lsds.streamsql.op.stateful.AggregationType;
import uk.ac.imperial.lsds.streamsql.op.stateful.MicroAggregation;
import uk.ac.imperial.lsds.streamsql.op.stateful.ThetaJoin;
import uk.ac.imperial.lsds.streamsql.op.stateless.Projection;
import uk.ac.imperial.lsds.streamsql.op.stateless.Selection;
import uk.ac.imperial.lsds.streamsql.predicates.ANDPredicate;
import uk.ac.imperial.lsds.streamsql.predicates.FloatComparisonPredicate;
import uk.ac.imperial.lsds.streamsql.predicates.IPredicate;
import uk.ac.imperial.lsds.streamsql.predicates.IntComparisonPredicate;

public class TestSmartGridData {
	
	private static final String usage = "usage: java TestSmartGridData";
	
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
		
		Utils.THREADS = 16;
		
		/*
		 * Query 1
		 * 
		 * select avg (load)
		 * from <input stream> [range 3600 seconds slide 1 second]
		 */
		QueryConf queryConf1 = new QueryConf(10, 1024);
		/*
		 * Set up configuration of query
		 */
		WindowType windowType1 = WindowType.fromString("range");
		long windowRange1 = 3600;
		long windowSlide1 = 1;
		int numberOfAttributesInSchema1 = 7;
		
		WindowDefinition window1 = 
			new WindowDefinition (windowType1, windowRange1, windowSlide1);
		
		int [] offsets1 = new int [numberOfAttributesInSchema1];
		/* First attribute is timestamp */
		offsets1[ 0] =  0;
		offsets1[ 1] =  8; /*     value */
		offsets1[ 2] = 12; /*  property */
		offsets1[ 3] = 16; /*      plug */
		offsets1[ 4] = 20; /* household */
		offsets1[ 5] = 24; /*     house */
		offsets1[ 6] = 28; /*   padding */
		
		int byteSize1 = 32;
		
		ITupleSchema schema1 = new TupleSchema (offsets1, byteSize1);
		
		IMicroOperatorCode q1code = new MicroAggregation (
			window1,
			AggregationType.fromString("avg"),
			new FloatColumnReference(1) /* value */
		);
		System.out.println(String.format("[DBG] %s", q1code));
		
		MicroOperator q1op;
		q1op = new MicroOperator (q1code, null, 1);
		
		Set<MicroOperator> q1operators = new HashSet<MicroOperator>();
		q1operators.add(q1op);
		SubQuery q1 = new SubQuery (0, q1operators, schema1, window1, queryConf1);
		
		/*
		 * Query 2
		 * 
		 * select avg (load)
		 * from <input stream> [range 3600 seconds slide 1 second]
		 * group by house, plug
		 */
		QueryConf queryConf2 = new QueryConf(10, 1024);
		/*
		 * Set up configuration of query
		 */
		WindowType windowType2 = WindowType.fromString("range");
		long windowRange2 = 3600;
		long windowSlide2 = 1;
		int numberOfAttributesInSchema2 = 7;
		
		WindowDefinition window2 = 
			new WindowDefinition (windowType2, windowRange2, windowSlide2);
		
		int [] offsets2 = new int [numberOfAttributesInSchema2];
		/* First attribute is timestamp */
		offsets2[ 0] =  0;
		offsets2[ 1] =  8; /*     value */
		offsets2[ 2] = 12; /*  property */
		offsets2[ 3] = 16; /*      plug */
		offsets2[ 4] = 20; /* household */
		offsets2[ 5] = 24; /*     house */
		offsets2[ 6] = 28; /*   padding */
		
		int byteSize2 = 32;
		
		ITupleSchema schema2 = new TupleSchema (offsets2, byteSize2);
		
		Expression[] groupByAttributes2 = new Expression [] {
			new IntColumnReference(3),
			new IntColumnReference(4),
			new IntColumnReference(5)
		};
		
		IMicroOperatorCode q2code = new MicroAggregation (
			window2,
			AggregationType.fromString("avg"),
			new FloatColumnReference(1), /* value */
			groupByAttributes2
		);
		System.out.println(String.format("[DBG] %s", q1code));
		
		MicroOperator q2op;
		q2op = new MicroOperator (q2code, null, 1);
		
		Set<MicroOperator> q2operators = new HashSet<MicroOperator>();
		q2operators.add(q2op);
		SubQuery q2 = new SubQuery (1, q2operators, schema2, window2, queryConf2);
		
		/*
		 * Query 3
		 * 
		 * select house, count(*)
		 * from <query 1 output stream> as S1, <query 2 output stream> as S2
		 * where S2.load > S1.load
		 */
		
		QueryConf queryConf3 = new QueryConf(1024, 1024); /* #tuples in either stream */
		
		WindowType __1st_windowType = WindowType.fromString("range");
		long __1st_windowRange = 1;
		long __1st_windowSlide = 1;
		int __1st_numberOfAttributesInSchema  = 1;
		
		WindowType __2nd_windowType = WindowType.fromString("range");
		long __2nd_windowRange = 1;
		long __2nd_windowSlide = 1;
		int __2nd_numberOfAttributesInSchema = 1;
		
		WindowDefinition __1st_window = new WindowDefinition (__1st_windowType, __1st_windowRange, __1st_windowSlide);
		WindowDefinition __2nd_window = new WindowDefinition (__2nd_windowType, __2nd_windowRange, __2nd_windowSlide);
		
		int [] __1st_offsets = new int[__1st_numberOfAttributesInSchema];
		__1st_offsets[0] = 0;
		
		int __1st_byteSize = 8;
		
		ITupleSchema __1st_schema = new TupleSchema (__1st_offsets, __1st_byteSize);
		
		int [] __2nd_offsets = new int[__2nd_numberOfAttributesInSchema];
		__2nd_offsets[0] = 0;

		int __2nd_byteSize = 8;
		
		ITupleSchema __2nd_schema = new TupleSchema (__2nd_offsets, __2nd_byteSize);

		IPredicate joinPredicate = null;
		
		IMicroOperatorCode q3code1 = new ThetaJoin(joinPredicate);
		System.out.println(String.format("[DBG] %s", q3code1));
		
		MicroOperator q3op1 = new MicroOperator(q3code1, null, 1);
		
		/* The second micro-operator is an aggregation over the joined stream */
		
		Selection having = new Selection (
			new FloatComparisonPredicate(
				FloatComparisonPredicate.GREATER_OP, 
				new FloatColumnReference(3), 
				new FloatColumnReference(4)
				)
			);
		
		IMicroOperatorCode q3code2 = new MicroAggregation (
			__1st_window,
			AggregationType.COUNT, 
			new FloatColumnReference(1), /* value */
			new Expression [] {
				new IntColumnReference(1)
			},
			having
		);
		
		MicroOperator q3op2 = new MicroOperator(q3code2, null, 1);
		
		/* Connect micro-operators */
		q3op1.connectTo(6001, q3op2);
		Set<MicroOperator> q3operators = new HashSet<>();
		q3operators.add(q3op1);
		q3operators.add(q3op2);
		
		SubQuery q3 = new SubQuery (2, q3operators, __1st_schema, __1st_window, queryConf3, __2nd_schema, __2nd_window);
		
		Utils._CIRCULAR_BUFFER_ = 64 * 1024 * 1024;
		Utils._UNBOUNDED_BUFFER_ = 1048576; /* 1MB */
		
		/* Connect queries */
		q1.connectTo(1001, q3);
		q2.connectTo(1002, q3);
		
		Set<SubQuery> queries = new HashSet<SubQuery>();
		queries.add(q1);
		queries.add(q2);
		queries.add(q3);
		
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
							buffer.clear();
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
