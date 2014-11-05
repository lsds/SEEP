package uk.ac.imperial.lsds.seepworker.core.input;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

public class IteratorVsPlainForLoopToAccessListPerformanceTest {

	@Test
	public void test() {
		List<String> list = new LinkedList<>();
		List<String> alist = new ArrayList<>();
		
		int size = 500000;
		// Full list
		for(int i = 0; i < size; i++){
			list.add(new Integer(i).toString());
			alist.add(new Integer(i).toString());
		}
		
		// total read with iterator
		Iterator<String> itl = list.iterator();
		Iterator<String> ital = alist.iterator();
		
		long startLIt = System.currentTimeMillis();
		while(itl.hasNext()){
			String a = itl.next();
			a = a + "a";
		}
		
		long stopLIt = System.currentTimeMillis();
		long startALIt = System.currentTimeMillis();
		
		while(ital.hasNext()){
			String a = ital.next();
			a = a + "a";
		}
		
		long stopALIt = System.currentTimeMillis();
		long startLFor = System.currentTimeMillis();
		
		for(int i = 0; i<size; i++){
			String a = list.get(i);
			a = a + "a";
		}
		
		long stopLFor = System.currentTimeMillis();
		long startALFor = System.currentTimeMillis();
		
		for(int i = 0; i<size; i++){
			String a = alist.get(i);
			a = a + "a";
		}
		
		long stopALFor = System.currentTimeMillis();
		
		System.out.println("IT linkedlist: "+(stopLIt-startLIt));
		System.out.println("IT arraylist: "+(stopALIt-startALIt));
		System.out.println("FOR linkedlist: "+(stopLFor-startLFor));
		System.out.println("FOR arraylist: "+(stopALFor-startALFor));
		
		assertTrue(true);
		
	}

}
