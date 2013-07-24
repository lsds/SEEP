/*******************************************************************************
 * Copyright (c) 2013 Imperial College London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial design and implementation
 ******************************************************************************/
package uk.co.imperial.lsds.seep.comm.serialization;

import java.util.ArrayList;

import uk.co.imperial.lsds.seep.comm.serialization.controlhelpers.Ack;
import uk.co.imperial.lsds.seep.comm.serialization.controlhelpers.BackupOperatorState;
import uk.co.imperial.lsds.seep.comm.serialization.controlhelpers.BackupRI;
import uk.co.imperial.lsds.seep.comm.serialization.controlhelpers.CloseSignal;
import uk.co.imperial.lsds.seep.comm.serialization.controlhelpers.InitOperatorState;
import uk.co.imperial.lsds.seep.comm.serialization.controlhelpers.InitRI;
import uk.co.imperial.lsds.seep.comm.serialization.controlhelpers.InvalidateState;
import uk.co.imperial.lsds.seep.comm.serialization.controlhelpers.OpenSignal;
import uk.co.imperial.lsds.seep.comm.serialization.controlhelpers.RawData;
import uk.co.imperial.lsds.seep.comm.serialization.controlhelpers.ReconfigureConnection;
import uk.co.imperial.lsds.seep.comm.serialization.controlhelpers.ReplayStateInfo;
import uk.co.imperial.lsds.seep.comm.serialization.controlhelpers.Resume;
import uk.co.imperial.lsds.seep.comm.serialization.controlhelpers.ScaleOutInfo;
import uk.co.imperial.lsds.seep.comm.serialization.controlhelpers.StateAck;
import uk.co.imperial.lsds.seep.comm.serialization.controlhelpers.StateChunk;
import uk.co.imperial.lsds.seep.operator.State;
import uk.co.imperial.lsds.seep.runtimeengine.CoreRE;

public class ControlTuple {

	private CoreRE.ControlTupleType type;
	
	private Ack ack;
	private BackupOperatorState backupState;
	private ReconfigureConnection reconfigureConnection;
	private ScaleOutInfo scaleOutInfo;
	private Resume resume;
	private InitOperatorState initState;
	private StateAck stateAck;
	private InvalidateState invalidateState;
	private BackupRI backupRI;
	private InitRI initRI;
	private RawData rawData;
	private OpenSignal openSignal;
	private CloseSignal closeSignal;
	private ReplayStateInfo replayStateInfo;
	private StateChunk stateChunk;

	public ControlTuple(){}
	
	public ControlTuple(CoreRE.ControlTupleType type, int opId, long ts){
		this.type = type;
		this.ack = new Ack(opId, ts);
	}
	
	public ControlTuple(CoreRE.ControlTupleType type, int opId){
		this.type = type;
		this.invalidateState = new InvalidateState(opId);
	}
	
	public CoreRE.ControlTupleType getType() {
		return type;
	}
	
	public void setType(CoreRE.ControlTupleType type) {
		this.type = type;
	}
	
	public Ack getAck() {
		return ack;
	}
	
	public void setAck(Ack ack) {
		this.ack = ack;
	}
	
//	@Deprecated
//	public BackupNodeState getBackupState() {
//		return backupState;
//	}
	
	public BackupOperatorState getBackupState(){
		return backupState;
	}
	
	public void setBackupState(BackupOperatorState backupState){
		this.backupState = backupState;
	}
	
//	public void setBackupState(BackupNodeState backupState) {
//		this.backupState = backupState;
//	}
	
	public ReconfigureConnection getReconfigureConnection() {
		return reconfigureConnection;
	}
	public void setReconfigureConnection(ReconfigureConnection reconfigureConnection) {
		this.reconfigureConnection = reconfigureConnection;
	}
	
	public ScaleOutInfo getScaleOutInfo() {
		return scaleOutInfo;
	}
	
	public void setScaleOutInfo(ScaleOutInfo scaleOutInfo) {
		this.scaleOutInfo = scaleOutInfo;
	}
	
	public Resume getResume() {
		return resume;
	}
	
	public void setResume(Resume resume) {
		this.resume = resume;
	}
	
	public InitOperatorState getInitOperatorState() {
		return initState;
	}
	
	public void setInitOperatorState(InitOperatorState initOperatorState) {
		this.initState = initOperatorState;
	}
	
	public StateAck getStateAck() {
		return stateAck;
	}
	
	public void setStateAck(StateAck stateAck) {
		this.stateAck = stateAck;
	}
	
	public InvalidateState getInvalidateState() {
		return invalidateState;
	}
	
	public void setInvalidateState(InvalidateState invalidateState) {
		this.invalidateState = invalidateState;
	}
	
	public BackupRI getBackupRI() {
		return backupRI;
	}
	
	public void setBackupRI(BackupRI backupRI) {
		this.backupRI = backupRI;
	}
	
	public InitRI getInitRI() {
		return initRI;
	}
	
	public void setInitRI(InitRI initRI) {
		this.initRI = initRI;
	}
	
	public RawData getRawData() {
		return rawData;
	}

	public void setRawData(RawData rawData) {
		this.rawData = rawData;
	}
	
	public OpenSignal getOpenSignal() {
		return openSignal;
	}

	public void setOpenSignal(OpenSignal openSignal) {
		this.openSignal = openSignal;
	}

	public CloseSignal getCloseSignal() {
		return closeSignal;
	}

	public void setCloseSignal(CloseSignal closeSignal) {
		this.closeSignal = closeSignal;
	}
	
	public ReplayStateInfo getReplayStateInfo() {
		return replayStateInfo;
	}

	public void setReplayStateInfo(ReplayStateInfo replayStateInfo) {
		this.replayStateInfo = replayStateInfo;
	}
	
