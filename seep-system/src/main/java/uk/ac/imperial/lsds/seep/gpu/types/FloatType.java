package uk.ac.imperial.lsds.seep.gpu.types;

public class FloatType implements PrimitiveType {

	public float value;
	
	public FloatType(float value) {
		this.value = value;
	}

	@Override
	public int compareTo(PrimitiveType o) {
		float oI = ((FloatType)o).value;

		if (value == oI)
			return 0;
		if (value < oI)
			return -1;
		return 1;
	}

	@Override
	public PrimitiveType add(PrimitiveType toAdd) {
		value += ((FloatType)toAdd).value;
		return this;
	}

	@Override
	public PrimitiveType sub(PrimitiveType toAdd) {
		value -= ((FloatType)toAdd).value;
		return this;
	}

	@Override
	public PrimitiveType mul(PrimitiveType toAdd) {
		value *= ((FloatType)toAdd).value;
		return this;
	}

	@Override
	public PrimitiveType div(PrimitiveType toAdd) {
		value /= ((FloatType)toAdd).value;
		return this;
	}
	
	@Override
	public String toString() {
		return ""+value;
	}

	@Override
	public void setFromString(String s) {
		value = Float.parseFloat(s);
	}

	@Override
	public PrimitiveType parseFromString(String s) {
		float newValue = Float.parseFloat(s);
		return new FloatType(newValue);
	}
	
	@Override
	public int hashCode(){
		return Float.floatToIntBits(value);
	}

}
