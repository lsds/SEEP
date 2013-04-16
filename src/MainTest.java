import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import seep.comm.serialization.DataTuple;
import seep.comm.serialization.messages.Payload;
import seep.comm.serialization.messages.TuplePayload;
import seep.comm.serialization.serializers.ArrayListSerializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferOutputStream;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/** SYNC STATE **/

public class MainTest{

	public void testByteBuffer(){
		
		File sm = new File("sharedMemory");
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(sm);
		} 
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ByteBuffer nativeBuffer = ByteBuffer.allocate(32);
		ByteBufferOutputStream bbos = new ByteBufferOutputStream(nativeBuffer);
		
		Output o = new Output(bbos);
		// finally we register this socket to the selector for the async behaviour, and we link nativeBuffer, for the selector to access it directly
		Kryo k = new Kryo();
		
		DataTuple dt = new DataTuple(null, new TuplePayload());
		dt.setValues("hija");
		
//		C c = new C();
//		c.id = 1548;
//		ArrayList<Integer> l = new ArrayList<Integer>();
//		l.add(5);
//		l.add(55);
//		l.add(555);
//		c.l = l;
		
		System.out.println("write object to o->bbos");
		k.register(ArrayList.class, new ArrayListSerializer());
		k.register(Payload.class);
		k.register(TuplePayload.class);
		k.writeObject(o, dt.getPayload());
		
		dt.setValues("hioj");
		k.writeObject(o, dt.getPayload());
		
		System.out.println("flush to bytebuffer");
		o.flush();
		
		ByteBuffer aux = ((ByteBufferOutputStream)o.getOutputStream()).getByteBuffer();
		System.out.println("AUX: "+aux.toString());
		
		System.out.println("serialized object in bytes, now write this to disk");
		
		try {
			fos.write(aux.array(), 0, aux.position());
			fos.close();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Read from disk");
		
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(sm);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Input i = new Input(fis);
		TuplePayload tp = k.readObject(i, TuplePayload.class);
		
		for(Object integer : tp.attrValues){
			System.out.println("1int: "+integer);
		}
		
		TuplePayload tp2 = k.readObject(i, TuplePayload.class);
		for(Object integer : tp2.attrValues){
			System.out.println("2int: "+integer);
		}
		
//		for(int integer : c.l){
//			System.out.println("List element: "+integer);
//		}
		
		
	}
	
	public static void main(String args[]){
		
		MainTest mt = new MainTest();
		mt.testByteBuffer();
	}
}


/** INPUT QUEUE SYNC STUFF**/

