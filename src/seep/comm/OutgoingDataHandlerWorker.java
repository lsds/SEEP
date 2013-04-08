package seep.comm;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;

import seep.comm.serialization.messages.BatchTuplePayload;
import seep.comm.serialization.messages.Payload;
import seep.comm.serialization.messages.TuplePayload;
import seep.comm.serialization.serializers.ArrayListSerializer;

import com.esotericsoftware.kryo.Kryo;

public class OutgoingDataHandlerWorker implements Runnable{

	// TX data
	private Selector selector;
	
	// Serialization data
	private Kryo k;
	
	private boolean goOn = true;
	
	public OutgoingDataHandlerWorker(){
		this.k = this.initializeKryo();
	}
	
	private Kryo initializeKryo(){
		//optimize here kryo
		Kryo k = new Kryo();
		k.register(ArrayList.class, new ArrayListSerializer());
		k.register(Payload.class);
		k.register(TuplePayload.class);
		k.register(BatchTuplePayload.class);
		return k;
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
		// And retrieve byteBuffer with the data.
		ByteBuffer bb = (ByteBuffer) key.attachment();
		
		
	}

}