	public StateChunk getStateChunk() {
		return stateChunk;
	}

	public void setStateChunk(StateChunk stateChunk) {
		this.stateChunk = stateChunk;
	}
	
	public ControlTuple makeGenericAck(int nodeId){
		this.type = CoreRE.ControlTupleType.ACK;
		this.ack = new Ack(nodeId, 0);
		return this;
	}
	
	public ControlTuple makeStateAck(int nodeId, int mostUpstreamOpId){
		this.type = CoreRE.ControlTupleType.STATE_ACK;
		this.stateAck = new StateAck(nodeId, mostUpstreamOpId);
		return this;
	}
	
	public ControlTuple makeBackupState(BackupOperatorState bs){
		this.type = CoreRE.ControlTupleType.BACKUP_OP_STATE;
		this.backupState = bs;
		return this;
	}
	
	public ControlTuple makeRawData(RawData rw){
		this.type = CoreRE.ControlTupleType.RAW_DATA;
		this.rawData = rw;
		return this;
	}
	
//	@Deprecated
//	public ControlTuple makeBackupState(BackupNodeState bs){
//		this.type = CoreRE.ControlTupleType.BACKUP_NODE_STATE;
//		this.backupState = bs;
//		return this;
//	}
	
	public ControlTuple makeResume(ArrayList<Integer> opIds){
		this.type = CoreRE.ControlTupleType.RESUME;
		this.resume = new Resume(opIds);
		return this;
	}
	
	public ControlTuple makeScaleOut(int opIdToParallelize, int newOpId, boolean isStateful){
		this.type = CoreRE.ControlTupleType.SCALE_OUT;
		this.scaleOutInfo = new ScaleOutInfo(opIdToParallelize, newOpId, isStateful);
		return this;
	}
	
	public ControlTuple makeReconfigure(int opId, String command, String ip){
		this.type = CoreRE.ControlTupleType.RECONFIGURE;
		this.reconfigureConnection = new ReconfigureConnection(opId, command, ip);
		return this;
	}

	public ControlTuple makeReconfigure(int opId, String command, String ip, int nodePort, int inC, int inD, boolean operatorNature, String operatorType) {
		this.type = CoreRE.ControlTupleType.RECONFIGURE;
		this.reconfigureConnection = new ReconfigureConnection(opId, command, ip, nodePort, inC, inD, operatorNature, operatorType);
		return this;
	}
	
	public ControlTuple makeReconfigureSourceRate(int opId, String command, int inC){
		this.type = CoreRE.ControlTupleType.RECONFIGURE;
		this.reconfigureConnection = new ReconfigureConnection(opId, command, inC);
		return this;
		
	}
	
	public ControlTuple makeReconfigureSingleCommand(String command){
		this.type = CoreRE.ControlTupleType.RECONFIGURE;
		this.reconfigureConnection = new ReconfigureConnection(command);
		return this;
	}
	
	public ControlTuple makeInitRI(int nodeId, ArrayList<Integer> indexes, ArrayList<Integer> keys){
		this.type = CoreRE.ControlTupleType.INIT_RI;
		this.initRI = new InitRI(nodeId, indexes, keys);
		return this;
	}
	
	public ControlTuple makeBackupRI(int opId, ArrayList<Integer> indexes, ArrayList<Integer> keys, String operatorType){
		this.type = CoreRE.ControlTupleType.BACKUP_RI;
		this.backupRI = new BackupRI(opId, indexes, keys, operatorType);
		return this;
	}
	
//	@Deprecated
//	public ControlTuple makeInitNodeState(int senderOperatorId, int nodeId, InitOperatorState initOperatorState){
//		this.type = CoreRE.ControlTupleType.INIT_STATE;
//		this.initNodeState = new InitNodeState(senderOperatorId, nodeId, initOperatorState);
//		return this;
//	}
	
	public ControlTuple makeInitOperatorState(int senderOperatorId, State initOperatorState){
		this.type = CoreRE.ControlTupleType.INIT_STATE;
		this.initState = new InitOperatorState(senderOperatorId, initOperatorState);
		return this;
	}
	
	public ControlTuple makeInvalidateMessage(int opIdToInvalidate){
		this.type = CoreRE.ControlTupleType.INVALIDATE_STATE;
		this.invalidateState = new InvalidateState(opIdToInvalidate);
		return this;
	}
	
	public ControlTuple makeOpenSignalBackup(int opId){
		this.type = CoreRE.ControlTupleType.OPEN_BACKUP_SIGNAL;
		this.openSignal = new OpenSignal(opId);
		return this;
	}
	
	public ControlTuple makeCloseSignalBackup(int opId, int totalNumberOfChunks){
		this.type = CoreRE.ControlTupleType.CLOSE_BACKUP_SIGNAL;
		this.closeSignal = new CloseSignal(opId, totalNumberOfChunks);
		return this;
	}
	
	public ControlTuple makeReplayStateInfo(int oldOpId, int newOpId, boolean singleNode){
		this.type = CoreRE.ControlTupleType.REPLAY_STATE;
		this.replayStateInfo = new ReplayStateInfo(oldOpId, newOpId, singleNode);
		return this;
	}
	
	public ControlTuple makeStateChunk(int opId, int partitionNumber, int sequenceNumber, int totalChunks, State state, ArrayList<Integer> partitioningRange){
		this.type = CoreRE.ControlTupleType.STATE_CHUNK;
		this.stateChunk = new StateChunk(opId, partitionNumber, sequenceNumber, totalChunks, state, partitioningRange);
		return this;
	}
	
	@Override
	public String toString(){
		return "ControlTuple."+this.type;
	}
}