//import java.util.ArrayList;
//import java.util.concurrent.ArrayBlockingQueue;
//import java.util.concurrent.BlockingQueue;
//
//import seep.comm.serialization.DataTuple;
//import seep.infrastructure.monitor.MetricsReader;
//import seep.operator.InputQueue;
//
//
//public class MainTest {
//
//	InputQueue inputQueue = new InputQueue(100000);
//	InputQueue2 inputQueue2 = null;
//	
//	private BlockingQueue<DataTuple> iq = new ArrayBlockingQueue<DataTuple>(100000);
//	
//	int totalE = 0;
//	
//	public synchronized void addEvents(int events){
//		totalE += events;
//	}
//	
//	public class InputQueue2{
//		
//		private InputQueue iq = null;
//		
//		public InputQueue2(InputQueue iq){
//			this.iq = iq;
//		}
//		
//		public synchronized void push2(DataTuple dt){
//			iq.push(dt);
//		}
//		
//		public DataTuple pull2(){
//			return iq.pull();
//		}
//		
//	}
//	
//	public static void main(String args[]){
//		MainTest mt = new MainTest();
//		int p = 2;
//		int c = 1;
//		mt.singleQueue(p, c);
////		mt.singleQueueDirect(p,c);
////		mt.multiQueue(p,c);
////		mt.singleQueueDoubleWrapper(p,c);
//	}
//	
//	public void singleQueueDoubleWrapper(int p, int c){
//		
//		this.inputQueue2 = new InputQueue2(inputQueue);
//		
//		for(int j = 0; j<c; j++){
//			Thread con = new Thread(new ConsumerDoubleWrapper());
//			con.start();
//		}
//		
//		for(int i = 0; i<p; i++){
//			Thread prod = new Thread(new ProducerDoubleWrapper());
//			prod.start();
//		}
//	}
//	
//	public void singleQueueDirect(int p, int c){
//		for(int j = 0; j<c; j++){
//			Thread con = new Thread(new ConsumerDirect());
//			con.start();
//		}
//		
//		for(int i = 0; i<p; i++){
//			Thread prod = new Thread(new ProducerDirect());
//			prod.start();
//		}
//	}
//
//	public void multiQueue(int p, int c){
//		
//		ArrayList<InputQueue> queues = new ArrayList<InputQueue>();
//		
//		for(int i = 0; i<p; i++){
//			ProducerOwnQueue poq = new ProducerOwnQueue();
//			queues.add(poq.getInputQueue());
//			Thread prod = new Thread(poq);
//			prod.start();
//		}
//		
//		Thread con = new Thread(new ConsumerMultiQueue(queues));
//		con.start();
//	}
//	
//	public void singleQueue(int p, int c){
//		
//		for(int j = 0; j<c; j++){
//			Thread con = new Thread(new Consumer());
//			con.start();
//		}
//		
//		for(int i = 0; i<p; i++){
//			Thread prod = new Thread(new Producer());
//			prod.start();
//		}
//	}
//
//	public class Producer implements Runnable{
//
//		DataTuple data = new DataTuple();
//		
//		@Override
//		public void run() {
//			while(true){
//				data.setId(1);
//				inputQueue.push(data);
//			}
//		}
//	}
//	
//	public class ProducerDirect implements Runnable{
//
//		DataTuple data = new DataTuple();
//		
//		@Override
//		public void run() {
//			while(true){
//				data.setId(1);
//				try {
//					synchronized(iq){	
//						iq.put(data);
//					}
////					MetricsReader.eventsInputQueue.inc();
//				} 
//				catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
//	}
//	
//	public class ProducerDoubleWrapper implements Runnable{
//
//		DataTuple data = new DataTuple();
//		
//		@Override
//		public void run() {
//			while(true){
//				data.setId(1);
//				inputQueue2.push2(data);
//			}
//		}
//	}
//	
//	public class ProducerOwnQueue implements Runnable{
//
//		InputQueue ownQueue = new InputQueue(100000);
//		DataTuple data = new DataTuple();
//		
//		public InputQueue getInputQueue(){
//			return ownQueue;
//		}
//		
//		@Override
//		public void run() {
//			while(true){
//				data.setId(1);
//				ownQueue.push(data);
//			}
//		}
//	}
//	
//	public class ConsumerMultiQueue implements Runnable{
//
//		ArrayList<InputQueue> queues = null;
//		int accessIndex = 0;
//		
//		private int counter = 0;
//		
//		boolean first = true;
//		long t_start = 0;
//		long i_time = 0;
//		
//		public ConsumerMultiQueue(ArrayList<InputQueue> queues){
//			this.queues = queues;
//		}
//		
//		@Override
//		public void run() {
//			while(true){
//				InputQueue accessor = queues.get(accessIndex);
//				if(accessIndex == (queues.size()-1)){
//					accessIndex = 0;
//				}
//				else{
//					accessIndex++;
//				}
//				DataTuple data = accessor.pull();
//				counter++;
//				if(first){
//					t_start = System.currentTimeMillis();
//					first = false;
//				}
//				i_time = System.currentTimeMillis();
//				long currentTime = i_time - t_start;
//				
//				if(currentTime >= 1000){
//					System.out.println("E/S: "+counter);
//					t_start = System.currentTimeMillis();
//					counter = 0;
//				}
//			}
//		}
//	}
//	
//	public class Consumer implements Runnable{
//
//		
//		private int counter = 0;
//		
//		boolean first = true;
//		long t_start = 0;
//		long i_time = 0;
//		
//		@Override
//		public void run() {
//			while(true){
//				DataTuple data = inputQueue.pull();
//				counter++;
//				if(first){
//					t_start = System.currentTimeMillis();
//					first = false;
//				}
//				i_time = System.currentTimeMillis();
//				long currentTime = i_time - t_start;
//				
//				if(currentTime >= 1000){
//					System.out.println("THREAD: "+Thread.currentThread());
//					System.out.println("E/S: "+counter);
//					System.out.println("InputQueue Size: "+MetricsReader.eventsInputQueue.getCount());
//					t_start = System.currentTimeMillis();
//					counter = 0;
//				}
//			}
//		}
//	}
//	
//public class ConsumerDoubleWrapper implements Runnable{
//
//		
//		private int counter = 0;
//		
//		boolean first = true;
//		long t_start = 0;
//		long i_time = 0;
//		
//		@Override
//		public void run() {
//			while(true){
//				DataTuple data = inputQueue2.pull2();
//				counter++;
//				if(first){
//					t_start = System.currentTimeMillis();
//					first = false;
//				}
//				i_time = System.currentTimeMillis();
//				long currentTime = i_time - t_start;
//				
//				if(currentTime >= 1000){
//					System.out.println("THREAD: "+Thread.currentThread());
//					System.out.println("E/S: "+counter);
//					System.out.println("InputQueue Size: "+MetricsReader.eventsInputQueue.getCount());
//					t_start = System.currentTimeMillis();
//					counter = 0;
//				}
//			}
//		}
//	}
//		
//	public class ConsumerDirect implements Runnable{	
//		private int counter = 0;
//		
//		boolean first = true;
//		long t_start = 0;
//		long i_time = 0;
//		
//		@Override
//		public void run() {
//			while(true){
//				try {
//					DataTuple data = iq.take();
////					MetricsReader.eventsInputQueue.dec();
//				} 
//				catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				counter++;
//				if(first){
//					t_start = System.currentTimeMillis();
//					first = false;
//				}
//				i_time = System.currentTimeMillis();
//				long currentTime = i_time - t_start;
//				
//				if(currentTime >= 1000){
//					System.out.println("THREAD: "+Thread.currentThread());
//					System.out.println("E/S: "+counter);
//					System.out.println("InputQueue Size: "+MetricsReader.eventsInputQueue.getCount());
//					t_start = System.currentTimeMillis();
//					counter = 0;
//				}
//			}
//		}
//	}
//		
//	
//}
