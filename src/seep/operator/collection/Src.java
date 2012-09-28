package seep.operator.collection;

import seep.operator.*;

import seep.comm.serialization.DataTuple;

public class Src extends Operator{


	//test purposes
	int counter = 0;
	int n_events = 100000;
	long t_start = 0;
	long t_end = 0;
	//finish test purposes

	public Src(int opID){
		super(opID);
	}
	
	public void processData(DataTuple dt){
//		//This while is here to simulate a real source.
//		boolean listen = true;
//		int value = 0;
//		while(listen){
//			Seep.DataTuple.Builder tuple = Seep.DataTuple.newBuilder();
//			tuple.setTs(System.currentTimeMillis());
//			//Data generated is an incrementing value...
////			tuple.setInt(value++);
//			sendDown(tuple.build());
////			System.out.println("TESTSOURCE: sending: ts->"+tuple.getTs()+" value->"+tuple.getInt());
//			//System.out.println();
//
//			counter++;
//			if(counter == n_events){
//				t_end = System.currentTimeMillis();
//				System.out.println("SRC: "+(t_end-t_start)+" ms for "+n_events+" events");
//				t_start = System.currentTimeMillis();
//				counter = 0;
//			}
//
//			try{
//				Thread.sleep(1000);
//			}
//			catch(InterruptedException ie){
//				System.out.println("SOURCE EXCEPTION: "+ie.getMessage());
//			}	
//		}
	}

	@Override
	public boolean isOrderSensitive() {
		// TODO Auto-generated method stub
		return false;
	}
}
