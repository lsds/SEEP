package uk.ac.imperial.lsds.streamsql.op.gpu.stateless;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;
import uk.ac.imperial.lsds.seep.multi.TheGPU;
import uk.ac.imperial.lsds.seep.multi.UnboundedQueryBufferFactory;
import uk.ac.imperial.lsds.seep.multi.Utils;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.op.gpu.KernelCodeGenerator;
import uk.ac.imperial.lsds.streamsql.predicates.IPredicate;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;

public class ASelectionKernel implements IStreamSQLOperator, IMicroOperatorCode {
	
	private IPredicate predicate;
	private ITupleSchema schema;
	
	private String filename = null;
	
	/*
	 * This size must be greater or equal to the size of the byte array backing
	 * an input window batch.
	 */
	private static int _default_size = Utils._GPU_INPUT_;
	/*
	 * Operator configuration parameters
	 */
	private static final int THREADS_PER_GROUP = 256;
	private static final int TUPLES_PER_THREAD = 2;
	
	private static final int _INT_ = 4; /* sizeof(int) */
	
	private static final int PIPELINES = 1;
	
	int qid; /* Query id */
	
	private boolean pinned = false;
	
	private int [] args; /* Arguments to the selection kernel */
	
	private int [] taskIdx; /* Control output based on depth of pipeline */
	private int [] freeIdx;
	
	private int tuples;
	private int threads,  groups;
	private int bundles, _bundle;
	
	private int _input_size;
	
	/* Temporary storage */
	byte [] flags;
	byte [] offsets;
	byte [] groupOffsets;
	
	public ASelectionKernel (IPredicate predicate, ITupleSchema schema,
		String filename) {
		
		this.predicate = predicate;
		this.schema = schema;
		this.filename = filename;
		
		this._input_size = _default_size;
		
		// setup ();
	}
	
	public ASelectionKernel (IPredicate predicate, ITupleSchema schema) {
		this(predicate, schema, null);
	}
	
	public ASelectionKernel (IPredicate predicate) {
		this(predicate, null, null);
	}
	
	public IPredicate getPredicate () {
		return this.predicate;
	}
	
	public void setInputSize (int inputSize) {
		this._input_size = inputSize;
	}
	
	public void setup () {
		
		/* Configure kernel arguments */
		this.tuples  = this._input_size / schema.getByteSizeOfTuple();
		
		/* Static configuration of the GPU work groups */
		this.bundles = TUPLES_PER_THREAD;
		this._bundle = THREADS_PER_GROUP * TUPLES_PER_THREAD;
		
		this.threads = tuples  / TUPLES_PER_THREAD;
		this.groups  = threads / THREADS_PER_GROUP;
		
		/* For debugging purposes */
		System.out.println(String.format("[DBG] %6d tuples", this.tuples));
		System.out.println(String.format("[DBG] %6d threads", this.threads));
		System.out.println(String.format("[DBG] %6d threads/group", THREADS_PER_GROUP));
		System.out.println(String.format("[DBG] %6d groups", this.groups));
		System.out.println(String.format("[DBG] %6d tuples/group (bundle size)", this._bundle));
		System.out.println(String.format("[DBG] %6d bundles", this.bundles));
		
		args = new int[5];
		args[0] = _input_size; /* #bytes */
		args[1] = tuples; 
		args[2] = _bundle;
		args[3] = bundles;
		args[4] = 2 * _INT_ * THREADS_PER_GROUP;
		
		String source = KernelCodeGenerator.getSelection (schema, schema, predicate, filename);
		
		System.out.println(source);
		
		// TheGPU.getInstance().init(1);
		
		qid = TheGPU.getInstance().getQuery(source, 2, 1, 4);
		
		System.out.println(String.format("[DBG] GPU selection qid %d", qid));
		
		TheGPU.getInstance().setInput (qid, 0, _input_size);
		/* These are read-write buffers */
		 TheGPU.getInstance().setOutput(qid, 0, _INT_ * tuples, 0);
		 TheGPU.getInstance().setOutput(qid, 1, _INT_ * tuples, 0);
		 TheGPU.getInstance().setOutput(qid, 2, _INT_ * groups, 0);
		/* Output is write-only */
		TheGPU.getInstance().setOutput(qid, 3, _input_size, 1);
		
		TheGPU.getInstance().setKernelSelect (qid, args);
		
		taskIdx = new int [PIPELINES];
		freeIdx = new int [PIPELINES];
		for (int i = 0; i < PIPELINES; i++) {
			taskIdx[i] = -1;
			freeIdx[i] = -1;
		}
		
		flags   = new byte [_INT_ * tuples];
		offsets = new byte [_INT_ * tuples];
		
		groupOffsets = new byte [_INT_ * groups];
		
		Arrays.fill(flags  , (byte) 0);
		Arrays.fill(offsets, (byte) 0);
		
		Arrays.fill(groupOffsets, (byte) 0);
		
		
	}
	
