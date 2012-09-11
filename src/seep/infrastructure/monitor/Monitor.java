package seep.infrastructure.monitor;

import seep.Main;
import seep.comm.tuples.*;
import seep.infrastructure.NodeManager;
import seep.operator.OperatorContext;
import seep.operator.OperatorContext.PlacedOperator;
import seep.utils.ExecutionConfiguration;

import java.net.*;
import java.io.*;
import java.util.*;

/**
* Monitor. This class implements runnable and is in charge of retrieving information from the system. There is a monitor in each node that is being used by the system.
*/

public class Monitor implements Runnable{

	private Socket conn = null;
	private InetAddress addrMon = null;
	private int portMon = Integer.parseInt(Main.valueFor("monitorManagerPort"));
	private OutputStream out = null;
//	private InputStreamReader isr = null;
//	private BufferedReader br = null;
//	private String fileWithCpuU = ExecutionConfiguration.fileWithCpuU;
	private RandomAccessFile sourceFile;
	private int opId = -1;

	private double throughput;
	private int second;
	
	public void info(double throughput, int second){
		this.throughput = throughput;
		this.second = second;
	}

	private void sendTh() throws IOException{
		//Send th every second
		Seep.Statistics.Builder stat = Seep.Statistics.newBuilder();
		stat.setOpId(opId);
		//static variables at nodeMonitor
		stat.setTime(second);
		stat.setTh(throughput);
		stat.setCpuU(-1);
		try{
			(stat.build()).writeDelimitedTo(out);
		}
		catch(IOException io){
			System.out.println("MONITOR: While sending time,th: "+io.getMessage());
			io.printStackTrace();
		}
	}

/// \todo {this uses a seep.proto message that could be put in another place}
//	private void sendStatistics() throws IOException{
//		String command = "scripts/cpuUOld";
//		Process c = null;
//		//Gather required metrics
//		c = (Runtime.getRuntime().exec(command));
//		isr = new InputStreamReader(c.getInputStream());
////		Sleep
//		br = new BufferedReader(isr);
//		String line = br.readLine();
//		double cpuU = Double.parseDouble(line);
//		System.out.println("####");
//		System.out.println("OP: "+opId+" CPU_U: "+cpuU);
//		//Send metrics
//		Seep.Statistics.Builder stat = Seep.Statistics.newBuilder();
//		List<Integer> ops = new ArrayList<Integer>(NodeManager.mapOP_ID.keySet());
//		Integer opIds[] = ops.toArray(new Integer[0]);
//		opId = opIds[0];
//		stat.setOpId(opId);
//		stat.setCpuU(cpuU);
//		(stat.build()).writeDelimitedTo(out);
//		for(PlacedOperator op : ((NodeManager.mapOP_ID.get(opIds[0]))).getOpContext().downstreams){
//			int opId = op.opID();
//			if ((OperatorContext.downstreamBuffers.get(opId)) != null) 
//				System.out.println("OP:"+opId+" BUF: "+(OperatorContext.downstreamBuffers.get(opId)).size());
//		}
//		//Sleep
//		c.destroy();
//	}

	private void sendMonitorInfo() throws IOException{
		//Gather required metrics
//		String line = br.readLine();
//System.out.println("READED: "+line);
//		if(line == null || line.equals("")) return;
//		double cpuU = Double.parseDouble(line);
//System.out.println("TO READ");
		sourceFile = new RandomAccessFile(Main.valueFor("fileWithCpuU"), "r");
		String line = sourceFile.readLine();
		if(line.equals("")) return;
		double cpuU = Double.parseDouble(line);
//System.out.println("READED: "+cpuU);
		if(cpuU == 0) return;
		sourceFile.close();
//		System.out.println("####");
//		System.out.println("OP: "+opId+" CPU_U: "+cpuU);
		//Send metrics
		Seep.Statistics.Builder stat = Seep.Statistics.newBuilder();
		List<Integer> ops = new ArrayList<Integer>(NodeManager.mapOP_ID.keySet());
		Integer opIds[] = ops.toArray(new Integer[0]);
		opId = opIds[0];
		stat.setOpId(opId);
		stat.setCpuU(cpuU);
		(stat.build()).writeDelimitedTo(out);
//		if(!NodeManager.monitorOfSink){
//			for(PlacedOperator op : ((NodeManager.mapOP_ID.get(opIds[0]))).getOpContext().downstreams){
//				int opId = op.opID();
//				if ((OperatorContext.downstreamBuffers.get(opId)) != null) 
//					System.out.println("OP:"+opId+" BUF: "+(OperatorContext.downstreamBuffers.get(opId)).size());
//			}
//		}
	}


	public void run(){
		boolean listen = true;
		try{
			addrMon = InetAddress.getByName(Main.valueFor("mainAddr"));
			System.out.println("MONITOR-> conn ip: "+addrMon.toString()+" port: "+portMon);
			conn = new Socket(addrMon, portMon);
			out = conn.getOutputStream();
/** OPT2 **/
//			sourceFile = new RandomAccessFile(ExecutionConfiguration.fileWithCpuU, "r");
//			sourceFile.seek(0);
/** OPT2 **/
//			br = new BufferedReader(new FileReader(f));
//			br.mark(10);
			System.out.println("######################");
			System.out.println("######################");
//			System.out.println("FILE OPENING: "+f.getAbsolutePath());
			System.out.println("######################");
			System.out.println("######################");
			int times = 0;
			
			int sleepInterval = 1000*(Integer.parseInt(Main.valueFor("monitorInterval"))-1);
			
			while(listen){
				//Every second
//				if(NodeManager.monitorOfSink){
////					System.out.println("HERE////");
//					sendTh();
//				}
				Thread.sleep(sleepInterval);
				//times++;
				//Every second*monitorInterval
				//if(times == (ExecutionConfiguration.monitorInterval-1)){
					sendMonitorInfo();
					//times = 0;
				//}
			}
			conn.close();
		}
		catch(IOException io){
			System.out.println("When trying to connect to the MonitorManager: "+io.getMessage());
		}
		catch(InterruptedException ie){
			System.out.println("When trying to sleep: "+ie.getMessage());
		}
	}
}
