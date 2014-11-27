import java.util.Arrays;


public class CastRunner {

	private static void f1(int[] i) {
		i[0] = 2;
	}

	private static void f2(Integer i) {
		i = new Integer(3);
	}

	
	public static void main(String[] args) {

		
		int[] n = new int[] {0};
		System.out.println(Arrays.toString(n));
		
		f1(n);
		System.out.println(Arrays.toString(n));
		
		
		Integer n2 = 5;
		System.out.println(n2);
		f2(n2);
		System.out.println(n2);
		
		
//		int value = 1000000000;
//		
//		System.out.println("Operations: " + value);
//		Object o = new Integer(1);
//		
//		long start = System.currentTimeMillis();
//		for (int i = 0; i < value; i++) {
//			Integer k = (Integer)o;
//		}
//		long end= System.currentTimeMillis();
//		
//		double secCastToObject = (end - start)/1000.0;
//		System.out.println("Cast Object to Integer: " + secCastToObject);
//
//		start = System.currentTimeMillis();
//		for (int i = 0; i < value; i++) {
//			int k = (int)o;
//		}
//		end= System.currentTimeMillis();
//		double secCastToPrim = (end - start)/1000.0;
//		System.out.println("Cast Object to int: " + secCastToPrim);
//
//		
//		String s = "1";
//		start = System.currentTimeMillis();
//		for (int i = 0; i < value; i++) {
//			Integer k = Integer.valueOf(s);
//		}
//		end= System.currentTimeMillis();
//		
//		double secParseToObject = (end - start)/1000.0;
//
//		System.out.println("Parse Integer from String: " + secParseToObject);
//
//		start = System.currentTimeMillis();
//		for (int i = 0; i < value; i++) {
//			int k = Integer.parseInt(s);
//		}
//		end= System.currentTimeMillis();
//		
//		double secParseToPrim = (end - start)/1000.0;
//
//		System.out.println("Parse int from String: " + secParseToPrim);


	}

}
