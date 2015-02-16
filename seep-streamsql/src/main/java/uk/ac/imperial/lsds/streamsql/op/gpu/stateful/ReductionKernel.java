package uk.ac.imperial.lsds.streamsql.op.gpu.stateful;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.UnboundedQueryBufferFactory;
import uk.ac.imperial.lsds.seep.multi.Utils;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;
import uk.ac.imperial.lsds.streamsql.expressions.Expression;
import uk.ac.imperial.lsds.streamsql.expressions.ExpressionsUtil;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.elong.LongColumnReference;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;
import uk.ac.imperial.lsds.streamsql.op.gpu.KernelCodeGenerator;
import uk.ac.imperial.lsds.streamsql.op.gpu.TheGPU;
import uk.ac.imperial.lsds.streamsql.op.stateful.AggregationType;

public class ReductionKernel implements IStreamSQLOperator, IMicroOperatorCode {
	
	/*
	 * This size must be greater or equal to the size of the byte array backing
	 * an input window batch.
	 */
	private static final int _default_input_size = Utils._GPU_INPUT_;
	private static final int SIZEOF_INT = 4;
	/*
	 * Operator configuration parameters
	 */
	private static final int THREADS_PER_GROUP = 256;
	
	private static final int PIPELINES = 2;
	
	private AggregationType type;
	private FloatColumnReference _the_aggregate;
	
	private LongColumnReference timestampReference;
	
	private int batchSize;
	
	private ITupleSchema inputSchema, outputSchema;
	
	private String filename = null;
	
	private int qid;
	
	private int [] args;
	
	private int tuples;
	private int threads, groups;

	/* Local memory sizes */
	private int  _input_size, _window_ptrs_size;
	private int _output_size, _local_input_size;
	
	private byte [] startPtrs;
	private byte [] endPtrs;
	
	private int [] taskIdx;
	private int [] freeIdx;
	
	public ReductionKernel (AggregationType type, FloatColumnReference _the_aggregate) {
		this (type, _the_aggregate, null);
	}
	
	public ReductionKernel (AggregationType type, FloatColumnReference _the_aggregate, ITupleSchema inputSchema) {
		this.type = type;
		this._the_aggregate = _the_aggregate;
		this.inputSchema = inputSchema;
		/* Create output schema */
		this.timestampReference = new LongColumnReference(0);
		Expression[] outputAttributes = new Expression[3];
		outputAttributes[0] = this.timestampReference;
		outputAttributes[1] = this._the_aggregate;
		outputAttributes[2] = new IntColumnReference(2);
		this.outputSchema = ExpressionsUtil.getTupleSchemaForExpressions(outputAttributes);
	}
	
	public void setSource (String filename) {
		this.filename = filename;
	}
	
	public void setInputSize (int size) {
		this._input_size = size;
	}
	
	public void setInputSchema (ITupleSchema inputSchema) {
		this.inputSchema = inputSchema;
	}
	
	public void setBatchSize (int batchSize) {
		this.batchSize = batchSize;
	}
	
	public void setup () {
		
		/* Configure kernel arguments */
		/* this._input_size = _default_input_size; */
		this.tuples = _input_size / inputSchema.getByteSizeOfTuple();
		
		/* We assign 1 group per window */
		this.groups = this.batchSize;
		this.threads = groups * THREADS_PER_GROUP;
		
		this._output_size = groups * outputSchema.getByteSizeOfTuple();
		
		this._window_ptrs_size = SIZEOF_INT * groups;
		this._local_input_size = SIZEOF_INT * THREADS_PER_GROUP;
		
		System.out.println(String.format("[DBG] %6d tuples", tuples));
		System.out.println(String.format("[DBG] %6d threads", threads));
		System.out.println(String.format("[DBG] %6d groups", groups));
		System.out.println(String.format("[DBG] %6d bytes scratch memory", _local_input_size));
		System.out.println(String.format("[DBG] %6d bytes output", _output_size));
		
		/* Arguments are: tuples, bytes, _local_input_size */
		args = new int [3];
		args[0] = tuples;
		args[1] = _input_size;
		args[2] =  _local_input_size;
		
		startPtrs = new byte [_window_ptrs_size];
		endPtrs   = new byte [_window_ptrs_size];
		ByteBuffer b = ByteBuffer.wrap(startPtrs).order(ByteOrder.LITTLE_ENDIAN);
		ByteBuffer d = ByteBuffer.wrap(  endPtrs).order(ByteOrder.LITTLE_ENDIAN);
		int tpw = tuples / groups;
		int bpw = tpw * inputSchema.getByteSizeOfTuple();
		for (int i = 0; i < groups; i++) {
			b.putInt(i * bpw);
			d.putInt(i * bpw + bpw - 1);
		}
		b.clear();
		d.clear();
		
		String source = KernelCodeGenerator.getReduction (inputSchema, outputSchema, filename, type);
		// System.out.println(source);
		
		// TheGPU.getInstance().init(1);
		qid = TheGPU.getInstance().getQuery(source, 1, 3, 1);
		System.out.println(String.format("[DBG] GPU reduction qid %d", qid));
		TheGPU.getInstance().setInput (qid, 0,  _input_size);
		TheGPU.getInstance().setInput (qid, 1,  _window_ptrs_size);
		TheGPU.getInstance().setInput (qid, 2,  _window_ptrs_size);
		TheGPU.getInstance().setOutput(qid, 0, _output_size, 1);
		TheGPU.getInstance().setKernelReduce (qid, args);
		
		taskIdx = new int [PIPELINES];
		freeIdx = new int [PIPELINES];
		for (int i = 0; i < PIPELINES; i++) {
			taskIdx[i] = -1;
			freeIdx[i] = -1;
		}
	}
	
	@Override
	public String toString () {
		final StringBuilder sb = new StringBuilder();
		sb.append(type.asString(_the_aggregate.toString()));
		return sb.toString();
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
		/* Create the other two input buffers */
		
		windowBatch.initWindowPointers(startPtrs, endPtrs);
		// windowBatch.normalizeWindowPointers();
		
		// int [] _startPtrs = windowBatch.getWindowStartPointers();
		// int [] _endPtrs = windowBatch.getWindowEndPointers();
		// ByteBuffer b = ByteBuffer.wrap(startPtrs).order(ByteOrder.LITTLE_ENDIAN);
		// ByteBuffer d = ByteBuffer.wrap(  endPtrs).order(ByteOrder.LITTLE_ENDIAN);
		// b.clear();
		// d.clear();
		// for (int i = 0; i < _startPtrs.length; i++) {
		//	b.putInt(_startPtrs[i]);
		// 	d.putInt(_endPtrs[i]);
		// }
		
		TheGPU.getInstance().setInputBuffer(qid, 1, startPtrs);
		TheGPU.getInstance().setInputBuffer(qid, 2, endPtrs);
		
		TheGPU.getInstance().execute(qid, threads, THREADS_PER_GROUP);
		
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
	public void accept(OperatorVisitor visitor) {
		visitor.visit(this);
	}
	
	@Override
	public void processData (WindowBatch firstWindowBatch, WindowBatch secondWindowBatch, IWindowAPI api) {
		throw new UnsupportedOperationException("ReductionKernel operates on a single stream only");
	}
}
