package seep.operator.collection.lrbenchmark;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import seep.Main;
import seep.comm.tuples.Seep;
import seep.comm.tuples.Seep.BackupState;
import seep.comm.tuples.Seep.InitState;
import seep.comm.tuples.Seep.BackupState.Builder;
import seep.operator.Operator;
import seep.operator.StateSplitI;
import seep.operator.StatefullOperator;
import seep.operator.workers.StateBackupWorker;

@SuppressWarnings("serial")
public class TollCalculator extends Operator implements StatefullOperator, StateSplitI{

	//Store the stopped cars in every position. position-car
	private HashMap<Integer, ArrayList<Integer>> stoppedCars_pos_vid = new HashMap<Integer, ArrayList<Integer>>(1000);
	//store previous vehicle positions. car-segment
	private HashMap<Integer, Integer> vid_lastPos = new HashMap<Integer, Integer>(1000);
	//store number of consecutive reports from same segment. vehicle-number_of_reports
	private HashMap<Integer, Integer> consecutiveReports = new HashMap<Integer, Integer>(1000);
	//stores per segment if there is an accident (true) or not
	private HashMap<Integer, Boolean> accidents = new HashMap<Integer, Boolean>(200);
	
	//segment num_vehicles last minute and current minute. segment - num
	private HashMap<Integer, ArrayList<Integer>> numVidCurrentMinSeg_seg_listVid = new HashMap<Integer, ArrayList<Integer>>(200);
	private HashMap<Integer, Integer> numVidLastMinSeg_seg_numVid = new HashMap<Integer, Integer>(200);
	
	//store vehicle-speed for the currentMinute
	private HashMap<Integer, Integer> currentVidSpeed_vid_speed = new HashMap<Integer, Integer>(1000);
	//average speed of vehicles past 5 min. segment - avg
	private HashMap<Integer, Integer> speedAvg1Minutes = new HashMap<Integer, Integer>(200);
	private HashMap<Integer, Integer> speedAvg2Minutes = new HashMap<Integer, Integer>(200);
	private HashMap<Integer, Integer> speedAvg3Minutes = new HashMap<Integer, Integer>(200);
	private HashMap<Integer, Integer> speedAvg4Minutes = new HashMap<Integer, Integer>(200);
	private HashMap<Integer, Integer> speedAvg5Minutes = new HashMap<Integer, Integer>(200);
	
	private int currentMinute = 1;
	private boolean firstTime = true;
	
	private int counter = 0;
	private int backupTime = 20000;
	
	public TollCalculator(int opID) {
		super(opID);
		subclassOperator = this;
		//for(int i = -25105; i<25105; i++){
//		for(int i = -1105; i<1105; i++){
//			accidents.put(i, false);
//		}
		/// \todo Consider moving this piece of code to main (to compose from the interface).
//		ContentBasedFilter cbf = new ContentBasedFilter("getType");
//		// toll notification to both toll collector and toll assesssment
//		cbf.routeValueToDownstream(RelationalOperator.EQ, 0, 31);
//		cbf.routeValueToDownstream(RelationalOperator.EQ, 0, 20);
//		// accident only to toll collector
//		cbf.routeValueToDownstream(RelationalOperator.EQ, 1, 31);
//		setDispatchPolicy(DispatchPolicy.CONTENT_BASED, cbf);
		
	}
	
	private void setCounter(int i) {
		counter = i;
	}
	
	@Override
	public long getBackupTime() {
		return backupTime;
	}
//TESTING
long t_start = 0;
long tinit = 0;
	