	@Override
	public void processData (WindowBatch windowBatch, IWindowAPI api) {
		
		if (! pinned) {
			TheGPU.getInstance().bind(1);
			pinned = true;
		}
		
		int currentTaskIdx = windowBatch.getTaskId();
		int currentFreeIdx = windowBatch.getFreeOffset();
		
		// System.out.println(String.format("[DBG] GPU selection executes %d", currentTaskIdx));
		
		/* Set input and output */
		byte [] inputArray = windowBatch.getBuffer().array();
		
		int start = windowBatch.getBatchStartPointer();
		int end   = windowBatch.getBatchEndPointer();
		
		// System.out.println(String.format("[DBG] GPU selection start %10d end %10d", start, end));
		
		TheGPU.getInstance().setInputBuffer(qid, 0, inputArray, start, end);
		
		IQueryBuffer outputBuffer = UnboundedQueryBufferFactory.newInstance();
		
		 TheGPU.getInstance().setOutputBuffer(qid, 0, flags);
		 TheGPU.getInstance().setOutputBuffer(qid, 1, offsets);
		 TheGPU.getInstance().setOutputBuffer(qid, 2, groupOffsets);
		
		TheGPU.getInstance().setOutputBuffer(qid, 3, outputBuffer.array());
		
		TheGPU.getInstance().execute(qid, threads, THREADS_PER_GROUP);
		
///*//		/* Print flags and offsets */
//		ByteBuffer b1 = ByteBuffer.allocate(flags.length).order(ByteOrder.LITTLE_ENDIAN);
//		b1.put(flags);
////		ByteBuffer b2 = ByteBuffer.wrap(offsets).order(ByteOrder.LITTLE_ENDIAN);
//		b1.clear();
////		b2.clear();
//		int count = 0;
//		while (b1.hasRemaining() && count < 256) {
//			System.out.println(String.format("[DBG] [select] flag %13d", b1.getInt()));
//			count ++;
//		}
//		System.out.println(String.format("[DBG] [select] %d entries", count));*/
		
//		outputBuffer.position(_INT_ * tuples);
		
		outputBuffer.putLong(0, windowBatch.getBuffer().getLong(windowBatch.getBatchStartPointer()));
		
		windowBatch.setBuffer(outputBuffer);
		
		windowBatch.setTaskId     (taskIdx[0]);
		windowBatch.setFreeOffset (freeIdx[0]);
		
		for (int i = 0; i < taskIdx.length - 1; i++) {
			taskIdx[i] = taskIdx [i + 1];
			freeIdx[i] = freeIdx [i + 1];
		}
		taskIdx [taskIdx.length - 1] = currentTaskIdx;
		freeIdx [freeIdx.length - 1] = currentFreeIdx;
		
		api.outputWindowBatchResult(-1, windowBatch);
	}

	@Override
	public void accept (OperatorVisitor visitor) {
		visitor.visit(this);
	}
	
	@Override
	public String toString () {
		final StringBuilder sb = new StringBuilder();
		sb.append("Selection (");
		sb.append(predicate.toString());
		sb.append(")");
		return sb.toString();
	}
	
	@Override
	public void processData(WindowBatch first, WindowBatch second, IWindowAPI api) {
		
		throw new UnsupportedOperationException("SelectionKernel operates on a single stream only");
	}
}
