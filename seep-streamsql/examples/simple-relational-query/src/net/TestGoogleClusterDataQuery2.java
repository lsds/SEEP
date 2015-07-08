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

import uk.ac.imperial.lsds.seep.multi.AggregationType;
import uk.ac.imperial.lsds.seep.multi.IAggregateOperator;
import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.MicroOperator;
import uk.ac.imperial.lsds.seep.multi.MultiOperator;
import uk.ac.imperial.lsds.seep.multi.QueryConf;
import uk.ac.imperial.lsds.seep.multi.SubQuery;
import uk.ac.imperial.lsds.seep.multi.TheCPU;
import uk.ac.imperial.lsds.seep.multi.TheGPU;
import uk.ac.imperial.lsds.seep.multi.TupleSchema;
import uk.ac.imperial.lsds.seep.multi.Utils;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition.WindowType;
import uk.ac.imperial.lsds.streamsql.expressions.Expression;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatConstant;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.elong.LongColumnReference;
import uk.ac.imperial.lsds.streamsql.op.gpu.stateful.PartialAggregationKernel;
import uk.ac.imperial.lsds.streamsql.op.stateful.MicroAggregation;
import uk.ac.imperial.lsds.streamsql.op.stateful.PartialMicroAggregation;
import uk.ac.imperial.lsds.streamsql.op.stateless.Projection;
import uk.ac.imperial.lsds.streamsql.op.stateless.Selection;
import uk.ac.imperial.lsds.streamsql.predicates.FloatComparisonPredicate;

public class TestGoogleClusterDataQuery2 {
	
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
		Utils.GPU =  true;
		
		Utils.HYBRID = Utils.CPU && Utils.GPU;
		
		Utils.THREADS = 2;
		
		Utils._CIRCULAR_BUFFER_ = 1024 * 1024 * 1024;
		Utils._UNBOUNDED_BUFFER_ = 4 * 1048576; /* 1MB */
		
		QueryConf queryConf = new QueryConf(1048576, 1024);
		
		TheGPU.getInstance().init(1);
		
		/*
		 * Set up configuration of query
		 */
		WindowType windowType = WindowType.fromString("range");
		long windowRange = 60;
		long windowSlide = 1;
		int numberOfAttributesInSchema = 12;
		
		AggregationType aggregationType = AggregationType.fromString("cnt");
		
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
		
		/* 0:undefined 1:int, 2:float, 3:long */
		schema.setType( 0, 3);
		schema.setType( 1, 3);
		schema.setType( 2, 3);
		schema.setType( 3, 3);
		schema.setType( 4, 1);
		schema.setType( 5, 1);
		schema.setType( 6, 1);
		schema.setType( 7, 1);
		schema.setType( 8, 2);
		schema.setType( 9, 2);
		schema.setType(10, 2);
		schema.setType(11, 1);
		
		Expression [] groupBy = new Expression [] {
			new IntColumnReference(6)
		};
		
		/*
		 * The output of the aggregation is:
		 * 
		 * 0:timestamp, 1:jobId, 2: maxCpu
		 *  
		 */
//		Selection having = new Selection (
//			new FloatComparisonPredicate(
//				FloatComparisonPredicate.GREATER_OP, 
//				new FloatColumnReference(2), 
//				new FloatConstant(100F)
//				)
//			);
		
		IMicroOperatorCode aggregationCode = new PartialMicroAggregation(
				window,
				aggregationType,
				new FloatColumnReference(8), /* cpu */
				groupBy
				);
		
		IMicroOperatorCode gpuAggCode =
				new PartialAggregationKernel
					(
						aggregationType,
						new FloatColumnReference(8), 
						groupBy, 
						null,
						schema
					);

		((PartialAggregationKernel) gpuAggCode).setBatchSize(1048576);
		((PartialAggregationKernel) gpuAggCode).setWindowDefinition(window);
		((PartialAggregationKernel) gpuAggCode).setup();
		
		MicroOperator uoperator;
		if (Utils.GPU && ! Utils.HYBRID)
			uoperator = new MicroOperator (gpuAggCode, aggregationCode, 1);
		else 
			uoperator = new MicroOperator (aggregationCode, gpuAggCode, 1);
		
		Set<MicroOperator> operators = new HashSet<MicroOperator>();
		operators.add(uoperator);
		Set<SubQuery> queries = new HashSet<SubQuery>();
		SubQuery query = new SubQuery (0, operators, schema, window, queryConf);
		query.setAggregateOperator((IAggregateOperator) aggregationCode);
		queries.add(query);
		MultiOperator operator = new MultiOperator(queries, 0);
		operator.setup();
		
		TheCPU.getInstance().bind(0);
		
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
							
							if (! buffer.hasRemaining()) {
								/*
								 * System.out.println(String.format("[DBG] %6d bytes received; %6d bytes remain", 
								 *		bytes, buffer.remaining()));
								 *
								 * Make sure the buffer is rewind before reading.
								 */
								buffer.rewind();
								operator.processData (buffer.array(), buffer.capacity());
							
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
