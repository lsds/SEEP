package seep.operator.workers;

import seep.operator.Operator;

import java.io.*;

/**
* ACKWorker. This runnable object is in charge of watching to the last processed tuple and generating an ACK when this has changed.
*/

public class ACKWorker implements Runnable, Serializable{

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

	
//	private static final long serialVersionUID = 1L;
//	
//	private Snk sink = null;
//	private seep.operator.collection.Snk snk = null;
//	private long memory = 0;
//
//	public ACKWorker(Operator sink){
//		if(sink instanceof seep.operator.collection.Snk){
//			this.snk = (seep.operator.collection.Snk)sink;
//		}
//		else if(sink instanceof seep.operator.collection.lrbenchmark.Snk){
//			this.sink = (seep.operator.collection.lrbenchmark.Snk)sink;
//		}
//	}
//	/// \todo{refactor this method}
//	public void run(){
//		if(sink != null){
//			while(true){
//				long currentTs = sink.getTACK();
//				if(currentTs > memory){
//	//long a = System.currentTimeMillis();
//					sink.ack(currentTs);
//	sink.ackCounter++;
//	//long b = System.currentTimeMillis() -a;
//	//System.out.println("*gen ACK: "+b);
//					memory = currentTs;
//				}
//				try{
//					//Sleep during 1 ms, the ACK flow traffic is decreased by doing this.
//					//update: 10 ms to check if this is the source of the sink bottleneck in LRB
//					//update: 100 ms to further reduce overhead
//					Thread.sleep(100);
//				}
//				catch(InterruptedException ie){
//					System.out.println("ACKWorker: while trying to sleep "+ie.getMessage());
//					ie.printStackTrace();
//				}
//			}
//		}
//		if(snk != null){
//			while(true){
//				long currentTs = snk.getTACK();
//				if(currentTs > memory){
//	//long a = System.currentTimeMillis();
//					snk.ack(currentTs);
//	snk.ackCounter++;
//	//long b = System.currentTimeMillis() -a;
//	//System.out.println("*gen ACK: "+b);
//					memory = currentTs;
//				}
//				try{
//					//Sleep during 1 ms, the ACK flow traffic is decreased by doing this.
//					//update: 10 ms to check if this is the source of the sink bottleneck in LRB
//					//update: 100 ms to further reduce overhead
//					Thread.sleep(100);
//				}
//				catch(InterruptedException ie){
//					System.out.println("ACKWorker: while trying to sleep "+ie.getMessage());
//					ie.printStackTrace();
//				}
//			}
//		}
//	}
}
