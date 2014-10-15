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
package uk.ac.imperial.lsds.seep.comm.serialization;

/* SeePSerializer is a tentative generic solution that defines serialize and deserialize making serialization/deserialization solutions independent. However,
 * there are several issues in this attempt, and this is an ongoing test. */

public interface SeePSerializer<T> {

	public byte[] serialize(T data) throws SeePSerializationException;
	
	public T deserialize(byte[] data) throws SeePSerializationException;
	
}
