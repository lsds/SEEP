package uk.ac.imperial.lsds.seep.manet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE;

public class LinkCostWorker implements Runnable {

	private final Logger log = LoggerFactory.getLogger(LinkCostWorker.class);
	private final String linkPainterAddr = GLOBALS.valueFor("linkPainterAddr");
	private final int linkPainterPort = Integer.parseInt(GLOBALS.valueFor("linkPainterPort"));
	private final String charSet = GLOBALS.valueFor("linkCostCharSet");
	private final CoreRE owner;
	private final Socket inputSocket;
	private boolean goOn = true;

	public LinkCostWorker(CoreRE owner, Socket inputSocket)
	{
		this.owner = owner;
		this.inputSocket = inputSocket;
	}

	@Override
	public void run()
	{
		//Read link cost update
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(inputSocket.getInputStream(), charSet));
		} catch (UnsupportedEncodingException e) {
			log.error("Error opening link cost reader: ",e); System.exit(1);
		} catch (IOException e) {
			log.error("Error opening link cost reader: ",e); System.exit(1);
		}


		try {
			//Read the connection to get the data
			while(goOn){
				String line = reader.readLine();
				if (line != null)
				{
					log.error("Link cost worker received line:"+ line);
				}
				else { break; }
			}
		} catch (IOException e) {
			log.error("Unexpected exception reading link cost update: ",e); System.exit(1);
		}

		try
		{
			inputSocket.close();
		}
		catch(IOException e){
			log.error("Exn closing link cost socket:", e);
		}

		if (!goOn) return;
		//Log it

		//TODO: Compute lowest cost path and next hop to the destination.
		//Notify the CORE EMANE link cost painter to do something.
		//TODO: notify the CORE EMANE link cost painter with (local node id -> next hop id)
		//TODO: update the local router with the lowest cost next hop.
	}

	public void setGoOn(boolean goOn) {
		this.goOn = goOn;
	}
}
