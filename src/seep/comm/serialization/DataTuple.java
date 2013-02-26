package seep.comm.serialization;

import java.io.Serializable;
import java.util.ArrayList;

public class DataTuple implements Serializable{

	private static final long serialVersionUID = 1L;

	private int tupleSchemaId;
	private long timestamp;
	private int id;
	
	// Required empty constructor for Kryo serialization
	public DataTuple(){
		
	}

	public int getTupleSchemaId(){
		return tupleSchemaId;
	}
	
	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public void setId(int id){
		this.id = id;
	}
	
	public int getId(){
		return id;
	}
	
	public DataTuple(int tupleSchemaId){
		this.tupleSchemaId = tupleSchemaId;
	}
	
	public DataTuple(int tupleSchemaId, long timestamp, int id){
		this.tupleSchemaId = tupleSchemaId;
		this.timestamp = timestamp;
		this.id = id;
	}


	//Ad-hoc for experimentation
	
	
	private int userId;
	private int itemId;
	private int rating;
	private boolean requiresUpdate;
	private ArrayList userVector;
	private ArrayList partialRecommendationVector;
	private ArrayList noRec;
	private int recommendation;
	
	public boolean getRequiresUpdate(){
		return requiresUpdate;
	}
	
	public void setRequiresUpdate(boolean requiresUpdate){
		this.requiresUpdate = requiresUpdate;
	}
	
	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}
	
	public ArrayList getUserVector(){
		return userVector;
	}
	
	public void setUserVector(ArrayList userVector){
		this.userVector = userVector;
	}
	
	public ArrayList getPartialRecommendationVector(){
		return partialRecommendationVector;
	}
	
	public void setPartialRecommendationVector(ArrayList partialRecommendationVector){
		this.partialRecommendationVector = partialRecommendationVector;
	}
	
	public ArrayList getNoRec(){
		return noRec;
	}
	
	public void setNoRec(ArrayList noRec){
		this.noRec = noRec;
	}
	
	public int getRecommendation(){
		return recommendation;
	}
	
	public void setRecommendation(int recommendation){
		this.recommendation = recommendation;
	}
}