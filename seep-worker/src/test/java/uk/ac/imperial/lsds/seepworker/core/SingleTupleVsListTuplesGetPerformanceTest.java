package uk.ac.imperial.lsds.seepworker.core;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.junit.Test;

public class SingleTupleVsListTuplesGetPerformanceTest {

	private BlockingQueue<DataS> single;
	private BlockingQueue<List<DataS>> list;
	
	@Test
	public void test() {
		int size = 1000000;
		single = new ArrayBlockingQueue<>(size);
		list = new ArrayBlockingQueue<>(size);
		
		/** WRITE **/
		
		long startSingle = System.currentTimeMillis();
		// Full single
		for(int i = 0; i < size; i++){
			DataS d = new DataS(i, i+2, i+6, this, i/5);
			try {
				single.put(d);
			} 
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		long stopSingleStartList = System.currentTimeMillis();
		
		// Full list
		for(int i = 0; i < size; i++){
			DataS d = new DataS(i, i+2, i+6, this, i/5);
			ArrayList<DataS> l = new ArrayList<DataS>();
			l.add(d);
			try {
				list.put(l);
			} 
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		long stopList = System.currentTimeMillis();
		
		System.out.println("Time to WRITE single: "+(stopSingleStartList - startSingle));
		System.out.println("Time to WRITE list: "+(stopList - stopSingleStartList));
		
		/** READ **/
		startSingle = System.currentTimeMillis();
		int cum = 0;
		for(int i = 0; i < size; i++){
			DataS s = null;
			try {
				s = single.take();
			} 
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(s.d.equals(this)){
				cum = cum + s.a + s.c + s.b;
			}
			else{
				cum = cum + s.b * 8;
			}
		}
		stopSingleStartList = System.currentTimeMillis();
		for(int i = 0; i < size; i++){
			List<DataS> l = null;
			try{
				l = list.take();
			}
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			DataS s = l.get(0);
			
			if(s.d.equals(this)){
				cum = cum + s.a + s.c + s.b;
			}
			else{
				cum = cum + s.b * 8;
			}
		}
		stopList = System.currentTimeMillis();
		
		System.out.println("Time to READ single: "+(stopSingleStartList - startSingle));
		System.out.println("Time to READ list: "+(stopList - stopSingleStartList));
		
		assertTrue(true);
	}
	
	class DataS{
		public int a;
		public int b;
		public int c;
		public Object d;
		public float e;
		
		public DataS(int a, int b, int c, Object d, float e){
			this.a = a;
			this.b = b;
			this.c = c;
			this.d = d;
			this.e = e;
		}
	}

}
