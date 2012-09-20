package seep.operator.collection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import seep.Main;
import seep.comm.serialization.DataTuple;
import seep.comm.tuples.Seep;
import seep.operator.StatelessOperator;
import seep.comm.tuples.Seep.DataTuple.Builder;

@SuppressWarnings("serial")
public class WordSrc extends Src implements StatelessOperator{

	//test purposes
	int counter = 0;
	//int n_events = ExecutionConfiguration.nEventsToMeasure;
	long t_start = 0;
	//finish test purposes

	public WordSrc(int opID) {
		super(opID);
		subclassOperator = this;
		
	}
	
	 
	public void processData(DataTuple dt) {
//		boolean listen = true;
//		String toSend = "";
//		try{
//			/*
//			// hacker crackdown examples
//			File f = new File("theHackerCrackdown.txt");
//			BufferedReader br = new BufferedReader(new FileReader(f));
//			String line = null;
//			line = br.readLine();
//			while(line != null){
//				toSend = toSend.concat(line);
//				line = br.readLine();
//			}
//			br.close();
//			*/
//			String aux = "42397239 ]#'][#'][#']``[`' 54][34fdsl0 2154-55-*/ 3489kldf348egjk /*8956547945562121346 z9z9z9z9 zyzyzyzy wwwwwwww ywywywyw yyyyyyyy zzzzzzzz";
//			/*
//			TEST, size of this sentence in bytes.
//			*/
//			byte[] bytes = null;
//			bytes = aux.getBytes();
//			int bits = bytes.length * 8;
//			// silly line..
//			Integer.parseInt(Main.valueFor("sentenceSize"));
//
//			int index = 0;
//			int rateCounter = 0;
//			t_start = System.currentTimeMillis();
//			
//			Seep.DataTuple.Builder event = dt.toBuilder();
//			
//			System.out.println("SIZE AUX: "+aux);
//			
//			
//			while(listen){
//				rateCounter++;
//				Seep.DataTuple.Builder tuple = Seep.DataTuple.newBuilder();
//				tuple.setTs(System.currentTimeMillis());
///* BOOK EXAMPLE */
//				/*
//				//Data is retrieved from memory
//				int newIndex = toSend.indexOf(".", index);
//				if(newIndex < 0){
//					System.out.println("FILE PARSED");
//					index = 0;
//					newIndex = toSend.indexOf(".", index);
//				}
//				aux = toSend.substring(index, newIndex);
//				index = (newIndex+1);
//				*/
///*end book*/
//				/** FOR ROUTING, BEFORE REFACTORING THIS IS NECESSARY **/
//				tuple.setInt(0);
//				tuple.setString(aux);
//				//System.out.println("TESTSOURCE: sending sentence: "+aux);
//				//System.out.println();
//		
//				sendDown(tuple.build());
//				//test purposes
//				if(rateCounter == Main.eventR){
//					int sleepTime = (int) (Main.period - (System.currentTimeMillis() - t_start));
//					if(!Main.maxRate){
//						if(sleepTime < 0){
//							System.out.println("WARNING: Processing "+Main.eventR+" took more than "+Main.period+" ms");
//						}
//						else{
//							Thread.sleep(sleepTime);
//						}
//					}
//					//rate in kbps
//					int rate = (rateCounter * bits)/1000;
//					int es = rateCounter;
//					System.out.println("Rate: "+rate+" kbps,  E/S: "+rateCounter);
////System.out.println("sleeped "+sleepTime+" ms");
//					//System.out.println("SRC: "+(System.currentTimeMillis()-t_start)+" ms for "+ExecutionConfiguration.eventR+" events");
//					t_start = System.currentTimeMillis();
//					rateCounter = 0;
//				}
//				//finish test purposes
//			}
//		}
//	/*	catch(IOException io){
//			System.out.println("ERROR in TestSource IOException "+io.getMessage());
//			io.printStackTrace();
//		} */
//		catch(InterruptedException ie){
//			System.out.println("ERROR in TestSource INTERRUPTEDEXC "+ie.getMessage());
//			ie.printStackTrace();
//		}
	}
}
