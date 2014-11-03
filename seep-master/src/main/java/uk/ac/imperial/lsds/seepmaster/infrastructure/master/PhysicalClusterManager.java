package uk.ac.imperial.lsds.seepmaster.infrastructure.master;

import java.net.InetAddress;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.comm.Connection;
import uk.ac.imperial.lsds.seep.infrastructure.ExecutionUnitType;

public class PhysicalClusterManager implements InfrastructureManager {
	
	final private Logger LOG = LoggerFactory.getLogger(PhysicalClusterManager.class);
	
	public final ExecutionUnitType executionUnitType = ExecutionUnitType.PHYSICAL_NODE;
	private Deque<ExecutionUnit> physicalNodes;
	private Map<Integer, Connection> connectionsToPhysicalNodes;

	public PhysicalClusterManager(){
		this.physicalNodes = new ArrayDeque<>();
		this.connectionsToPhysicalNodes = new HashMap<>();
	}
	
	@Override
	public ExecutionUnit buildExecutionUnit(InetAddress ip, int port) {
		return new PhysicalNode(ip, port);
	}
	
	@Override
	public void addExecutionUnit(ExecutionUnit eu) {
		physicalNodes.push(eu);
		connectionsToPhysicalNodes.put(eu.getId(), new Connection(eu.getEndPoint()));
	}
	
	@Override
	public ExecutionUnit getExecutionUnit(){
		if(physicalNodes.size() > 0){
			LOG.debug("Returning 1 executionUnit, remaining: {}", physicalNodes.size()-1);
			return physicalNodes.pop();
		}
		else{
			LOG.error("No available executionUnits !!!");
			return null;
		}
	}

	@Override
	public boolean removeExecutionUnit(int id) {
		for(ExecutionUnit eu : physicalNodes){
			if(eu.getId() == id){
				physicalNodes.remove(eu);
				return true;
			}
		}
		return false;
	}

	@Override
	public int executionUnitsAvailable() {
		return physicalNodes.size();
	}

	@Override
	public void claimExecutionUnits(int numExecutionUnits) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void decommisionExecutionUnits(int numExecutionUnits) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void decommisionExecutionUnit(ExecutionUnit eu) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<Connection> getConnectionsTo(Set<Integer> executionUnitIds) {
		Set<Connection> cs = new HashSet<>();
		for(Integer id : executionUnitIds) {
			// TODO: check that the conn actually exists
			cs.add(connectionsToPhysicalNodes.get(id));
		}
		return cs;
	}

	@Override
	public Connection getConnectionTo(int executionUnitId) {
		return connectionsToPhysicalNodes.get(executionUnitId);
	}

}
