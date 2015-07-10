package uk.ac.imperial.lsds.streamsql.op.gpu.stateful;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import uk.ac.imperial.lsds.seep.multi.AggregationType;
import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.PartialWindowResults;
import uk.ac.imperial.lsds.seep.multi.PartialWindowResultsFactory;
import uk.ac.imperial.lsds.seep.multi.TheGPU;
import uk.ac.imperial.lsds.seep.multi.ThreadMap;
import uk.ac.imperial.lsds.seep.multi.UnboundedQueryBufferFactory;
import uk.ac.imperial.lsds.seep.multi.Utils;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition;
import uk.ac.imperial.lsds.streamsql.expressions.Expression;
import uk.ac.imperial.lsds.streamsql.expressions.ExpressionsUtil;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatExpression;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntExpression;
import uk.ac.imperial.lsds.streamsql.expressions.elong.LongColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.elong.LongExpression;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;
import uk.ac.imperial.lsds.streamsql.op.gpu.KernelCodeGenerator;

public class PartialAggregationKernel implements IStreamSQLOperator, IMicroOperatorCode {
	
	private static final int threadsPerGroup = 256;
	
	private static int kdbg = 0;
	private static boolean debug = false;
	
	private static int pipelines = Utils.PIPELINE_DEPTH;
	private int [] taskIdx;
	private int [] freeIdx;
	private int [] markIdx; /* Latency mark */
	
	private AggregationType type;
	private FloatColumnReference _the_aggregate;
	
	private Expression [] groupBy;
	
	private String selectionClause = null;
	
	private LongColumnReference timestampReference;
	
	private WindowDefinition windowDefinition = null;
	
	private ITupleSchema inputSchema, outputSchema;
	
	private static String filename = Utils.SEEP_HOME + 
			"/seep-system/clib/templates/PartialAggregation.cl";
	
	private int qid;
	
	private int  []  intArgs;
	private long [] longArgs;
	
	private int tuples;
	
	private int [] threads;
	private int [] tgs;
	
	private int batchSize = -1;

	/* Global and local memory sizes */
	private int inputSize = -1, outputSize;
	private int windowPtrsSize;
	
	private int tableSize; /* The size of a hash table (1 per window) in bytes */
	
	/* Read/write state that may be moved or not */
	private byte [] startPtrs;
	private byte [] endPtrs;
	
	private byte [] failed, attempts;
	private byte [] offset, windowCounts;
	
	private int intermediateTupleSize, outputTupleSize;
	private int keyLength;
	
	private int failedLength, attemptsLength, offsetLength, windowCntLength;
	
	int clearThreads;
	
	@SuppressWarnings("unused")
	private static boolean isPowerOfTwo (int n) {
		if (n == 0)
			return false;
		while (n != 1) {
			if (n % 2 != 0)
				return false;
			n = n/2;
		}
		return true;
	}
	
	@SuppressWarnings("unused")
	private void printWindowPointers (byte [] startPtrs, byte [] endPtrs, int count) {
		
		ByteBuffer b = ByteBuffer.wrap(startPtrs).order(ByteOrder.LITTLE_ENDIAN);
		ByteBuffer d = ByteBuffer.wrap(  endPtrs).order(ByteOrder.LITTLE_ENDIAN);
		int wid = 0;
		while (b.hasRemaining() && d.hasRemaining() && wid < count) {
			System.out.println(String.format("w %02d: starts %10d ends %10d", 
				wid, b.getInt(), d.getInt()));
				wid ++;
		}
	}
	
