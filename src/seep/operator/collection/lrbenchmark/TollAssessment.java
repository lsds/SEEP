package seep.operator.collection.lrbenchmark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import seep.comm.serialization.DataTuple;
import seep.comm.serialization.controlhelpers.InitState;
import seep.operator.Operator;
import seep.operator.StatefullOperator;
import seep.operator.workers.StateBackupWorker;

@SuppressWarnings("serial")
public class TollAssessment extends Operator implements StatefullOperator{

	//Store for every vehicle the last toll notification, that will be the toll assessed in the future
	private HashMap<Integer, Integer> previousToll = new HashMap<Integer, Integer>(1000);
	//Store for every vehicle the last segment
	private HashMap<Integer, Integer> previousSegment = new HashMap<Integer, Integer>(1000);
	//Store for every vehicle the balance account, sum of all the tolls assessed
	private HashMap<Integer, Integer> balanceAccount = new HashMap<Integer, Integer>(1000);
	//last updated time of balanceAccount
	private int lastUpdateOfBA = 0;
	
	private boolean firstTime = true;
	private long tinit = 0;
	
	private int counter = 0;
	private int backupTime = 30000;
	
	@Override
	public long getBackupTime() {
		return backupTime;
	}
	
	public TollAssessment(int opID) {
		super(opID);
		subclassOperator = this;
	}
	
	private void setCounter(int i) {
		counter = i;
	}
private long t_start = 0;
	
	
	
	public synchronized void processData(DataTuple dt) {
////if(dt.getType() == 2)
////System.out.println("TA: "+dt.getType());
//		counter++;
//		if(firstTime){
//			firstTime = false;
//			StateBackupWorker stw = new StateBackupWorker(this);
//			tinit = System.currentTimeMillis();
//			t_start = System.currentTimeMillis();
//			new Thread(stw).start();
//		}
//		
////		int t = (int) ((int)(System.currentTimeMillis() - tinit)/1000);
////	 	if(t > dt.getTime()+5){
////			System.out.println("TA time: emit: "+dt.getTime()+" current: "+t);
////		}
//		
////		if((System.currentTimeMillis()-t_start) >= 1000){
////		t_start = System.currentTimeMillis();
//////		System.out.println("FW E/S: "+ackCounter);
////		System.out.println("FW E/S: "+counter);
//////		printRoutingInfo();
//////		ackCounter = 0;
////		counter = 0;
////		}
//		
//		int vid = dt.getVid();
//		int xway = 1000000 * dt.getXway();
//		//east direction positive, west direction negative. all segments are stored in the system with +1 in order to avoid problems with 0
//		int segment = (dt.getDir() == 1) ? (dt.getSeg()+1)*-1 : (dt.getSeg())+1;
//		segment = segment + xway;
//		if(dt.getType() == 0){
//			//If the vehicle is entering a new segment...
////System.out.println("currentSeg: "+segment+" prevSeg: "+previousSegment.get(vid));
//			if(previousSegment.get(vid) != null){
//				if(segment != previousSegment.get(vid)){
////System.out.println("new segment");
//					if(previousToll.get(vid) != null){
//						int tollPrice = previousToll.get(vid);
//						int currentBalanceAccount = 0;
//						if(balanceAccount.get(vid) != null){
//							currentBalanceAccount = balanceAccount.get(vid);
//						}
//						else{
////System.out.println("toll price registered");
//							balanceAccount.put(vid, tollPrice);
//						}
//						currentBalanceAccount = currentBalanceAccount + tollPrice;
////System.out.println("current ba: "+currentBalanceAccount);
//						balanceAccount.put(vid, currentBalanceAccount);
//						lastUpdateOfBA = (int)(System.currentTimeMillis() - tinit)/1000;
//					}
//					else{
//						previousToll.put(vid, dt.getToll());
//					}
//				}
//			}
//			else{
//				previousSegment.put(vid, segment);
//			}
//		}		
//		else if(dt.getType() == 2){
////System.out.println("VID: "+dt.getVid()+" BA request, notifying...");
//			Seep.DataTuple.Builder event = Seep.DataTuple.newBuilder(dt);
//			if(balanceAccount.get(vid) == null){
//				balanceAccount.put(vid, 0);
//			}
//			event.setBa(balanceAccount.get(vid));
//			event.setResultTime(lastUpdateOfBA);
////System.out.println("VID: "+dt.getVid()+" BA total: "+dt.getBa());
//			sendDown(event.build());
//		}
//		
//		//UPDATE previous segment of vehicles
//		previousSegment.put(vid, segment);
//		previousToll.put(vid, dt.getToll());
//		
	}

//	@Override
//	public synchronized void generateBackupState() {
//		
//			Seep.BalanceAccountState.Builder baS = Seep.BalanceAccountState.newBuilder();
//			//balance account
//			List<Integer> vids = null;
//			synchronized(balanceAccount){
//				vids = new ArrayList<Integer>(balanceAccount.keySet());
//			}
//			Integer vidsA[] = vids.toArray(new Integer[0]);
//			List<Integer> sum = null;
//			synchronized(balanceAccount){
//				sum = new ArrayList<Integer>(balanceAccount.values());
//			}
//			Integer sumA[] = sum.toArray(new Integer[0]);
//			Seep.BalanceAccountState.DataII.Builder ba = Seep.BalanceAccountState.DataII.newBuilder();
//			for(int i = 0; i<vidsA.length; i++){
//				ba.setKey(vidsA[i]);
//				ba.setValue(sumA[i]);
//				baS.addBalanceAccount(ba.build());
//			}
//			
//			//previous segment
//			vids = null;
//			synchronized(previousSegment){
//				vids = new ArrayList<Integer>(previousSegment.keySet());
//			}
//			vidsA = vids.toArray(new Integer[0]);
//			sum = null;
//			synchronized(previousSegment){
//				sum = new ArrayList<Integer>(previousSegment.values());
//			}
//			sumA = sum.toArray(new Integer[0]);
//			Seep.BalanceAccountState.DataII.Builder ps = Seep.BalanceAccountState.DataII.newBuilder();
//			for(int i = 0; i<vidsA.length; i++){
//				ps.setKey(vidsA[i]);
//				ps.setValue(sumA[i]);
//				baS.addPreviousSegment(ps.build());
//			}
//			
//			//previous toll
//			vids = null;
//			synchronized(previousToll){
//				vids = new ArrayList<Integer>(previousToll.keySet());
//			}
//			vidsA = vids.toArray(new Integer[0]);
//			sum = null;
//			synchronized(previousToll){
//				sum = new ArrayList<Integer>(previousToll.values());
//			}
//			sumA = sum.toArray(new Integer[0]);
//			Seep.BalanceAccountState.DataII.Builder pt = Seep.BalanceAccountState.DataII.newBuilder();
//			for(int i = 0; i<vidsA.length; i++){
//				pt.setKey(vidsA[i]);
//				pt.setValue(sumA[i]);
//				baS.addPreviousToll(pt.build());
//			}
//			
//			baS.setLastUpdateOfBA(lastUpdateOfBA);
//			//To indicate that this is a toll assessment state
//			baS.setStateId(1);
//
//			Seep.BackupState.Builder bsB = Seep.BackupState.newBuilder();
//			//Developer needs to save just the state
//			bsB.setBaState(baS.build());
//			backupState(bsB);
//			setCounter(0);
//		
//		
//	}

