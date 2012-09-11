package seep.operator.collection;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import seep.Main;
import seep.infrastructure.NodeManager;
import seep.operator.*;
import seep.operator.workers.ACKWorker;
import seep.comm.*;
import seep.comm.tuples.*;
import seep.utils.ExecutionConfiguration;

@SuppressWarnings("serial")
public class Snk extends Operator implements StatelessOperator{

	private ACKWorker ackWorker;
	private long tACK = 0;
	private boolean first = true;

	public long getTACK(){
		return tACK;
	}
	
	public Snk(int opID){
		super(opID);
		//There is just one sink
		subclassOperator = this;
		ackWorker = new ACKWorker(this);
	}

	int counter = 0;
	long t_start = 0;
	long period = 1000;
	int avgFactor = 0;
	int second = 0;


	public void processData(Seep.DataTuple dt){
		System.out.println("SINK: Data received, WORD-> "+dt.getString()+" COUNT->"+dt.getInt());
		tACK = dt.getTs();

		if(first){
			//Start the ACK worker
			new Thread(ackWorker).start();
			NodeManager.monitorOfSink = true;
			first = false;
			//Profiling variables
			t_start = System.currentTimeMillis();
			period = 1000;
		}
		counter++;
		if((System.currentTimeMillis()-t_start) >= period){
			//number of bits processed over a second is the Throughput, kbps
			//double bps = (double)((counter/12)*ExecutionConfiguration.sentenceSize);
			int bps = (counter/12) * Integer.parseInt(Main.valueFor("sentenceSize"));
			int th = (bps)/1000;
			System.out.println("E/S: "+counter+ "Th: "+th);
			//NodeManager.nodeMonitor.info(th, second);
			second++;
			t_start = System.currentTimeMillis();
			counter = 0;
		}
	}

	@Override
	public boolean isOrderSensitive() {
		// TODO Auto-generated method stub
		return false;
	}
}