	public PartialAggregationKernel (
			AggregationType type, 
			FloatColumnReference _the_aggregate,
			Expression [] groupBy,
			String selectionClause,
			ITupleSchema inputSchema) {
		
		this.type = type;
		this._the_aggregate = _the_aggregate;
		
		this.groupBy = groupBy;
		
		this.selectionClause = selectionClause;
		
		this.inputSchema = inputSchema;
		
		/* Create output schema */
		this.timestampReference = new LongColumnReference(0);
		
		Expression [] outputAttributes = new Expression [this.groupBy.length + 2];
		/* First attribute is the time stamp */
		outputAttributes[0] = this.timestampReference;
		
		/* Followed by the group-by (composite) key */
		keyLength = 0;
		
		for (int i = 1; i <= this.groupBy.length; i++) {
			
			Expression e = this.groupBy[i - 1];
			
			if (e instanceof   IntExpression) { outputAttributes[i] = new   IntColumnReference(i); keyLength += 4;
			} else 
			if (e instanceof  LongExpression) { outputAttributes[i] = new  LongColumnReference(i); keyLength += 8;
			} else 
			if (e instanceof FloatExpression) { outputAttributes[i] = new FloatColumnReference(i); keyLength += 4;
			} else {
				throw new IllegalArgumentException("error: unknown group by expression type");
			}
		}
		/* Last attribute is the aggregate */
		outputAttributes[this.groupBy.length + 1] = new FloatColumnReference(this.groupBy.length + 1);
		
		this.outputSchema = ExpressionsUtil.getTupleSchemaForExpressions(outputAttributes);
		
		this.outputTupleSize = outputSchema.getByteSizeOfTuple();
		System.out.println(String.format("[DBG] output tuple size is %d bytes", this.outputTupleSize));
		
		this.intermediateTupleSize = KernelCodeGenerator.getPartialIntermediateStructLength(groupBy);
		System.out.println(String.format("[DBG] intermediate tuple size is %d bytes", this.intermediateTupleSize));
		
		taskIdx = new int [pipelines];
		freeIdx = new int [pipelines];
		markIdx = new int [pipelines];
		for (int i = 0; i < pipelines; i++) {
			taskIdx[i] = -1;
			freeIdx[i] = -1;
			markIdx[i] = -1;
		}
	}
	
	public void setBatchSize (int batchSize) {
		this.batchSize = batchSize;
	}
	
	public void setWindowDefinition (WindowDefinition windowDefinition) {
		this.windowDefinition = windowDefinition;
	}
	
