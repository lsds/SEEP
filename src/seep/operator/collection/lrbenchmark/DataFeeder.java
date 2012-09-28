package seep.operator.collection.lrbenchmark;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

import seep.Main;
import seep.comm.serialization.DataTuple;
import seep.operator.Operator;
import seep.operator.StatelessOperator;

@SuppressWarnings("serial")
public class DataFeeder extends Operator implements StatelessOperator{

	private File inputFile = new File(Main.valueFor("pathToInputFile"));
	private String[] buffer = new String[100000];
	
	private DataTuple[] bufferEvents = null;
	
	
	//OPTION 4
	private File gpbInput = new File(Main.valueFor("pathToOutputFile"));
	private File gpbInputConstant = new File(Main.valueFor("pathToOutputFileConstant"));
	
	
	public DataFeeder(int opID) {
		super(opID);
		subclassOperator = this;
	}	
	
	
	public void processData(DataTuple dt){
//		boolean listen = true;
//		long eventTime = 0;
//		long t_start = System.currentTimeMillis();
//		FileInputStream fis = null;
//		try{
//			BufferedInputStream bis = null;
//			
//			if(Main.valueFor("normalLRB").equals("true")){
//				bis = new BufferedInputStream(new FileInputStream(gpbInput), 8000000);
//			}
//			else{
//				bis = new BufferedInputStream(new FileInputStream(gpbInputConstant), 8000000);
//			}
//			
//			int counter = 0;
//			long s = System.currentTimeMillis();
//			
//			Seep.DataTuple event = null;
//			long clock = 0;
//			long systemTs = 0;
//
//			int dist = 0;
//
//			while(listen){
//
//				systemTs = System.currentTimeMillis();
//				clock = systemTs-t_start;
//				if(eventTime*1000 <= clock){
//					event = Seep.DataTuple.parseDelimitedFrom(bis);
//					if(Main.valueFor("normalLRB").equals("true")){
//						//Get timestamps every numberOfXways iterations to have a accurate ts
//						systemTs = System.currentTimeMillis();
////						int ab = 0;
//						for(int i = 0; i<Main.numberOfXWays; i++){
////							ab++;
//							//get ts every 100 iterations to reduce overhead
//							Seep.DataTuple.Builder builder = event.toBuilder();
//							builder.setXway(i);
//							builder.setTs(systemTs);
//							event = builder.build();
//	
//							eventTime = event.getTime();
//						
//							sendDown(event);
//							counter++;
////							if(ab == ExecutionConfiguration.numberOfXWays/2){
////								systemTs = System.currentTimeMillis();
////							}
//						}
//					}
//					else{
//						for(int i = 0; i<Main.eventR; i++){
//							Seep.DataTuple.Builder builder = event.toBuilder();
//							builder.setXway(i);
//							builder.setTs(System.currentTimeMillis());
//							event = builder.build();
//	
//							eventTime = event.getTime() - 5000;
//						
//							sendDown(event);
//							counter++;
//						}
//					}
//				}
//				else{
//					Thread.sleep(eventTime*1000 - clock);
//					System.out.println(eventTime+" "+counter);
//					counter = 0;
//				}
//			}
//		}
//		catch(IOException io){
//			io.printStackTrace();
//		}
//		catch (InterruptedException e) {
//			e.printStackTrace();
//		}
	}

	
	public boolean isOrderSensitive() {
		// TODO Auto-generated method stub
		return false;
	}

}
	
