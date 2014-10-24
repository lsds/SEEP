package uk.ac.imperial.lsds.seep.infrastructure;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;

public class ExtendedObjectOutputStream extends ObjectOutputStream {

	public ExtendedObjectOutputStream() throws IOException, SecurityException {
		
	}

	public ExtendedObjectOutputStream(OutputStream out) throws IOException {
		super(out);
	}
	
	public void writeClassDescriptor(ObjectStreamClass osc){
		try {
			super.writeClassDescriptor(osc);
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}