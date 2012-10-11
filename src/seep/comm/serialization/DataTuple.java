package seep.comm.serialization;

import java.util.ArrayList;

public class DataTuple {

	private long timestamp;
	private int id;
	private String countryCode;
	private String article;
	private int mrValue;
	
	private ArrayList<String> top_ccode = new ArrayList<String>();
	private ArrayList<Integer> top_visits = new ArrayList<Integer>();

	public ArrayList<String> getTopCCode(){
		return top_ccode;
	}
	
	public ArrayList<Integer> getTopVisits(){
		return top_visits;
	}
	
	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getArticle() {
		return article;
	}

	public void setArticle(String article) {
		this.article = article;
	}
	
	public long getTs() {
		return timestamp;
	}

	public void setTs(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public void setId(int id){
		this.id = id;
	}
	
	public int getId(){
		return id;
	}
	
	public DataTuple(){
		
	}
	
	public DataTuple(long timestamp, int id){
		this.timestamp = timestamp;
		this.id = id;
	}

	public void setMRValue(int i) {
		this.mrValue = i;
	}
	
	public int getMRValue(){
		return mrValue;
	}

	public void setTop5(String first, int firstVisits, String second, int secondVisits, String third, int thirdVisits, 
			String fourth, int fourthVisits, String fifth, int fifthVisits) {
		top_ccode.add(first);
		top_ccode.add(second);
		top_ccode.add(third);
		top_ccode.add(fourth);
		top_ccode.add(fifth);
		
		top_visits.add(firstVisits);
		top_visits.add(secondVisits);
		top_visits.add(thirdVisits);
		top_visits.add(fourthVisits);
		top_visits.add(fifthVisits);
	}
	
}
