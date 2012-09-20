package seep.operator.collection.lrbenchmark;

import seep.comm.routing.Router;
import seep.comm.serialization.DataTuple;
import seep.comm.tuples.Seep;
import seep.comm.tuples.Seep.BackupState;
import seep.comm.tuples.Seep.TollCalculatorState;
import seep.operator.Operator;
import seep.operator.StateSplitI;
import seep.operator.StatelessOperator;

@SuppressWarnings("serial")
public class Forwarder extends Operator implements StatelessOperator, StateSplitI{

	public Forwarder(int opID) {
		
		//Indicate ID
		super(opID);
		//Provide subclass to the father
		subclassOperator = this;
		
		//Define dispatching filter for different downstreams
		
	}

//TESTING
	int counter = 0;
	long t_start = 0;
	boolean firstTime = true;
	long tinit = 0;
	
	
	
	public void processData(DataTuple dt) {
//System.out.println("XWAY: "+dt.getXway());
//		counter++;
//		if(firstTime){
//			firstTime = false;
//			t_start = System.currentTimeMillis();
//			tinit = System.currentTimeMillis();
//		}
//		//In this case, splitting is done by segment, so get segment.
//		int value = dt.getSeg();
//		int xway = dt.getXway();
//		int dir = dt.getDir();
//		value = value + (1000*xway);
//		value = (dir == 1) ? (value+1)*-1 : (value)+1;
//		
//		sendDown(dt, value);
//		
//		int t = (int) ((int)(System.currentTimeMillis() - tinit)/1000);
////	 	if(t > dt.getTime()+5){
////			System.out.println("FW time: emit: "+dt.getTime()+" current: "+t);
////		}
//		
////		if((System.currentTimeMillis()-t_start) >= 1000){
////			t_start = System.currentTimeMillis();
//////			System.out.println("FW E/S: "+ackCounter);
////			System.out.println("FW E/S: "+counter);
//////			printRoutingInfo();
//////			ackCounter = 0;
////			counter = 0;
////		}
	}

