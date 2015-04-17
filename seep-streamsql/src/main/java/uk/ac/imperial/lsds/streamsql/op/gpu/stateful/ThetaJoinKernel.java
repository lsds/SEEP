package uk.ac.imperial.lsds.streamsql.op.gpu.stateful;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;
import uk.ac.imperial.lsds.seep.multi.TheGPU;
import uk.ac.imperial.lsds.seep.multi.UnboundedQueryBufferFactory;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition;
import uk.ac.imperial.lsds.streamsql.expressions.ExpressionsUtil;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.op.gpu.KernelCodeGenerator;
import uk.ac.imperial.lsds.streamsql.predicates.IPredicate;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;

public class ThetaJoinKernel implements IStreamSQLOperator, IMicroOperatorCode {
	
	private static final int threadsPerGroup = 256;
	private static final int tuplesPerThread = 2;
	
	private static final int pipelines = 2;
	private int [] taskIdx; /* Control output based on depth of pipeline */
	private int [] freeIdx;
	
	private IPredicate predicate;

	private ITupleSchema leftInputSchema, rightInputSchema;
	private ITupleSchema outputSchema = null;
	
	private static String filename = "/home/akolious/seep/seep-system/clib/templates/ThetaJoin.cl";
	
	private int leftInputSize = -1, rightInputSize = -1, outputSize;
	
	private int leftTuples, rightTuples;
	private int leftTuples_;
	
	private int qid;
	
	private int [] args;
	
	private int [] threads;
	private int [] tgs;
	
	private int ngroups;
	private int product;
	
	private int outputTupleSize;
	
	private int localInputSize;
	
	byte [] flags, offsets, partitions;
	
	byte [] startPtrs, endPtrs;
	
	private void printWindowPointers (byte [] startPtrs, byte [] endPtrs) {
		
		ByteBuffer b = ByteBuffer.wrap(startPtrs).order(ByteOrder.LITTLE_ENDIAN);
		ByteBuffer d = ByteBuffer.wrap(  endPtrs).order(ByteOrder.LITTLE_ENDIAN);
		int wid = 0;
		while (b.hasRemaining() && d.hasRemaining()) {
			System.out.println(String.format("w %02d: starts %10d ends %10d", 
				wid, b.getInt(), d.getInt()));
				wid ++;
		}
	}
	
	private boolean isPowerOfTwo (int n) {
		if (n == 0)
			return false;
		while (n != 1) {
			if (n % 2 != 0)
				return false;
			n = n / 2;
		}
		return true;
	}
	
	public ThetaJoinKernel (IPredicate predicate, ITupleSchema leftInputSchema, ITupleSchema rightInputSchema) {
		this.predicate = predicate;
		
		this.leftInputSchema = leftInputSchema;
		this.rightInputSchema = rightInputSchema;
		
		outputSchema = ExpressionsUtil.mergeTupleSchemas(this.leftInputSchema, this.rightInputSchema);
		
		/* Task pipelining internal state */
		
		taskIdx = new int [pipelines];
		freeIdx = new int [pipelines];
		for (int i = 0; i < pipelines; i++) {
			taskIdx[i] = -1;
			freeIdx[i] = -1;
		}
	}
	
	public void setLeftInputSize (int leftInputSize) {
		this.leftInputSize = leftInputSize;
	}
	
	public void setRightInputSize (int rightInputSize) {
		this.rightInputSize = rightInputSize;
	}
	
