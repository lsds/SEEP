package seep.operator.collection.lrbenchmark;

import java.util.ArrayList;
import java.util.HashMap;

import seep.comm.serialization.DataTuple;
import seep.comm.serialization.controlhelpers.InitState;
import seep.operator.Operator;
import seep.operator.StatefullOperator;
import seep.operator.collection.lrbenchmark.beans.VehiclePosition;

@SuppressWarnings("serial")
public class AccidentDetector extends Operator implements StatefullOperator{

	//Store the stopped cars in every position. position-car
	private HashMap<Integer, ArrayList<Integer>> stoppedCars = new HashMap<Integer, ArrayList<Integer>>(60000);
	//store previous vehicle positions. car-segment
	private HashMap<Integer, VehiclePosition> lastVSegment = new HashMap<Integer, VehiclePosition>(150000);
	//store number of consecutive reports from same segment. vehicle-number_of_reports
	private HashMap<Integer, Integer> consecutiveReports = new HashMap<Integer, Integer>(150000);
	
	//stores per segment if there is an accident (true) or not
	private HashMap<Integer, Boolean> accidents = new HashMap<Integer, Boolean>(100);
	
	public AccidentDetector(int opID) {
		super(opID);
		subclassOperator = this;
	}
	
	public void processData(DataTuple dt){
//		int segment = dt.getSeg();
//		/** AD**/
//		//execute logic for detecting accidents
//		accidentDetector(segment, dt);
//		//if there is an accident in the nearby area
//		if(accidentArea(segment, segment)){
//			//notify the accident with toll=0
//			notifyArea(dt);
//			return;
//		}
		/** AD**/
	}
	
	private boolean accidentArea(int segment, int seg) {
		if(accidents.get(seg) || 
		   accidents.get(seg - 1) || 
		   accidents.get(seg - 2) || 
		   accidents.get(seg - 3)|| 
		   accidents.get(seg - 4)) return true;
		else return false;
	}
	
//	private void accidentDetector(int segment, Seep.DataTuple dt) {
//		//var to work with
//		int vehicle = dt.getVid();
//		int position = dt.getPos();
//		VehiclePosition currentPosition = new VehiclePosition(vehicle, segment, dt.getDir(), dt.getLane(), dt.getPos());
//		//get this vehicle previous position report
//		VehiclePosition prevPosition = lastVSegment.get(vehicle);
//		if(prevPosition != null){
//			//If the car has been previously identified as stopped...
//			if(stoppedCars.get(position) != null && stoppedCars.get(position).indexOf(vehicle) != -1){
//				//if it is in the same position return
//				if(prevPosition.pos == currentPosition.pos){
//					//System.out.println("Vehicle "+vehicle+" was previously stopped, ignore pos report");
//					return;
//				}
//				//otherwise reinitialize the structures...
//				else {
//					System.out.println("Vehicle "+vehicle+" ACCIDENT CLEAR");
//					//it is no longer in an accident
//					ArrayList<Integer> cars = stoppedCars.get(position);
//					//remove from stopped cars
//					cars.remove(vehicle);
//					stoppedCars.put(position, cars);
//					accidents.put(dt.getSeg(), false);
//					return;
//				}
//			}
//			// if the previous position is the same as current one
//			if(prevPosition.pos == currentPosition.pos){
//				//System.out.println("Vehicle: "+vehicle+" reported same consecutive position");
//				// check how many times has reported the same position and increment the counter
//				if(consecutiveReports.get(vehicle) != null){
//					int aux = consecutiveReports.get(vehicle);
//					aux++;
//					// if it has reported 4 times, means it is stopped
//					if(aux == 4){
//						//System.out.println("Vehicle: "+vehicle+" 4 consect same position reports");
//						// check if there is another car stopped in the same position
//						if(stoppedCars.get(position) != null){
//							if (stoppedCars.get(position).size() > 0){
//					
//								System.out.println("Vehicle: "+vehicle+" ACCIDENT");
//								stoppedCars.get(position).add(vehicle);
//								accidents.put(dt.getSeg(), true);
//							}
//							else{
//								//System.out.println("Vehicle: "+vehicle+" is stopped at pos: "+position);
//								ArrayList<Integer> cars = stoppedCars.get(position);
//								cars.add(vehicle);
//								stoppedCars.put(position, cars);
//							}
//						}
//						else{
//							//System.out.println("Vehicle: "+vehicle+" is stopped at pos: "+position);
//							ArrayList<Integer> cars = new ArrayList<Integer>();
//							cars.add(vehicle);
//							stoppedCars.put(position, cars);
//						}
//					}
//					// if not, indicate the number of consecutive position reports
//					else{
//						consecutiveReports.put(vehicle, aux);
//					}
//				}
//				else{
//					//notify of this report
//					consecutiveReports.put(vehicle, 1);
//				}
//			}
//			else{
//				// if not, then update vehicle position
//				lastVSegment.put(vehicle, currentPosition);
//				//reset the consecutive reports
//				consecutiveReports.put(vehicle, 0);
//			}
//		}
//		else{
//			lastVSegment.put(vehicle, currentPosition);
//		}
//	}
	
