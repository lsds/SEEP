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
		srcFields.add("value0");
		srcFields.add("value1");
		srcFields.add("value2");
		srcFields.add("value3");
		srcFields.add("value4");
		srcFields.add("value5");
		srcFields.add("value6");
		srcFields.add("value7");
		srcFields.add("value8");
		srcFields.add("value9");
		srcFields.add("value10");
		Connectable src = QueryBuilder.newStatelessSource(new Source(), 0, srcFields);

		// Declare processor
		ArrayList<String> pFields = new ArrayList<String>();
		pFields.add("value0");
		pFields.add("value1");
		pFields.add("value2");
		pFields.add("value3");
		pFields.add("value4");
		pFields.add("value5");
		pFields.add("value6");
		pFields.add("value7");
		pFields.add("value8");
		pFields.add("value9");
		pFields.add("value10");
		Connectable p1 = QueryBuilder.newStatelessOperator(new Processor1(), 1, pFields);

		// Declare processor1
		ArrayList<String> pFields1 = new ArrayList<String>();
		pFields1.add("value0");
		pFields1.add("value1");
		pFields1.add("value2");
		pFields1.add("value3");
		pFields1.add("value4");
		pFields1.add("value5");
		pFields1.add("value6");
		pFields1.add("value7");
		pFields1.add("value8");
		pFields1.add("value9");
		pFields1.add("value10");
		Connectable p = QueryBuilder.newStatelessOperator(new Processor(), 2, pFields);

		// Declare sink
		ArrayList<String> snkFields = new ArrayList<String>();
		snkFields.add("value0");
		snkFields.add("value1");		
		snkFields.add("value2");
		snkFields.add("value3");
		snkFields.add("value4");
		snkFields.add("value5");
		snkFields.add("value6");
		snkFields.add("value7");
		snkFields.add("value8");
		snkFields.add("value9");
		snkFields.add("value10");
		Connectable snk = QueryBuilder.newStatelessSink(new Sink(), 3, snkFields);

		/** Connect operators **/
		src.connectTo(p1, true, 0);
		p1.connectTo(p, true, 0);
		p.connectTo(snk, true, 0);

		LOG.info(">>>>>>>>>>>>>>>>>>>>From Base<<<<<<<<<<<<<<<<<<<<<<<<<");

		return QueryBuilder.build();
	}
}