	@Override
	public int getCounter() {
		return counter;
	}

//	@Override
//	public void installState(InitState is) {
//		
//		balanceAccount.clear();
//		previousSegment.clear();
//		previousToll.clear();
//		
//		balanceAccount = new HashMap<Integer, Integer>();
//		Seep.BalanceAccountState baS = is.getBaState();
//		Seep.BalanceAccountState.DataII bAccount = null;
//		for(int i = 0; i < baS.getBalanceAccountCount(); i++){
//			bAccount = baS.getBalanceAccount(i);
//			balanceAccount.put(bAccount.getKey(), bAccount.getValue());
//		}
//		
//		previousSegment = new HashMap<Integer, Integer>();
//		Seep.BalanceAccountState.DataII pSegment = null;
//		for(int i = 0; i < baS.getPreviousSegmentCount(); i++){
//			pSegment = baS.getPreviousSegment(i);
//			previousSegment.put(pSegment.getKey(), pSegment.getValue());
//		}
//		
//		previousToll = new HashMap<Integer, Integer>();
//		Seep.BalanceAccountState.DataII pToll = null;
//		for(int i = 0; i < baS.getPreviousTollCount(); i++){
//			pToll = baS.getPreviousToll(i);
//			previousToll.put(pToll.getKey(), pToll.getValue());
//		}
//		
//		lastUpdateOfBA = baS.getLastUpdateOfBA();
//		
//		System.out.println("OP"+getOperatorId()+" -> has restored state");
//	}

	@Override
	public boolean isOrderSensitive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void installState(InitState is) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void generateBackupState() {
		// TODO Auto-generated method stub
		
	}
}
