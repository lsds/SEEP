package uk.co.imperial.lsds.seep.infrastructure.monitor;

public class LocalReporterMonitor implements Runnable{

	@Override
	public void run() {
		// Time control variables (for local output)
		long init = 0;
		int sec = 0;
		
		while(true){
			//Local output info
			long elapsed = (System.currentTimeMillis() - init);
			if(elapsed > 1000){
				System.out.println("& "+sec+" "+MetricsReader.eventsProcessed.getCount());
				System.out.println("BUF: "+MetricsReader.loggedEvents.getCount());
				sec++;
				init = System.currentTimeMillis();
				MetricsReader.eventsProcessed.clear();
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
