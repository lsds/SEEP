package uk.ac.imperial.lsds.streamsql.types;

public class StringType implements PrimitiveType {

	public String value;
	
	public StringType(String value) {
		this.value = value;
	}
	
	@Override
	public Object clone() {
		return new StringType(value);
	}

	@Override
	public int compareTo(PrimitiveType o) {
		return value.compareTo(((StringType)o).value);
	}

	@Override
	public PrimitiveType add(PrimitiveType toAdd) {
		value += ((StringType)toAdd).value;
		return this;
	}

	@Override
	public PrimitiveType sub(PrimitiveType toAdd) {
		throw new UnsupportedOperationException("No substraction for Strings");
	}

	@Override
	public PrimitiveType mul(PrimitiveType toAdd) {
		throw new UnsupportedOperationException("No multiplication for Strings");
	}

	@Override
	public PrimitiveType div(PrimitiveType toAdd) {
		throw new UnsupportedOperationException("No division for Strings");
	}

	public StringType replace(CharSequence string, CharSequence string2) {
		value = value.replace(string, string2);
		return this;
	}

	public boolean contains(StringType string) {
		return value.contains(((StringType)string).value);
	}

	@Override
	public void setFromString(String s) {
		value = s;
	}

	@Override
	public PrimitiveType parseFromString(String s) {
		return new StringType(s);
	}
	
	@Override
	public int hashCode() {
		return value.hashCode();
	}
	
}
