package uk.ac.imperial.lsds.seepworker.core;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.junit.Test;

public class ByteComparisonOverheadToChooseDataStructurePerformanceTest {

	private BlockingQueue<DataS> single;
	private BlockingQueue<List<DataS>> list;
	
	@Test
	public void test() {
		// initialize structures
		int size = 8000000;
		single = new ArrayBlockingQueue<>(size);
		list = new ArrayBlockingQueue<>(size);
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
		
		
		/** Intertwine different access methods **/
		OneAdapter oa = new OneAdapter();
		Umbrella u = new OneAdapter();
		Umbrella u2 = new ManyAdapter();
		
		long startone = System.currentTimeMillis();
		// Get all from OneAdapter
		
		int dummy = 0;
		for(int i = 0; i< size; i++){
			DataS d = oa.getOne();
			dummy = d.a + d.b;
		}
		dummy = dummy/2;
		
		long stopone= System.currentTimeMillis();
		
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
		
		long startmany = System.currentTimeMillis();
		
		// Get all from Umbrella
		
		dummy = 0;
		for(int i = 0; i < size; i++){
			
			if(u.type() == 0){
				
				DataS d = u.getOne();
				dummy = d.a + d.b;
			}
			else if(u.type() == 1){
				
			}
		}
		dummy = dummy/2;
		
		long stopmany = System.currentTimeMillis();
		
		System.out.println("Time for DIRECT access: "+(stopone-startone));
		System.out.println("Time for COMPARISON access: "+(stopmany-startmany));
		
		assertTrue(true);
	}
	
	
	interface Umbrella{
		int type();
		DataS getOne();
		List<DataS> getList();
	}
	
	class OneAdapter implements Umbrella{
		
		int type = 0;
		
		public int type(){
			return type;
		}
		
		@Override
		public DataS getOne() {
			try {
				return single.take();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public List<DataS> getList() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	class ManyAdapter implements Umbrella {

		int type = 1;
		
		public int type(){
			return type;
		}
		
		@Override
		public DataS getOne() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<DataS> getList() {
			try {
				return list.take();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
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
