/*******************************************************************************
 * Copyright (c) 2013 Imperial College London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial design and implementation
 *     Martin Rouaux - Changes to support scale-in of operators
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.comm.serialization;

import java.util.ArrayList;
import java.util.Set;

import com.google.common.collect.RangeSet;

import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.Ack;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.BackupOperatorState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.BackupRI;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.CloseSignal;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.DistributedScaleOutInfo;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.DownUpRCtrl;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.FailureCtrl;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.InitOperatorState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.InitRI;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.InvalidateState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.KeyBounds;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.OpFailureCtrl;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.OpenSignal;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.RawData;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.ReconfigureConnection;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.ReplayStateInfo;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.Resume;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.ScaleInInfo;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.ScaleOutInfo;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.StateAck;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.StateChunk;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.StreamState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.UpDownRCtrl;
import uk.ac.imperial.lsds.seep.reliable.MemoryChunk;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE;
import uk.ac.imperial.lsds.seep.state.StateWrapper;

public class ControlTuple {

	private CoreRE.ControlTupleType type;
	
	//private Ack ack;
	private OpFailureCtrl opFctrl;
	//private BackupOperatorState backupState;
	//private ReconfigureConnection reconfigureConnection;
	//private ScaleOutInfo scaleOutInfo;
    //private ScaleInInfo scaleInInfo;
	//private DistributedScaleOutInfo distributedScaleOutInfo;
	//private Resume resume;
	//private InitOperatorState initState;
	//private StateAck stateAck;
	//private InvalidateState invalidateState;
	//private BackupRI backupRI;
	//private InitRI initRI;
	//private RawData rawData;
	//private OpenSignal openSignal;
	//private CloseSignal closeSignal;
	//private ReplayStateInfo replayStateInfo;
	//private StateChunk stateChunk;
	//private StreamState streamState;
	//private KeyBounds keyBounds;

	private DownUpRCtrl downUp;
	private UpDownRCtrl upDown;
	private long tsSend = 0;

	public ControlTuple(){}
	
	public ControlTuple(CoreRE.ControlTupleType type, int opId, long ts){
		this.type = type;
		//this.ack = new Ack(opId, ts);
	}
	
	public ControlTuple(CoreRE.ControlTupleType type, int opId){
		this.type = type;
		//this.invalidateState = new InvalidateState(opId);
	}
	
	public ControlTuple(CoreRE.ControlTupleType type, int opId, FailureCtrl fctrl)
	{
		this.type = type;
		this.opFctrl = new OpFailureCtrl(opId, fctrl.lw(), fctrl.acks(), fctrl.alives());
	}
	
	public ControlTuple(CoreRE.ControlTupleType type, int opId, int qLen)
	{
		if (!type.equals(CoreRE.ControlTupleType.UP_DOWN_RCTRL)) { throw new RuntimeException("Logic error."); }
		this.type = type;
		this.upDown = new UpDownRCtrl(opId, qLen);
		this.tsSend = System.currentTimeMillis();
	}

	public ControlTuple(CoreRE.ControlTupleType type, int opId, double weight, RangeSet<Long> unmatched)
	{
		this.type = type;
		this.downUp = new DownUpRCtrl(opId, weight, unmatched);
		this.tsSend = System.currentTimeMillis();
	}
	
	public ControlTuple(CoreRE.ControlTupleType type, int opId, double weight, RangeSet<Long> unmatched, FailureCtrl fctrl)
	{
		this.type = type;
		this.downUp = new DownUpRCtrl(opId, weight, unmatched);
		this.opFctrl = new OpFailureCtrl(opId, fctrl.lw(), fctrl.acks(), fctrl.alives());
		this.tsSend = System.currentTimeMillis();
	}

	public CoreRE.ControlTupleType getType() {
		return type;
	}
	
	public void setType(CoreRE.ControlTupleType type) {
		this.type = type;
	}
	
	public Ack getAck() {
		//return ack;
		return null;
	}
	
	public void setAck(Ack ack) {
		//this.ack = ack;
	}

	public OpFailureCtrl getOpFailureCtrl() {
		return opFctrl;
	}
	
	public void setOpFailureCtrl(OpFailureCtrl opFctrl)
	{
		this.opFctrl = opFctrl;
	}
	
	public BackupOperatorState getBackupState(){
		//return backupState;
		return null;
	}
	
	public void setBackupState(BackupOperatorState backupState){
		//this.backupState = backupState;
	}

	public ReconfigureConnection getReconfigureConnection() {
		//return reconfigureConnection;
		return null;
	}
	public void setReconfigureConnection(ReconfigureConnection reconfigureConnection) {
		//this.reconfigureConnection = reconfigureConnection;
	}
	
	public ScaleOutInfo getScaleOutInfo() {
		//return scaleOutInfo;
		return null;
	}
	
	public DistributedScaleOutInfo getDistributedScaleOutInfo(){
		//return distributedScaleOutInfo;
		return null;
	}
	
	public void setScaleOutInfo(ScaleOutInfo scaleOutInfo) {
		//this.scaleOutInfo = scaleOutInfo;
	}
	
	public void setDistributedScaleOutInfo(DistributedScaleOutInfo distributedScaleOutInfo) {
		//this.distributedScaleOutInfo = distributedScaleOutInfo;
	}
	
	public Resume getResume() {
		//return resume;
		return null;
	}
	
	public void setResume(Resume resume) {
		//this.resume = resume;
	}
	
	public InitOperatorState getInitOperatorState() {
		//return initState;
		return null;
	}
	
	public void setInitOperatorState(InitOperatorState initOperatorState) {
		//this.initState = initOperatorState;
	}
	
	public StateAck getStateAck() {
		//return stateAck;
		return null;
	}
	
	public void setStateAck(StateAck stateAck) {
		//this.stateAck = stateAck;
		
	}
	
	public InvalidateState getInvalidateState() {
		//return invalidateState;
		return null;
	}
	
	public void setInvalidateState(InvalidateState invalidateState) {
		//this.invalidateState = invalidateState;
	}
	
	public BackupRI getBackupRI() {
		//return backupRI;
		return null;
	}
	
	public void setBackupRI(BackupRI backupRI) {
		//this.backupRI = backupRI;
	}
	
	public InitRI getInitRI() {
		//return initRI;
		return null;
	}
	
	public void setInitRI(InitRI initRI) {
		//this.initRI = initRI;
	}
	
	public RawData getRawData() {
		//return rawData;
		return null;
	}

	public void setRawData(RawData rawData) {
		//this.rawData = rawData;
	}
	
	public OpenSignal getOpenSignal() {
		//return openSignal;
		return null;
	}

	public void setOpenSignal(OpenSignal openSignal) {
		//this.openSignal = openSignal;
	}

	public CloseSignal getCloseSignal() {
		//return closeSignal;
		return null;
	}

	public void setCloseSignal(CloseSignal closeSignal) {
		//this.closeSignal = closeSignal;
	}
	
	public ReplayStateInfo getReplayStateInfo() {
		//return replayStateInfo;
		return null;
	}

	public void setReplayStateInfo(ReplayStateInfo replayStateInfo) {
		//this.replayStateInfo = replayStateInfo;
	}
	
	public StateChunk getStateChunk() {
		//return stateChunk;
		return null;
	}

	public void setStateChunk(StateChunk stateChunk) {
		//this.stateChunk = stateChunk;
	}
	
	public void setKeyBounds(KeyBounds keyBounds){
		//this.keyBounds = keyBounds;
	}
	
	public KeyBounds getKeyBounds(){
		//return keyBounds;
		return null;
	}
	
	public void setStreamState(StreamState streamState){
		//this.streamState = streamState;
	}
	
	public StreamState getStreamState(){
		//return streamState;
		return null;
	}
	
	public ControlTuple makeGenericAck(int nodeId){
		this.type = CoreRE.ControlTupleType.ACK;
		//this.ack = new Ack(nodeId, 0);
		return this;
	}
	
	public ControlTuple makeStateAck(int nodeId, int mostUpstreamOpId){
		this.type = CoreRE.ControlTupleType.STATE_ACK;
		//this.stateAck = new StateAck(nodeId, mostUpstreamOpId);
		return this;
	}
	
	public ControlTuple makeBackupState(BackupOperatorState bs){
		this.type = CoreRE.ControlTupleType.BACKUP_OP_STATE;
		//this.backupState = bs;
		return this;
	}
	
	public ControlTuple makeResume(ArrayList<Integer> opIds){
		this.type = CoreRE.ControlTupleType.RESUME;
		//this.resume = new Resume(opIds);
		return this;
	}
	
	public ControlTuple makeScaleOut(int opIdToParallelize, int newOpId, boolean isStateful){
		this.type = CoreRE.ControlTupleType.SCALE_OUT;
		//this.scaleOutInfo = new ScaleOutInfo(opIdToParallelize, newOpId, isStateful);
		return this;
	}
	
    public ControlTuple makeScaleIn(int opId, int victimOpId, boolean isStateful) {
        this.type = CoreRE.ControlTupleType.SCALE_IN;
        //this.scaleInInfo = new ScaleInInfo(opId, victimOpId, isStateful);
        return this;
    }
    
	public ControlTuple makeDistributedScaleOut(int opIdToParallelize, int newOpId){
		this.type = CoreRE.ControlTupleType.DISTRIBUTED_SCALE_OUT;
		//this.distributedScaleOutInfo = new DistributedScaleOutInfo(opIdToParallelize, newOpId);
		return this;
	}
	
	public ControlTuple makeReconfigure(int opId, String command, String ip){
		this.type = CoreRE.ControlTupleType.RECONFIGURE;
		//this.reconfigureConnection = new ReconfigureConnection(opId, command, ip);
		return this;
	}

	public ControlTuple makeReconfigure(int opId, int originalOpId, String command, 
			String ip, int nodePort, int inC, int inD, boolean operatorNature, String operatorType) {
		this.type = CoreRE.ControlTupleType.RECONFIGURE;
		//this.reconfigureConnection = new ReconfigureConnection(opId, originalOpId, command, 
		//		ip, nodePort, inC, inD, operatorNature, operatorType);
		return this;
	}
	
	public ControlTuple makeReconfigureSourceRate(int opId, String command, int inC){
		this.type = CoreRE.ControlTupleType.RECONFIGURE;
		//this.reconfigureConnection = new ReconfigureConnection(opId, command, inC);
		return this;
		
	}
	
	public ControlTuple makeReconfigureSingleCommand(String command){
		this.type = CoreRE.ControlTupleType.RECONFIGURE;
		//this.reconfigureConnection = new ReconfigureConnection(command);
		return this;
	}
	
	public ControlTuple makeInitRI(int nodeId, ArrayList<Integer> indexes, ArrayList<Integer> keys){
		this.type = CoreRE.ControlTupleType.INIT_RI;
		//this.initRI = new InitRI(nodeId, indexes, keys);
		return this;
	}
	
	public ControlTuple makeBackupRI(int opId, ArrayList<Integer> indexes, ArrayList<Integer> keys, String operatorType){
		this.type = CoreRE.ControlTupleType.BACKUP_RI;
		//this.backupRI = new BackupRI(opId, indexes, keys, operatorType);
		return this;
	}
	
	public ControlTuple makeInitOperatorState(int senderOperatorId, StateWrapper initOperatorState){
		this.type = CoreRE.ControlTupleType.INIT_STATE;
		//this.initState = new InitOperatorState(senderOperatorId, initOperatorState);
		return this;
	}
	
	public ControlTuple makeInvalidateMessage(int opIdToInvalidate){
		this.type = CoreRE.ControlTupleType.INVALIDATE_STATE;
		//this.invalidateState = new InvalidateState(opIdToInvalidate);
		return this;
	}
	
	public ControlTuple makeOpenSignalBackup(int opId){
		this.type = CoreRE.ControlTupleType.OPEN_BACKUP_SIGNAL;
		//this.openSignal = new OpenSignal(opId);
		return this;
	}
	
	public ControlTuple makeCloseSignalBackup(int opId, int totalNumberOfChunks){
		this.type = CoreRE.ControlTupleType.CLOSE_BACKUP_SIGNAL;
		//this.closeSignal = new CloseSignal(opId, totalNumberOfChunks);
		return this;
	}
	
	public ControlTuple makeReplayStateInfo(int oldOpId, int newOpId, boolean singleNode){
		this.type = CoreRE.ControlTupleType.STREAM_STATE;
		//this.replayStateInfo = new ReplayStateInfo(oldOpId, newOpId, singleNode);
		return this;
	}
	
	public ControlTuple makeStateChunk(int ownerOpId, int keeperOpId, int seqNumber, int totalChunks, MemoryChunk mc, int splittingKey){
		this.type = CoreRE.ControlTupleType.STATE_CHUNK;
		//this.stateChunk = new StateChunk(ownerOpId, keeperOpId, seqNumber, totalChunks, mc, splittingKey);
		return this;
	}
	
	public ControlTuple makeKeyBounds(int minBound, int maxBound){
		this.type = CoreRE.ControlTupleType.KEY_SPACE_BOUNDS;
		//this.keyBounds = new KeyBounds(minBound, maxBound);
		return this;
	}
	
	public ControlTuple makeStreamState(int targetOpId){
		this.type = CoreRE.ControlTupleType.STREAM_STATE;
		//this.streamState = new StreamState(targetOpId);
		return this;
	}

    public ScaleInInfo getScaleInInfo() {
        //return scaleInInfo;
        return null;
    }

    public void setScaleInInfo(ScaleInInfo scaleInInfo) {
        //this.scaleInInfo = scaleInInfo;
    }
	
	public OpFailureCtrl getOpFctrl() {
		return opFctrl;
	}

	public DownUpRCtrl getDownUp() {
		return downUp;
	}

	public UpDownRCtrl getUpDown() {
		return upDown;
	}
  

	public long getTsSend() {
		return tsSend;
	}

	public void setTsSend(long tsSend)
	{
		this.tsSend = tsSend;
	}

	@Override
	public String toString(){
		return "ControlTuple."+this.type;
	}
}