	@Override
	public synchronized void processData(Seep.DataTuple dt) {
		int value2 = dt.getSeg();
		int xway2 = dt.getXway();
		int dir2 = dt.getDir();
		value2 = value2 + (1000*xway2);
		value2 = (dir2 == 1) ? (value2+1)*-1 : (value2)+1;
//System.out.println("TC, seg: "+value2);
//long a = System.currentTimeMillis();
		if(firstTime){
			firstTime = false;
			StateBackupWorker stw = new StateBackupWorker(this);
			t_start = System.currentTimeMillis();
			tinit = System.currentTimeMillis();
			new Thread(stw).start();
		}
		counter++;
		
//		int t = (int) ((int)(System.currentTimeMillis() - tinit)/1000);
//	 	if(t > dt.getTime()+5){
//			System.out.println("TC time: emit: "+dt.getTime()+" current: "+t);
//		}
		
//	 	if((System.currentTimeMillis()-t_start) >= 1000){
//			t_start = System.currentTimeMillis();
////			System.out.println("FW E/S: "+ackCounter);
//			System.out.println("TC E/S: "+counter);
////			printRoutingInfo();
////			ackCounter = 0;
//			counter = 0;
//		}
	 	
		/** INITIALIZATION **/
		int vehicle = dt.getVid();
		long time = dt.getTime();
		int speed = dt.getSpeed();
		// We reduce the position, segment, dir, xway. Dir is indicated by the sign. xWay by the millionths, segment and position are expressed
		// in miles and feet.
		int xway = 1000 * dt.getXway();
		//east direction positive, west direction negative. all segments are stored in the system with +1 in order to avoid problems with 0
		int segment = dt.getSeg() + xway;
		segment = (dt.getDir() == 1) ? (segment+1)*-1 : (segment)+1;
		/**this is the value that uses the system to parallelize**/
		int value = segment;
		
		int avgSpeed = 0;
		int numVehiclesSegment = 0;
		int toll = 0;
		/** ACCIDENT LOGIC **/
		//execute logic for detecting accidents
		accidentDetector(segment, dt);
		
		/** UPDATE DATA STRUCTURES**/
		//What minute are we in?
		int minute = (int)(time/60) +1;
		//new minute, update data structures
		if(minute > currentMinute){
System.out.println("##MIN CHANGED: last-> "+currentMinute+" current -> "+minute);
long ab = System.currentTimeMillis();
			currentMinute = minute;
			//UPDATE SEGMENT SPEED STATISTICS
			speedAvg5Minutes = speedAvg4Minutes;
			speedAvg4Minutes = speedAvg3Minutes;
			speedAvg3Minutes = speedAvg2Minutes;
			speedAvg2Minutes = speedAvg1Minutes;
			speedAvg1Minutes = computeAvgSpeed(numVidCurrentMinSeg_seg_listVid, currentVidSpeed_vid_speed);
			currentVidSpeed_vid_speed = new HashMap<Integer, Integer>();
			//UPDATE SEGMENT NUMVEHICLES STATISTICS
//			numVidLastMinSeg_seg_numVid = null;
			numVidLastMinSeg_seg_numVid = computeNumVehicles(numVidCurrentMinSeg_seg_listVid);
			numVidCurrentMinSeg_seg_listVid = new HashMap<Integer, ArrayList<Integer>>();
long ba = System.currentTimeMillis();
System.out.println("##TIME TO UPDATE: "+(ba-ab));
		}
	
		//UPDATE CURRENT MINUTE STATISTICS
		//update num vehicles in current segment in this minute
		if (numVidCurrentMinSeg_seg_listVid.get(segment) != null) {
			//if the car was not registered yet in this segment we add it
//System.out.println("VID: "+vehicle+" was not? "+numVehiclesCurrentMinuteSegment.get(segment).indexOf(vehicle));
			if(!numVidCurrentMinSeg_seg_listVid.get(segment).contains(vehicle)){
				if(numVidCurrentMinSeg_seg_listVid == null){
System.out.println("ESTO ES NULL:::: ");

				}
				if (numVidCurrentMinSeg_seg_listVid.get(segment) == null) {
System.out.println("NUMVID.get(segment) NULL NULL seg: "+segment);
				}
				if(numVidCurrentMinSeg_seg_listVid.get(segment).contains(vehicle)) numVidCurrentMinSeg_seg_listVid.get(segment).add(vehicle);
//System.out.println("num V in this seg: "+numVehiclesCurrentMinuteSegment.get(segment).size());
			}
		}
		else{
			ArrayList<Integer> aux = new ArrayList<Integer>();
			aux.add(vehicle);
			numVidCurrentMinSeg_seg_listVid.put(segment, aux);
		}
		//if there is a vehicle that has reported
		if (currentVidSpeed_vid_speed.get(vehicle) != null){
			//calculate average of vehicle and store it
			int vSpeed = (int)((int)(currentVidSpeed_vid_speed.get(vehicle) + speed)) / 2;
			currentVidSpeed_vid_speed.put(vehicle, vSpeed);
		}
		//if the vehicle has not reported yet, store its speed
		else{
			currentVidSpeed_vid_speed.put(vehicle, speed);
		}
		/** TOLL CALCULATION LOGIC **/
		//IF NOT ACCIDENT AREA
		if(!accidentArea(segment)){
			//IF NOT EXIT LANE
			if(dt.getLane() != 4){
				//IF NOT LESS THAN 50 VEHICLES in the previous minute
				if(numVidLastMinSeg_seg_numVid.get(segment) != null){
					numVehiclesSegment = numVidLastMinSeg_seg_numVid.get(segment);
//					if (numVehiclesSegment > 40)System.out.println("<-- NUMVEHICLES --> "+numVehiclesSegment);
					if(numVehiclesSegment > 50){
						avgSpeed = computeSpeedLast5Min(segment);
						//IF NOT FASTER THAN 40mph
						if(avgSpeed < 40){
							toll = (int) (2 * Math.pow((numVehiclesSegment-50), 2));
//System.out.println("toll "+toll);
						}
						//IF FASTER THAN 40mph -> toll is 0
						else{
//System.out.println("faster than 40 "+avgSpeed);
							toll = 0;
						}
					}
					//IF LESS THAN 50 VEHICLES -> toll is 0
					else{
						toll = 0;
//System.out.println("num?vehicles last min: "+numVehiclesSegment);
					}
				}
				else{
					//this means that it is the first minute, previously were 0 vehicles, so toll = 0
					toll = 0;
//System.out.println("first minute");
				}
			}
			//IF EXIT LANE -> toll is 0
			else{
				toll = 0;
//System.out.println("exit lane: "+dt.getLane());
			}
		}
		//IF ACCIDENT AREA -> toll is 0
		else{
			notifyAccident(dt);
//System.out.println("accident area: "+segment);
			return;
		}
		//Once toll has been calculated, emit the output tuple.
		Seep.DataTuple.Builder event = Seep.DataTuple.newBuilder(dt);
		event.setType(0);
		event.setSpeed(avgSpeed);
		event.setToll(toll);
		//finally send toll event notification
//long aa = System.currentTimeMillis();
		sendDown(event.build(), value);
//long ee = System.currentTimeMillis();
//if((ee-aa) > 5){
//	System.out.println("notify: "+(ee-aa));
//}
//		
//long e = System.currentTimeMillis();
//if((e-a)>50){
//System.out.println("btnck? "+(e-a));
//}
		if(counter == backupTime ){
			//generateBackupState();
			counter = 0;
		}
	}

