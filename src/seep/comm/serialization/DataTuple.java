package seep.comm.serialization;

public class DataTuple {

	private long timestamp;
	private int id;
	private String countryCode;
	private String article;
	private int mrValue;
	
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
	
}
