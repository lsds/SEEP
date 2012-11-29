
public class MagicTest {

	public Class composeClass(){
		Class c = new Base().getClass();
		
		return null;
	}
	
	public static void main(String args[]){
	
		MagicTest mt = new MagicTest();
		
		Class composed = mt.composeClass();
		
	}
	
	class Base{
		
	}
}
