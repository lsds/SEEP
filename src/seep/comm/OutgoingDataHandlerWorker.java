package seep.comm;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import seep.infrastructure.NodeManager;

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
				
				// Check events
				selector.select();
				
				//Iterate on the events if any
				Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
				while(selectedKeys.hasNext()){
					// We choose one key
					SelectionKey key = (SelectionKey) selectedKeys.next();
					selectedKeys.remove();
					
					// Sanity check
					if(!key.isValid()){
						continue;
					}
					
					// Check the write event
					if(key.isWritable()){
						write(key);
					}
				}
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void write(SelectionKey key){
		// Retrieve socket
		SocketChannel sc = (SocketChannel) key.channel();
		// And retrieve Output with the (underlying) buffer with data.
		Output o = (Output) key.attachment();
		ByteBuffer bb = ByteBuffer.wrap(o.getBuffer());
		try {
			synchronized(bb){
				sc.write(bb);
			}
		}
		catch (IOException e) {
			NodeManager.nLogger.severe("-> While trying to write in the aync sc: "+e.getMessage());
			e.printStackTrace();
		}
	}

}
