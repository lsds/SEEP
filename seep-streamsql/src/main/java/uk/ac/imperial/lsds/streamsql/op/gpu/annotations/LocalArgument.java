package uk.ac.imperial.lsds.streamsql.op.gpu.annotations;

import com.amd.aparapi.opencl.OpenCL.Local;

import java.lang.annotation.Annotation;

public class LocalArgument implements Annotation, Local {
	
	private String name;
	
	public LocalArgument (String name) {
		this.name = name;	
	}
	
	@Override
	public String value() { 
		return name; 
	}
	
	@Override
	public Class<? extends Annotation> annotationType() {
		return this.getClass();
	}
}
