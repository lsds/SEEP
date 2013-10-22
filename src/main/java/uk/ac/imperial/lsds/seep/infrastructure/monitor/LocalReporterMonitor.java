/*******************************************************************************
 * Copyright (c) 2013 Imperial College London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial design and implementation
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.infrastructure.monitor;

public class LocalReporterMonitor implements Runnable{

	private final int PERIOD = 1000;
	
	@Override
	public void run() {
		// Time control variables (for local output)
		long init = 0;
		int sec = 0;
		
		while(true){
			//Local output info
			long elapsed = (System.currentTimeMillis() - init);
			if(elapsed > PERIOD){
				System.out.println("& "+sec+" "+MetricsReader.eventsProcessed.getCount());
				System.out.println("BUF: "+MetricsReader.loggedEvents.getCount());
				sec++;
				init = System.currentTimeMillis();
				//MetricsReader.eventsProcessed.clear();
				MetricsReader.reset(MetricsReader.eventsProcessed);
			} 
			else
				try {
					Thread.sleep(1000-elapsed);
				} 
				catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
}