	@Override
	public BackupState.Builder[] parallelizeState(BackupState toSplit, int key){
System.out.println("#######################");
System.out.println("PARALLEL KEY: "+key);
System.out.println("#######################");
		//All smaller than key to the oldBackupState
		long aa = System.currentTimeMillis();
		BackupState.Builder splitted[] = new BackupState.Builder[2];
		//Is a balance account the state I need to split? getState id is 1 to identify the state backuping
		if(toSplit.getBaState().getStateId() == 1){
			System.out.println("BACKUP BA STATE");
			Seep.BalanceAccountState baS = toSplit.getBaState();
			
			Seep.BackupState.Builder oldBS = BackupState.newBuilder();
			Seep.BackupState.Builder newBS = BackupState.newBuilder();
			Seep.BalanceAccountState.Builder oldBaS = Seep.BalanceAccountState.newBuilder();
			Seep.BalanceAccountState.Builder newBaS = Seep.BalanceAccountState.newBuilder();
			
			//Balance account
			oldBaS.addAllBalanceAccount(baS.getBalanceAccountList());
			newBaS.addAllBalanceAccount(baS.getBalanceAccountList());
//			for(Seep.BalanceAccountState.DataII data : baS.getBalanceAccountList()){
//				oldBaS.addBalanceAccount(data);
//				newBaS.addBalanceAccount(data);
//			}
			
			//previous segment
			oldBaS.addAllPreviousSegment(baS.getPreviousSegmentList());
			newBaS.addAllPreviousSegment(baS.getPreviousSegmentList());
//			for(Seep.BalanceAccountState.DataII data : baS.getPreviousSegmentList()){
//				oldBaS.addPreviousSegment(data);
//				newBaS.addPreviousSegment(data);
//			}
			
			//previous toll
			oldBaS.addAllPreviousToll(baS.getPreviousTollList());
			newBaS.addAllPreviousToll(baS.getPreviousTollList());
//			for(Seep.BalanceAccountState.DataII data : baS.getPreviousTollList()){
//				oldBaS.addPreviousToll(data);
//				newBaS.addPreviousToll(data);
//			}
			
			oldBaS.setLastUpdateOfBA(baS.getLastUpdateOfBA());
			newBaS.setLastUpdateOfBA(baS.getLastUpdateOfBA());
			
			oldBaS.setStateId(1);
			newBaS.setStateId(1);
			
			oldBS.setBaState(oldBaS);
			newBS.setBaState(newBaS);
			
			splitted[0] = oldBS;
			splitted[1] = newBS;
			
		}
		//or is it a toll calculator state?
		else if(toSplit.getTcState().getStateId() == 1){
			//SPLIT BY SEGMENT -> avg5-4-3-2-1min, numVLastSeg, numVCurrent, accidents
			//Leave equal -> vSpdMin, stoppedCars, lastVSegment, consecutive-reports
			TollCalculatorState tcState = toSplit.getTcState();
			
			Seep.BackupState.Builder oldBS = BackupState.newBuilder();
			Seep.BackupState.Builder newBS = BackupState.newBuilder();
			Seep.TollCalculatorState.Builder oldTC = TollCalculatorState.newBuilder();
			TollCalculatorState.Builder newTC = TollCalculatorState.newBuilder();
			//split SpeedAvg5Minutes
			for(Seep.TollCalculatorState.DataII data : tcState.getSpeedAvg5MinutesList()){
				if(Router.customHash(data.getKey()) < key){
					oldTC.addSpeedAvg5Minutes(data);
				}
				else{
					newTC.addSpeedAvg5Minutes(data);
				}
			}
			//split SpeedAvg4Minutes
			for(Seep.TollCalculatorState.DataII data : tcState.getSpeedAvg4MinutesList()){
				if(Router.customHash(data.getKey()) < key){
					oldTC.addSpeedAvg4Minutes(data);
				}
				else{
					newTC.addSpeedAvg4Minutes(data);
				}
			}
			//split SpeedAvg3Minutes
			for(Seep.TollCalculatorState.DataII data : tcState.getSpeedAvg3MinutesList()){
				if(Router.customHash(data.getKey()) < key){
					oldTC.addSpeedAvg3Minutes(data);
				}
				else{
					newTC.addSpeedAvg3Minutes(data);
				}
			}
			//split SpeedAvg2Minutes
			for(Seep.TollCalculatorState.DataII data : tcState.getSpeedAvg2MinutesList()){
				if(Router.customHash(data.getKey()) < key){
					oldTC.addSpeedAvg2Minutes(data);
				}
				else{
					newTC.addSpeedAvg2Minutes(data);
				}
			}
			//split SpeedAvg1Minutes
			for(Seep.TollCalculatorState.DataII data : tcState.getSpeedAvg1MinutesList()){
				if(Router.customHash(data.getKey()) < key){
					oldTC.addSpeedAvg1Minutes(data);
				}
				else{
					newTC.addSpeedAvg1Minutes(data);
				}
			}
			//split NumVidLastMinSegSegNumVid
//System.out.println("numVidLastMin_seg_numVid: "+tcState.getNumVidLastMinSegSegNumVidCount());
			for(Seep.TollCalculatorState.DataII data : tcState.getNumVidLastMinSegSegNumVidList()){
//System.out.println("NON EMPTY: "+data.getKey());
				if(Router.customHash(data.getKey()) < key){
//System.out.println("NON EMPTY: "+data.getKey());
					oldTC.addNumVidLastMinSegSegNumVid(data);
				}
				else{
//System.out.println("NON EMPTY: "+data.getKey());
					newTC.addNumVidLastMinSegSegNumVid(data);
				}
			}
			//split NumVidCurrentMinSegSegListVid
//System.out.println("PAR-> NumVidCurrentMinSegSegListVid: "+tcState.getNumVidCurrentMinSegSegListVidCount());
			for(Seep.TollCalculatorState.DataListI data : tcState.getNumVidCurrentMinSegSegListVidList()){
//System.out.println("PAR-> listSize: "+data.getValueCount());
				if(Router.customHash(data.getKey()) < key){
					oldTC.addNumVidCurrentMinSegSegListVid(data);
				}
				else{
					newTC.addNumVidCurrentMinSegSegListVid(data);
				}
			}
			//split accidents
			for(Seep.TollCalculatorState.DataBool data : tcState.getAccidentsList()){
				if(Router.customHash(data.getKey()) < key){
					oldTC.addAccidents(data);
				}
				else{
					newTC.addAccidents(data);
				}
			}
			//CurrentVidSpeedVidSpeed
			oldTC.addAllCurrentVidSpeedVidSpeed(tcState.getCurrentVidSpeedVidSpeedList());
			newTC.addAllCurrentVidSpeedVidSpeed(tcState.getCurrentVidSpeedVidSpeedList());
//			for(Seep.TollCalculatorState.DataII data : tcState.getCurrentVidSpeedVidSpeedList()){
//				oldTC.addCurrentVidSpeedVidSpeed(data);
//				newTC.addCurrentVidSpeedVidSpeed(data);
//			}
			//StoppedCarsPosVid
			oldTC.addAllStoppedCarsPosVid(tcState.getStoppedCarsPosVidList());
			newTC.addAllStoppedCarsPosVid(tcState.getStoppedCarsPosVidList());
//			for(Seep.TollCalculatorState.DataListI data : tcState.getStoppedCarsPosVidList()){
//				oldTC.addStoppedCarsPosVid(data);
//				newTC.addStoppedCarsPosVid(data);
//			}
			//consecutive reports
			oldTC.addAllConsecutiveReports(tcState.getConsecutiveReportsList());
			newTC.addAllConsecutiveReports(tcState.getConsecutiveReportsList());
//			for(Seep.TollCalculatorState.DataII data : tcState.getConsecutiveReportsList()){
//				oldTC.addConsecutiveReports(data);
//				newTC.addConsecutiveReports(data);
//			}
			//VidLastPos
			oldTC.addAllVidLastPos(tcState.getVidLastPosList());
			newTC.addAllVidLastPos(tcState.getVidLastPosList());
//			for(Seep.TollCalculatorState.DataII data : tcState.getVidLastPosList()){
//				oldTC.addVidLastPos(data);
//				newTC.addVidLastPos(data);
//			}
System.out.println("ORIGINAL_STATE size: "+tcState.getSerializedSize());
//System.out.println("OLD_STATE size: "+oldTC.build().getSerializedSize());
//System.out.println("NEW_STATE size: "+newTC.build().getSerializedSize());
			oldTC.setCurrentMinute(tcState.getCurrentMinute());
			newTC.setCurrentMinute(tcState.getCurrentMinute());
			//This must be set here to indicate the type of message... this is a hack for the state in LRB
			oldTC.setStateId(1);
			newTC.setStateId(1);
			
			//save old and new buffer in appropiate structure
			oldBS.setTcState(oldTC);
			newBS.setTcState(newTC);
			splitted[0] = oldBS;
			splitted[1] = newBS;
			
		}
		else{
			System.out.println("ERROR NON STATE TO PARALLELIZE");
		}
		long bb = System.currentTimeMillis() - aa;
		System.out.println("ParallelizeState-TIME: "+bb);
		return splitted;
	}

	@Override
	public boolean isOrderSensitive() {
		// TODO Auto-generated method stub
		return false;
	}
}