	public void setup () {
		
		if (leftInputSize < 0 || rightInputSize < 0) {
			System.err.println("error: invalid input size");
			System.exit(1);
		}
		this.leftTuples  =  leftInputSize /  leftInputSchema.getByteSizeOfTuple();
		this.rightTuples = rightInputSize / rightInputSchema.getByteSizeOfTuple();
		
		System.out.println(String.format("[DBG] %10d  left tuples",   this.leftTuples));
		System.out.println(String.format("[DBG] %10d right tuples", this.rightTuples));
		
		startPtrs = new byte [leftTuples * 4];
		  endPtrs = new byte [leftTuples * 4];
		
		/* Determine #threads */
		tgs = new int [3];
		tgs[0] = threadsPerGroup;
		/* For scan and compact kernels */
		tgs[1] = threadsPerGroup;
		tgs[2] = threadsPerGroup;
		
		leftTuples_ = leftTuples;
		while (! isPowerOfTwo(leftTuples_))
			leftTuples_++;
		
		threads = new int [3];
		
		threads[0] = leftTuples_;
		
		product = leftTuples * rightTuples;
		System.out.println(String.format("[DBG] product is %10d (~2)", product));
		while (! isPowerOfTwo(product))
			product++;
		System.out.println(String.format("[DBG] product is %10d (^2)", product));
		
		/* For scan and compact kernels */
		threads[1] = product / tuplesPerThread;
		threads[2] = product / tuplesPerThread;
		
		System.out.println(String.format("[DBG] %10d threads[0]", threads[0]));
		System.out.println(String.format("[DBG] %10d threads[1]", threads[1]));
		System.out.println(String.format("[DBG] %10d threads[2]", threads[2]));
		
		ngroups = threads[1] / tgs[1];
		System.out.println(String.format("[DBG] %10d groups", ngroups));
		
		outputTupleSize = 
			leftInputSchema.getByteSizeOfTuple() + rightInputSchema.getByteSizeOfTuple();
		if (! isPowerOfTwo(outputTupleSize))
			outputTupleSize++; 
		
		outputSize = outputTupleSize * product;
		
		/* Intermediate state */
		
		flags      = new byte [4 * product];
		offsets    = new byte [4 * product];
		partitions = new byte [4 * ngroups];
		
		String source = KernelCodeGenerator.load(filename);
		
		qid = TheGPU.getInstance().getQuery(source, 3, 4, 4);
		
		TheGPU.getInstance().setInput(qid, 0,  leftInputSize);
		TheGPU.getInstance().setInput(qid, 1, rightInputSize);
		/* Start and end pointers */
		TheGPU.getInstance().setInput(qid, 2, startPtrs.length);
		TheGPU.getInstance().setInput(qid, 3,   endPtrs.length);
		
		System.out.println(String.format("[DBG] %10d bytes",      flags.length));
		System.out.println(String.format("[DBG] %10d bytes",    offsets.length));
		System.out.println(String.format("[DBG] %10d bytes", partitions.length));
		System.out.println(String.format("[DBG] %10d bytes",        outputSize));
		
		TheGPU.getInstance().setOutput(qid, 0,      flags.length, 0, 0, 1, 0);
		TheGPU.getInstance().setOutput(qid, 1,    offsets.length, 0, 0, 0, 0);
		TheGPU.getInstance().setOutput(qid, 2, partitions.length, 0, 0, 0, 0);
		TheGPU.getInstance().setOutput(qid, 3,        outputSize, 1, 0, 0, 1);
		
		localInputSize = 4 * tgs[1] * tuplesPerThread;
		
		args = new int [3];
		args[0] = leftTuples;
		args[1] = rightTuples; 
		args[2] = localInputSize;
		
		TheGPU.getInstance().setKernelThetaJoin(qid, args);
	}

	@Override
	public void processData(WindowBatch windowBatch, IWindowAPI api) {
		throw new UnsupportedOperationException("ThetaJoinKernel requires two input streams");
	}

	@Override
	public void processData(WindowBatch firstWindowBatch,
			WindowBatch secondWindowBatch, IWindowAPI api) {
		
		int currentTaskIdx = firstWindowBatch.getTaskId();
		int currentFreeIdx = firstWindowBatch.getFreeOffset();
		
		__computePointers (firstWindowBatch, secondWindowBatch);
		
		byte [] firstInputArray = firstWindowBatch.getBuffer().array();
		int firstStart = firstWindowBatch.getBatchStartPointer();
		int firstEnd   = firstWindowBatch.getBatchEndPointer();
		
		byte [] secondInputArray = secondWindowBatch.getBuffer().array();
		int secondStart = secondWindowBatch.getBatchStartPointer();
		int secondEnd   = secondWindowBatch.getBatchEndPointer();
		
		TheGPU.getInstance().setInputBuffer(qid, 0,  firstInputArray,  firstStart,  firstEnd);
		TheGPU.getInstance().setInputBuffer(qid, 1, secondInputArray, secondStart, secondEnd);
		
		TheGPU.getInstance().setInputBuffer(qid, 2, startPtrs);
		TheGPU.getInstance().setInputBuffer(qid, 3,   endPtrs);
		
		TheGPU.getInstance().setOutputBuffer(qid, 0,      flags);
		TheGPU.getInstance().setOutputBuffer(qid, 1,    offsets);
		TheGPU.getInstance().setOutputBuffer(qid, 2, partitions);
		
		IQueryBuffer outputBuffer = UnboundedQueryBufferFactory.newInstance();
		TheGPU.getInstance().setOutputBuffer(qid, 3, outputBuffer.array());
		
		TheGPU.getInstance().execute(qid, threads, tgs);
		
		firstWindowBatch.setBuffer(outputBuffer);
		firstWindowBatch.setSchema(outputSchema);
		
		firstWindowBatch.setTaskId     (taskIdx[0]);
		firstWindowBatch.setFreeOffset (freeIdx[0]);
		
		for (int i = 0; i < taskIdx.length - 1; i++) {
			taskIdx[i] = taskIdx [i + 1];
			freeIdx[i] = freeIdx [i + 1];
		}
		taskIdx [taskIdx.length - 1] = currentTaskIdx;
		freeIdx [freeIdx.length - 1] = currentFreeIdx;
		
		api.outputWindowBatchResult(-1, firstWindowBatch);
	}

	@Override
	public void accept(OperatorVisitor ov) {
		ov.visit(this);
	}
	
