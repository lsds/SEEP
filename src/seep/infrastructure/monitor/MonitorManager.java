package seep.infrastructure.monitor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

import seep.Main;
import seep.P;
import seep.comm.serialization.MetricsTuple;
import seep.infrastructure.Infrastructure;
import seep.operator.Operator;

/**
* MonitorManager. This class implements Runnable and runs in the master node. It is in charge of retrieving the information sent by the monitors that control the system.
*/


public class MonitorManager implements Runnable{

	boolean listen = true;
	private Infrastructure inf = null;
	private StatisticsPrinter sp = null;

	private int lastOpTrigger = -1000;
	private double lastCpuUTrigger = 0;
	private long t_lastSplit = 0;
	private Map<Integer, Integer> alertsMemory = new HashMap<Integer, Integer>();
	private boolean suspended = false;
	
	private ArrayBlockingQueue<MetricsTuple> mem = new ArrayBlockingQueue<MetricsTuple>(1);

	public void initSp(){
		System.out.println("Initializing SP: ");
		sp = new StatisticsPrinter();
		Thread stat = new Thread(sp);
		stat.start();
		//sp.init();
	}
	
	public void stopMManager(boolean stop){
		if(stop)
			listen = false;
		else
			listen = true;
	}

	public MonitorManager(Infrastructure inf){
		this.inf = inf;
	}

	public void run(){

		ServerSocket ss = null;
		Socket currentConn = null;
		try{
			ss = new ServerSocket(Integer.parseInt(P.valueFor("monitorManagerPort")));
			Thread consumer = new Thread(new Consumer(mem));
			consumer.start();
			while(listen){
				currentConn = ss.accept();
				Thread client = new Thread(new MonitorManagerWorker(currentConn, mem));
				client.start();
			}
			ss.close();
		}
		catch(IOException io){
			System.out.println("MonitorManager: "+io.getMessage());
		}
	}

	private synchronized void alertHandler(int opId, double cpuU){
		suspended = true;
		//Show only those cpuU greater than 25
		if(cpuU > 25) System.out.println("OP"+opId+" cpuU: "+cpuU);
		//If cpu is above the threshold and the operator is parallelizable
		/// \todo{get information regarding operator parallelizable or not from infrastructure (when new HIGH LEVEL API is available}
		///Change these lines to allow all operators to scale out
		//if(cpuU > ExecutionConfiguration.cpuUThreshold && opId != -1 && opId != -2 && opId != 34){
		String opType = inf.getOpType(opId);
		if((opType.equals("seep.operator.collection.lrbenchmark.Forwarder") && cpuU > 35) //15, 50 -> best 35
				||
			(opType.equals("seep.operator.collection.lrbenchmark.TollCollector") && cpuU > 50) //30, 50 best 50
				||
			(opType.equals("seep.operator.collection.lrbenchmark.TollCalculator") && cpuU > 50) //30, 90 best 50
				||
			(opType.equals("seep.operator.collection.lrbenchmark.TollAssessment") && cpuU > 40)) { //20, 90 best 40
//				&& cpuU > ExecutionConfiguration.cpuUThresholdTC)){
			//If it is not the same operator that inmediately before splitted, or if it is the same, but the CPU utilisation has increased above a pondered CPU_U
//			if((opId != lastOpTrigger) || (opId == lastOpTrigger && cpuU > (lastCpuUTrigger))){
//				lastCpuUTrigger = cpuU+5;
//				lastOpTrigger = opId;
				if(P.valueFor("enableAutomaticScaleOut").equals("true")){
					if(alertsMemory.containsKey(opId)){ 
						int numAlerts = alertsMemory.get(opId);
						//...provided that the operator has reported X consecutive times higher CPU utilisation
						if(numAlerts == Integer.parseInt(P.valueFor("numMaxAlerts"))){
//							int elapsedTime = (int)((int)(System.currentTimeMillis() - t_lastSplit))/1000;
//							if(elapsedTime > ExecutionConfiguration.minimumTimeBetweenSplit){
								//...ALERT cpu overloaded...
//								inf.getEiu().alertCPU(opId);
								inf.getEiu().alert(opId);
								alertsMemory.put(opId, 0);
								//update the time of last split
								t_lastSplit = System.currentTimeMillis();
//							}
//							else{
//								System.out.println("NOT ENOUGH TIME BTW SPLIT: "+elapsedTime+" seconds");
//							}
						}
						//... if not increase the counter
						else{
							numAlerts = numAlerts + 1;
							alertsMemory.put(opId, numAlerts);
						}
					}
					//if this is the first time the operator reports its cpu U, then place it into memory
					else{
						alertsMemory.put(opId, 1);
					}
				}
//			}
//			//if the operator is the same that inmediately before splitted, then reset the cpu threshold
//			else{
//				if(opId == lastOpTrigger){
//					lastCpuUTrigger = ExecutionConfiguration.cpuUThreshold;
//				}
//			}
		}
		//If the operator is not above the established CPU threshold, then reset the memory for this operator
		else{
			if(alertsMemory.containsKey(opId)){
				alertsMemory.put(opId, 0);
			}
			else{
				alertsMemory.put(opId, 0);
			}
		}
		suspended = false;
		t_lastSplit = System.currentTimeMillis();
	}
	
	class StatisticsPrinter implements Runnable{
		
		boolean goOn = false;
		long t_start = 0;
		long t_init = 0;

		public StatisticsPrinter(){
			goOn = true;
			t_start = System.currentTimeMillis();
			t_init = t_start;
		}
		
		public void setGoOn(boolean goOn) {
			this.goOn = goOn;
		}

		public void setT_start(long tStart) {
			t_start = tStart;
		}