//	private Seep.DataTuple.Builder buildDataTuple(String event){
//		Seep.DataTuple.Builder tuple = Seep.DataTuple.newBuilder();
//		String fields[] = event.split(",");
//		tuple.setType(Integer.parseInt(fields[0]));
//		if(tuple.getType() == 3 || tuple.getType() == 4) return null;
//		tuple.setTime(Integer.parseInt(fields[1]));
//		tuple.setVid(Integer.parseInt(fields[2]));
//		tuple.setSpeed(Integer.parseInt(fields[3]));
//		tuple.setXway(Integer.parseInt(fields[4]));
//		tuple.setLane(Integer.parseInt(fields[5]));
//		tuple.setDir(Integer.parseInt(fields[6]));
//		tuple.setSeg(Integer.parseInt(fields[7]));
//		tuple.setPos(Integer.parseInt(fields[8]));
//		tuple.setQid(Integer.parseInt(fields[9]));
//		/*tuple.setSInit(Integer.parseInt(fields[10]));
//		tuple.setSEnd(Integer.parseInt(fields[11]));
//		tuple.setDow(Integer.parseInt(fields[12]));
//		tuple.setTow(Integer.parseInt(fields[13]));
//		tuple.setDay(Integer.parseInt(fields[14]));*/
//		return tuple;
//	}
//}


/*	public void processData(Builder dt) {
//		if(f){
//			f = false;
//			t = System.currentTimeMillis();
//	}
		boolean listen = true;
		boolean fileParsed = false;
		long t_start = 0;
		long eventTime = 0;
		try {		
			BufferedReader br = new BufferedReader(new FileReader(inputFile));
			int pointer = 0;
			fillBuffer(br);
			//fillBufferTuple(br);
			int counter = 0;
			t_start = System.currentTimeMillis();
			long init = t_start;
			String aux = null;
			byte line[] = new byte[48];
			int index = 0;
	//MEMORY MAP FILE AND NIO		
			/*int offset = 0;
			FileChannel roChannel = new RandomAccessFile(inputFile, "r").getChannel();
			//lets try with memory mapping one gb
			int size = 1024*8*1024;
			//ByteBuffer roBuf = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, (int)roChannel.size());
			int type = -1;
			*/
	//SCANNER
			
			//Scanner scan = new Scanner(inputFile).useDelimiter(","); 
			
	
	//MEM MAP
			//while(listen){
	
			
				//if(eventTime <= (System.currentTimeMillis()-t_start)/1000){
					
		/*			Seep.DataTuple.Builder tuple = Seep.DataTuple.newBuilder();
					type = scan.nextInt();
					tuple.setType(type);
		//	System.out.println("TYPE: "+tuple.getType());
					eventTime = scan.nextLong();
					tuple.setTime(eventTime);
					tuple.setVid(scan.nextInt());
			//System.out.println("VID: "+tuple.getVid());
					tuple.setSpeed(scan.nextInt());
					tuple.setXway(scan.nextInt());
			//System.out.println("XWAY: "+tuple.getXway());
					tuple.setLane(scan.nextInt());
					tuple.setDir(scan.nextInt());
					tuple.setSeg(scan.nextInt());
					tuple.setPos(scan.nextInt());
					tuple.setQid(scan.nextInt());
					tuple.setSInit(scan.nextInt());
					tuple.setSEnd(scan.nextInt());
					tuple.setDow(scan.nextInt());
					tuple.setTow(scan.nextInt());
		//	System.out.println("TOW: "+tuple.getTow());
					tuple.setDay(scan.nextInt());
//System.out.println("T: "+tuple);
					if(! (eventTime <= (System.currentTimeMillis()-t_start)/1000)){
						Thread.sleep((int)(eventTime*1000) - ((System.currentTimeMillis()-t_start)));
						System.out.println("eT: "+eventTime);
					}
					if(type != 3 && type != 4)
						sendDown(tuple);
				}
			}
		
		}*/
				
	//READ TEXT, PARSE MANUALLY, ALLOCATE ALL INITIALLY
	/*			String time = null;
				//int index = 0;
				if(eventTime <= (System.currentTimeMillis()-t_start)/1000){
					System.out.println("eT: "+eventTime);
					for(int i = pointer; i < bufferEvents.length; i++){
						eventTime = bufferEvents[i].getTime();
						if(eventTime <= (System.currentTimeMillis()-t_start)/1000){
							emitEvent(buffer[i]);
							counter++;
						}
						else{
							Thread.sleep((eventTime*1000) - ((System.currentTimeMillis()-t_start)));
						}
					}
				}
				else{
					Thread.sleep((eventTime*1000) - ((System.currentTimeMillis()-t_start)));
				}
			}
			br.close();
		}
	*/
		//MANUAL PARSING FROM FILE
			//counter = 0;
	/*			String time = null;
				//int index = 0;
				if(eventTime <= (System.currentTimeMillis()-t_start)/1000){
					System.out.println("eT: "+eventTime);
					boolean control = false;
					for(int i = pointer; i < buffer.length; i++){
						control = true;
						if((aux = buffer[i]) != null){
							// get event time
long h = System.currentTimeMillis();
							index = aux.indexOf(",", (aux.indexOf(",")+1));
							time = aux.substring(2, index);
							eventTime = Long.parseLong(time);
long hh = System.currentTimeMillis();
if((hh-h) > 5){
	System.out.print("INDEX: "+(hh-h));
}
							if(eventTime <= (System.currentTimeMillis()-t_start)/1000){
								emitEvent(aux);
								counter++;
								//System.out.println(buffer[i]);
							}
							else{
								//control = false;
								//pointer = i;
								//break;
								Thread.sleep((eventTime*1000) - ((System.currentTimeMillis()-t_start)));
							}
						}
						else{
							//file parsed, end data feeder.
							System.out.println("BREAKING the LOOP, FILE PARSED");
							listen = false;
							break;
						}
					}
					fileParsed = fillBuffer(br);
					if(fileParsed){
						listen = false;
						break;
					}
					
				}
				else{
					Thread.sleep((eventTime*1000) - ((System.currentTimeMillis()-t_start)));
				}
				
			}
			
			br.close();
		
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch(IOException io){
			io.printStackTrace();
		}
	/*	catch(InterruptedException ie){
			ie.printStackTrace();
		}
	 catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/

	/*private void fillBufferTuple(BufferedReader br) throws IOException{
		int bufferSize = countFileLines(br);
		System.out.println("Lines in FILE: "+bufferSize);
		br = new BufferedReader(new FileReader(inputFile));
		String aux = null;
		Seep.DataTuple.Builder event = null;
		bufferEvents = new Seep.DataTuple.Builder[bufferSize];
		for(int i = 0; i < bufferEvents.length; i++){
			aux = br.readLine();
			event = buildDataTuple(aux);
			if(event != null){
				System.out.println("TUPLE: "+i);
				bufferEvents[i] = event;
			}
		}
	}*/

