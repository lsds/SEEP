/*******************************************************************************
 * Copyright (c) 2013 Imperial College London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial design and implementation
 *     Martin Rouaux - Removal of upstream and downstream connections
 *     which is required to support scale-in of operators.
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.processingunit;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Map;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.FailureCtrl;
import uk.ac.imperial.lsds.seep.manet.BackpressureRouter;
import uk.ac.imperial.lsds.seep.operator.Operator;
import uk.ac.imperial.lsds.seep.operator.OperatorStaticInformation;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE;
import uk.ac.imperial.lsds.seep.runtimeengine.DataStructureAdapter;
import uk.ac.imperial.lsds.seep.runtimeengine.OutputQueue;
import uk.ac.imperial.lsds.seep.runtimeengine.TimestampTracker;

public interface IProcessingUnit {

	//This enum is for aiding in the implementation of the protocols
	public enum SystemStatus {
		NORMAL, WAITING_FOR_STATE_ACK, INITIALISING_STATE//, REPLAYING_BUFFER//, RECONFIGURING_COMM
		, MERGING_STATE
	}

	public CoreRE getOwner();

	public Operator getOperator();

	public Dispatcher getDispatcher();

	public boolean isNodeStateful();

	public SystemStatus getSystemStatus();

	public void setSystemStatus(SystemStatus systemStatus);

	public void newOperatorInstantiation(Operator o);

	public boolean isOperatorReady();

	public void setOpReady(int opId);

	public void setOutputQueue(OutputQueue outputQueue);
	public void setOutputQueueList(ArrayList<OutputQueue> downOpId_outputQ_map);

	public PUContext setUpRemoteConnections();

	public void startDataProcessing();

	public void initOperator();

	public Map<String, Integer> createTupleAttributeMapper();

	public void processData(DataTuple data);

	public void processData(ArrayList<DataTuple> data);

	public void sendData(DataTuple dt, ArrayList<Integer> targets);

	public void sendPartitionedData(DataTuple[] dt, ArrayList<Integer> targets);

	public void sendDataByThreadPool(DataTuple dt, ArrayList<Integer> targets);

	public void stopConnection(int opId);

	public void reconfigureOperatorLocation(int opId, InetAddress ip);

	public void reconfigureOperatorConnection(int opId, InetAddress ip);

	public void invalidateState(int opId);

	public void registerManagedState(int opId);

	public boolean isManagingStateOf(int opId);

	public void addDownstream(int opId, OperatorStaticInformation location);

	public void removeDownstream(int opId);

	public void addUpstream(int opId, OperatorStaticInformation location);

	public void removeUpstream(int opId);

	public void launchMultiCoreMechanism(CoreRE core, DataStructureAdapter dsa);

	public void disableMultiCoreSupport();

	public boolean isMultiCoreEnabled();

	public void createAndRunAckWorker();

	public void createAndRunFailureCtrlWriter();

	public TimestampTracker getLastACK();

	public void emitACK(TimestampTracker currentTs);

	public void emitFailureCtrl(FailureCtrl combinedDownFctrl, boolean downstreamsRoutable);

	public ArrayList<Integer> getRouterIndexesInformation(int opId);

	public ArrayList<Integer> getRouterKeysInformation(int opId);

	public int getOriginalUpstreamFromOpId(int opId);

	public int getOpIdFromUpstreamIp(InetAddress ip);

	public int getOpIdFromUpstreamIpPort(InetAddress ip, int port);

	public PUContext getPUContext();

	public void sendDataDispatched(DataTuple dt);

	public void ack(DataTuple dt);

}
