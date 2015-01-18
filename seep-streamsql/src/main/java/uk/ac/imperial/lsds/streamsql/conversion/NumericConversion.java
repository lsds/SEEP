package uk.ac.imperial.lsds.streamsql.conversion;

public interface NumericConversion<T extends Number> extends TypeConversion<T> {

	public T getMinValue();
	
	public T getMaxValue();
	
	public T fromDouble(double d);

	public double toDouble(Object obj);

}