	private HashMap<Integer, Integer> computeAvgSpeed(HashMap<Integer, ArrayList<Integer>> numVehiclesCurrentMinuteSegment,	HashMap<Integer, Integer> vehicleSpeedMin) {
		HashMap<Integer, Integer> updated = new HashMap<Integer, Integer>();
		//for each segment
		ArrayList<Integer> keySet = null;
		synchronized(numVehiclesCurrentMinuteSegment){
			keySet = new ArrayList<Integer>(numVehiclesCurrentMinuteSegment.keySet());
		}
		for(Integer s : keySet){
//System.out.println("CAS_ SEGMENT: "+s);
			//for each vehicle in the segment
			int avgSpeedInSegment = 0;
			int numVid = 0;
			if(numVehiclesCurrentMinuteSegment.containsKey(s)){
				for(Integer vid : numVehiclesCurrentMinuteSegment.get(s)){
					//If the vehicle has a previous stored speed in the segment
					if(vehicleSpeedMin.get(vid) != null){
						//compute average speed
						avgSpeedInSegment = avgSpeedInSegment + vehicleSpeedMin.get(vid);
						numVid++;
					}
				}
			}
			int avgSegment = (avgSpeedInSegment == 0) ? 0 : avgSpeedInSegment/numVid;
			//save updated speed per segment
			updated.put(s, avgSegment);
		}
		return updated;
	}

	private HashMap<Integer, Integer> computeNumVehicles(HashMap<Integer, ArrayList<Integer>> numVehiclesCurrentMinuteSegment) {
		HashMap<Integer, Integer> updated = new HashMap<Integer, Integer>();
		//for each segment
		for(Integer i : numVehiclesCurrentMinuteSegment.keySet()){
			//count number of vehicles and update
			updated.put(i, numVehiclesCurrentMinuteSegment.get(i).size());
//System.out.println("In segment "+i+" numVehicles: "+numVehiclesCurrentMinuteSegment.get(i).size());
		}
		return updated;
	}

	private int computeSpeedLast5Min(int s) {
		int speed = 0;
		int aux = 0;
	//System.out.println("SEGMENT: "+s);
		if(speedAvg5Minutes.containsKey(s)){
			speed = (int)speedAvg5Minutes.get(s);
			aux++;
		}
		if(speedAvg4Minutes.containsKey(s)){
			speed = speed + speedAvg4Minutes.get(s);
			aux++;
		}
		if(speedAvg3Minutes.containsKey(s)){
			speed = speed + speedAvg3Minutes.get(s);
			aux++;
		}
		if(speedAvg2Minutes.containsKey(s)){
			speed = speed + speedAvg2Minutes.get(s);
			aux++;
		}
		if(speedAvg1Minutes.containsKey(s)){
			speed = speed + speedAvg1Minutes.get(s);
			aux++;
		}
		if(aux > 0) speed = speed/aux;
		return speed;
	}

	private boolean accidentArea(int seg) {
//System.out.println("ACCESSING TO THIS SEGMENT: "+seg);
//System.out.println("ACCIDENTS length: "+accidents.size());
		if(accidents.get(seg) != null && accidents.get(seg) || 
		   accidents.get(seg-1) != null && accidents.get(seg - 1) || 
		   accidents.get(seg-2) != null && accidents.get(seg - 2) || 
		   accidents.get(seg-3) != null && accidents.get(seg - 3)|| 
		   accidents.get(seg-4) != null && accidents.get(seg - 4)) return true;
		else return false;
	}

	private void notifyAccident(Seep.DataTuple dt) {
		Seep.DataTuple.Builder event = Seep.DataTuple.newBuilder(dt);
		// if there is an accident, there is no toll
		event.setToll(0);
		//send accident notification
		event.setType(1);
//System.out.println("Segment: "+dt.getSeg()+" Accident near");
long a = System.currentTimeMillis();
		sendDown(event.build());
long e = System.currentTimeMillis();
if((e-a) > 5){
	System.out.println("ACC-AREA: "+(e-a));
}
	}

