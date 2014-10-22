package uk.ac.imperial.lsds.seepmaster.infrastructure.master;

import uk.ac.imperial.lsds.seep.infrastructure.EndPoint;
import uk.ac.imperial.lsds.seep.infrastructure.ExecutionUnitType;

public class PhysicalNode implements ExecutionUnit {

	private static final ExecutionUnitType executionUnitType = ExecutionUnitType.PHYSICAL_NODE;
	
	private EndPoint ep;
	private int id;
	
	public PhysicalNode(EndPoint ep) {
		this.ep = ep;
		this.id = ep.getIp().hashCode() + ep.getPort();
	}

	@Override
	public EndPoint getEndPoint() {
		return ep;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public ExecutionUnitType getType() {
		return executionUnitType;
	}
	
	@Override
	public String toString(){
		String ls = System.getProperty("line.separator");
		StringBuilder sb = new StringBuilder();
		sb.append("TYPE: "+executionUnitType.name());
		sb.append(ls);
		sb.append("IP: "+ep.getIp().toString());
		sb.append(ls);
		sb.append("PORT: "+ep.getPort());
		sb.append(ls);
		return sb.toString();
	}

}