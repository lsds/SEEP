/*******************************************************************************
 * Copyright (c) 2014 Imperial College London
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial API and implementation
 ******************************************************************************/
package uk.ac.imperial.lsds.java2sdg;

public class ComputeCallGraph {
//	PhaseOptions.v().setPhaseOption("cg", "on"); // Construct a call graph
//	PhaseOptions.v().setPhaseOption("wjtp", "on"); // jimple transformation pack
//	PhaseOptions.v().setPhaseOption("wjap", "on"); // jimple annotation pack
	//PhaseOptions.v().setPhaseOption("wjop", "on"); // jimple optimization pack
//	PhaseOptions.v().setPhaseOption("tag.fieldrw", "on"); // tag read/write access to fields
//	PhaseOptions.v().setPhaseOption("jap.fieldrw", "on"); // process read/write access to fields

//	Scene.v().addBasicClass("java.lang.Object", SootClass.BODIES);
//	Scene.v().addBasicClass("java.util.HashMap", SootClass.BODIES);
//	Scene.v().addBasicClass("java.util.AbstractMap", SootClass.BODIES);
	
//	String arguments[] = {"-allow-phantom-refs", "-w", "-app", // whole program analysis and ignore missing classes
//			"-p", "cg.spark", "enabled", "-annot-side-effect", //stuff to annotate file with fieldRW
//			"-print-tags-in-output", "-f", "J", className}; // output in jimple and print tags
//	soot.Main.main(arguments);
//	
//	CallGraph cg = Scene.v().getCallGraph();
	
//	System.out.println("CALL GRAPH");
//	
//	Iterator<MethodOrMethodContext> methods = cg.sourceMethods();
//	while(methods.hasNext()){
//		MethodOrMethodContext current = methods.next();
//		String decl = current.method().getName();
////		System.out.println(decl);
//		if(decl.equals("simpleReadAndWriteState")){
//			System.out.println("MATCH");
//			System.out.println(current.method().toString());
//			List<Tag> tags = current.method().getTags();
//			Iterator<Tag> iTags = tags.iterator();
//			while(iTags.hasNext()){
//				Tag t = iTags.next();
//				System.out.println(t.getName()+": "+t.toString());
//			}
//			System.out.println(current.method().getTag("FieldReadTag"));
//		}
//		
////		System.out.println();
//	}

}