	private void __computePointers(WindowBatch batch1, WindowBatch batch2) {
		
		int currentIndex1 = batch1.getBatchStartPointer();
		int currentIndex2 = batch2.getBatchStartPointer();

		int endIndex1 = batch1.getBatchEndPointer();
		int endIndex2 = batch2.getBatchEndPointer();
		
		int currentWindowStart1 = currentIndex1;
		int currentWindowStart2 = currentIndex2;
		
		int currentWindowEnd1 = currentIndex1;
		int currentWindowEnd2 = currentIndex2;

		ITupleSchema schema1 = batch1.getSchema();
		ITupleSchema schema2 = batch2.getSchema();

		int tupleSize1 = schema1.getByteSizeOfTuple();
		int tupleSize2 = schema2.getByteSizeOfTuple();

		WindowDefinition windowDef1 = batch1.getWindowDefinition();
		WindowDefinition windowDef2 = batch2.getWindowDefinition();

		long currentIndexTime1;
		long startTime1;
		
		long currentIndexTime2;
		long startTime2;
		
		int ntuples1 = (endIndex1 - currentIndex1) / tupleSize1;
		
		int [] __startPointers = new int[ntuples1];
		int []   __endPointers = new int[ntuples1];
		
		/*
		 * Is one of the windows empty?
		 */
		if (currentIndex1 == endIndex1 || currentIndex2 == endIndex2) {
			System.err.println("warning: empty window");
			return;
		}
		
		int __firstTupleIndex = 0;
		
		while (currentIndex1 < endIndex1 || currentIndex2 < endIndex2) {
	
			/*
			 * Get timestamps of currently processed tuples in either batch
			 */
			currentIndexTime1 = batch1.getLong(currentIndex1, 0);
			currentIndexTime2 = batch2.getLong(currentIndex2, 0);
	
			/*
			 * Move in first batch?
			 */
			if (currentIndexTime1 < currentIndexTime2 
					|| (currentIndexTime1 == currentIndexTime2 && currentIndex2 >= endIndex2)) {
					
				__startPointers[__firstTupleIndex] = currentWindowStart2;
				  __endPointers[__firstTupleIndex] = currentWindowEnd2;
				  
				__firstTupleIndex++;
					
				
				/* Add current tuple to window over first batch */
				currentWindowEnd1 = currentIndex1;
	
				/* Remove old tuples in window over first batch */
				if (windowDef1.isRowBased()) {
					
					if ((currentWindowEnd1 - currentWindowStart1)/tupleSize1 > windowDef1.getSize()) 
						currentWindowStart1 += windowDef1.getSlide() * tupleSize1;
					
				} else 
				if (windowDef1.isRangeBased()) {
					
					startTime1 = batch1.getLong(currentWindowStart1, 0);
					
					while (startTime1 < currentIndexTime1 - windowDef1.getSize()) {
						
						currentWindowStart1 += tupleSize1;
						startTime1 = batch1.getLong(currentWindowStart1, 0);
					}
				}
				
				/* Remove old tuples in window over second batch (only for range windows) */
				if (windowDef2.isRangeBased()) {
					
					startTime2 = batch2.getLong(currentWindowStart2, 0);
					
					while (startTime2 < currentIndexTime1 - windowDef2.getSize()) {
						
						currentWindowStart2 += tupleSize2;
						startTime2 = batch2.getLong(currentWindowStart2, 0);
					}
				}
					
				/* Do the actual move in first window batch */
				currentIndex1 += tupleSize1;
				
			} else { /* Move in second batch! */
				
				for (int i = currentWindowStart1; i <= (currentWindowEnd1-tupleSize1); i += tupleSize1) {
					int __tmpIndex = (i - batch1.getBatchStartPointer()) / tupleSize1;
					__endPointers[__tmpIndex] = currentIndex2;
				}
				
				/* Add current tuple to window over second batch */
				currentWindowEnd2 = currentIndex2;
				
				/* Remove old tuples in window over second batch */
				if (windowDef2.isRowBased()) {
					
					if ((currentWindowEnd2 - currentWindowStart2)/tupleSize2 > windowDef2.getSize()) 
						currentWindowStart2 += windowDef2.getSlide() * tupleSize2;
					
				} else 
				if (windowDef2.isRangeBased()) {
					
					startTime2 = batch2.getLong(currentWindowStart2, 0);
					
					while (startTime2 < currentIndexTime2 - windowDef2.getSize()) {
						
						currentWindowStart2 += tupleSize2;
						startTime2 = batch2.getLong(currentWindowStart2, 0);
					}
				}
				
				/* Remove old tuples in window over first batch (only for range windows) */
				if (windowDef1.isRangeBased()) {
					
					startTime1 = batch1.getLong(currentWindowStart1, 0);
					
					while (startTime1 < currentIndexTime2 - windowDef1.getSize()) {
						
						currentWindowStart1 += tupleSize1;
						startTime1 = batch1.getLong(currentWindowStart1, 0);
					}
				}
					
				/* Do the actual move in second window batch */
				currentIndex2 += tupleSize2;
			}
		}
		
		/* Print start and end pointers */
		for (int i = 0; i < ntuples1; i++) {
			System.out.println(String.format("1st batch tuple %6d 2nd batch window [%10d, %10d]", 
					i, __startPointers[i], __endPointers[i]));
		}
	}
}
