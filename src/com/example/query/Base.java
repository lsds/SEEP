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
		Connectable p1 = QueryBuilder.newStatelessOperator(new Processor1(), 1, pFields1);

		// Declare processor1
		ArrayList<String> pFields2 = new ArrayList<String>();
		pFields2.add("value0");
		pFields2.add("value1");
		pFields2.add("value2");
		pFields2.add("value3");
		pFields2.add("value4");
		pFields2.add("value5");
		pFields2.add("value6");
		pFields2.add("value7");
		pFields2.add("value8");
		pFields2.add("value9");
		pFields2.add("value10");
		Connectable p2 = QueryBuilder.newStatelessOperator(new Processor(), 2, pFields2);

//		// Declare processor
//		ArrayList<String> pFields3 = new ArrayList<String>();
//		pFields3.add("value0");
//		pFields3.add("value1");
//		pFields3.add("value2");
//		pFields3.add("value3");
//		pFields3.add("value4");
//		pFields3.add("value5");
//		pFields3.add("value6");
//		pFields3.add("value7");
//		pFields3.add("value8");
//		pFields3.add("value9");
//		pFields3.add("value10");
//		Connectable p3 = QueryBuilder.newStatelessOperator(new Processor1(), 3, pFields3);
//
//		// Declare processor
//		ArrayList<String> pFields4 = new ArrayList<String>();
//		pFields4.add("value0");
//		pFields4.add("value1");
//		pFields4.add("value2");
//		pFields4.add("value3");
//		pFields4.add("value4");
//		pFields4.add("value5");
//		pFields4.add("value6");
//		pFields4.add("value7");
//		pFields4.add("value8");
//		pFields4.add("value9");
//		pFields4.add("value10");
//		Connectable p4 = QueryBuilder.newStatelessOperator(new Processor(), 4, pFields4);

//
//		// Declare processor1
//		ArrayList<String> pFields5 = new ArrayList<String>();
//		pFields5.add("value0");
//		pFields5.add("value1");
//		pFields5.add("value2");
//		pFields5.add("value3");
//		pFields5.add("value4");
//		pFields5.add("value5");
//		pFields5.add("value6");
//		pFields5.add("value7");
//		pFields5.add("value8");
//		pFields5.add("value9");
//		pFields5.add("value10");
//		Connectable p5 = QueryBuilder.newStatelessOperator(new Processor1(), 5, pFields5);
//
//		// Declare processor
//		ArrayList<String> pFields6 = new ArrayList<String>();
//		pFields6.add("value0");
//		pFields6.add("value1");
//		pFields6.add("value2");
//		pFields6.add("value3");
//		pFields6.add("value4");
//		pFields6.add("value5");
//		pFields6.add("value6");
//		pFields6.add("value7");
//		pFields6.add("value8");
//		pFields6.add("value9");
//		pFields6.add("value10");
//		Connectable p6 = QueryBuilder.newStatelessOperator(new Processor(), 6, pFields6);

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
		Connectable snk = QueryBuilder.newStatelessSink(new Sink(), 7, snkFields);

		/** Connect operators **/
		src.connectTo(p1, true, 0);
//		src.connectTo(p3, true, 0);
//		src.connectTo(p5, true, 0);
		p1.connectTo(p2, true, 0);
//		p3.connectTo(p4, true, 0);
//		p5.connectTo(p6, true, 0);
		p2.connectTo(snk, true, 0);
//		p4.connectTo(snk, true, 0);
//		p6.connectTo(snk, true, 0);
//		src.connectTo(snk, true, 0);
		

		LOG.info(">>>>>>>>>>>>>>>>>>>>From Base<<<<<<<<<<<<<<<<<<<<<<<<<");

		return QueryBuilder.build();
	}
}
