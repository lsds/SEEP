package seep.infrastructure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class EC2Worker implements Runnable{

	private Infrastructure inf = null;
	
	public EC2Worker(Infrastructure inf){
		this.inf = inf;
	}
	
	@Override
	public void run() {
		InputStreamReader isr = null;
		BufferedReader br = null;
		String command = "scripts/amazonEC2NewMachine";
		Process c = null;
		//Gather required metrics
		try {
			c = (Runtime.getRuntime().exec(command));
		
		isr = new InputStreamReader(c.getInputStream());
		br = new BufferedReader(isr);
		String line = br.readLine();
		
		c.destroy();
		} 
		catch (IOException e) {
			
			e.printStackTrace();
		}
		// instantiate a new amazon machine
		// start new amazon machine and get address
		// wait until available
		// upload necessary files
		// ssh and execute system secondary
		// return
	}

}
