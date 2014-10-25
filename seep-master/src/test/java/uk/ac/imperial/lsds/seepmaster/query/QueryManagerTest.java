package uk.ac.imperial.lsds.seepmaster.query;

import java.util.Map;

import org.junit.Test;

import uk.ac.imperial.lsds.seep.api.BaseTest;
import uk.ac.imperial.lsds.seep.api.LogicalSeepQuery;
import uk.ac.imperial.lsds.seep.infrastructure.EndPoint;
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
		
		QueryManager qm = new QueryManager(lsq, inf, mapOperatorToEndPoint);
		
		qm.deployQueryToNodes();
		//need to put nodes in infrastructure...
	}

}
