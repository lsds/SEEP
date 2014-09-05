package uk.ac.imperial.lsds.seep.gpu.types;

public class StringType implements PrimitiveType {

	private String value;
	
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
//		value += ((StringType)toAdd).value;
		return new StringType(value + ((StringType)toAdd).value);
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
//		value = value.replace(string, string2);
		return new StringType(value.replace(string, string2));
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
