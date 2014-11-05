package uk.ac.imperial.lsds.seep.api.data;

public abstract class Type {
	
	public abstract String toString();
	
	public static final Type BYTE = new Type() {
		
		public String toString(){
			return "BYTE";
		}
	};
	
	public static final Type SHORT = new Type() {
		
		public String toString(){
			return "SHORT";
		}
	};
	
	public static final Type INT = new Type() {
		
		public String toString(){
			return "INT";
		}
	};
	
	public static final Type LONG = new Type() {
		
		public String toString(){
			return "LONG";
		}
	};
	
	public static final Type BYTES = new Type() {
		
		public String toString(){
			return "BYTES";
		}
	};
	
}
