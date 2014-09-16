package com.example.query;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.api.QueryBuilder;
import uk.ac.imperial.lsds.seep.api.QueryComposer;
import uk.ac.imperial.lsds.seep.api.QueryPlan;
import uk.ac.imperial.lsds.seep.operator.Connectable;
import uk.ac.imperial.lsds.seep.operator.DistributedApi;

public class BaseScale2 implements QueryComposer {
	Logger LOG = LoggerFactory.getLogger(BaseScale2.class);
	@Override
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
		p1.connectTo(p2, true, 0);
		p2.connectTo(snk, true, 0);
		
		
		LOG.info(">>>>>>>>>>>>>>>>>>>>From Base Scaleout (k=2) <<<<<<<<<<<<<<<<<<<<<<<<<");
		QueryBuilder.scaleOut(p1.getOperatorId(), 2);
		QueryBuilder.scaleOut(p2.getOperatorId(), 2);
		return QueryBuilder.build();
	}

}
