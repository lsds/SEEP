/*******************************************************************************
 * Copyright (c) 2013 Imperial College London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial design and implementation
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.infrastructure.dynamiccodedeployer;

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
