package uk.ac.imperial.lsds.streamsql.op.gpu.annotations;

import com.amd.aparapi.opencl.OpenCL.GlobalReadWrite;

import java.lang.annotation.Annotation;

public class GlobalReadWriteArgument implements Annotation, GlobalReadWrite {
	
	private String name;
	
	public GlobalReadWriteArgument (String name) {
		this.name = name;	
	}
	
	public String value() { 
		return name; 
	}
	
	public Class<? extends Annotation> annotationType() {
		return this.getClass();
	}
}