//	private int countFileLines(BufferedReader br){
//		String aux = null;
//		int counter = 0;
//		try {
//			while((aux = br.readLine()) != null){
//				counter++;
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return counter;
//	}
//	
//	private boolean fillBuffer(BufferedReader br) {
//long a = System.currentTimeMillis();
//		String aux = null;
//		boolean fileParsed = false;
//		//Reinitialize buffer every time is filled
//		buffer = null;
//		buffer = new String[10000];
//		for(int i = 0; i < buffer.length; i++){
//			try {
//				if((aux = br.readLine()) != null){
//					buffer[i] = aux;
//				}
//				else{
//					fileParsed = true;
//					System.out.println("FILE PARSED");
//					break;
//				}
//			} 
//			catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//long e = System.currentTimeMillis();
//System.out.println("fillBUF: "+(e-a));
//		return fileParsed;
//	}
	
	
//private void emitEvent(String event) {
//Seep.DataTuple.Builder tuple = buildDataTuple(event);
//if(tuple != null)
//	sendDown(tuple.build());
//}

//int index = 0;
//FileChannel roChannel = new RandomAccessFile(inputFile, "r").getChannel();
//lets try with memory mapping one gb
//ByteBuffer roBuf = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, (1<<30));

//System.out.println("LINE: "+aux);
/*	Seep.DataTuple.Builder tuple = Seep.DataTuple.newBuilder();
	tuple.setType(roBuf.getInt(index));
	System.out.println("TYPE: "+tuple.getType());
	eventTime = roBuf.getInt(index+5);
	tuple.setTime(eventTime);
	tuple.setVid(roBuf.getInt(index+10));
	System.out.println("VID: "+tuple.getVid());
	tuple.setSpeed(roBuf.getInt(index+15));
	tuple.setXway(roBuf.getInt(index+20));
	System.out.println("XWAY: "+tuple.getXway());
	tuple.setLane(roBuf.getInt(roBuf+25));
	tuple.setDir(roBuf.getInt(index+30));
	tuple.setSeg(roBuf.getInt(index+35));
	tuple.setPos(roBuf.getInt(index+40));
	tuple.setQid(roBuf.getInt(index+45));
	tuple.setSInit(roBuf.getInt(index+50));
	tuple.setSEnd(roBuf.getInt(index+55));
	tuple.setDow(roBuf.getInt(index+60));
	tuple.setTow(roBuf.getInt(index+65));
	System.out.println("TOW: "+tuple.getTow());
	tuple.setDay(roBuf.getInt(index+70));
	// get event time
	if(eventTime <= (System.currentTimeMillis()-t_start)/1000){
		sendDown(tuple);
	}
	else{
		System.out.println("eT: "+eventTime);
		Thread.sleep((eventTime*1000) - ((System.currentTimeMillis()-t_start)));
	}
	//next line
	index = index+71;
*/

