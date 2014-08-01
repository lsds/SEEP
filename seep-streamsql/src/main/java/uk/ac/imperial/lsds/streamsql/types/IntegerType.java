package uk.ac.imperial.lsds.streamsql.types;

public class IntegerType implements PrimitiveType {

	public int value;
	
	public IntegerType(int value) {
		this.value = value;
	}

	@Override
	public int compareTo(PrimitiveType o) {
		int oI = ((IntegerType)o).value;

		if (value == oI)
			return 0;
		if (value < oI)
			return -1;
		return 1;
	}

	@Override
	public PrimitiveType add(PrimitiveType toAdd) {
		value += ((IntegerType)toAdd).value;
		return this;
	}

	@Override
	public PrimitiveType sub(PrimitiveType toAdd) {
		value -= ((IntegerType)toAdd).value;
		return this;
	}

	@Override
	public PrimitiveType mul(PrimitiveType toAdd) {
		value *= ((IntegerType)toAdd).value;
		return this;
	}

	@Override
	public PrimitiveType div(PrimitiveType toAdd) {
		value = Math.round(1f*value / ((IntegerType)toAdd).value) ;
		return this;
	}
	
	@Override
	public String toString() {
		return ""+value;
	}
	
	@Override
	public void setFromString(String s) {
		value = Integer.parseInt(s);
	}

	@Override
	public PrimitiveType parseFromString(String s) {
		int newValue = Integer.parseInt(s);
		return new IntegerType(newValue);
	}

	@Override
	public int hashCode() {
		return value;
	}

}
