package uk.ac.imperial.lsds.seepmaster.comm;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import org.junit.Test;

import uk.ac.imperial.lsds.seep.comm.Comm;
import uk.ac.imperial.lsds.seep.comm.IOComm;
import uk.ac.imperial.lsds.seep.comm.serialization.JavaSerializer;
import uk.ac.imperial.lsds.seep.infrastructure.EndPoint;
import uk.ac.imperial.lsds.seepmaster.infrastructure.master.InfrastructureManager;
import uk.ac.imperial.lsds.seepmaster.infrastructure.master.InfrastructureManagerFactory;
import uk.ac.imperial.lsds.seepmaster.infrastructure.master.InfrastructureType;
import uk.ac.imperial.lsds.seepmaster.query.QueryManager;

public class MasterWorkerAPIImplementationTest {

	@Test
	public void testBootstrap() {
		InfrastructureManager inf = InfrastructureManagerFactory.createInfrastructureManager(InfrastructureType.PHYSICAL_CLUSTER);
		Map<Integer, EndPoint> mapOperatorToEndPoint = null;
		Comm cu = new IOComm(new JavaSerializer(), Executors.newCachedThreadPool());
		QueryManager qm = QueryManager.getInstance(inf, mapOperatorToEndPoint, cu);
		
		MasterWorkerAPIImplementation api = new MasterWorkerAPIImplementation(qm, inf);
		
		int avail = inf.executionUnitsAvailable();
		assert(avail == 0);
		
		Map<String, String> kv = new HashMap<>();
		kv.put("ip", "10.0.0.1");
		kv.put("port", "3500");
		try {
			api.bootstrapCommand(kv);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		avail = inf.executionUnitsAvailable();
		assert(avail == 1);
	}

}
