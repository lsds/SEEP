package uk.ac.imperial.lsds.streamsql.op.gpu.deprecated.annotations;

import com.amd.aparapi.opencl.OpenCL.Arg;

import java.lang.annotation.Annotation;

public class KernelArgument implements Annotation, Arg {
	
	private String name;
	
	public KernelArgument (String name) {
		this.name = name;	
	}
	
	public String value() { 
		return name; 
	}
	
	public Class<? extends Annotation> annotationType() {
		return this.getClass();
	}
}
