package uk.ac.imperial.lsds.seep.operator.compose.multi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Monitor {
	
	public Monitor() {
	}
	
	private long lastPrint = 0;

	public void monitor(String s) {
		if (System.currentTimeMillis() - lastPrint > 1000) {
				System.out.println(System.currentTimeMillis() + " - " + s);
				lastPrint = System.currentTimeMillis();
			} 
	
	}

	
	public void monitor(String s, Object o, Method m) {
		if (System.currentTimeMillis() - lastPrint > 1000) {
			try {
				System.out.println(s + " " + m.invoke(o, new Object[0]).toString());
				lastPrint = System.currentTimeMillis();
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
