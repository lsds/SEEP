package seep.comm.serialization;

import java.util.ArrayList;

public class DataTuple {

	private long timestamp;
	private int id;
	
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
	
	private int second;
	private int strikePrice;
	private String exchangeId;
	private int expiryDay;
	private int expiryYear;
	private String month;
	private int monthCode;
	
	private int key;
	
	private String xParity;
	
	public String getxParity() {
		return xParity;
	}

	public void setxParity(String xParity) {
		this.xParity = xParity;
	}

	public int getMonthCode() {
		return monthCode;
	}

	public void setMonthCode(int monthCode) {
		this.monthCode = monthCode;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public int getSecond() {
		return second;
	}

	public void setSecond(int second) {
		this.second = second;
	}

	public int getStrikePrice() {
		return strikePrice;
	}

	public void setStrikePrice(int strikePrice) {
		this.strikePrice = strikePrice;
	}

	public String getExchangeId() {
		return exchangeId;
	}

	public void setExchangeId(String exchangeId) {
		this.exchangeId = exchangeId;
	}

	public int getExpiryDay() {
		return expiryDay;
	}

	public void setExpiryDay(int expiryDay) {
		this.expiryDay = expiryDay;
	}

	public int getExpiryYear() {
		return expiryYear;
	}

	public void setExpiryYear(int expiryYear) {
		this.expiryYear = expiryYear;
	}

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public void setKey(int key) {
		this.key = key;
	}

	public int getKey() {
		return key;
	}
	
	
//	private String countryCode;
//	private String article;
//	private int mrValue;
//	
//	private ArrayList<String> top_ccode = new ArrayList<String>();
//	private ArrayList<Integer> top_visits = new ArrayList<Integer>();
//
//	public ArrayList<String> getTopCCode(){
//		return top_ccode;
//	}
//	
//	public ArrayList<Integer> getTopVisits(){
//		return top_visits;
//	}
//	
//	public String getCountryCode() {
//		return countryCode;
//	}
//
//	public void setCountryCode(String countryCode) {
//		this.countryCode = countryCode;
//	}
//
//	public String getArticle() {
//		return article;
//	}
//
//	public void setArticle(String article) {
//		this.article = article;
//	}
//	

//
//	public void setMRValue(int i) {
//		this.mrValue = i;
//	}
//	
//	public int getMRValue(){
//		return mrValue;
//	}
//
//	public void setTop5(String first, int firstVisits, String second, int secondVisits, String third, int thirdVisits, 
//			String fourth, int fourthVisits, String fifth, int fifthVisits) {
//		top_ccode.add(first);
//		top_ccode.add(second);
//		top_ccode.add(third);
//		top_ccode.add(fourth);
//		top_ccode.add(fifth);
//		
//		top_visits.add(firstVisits);
//		top_visits.add(secondVisits);
//		top_visits.add(thirdVisits);
//		top_visits.add(fourthVisits);
//		top_visits.add(fifthVisits);
//	}
	
}
