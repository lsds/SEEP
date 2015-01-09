package uk.ac.imperial.lsds.streamsql.op.gpu.stateful;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.UnboundedQueryBufferFactory;
import uk.ac.imperial.lsds.seep.multi.Utils;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;
import uk.ac.imperial.lsds.seep.multi.WindowDefinition;
import uk.ac.imperial.lsds.streamsql.expressions.Expression;
import uk.ac.imperial.lsds.streamsql.expressions.ExpressionsUtil;
import uk.ac.imperial.lsds.streamsql.expressions.efloat.FloatColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.eint.IntColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.elong.LongColumnReference;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;
import uk.ac.imperial.lsds.streamsql.op.gpu.GPU;
import uk.ac.imperial.lsds.streamsql.op.stateful.AggregationType;

public class ReductionKernel implements IStreamSQLOperator, IMicroOperatorCode {
	
	/*
	 * This size must be greater or equal to the size of the byte array backing
	 * an input window batch.
	 */
	private static final int _default_input_size = Utils._GPU_INPUT_;
	/*
	 * Operator configuration parameters
	 */
	private static final int THREADS_PER_GROUP = 128;
	private static final int TUPLES_PER_THREAD = 4;
	
	private AggregationType type;
	private FloatColumnReference _the_aggregate;
	
	private LongColumnReference timestampReference;
	
	private WindowDefinition window;
	private int batchSize;
	
	private ITupleSchema inputSchema, outputSchema;
	
	private String filename = null;
	
	private int tuples;
	private int threads, groups;

	int outputSize, localSize;
	
	private String load (String filename) {
		File file = new File(filename);
		try {
			byte [] bytes = Files.readAllBytes(file.toPath());
			return new String (bytes, "UTF8");
		} catch (FileNotFoundException e) {
			System.err.println(String.format("error: file %s not found", filename));
		} catch (IOException e) {
			System.err.println(String.format("error: cannot read file %s", filename));
		}
		return null;
	}
	
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
	
	public void setWindowDefinition (WindowDefinition window) {
		this.window = window;
	}
	
	public void setInputSchema (ITupleSchema inputSchema) {
		this.inputSchema = inputSchema;
	}
	
	public void setBatchSize (int batchSize) {
		this.batchSize = batchSize;
	}
	
	public void setup () {
		
		this.tuples = _default_input_size / inputSchema.getByteSizeOfTuple();
		
		this.threads = tuples / TUPLES_PER_THREAD;
		this.groups = threads / THREADS_PER_GROUP;
		
		this.localSize = (THREADS_PER_GROUP) * this.outputSchema.getByteSizeOfTuple();
		this.outputSize = this.outputSchema.getByteSizeOfTuple() * groups;
		
		System.out.println(String.format("[DBG] %6d tuples", tuples));
		System.out.println(String.format("[DBG] %6d threads", threads));
		System.out.println(String.format("[DBG] %6d groups", groups));
		System.out.println(String.format("[DBG] %6d bytes scratch memory", localSize));
		System.out.println(String.format("[DBG] %6d bytes output", outputSize));
		
		String source = load (filename);
		
		int error; 
		error  = GPU.getInstance().getPlatform();
		error |= GPU.getInstance().getDevice();
		error |= GPU.getInstance().createContext();
		error |= GPU.getInstance().createCommandQueue();
		error |= GPU.getInstance().createProgram(source);
		
		if (error != 0) {
			System.err.println("Fatal error.");
			System.exit(1);
		}
		
		GPU.getInstance().createInputBuffer(_default_input_size);
		GPU.getInstance().createOutputBuffer(outputSize);
		
		GPU.getInstance().createWindowStartPointersBuffer(batchSize);
		GPU.getInstance().createWindowEndPointersBuffer(batchSize);
		
		GPU.getInstance().createKernel("reduce");
		
		GPU.getInstance().setReductionKernelArgs(tuples, localSize, false);
	}
	
	@Override
	public String toString () {
		final StringBuilder sb = new StringBuilder();
		sb.append(type.asString(_the_aggregate.toString()));
		return sb.toString();
	}
	
	@Override
	public void processData (WindowBatch windowBatch, IWindowAPI api) {
		
		IQueryBuffer outputBuffer = UnboundedQueryBufferFactory.newInstance();
		windowBatch.initWindowPointers();
		// windowBatch.normalizeWindowPointers();
		windowBatch.debug();
		
		/* 
		 * We copy input directly from the circular buffer to the zero-copy
		 * input buffer that lives in JNI land.
		 * 
		 * windowBatch.getBuffer().appendBytesTo(
		 *	windowBatch.getBatchStartPointer(), 
		 *	windowBatch.getBatchEndPointer(), 
		 *	input);
		 */
		
//		GPU.getInstance().setInputDataBuffer(windowBatch.getBuffer().array(), windowBatch.getBatchStartPointer(), windowBatch.getBatchEndPointer());
//		
//		GPU.getInstance().setWindowStartPointersBuffer(windowBatch.getWindowStartPointers());
//		GPU.getInstance().setWindowEndPointersBuffer(windowBatch.getWindowEndPointers());
//		
//		GPU.getInstance().setOutputDataBuffer(outputBuffer.array());
//		/* Execute kernel */
//		GPU.getInstance().invokeReductionOperatorKernel(threads, THREADS_PER_GROUP, false);
		
		windowBatch.setBuffer(outputBuffer);
		api.outputWindowBatchResult(-1, windowBatch);
		
		// System.exit(1);
		// GPU.getInstance().inc();
		// GPU.getInstance().debug();
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
