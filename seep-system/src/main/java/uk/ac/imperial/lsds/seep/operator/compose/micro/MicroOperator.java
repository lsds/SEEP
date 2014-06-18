package uk.ac.imperial.lsds.seep.operator.compose.micro;


public class MicroOperator {

	IMicroOperatorCode op;
	int id;

//	Map<Integer, BlockingQueue<DataTuple>> inputQueues;
//	
//	Map<Integer, BlockingQueue<DataTuple>> outputQueues;
//
//	ISubQueryConnectable parent;
//	
//	public class OPWorker implements Runnable, API, Callback {
//		
//		private int inQueueIndex = 0;
//		private int outQueueIndex = 0;
//		private int batchSize;
//		
//		public OPWorker() { this(1); }
//		
//		public OPWorker(int batchSize) {
//			this.batchSize = batchSize;
//		}
//		
//		@Override
//		public void run() {
//			
//			API localApi = new LocalApi(this);
//			
//			while (true) {
//			
//				Queue<DataTuple> batch = new LinkedList<>();
//				
//				try {
//					for (int i = 0; i < this.batchSize; i++)
//						batch.add(inputQueues.get(inQueueIndex).take());
//	
//				} catch (InterruptedException e) { 
//					//TODO: put batch back to input queue
//				}
//				
//				/*
//				 * drainTo does not block
//				 */
//				//inputQueues.get(queueIndex).drainTo(batch, batchSize);
//				
//				inQueueIndex = (inQueueIndex++) % inputQueues.values().size();
//					
//				for (DataTuple tuple : batch)
//					op.processData(tuple, localApi);
//				
//			}
//		}
//
//	}
//	
	private MicroOperator(IMicroOperatorCode op, int id) {
		this.op = op;
		this.id = id;
//		this.inputQueues = new HashMap<Integer, BlockingQueue<DataTuple>>();
//		this.outputQueues = new HashMap<Integer, BlockingQueue<DataTuple>>();
	}
//	
//	@Override
//	public void setParentLocalConnectable(ISubQueryConnectable parent) {
//		this.parent = parent;
//	}
//	
	public static MicroOperator newMicroOperator(IMicroOperatorCode op, int id) {
		return new MicroOperator(op, id);
	}
//	
//	
//	@Override
//	public void execute(ExecutorService executorService, int numberThreads, int batchSize) {
//		for (int i = 0; i < numberThreads; i++)
//			executorService.execute(new OPWorker(batchSize));
//	}
//
//	@Override
//	public int getMicroOperatorId() {
//		return id;
//	}
//
//	@Override
//	public void registerInputQueue(Integer streamId,
//			BlockingQueue<DataTuple> queue) {
//		this.inputQueues.put(streamId, queue);
//	}
//
//	@Override
//	public void registerOutputQueue(Integer streamId,
//			BlockingQueue<DataTuple> queue) {
//		this.outputQueues.put(streamId, queue);
//	}
//
//	@Override
//	public void pushData(DataTuple tuple) {
//		/*
//		 * This method is only used to push data
//		 * to the most upstream operator, which cannot
//		 * have more than one input queue		
//		 */
//		try {
//			this.inputQueues.values().iterator().next().put(tuple);
//		} catch (InterruptedException e) {
//			//TODO: notify microOp and multiOp about failure
//		}
//	}
	
}
