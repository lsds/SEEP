//package seep.buffer;
//
//
//import java.io.IOException;
//import java.net.Socket;
//
//import seep.comm.serialization.ControlTuple;
//import seep.comm.serialization.controlhelpers.BackupOperatorState;
//import seep.comm.serialization.controlhelpers.InitState;
//import seep.runtimeengine.CommunicationChannel;
//
///**
//* StateReplayer. This runnable object is in charge of replaying the state when necessary.
//*/
//
//public class StateReplayer implements Runnable {
//
////	private Socket socket;
//	private Buffer buffer;
//	private Socket controlDownstreamSocket = null;
//
//	public StateReplayer(CommunicationChannel oi){
//		this.controlDownstreamSocket = oi.getDownstreamControlSocket();
//		this.buffer = oi.getBuffer();
//	}
//
//	public void run(){
//		//Get a proper init state and just send it
//		ControlTuple ctB = new ControlTuple();
//
//		InitState state = null;
//		BackupOperatorState bs = buffer.getBackupState();
//		//if state is null (upstream backup or new model at the start)
//		if (bs != null) {
//			InitState isB = new InitState();
//			//Ts of init state is the newest ts of the checkpointed state
//			isB.setTs(bs.getTs_e());
//			//This line is specially important, since each message state has a different name in the proto.file
///// \todo {this operator specific line must be avoided}
//			//System.out.println("WORD COUNTER????");
//			//isB.setWcState(bs.getWcState());
//			System.out.println("LRB????");
//			isB.setState(bs.getState());
//			state = isB;
////			ctB.setBackupState(state);
//		}
//		else {
//			System.out.println("Replayer: sending empty state");
//		}
////		ctB.setType(Operator.ControlTupleType.INIT_STATE);
////		try{
////			System.out.println("STATEREPLAYER: State sent to OP: "+(controlDownstreamSocket.toString()));
////			//If there is a state, send it ALWAYS
////			if(state != null){
////				synchronized(controlDownstreamSocket){
//////					(ctB.build()).writeDelimitedTo(controlDownstreamSocket.getOutputStream());
////				}
////			}
////		}
////		catch(IOException io){
////			System.out.println("REPLAYER: Error while trying to send the InitState msg: "+io.getMessage());
////		}
//	}
//}