/*			Seep.DataTuple.Builder tuple = Seep.DataTuple.newBuilder();
type = (int)roBuf.getInt(index);
tuple.setType(type);
System.out.println("TYPE: "+tuple.getType());
eventTime = roBuf.getInt(index+5);
tuple.setTime(eventTime);
tuple.setVid(roBuf.getInt(index+10));
System.out.println("VID: "+tuple.getVid());
tuple.setSpeed(roBuf.getInt(index+15));
tuple.setXway(roBuf.getInt(index+20));
System.out.println("XWAY: "+tuple.getXway());
tuple.setLane(roBuf.getInt(index+25));
tuple.setDir(roBuf.getInt(index+30));
tuple.setSeg(roBuf.getInt(index+35));
tuple.setPos(roBuf.getInt(index+40));
tuple.setQid(roBuf.getInt(index+45));
tuple.setSInit(roBuf.getInt(index+50));
tuple.setSEnd(roBuf.getInt(index+55));
tuple.setDow(roBuf.getInt(index+60));
tuple.setTow(roBuf.getInt(index+65));
System.out.println("TOW: "+tuple.getTow());
tuple.setDay(roBuf.getInt(index+70));
// get event time
if(eventTime <= (System.currentTimeMillis()-t_start)/1000){
	if(type != 3 && type != 4)
		sendDown(tuple);
}
else{
	System.out.println("eT: "+eventTime);
	Thread.sleep((eventTime*1000) - ((System.currentTimeMillis()-t_start)));
}
//next line
index = index+71;
}
*/




//boolean f = true;
//long t = 0;

