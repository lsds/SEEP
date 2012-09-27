package seep.comm.serialization;

public class DataTuple {

	private long timestamp;
	private int id;
	private String a = "as;ldfkalsdfqweriouqweklgnj;qlwedgn;lasdkhg;lahsd;glkjasdl;fjk;alsdkjf;qowjeo;qjief;lakns,lmngfaskl;dhago;sdhg;pawoelrkjqwef;jqlwe";
	private String b = "as;ldfkalsdfqweriouqweklgnj;qlasdfdkhg;lahsd;glkjasdl;fjk;alsdkjf;qowjeo;qjief;lakns,lmngfaskl;dhago;sdhg;pawoelrkjqwef;jqlwe";
	private String d = "as;ldfkalsdfqweriouqweklgnj;qlwedgn;lasdkhg;lahsd;glkjasdl;fjk;alsdkjf;qowjesdfakns,lmngfaskl;dhago;sdhg;pawoelrkjqwef;jqlwe";
	private String e = "as;ldfkalsdfqweriouqweklgnj;qlwedgn;lasdkhg;lahsd;glkjasdl;fjk;alssdfqowjeo;qjief;lakns,lmngfaskl;dhago;sdhg;pawoelrkjqwef;jqlwe";
	private String f = "as;lsdfqweriouqweklgnj;qlwedgn;lasdkhg;lahsd;glkjasdl;fjk;alsdkjf;qowjeo;qjief;lakns,lmngfaskl;dhago;sdhg;pawoelrkjqwef;jqlwe";
	private String g = "as;ldfkalsdfqweriouqweklgnj;qlwedgn;lasdkhg;lahsd;glkjasdl;fjk;alsdkjf;qowjeo;qjief;sdfaskl;dhago;sdhg;pawoelrkjqwef;jqlwe";
	private String h = "as;ldfkalsdfqweriouqweklgnj;qlwedgn;lasalkjasdl;fjk;alsdkjf;qowjeo;qjief;lakns,lmngfaskl;dhago;sdhg;pawoelrkjqwef;jqlwe";
	private String i = "as;ldfkalsdfqweriouqweklgnj;qlwedgn;lasdkhg;lahsdfalsdkjf;qowjeo;qjief;lakns,lmngfaskl;dhago;sdhg;pawoelrkjqwef;jqlwe";
//	private String j = "as;lsdfsdfqweriouqweklgnj;qlwedgn;lasdkhg;lahsd;glkjasdl;fjk;alsdkjf;qowjeo;qjief;lakns,lmngfaskl;dhago;sdhg;pawoelrkjqwef;jqlwe";
//	private String k = "as;lfsqweriouqweklgnj;qlwedgn;lasdkhg;lahsd;glkjasdl;fjk;alsdkjf;qowjeo;qjief;lakns,lmngfaskl;dhago;sdhg;pawoelrkjqwef;jqlwe";
//	private String l = "as;ldfkasfeklgnj;qlwedgn;lasdkhg;lahsd;ajk;alsdkjf;qowjeo;qjief;lakns,lmngfaskl;dhago;sdhg;pawoelrkjqwef;jqlwe";
//	private String m = "as;ldfkalsdfqweriouqsdfsddgn;lasdkhg;lahsd;glkjasdl;fjk;alsdkjf;qowjeo;qjief;lakns,dfsdfsdhg;pawoelrkjqwef;jqlwe";
//	private String n = "as;ldfkalsdfqweriouqweklgnj;qlwedgn;lasdkhg;lahsd;glkjasdl;fjk;alsdkjf;qowjeo;qjief;lakns,lmngfassdflmngfaskl;dhago;sdhg;pawoelrkjqwef;jqlwe";
//	private String p = "as;ldfkalsdfqweriouqweklgnj;qlwedgn;lasdkhg;lahsd;glkjasdl;fjk;alsdkjf;qowjeo;qjief;lakns,lmngsdfgo;sdhg;pawoelrkjqwef;jqlwe";
//	private String q = "as;ldfkalsdfqweriouqweklgnj;qlwedgn;lasdkhg;lahsd;glkjasdl;fjk;alsdkjf;qowjeo;qjief;lakns,lmngfasdfdhg;pawoelrkjqwef;jqlwe";
//	private String r = "as;ldfkalsdfqweriouqweklgnsdfn;lasdkhg;lahsd;glkjasdl;fjk;alsdkjf;qowjeo;qjief;lakns,lmngfaskl;dhago;sdhg;pawoelrkjqwef;jqlwe";
//	private String ao = "as;ldfkalsdfqweriouqweklgnj;qlwedgn;lasdkhg;lahsd;glkjasdl;fjk;alsdkjf;qowjeo;qjief;lakns,lmngfaskl;dhago;sdhg;pawoelrkjqwef;jqlwe";
//	private String bo= "as;ldfkalsdfqweriouqweklgnj;qlasdfdkhg;lahsd;glkjasdl;fjk;alsdkjf;qowjeo;qjief;lakns,lmngfaskl;dhago;sdhg;pawoelrkjqwef;jqlwe";
//	private String doo = "as;ldfkalsdfqweriouqweklgnj;qlwedgn;lasdkhg;lahsd;glkjasdl;fjk;alsdkjf;qowjesdfakns,lmngfaskl;dhago;sdhg;pawoelrkjqwef;jqlwe";
//	private String eo = "as;ldfkalsdfqweriouqweklgnj;qlwedgn;lasdkhg;lahsd;glkjasdl;fjk;alssdfqowjeo;qjief;lakns,lmngfaskl;dhago;sdhg;pawoelrkjqwef;jqlwe";
//	private String fo = "as;lsdfqweriouqweklgnj;qlwedgn;lasdkhg;lahsd;glkjasdl;fjk;alsdkjf;qowjeo;qjief;lakns,lmngfaskl;dhago;sdhg;pawoelrkjqwef;jqlwe";
//	private String go = "as;ldfkalsdfqweriouqweklgnj;qlwedgn;lasdkhg;lahsd;glkjasdl;fjk;alsdkjf;qowjeo;qjief;sdfaskl;dhago;sdhg;pawoelrkjqwef;jqlwe";
//	private String ho = "as;ldfkalsdfqweriouqweklgnj;qlwedgn;lasalkjasdl;fjk;alsdkjf;qowjeo;qjief;lakns,lmngfaskl;dhago;sdhg;pawoelrkjqwef;jqlwe";
//	private String io = "as;ldfkalsdfqweriouqweklgnj;qlwedgn;lasdkhg;lahsdfalsdkjf;qowjeo;qjief;lakns,lmngfaskl;dhago;sdhg;pawoelrkjqwef;jqlwe";
//	private String jo = "as;lsdfsdfqweriouqweklgnj;qlwedgn;lasdkhg;lahsd;glkjasdl;fjk;alsdkjf;qowjeo;qjief;lakns,lmngfaskl;dhago;sdhg;pawoelrkjqwef;jqlwe";
//	private String ko = "as;lfsqweriouqweklgnj;qlwedgn;lasdkhg;lahsd;glkjasdl;fjk;alsdkjf;qowjeo;qjief;lakns,lmngfaskl;dhago;sdhg;pawoelrkjqwef;jqlwe";
//	private String lo = "as;ldfkasfeklgnj;qlwedgn;lasdkhg;lahsd;ajk;alsdkjf;qowjeo;qjief;lakns,lmngfaskl;dhago;sdhg;pawoelrkjqwef;jqlwe";
//	private String mo = "as;ldfkalsdfqweriouqsdfsddgn;lasdkhg;lahsd;glkjasdl;fjk;alsdkjf;qowjeo;qjief;lakns,dfsdfsdhg;pawoelrkjqwef;jqlwe";
//	private String no = "as;ldfkalsdfqweriouqweklgnj;qlwedgn;lasdkhg;lahsd;glkjasdl;fjk;alsdkjf;qowjeo;qjief;lakns,lmngfassdflmngfaskl;dhago;sdhg;pawoelrkjqwef;jqlwe";
//	private String po = "as;ldfkalsdfqweriouqweklgnj;qlwedgn;lasdkhg;lahsd;glkjasdl;fjk;alsdkjf;qowjeo;qjief;lakns,lmngsdfgo;sdhg;pawoelrkjqwef;jqlwe";
//	private String qo = "as;ldfkalsdfqweriouqweklgnj;qlwedgn;lasdkhg;lahsd;glkjasdl;fjk;alsdkjf;qowjeo;qjief;lakns,lmngfasdfdhg;pawoelrkjqwef;jqlwe";
//	private String ro = "as;ldfkalsdfqweriouqweklgnsdfn;lasdkhg;lahsd;glkjasdl;fjk;alsdkjf;qowjeo;qjief;lakns,lmngfaskl;dhago;sdhg;pawoelrkjqwef;jqlwe";
	
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
	
}
