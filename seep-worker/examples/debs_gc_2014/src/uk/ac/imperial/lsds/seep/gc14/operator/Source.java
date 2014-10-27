package uk.ac.imperial.lsds.seep.gc14.operator;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.TuplePayload;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;

public class Source implements StatelessOperator {

	private static final long serialVersionUID = 1L;

	/*
	 * Get the information on the structure of processed tuples
	 */
	private DataTuple dataTupleStructure;
	
	/*
	 * Members needed for parsing the data block-wise
	 */
	public final static String[] KEYS = "id,timestamp,value,property,plug_id,household_id,house_id".split(",");
	public final static String[] TYPES = "Long,Integer,Float,Integer,Integer,Integer,Integer".split(",");
	private BufferedReader reader = null;
	private int blockSize = -1;
	private List<DataTuple> block = new ArrayList<DataTuple>();
	
	// data is now a field, so that it is accessible from outside processData()
	DataTuple data;
	
	int bufferSize = 5000000;
	
	@Override
	public void processData(DataTuple arg0) {
		//tuple schema stuff
		Map<String, Integer> mapper = api.getDataMapper();
		data = new DataTuple(mapper, new TuplePayload());
		
		//time control stuff
		int c = 0;
		long init = System.currentTimeMillis();
		
		while(true){
			c++;

			/** 
			 * READ FROM DISK
			 */
			try {
				DataTuple toSend = readNext();
				toSend.getPayload().instrumentation_ts = System.currentTimeMillis();
				if(toSend != null){
					if(toSend.getInt("timestamp") > 1378339200){
						System.out.println("DONE pass");
						return;
					}
					api.send(toSend);
				}
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
			
			//TIMING
			if((System.currentTimeMillis() - init) > 1000){
				System.out.println("SRC "+c+" ");
				c = 0;
				init = System.currentTimeMillis();
			}
		}
	}
	
	class DiskReader implements Runnable{
		LinkedBlockingDeque<DataTuple> buffer;
		
		public DiskReader(LinkedBlockingDeque<DataTuple> buffer){
			this.buffer = buffer;
		}
		
		@Override
		public void run() {
			// never stop
			while(true){
				//just read from disk and inject into the deque
				try {
					buffer.putLast(readNext());
				} 
				catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	class Injector implements Runnable {
		
		private ArrayList<DataTuple> buffer = new ArrayList<DataTuple>();
		private int downstreamId = 0;
		
		public Injector(ArrayList<DataTuple> buffer, int downstreamId){
			// deep copy array
			for(int i = 0; i<buffer.size(); i++){
				this.buffer.add(buffer.get(i));
			}
			this.downstreamId = downstreamId;
		}
		
		public void run(){
			int index = 0;
			while(true){
				DataTuple output = buffer.get(index);
				index++;
				if(index == bufferSize-2){
					index = 0;
				}
				api.send_toStreamId(output, downstreamId);
			}
		}
	}
	
	public DataTuple readNext() throws Exception {

		if (blockSize <= 0) {
			return readItem();
		}

		if (block.isEmpty()) {
			int read = readBlock();
			if (read == 0)
				return null;
		}

		DataTuple item = block.remove(0);
		return item;
	}
	

	protected int readBlock() throws Exception {
		block.clear();
		int read = 0;
		for (int i = 0; i < blockSize; i++) {
			DataTuple item = readItem();
			if (item != null) {
				block.add(item);
				read++;
			} else
				return read;
		}
		return read;
	}

	protected DataTuple readItem() throws Exception {
//		System.out.println("readItem()");
		if (dataTupleStructure == null){
			System.out.println("dataTupleStructure is null");
			return null;
		}
		
		String line = reader.readLine();
//		System.out.println(line);
		if (line == null)
			return null;

		Object[] values = new Object[KEYS.length];
		

		int i = 0;
		int len = line.length();
		int max = len - 1;
		int floating = 0;

		Long longVal = 0L;
		Float floatVal = 0f;

		for (int c = 0; c < len; c++) {
			char ch = line.charAt(c);
			if (ch == ',') {
				if (floating > 0) 
					values[i] = hardCodedCast(i,String.valueOf(longVal + floatVal / floating));
				else 
					values[i] = hardCodedCast(i,String.valueOf(longVal));
				
				longVal = 0L;
				floatVal = 0f;
				floating = 0;
				i++;

				if (ch == '\n'){
					DataTuple output = data.newTuple(values);
					return output;
				}
				else{
					continue;
				}
			}
			if (ch == '.') {
				floating = 1;
				continue;
			}

			if (floating > 0) {
				floatVal *= 10;
				floatVal += (ch - '0');
				floating *= 10;
			}
			else {
				longVal *= 10;
				longVal += (ch - '0');
			}

			if (c == max) {
				if (floating > 0) 
					values[i] = hardCodedCast(i,String.valueOf(longVal + floatVal / floating));
				else 
					values[i] = hardCodedCast(i,String.valueOf(longVal));
				DataTuple output = data.newTuple(values);
				return output;
			}
		}
		DataTuple output = data.newTuple(values);
		return output;
	}

	@Override
	public void processData(List<DataTuple> arg0) { }
	
	@Override
	public void setUp() {
		String filePath = null; 
		int opId = api.getOperatorId();
		if(opId == 0){
			filePath = "file:///houses0-4.csv";
		}
		else if(opId == 11){
			filePath = "file:///houses5-9.csv";
		}
		else if(opId == 12){
			filePath = "file:///houses10-14.csv";
		}
		else if(opId == 13){
			filePath = "file:///houses15-19.csv";
		}
		filePath = "file:///input.csv";
		URL url;
		try {
			url = new URL(filePath);
			setUp(url);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Object hardCodedCast(int i, String value) {
		switch (TYPES[i]) {
		case "Integer":
			return new Integer(value);
		case "Long":
			return new Long(value);
		case "Float":
			return new Float(value);
		default:
			return value;
		}
	}

	public void setUp(URL url) throws IOException {
		this.reader = new BufferedReader(new InputStreamReader(url.openStream()));
		if (blockSize > 0) {
			this.block = new ArrayList<DataTuple>(blockSize);
		}
		
		Map<String, Integer> mapper = api.getDataMapper();
		this.dataTupleStructure = new DataTuple(mapper, new TuplePayload());
	}

	public int getBlockSize() {
		return blockSize;
	}

	public void setBlockSize(int blockSize) {
		this.blockSize = blockSize;
	}
	
	class BufferedMemoryMapInputStreamAdapter extends BufferedInputStream{
		public BufferedMemoryMapInputStreamAdapter(InputStream in) {
			super(in);
		}
	}
	
	class MemoryMapInputStreamAdapter extends InputStream{
		MappedByteBuffer b = null;
		public MemoryMapInputStreamAdapter(MappedByteBuffer b){
			this.b = b;
		}

		@Override
		public int read() throws IOException {
			return b.get();
		}
	}
}