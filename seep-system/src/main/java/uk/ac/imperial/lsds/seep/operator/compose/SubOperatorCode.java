/*******************************************************************************
 * Copyright (c) 2014 Imperial College London
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial API and implementation
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.operator.compose;

import java.io.Serializable;
import java.util.ArrayList;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.LocalApi;

public interface SubOperatorCode extends Serializable{

	public LocalApi api = LocalApi.getInstance();
	
	public void setUp();
	public void processData(DataTuple data);
	public void processData(ArrayList<DataTuple> dataList);
	
}
