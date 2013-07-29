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
class Th implements Runnable{

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

//	private B b;
//	private C cc;
//	
////	public Th(B c){
////		this.b = c;
////	}
//	
//	public Th(C c){
//		this.cc = c;
//	}
//	
//	@Override
//	public void run() {
//		while(true){
//			// pick lock for 500 sec, and release for 1000 sec again
//			//synchronized(b.getEx()){
//			//synchronized(cc){
//			cc.ai.compareAndSet(0, 2);
//			if(cc.ai.get() == 2){
//				try {
//					for(int i = 0; i<5; i++){
//						System.out.println("Proc in background...");
//						Thread.sleep(200);
//					}
//				}	 
//				catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				//Set free again
//				cc.ai.set(0);
//				synchronized(cc){
//					cc.notify();
//				}
//			}
//			else{
//				synchronized(cc){
////					try {
////						cc.wait();
////					} catch (InterruptedException e) {
////						// TODO Auto-generated catch block
////						e.printStackTrace();
////					}
//				}
//			}
//			try {
//				Thread.sleep(2000);
//			} 
//			catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	}
}
