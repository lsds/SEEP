package uk.ac.imperial.lsds.streamsql.conversion;

public class LongConversion implements NumericConversion<Long> {
	private static final long serialVersionUID = 1L;

	@Override
	public Long fromDouble(double d) {
		return (long) d;
	}

	@Override
	public Long fromString(String str) {
		return Long.valueOf(str);
	}

	@Override
	public double getDistance(Long bigger, Long smaller) {
		return bigger - smaller;
	}

	@Override
	public Long getInitialValue() {
		return 0L;
	}
	
	@Override
	public Long getMinValue() {
		return Long.MIN_VALUE;
	}

	@Override
	public Long getMaxValue() {
		return Long.MAX_VALUE;
	}	

	@Override
	public double toDouble(Object obj) {
		final long value = (Long) obj;
		return value;
	}

	// for printing(debugging) purposes
	@Override
	public String toString() {
		return "LONG";
	}

	@Override
	public String toString(Long obj) {
		return obj.toString();
	}
}