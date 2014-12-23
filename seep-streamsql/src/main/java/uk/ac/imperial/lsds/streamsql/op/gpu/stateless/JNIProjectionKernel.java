package uk.ac.imperial.lsds.streamsql.op.gpu.stateless;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;

import sun.misc.Unsafe;
import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.UnboundedQueryBufferFactory;
import uk.ac.imperial.lsds.seep.multi.Utils;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;
import uk.ac.imperial.lsds.streamsql.expressions.Expression;
import uk.ac.imperial.lsds.streamsql.expressions.ExpressionsUtil;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;
import uk.ac.imperial.lsds.streamsql.op.gpu.GPU;

public class JNIProjectionKernel implements IStreamSQLOperator, IMicroOperatorCode {
	
	/*
	 * This size must be greater or equal to the size of the byte array backing
	 * an input window batch.
	 */
	private static final int _default_input_size = Utils._GPU_INPUT_;
	private static final int _default_output_size = Utils._GPU_OUTPUT_;
	
	private Expression[] expressions;
	private ITupleSchema inputSchema, outputSchema;
	
	private String filename = null;
	
	private int tuples;
	private int threads;
	private int _thread_group_;
	
	private byte [] input;
	int localSize;
	long inputAddr, outputAddr;
	
	Unsafe unsafe;
	
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
	
	public Unsafe getUnsafeMemory() {
		try {
			Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
			theUnsafe.setAccessible(true);
			return (Unsafe) theUnsafe.get(null);
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}
	
	public JNIProjectionKernel(Expression[] expressions, ITupleSchema inputSchema,
			String filename) {
		this.expressions = expressions;
		this.inputSchema = inputSchema;
		this.outputSchema = ExpressionsUtil
				.getTupleSchemaForExpressions(expressions);
		
		this.filename = filename;
		
		setup();
	}
	
	public JNIProjectionKernel (Expression[] expressions) {
		this(expressions, null, null);
	}
	
	public JNIProjectionKernel (Expression expression) {
		this(new Expression[] { expression }, null, null);
	}
	
	private void setup() {
		
		this.input  = new byte[ _default_input_size];
		
		this.tuples = _default_input_size / inputSchema.getByteSizeOfTuple();
		this.threads = tuples;
		this._thread_group_ = 128;
		this.localSize = (_thread_group_) * inputSchema.getByteSizeOfTuple();
		
		unsafe = getUnsafeMemory();
		String source = load (filename);
		GPU.getInstance().getPlatform();
		GPU.getInstance().getDevice();
		GPU.getInstance().createContext();
		GPU.getInstance().createCommandQueue();
		GPU.getInstance().createProgram(source);
		GPU.getInstance().createKernel("project");
		inputAddr = GPU.getInstance().createInputBuffer(_default_input_size);
		outputAddr = GPU.getInstance().createOutputBuffer(_default_output_size);
		GPU.getInstance().setKernelArgs(tuples, localSize, false);
	}
	
	@Override
	public String toString () {
		StringBuilder sb = new StringBuilder();
		sb.append("Projection (");
		for (Expression expr : expressions)
			sb.append(expr.toString() + " ");
		sb.append(")");
		return sb.toString();
	}
	
	@Override
	public void processData (WindowBatch windowBatch, IWindowAPI api) {
		/* Copy input */
		windowBatch.getBuffer().appendBytesTo(
			windowBatch.getBatchStartPointer(), 
			windowBatch.getBatchEndPointer(), 
			input);
		
		IQueryBuffer outputBuffer = UnboundedQueryBufferFactory.newInstance();
		byte [] outputArray = outputBuffer.array();
		
		/* Execute kernel */
		unsafe.copyMemory(input, Unsafe.ARRAY_BYTE_BASE_OFFSET, null, inputAddr, _default_input_size);
		GPU.getInstance().invokeKernel(threads, _thread_group_, false, false);
		unsafe.copyMemory(null, outputAddr, outputArray, Unsafe.ARRAY_BYTE_BASE_OFFSET, _default_output_size);
		windowBatch.setBuffer(outputBuffer);
		api.outputWindowBatchResult(-1, windowBatch);
	}
	
	@Override
	public void accept(OperatorVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public void processData(WindowBatch firstWindowBatch, WindowBatch secondWindowBatch, IWindowAPI api) {
		throw new UnsupportedOperationException("JNIProjectionKernel is single input operator and does not operate on two streams");
	}
}
