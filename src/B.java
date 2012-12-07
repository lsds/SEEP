import java.util.concurrent.atomic.AtomicInteger;


abstract class B{
		protected B ex;
		
		public AtomicInteger ai = new AtomicInteger();
		
		public B getEx(){
			return ex;
		}
	}