/*	@Override
public void processData(Seep.DataTuple dt){
	boolean listen = true;
	long eventTime = 0;
	long t_start = System.currentTimeMillis();
	FileInputStream fis = null;
	try{
		//GZIPInputStream gZipIs = new GZIPInputStream(new FileInputStream(gpbInput));
		fis = new FileInputStream(gpbInput);
		bufferEvents = new Seep.DataTuple[100000];
		fillBuffer(fis);
		while(listen){
			if(eventTime <= (System.currentTimeMillis()-t_start)/1000){
				for(int i = 0; i< bufferEvents.length; i++){
					//Seep.DataTuple event = Seep.DataTuple.parseDelimitedFrom(fis);
					//eventTime = bufferEvents[i].getTime();
					if(eventTime <= (System.currentTimeMillis()-t_start)/1000){
						//System.out.println("EVENT: type-> "+event.getType()+" time-> "+event.getTime()+" xway-> "+event.getXway()+" vid: "+event.getVid());
						sendDown(bufferEvents[i]);
					}
				}
				fillBuffer(fis);
			}
			else{
				Thread.sleep((eventTime*1000) - ((System.currentTimeMillis()-t_start)));
				//System.out.println("eT: "+eventTime);
			}
		}
	}
	catch(IOException io){
		io.printStackTrace();
	}
	catch (InterruptedException e) {
		e.printStackTrace();
	}
}

private void fillBuffer(FileInputStream fis) {
	long aa = System.currentTimeMillis();
	for(int i = 0; i < bufferEvents.length; i++){
		try {
			bufferEvents[i] = Seep.DataTuple.parseDelimitedFrom(fis);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	long bb = System.currentTimeMillis() - aa;
	System.out.println("TIME to fill buf: "+bb);
}
*/
/** SCALABILITY EXPERIMENT **/
/*
ContentBasedFilter cbf = new ContentBasedFilter("getXway");
//subsystem in downstream 0 copes with 3L
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,0, 0);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,6, 0);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,12, 0);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,48, 0);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,49, 0);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,50, 0);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,51, 0);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,52, 0);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,53, 0);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,54, 0);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,55, 0);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,56, 0);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,57, 0);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,58, 0);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,59, 0);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,60, 0);
//subsystem in downstream 1 copes with 3L
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,1, 1);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,7, 1);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,13, 1);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,61, 1);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,62, 1);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,63, 1);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,64, 1);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,65, 1);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,66, 1);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,67, 1);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,68, 1);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,69, 1);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,70, 1);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,71, 1);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,72, 1);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,73, 1);
//subsystem in downstream 2 copes with 3L
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,2, 2);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,8, 2);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,14, 2);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,74, 2);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,75, 2);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,76, 2);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,77, 2);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,78, 2);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,79, 2);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,80, 2);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,81, 2);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,82, 2);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,83, 2);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,84, 2);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,85, 2);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,86, 2);
//subsystem in downstream 3 copes with 3L
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,3, 3);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,9, 3);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,15, 3);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,87, 3);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,88, 3);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,89, 3);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,90, 3);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,91, 3);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,92, 3);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,93, 3);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,94, 3);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,95, 3);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,96, 3);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,97, 3);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,98, 3);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,99, 3);
//subsystem in downstream 4 copes with 3L
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,4, 4);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,10, 4);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,16, 4);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,100, 4);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,101, 4);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,102, 4);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,103, 4);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,104, 4);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,105, 4);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,106, 4);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,107, 4);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,108, 4);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,109, 4);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,110, 4);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,111, 4);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,112, 4);
//subsystem in downstream 5 copes with 3L
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,5, 5);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,11, 5);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,17, 5);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,113, 5);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,114, 5);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,115, 5);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,116, 5);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,117, 5);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,118, 5);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,119, 5);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,120, 5);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,121, 5);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,122, 5);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,123, 5);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,124, 5);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,125, 5);
//subsystem in downstream 6 copes with 3L
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,18, 6);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,19, 6);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,20, 6);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,126, 6);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,127, 6);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,128, 6);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,129, 6);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,130, 6);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,131, 6);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,132, 6);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,133, 6);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,134, 6);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,135, 6);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,136, 6);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,137, 6);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,138, 6);
//subsystem in downstream 7 copes with 3L
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,21, 7);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,22, 7);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,23, 7);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,139, 7);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,140, 7);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,141, 7);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,142, 7);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,143, 7);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,144, 7);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,145, 7);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,146, 7);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,147, 7);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,148, 7);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,149, 7);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,150, 7);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,151, 7);
//subsystem in downstream 8 copes with 3L
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,24, 8);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,32, 8);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,40, 8);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,152, 8);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,153, 8);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,154, 8);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,155, 8);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,156, 8);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,157, 8);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,158, 8);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,159, 8);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,160, 8);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,161, 8);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,162, 8);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,163, 8);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,164, 8);
//cbf.routeValueToDownstream("getXway", RouteOperator.EQ,18, 0);
//subsystem in downstream 9 copes with 3L
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,25, 9);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,33, 9);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,41, 9);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,165, 9);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,166, 9);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,167, 9);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,168, 9);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,169, 9);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,170, 9);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,171, 9);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,172, 9);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,173, 9);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,174, 9);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,175, 9);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,176, 9);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,177, 9);
//cbf.routeValueToDownstream("getXway", RouteOperator.EQ,19, 1);
//subsystem in downstream 10 copes with 3L
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,26, 10);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,34, 10);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,42, 10);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,178, 10);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,179, 10);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,180, 10);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,181, 10);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,182, 10);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,183, 10);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,184, 10);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,185, 10);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,186, 10);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,187, 10);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,188, 10);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,189, 10);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,190, 10);
//cbf.routeValueToDownstream("getXway", RouteOperator.EQ,20, 2);
//subsystem in downstream 11 copes with 3L
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,27, 11);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,35, 11);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,43, 11);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,191, 11);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,192, 11);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,193, 11);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,194, 11);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,195, 11);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,196, 11);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,197, 11);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,198, 11);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,199, 11);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,200, 11);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,201, 11);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,202, 11);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,203, 11);
//cbf.routeValueToDownstream("getXway", RouteOperator.EQ,21, 3);
//subsystem in downstream 12 copes with 3L
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,28, 12);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,36, 12);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,44, 12);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,204, 12);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,205, 12);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,206, 12);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,207, 12);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,208, 12);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,209, 12);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,210, 12);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,211, 12);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,212, 12);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,213, 12);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,214, 12);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,215, 12);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,216, 12);

//cbf.routeValueToDownstream("getXway", RouteOperator.EQ,22, 4);
//subsystem in downstream 13 copes with 3L
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,29, 13);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,37, 13);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,45, 13);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,217, 13);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,218, 13);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,219, 13);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,220, 13);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,221, 13);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,222, 13);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,223, 13);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,224, 13);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,225, 13);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,226, 13);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,227, 13);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,228, 13);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,229, 13);
//cbf.routeValueToDownstream("getXway", RouteOperator.EQ,23, 5);
//subsystem in downstream 14 copes with 3L
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,30, 14);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,38, 14);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,46, 14);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,230, 14);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,231, 14);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,232, 14);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,233, 14);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,234, 14);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,235, 14);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,236, 14);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,237, 14);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,238, 14);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,239, 14);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,240, 14);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,241, 14);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,242, 14);
//subsystem in downstream 15 copes with 3L
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,31, 15);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,39, 15);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,47, 15);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,243, 15);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,244, 15);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,245, 15);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,246, 15);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,247, 15);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,248, 15);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,249, 15);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,250, 15);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,251, 15);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,252, 15);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,253, 15);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,254, 15);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,255, 15);
//queries of type 2 goes everywhere
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,-1, 0);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,-1, 1);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,-1, 2);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,-1, 3);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,-1, 4);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,-1, 5);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,-1, 6);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,-1, 7);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,-1, 8);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,-1, 9);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,-1, 10);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,-1,11);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,-1, 12);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,-1, 13);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,-1, 14);
cbf.routeValueToDownstream("getXway", RouteOperator.EQ,-1, 15);
setDispatchPolicy(DispatchPolicy.CONTENT_BASED, cbf);
*/
/** ELASTICITY EXPERIMENT **/

//ContentBasedFilter cbf = new ContentBasedFilter("getXway");
////subsystem in downstream 0 copes with 2L
//cbf.routeValueToDownstream( RouteOperator.EQ, 0, 0);
//cbf.routeValueToDownstream(RouteOperator.EQ, 1, 0);
//
////queries of type 2 goes everywhere
//cbf.routeValueToDownstream("getXway", RouteOperator.EQ, -1, 0);
//setDispatchPolicy(DispatchPolicy.CONTENT_BASED, cbf);