	public void setup () {
		
		if (batchSize < 0) {
			System.err.println("error: invalid batch size");
			System.exit(1);
		}
		if (windowDefinition == null) {
			System.err.println("error: unspecified window definition");
			System.exit(1);
		}
		
		inputSize = batchSize;
		tuples = inputSize / inputSchema.getByteSizeOfTuple();
		
		int maxWindows = 8192;
		
		windowPtrsSize = maxWindows * 4; /* `tuples` windows x sizeof(int) */
		
		tableSize  = Utils.HASH_TABLE_SIZE;
		
		outputSize = Utils._UNBOUNDED_BUFFER_;
		
		int tuplesPerTable  =  tableSize / intermediateTupleSize;
		int tablesPerOutput = outputSize / tableSize;
		
		System.out.println(String.format("[DBG] %10d tuples/table",   tuplesPerTable));
		System.out.println(String.format("[DBG] %10d tables/output", tablesPerOutput));
		
		clearThreads = outputSize / intermediateTupleSize;
		
		/* Intermediate state */
		failedLength    =  4 * tuples; /* x sizeof(int) */
		attemptsLength  =  4 * tuples; /* x sizeof(int) */
		offsetLength    = 16;          /* 2 longs */
		windowCntLength = 16 + 4;      /* 4 integers, +1 that is the mark */
		
		System.out.println(String.format("[DBG]         input.length = %13d",       inputSize));
		System.out.println(String.format("[DBG] startPointers.length = %13d",  windowPtrsSize));
		System.out.println(String.format("[DBG]   endPointers.length = %13d",  windowPtrsSize));
		System.out.println(String.format("[DBG]        failed.length = %13d",    failedLength));
		System.out.println(String.format("[DBG]      attempts.length = %13d",  attemptsLength));
		System.out.println(String.format("[DBG]        offset.length = %13d",    offsetLength));
		System.out.println(String.format("[DBG]  windowCounts.length = %13d", windowCntLength));
		System.out.println(String.format("[DBG]   (4x) output.length = %13d",      outputSize));
		
		/* Determine #threads */
		tgs = new int [5];
		tgs[0] = threadsPerGroup; /* This is a constant; it must be a power of 2 */
		tgs[1] = threadsPerGroup;
		tgs[2] = threadsPerGroup;
		tgs[3] = threadsPerGroup;
		tgs[4] = threadsPerGroup;
		
		threads = new int [5];
		threads[0] = clearThreads;
		threads[1] = tuples;
		threads[2] = tuples;
		threads[3] = tuples;
		threads[4] = clearThreads;
		
		if (kdbg > 0) {
			startPtrs    = new byte [ windowPtrsSize];
			endPtrs      = new byte [ windowPtrsSize];
			failed       = new byte [   failedLength];
			attempts     = new byte [ attemptsLength];
			offset       = new byte [   offsetLength];
		}
		
		/* This state is necessary to parse the output of a query task */
		windowCounts = new byte [windowCntLength];
		
		String source = 
			KernelCodeGenerator.getPartialAggregation(
				inputSchema, 
				outputSchema, 
				filename, 
				type, 
				_the_aggregate, 
				groupBy, 
				selectionClause,
				windowDefinition);
		
		System.out.println(source);
		
		qid = TheGPU.getInstance().getQuery(source, 5, 1, 7);
		System.out.println("[DBG] GPU query id is " + qid);
		
		TheGPU.getInstance().setInput(qid, 0, inputSize);
		
		/* Configure the output */
		TheGPU.getInstance().setOutput(qid, 0,   windowPtrsSize, 0, 1, 0, 0); /* Start and end pointers */
		TheGPU.getInstance().setOutput(qid, 1,   windowPtrsSize, 0, 1, 0, 0);

		TheGPU.getInstance().setOutput(qid, 2,     failedLength, 0, 1, 0, 0);
		TheGPU.getInstance().setOutput(qid, 3,   attemptsLength, 0, 1, 0, 0);

		TheGPU.getInstance().setOutput(qid, 4,     offsetLength, 0, 1, 0, 0);
		TheGPU.getInstance().setOutput(qid, 5,  windowCntLength, 0, 0, 1, 0); /* Bears mark for output size */

		TheGPU.getInstance().setOutput(qid, 6,       outputSize, 1, 0, 0, 1);
		
		intArgs = new int [6];
		
		intArgs[0] =     tuples;
		intArgs[1] =  inputSize;
		intArgs[2] = outputSize;
		intArgs[3] =  tableSize;
		intArgs[4] = maxWindows;
		/* Set local memory */
		intArgs[5] = tgs[0] * keyLength; 
		
		/* These arguments are set in real-time */
		longArgs = new long [2];
		longArgs[0] = 0;
		longArgs[1] = 0;
		
		TheGPU.getInstance().setKernelPartialAggregate(qid, intArgs, longArgs);
	}
	
	@Override
	public String toString () {
		final StringBuilder sb = new StringBuilder();
		sb.append(type.asString(_the_aggregate.toString()));
		return sb.toString();
	}
	
