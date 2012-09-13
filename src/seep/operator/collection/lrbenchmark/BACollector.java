package seep.operator.collection.lrbenchmark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import seep.Main;
import seep.comm.Dispatcher.DispatchPolicy;
import seep.comm.tuples.Seep;
import seep.comm.tuples.Seep.InitState;
import seep.operator.Operator;
import seep.operator.StatefullOperator;
import seep.utils.ExecutionConfiguration;

@SuppressWarnings("serial")
public class BACollector extends Operator implements StatefullOperator{

	private int numUpstreamNodes = 0;
	
	//Map storing the number of times it has received any QID msg. QID-Integer
	private HashMap<Integer, Integer> queryNum = new HashMap<Integer, Integer>();
	//For every QiD the balance account accumulated till now.
	private HashMap<Integer, Integer> queryBA = new HashMap<Integer, Integer>();
	
	boolean firstTime = true;
	
	private int counter = 0;
	
	public BACollector(int opID) {
		super(opID);
System.out.println("NUM-UPStREAMS: "+numUpstreamNodes);
		subclassOperator = this;
		setDispatchPolicy(DispatchPolicy.ALL);
	}

	public void setCounter(int i){
		counter = i;
	}
	
long tinit = 0;
long t_start = 0;
	
	@Override
	public synchronized void processData(Seep.DataTuple dt) {
		Seep.DataTuple.Builder event = Seep.DataTuple.newBuilder(dt);
		//manually update this value (overhead but it is necessary for correctness of this operator) Furthermore, this is not a bottleneck
		numUpstreamNodes = getOpContext().upstreams.size();
		counter++;
		if(firstTime){
			firstTime = false;
			tinit = System.currentTimeMillis();
			t_start = System.currentTimeMillis();
		}
		
//		int t = (int) ((int)(System.currentTimeMillis() - tinit)/1000);
//	 	if(t > dt.getTime()+5){
//			System.out.println("BACol time: emit: "+dt.getTime()+" current: "+t);
//		}
	 	
//		if((System.currentTimeMillis()-t_start) >= 1000){
//			t_start = System.currentTimeMillis();
//			System.out.println("FW E/S: "+ackCounter);
////			System.out.println("FW E/S: "+counter);
//			ackCounter = 0;
////			counter = 0;
//		}
		
		int qid = dt.getQid();
		//initialize structures in case they are not yet.
		if(queryNum.get(qid) == null){
			queryNum.put(qid, 0);
			queryBA.put(qid, dt.getBa());
		}
		int numReceived = queryNum.get(qid);
		numReceived++;
		//If we have received all responses for the given query
//System.out.println("numReceived: "+numReceived+" numUpstreams: "+numUpstreamNodes);
		if(numReceived == numUpstreamNodes){
			int totalBA = 0;
			if(queryBA.get(qid) != null){
				totalBA = queryBA.get(qid) + dt.getBa();
			}
			else{
				totalBA = 0;
			}
			queryNum.put(qid, 0);
			event.setBa(totalBA);
//System.out.println("forwarding to sink");
			sendDown(event.build());
		}
		else{
			queryNum.put(qid, numReceived);
			int cummulated = 0;
			if(queryBA.get(qid) != null){
				cummulated = queryBA.get(qid);
				cummulated = cummulated + dt.getBa();
			}
			else{
				cummulated = 0;
			}
			queryBA.put(qid, cummulated);
		}
	}

	@Override
	public void generateBackupState() {
		Seep.BalanceAccountCollectorState.Builder bacS = Seep.BalanceAccountCollectorState.newBuilder();
		//query num
		List<Integer> qid = null;
		synchronized(queryNum){
			qid = new ArrayList<Integer>(queryNum.keySet());
		}
		Integer qidA[] = qid.toArray(new Integer[0]);
		List<Integer> num = null;
		synchronized(queryNum){
			num = new ArrayList<Integer>(queryNum.values());
		}
			Integer numA[] = num.toArray(new Integer[0]);
			Seep.BalanceAccountCollectorState.DataII.Builder bac = Seep.BalanceAccountCollectorState.DataII.newBuilder();
			for(int i = 0; i<qidA.length; i++){
				bac.setKey(qidA[i]);
				bac.setValue(numA[i]);
				bacS.addQueryNum(bac.build());
			}
			
			//query num
			qid = null;
			synchronized(queryBA){
				qid = new ArrayList<Integer>(queryBA.keySet());
			}
			qidA = qid.toArray(new Integer[0]);
			num = null;
			synchronized(queryBA){
				num = new ArrayList<Integer>(queryBA.values());
			}
			numA = num.toArray(new Integer[0]);
			Seep.BalanceAccountCollectorState.DataII.Builder bac2 = Seep.BalanceAccountCollectorState.DataII.newBuilder();
			for(int i = 0; i<qidA.length; i++){
				bac2.setKey(qidA[i]);
				bac2.setValue(numA[i]);
				bacS.addQueryBA(bac2.build());
			}
			numUpstreamNodes = bacS.getNumUpstreams();
		
	}

	@Override
	public int getCounter() {
		return counter;
	}

	@Override
	public void installState(InitState is) {
		queryNum = new HashMap<Integer, Integer>();
		Seep.BalanceAccountCollectorState tcS = is.getBacState();
		Seep.BalanceAccountCollectorState.DataII bcAccount = null;
		for(int i = 0; i < tcS.getQueryNumCount(); i++){
			bcAccount = tcS.getQueryNum(i);
			queryNum.put(bcAccount.getKey(), bcAccount.getValue());
		}
		
		queryBA = new HashMap<Integer, Integer>();
		Seep.BalanceAccountCollectorState.DataII bcAccount1 = null;
		for(int i = 0; i < tcS.getQueryBACount(); i++){
			bcAccount1 = tcS.getQueryBA(i);
			queryBA.put(bcAccount1.getKey(), bcAccount1.getValue());
		}
		
		System.out.println("OP"+getOperatorId()+" -> has restored state");
	}

	@Override
	public long getBackupTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isOrderSensitive() {
		// TODO Auto-generated method stub
		return false;
	}
}
