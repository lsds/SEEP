package uk.ac.imperial.lsds.streamsql.conversion;

import java.io.Serializable;

public interface TypeConversion<T> extends Serializable {
	public T fromString(String str);

	// bigger - smaller
	public double getDistance(T bigger, T smaller);

	public T getInitialValue();

	public String toString(T obj);
}