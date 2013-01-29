package seep.runtimeengine;

public class DataStructureAdapter {

	private DataStructureI dso;
	
	public DataStructureI getDSO(){
		return dso;
	}
	
	public void setDSO(DataStructureI dso){
		this.dso = dso;
	}
	
	public DataStructureAdapter(){
		
	}
}
