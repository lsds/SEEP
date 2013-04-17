package seep.processingunit;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Map;

import seep.comm.serialization.DataTuple;
import seep.operator.Operator;
import seep.operator.OperatorStaticInformation;
import seep.runtimeengine.CoreRE;
import seep.runtimeengine.OutputQueue;

public class StatelessProcessingUnit implements IProcessingUnit {

	private CoreRE owner;
	private PUContext ctx;
	
	public StatelessProcessingUnit(CoreRE owner){
		this.owner = owner;
		ctx = new PUContext(owner.getNodeDescr());
	}
	
	@Override
	public void addDownstream(int opId, OperatorStaticInformation location) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addUpstream(int opId, OperatorStaticInformation location) {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<String, Integer> createTupleAttributeMapper() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Operator getOperator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SystemStatus getSystemStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initOperator() {
		// TODO Auto-generated method stub

	}

	@Override
	public void invalidateState(int opId) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isManagingStateOf(int opId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isNodeStateful() {
		return false;
	}

	@Override
	public boolean isOperatorReady() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void newOperatorInstantiation(Operator o) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processData(DataTuple data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processData(ArrayList<DataTuple> data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void reconfigureOperatorConnection(int opId, InetAddress ip) {
		// TODO Auto-generated method stub

	}

	@Override
	public void reconfigureOperatorLocation(int opId, InetAddress ip) {
		// TODO Auto-generated method stub

	}

	@Override
	public void registerManagedState(int opId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendData(DataTuple dt, ArrayList<Integer> targets) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setOpReady(int opId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setOutputQueue(OutputQueue outputQueue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSystemStatus(SystemStatus systemStatus) {
		// TODO Auto-generated method stub

	}

	@Override
	public PUContext setUpRemoteConnections() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void startDataProcessing() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopConnection(int opId) {
		// TODO Auto-generated method stub

	}

}
