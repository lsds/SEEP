package seep.buffer;


import seep.Main;
import seep.comm.tuples.*;
import seep.utils.*;

import java.io.*;
import java.net.*;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

/**
* StateReplayer. This runnable object is in charge of replaying the state when necessary.
*/

public class StateReplayer implements Runnable {

//	private Socket socket;
	private Buffer buffer;
	private Socket controlDownstreamSocket = null;

	private AtomicBoolean replay;
	private AtomicBoolean stop;

	private Iterator<Seep.DataTuple> sharedIterator;

	public StateReplayer(CommunicationChannel oi){
//		this.socket = oi.downstreamSocketD;
		this.controlDownstreamSocket = oi.downstreamControlSocket;
		this.buffer = oi.getBuffer();
		this.replay = oi.getReplay();
		this.stop = oi.getStop();
	}

	public void run(){
//		OutputStream out = null;
//		try{
//			out = socket.getOutputStream();
//		}
//		catch(IOException io){
//			System.out.println("REPLAYER, while getting outputstreamm (WEIRD) "+io.getMessage());
//		}

/// \todo {this block of code should not be ft model dependant}
		if(!Main.valueFor("ftmodel").equals("twitterStormModel")){
			//Get a proper init state and just send it
			Seep.ControlTuple.Builder ctB = Seep.ControlTuple.newBuilder();

			Seep.InitState state = null;
			Seep.BackupState bs = buffer.getBackupState();
			//if state is null (upstream backup or new model at the start)
			if (bs != null) {
				Seep.InitState.Builder isB = Seep.InitState.newBuilder();
				//Ts of init state is the newest ts of the checkpointed state
				isB.setTs(bs.getTsE());
				//This line is specially important, since each message state has a different name in the proto.file
/// \todo {this operator specific line must be avoided}
				//System.out.println("WORD COUNTER????");
				//isB.setWcState(bs.getWcState());
				System.out.println("LRB????");
				isB.setTcState(bs.getTcState());
				state = isB.build();
				
				ctB.setInitState(state);
			}
			else {
				System.out.println("Replayer: sending empty state");
			}
			ctB.setType(Seep.ControlTuple.Type.INIT_STATE);
			try{
				System.out.println("STATEREPLAYER: State sent to OP: "+(controlDownstreamSocket.toString()));
				//If there is a state, send it ALWAYS
				if(state != null){
					synchronized(controlDownstreamSocket){
						(ctB.build()).writeDelimitedTo(controlDownstreamSocket.getOutputStream());
					}
				}
				//If there is no state, send the empty state just if the ft model is not new model. In new model someone else is sending a state.
				else{
					if(!Main.valueFor("ftmodel").equals("newModel")){
						synchronized(controlDownstreamSocket){
							(ctB.build()).writeDelimitedTo(controlDownstreamSocket.getOutputStream());
						}
					}
				}
			}
			catch(IOException io){
				System.out.println("REPLAYER: Error while trying to send the InitState msg: "+io.getMessage());
			}
		}
	}
}