	private void accidentDetector(int segment, Seep.DataTuple dt) {
		//var to work with
		int vehicle = dt.getVid();
		int lane = dt.getLane();
		int xway = 1000000 * dt.getXway();
		int currentPosition = dt.getPos() + xway;
		currentPosition = (dt.getDir() == 1) ? (currentPosition+1)*-1 : currentPosition+1;
		//get this vehicle previous position report
		
		if(vid_lastPos.get(vehicle) != null){
			//get this vehicle previous position report
			int prevPosition = vid_lastPos.get(vehicle);
			// if the previous position is the same as current one
			if(prevPosition == currentPosition){
				//If the car was still registered as stopped in the previous pos...
				if(stoppedCars_pos_vid.get(prevPosition) != null && stoppedCars_pos_vid.get(prevPosition).contains(vehicle)){
					System.out.println("Vehicle "+vehicle+" was previously stopped, ignore pos report");
					return;
				}
				//System.out.println("Vehicle: "+vehicle+" reported same consecutive position");
				// check how many times has reported the same position and increment the counter
				if(consecutiveReports.get(vehicle) != null){
					int aux = consecutiveReports.get(vehicle);
					aux++;
					// if it has reported 4 times, means it is stopped
					if(aux > 3){
						//System.out.println("Vehicle: "+vehicle+" 4 consect same position reports");
						// check if there is another car stopped in the same position
						if(stoppedCars_pos_vid.get(currentPosition) != null){
							if (stoppedCars_pos_vid.get(currentPosition).size() > 0){
								System.out.println("Vehicle: "+vehicle+" ACCIDENT");
								stoppedCars_pos_vid.get(currentPosition).add(vehicle);
								accidents.put(dt.getSeg(), true);
							}
							else{
								//System.out.println("Vehicle: "+vehicle+" is stopped at pos: "+position);
								ArrayList<Integer> cars = stoppedCars_pos_vid.get(currentPosition);
								cars.add(vehicle);
								stoppedCars_pos_vid.put(currentPosition, cars);
							}
						}
						else{
							//System.out.println("Vehicle: "+vehicle+" is stopped at pos: "+position);
							ArrayList<Integer> cars = new ArrayList<Integer>();
							cars.add(vehicle);
							stoppedCars_pos_vid.put(currentPosition, cars);
						}
					}
					// if not, indicate the number of consecutive position reports
					else{
						consecutiveReports.put(vehicle, aux);
					}
				}
				else{
					//notify of this report
					consecutiveReports.put(vehicle, 1);
				}
			}
			else{
				//if not same position but previous one was accident, clear accident
				if(stoppedCars_pos_vid.get(prevPosition) != null && stoppedCars_pos_vid.get(prevPosition).indexOf(vehicle) != -1){
					//System.out.println("Vehicle "+vehicle+" ACCIDENT CLEAR");
					//it is no longer in an accident
					ArrayList<Integer> cars = stoppedCars_pos_vid.get(prevPosition);
					//remove from stopped cars
					int indexToRemove = cars.indexOf(vehicle);
					cars.remove(indexToRemove);
					stoppedCars_pos_vid.put(prevPosition, cars);
					//from position (feet) get segment
		//in pos, xway is expressed in the millionths, in segment in the thousands.
					int s = (int) (prevPosition-xway)/5280;
					s = s + 1000*dt.getXway();
					s = (dt.getDir() == 1) ? (s+1)*-1 : s+1;
					accidents.put(s, false);
				}
				// if not, then update vehicle position
//CHECK LANE 4
				if(lane != 4){
					vid_lastPos.put(vehicle, currentPosition);
				}
				else{
					//if the vehicle is exiting the xway we remove it from this db
					vid_lastPos.remove(vehicle);
				}
				//reset the consecutive reports
				//consecutiveReports.put(vehicle, 0);
				//to try to make state smaller
				consecutiveReports.remove(vehicle);
			}
		}
		else{
			vid_lastPos.put(vehicle, currentPosition);
		}
	}

