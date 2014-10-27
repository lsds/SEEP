package uk.ac.imperial.lsds.seepmaster.query;

import java.util.Map;
import java.util.concurrent.Executors;

import org.junit.Test;

import uk.ac.imperial.lsds.seep.api.BaseTest;
import uk.ac.imperial.lsds.seep.api.LogicalSeepQuery;
import uk.ac.imperial.lsds.seep.comm.serialization.JavaSerializer;
import uk.ac.imperial.lsds.seep.infrastructure.EndPoint;
import uk.ac.imperial.lsds.seepmaster.comm.Comm;
import uk.ac.imperial.lsds.seepmaster.comm.IOComm;
import uk.ac.imperial.lsds.seepmaster.infrastructure.master.InfrastructureManager;
import uk.ac.imperial.lsds.seepmaster.infrastructure.master.InfrastructureManagerFactory;
import uk.ac.imperial.lsds.seepmaster.infrastructure.master.InfrastructureType;

public class QueryManagerTest {

	@Test
	public void test() {
		//Create Base class
		BaseTest bt = new BaseTest();
		//Get logical seep query by composing the base class
		LogicalSeepQuery lsq = bt.compose();
		
		InfrastructureManager inf = InfrastructureManagerFactory.createInfrastructureManager(InfrastructureType.PHYSICAL_CLUSTER);
		// TODO: get file from config if exists and parse it to get a map from operator to endPoint
		Map<Integer, EndPoint> mapOperatorToEndPoint = null;
		Comm cu = new IOComm(new JavaSerializer(), Executors.newCachedThreadPool());
		QueryManager qm = new QueryManager(lsq, inf, mapOperatorToEndPoint, cu);
		
		qm.deployQueryToNodes();
		//need to put nodes in infrastructure...
	}

}