	@Override
	public void processData (WindowBatch windowBatch, IWindowAPI api) {
		
		int workerId = ThreadMap.getInstance().get(Thread.currentThread().getId());
		
		int currentTaskIdx = windowBatch.getTaskId();
		int currentFreeIdx = windowBatch.getFreeOffset();
		int currentMarkIdx = windowBatch.getLatencyMark();
		
		/* Set input */
		byte [] inputArray = windowBatch.getBuffer().array();
		int start = windowBatch.normalise(windowBatch.getBufferStartPointer());
		int end   = windowBatch.normalise(windowBatch.getBufferEndPointer());
		
		if (end > windowBatch.getBuffer().capacity()) {
			System.err.println(String.format("warning: batch end pointer (%d) is greater than its buffer size (%d)", 
				end, windowBatch.getBuffer().capacity()));
			System.exit(1);
		}
		
		TheGPU.getInstance().setInputBuffer(qid, 0, inputArray, start, end);
		
		/* Set output */
		IQueryBuffer outputBuffer = UnboundedQueryBufferFactory.newInstance();
		TheGPU.getInstance().setOutputBuffer(qid, 6, outputBuffer.array());
		
		TheGPU.getInstance().setOutputBuffer(qid, 5, windowCounts);
		
		if (kdbg > 0) {
			TheGPU.getInstance().setOutputBuffer(qid, 0,    startPtrs);
			TheGPU.getInstance().setOutputBuffer(qid, 1,      endPtrs);
			TheGPU.getInstance().setOutputBuffer(qid, 2,       failed);
			TheGPU.getInstance().setOutputBuffer(qid, 3,     attempts);
			TheGPU.getInstance().setOutputBuffer(qid, 4,       offset);
		}
		
		/*
		 * Deep dive
		 * 
		for (int i = 0; i < 10; i++) {
			System.out.println(String.format("[DBG] batch %10d tuple timestamp %20d", 
					windowBatch.getTaskId(), 
					windowBatch.getTimestamp(windowBatch.getBufferStartPointer() + (i * windowBatch.getSchema().getByteSizeOfTuple()))));
		}
		*/
		
		/* Set previous pane id */
		if (windowBatch.getWindowDefinition().isRangeBased()) {
			if (windowBatch.getBatchStartPointer() == 0) {
				longArgs[0] = -1;
			} else {
				/* Check the last tuple of the previous batch */
				longArgs[0] = 
						(
						windowBatch.getTimestamp(windowBatch.getBufferStartPointer() - inputSchema.getByteSizeOfTuple()) / 
						windowBatch.getWindowDefinition().getPaneSize()
						);
			}
		} else {
			if (windowBatch.getBatchStartPointer() == 0) {
				longArgs[0] = -1;
			} else {
				/* Check the last tuple of the previous batch */
				longArgs[0] = 
						(
						((windowBatch.getBatchStartPointer() - inputSchema.getByteSizeOfTuple()) / inputSchema.getByteSizeOfTuple()) / 
						windowBatch.getWindowDefinition().getPaneSize());
			}
		}
 		longArgs[1] = windowBatch.getBatchStartPointer();
 		
 		if (debug)
 			System.out.println(String.format("[DBG] args[0] = %d args[1] = %d", longArgs[0], longArgs[1]));
		
		TheGPU.getInstance().executePartialAggregate(qid, threads, tgs, longArgs);
		
		/* System.err.println(String.format("[ERR] executed GPU query %d; output belongs to query %d", 
		 * qid, TheGPU.getInstance().getLastQuery()));
		 */
		
		/*
		ByteBuffer b0 = ByteBuffer.wrap(failed).order(ByteOrder.LITTLE_ENDIAN);
		int eventCounter = 0;
		while (b0.hasRemaining() && eventCounter < 1024) {
			System.out.println(String.format("[DBG] idx %10d failed = %15d", eventCounter, b0.getInt()));
			eventCounter++;
		}
		*/
		
		/*
		ByteBuffer b1 = ByteBuffer.wrap(offset).order(ByteOrder.LITTLE_ENDIAN);
		if (debug)
			System.out.println(String.format("[DBG] offsets %6d/%6d", 
				b1.getLong(), b1.getLong()));
		*/
		
		/*
		printWindowPointers(startPtrs, endPtrs, 32);
		*/
		
		ByteBuffer b2 = ByteBuffer.wrap(windowCounts).order(ByteOrder.LITTLE_ENDIAN);
		int  nclosing = b2.getInt();
		int  npending = b2.getInt();
		int ncomplete = b2.getInt();
		int  nopening = b2.getInt();
		
		if (debug)
			System.out.println(String.format("[DBG] Task id %4d window counts: %6d/%6d/%6d/%6d", 
				taskIdx[0], nclosing, npending, ncomplete, nopening));
		
		/* 
		 * Set position based on the data size returned from the GPU engine
		 */
		outputBuffer.position(TheGPU.getInstance().getPosition(qid, 6));
		
		if (debug)
			System.out.println("[DBG] output buffer position is " + outputBuffer.position());
		
		outputBuffer.close();
		
		/* Split output buffer into partial results */
		PartialWindowResults closing  = PartialWindowResultsFactory.newInstance(workerId);
		PartialWindowResults pending  = PartialWindowResultsFactory.newInstance(workerId);
		PartialWindowResults complete = PartialWindowResultsFactory.newInstance(workerId);
		PartialWindowResults opening  = PartialWindowResultsFactory.newInstance(workerId);
		
		IQueryBuffer  closingOutputBuffer = UnboundedQueryBufferFactory.newInstance();
		IQueryBuffer  pendingOutputBuffer = UnboundedQueryBufferFactory.newInstance();
		IQueryBuffer completeOutputBuffer = UnboundedQueryBufferFactory.newInstance();
		IQueryBuffer  openingOutputBuffer = UnboundedQueryBufferFactory.newInstance();
		
		 closing.setBuffer( closingOutputBuffer);
		 pending.setBuffer( pendingOutputBuffer);
		complete.setBuffer(completeOutputBuffer);
		 opening.setBuffer( openingOutputBuffer);
		
		/* Copy closing  windows */
		int offset = 0;
		for (int i = 0; i < nclosing; i++) {
			// closing.increment();
			// closingOutputBuffer.getByteBuffer().put(outputBuffer.array(), offset, tableSize);
			offset += tableSize;
		}
		/* Copy pending  windows */
		if (npending  > 0) {
			// pending.increment();
			// pendingOutputBuffer.getByteBuffer().put(outputBuffer.array(), offset, tableSize);
			int skip = (npending / threadsPerGroup) - 1;
			offset += (skip * tableSize);
		}
		/* Copy complete windows */		
		for (int i = 0; i < 1/* ncomplete */; i++) {
			/*
			if (i == 0) {
				
				System.out.println("[DBG] offset is " + offset);
				
				for (int idx = offset; idx < offset + tableSize; idx += intermediateTupleSize) {
					
					byte mark = outputBuffer.getByteBuffer().get(idx);
					long timestamp = outputBuffer.getLong(idx + 8);
					
					if (mark != 1)
						System.out.println(String.format("[DBG] %7d: %7d...", idx, mark));
					else
						System.out.println(String.format("[DBG] %7d: %7d <%4d, %3d, %3d, %3d [, %3d]>...", 
							idx, mark, timestamp, 
							outputBuffer.getInt(idx + 16), 
							outputBuffer.getInt(idx + 20),
							outputBuffer.getInt(idx + 24),
							outputBuffer.getInt(idx + 28)
							));
				}
			}
			*/
			complete.increment();
			completeOutputBuffer.getByteBuffer().put(outputBuffer.array(), offset, tableSize);
			offset += tableSize;
		}
		/* Copy opening  windows */	
		for (int i = 0; i < nopening; i++) {
			// opening.increment();
			// openingOutputBuffer.getByteBuffer().put(outputBuffer.array(), offset, tableSize);
			offset += tableSize;
		}
		
		windowBatch.setTaskId     (taskIdx[0]);
		windowBatch.setFreeOffset (freeIdx[0]);
		windowBatch.setLatencyMark(markIdx[0]);
		
		windowBatch.setSchema(outputSchema);
		
		/* At the end of processing, set window batch accordingly */
		windowBatch.setClosing  ( closing);
		windowBatch.setPending  ( pending);
		windowBatch.setComplete (complete);
		windowBatch.setOpening  ( opening);
		
		outputBuffer.release();
		
		for (int i = 0; i < taskIdx.length - 1; i++) {
			taskIdx[i] = taskIdx [i + 1];
			freeIdx[i] = freeIdx [i + 1];
			markIdx[i] = markIdx [i + 1];
		}
		taskIdx [taskIdx.length - 1] = currentTaskIdx;
		freeIdx [freeIdx.length - 1] = currentFreeIdx;
		markIdx [markIdx.length - 1] = currentMarkIdx;
		
		api.outputWindowBatchResult(-1, windowBatch);
		/*
		if (windowBatch.getTaskId() == 1) {
			System.err.println("Disrupted");
			System.exit(-1);
		}
		*/
	}
	
	@Override
	public void accept(OperatorVisitor visitor) {
		visitor.visit(this);
	}
	
	@Override
	public void processData (WindowBatch firstWindowBatch, WindowBatch secondWindowBatch, IWindowAPI api) {
		throw new UnsupportedOperationException("AggregationKernel operates on a single stream only");
	}
}
