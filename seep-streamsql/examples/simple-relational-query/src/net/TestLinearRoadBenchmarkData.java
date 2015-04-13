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
import uk.ac.imperial.lsds.streamsql.op.stateful.ThetaJoin;
import uk.ac.imperial.lsds.streamsql.op.stateless.Projection;
import uk.ac.imperial.lsds.streamsql.op.stateless.Selection;
import uk.ac.imperial.lsds.streamsql.predicates.ANDPredicate;
import uk.ac.imperial.lsds.streamsql.predicates.FloatComparisonPredicate;
import uk.ac.imperial.lsds.streamsql.predicates.IPredicate;
import uk.ac.imperial.lsds.streamsql.predicates.IntComparisonPredicate;

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
		
		Utils.THREADS = 4;
		QueryConf queryConf1 = new QueryConf(10, 1024);
		/*
		 * Set up configuration of query
		 */
		WindowType windowType1 = WindowType.fromString("range");
		long windowRange1 = 1;
		long windowSlide1 = 1;
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
		
		/*
		 * Query 1 (Projection)
		 * 
		 * select (timestamp), vehicle, speed, highway, (lane), direction, position / 5280 as segment 
		 * from <input stream>
		 * 
		 * Note that we introduce `lane` in the output stream instead of padding
		 */
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
		 * c) select segment, direction, highway, count(vehicle) as numVehicles
		 *    from <query 2b) output stream>
		 *    group by segment, direction, highway
		 */
		
		QueryConf queryConf2 = new QueryConf(10, 1024);
		/*
		 * Set up configuration of query
		 */
		WindowType windowType2 = WindowType.fromString("range");
		long windowRange2 = 30;
		long windowSlide2 =  1;
		int numberOfAttributesInSchema2 = 7;
		
		WindowDefinition window2 = 
			new WindowDefinition (windowType2, windowRange2, windowSlide2);
		
		int [] offsets2 = new int [numberOfAttributesInSchema2];
		/* First attribute is timestamp */
		offsets2[ 0] =  0;
		offsets2[ 1] =  8; /*   vehicle */ 
		offsets2[ 2] = 12; /*     speed */
		offsets2[ 3] = 16; /*   highway */
		offsets2[ 4] = 20; /*      lane */
		offsets2[ 5] = 24; /* direction */
		offsets2[ 6] = 28; /*  position */
		
		int byteSize2 = 32;
		
		ITupleSchema schema2 = new TupleSchema (offsets2, byteSize2);
		
		IMicroOperatorCode q2code = new LRBUDF(
			window2,
			new FloatColumnReference(2), /* speed */
			new Expression [] { 
				new IntColumnReference(3), /*   highway */
				new IntColumnReference(5), /* direction */
				new IntColumnReference(6)  /*   segment */
			}
			);
		
		System.out.println(String.format("[DBG] %s", q2code));
		
		MicroOperator q2op = new MicroOperator(q2code, null, 1);

		Set<MicroOperator> q2operators = new HashSet<>();
		q2operators.add(q2op);
		
		SubQuery q2 = new SubQuery (0, q2operators, schema2, window2, queryConf2);
		
		/*
		 * Query 3 (Aggregation)
		 * 
		 * select segment, direction, highway
		 * from <query 1 output stream> [range 5 minutes]
		 * group by segment, direction, highway
		 * having AVG(speed) < 40
		 */
		
		QueryConf queryConf3 = new QueryConf(10, 1024);
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
		
		Selection having = new Selection(
			new FloatComparisonPredicate(
				FloatComparisonPredicate.LESS_OP, 
				new FloatColumnReference(3), 
				new FloatConstant(40f)
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
		 * Query 4 (Join)
		 * 
		 * select vehicle, basetoll * (numVehicles - 150)^2
		 * from <query 2 output stream> [Now] as A
		 * from <query 3 output stream> [ ? ] as B
		 * where A.segment   = B.segment   and
		 *       A.direction = B.direction and
		 *       A.highway   = B.highway 
		 */
		
		QueryConf queryConf4 = new QueryConf(1024, 1024); /* #tuples in either stream */
		
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
		
		int [] __1st_offsets = new int[__1st_numberOfAttributesInSchema + 1];
		__1st_offsets[0] = 0;
		
		int __1st_byteSize = 8;
		
		ITupleSchema __1st_schema = new TupleSchema (__1st_offsets, __1st_byteSize);
		
		int [] __2nd_offsets = new int[__2nd_numberOfAttributesInSchema + 1];
		__2nd_offsets[0] = 0;

		int __2nd_byteSize = 8;
		
		ITupleSchema __2nd_schema = new TupleSchema (__2nd_offsets, __2nd_byteSize);

		IPredicate joinPredicate =  
			new ANDPredicate (
				new IntComparisonPredicate(
					IntComparisonPredicate.EQUAL_OP, 
					new IntColumnReference(1),
					new IntColumnReference(1)
				),
				new IntComparisonPredicate(
					IntComparisonPredicate.EQUAL_OP, 
					new IntColumnReference(1),
					new IntColumnReference(1)
				),
				new IntComparisonPredicate(
					IntComparisonPredicate.EQUAL_OP, 
					new IntColumnReference(1),
					new IntColumnReference(1)
				)
			);
		
		IMicroOperatorCode q4code = new ThetaJoin(joinPredicate);
		System.out.println(String.format("[DBG] %s", q4code));
		
		MicroOperator q4op = new MicroOperator(q4code, null, 1);

		Set<MicroOperator> q4operators = new HashSet<>();
		q4operators.add(q4op);
		
		SubQuery q4 = new SubQuery (0, q4operators, __1st_schema, __1st_window, queryConf4, __2nd_schema, __2nd_window);
		
		Utils._CIRCULAR_BUFFER_ = 1024 * 1024 * 1024;
		Utils._UNBOUNDED_BUFFER_ = 256 * 1048576; /* 1MB */
		
		/* Query graph */
		q1.connectTo(10000, q2);
		q1.connectTo(10001, q3);
		q2.connectTo(10002, q4);
		q3.connectTo(10002, q4);
		
		Set<SubQuery> queries = new HashSet<SubQuery>();
		queries.add(q1);
		queries.add(q2);
		queries.add(q3);
		queries.add(q4);
		
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
