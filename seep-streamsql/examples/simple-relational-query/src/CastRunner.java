
public class CastRunner {

	public static void main(String[] args) {
		
		int value = 1000000000;
		
		System.out.println("Operations: " + value);
		Object o = new Integer(1);
		
		long start = System.currentTimeMillis();
		for (int i = 0; i < value; i++) {
			Integer k = (Integer)o;
		}
		long end= System.currentTimeMillis();
		
		double secCastToObject = (end - start)/1000.0;
		System.out.println("Cast Object to Integer: " + secCastToObject);

		start = System.currentTimeMillis();
		for (int i = 0; i < value; i++) {
			int k = (int)o;
		}
		end= System.currentTimeMillis();
		double secCastToPrim = (end - start)/1000.0;
		System.out.println("Cast Object to int: " + secCastToPrim);

		
		String s = "1";
		start = System.currentTimeMillis();
		for (int i = 0; i < value; i++) {
			Integer k = Integer.valueOf(s);
		}
		end= System.currentTimeMillis();
		
		double secParseToObject = (end - start)/1000.0;

		System.out.println("Parse Integer from String: " + secParseToObject);

		start = System.currentTimeMillis();
		for (int i = 0; i < value; i++) {
			int k = Integer.parseInt(s);
		}
		end= System.currentTimeMillis();
		
		double secParseToPrim = (end - start)/1000.0;

		System.out.println("Parse int from String: " + secParseToPrim);


	}

}
