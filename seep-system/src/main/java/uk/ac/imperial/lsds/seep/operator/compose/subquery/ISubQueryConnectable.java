package uk.ac.imperial.lsds.seep.operator.compose.subquery;

import java.util.Map;
import java.util.Set;

import uk.ac.imperial.lsds.seep.operator.compose.multi.ISubQueryTaskResultForwarder;
import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOpTuple;
import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOperator;
import uk.ac.imperial.lsds.seep.operator.compose.multi.SubQueryBufferWindowWrapper;
import uk.ac.imperial.lsds.seep.operator.compose.multi.SubQueryTaskDispatcher;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowDefinition;

public interface ISubQueryConnectable {

	public void setUp();

	public SubQuery getSubQuery();
	
	public void setParentMultiOperator(MultiOperator parent); 
	public MultiOperator getParentMultiOperator();
	
	public boolean isMostLocalDownstream();
	public boolean isMostLocalUpstream();
	public void connectTo(ISubQueryConnectable so, int streamID);

	public Map<Integer, SubQueryBufferWindowWrapper> getLocalDownstreamBuffers();
	public Map<Integer, SubQueryBufferWindowWrapper> getLocalUpstreamBuffers();

	public void registerLocalUpstreamBuffer(SubQueryBufferWindowWrapper so, int streamID);
	public void registerLocalDownstreamBuffer(SubQueryBufferWindowWrapper so, int streamID);
	
	public void processData(MultiOpTuple tuple);
	public Map<Integer, IWindowDefinition> getWindowDefinitions();
	public SubQueryTaskDispatcher getTaskDispatcher();

	public void addResultForwarder(ISubQueryTaskResultForwarder forwarder);
	public Set<ISubQueryTaskResultForwarder> getResultForwarders();

}
