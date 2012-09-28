package seep.comm.serialization;

import java.util.ArrayList;

import seep.comm.serialization.controlhelpers.Ack;
import seep.comm.serialization.controlhelpers.BackupRI;
import seep.comm.serialization.controlhelpers.BackupState;
import seep.comm.serialization.controlhelpers.InitRI;
import seep.comm.serialization.controlhelpers.InitState;
import seep.comm.serialization.controlhelpers.InvalidateState;
import seep.comm.serialization.controlhelpers.ReconfigureConnection;
import seep.comm.serialization.controlhelpers.Resume;
import seep.comm.serialization.controlhelpers.ScaleOutInfo;
import seep.comm.serialization.controlhelpers.StateAck;
import seep.comm.serialization.controlhelpers.StateI;
import seep.operator.Operator;

public class ControlTuple {

	private Operator.ControlTupleType type;
	
	private Ack ack;
	private BackupState backupState;
	private ReconfigureConnection reconfigureConnection;
	private ScaleOutInfo scaleOutInfo;
	private Resume resume;
	private InitState initState;
	private StateAck stateAck;
	private InvalidateState invalidateState;
	private BackupRI backupRI;
	private InitRI initRI;
	
	public ControlTuple(){}
	
	public ControlTuple(Operator.ControlTupleType type, int opId, long ts){
		this.type = type;
		this.ack = new Ack(opId, ts);
	}
	
	public ControlTuple(Operator.ControlTupleType type, int opId){
		this.type = type;
		this.invalidateState = new InvalidateState(opId);
	}
	
	public Operator.ControlTupleType getType() {
		return type;
	}
	public void setType(Operator.ControlTupleType type) {
		this.type = type;
	}
	public Ack getAck() {
		return ack;
	}
	public void setAck(Ack ack) {
		this.ack = ack;
	}
	public BackupState getBackupState() {
		return backupState;
	}
	public void setBackupState(BackupState backupState) {
		this.backupState = backupState;
	}
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
	public InitState getInitState() {
		return initState;
	}
	public void setInitState(InitState initState) {
		this.initState = initState;
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
	
	public ControlTuple makeStateAck(int opId){
		this.type = Operator.ControlTupleType.STATE_ACK;
		this.stateAck = new StateAck(opId);
		return this;
	}
	
	public ControlTuple makeBackupState(BackupState bs){
		this.type = Operator.ControlTupleType.BACKUP_STATE;
		this.backupState = bs;
		return this;
	}
	
	public ControlTuple makeResume(ArrayList<Integer> opIds){
		this.type = Operator.ControlTupleType.RESUME;
		this.resume = new Resume(opIds);
		return this;
	}
	
	public ControlTuple makeScaleOut(int opIdToParallelize, int newOpId){
		this.type = Operator.ControlTupleType.SCALE_OUT;
		this.scaleOutInfo = new ScaleOutInfo(opIdToParallelize, newOpId);
		return this;
	}
	
	public ControlTuple makeReconfigure(int opId, String command, String ip){
		this.type = Operator.ControlTupleType.RECONFIGURE;
		this.reconfigureConnection = new ReconfigureConnection(opId, command, ip);
		return this;
	}

	public ControlTuple makeReconfigure(int opId, String command, String ip, int nodePort, int inC, int inD, boolean operatorNature) {
		this.type = Operator.ControlTupleType.RECONFIGURE;
		this.reconfigureConnection = new ReconfigureConnection(opId, command, ip, nodePort, inC, inD, operatorNature);
		return this;
	}
	
	public ControlTuple makeReconfigureSourceRate(int opId, String command, int inC){
		this.type = Operator.ControlTupleType.RECONFIGURE;
		this.reconfigureConnection = new ReconfigureConnection(opId, command, inC);
		return this;
		
	}
	
	public ControlTuple makeReconfigureSingleCommand(String command){
		this.type = Operator.ControlTupleType.RECONFIGURE;
		this.reconfigureConnection = new ReconfigureConnection(command);
		return this;
	}
	
	public ControlTuple makeInitRI(int opId, ArrayList<Integer> indexes, ArrayList<Integer> keys){
		this.type = Operator.ControlTupleType.INIT_RI;
		this.initRI = new InitRI(opId, indexes, keys);
		return this;
	}
	
	public ControlTuple makeBackupRI(int opId, ArrayList<Integer> indexes, ArrayList<Integer> keys, String operatorType){
		this.type = Operator.ControlTupleType.BACKUP_RI;
		this.backupRI = new BackupRI(opId, indexes, keys, operatorType);
		return this;
	}
	
	public ControlTuple makeInitState(int opId, long ts, StateI state){
		this.type = Operator.ControlTupleType.INIT_STATE;
		this.initState = new InitState(opId, ts, state);
		return this;
	}
}