	@Override
	public synchronized void generateBackupState() {
long a = System.currentTimeMillis();
		
			Seep.TollCalculatorState.Builder tcS = Seep.TollCalculatorState.newBuilder();
			//avg Speed min 5
			List<Integer> segment = null;
			synchronized(speedAvg5Minutes){
				segment = new ArrayList<Integer>(speedAvg5Minutes.keySet());
			}
			/*
			for(Integer i : segment){
				System.out.println("avg5 key: "+i);
			}
			*/
//System.out.println("speedAvg5Minutes: "+segment.size());
			Integer segmentA[] = segment.toArray(new Integer[0]);
			List<Integer> speeds = null;
			synchronized(speedAvg5Minutes){
				speeds = new ArrayList<Integer>(speedAvg5Minutes.values());
			}
			Integer speedsA[] = speeds.toArray(new Integer[0]);
			Seep.TollCalculatorState.DataII.Builder dataii5B = Seep.TollCalculatorState.DataII.newBuilder();
			for(int i = 0; i<segmentA.length; i++){
				dataii5B.setKey(segmentA[i]);
				dataii5B.setValue(speedsA[i]);
				tcS.addSpeedAvg5Minutes(dataii5B.build());
			}
//tcS.setCurrentMinute(currentMinute);
//Seep.TollCalculatorState aux = tcS.build();
//System.out.println("AVG5: "+aux.getSerializedSize());
			
			//avg Speed min 4
			segment = null;
			synchronized(speedAvg4Minutes){
				segment = new ArrayList<Integer>(speedAvg4Minutes.keySet());
			}
//System.out.println("speedAvg4Minutes: "+segment.size());
			segmentA = segment.toArray(new Integer[0]);
			speeds = null;
			synchronized(speedAvg4Minutes){
				speeds = new ArrayList<Integer>(speedAvg4Minutes.values());
			}
			speedsA = speeds.toArray(new Integer[0]);
			Seep.TollCalculatorState.DataII.Builder dataii4B = Seep.TollCalculatorState.DataII.newBuilder();
			for(int i = 0; i<segmentA.length; i++){
				dataii4B.setKey(segmentA[i]);
				dataii4B.setValue(speedsA[i]);
				tcS.addSpeedAvg4Minutes(dataii4B.build());
			}
//Seep.TollCalculatorState aux2 = tcS.build();
//System.out.println("AVG5-4: "+aux2.getSerializedSize());	
			//avg Speed min 3
			segment = null;
			synchronized(speedAvg3Minutes){
				segment = new ArrayList<Integer>(speedAvg3Minutes.keySet());
			}
//System.out.println("speedAvg3Minutes: "+segment.size());
			segmentA = segment.toArray(new Integer[0]);
			speeds = null;
			synchronized(speedAvg3Minutes){
				speeds = new ArrayList<Integer>(speedAvg3Minutes.values());
			}
			speedsA = speeds.toArray(new Integer[0]);
			Seep.TollCalculatorState.DataII.Builder dataii3B = Seep.TollCalculatorState.DataII.newBuilder();
			for(int i = 0; i<segmentA.length; i++){
				dataii3B.setKey(segmentA[i]);
				dataii3B.setValue(speedsA[i]);
				tcS.addSpeedAvg3Minutes(dataii3B.build());
			}
//Seep.TollCalculatorState aux3 = tcS.build();
//System.out.println("AVG5-4-3: "+aux3.getSerializedSize());
			//avg Speed min 2
			segment = null;
			synchronized(speedAvg2Minutes){
				segment = new ArrayList<Integer>(speedAvg2Minutes.keySet());
			}
//System.out.println("speedAvg2Minutes: "+segment.size());
			segmentA = segment.toArray(new Integer[0]);
			speeds = null;
			synchronized(speedAvg2Minutes){
				speeds = new ArrayList<Integer>(speedAvg2Minutes.values());
			}
			speedsA = speeds.toArray(new Integer[0]);
			Seep.TollCalculatorState.DataII.Builder dataii2B = Seep.TollCalculatorState.DataII.newBuilder();
			for(int i = 0; i<segmentA.length; i++){
				dataii2B.setKey(segmentA[i]);
				dataii2B.setValue(speedsA[i]);
				tcS.addSpeedAvg2Minutes(dataii2B.build());
			}
			
//Seep.TollCalculatorState aux4 = tcS.build();
//System.out.println("AVG5-4-3-2: "+aux4.getSerializedSize());		
			//avg Speed min 1
			segment = null;
			synchronized(speedAvg1Minutes){
				segment = new ArrayList<Integer>(speedAvg1Minutes.keySet());
			}
//System.out.println("speedAvg1Minutes: "+segment.size());
			segmentA = segment.toArray(new Integer[0]);
			speeds = null;
			synchronized(speedAvg1Minutes){
				speeds = new ArrayList<Integer>(speedAvg1Minutes.values());
			}
			speedsA = speeds.toArray(new Integer[0]);
			Seep.TollCalculatorState.DataII.Builder dataii1B = Seep.TollCalculatorState.DataII.newBuilder();
			for(int i = 0; i<segmentA.length; i++){
				dataii1B.setKey(segmentA[i]);
				dataii1B.setValue(speedsA[i]);
				tcS.addSpeedAvg1Minutes(dataii1B.build());
			}
	
//Seep.TollCalculatorState aux5 = tcS.build();
//System.out.println("AVG5-4-3-2-1: "+aux5.getSerializedSize());			
			
			//avg Speed current
			segment = null;
			synchronized(currentVidSpeed_vid_speed){
				segment = new ArrayList<Integer>(currentVidSpeed_vid_speed.keySet());
			}
//System.out.println("currentVidSpeed_vid_speed: "+segment.size());
			segmentA = segment.toArray(new Integer[0]);
			speeds = null;
			synchronized(currentVidSpeed_vid_speed){
				speeds = new ArrayList<Integer>(currentVidSpeed_vid_speed.values());
			}
			speedsA = speeds.toArray(new Integer[0]);
			Seep.TollCalculatorState.DataII.Builder dataiiCB = Seep.TollCalculatorState.DataII.newBuilder();
			for(int i = 0; i<segmentA.length; i++){
				dataiiCB.setKey(segmentA[i]);
				dataiiCB.setValue(speedsA[i]);
				tcS.addCurrentVidSpeedVidSpeed(dataiiCB.build());
			}

//Seep.TollCalculatorState aux6 = tcS.build();
//System.out.println("AVG's-current: "+aux6.getSerializedSize());
			
			//last minute num vehicles
			segment = null;
			synchronized(numVidLastMinSeg_seg_numVid){
				segment = new ArrayList<Integer>(numVidLastMinSeg_seg_numVid.keySet());
			}
//System.out.println("numVidLastMinSeg_seg_numVid: "+segment.size());
		/*	for(Integer i : segment){
				System.out.println("numVehLastMinuteSegment key: "+i);
			}*/
			segmentA = segment.toArray(new Integer[0]);
			speeds = null;
			synchronized(numVidLastMinSeg_seg_numVid){
				speeds = new ArrayList<Integer>(numVidLastMinSeg_seg_numVid.values());
			}
			speedsA = speeds.toArray(new Integer[0]);
			Seep.TollCalculatorState.DataII.Builder dataiiNVB = Seep.TollCalculatorState.DataII.newBuilder();
			for(int i = 0; i<segmentA.length; i++){
//System.out.println("NON EMPTY NON EMPTY: "+segmentA[i]);
				dataiiNVB.setKey(segmentA[i]);
				dataiiNVB.setValue(speedsA[i]);
				tcS.addNumVidLastMinSegSegNumVid(dataiiNVB.build());
			}
		
//Seep.TollCalculatorState aux7 = tcS.build();
//System.out.println("til numVidLastSeg: "+aux7.getSerializedSize());	
			
			//consecutive reports
			segment = null;
			synchronized(consecutiveReports){
				segment = new ArrayList<Integer>(consecutiveReports.keySet());
			}
//System.out.println("consecutiveReports: "+segment.size());
			/*for(Integer i : segment){
				System.out.println("consecutiveRep key: "+i);
			}*/
			segmentA = segment.toArray(new Integer[0]);
			speeds = null;
			synchronized(consecutiveReports){
				speeds = new ArrayList<Integer>(consecutiveReports.values());
			}
			speedsA = speeds.toArray(new Integer[0]);
			Seep.TollCalculatorState.DataII.Builder cr = Seep.TollCalculatorState.DataII.newBuilder();
			for(int i = 0; i<segmentA.length; i++){
				cr.setKey(segmentA[i]);
				cr.setValue(speedsA[i]);
				tcS.addConsecutiveReports(cr.build());
			}
	
//Seep.TollCalculatorState aux8 = tcS.build();
//System.out.println("til consecRep: "+aux8.getSerializedSize());	
			
			//current minute num vehicles
			segment = null;
			synchronized(numVidCurrentMinSeg_seg_listVid){
				segment = new ArrayList<Integer>(numVidCurrentMinSeg_seg_listVid.keySet());
			}
			/*for(Integer i : segment){
				System.out.println("numVehicles Current MIn key: "+i);
			}*/
			segmentA = segment.toArray(new Integer[0]);
			speeds = null;
			ArrayList<Integer> aux = null;
//System.out.println("numVidCurrentMinSeg_seg_listVid: "+segmentA.length);
			for(int i = 0; i<segmentA.length; i++){
//				dataiiCVB.setKey(segmentA[i]);
				synchronized(numVidCurrentMinSeg_seg_listVid){
					 aux = numVidCurrentMinSeg_seg_listVid.get(i);
				}
				if(aux != null){
					Seep.TollCalculatorState.DataListI.Builder dataiiCVB = Seep.TollCalculatorState.DataListI.newBuilder();
					dataiiCVB.setKey(segmentA[i]);
//System.out.println("listSize: "+numVidCurrentMinSeg_seg_listVid.get(i).size());
					for(Integer v : aux){
						if(v != null){
							dataiiCVB.addValue(v);
						}
						else{
System.out.println("621 TOLL CALCULATOR NULL");
						}
					}
					tcS.addNumVidCurrentMinSegSegListVid(dataiiCVB.build());
				}
//				tcS.addNumVidCurrentMinSegSegListVid(dataiiCVB.build());
			}
			
//Seep.TollCalculatorState aux9 = tcS.build();
//System.out.println("til numVidCurrentSeg: "+aux9.getSerializedSize());
			
			//stopped cars
			segment = null;
			synchronized(stoppedCars_pos_vid){
				segment = new ArrayList<Integer>(stoppedCars_pos_vid.keySet());
			}
//System.out.println("stoppedCars_pos_vid: "+segment.size());
			segmentA = segment.toArray(new Integer[0]);
			speeds = null;
			aux = null;
			for(int i = 0; i<segmentA.length; i++){
				synchronized(stoppedCars_pos_vid){
					 aux = stoppedCars_pos_vid.get(i);
				}
//				sc.setKey(segmentA[i]);
				if(aux != null){
					Seep.TollCalculatorState.DataListI.Builder sc = Seep.TollCalculatorState.DataListI.newBuilder();
					sc.setKey(segmentA[i]);
					for(Integer v : aux){
						sc.addValue(v);
					}
					tcS.addStoppedCarsPosVid(sc.build());
				}
			}
			
//Seep.TollCalculatorState aux10 = tcS.build();
//System.out.println("til stopeCar: "+aux10.getSerializedSize());	

			//last v segment
			segment = null;
			synchronized(vid_lastPos){
				segment = new ArrayList<Integer>(vid_lastPos.keySet());
			}
//System.out.println("vid_lastPos: "+segment.size());
			segmentA = segment.toArray(new Integer[0]);
			speeds = null;
			synchronized(vid_lastPos){
				speeds = new ArrayList<Integer>(vid_lastPos.values());
			}
			speedsA = speeds.toArray(new Integer[0]);
			Seep.TollCalculatorState.DataII.Builder stopCB = Seep.TollCalculatorState.DataII.newBuilder();
			for(int i = 0; i<segmentA.length; i++){
				stopCB.setKey(segmentA[i]);
				stopCB.setValue(speedsA[i]);
				tcS.addVidLastPos(stopCB.build());
			}
	
//Seep.TollCalculatorState aux11 = tcS.build();
//System.out.println("til vid_lastPos: "+aux11.getSerializedSize());
			
			//accidents
			segment = null;
			synchronized(accidents){
				segment = new ArrayList<Integer>(accidents.keySet());
			}
//System.out.println("accidents: "+segment.size());
			segmentA = segment.toArray(new Integer[0]);
			List<Boolean> bool = null;
			synchronized(accidents){
				bool = new ArrayList<Boolean>(accidents.values());
			}
			Boolean boolA[] = bool.toArray(new Boolean[0]);
			Seep.TollCalculatorState.DataBool.Builder dataBoolB = Seep.TollCalculatorState.DataBool.newBuilder();
			for(int i = 0; i<segmentA.length; i++){
				dataBoolB.setKey(segmentA[i]);
				dataBoolB.setValue(boolA[i]);
				tcS.addAccidents(dataBoolB.build());
			}
			
//Seep.TollCalculatorState aux12 = tcS.build();
//System.out.println("til accidents: "+aux12.getSerializedSize());
			
			tcS.setCurrentMinute(currentMinute);
			
			//to indicate that this backupstate has a toll calculator state.
			tcS.setStateId(1);
			
			Seep.BackupState.Builder bsB = Seep.BackupState.newBuilder();
			//Developer needs to save just the state
			Seep.TollCalculatorState tcSbuilt = tcS.build();
			bsB.setTcState(tcSbuilt);
			backupState(bsB);
			setCounter(0);
			
long e = System.currentTimeMillis();
//System.out.println("### Generate BS: "+(e-a));
//System.out.println("### SIZE SENT, BSB: "+bsB.build().getSerializedSize());
//System.out.println("### SIZE SENT, TCS: "+tcSbuilt.getSerializedSize());
		
	}

