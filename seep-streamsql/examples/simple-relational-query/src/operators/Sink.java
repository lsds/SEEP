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
package operators;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.API;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;

public class Sink implements StatelessOperator {

	private static final long serialVersionUID = 1L;

	public static int BUFFER_SIZE = 1;
	
	private String dataPath = "";
	
	private List<String> buffer = new ArrayList<>();
 	
	private List<String> fields = new ArrayList<String>();

	public void setUp() {

	}
	
	public Sink(String dataPath) {
		this.dataPath = dataPath;
	}

	@Override
	public void processData(DataTuple dt, API api) {

		/*
		 * Make sure that the order of fields is consistent
		 */
		if (fields.isEmpty())
			this.fields.addAll(api.getDataMapper().keySet());
		
//		System.out.println("SNK: " + dt.toString());
		
		StringBuilder sb = new StringBuilder();

		for (String key : fields) {
			sb.append(dt.getValue(key).toString());
			sb.append(',');
		}
		
		if (sb.length() >= 1)
			this.buffer.add(sb.substring(0, sb.length()-1));
		else
			this.buffer.add(sb.substring(0, sb.length()));

		if (this.buffer.size() > BUFFER_SIZE) {
			try {
				FileWriter fw = new FileWriter(this.dataPath, true);
				BufferedWriter bw = new BufferedWriter(fw);
				for (String s : this.buffer)
					bw.write(s + "\n");
				bw.close();
				
				this.buffer.clear();
				
				System.out.println("SNK: wrote results to " + this.dataPath);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void processData(List<DataTuple> arg0, API api) {
		for (DataTuple dt : arg0)
			processData(dt, api);
	}
	
}
