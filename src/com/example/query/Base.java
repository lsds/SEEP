package com.example.query;
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
import java.util.ArrayList;


import uk.ac.imperial.lsds.seep.api.QueryBuilder;
import uk.ac.imperial.lsds.seep.api.QueryComposer;
import uk.ac.imperial.lsds.seep.api.QueryPlan;
import uk.ac.imperial.lsds.seep.comm.NodeManagerCommunication;
import uk.ac.imperial.lsds.seep.operator.Connectable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class Base implements QueryComposer{
	Logger LOG = LoggerFactory.getLogger(Base.class);

	public QueryPlan compose() {



		/** Declare operators **/

		// Declare Source
		ArrayList<String> srcFields = new ArrayList<String>();
		srcFields.add("value1");
		Connectable src = QueryBuilder.newStatelessSource(new Source(), 0, srcFields);

		// Declare processor1
		ArrayList<String> pFields = new ArrayList<String>();
		pFields.add("value1");
		Connectable p = QueryBuilder.newStatelessOperator(new Processor(), 1, pFields);

//		// Declare processor-1
//		ArrayList<String> pFields1 = new ArrayList<String>();
//		pFields1.add("value1");
//		Connectable p1 = QueryBuilder.newStatelessOperator(new Processor(), -1, pFields1);
//				
		// Declare sink
		ArrayList<String> snkFields = new ArrayList<String>();
		snkFields.add("value1");	
		Connectable snk = QueryBuilder.newStatelessSink(new Sink(), 2, snkFields);

		/** Connect operators **/
		src.connectTo(p, true, 0);
		p.connectTo(snk, true, 0);
//		src.connectTo(p1, true, 0);
//		p1.connectTo(snk, true, 0);


		LOG.info(">>>>>>>>>>>>>>>>>>>>From Base<<<<<<<<<<<<<<<<<<<<<<<<<");

		return QueryBuilder.build();
	}
}