	@Override
	public int getCounter() {
		return counter;
	}

	@Override
	public void installState(InitState is) {
		//Clean STATE
		speedAvg5Minutes.clear();
		speedAvg4Minutes.clear();
		speedAvg3Minutes.clear();
		speedAvg2Minutes.clear();
		speedAvg1Minutes.clear();
		currentVidSpeed_vid_speed.clear();
		numVidLastMinSeg_seg_numVid.clear();
		consecutiveReports.clear();
		numVidCurrentMinSeg_seg_listVid.clear();
		stoppedCars_pos_vid.clear();
		accidents.clear();
		vid_lastPos.clear();
		
		//INSTALL new state
//		speedAvg5Minutes = new HashMap<Integer, Integer>();
		Seep.TollCalculatorState tcS = is.getTcState();
System.out.println("<>--<> ## INSTALL_size: "+tcS.getSerializedSize());
		Seep.TollCalculatorState.DataII fifth = null;
		for(int i = 0; i < tcS.getSpeedAvg5MinutesCount(); i++){
			fifth = tcS.getSpeedAvg5Minutes(i);
			speedAvg5Minutes.put(fifth.getKey(), fifth.getValue());
		}
//System.out.println("5 compare: "+tcS.getSpeedAvg5MinutesCount()+" with: "+speedAvg5Minutes.keySet().size());
		
//		speedAvg4Minutes = new HashMap<Integer, Integer>();
		Seep.TollCalculatorState.DataII fourth = null;
		for(int i = 0; i < tcS.getSpeedAvg4MinutesCount(); i++){
			fourth = tcS.getSpeedAvg4Minutes(i);
			speedAvg4Minutes.put(fourth.getKey(), fourth.getValue());
		}
//System.out.println("4 compare: "+tcS.getSpeedAvg4MinutesCount()+" with: "+speedAvg4Minutes.keySet().size());
		
//		speedAvg3Minutes = new HashMap<Integer, Integer>();
		Seep.TollCalculatorState.DataII third = null;
		for(int i = 0; i < tcS.getSpeedAvg3MinutesCount(); i++){
			third = tcS.getSpeedAvg3Minutes(i);
			speedAvg3Minutes.put(third.getKey(), third.getValue());
		}
//System.out.println("3 compare: "+tcS.getSpeedAvg3MinutesCount()+" with: "+speedAvg3Minutes.keySet().size());
		
//		speedAvg2Minutes = new HashMap<Integer, Integer>();
		Seep.TollCalculatorState.DataII second = null;
		for(int i = 0; i < tcS.getSpeedAvg2MinutesCount(); i++){
			second = tcS.getSpeedAvg2Minutes(i);
			speedAvg2Minutes.put(second.getKey(), second.getValue());
		}
//System.out.println("2 compare: "+tcS.getSpeedAvg2MinutesCount()+" with: "+speedAvg2Minutes.keySet().size());
		
//		speedAvg1Minutes = new HashMap<Integer, Integer>();
		Seep.TollCalculatorState.DataII first = null;
		for(int i = 0; i < tcS.getSpeedAvg1MinutesCount(); i++){
			first = tcS.getSpeedAvg1Minutes(i);
			speedAvg1Minutes.put(first.getKey(), first.getValue());
		}
//System.out.println("1 compare: "+tcS.getSpeedAvg1MinutesCount()+" with: "+speedAvg1Minutes.keySet().size());
		
//		currentVidSpeed_vid_speed = new HashMap<Integer, Integer>();
		Seep.TollCalculatorState.DataII current = null;
		for(int i = 0; i < tcS.getCurrentVidSpeedVidSpeedCount(); i++){
			current = tcS.getCurrentVidSpeedVidSpeed(i);
			currentVidSpeed_vid_speed.put(current.getKey(), current.getValue());
		}
//System.out.println("current compare: "+tcS.getCurrentVidSpeedVidSpeedCount()+" with: "+currentVidSpeed_vid_speed.keySet().size());
		
//		numVidLastMinSeg_seg_numVid = new HashMap<Integer, Integer>();
		Seep.TollCalculatorState.DataII nvlms = null;
//System.out.println("installing numVidLastMinSeg_seg_numVid: "+ tcS.getNumVidLastMinSegSegNumVidCount());
		for(int i = 0; i < tcS.getNumVidLastMinSegSegNumVidCount(); i++){
//System.out.println("WARNING WARNING");
			nvlms = tcS.getNumVidLastMinSegSegNumVid(i);
			numVidLastMinSeg_seg_numVid.put(nvlms.getKey(), nvlms.getValue());
		}
//System.out.println("numVidLast compare: "+tcS.getNumVidLastMinSegSegNumVidCount()+" with: "+numVidLastMinSeg_seg_numVid.keySet().size());
		
//		consecutiveReports = new HashMap<Integer, Integer>();
		Seep.TollCalculatorState.DataII consecR = null;
		for(int i = 0; i < tcS.getConsecutiveReportsCount(); i++){
			consecR = tcS.getConsecutiveReports(i);
			consecutiveReports.put(consecR.getKey(), consecR.getValue());
		}
//System.out.println("consecRep compare: "+tcS.getConsecutiveReportsCount()+" with: "+consecutiveReports.keySet().size());
	
//		numVidCurrentMinSeg_seg_listVid = new HashMap<Integer, ArrayList<Integer>>();
		
//System.out.println("##################################");
//System.out.println("##################################");
//System.out.println("##################################");

		Seep.TollCalculatorState.DataListI vCurrentSegment = null;
//System.out.println("installing numVidCurrentMinSeg_seg_listVid: "+ tcS.getNumVidCurrentMinSegSegListVidCount());
		for(int i = 0; i < tcS.getNumVidCurrentMinSegSegListVidCount(); i++){
			vCurrentSegment = tcS.getNumVidCurrentMinSegSegListVid(i);
			ArrayList<Integer> aux = new ArrayList<Integer>();
//System.out.println("listSize: "+vCurrentSegment.getValueCount());
			for(int j = 0; j < vCurrentSegment.getValueCount(); j++){
				aux.add(vCurrentSegment.getValue(j));
			}
//System.out.println("SEG: "+vCurrentSegment.getKey());
			numVidCurrentMinSeg_seg_listVid.put(vCurrentSegment.getKey(), aux);
		}
//System.out.println("numVidCurrent compare: "+tcS.getNumVidCurrentMinSegSegListVidCount()+" with: "+numVidCurrentMinSeg_seg_listVid.keySet().size());

//System.out.println("##################################");
//System.out.println("##################################");
//		stoppedCars_pos_vid = new HashMap<Integer, ArrayList<Integer>>();
		Seep.TollCalculatorState.DataListI stopC = null;
		for(int i = 0; i < tcS.getStoppedCarsPosVidCount(); i++){
			stopC = tcS.getStoppedCarsPosVid(i);
			ArrayList<Integer> aux = new ArrayList<Integer>();
			for(int j = 0; j < stopC.getValueCount(); j++){
				aux.add(stopC.getValue(j));
			}
			stoppedCars_pos_vid.put(stopC.getKey(), aux);
		}
//System.out.println("stopeCar compare: "+tcS.getStoppedCarsPosVidCount()+" with: "+stoppedCars_pos_vid.keySet().size());

//		accidents = new HashMap<Integer, Boolean>();
		Seep.TollCalculatorState.DataBool db = null;
		for(int i = 0; i < tcS.getAccidentsCount(); i++){
			db = tcS.getAccidents(i);
			accidents.put(db.getKey(), db.getValue());
		}
//System.out.println("accident compare: "+tcS.getAccidentsCount()+" with: "+accidents.keySet().size());

//System.out.println("INSTALLED ACCIDENTS: "+accidents.containsKey(""));
		
//		vid_lastPos = new HashMap<Integer, Integer>();
		Seep.TollCalculatorState.DataII vehpos = null;
		for(int i = 0; i < tcS.getVidLastPosCount(); i++){
			vehpos = tcS.getVidLastPos(i);
//			VehiclePosition vp = null;
//			for(int j = 0; j < vehpos.getValueCount(); j++){
//				Seep.TollCalculatorState.DataIV.Vehicle a = vehpos.getValue(j);
//				vp = new VehiclePosition(a.getVid(), a.getSegment(),
//										 a.getDir(), a.getLane(),
//										 a.getPos());
//			}
			vid_lastPos.put(vehpos.getKey(), vehpos.getValue());
		}
//System.out.println("vid_lastPos compare: "+tcS.getVidLastPosCount()+" with: "+vid_lastPos.keySet().size());

		
		currentMinute = tcS.getCurrentMinute();
//System.out.println("CURRENT MINUTE INSTALLING "+currentMinute);

		System.out.println("OP"+getOperatorId()+" -> has restored state");
	}

