package seep.processingunit;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Map;

import seep.comm.serialization.DataTuple;
import seep.operator.Operator;
import seep.operator.OperatorStaticInformation;
import seep.runtimeengine.CoreRE;
import seep.runtimeengine.DataStructureAdapter;
import seep.runtimeengine.OutputQueue;

public interface IProcessingUnit {
	
	//This enum is for aiding in the implementation of the protocols
	public enum SystemStatus {
		NORMAL, WAITING_FOR_STATE_ACK, INITIALISING_STATE//, REPLAYING_BUFFER//, RECONFIGURING_COMM
	}
	
	public CoreRE getOwner();
	
	public Operator getOperator();
		
	public boolean isNodeStateful();
		
	public SystemStatus getSystemStatus();
		
	public void setSystemStatus(SystemStatus systemStatus);
		
	public void newOperatorInstantiation(Operator o);
		
	public boolean isOperatorReady();
		
	public void setOpReady(int opId);
	
	public void setOutputQueue(OutputQueue outputQueue);
	
	public PUContext setUpRemoteConnections();

	public void startDataProcessing();
	
	public void initOperator();
		
	public Map<String, Integer> createTupleAttributeMapper();
	
	public void processData(DataTuple data);
	
	public void processData(ArrayList<DataTuple> data);
	
	public void sendData(DataTuple dt, ArrayList<Integer> targets);

	public void stopConnection(int opId);
	
	public void reconfigureOperatorLocation(int opId, InetAddress ip);
	
	public void reconfigureOperatorConnection(int opId, InetAddress ip);
	
	public void invalidateState(int opId);
	
	public void registerManagedState(int opId);
	
	public boolean isManagingStateOf(int opId);
		
	public void addDownstream(int opId, OperatorStaticInformation location);
	
	public void addUpstream(int opId, OperatorStaticInformation location);
	
	public void launchMultiCoreMechanism(CoreRE core, DataStructureAdapter dsa);
	
	public void disableMultiCoreSupport();
	
	public boolean isMultiCoreEnabled();

	public void createAndRunAckWorker();

	public long getLastACK();

	public void emitACK(long currentTs);
	
	public ArrayList<Integer> getRouterIndexesInformation(int opId);
	
	public ArrayList<Integer> getRouterKeysInformation(int opId);
	
}
