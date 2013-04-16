package seep.comm;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import seep.comm.serialization.DataTuple;
import seep.comm.serialization.messages.TuplePayload;
import seep.infrastructure.NodeManager;
import seep.runtimeengine.AsynchronousCommunicationChannel;

import com.esotericsoftware.kryo.io.ByteBufferInputStream;
import com.esotericsoftware.kryo.io.ByteBufferOutputStream;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class OutgoingDataHandlerWorker implements Runnable{

	// TX data
	private Selector selector;
	
	private boolean goOn = true;
	
	public OutgoingDataHandlerWorker(Selector selector){
		this.selector = selector;
	}
	
	@Override
	public void run() {
	
		while(goOn){
			try {
				
//System.out.println("first exec");
				// Check events
				selector.select();
//System.out.println(".");
				
				//Iterate on the events if any
				Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
//System.out.println("Selected key: "+selector.selectedKeys().toString());
//if(selector.selectedKeys().isEmpty()){
//	SelectionKey sk = (SelectionKey) (selector.keys().toArray())[0];
//System.out.println("selection keys: "+sk.toString());
//System.out.println("interest ops "+sk.interestOps());
//System.out.println("ready ops "+sk.readyOps());
//System.out.println("attachement "+((Output)sk.attachment()).toString());
//System.out.println("channel "+sk.channel().toString());
//System.exit(0);
//}
				while(selectedKeys.hasNext()){
					// We choose one key
					SelectionKey key = (SelectionKey) selectedKeys.next();
//System.out.println("first key: "+key.toString());
					selectedKeys.remove();
					
					// Sanity check
					if(!key.isValid()){
//System.out.println("NO VALID");
						continue;
					}
//System.out.println("VALID");
					// Check the write event
					if(key.isWritable()){
//System.out.println("WRITABLE");
						write(key);
//System.exit(0);
					}
//System.out.println("NO WRITABLE");
//System.exit(0);
				}
			}
			catch (IOException e) {
				NodeManager.nLogger.severe("-> While checking the selector events: "+e.getMessage());
				e.printStackTrace();
			}
		}
		System.out.println("######################################");
		System.exit(-1);
	}
	
	private void write(SelectionKey key){
		// Retrieve socket
		SocketChannel sc = (SocketChannel) key.channel();
		// And retrieve native ByteBuffer
		AsynchronousCommunicationChannel acc = (AsynchronousCommunicationChannel) key.attachment();
		
		// Socket is ready to write, check if batch is complete or not...
		if(acc.isBatchAvailable()){
			Output o = acc.getOutput();
			// Pick the buffer to send
			ByteBuffer nb = ((ByteBufferOutputStream)o.getOutputStream()).getByteBuffer();
			try {
				synchronized(nb){
					nb.flip();
					int bytesWritten = sc.write(nb);
					nb.clear();
					o.clear();
	                synchronized(acc){
	                	acc.resetBatch();
	                	acc.notify();
	                }
//	                if(bytesWritten > 0){
//	                	System.out.println("!!!! bytes written: "+bytesWritten);
//	                	for(int i = 0; i<nb.array().length; i++){
//	                		System.out.print(nb.array()[i]);
//	                	}
//	                }
				}
			}
			catch (IOException e) {
				NodeManager.nLogger.severe("-> While trying to write in the aync sc: "+e.getMessage());
				e.printStackTrace();
			}
		}
	}

}