	@Override
	public Builder[] parallelizeState(BackupState toSplit, int key) {
		System.out.println("#######################");
		System.out.println("PARALLEL KEY: "+key);
		System.out.println("#######################");
		BackupState.Builder splitted[] = new BackupState.Builder[2];
		System.out.println("BACKUP BA STATE");
		Seep.BalanceAccountState baS = toSplit.getBaState();
		
		Seep.BackupState.Builder oldBS = BackupState.newBuilder();
		Seep.BackupState.Builder newBS = BackupState.newBuilder();
		Seep.BalanceAccountState.Builder oldBaS = Seep.BalanceAccountState.newBuilder();
		Seep.BalanceAccountState.Builder newBaS = Seep.BalanceAccountState.newBuilder();
		
		//Balance account
		oldBaS.addAllBalanceAccount(baS.getBalanceAccountList());
		newBaS.addAllBalanceAccount(baS.getBalanceAccountList());
		
		//previous segment
		oldBaS.addAllPreviousSegment(baS.getPreviousSegmentList());
		newBaS.addAllPreviousSegment(baS.getPreviousSegmentList());
//		for(Seep.BalanceAccountState.DataII data : baS.getPreviousSegmentList()){
//			oldBaS.addPreviousSegment(data);
//			newBaS.addPreviousSegment(data);
//		}
		
		//previous toll
		oldBaS.addAllPreviousToll(baS.getPreviousTollList());
		newBaS.addAllPreviousToll(baS.getPreviousTollList());
//		for(Seep.BalanceAccountState.DataII data : baS.getPreviousTollList()){
//			oldBaS.addPreviousToll(data);
//			newBaS.addPreviousToll(data);
//		}
		
		oldBaS.setLastUpdateOfBA(baS.getLastUpdateOfBA());
		newBaS.setLastUpdateOfBA(baS.getLastUpdateOfBA());
		
		oldBaS.setStateId(1);
		newBaS.setStateId(1);
		
		oldBS.setBaState(oldBaS);
		newBS.setBaState(newBaS);
		
		splitted[0] = oldBS;
		splitted[1] = newBS;
		
		return splitted;
	}

	@Override
	public boolean isOrderSensitive() {
		//this IS order-sensitive
		return true;
	}
}