	private void notifyArea(DataTuple dt) {
//		Seep.DataTuple.Builder event = Seep.DataTuple.newBuilder(dt);
//		// if there is an accident, there is no toll
//		event.setToll(0);
//		//send accident notification
//		event.setType(1);
////System.out.println("Segment: "+dt.getSeg()+" Accident near");
//long a = System.currentTimeMillis();
//		sendDown(event.build());
//long e = System.currentTimeMillis();
//if((e-a) > 5){
//	System.out.println("notify: "+(e-a));
//}
	}
	
	
	@Override
	public void generateBackupState() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getCounter() {
		// TODO Auto-generated method stub
		return 0;
	}

//	@Override
//	public void installState(InitState is) {
//		// TODO Auto-generated method stub
//		
//	}

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

	@Override
	public void installState(InitState is) {
		// TODO Auto-generated method stub
		
	}
	
	
/*	public void processData(Builder dt) {
		//var to work with
		int vehicle = dt.getVid();
		int segment = dt.getSeg();
		int position = dt.getPos();
		VehiclePosition currentPosition = new VehiclePosition(vehicle, segment, dt.getDir(), dt.getLane(), dt.getPos());
		//get this vehicle previous position report
		VehiclePosition prevPosition = lastVSegment.get(vehicle);
		if(prevPosition != null){
			//If the car has been previously identified as stopped...
			if(stoppedCars.get(position) != null) if(stoppedCars.get(position).indexOf(vehicle) != -1){
				//if it is in the same position return
				if(prevPosition.pos == currentPosition.pos){
					System.out.println("Vehicle "+vehicle+" was previously stopped, ignore pos report");
					return;
				}
				//otherwise reinitialize the structures...
				else {
					System.out.println("Vehicle "+vehicle+" is driving again");
					//it is no longer in an accident
					ArrayList<Integer> cars = stoppedCars.get(position);
					cars.add(vehicle);
					stoppedCars.put(position, cars);
					emitTupleDeleteAccident(segment, dt);
					return;
				}
			}
			// if the previous position is the same as current one
			if(prevPosition.pos == currentPosition.pos){
				System.out.println("Vehicle: "+vehicle+" reported same consecutive position");
				// check how many times has reported the same position and increment the counter
				if(consecutiveReports.get(vehicle) != null){
					int aux = consecutiveReports.get(vehicle);
					aux++;
					// if it has reported 4 times, means it is stopped
					if(aux == 4){
						System.out.println("Vehicle: "+vehicle+" 4 consect same position reports");
						// check if there is another car stopped in the same position
						if(stoppedCars.get(position) != null){
							if (stoppedCars.get(position).size() > 0){
					
								System.out.println("Vehicle: "+vehicle+" ACCIDENT");
								stoppedCars.get(position).add(vehicle);
								emitTupleNewAccident(segment, dt);
							}
							else{
								System.out.println("Vehicle: "+vehicle+" is stopped at pos: "+position);
								ArrayList<Integer> cars = stoppedCars.get(position);
								cars.add(vehicle);
								stoppedCars.put(position, cars);
							}
						}
						else{
							System.out.println("Vehicle: "+vehicle+" is stopped at pos: "+position);
							ArrayList<Integer> cars = new ArrayList<Integer>();
							cars.add(vehicle);
							stoppedCars.put(position, cars);
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
				// if not, then update vehicle position
				lastVSegment.put(vehicle, currentPosition);
				//reset the consecutive reports
				consecutiveReports.put(vehicle, 0);
			}
		}
		else{
			lastVSegment.put(vehicle, currentPosition);
		}
	}*/

/*	private void emitTupleDeleteAccident(int segment, Builder dt) {
		//5 is a given type for indicate not accident anymore
		dt.setType(5);
		dt.setSeg(segment);
		sendDown(dt);
	}

	private void emitTupleNewAccident(int segment, Builder dt) {
		dt.setType(1);
		dt.setSeg(segment);
		sendDown(dt);
	}

	private boolean samePosition(VehiclePosition prevPosition, VehiclePosition currentPosition) {
		if(prevPosition.segment == currentPosition.segment &&
			prevPosition.dir == currentPosition.dir &&
			prevPosition.lane == currentPosition.lane &&
			prevPosition.pos == currentPosition.pos){
			return true;
		}
		return false;
	}
*/
	
}
