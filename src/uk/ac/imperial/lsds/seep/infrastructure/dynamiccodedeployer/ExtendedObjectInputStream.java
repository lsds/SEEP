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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ExtendedObjectInputStream extends ObjectInputStream {

	private RuntimeClassLoader rcl = null;
	Logger LOG = LoggerFactory.getLogger(ExtendedObjectInputStream.class);

	public ExtendedObjectInputStream(InputStream in, RuntimeClassLoader rcl) throws IOException {
		super(in);
		this.rcl = rcl;
	}
	
	public ObjectStreamClass readClassDescriptor()  {

				ObjectStreamClass result = null;
				Object o = null;
				try {
					o = super.readClassDescriptor();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				
				result = (ObjectStreamClass) o;
				return result;

	}
	
	public Class<?> resolveClass(ObjectStreamClass osc){
		///\fixme{Check if the class is already loaded in the system before loading it}
		Class hack = null;
		hack = rcl.loadClass(osc.getName());
		return hack;
	}
	
	public Object resolveObject(Object mock){
		this.enableResolveObject(true);
		try {
			return super.resolveObject(this.readObject());
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mock;
	}
	
}
