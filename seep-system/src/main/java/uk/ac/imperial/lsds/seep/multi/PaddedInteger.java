package uk.ac.imperial.lsds.seep.multi;
public class PaddedInteger {
	
	public int value;
	
	public PaddedInteger (int value) {
		this.value = value;
	}
	
public volatile int _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12, _13, _14, _15 = 15;
	
	public long dummy () {
		return (_2 + _3 + _4 + _5 + _6 + _7 + _8 + _9 + _10 + _11 + _12 + _13 + _14 + _15);
	}
}

