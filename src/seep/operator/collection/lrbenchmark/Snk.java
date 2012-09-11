package seep.operator.collection.lrbenchmark;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;

import seep.comm.tuples.Seep;
import seep.infrastructure.NodeManager;
import seep.operator.Operator;
import seep.operator.StatelessOperator;
import seep.operator.workers.ACKWorker;

@SuppressWarnings("serial")
public class Snk extends Operator implements StatelessOperator{

	private boolean firstTime = true;
	private long tinit = 0;
	private ACKWorker ackWorker;
	private long tACK = 0;
	
	private long t_start = 0;
	private int counter = 0;
	private int bytes = 0;
	
	private int eventDiscarted = 0;
	private int acumLatency = 0;
	private StringBuilder sb = null;
	private boolean flag = false;
	private long mem = 0;
	
	private int numTuples = 0;

	public long getTACK(){
		return tACK;
	}
	
	public Snk(int opID) {
		super(opID);
		subclassOperator = this;
		ackWorker = new ACKWorker(this);
	}
	
	public void save(){
		try {
			BufferedWriter bos = new BufferedWriter(new FileWriter(new File("latency.dat")));
			String data = null;
			synchronized(sb){
				data = sb.toString();
			}
//			System.out.println("DATA: "+data);
			bos.write(data);
			bos.close();
		} 
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("SAVED!!!!!");
	}

	@Override
	public synchronized void processData(Seep.DataTuple dt) {
		//instrumentation variables
		counter++;
		bytes += dt.getSerializedSize();
		
		//current timestamp
		long instantTime = System.currentTimeMillis();
		//current elapsed time
		int currentTime = (int)(instantTime - t_start);
		//current elapsed time in seconds
		int t = (int) ((int)(instantTime - tinit)/1000);
		
		//sampling tuple latency
		if(dt.getType() == 0){
			numTuples++;
			if(numTuples == 100){
				//Print time and latency for this tuple
				String out = (t+" "+(instantTime - dt.getTs())+"\n");
				sb.append(out);
//				System.out.println("# "+t+" "+(instantTime - dt.getTs()));
				numTuples = 0;
			}
			//acumLatency += (instantTime-dt.getTs());
		}
		else{
			//eventDiscarted++;
		}
		
		if(firstTime){
			firstTime = false;
//			tinit = dt.getTime();
			// one second margin for warm-up of the system
			NodeManager.monitorOfSink = true;
			tinit = instantTime;
			t_start = instantTime;
			new Thread(ackWorker).start();
			
			//builder
			sb = new StringBuilder();
		}
		tACK = dt.getTs();
		
		
	 	if(t > dt.getTime()+5 && dt.getType() != 2 ){
			//System.out.println("TS violated -> emit: "+dt.getTime()+" current: "+t);
	 		mem = dt.getTime() - t;
			flag = true;
		}

		if(currentTime >= 1000){
			t_start = instantTime;
			int kbps = (bytes*8)/1000;
			//TIME - E/S - kbps - ilatency - avgLatency during last second
			//System.out.println(t+" "+counter+" "+kbps+" "+(instantTime-dt.getTs())+" "+(acumLatency/(counter-eventDiscarted)));
			System.out.println(t+" "+counter+" "+kbps);
			if(flag){
//				System.out.println("TS violated -> emit: "+dt.getTime()+" current: "+t);
				System.out.println("TS violated -> "+ mem);
				flag = false;
			}
//			System.out.println("T: "+t+" E/S: "+counter+" kbps: "+kbps+" iL: "+(instantTime-dt.getTs())+" avgL: "+(acumLatency/(counter-eventDiscarted)));
//			System.out.println("E/S: "+counter);
			//System.out.println("AvgLatency: "+(acumLatency/(counter-eventDiscarted)));
			bytes = 0;
			counter = 0;
			acumLatency = 0;
			eventDiscarted = 0;
			ackCounter = 0;
		}
	}

	@Override
	public boolean isOrderSensitive() {
		// TODO Auto-generated method stub
		return false;
	}
}