		public void setT_init(long tInit) {
			t_init = tInit;
		}
		
		public void init(){
			goOn = true;
			t_start = System.currentTimeMillis();
			t_init = t_start;
		}
		
		@Override
		public void run() {
			
			long instantTime = 0;
			System.out.println("WAITING TO ORDER TO WRITE STATS");
			while(true){
				
				if(goOn){
					instantTime = System.currentTimeMillis();
				
					int t = (int) ((int)(instantTime - t_init)/1000);
			
					if((instantTime - t_start) >= 1000){
						t_start = System.currentTimeMillis();
//						System.out.println("FW E/S: "+ackCounter);
						System.out.println("# "+t+" "+inf.getNumberRunningMachines());
					}
				}
			}
		}
	}
	
	
	class Consumer implements Runnable{

		boolean scalingOut = false;
		private boolean listen = true;
		private ArrayBlockingQueue<MetricsTuple> mem = null;
		
		public Consumer(ArrayBlockingQueue<MetricsTuple> mem){
			this.mem = mem;
		}
		
		private synchronized void decider(int opId, long queueSize){
		
			
//			try {
//				Thread.sleep(2000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			Operator toScale = inf.getOperatorById(opId);
			
			if(!scalingOut){
				synchronized(inf){
					scalingOut = true;
					inf.getEiu().alert(opId);
					scalingOut = false;
				}
			}
			if(queueSize > 90000){
				if(P.valueFor("enableAutomaticScaleOut").equals("true")){
					if(!scalingOut){
						synchronized(inf){
							scalingOut = true;
							inf.getEiu().alert(opId);
							scalingOut = false;
						}
					}
				}
			}
		}
		
		
		@Override
		public void run() {
			while(listen){
				/**TEMPORAL HACK heuristic policy **/
//				System.out.println("get something");
//				triggers++;
				MetricsTuple m = null;
				try {
					m = mem.take();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				long n = m.getInputQueueEvents();
				if(n > 30000){
//					System.out.println("bigger than 30K, and triggers is: "+triggers);
//					if(triggers > 5){
						System.out.println("To Decisor");
						decider(m.getOpId(), n);
//						triggers = 0;
					}
					System.out.println("OP: "+m.getOpId()+" IQL: "+n);
				}
				
				/**END TEMPORAL HACK**/
			}
		}
	}
	
	class MonitorManagerWorker implements Runnable{

		private Socket conn = null;
		private InputStream is = null;
		private BufferedWriter bw = null;
		private Input input = null;
		private Kryo k = null;
		private boolean listen = true;
		private ArrayBlockingQueue<MetricsTuple> mem = null;

		public MonitorManagerWorker(Socket conn, ArrayBlockingQueue<MetricsTuple> mem){
			this.conn = conn;
			this.k = initializeKryo();
			this.mem = mem;
		}
		
		private Kryo initializeKryo(){
			k = new Kryo();
			k.register(MetricsTuple.class);
			return k;
		}
		
		public void run(){
			int triggers = 0;
			try{
				is = conn.getInputStream();
				input = new Input(is);
				//bw = new OutputStreamWriter(new FileOutputStream("m.csv"));
				
				while(listen){
//					Seep.Statistics stat = Seep.Statistics.parseDelimitedFrom(is);
					MetricsTuple m = k.readObject(input, MetricsTuple.class);
					//Put in a one-size queue
					mem.offer(m);
				}
				input.close();
				is.close();
				conn.close();
				bw.flush();
				bw.close();
			}
			catch(IOException io){
				System.out.println("While reading input in monitorManangerWorker: "+io.getMessage());
			}
		}

//		public synchronized void statisticsHandler(int opId, double cpuU, int time, double th, BufferedWriter bw){
//			Infrastructure.msh.th = th;
//			Infrastructure.msh.numberRunningMachines = inf.getNumberRunningMachines();
//			/// \fix{hack, invert cpu, cause we are sending idle cpu time}
////			int value = (int) (100 - cpuU);
//			double value = cpuU;
//			//Sanity check in case the information is corrupted (reported case where cpu was 882% in a slow wombat)
//			if(cpuU > 0 && cpuU < 150){
////				int elapsedTime = (int)(System.currentTimeMillis() - t_lastSplit)/1000;
////				System.out.println("elapsedTime: "+elapsedTime);
////				if(!suspended && elapsedTime > 3){
//				if(!suspended){
//					alertHandler(opId, value);
//				}
//			}
//			else{
//				//double rate = ((double)(ExecutionConfiguration.eventR*ExecutionConfiguration.sentenceSize)/1000);
//				if(time != 0){
//					//System.out.println("Time: "+time+" rate: "+rate+" th: "+throughput+" numberOfMachines: "+inf.getNumberRunningMachines());
////					System.out.println("Time: "+time+" th: "+th+" numberOfMachines: "+inf.getNumberRunningMachines());
//				}
//				//String info = time+", "+rate+", "+throughput+", "+(inf.getNumberRunningMachines());
//				String info = time+", "+th+", "+(inf.getNumberRunningMachines());
//				
//				log(bw, info);
//			}
//		}

		private void log(BufferedWriter bw, String info) {
			/**
			 * DEBUGGING some exception when appending...
			 */
//			try{
//				bw = new BufferedWriter(new FileWriter("tests/m.csv", true));
//				bw.write(info);
//				bw.newLine();
//				bw.close();
//			}
//			catch(IOException io){
//				System.out.println("MonitorManager: While writing to file "+io.getMessage());
//				io.printStackTrace();
//				try{
//					bw.close();
//				}
//				catch(IOException io2){
//					System.out.println("MonitorManager: While closing bw "+io2.getMessage());
//					io2.printStackTrace();
//				}
//			}
		}
	}
