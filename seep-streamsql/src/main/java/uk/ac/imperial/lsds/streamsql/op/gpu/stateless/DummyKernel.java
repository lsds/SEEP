package uk.ac.imperial.lsds.streamsql.op.gpu.stateless;

import java.nio.ByteBuffer;

import uk.ac.imperial.lsds.seep.multi.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.multi.IQueryBuffer;
import uk.ac.imperial.lsds.seep.multi.ITupleSchema;
import uk.ac.imperial.lsds.seep.multi.IWindowAPI;
import uk.ac.imperial.lsds.seep.multi.TheGPU;
import uk.ac.imperial.lsds.seep.multi.UnboundedQueryBufferFactory;
import uk.ac.imperial.lsds.seep.multi.Utils;
import uk.ac.imperial.lsds.seep.multi.WindowBatch;
import uk.ac.imperial.lsds.streamsql.expressions.Expression;
import uk.ac.imperial.lsds.streamsql.expressions.ExpressionsUtil;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.op.gpu.KernelCodeGenerator;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;

public class DummyKernel implements IStreamSQLOperator, IMicroOperatorCode {
	
	private String filename = null;
	
	private int inputSize = 0;
	
	private ITupleSchema schema;
	
	private int qid;
	
	private int tuples;
	private int threads;
	private int tgs; /* Threads/group */
	
	public DummyKernel (ITupleSchema schema, String filename, int inputSize) {
		this.schema = schema;
		this.filename = filename;
		this.inputSize = inputSize;
	}
	
	public void setup() {
		
		this.tuples = inputSize / schema.getByteSizeOfTuple();
		
		this.threads = tuples;
		this.tgs = 256;
		
		String source = KernelCodeGenerator.load(filename);
		
		qid = TheGPU.getInstance().getQuery(source, 1, 1, 1);
		TheGPU.getInstance().setInput (qid, 0, inputSize);
		
		TheGPU.getInstance().setOutput(qid, 0, inputSize, 1, 0, 0, 1);
		
		TheGPU.getInstance().setKernelDummy (qid, null);
	}
	
	public int getQueryId () {
		return this.qid;
	}
	
	public int getThreads () {
		return this.threads;
	}
	
	public int getThreadsPerGroup () {
		return this.tgs;
	}
	
	@Override
	public String toString () {
		StringBuilder sb = new StringBuilder();
		sb.append("DummyKernel ()");
		return sb.toString();
	}
	
	@Override
	public void processData (WindowBatch windowBatch, IWindowAPI api) {
	}
	
	@Override
	public void accept(OperatorVisitor visitor) {
		visitor.visit(this);
	}
	
	@Override
	public void processData(WindowBatch firstWindowBatch,
			WindowBatch secondWindowBatch, IWindowAPI api) {
		throw new UnsupportedOperationException("ProjectionKernel operates on a single stream only");
	}
}
