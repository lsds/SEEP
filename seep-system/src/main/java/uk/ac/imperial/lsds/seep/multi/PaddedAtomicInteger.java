package uk.ac.imperial.lsds.seep.multi;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("serial")
public class PaddedAtomicInteger extends AtomicInteger {

	public PaddedAtomicInteger (final int value) {
		super(value);
	}
	
	public volatile int _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12, _13, _14, _15 = 15;
	
	public long dummy () {
		return (_2 + _3 + _4 + _5 + _6 + _7 + _8 + _9 + _10 + _11 + _12 + _13 + _14 + _15);
	} 
}
