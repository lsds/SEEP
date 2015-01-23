package uk.ac.imperial.lsds.streamsql.op.gpu.stateless;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;
import uk.ac.imperial.lsds.seep.multi.UnboundedQueryBufferFactory;
import uk.ac.imperial.lsds.seep.multi.Utils;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.op.gpu.TheGPU;
import uk.ac.imperial.lsds.streamsql.op.gpu.KernelCodeGenerator;
import uk.ac.imperial.lsds.streamsql.predicates.IPredicate;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;

import com.amd.aparapi.Range;

public class ASelectionKernel implements IStreamSQLOperator, IMicroOperatorCode {
	
	private IPredicate predicate;
	private ITupleSchema schema;
	
	private String filename = null;
	
	/*
	 * This size must be greater or equal to the size of the byte array backing
	 * an input window batch.
	 */
	private static final int _default_size = Utils._GPU_INPUT_;
	/*
	 * Operator configuration parameters
	 */
	private static final int THREADS_PER_GROUP = 256;
	private static final int TUPLES_PER_THREAD = 1;
	
	private static final int SIZEOF_INT = 4;
	
	private static final int PIPELINES = 2;
	
	int qid;
	
	private int [] args;
	
	private int [] taskIdx;
	private int [] freeIdx;
	
	private int tuples;
	private int threads, groups;
	private int bundles, _bundle;
	
	public ASelectionKernel (IPredicate predicate, ITupleSchema schema,
		String filename) {
		
		this.predicate = predicate;
		this.schema = schema;
		this.filename = filename;
		
		setup ();
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
	
	@SuppressWarnings("unchecked")
	private void setup () {
		
		/* Configure kernel arguments */
		this.tuples  = _default_size / schema.getByteSizeOfTuple();
		this.bundles = TUPLES_PER_THREAD;
		this._bundle = THREADS_PER_GROUP * TUPLES_PER_THREAD;
		this.threads = tuples / TUPLES_PER_THREAD;
		this.groups  = threads/ THREADS_PER_GROUP;
		
		System.out.println(String.format("[DBG] %6d threads", threads));
		System.out.println(String.format("[DBG] %6d threads/group", THREADS_PER_GROUP));
		System.out.println(String.format("[DBG] %6d groups", groups));
		System.out.println(String.format("[DBG] %6d tuples/group (bundle size)", _bundle));
		System.out.println(String.format("[DBG] %6d bundles", bundles));
		
		args = new int[5];
		args[0] = _default_size;
		args[1] = tuples; /* #bytes */
		args[2] = _bundle;
		args[3] = bundles;
		args[4] = SIZEOF_INT * THREADS_PER_GROUP;
		
		String source = KernelCodeGenerator.getSelection(schema, schema, filename);
		
		TheGPU.getInstance().init(1);
		qid = TheGPU.getInstance().getQuery(source, 2, 1, 4);
		TheGPU.getInstance().setInput (qid, 0, _default_size);
		/* These are read-write buffers */
		TheGPU.getInstance().setOutput(qid, 0, _default_size, 0);
		TheGPU.getInstance().setOutput(qid, 1, _default_size, 0);
		TheGPU.getInstance().setOutput(qid, 2, _default_size, 0);
		TheGPU.getInstance().setOutput(qid, 3, _default_size, 1);
		TheGPU.getInstance().setKernelSelect (qid, args);
		
		taskIdx = new int [PIPELINES];
		freeIdx = new int [PIPELINES];
		for (int i = 0; i < PIPELINES; i++) {
			taskIdx[i] = -1;
			freeIdx[i] = -1;
		}
	}
	
	@Override
	public void processData (WindowBatch windowBatch, IWindowAPI api) {
		
		int currentTaskIdx = windowBatch.getTaskId();
		int currentFreeIdx = windowBatch.getFreeOffset();
		
		/* Set input and output */
		byte [] inputArray = windowBatch.getBuffer().array();
		int start = windowBatch.getBatchStartPointer();
		int end   = windowBatch.getBatchEndPointer();
		TheGPU.getInstance().setInputBuffer(qid, 0, inputArray, start, end);
		IQueryBuffer outputBuffer = UnboundedQueryBufferFactory.newInstance();
		TheGPU.getInstance().setOutputBuffer(qid, 0, outputBuffer.array());
		TheGPU.getInstance().execute(qid, threads, THREADS_PER_GROUP);
		
		windowBatch.setBuffer(outputBuffer);
		
		/* windowBatch.setTaskId     (taskIdx[0]);
		windowBatch.setFreeOffset (freeIdx[0]); */
		
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
	public void processData(WindowBatch firstWindowBatch,
			WindowBatch secondWindowBatch, IWindowAPI api) {
		throw new UnsupportedOperationException("SelectionKernel is single input operator and does not operate on two streams");
	}
}
