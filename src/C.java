class C extends B{
		private String hello = "Hello";
		
		public String getHello(){
			return hello;
		}
		
		public C(){
			ex = this;
		}

	}