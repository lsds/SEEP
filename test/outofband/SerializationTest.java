package outofband;
/*******************************************************************************
 * Copyright (c) 2013 Imperial College London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial design and implementation
 ******************************************************************************/
public class SerializationTest {

	public int test(Object...objects ){
		return objects.length;
	}
	
	
	public static void main(String args[]){
		
		SerializationTest st = new SerializationTest();
		System.out.println("I put 5 inst objects");
		int a=st.test(0, 2, 3, 5, 6);
		System.out.println("I receive "+a+" objects");
		
		System.out.println("I put 4 objects (3 null)");
		int b = st.test(0, null, null, null);
		System.out.println("I receive "+b+" objects");
		
		
	}
}
